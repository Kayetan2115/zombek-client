package pl.zombek.client.core.time;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles safe, throttled persistence of playtime history, accumulated client runtime,
 * and stopwatch states to config/zombekclient/time_data.json.
 * Strictly adheres to atomic writes and corrupt file recovery.
 */
public class TimePersistenceManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimePersistenceManager.class);
    private static final int SCHEMA_VERSION = 1;
    private static final long MIN_SAVE_INTERVAL_MS = 30_000L; // Throttle to at most once per 30s

    private final Path configDir;
    private final Path dataFile;
    private final Gson gson;
    private final Object ioLock = new Object();

    private long lastSaveTimeMillis = 0L;

    public TimePersistenceManager(Path configDir) {
        this.configDir = configDir;
        this.dataFile = configDir.resolve("time_data.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public Path getDataFile() {
        return dataFile;
    }

    /**
     * Loads saved time data and restores accumulated stats and stopwatch profiles.
     */
    public void load(ZombekTimeService timeService) {
        synchronized (ioLock) {
            if (!Files.exists(dataFile)) {
                LOGGER.info("[TimePersistence] No existing time_data.json found. Using defaults.");
                return;
            }

            try (Reader reader = Files.newBufferedReader(dataFile, StandardCharsets.UTF_8)) {
                JsonElement rootElement = JsonParser.parseReader(reader);
                if (!rootElement.isJsonObject()) {
                    throw new JsonParseException("Root element of time_data.json is not a JSON object");
                }
                JsonObject root = rootElement.getAsJsonObject();

                // 1. Total historical client playtime
                if (root.has("totalHistoricalPlaytimeSeconds")) {
                    timeService.setHistoricalClientPlaytimeSeconds(root.get("totalHistoricalPlaytimeSeconds").getAsLong());
                }

                // 2. Playtime session history
                if (root.has("sessions") && root.get("sessions").isJsonArray()) {
                    List<PlaytimeSessionRecord> loadedSessions = new ArrayList<>();
                    for (JsonElement item : root.getAsJsonArray("sessions")) {
                        if (item.isJsonObject()) {
                            JsonObject s = item.getAsJsonObject();
                            loadedSessions.add(new PlaytimeSessionRecord(
                                    s.has("sessionId") ? s.get("sessionId").getAsString() : "unknown",
                                    s.has("startTimeEpoch") ? s.get("startTimeEpoch").getAsLong() : 0L,
                                    s.has("endTimeEpoch") ? s.get("endTimeEpoch").getAsLong() : 0L,
                                    s.has("durationSeconds") ? s.get("durationSeconds").getAsLong() : 0L,
                                    s.has("singleplayer") && s.get("singleplayer").getAsBoolean(),
                                    s.has("worldName") && !s.get("worldName").isJsonNull() ? s.get("worldName").getAsString() : null,
                                    s.has("serverAddress") && !s.get("serverAddress").isJsonNull() ? s.get("serverAddress").getAsString() : null
                            ));
                        }
                    }
                    timeService.restoreSessionHistory(loadedSessions);
                }

                // 3. Stopwatch profiles
                if (root.has("stopwatchProfiles") && root.get("stopwatchProfiles").isJsonObject()) {
                    JsonObject profs = root.getAsJsonObject("stopwatchProfiles");
                    for (Map.Entry<String, JsonElement> entry : profs.entrySet()) {
                        if (entry.getValue().isJsonObject()) {
                            JsonObject p = entry.getValue().getAsJsonObject();
                            String stateStr = p.has("state") ? p.get("state").getAsString() : "STOPPED";
                            long elapsedNanos = p.has("elapsedNanos") ? p.get("elapsedNanos").getAsLong() : 0L;
                            StopwatchProfile profile = timeService.getStopwatchProfile(entry.getKey());
                            if (profile != null) {
                                StopwatchState st = StopwatchState.valueOf(stateStr);
                                // If saved as RUNNING, restore as PAUSED so time does not skew while game was closed
                                if (st == StopwatchState.RUNNING) {
                                    st = StopwatchState.PAUSED;
                                }
                                profile.restoreState(st, elapsedNanos);
                            }
                        }
                    }
                }

                LOGGER.info("[TimePersistence] Successfully loaded time_data.json.");
            } catch (Exception e) {
                LOGGER.error("[TimePersistence] Failed to parse time_data.json. Backing up to time_data.json.bak", e);
                backupCorruptFile();
            }
        }
    }

    /**
     * Saves time data with throttling. If force=false and interval hasn't passed, returns immediately.
     */
    public void save(ZombekTimeService timeService, boolean force) {
        long now = System.currentTimeMillis();
        if (!force && (now - lastSaveTimeMillis < MIN_SAVE_INTERVAL_MS)) {
            return;
        }

        synchronized (ioLock) {
            try {
                if (!Files.exists(configDir)) {
                    Files.createDirectories(configDir);
                }

                JsonObject root = new JsonObject();
                root.addProperty("schemaVersion", SCHEMA_VERSION);
                root.addProperty("savedAtEpoch", now);
                root.addProperty("totalHistoricalPlaytimeSeconds", timeService.getTotalClientPlaytimeSeconds());

                // Sessions
                JsonArray sessionArr = new JsonArray();
                for (PlaytimeSessionRecord record : timeService.getSessionHistory()) {
                    JsonObject s = new JsonObject();
                    s.addProperty("sessionId", record.getSessionId());
                    s.addProperty("startTimeEpoch", record.getStartTimeEpoch());
                    s.addProperty("endTimeEpoch", record.getEndTimeEpoch());
                    s.addProperty("durationSeconds", record.getDurationSeconds());
                    s.addProperty("singleplayer", record.isSingleplayer());
                    if (record.getWorldName() != null) {
                        s.addProperty("worldName", record.getWorldName());
                    }
                    if (record.getServerAddress() != null) {
                        s.addProperty("serverAddress", record.getServerAddress());
                    }
                    sessionArr.add(s);
                }
                root.add("sessions", sessionArr);

                // Stopwatch profiles
                JsonObject profs = new JsonObject();
                for (StopwatchProfile profile : timeService.getAllStopwatchProfiles()) {
                    JsonObject p = new JsonObject();
                    p.addProperty("state", profile.getState().name());
                    p.addProperty("elapsedNanos", profile.getElapsedNanos());
                    profs.add(profile.getName(), p);
                }
                root.add("stopwatchProfiles", profs);

                // Atomic write via temp file
                Path tempFile = configDir.resolve("time_data.json.tmp");
                try (Writer writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
                    gson.toJson(root, writer);
                }

                try {
                    Files.move(tempFile, dataFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(tempFile, dataFile, StandardCopyOption.REPLACE_EXISTING);
                }

                this.lastSaveTimeMillis = now;
            } catch (IOException e) {
                LOGGER.error("[TimePersistence] Failed to write time_data.json", e);
            }
        }
    }

    private void backupCorruptFile() {
        try {
            Path bak = configDir.resolve("time_data.json.bak");
            if (Files.exists(dataFile)) {
                Files.move(dataFile, bak, StandardCopyOption.REPLACE_EXISTING);
                LOGGER.warn("[TimePersistence] Corrupt time_data.json preserved as time_data.json.bak");
            }
        } catch (IOException ioException) {
            LOGGER.error("[TimePersistence] Failed to rename corrupt time_data.json", ioException);
        }
    }
}
