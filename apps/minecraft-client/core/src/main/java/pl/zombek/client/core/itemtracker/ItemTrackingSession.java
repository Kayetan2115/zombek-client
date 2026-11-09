package pl.zombek.client.core.itemtracker;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Encapsulates a distinct tracking session during gameplay.
 */
public final class ItemTrackingSession {

    private final String sessionId;
    private final long startTimeEpoch;
    private final String worldId;
    private final boolean singleplayer;

    private boolean active = true;
    private boolean paused = false;
    private long pausedDurationMillis = 0L;
    private long lastPauseStartEpoch = 0L;

    private final Map<String, ItemTrackingStatistics> itemStats = new ConcurrentHashMap<>();
    private final ItemTrackingHistory history;

    public ItemTrackingSession(String sessionId, String worldId, boolean singleplayer, int maxHistoryCapacity) {
        this.sessionId = sessionId != null ? sessionId : UUID.randomUUID().toString();
        this.startTimeEpoch = System.currentTimeMillis();
        this.worldId = worldId;
        this.singleplayer = singleplayer;
        this.history = new ItemTrackingHistory(maxHistoryCapacity);
    }

    public void pause() {
        if (!paused) {
            paused = true;
            lastPauseStartEpoch = System.currentTimeMillis();
        }
    }

    public void resume() {
        if (paused) {
            paused = false;
            if (lastPauseStartEpoch > 0) {
                pausedDurationMillis += (System.currentTimeMillis() - lastPauseStartEpoch);
                lastPauseStartEpoch = 0L;
            }
        }
    }

    public void recordEvent(ItemTrackEvent event) {
        if (!active || paused || event == null) return;
        history.addEvent(event);
        ItemTrackingStatistics stats = itemStats.computeIfAbsent(
                event.getItemId(),
                id -> new ItemTrackingStatistics(id, event.getItemName())
        );
        stats.recordEvent(event);
    }

    public ItemTrackingStatistics getStatistics(String itemId) {
        return itemStats.get(itemId);
    }

    public Map<String, ItemTrackingStatistics> getAllStatistics() {
        return Collections.unmodifiableMap(itemStats);
    }

    public ItemTrackingHistory getHistory() {
        return history;
    }

    public String getSessionId() { return sessionId; }
    public long getStartTimeEpoch() { return startTimeEpoch; }
    public String getWorldId() { return worldId; }
    public boolean isSingleplayer() { return singleplayer; }
    public boolean isActive() { return active; }
    public boolean isPaused() { return paused; }

    public void stop() {
        this.active = false;
    }
}
