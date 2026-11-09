package pl.zombek.client.core.time;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Central time service orchestrating wall-clock, monotonic session clocks,
 * Minecraft world time, stopwatch profiles, and playtime history persistence.
 */
public class ZombekTimeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZombekTimeService.class);
    private static final int MAX_HISTORY_RECORDS = 100;

    private static ZombekTimeService instance;

    private ITimeSource timeSource;
    private TimePersistenceManager persistenceManager;

    // Client Session (Monotonic)
    private final long clientStartMonotonicNanos;
    private long historicalClientPlaytimeSeconds = 0L;

    // World Session (Monotonic)
    private boolean worldSessionActive = false;
    private long worldSessionStartMonotonicNanos = 0L;
    private long worldSessionStartWallEpoch = 0L;
    private String currentWorldName = null;
    private String currentServerAddress = null;
    private boolean currentIsSingleplayer = true;
    private String currentDimensionId = "minecraft:overworld";

    // World Time & Day Tracking
    private long lastObservedGameTime = -1L;
    private long lastObservedDayTime = -1L;
    private boolean timeResetDetected = false;

    // Stopwatch Profiles
    private final Map<String, StopwatchProfile> stopwatchProfiles = new LinkedHashMap<>();
    private String activeStopwatchProfileName = "Speedrun";

    // Playtime Session History
    private final List<PlaytimeSessionRecord> sessionHistory = new ArrayList<>();
    private boolean saveSessionHistoryEnabled = true;
    private boolean saveServerNameOptIn = false;
    private boolean saveWorldNameOptIn = true;

    private ZombekTimeService(ITimeSource timeSource) {
        this.timeSource = timeSource != null ? timeSource : new SystemTimeSource();
        this.clientStartMonotonicNanos = this.timeSource.getMonotonicNanos();

        // Initialize standard named stopwatch profiles
        addStopwatchProfile(new StopwatchProfile("Speedrun", StopwatchProfileType.SPEEDRUN, this.timeSource));
        addStopwatchProfile(new StopwatchProfile("Building", StopwatchProfileType.BUILDING, this.timeSource));
        addStopwatchProfile(new StopwatchProfile("Mining", StopwatchProfileType.MINING, this.timeSource));
        addStopwatchProfile(new StopwatchProfile("Custom", StopwatchProfileType.CUSTOM, this.timeSource));
    }

    public static synchronized ZombekTimeService getInstance() {
        if (instance == null) {
            instance = new ZombekTimeService(new SystemTimeSource());
        }
        return instance;
    }

    public static synchronized void init(Path configDir) {
        ZombekTimeService svc = getInstance();
        if (configDir != null) {
            svc.persistenceManager = new TimePersistenceManager(configDir);
            svc.persistenceManager.load(svc);
        }
    }

    public static synchronized void resetForTesting(ITimeSource customSource) {
        if (instance != null) {
            instance.shutdown();
        }
        instance = new ZombekTimeService(customSource != null ? customSource : new MockTimeSource());
    }

    public synchronized void setTimeSourceForTesting(ITimeSource source) {
        this.timeSource = source != null ? source : new SystemTimeSource();
        for (StopwatchProfile p : stopwatchProfiles.values()) {
            p.setTimeSource(this.timeSource);
        }
    }

    public ITimeSource getTimeSource() {
        return timeSource;
    }

    // ==========================================
    // 1. Real World Clock (Wall Clock)
    // ==========================================

    public LocalTime getLocalTime() {
        return timeSource.getLocalTime();
    }

    public LocalDate getLocalDate() {
        return timeSource.getLocalDate();
    }

    public ZonedDateTime getZonedDateTime() {
        return timeSource.getZonedDateTime();
    }

    // ==========================================
    // 2. Client & World Sessions (Monotonic)
    // ==========================================

    public long getClientSessionSeconds() {
        long elapsedNanos = timeSource.getMonotonicNanos() - clientStartMonotonicNanos;
        return Math.max(0L, elapsedNanos / 1_000_000_000L);
    }

    public long getTotalClientPlaytimeSeconds() {
        return historicalClientPlaytimeSeconds + getClientSessionSeconds();
    }

    public synchronized void setHistoricalClientPlaytimeSeconds(long seconds) {
        this.historicalClientPlaytimeSeconds = Math.max(0L, seconds);
    }

    public synchronized boolean isWorldSessionActive() {
        return worldSessionActive;
    }

    public synchronized long getWorldSessionSeconds() {
        if (!worldSessionActive) return 0L;
        long elapsedNanos = timeSource.getMonotonicNanos() - worldSessionStartMonotonicNanos;
        return Math.max(0L, elapsedNanos / 1_000_000_000L);
    }

    public synchronized void onWorldJoin(String worldName, String serverAddress, boolean singleplayer) {
        long nowNanos = timeSource.getMonotonicNanos();
        long nowWall = timeSource.getWallTimeMillis();

        if (worldSessionActive) {
            // Conclude previous session if any
            recordCurrentSessionToHistory(nowNanos, nowWall);
        }

        this.worldSessionActive = true;
        this.worldSessionStartMonotonicNanos = nowNanos;
        this.worldSessionStartWallEpoch = nowWall;
        this.currentWorldName = worldName;
        this.currentServerAddress = serverAddress;
        this.currentIsSingleplayer = singleplayer;
        this.timeResetDetected = false;
        this.lastObservedGameTime = -1L;
        this.lastObservedDayTime = -1L;

        LOGGER.info("[TimeService] World session started (SP={}, World={}, Server={})",
                singleplayer, worldName, saveServerNameOptIn ? serverAddress : "<hidden>");
    }

    public synchronized void onWorldLeave() {
        if (!worldSessionActive) return;

        long nowNanos = timeSource.getMonotonicNanos();
        long nowWall = timeSource.getWallTimeMillis();
        recordCurrentSessionToHistory(nowNanos, nowWall);

        this.worldSessionActive = false;
        this.currentWorldName = null;
        this.currentServerAddress = null;

        if (persistenceManager != null) {
            persistenceManager.save(this, false);
        }
        LOGGER.info("[TimeService] World session ended.");
    }

    public synchronized void onDimensionChange(String newDimensionId) {
        this.currentDimensionId = newDimensionId != null ? newDimensionId : "minecraft:overworld";
    }

    public String getCurrentDimensionId() {
        return currentDimensionId;
    }

    private void recordCurrentSessionToHistory(long endNanos, long endWallEpoch) {
        long duration = Math.max(0L, (endNanos - worldSessionStartMonotonicNanos) / 1_000_000_000L);
        // Only record sessions longer than 5 seconds
        if (saveSessionHistoryEnabled && duration >= 5L) {
            String sanitizedWorld = saveWorldNameOptIn ? currentWorldName : null;
            String sanitizedServer = saveServerNameOptIn ? currentServerAddress : null;

            PlaytimeSessionRecord record = new PlaytimeSessionRecord(
                    UUID.randomUUID().toString(),
                    worldSessionStartWallEpoch,
                    endWallEpoch,
                    duration,
                    currentIsSingleplayer,
                    sanitizedWorld,
                    sanitizedServer
            );

            if (sessionHistory.size() >= MAX_HISTORY_RECORDS) {
                sessionHistory.remove(0); // Bound memory
            }
            sessionHistory.add(record);
        }
    }

    // ==========================================
    // 3. Minecraft World Time & Day Counter
    // ==========================================

    public ITimeDataProvider getTimeDataProvider() {
        return TimeDataProviderRegistry.getInstance().getProvider();
    }

    public void updateWorldTime(long dayTime, long gameTime) {
        if (gameTime >= 0 && lastObservedGameTime >= 0) {
            // Detect if game time rolled backwards
            if (gameTime < lastObservedGameTime) {
                this.timeResetDetected = true;
            }
        }
        this.lastObservedDayTime = dayTime;
        this.lastObservedGameTime = gameTime;
    }

    public boolean isTimeResetDetected() {
        return timeResetDetected;
    }

    public void resetTimeResetFlag() {
        this.timeResetDetected = false;
    }

    /**
     * Calculates in-game day from total Minecraft game time ticks (24000 ticks/day).
     * Day 1 starts at 0 ticks. Returns -1 if gameTime < 0.
     */
    public static long calculateWorldDay(long gameTime, long manualOffset) {
        if (gameTime < 0) return -1L;
        long day = (gameTime / 24000L) + 1L;
        return Math.max(0L, day + manualOffset);
    }

    /**
     * Converts Minecraft day ticks [0..24000) into 24-hour hours (0-23).
     * In Minecraft, 0 ticks corresponds to 06:00.
     */
    public static int calculateMinecraftHours(long ticks) {
        long normalized = Math.floorMod(ticks, 24000L);
        return (int) ((normalized / 1000L + 6L) % 24L);
    }

    /**
     * Converts Minecraft day ticks [0..24000) into minutes (0-59).
     */
    public static int calculateMinecraftMinutes(long ticks) {
        long normalized = Math.floorMod(ticks, 24000L);
        return (int) (((normalized % 1000L) * 60L) / 1000L);
    }

    public static String formatMinecraftTime(long ticks, boolean is24h, boolean showAmPm, boolean leadingZero) {
        int hours = calculateMinecraftHours(ticks);
        int minutes = calculateMinecraftMinutes(ticks);

        if (is24h) {
            if (leadingZero) {
                return String.format("%02d:%02d", hours, minutes);
            } else {
                return String.format("%d:%02d", hours, minutes);
            }
        } else {
            int h12 = hours % 12;
            if (h12 == 0) h12 = 12;
            String ampm = hours >= 12 ? "PM" : "AM";
            String base = leadingZero ? String.format("%02d:%02d", h12, minutes) : String.format("%d:%02d", h12, minutes);
            return showAmPm ? base + " " + ampm : base;
        }
    }

    public static String formatWorldAge(long gameTime) {
        if (gameTime < 0) return "N/A";
        long days = gameTime / 24000L;
        long remainingTicks = gameTime % 24000L;
        int hours = (int) (remainingTicks / 1000L);
        return String.format("%dd %dh", days, hours);
    }

    // ==========================================
    // 4. Stopwatch Profiles
    // ==========================================

    public void addStopwatchProfile(StopwatchProfile profile) {
        if (profile != null) {
            stopwatchProfiles.put(profile.getName(), profile);
        }
    }

    public StopwatchProfile getStopwatchProfile(String name) {
        return stopwatchProfiles.get(name);
    }

    public Collection<StopwatchProfile> getAllStopwatchProfiles() {
        return Collections.unmodifiableCollection(stopwatchProfiles.values());
    }

    public StopwatchProfile getActiveStopwatchProfile() {
        StopwatchProfile profile = stopwatchProfiles.get(activeStopwatchProfileName);
        if (profile == null && !stopwatchProfiles.isEmpty()) {
            profile = stopwatchProfiles.values().iterator().next();
        }
        return profile;
    }

    public void setActiveStopwatchProfile(String name) {
        if (stopwatchProfiles.containsKey(name)) {
            this.activeStopwatchProfileName = name;
        }
    }

    public String getActiveStopwatchProfileName() {
        return activeStopwatchProfileName;
    }

    // ==========================================
    // 5. Playtime History & Statistics
    // ==========================================

    public synchronized List<PlaytimeSessionRecord> getSessionHistory() {
        return Collections.unmodifiableList(new ArrayList<>(sessionHistory));
    }

    public synchronized void restoreSessionHistory(List<PlaytimeSessionRecord> records) {
        this.sessionHistory.clear();
        if (records != null) {
            for (PlaytimeSessionRecord r : records) {
                if (this.sessionHistory.size() < MAX_HISTORY_RECORDS) {
                    this.sessionHistory.add(r);
                }
            }
        }
    }

    public synchronized void clearSessionHistory() {
        this.sessionHistory.clear();
        if (persistenceManager != null) {
            persistenceManager.save(this, true);
        }
    }

    public synchronized void deleteSessionRecord(String sessionId) {
        if (sessionId != null) {
            this.sessionHistory.removeIf(r -> sessionId.equals(r.getSessionId()));
            if (persistenceManager != null) {
                persistenceManager.save(this, true);
            }
        }
    }

    public synchronized void clearCurrentSession() {
        this.worldSessionStartMonotonicNanos = timeSource.getMonotonicNanos();
        this.worldSessionStartWallEpoch = timeSource.getWallTimeMillis();
    }

    public synchronized PlaytimeStats getPlaytimeStats() {
        long currentSec = getWorldSessionSeconds();
        long totalHistorical = historicalClientPlaytimeSeconds;
        long totalCurrentClient = totalHistorical + getClientSessionSeconds();

        if (sessionHistory.isEmpty()) {
            return new PlaytimeStats(totalCurrentClient, currentSec, currentSec, currentSec > 0 ? 1 : 0, currentSec);
        }

        long sumDuration = 0L;
        long maxDuration = 0L;
        for (PlaytimeSessionRecord r : sessionHistory) {
            sumDuration += r.getDurationSeconds();
            if (r.getDurationSeconds() > maxDuration) {
                maxDuration = r.getDurationSeconds();
            }
        }
        if (currentSec > maxDuration) {
            maxDuration = currentSec;
        }

        int count = sessionHistory.size() + (currentSec > 0 ? 1 : 0);
        long avg = count > 0 ? (sumDuration + currentSec) / count : 0L;

        return new PlaytimeStats(totalCurrentClient, avg, maxDuration, count, currentSec);
    }

    public boolean isSaveSessionHistoryEnabled() {
        return saveSessionHistoryEnabled;
    }

    public void setSaveSessionHistoryEnabled(boolean enabled) {
        this.saveSessionHistoryEnabled = enabled;
    }

    public boolean isSaveServerNameOptIn() {
        return saveServerNameOptIn;
    }

    public void setSaveServerNameOptIn(boolean saveServerNameOptIn) {
        this.saveServerNameOptIn = saveServerNameOptIn;
    }

    public boolean isSaveWorldNameOptIn() {
        return saveWorldNameOptIn;
    }

    public void setSaveWorldNameOptIn(boolean saveWorldNameOptIn) {
        this.saveWorldNameOptIn = saveWorldNameOptIn;
    }

    // ==========================================
    // 6. Tick & Shutdown Lifecycle
    // ==========================================

    public void onClientTick() {
        // Sync world time from active provider
        ITimeDataProvider provider = getTimeDataProvider();
        if (provider != null && provider.isWorldLoaded()) {
            if (!worldSessionActive) {
                onWorldJoin(provider.getWorldName(), provider.getServerAddress(), provider.isSingleplayer());
            }
            if (provider.isTimeAvailable()) {
                updateWorldTime(provider.getWorldTimeOfDay(), provider.getWorldGameTime());
            }
            onDimensionChange(provider.getDimensionId());
        } else {
            if (worldSessionActive) {
                onWorldLeave();
            }
        }

        // Throttled auto-save
        if (persistenceManager != null) {
            persistenceManager.save(this, false);
        }
    }

    public void shutdown() {
        if (worldSessionActive) {
            onWorldLeave();
        }
        if (persistenceManager != null) {
            persistenceManager.save(this, true);
        }
    }
}
