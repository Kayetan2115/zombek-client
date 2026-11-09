package pl.zombek.client.core.bedwars;

import java.util.*;

/**
 * Tracks recent Bedwars game events for display in the Event Feed HUD panel.
 * Handles event expiration, deduplication, and fade transitions.
 */
public class BedwarsEventTracker {
    private static BedwarsEventTracker instance;

    private boolean enabled = true;
    private int maxEvents = 5;
    private float eventDurationSeconds = 8.0f;
    private boolean showTimestamps = true;
    private boolean showIcons = true;
    private boolean fadeAnimation = true;

    private final List<BedwarsEvent> recentEvents = new ArrayList<>();
    private final Map<String, Long> deduplicationCache = new HashMap<>();
    private static final long DEDUP_WINDOW_MS = 3000L;

    public BedwarsEventTracker() {}

    public static synchronized BedwarsEventTracker getInstance() {
        if (instance == null) {
            instance = new BedwarsEventTracker();
        }
        return instance;
    }

    public synchronized void addEvent(BedwarsEvent event) {
        if (!enabled || event == null) {
            return;
        }

        long now = System.currentTimeMillis();
        pruneDeduplicationCache(now);

        // Deduplication check
        String eventKey = event.getId();
        Long lastTime = deduplicationCache.get(eventKey);
        if (lastTime != null && (now - lastTime) < DEDUP_WINDOW_MS) {
            return;
        }
        deduplicationCache.put(eventKey, now);

        recentEvents.add(0, event); // Insert at head (newest first)
        trimToMaxEvents();
    }

    public synchronized void pruneExpiredEvents() {
        long now = System.currentTimeMillis();
        long maxAgeMs = (long) (eventDurationSeconds * 1000.0f);
        recentEvents.removeIf(event -> (now - event.getTimestampMs()) > maxAgeMs);
    }

    private void trimToMaxEvents() {
        while (recentEvents.size() > maxEvents) {
            recentEvents.remove(recentEvents.size() - 1);
        }
    }

    private void pruneDeduplicationCache(long now) {
        deduplicationCache.entrySet().removeIf(entry -> (now - entry.getValue()) > DEDUP_WINDOW_MS * 2);
    }

    public synchronized List<BedwarsEvent> getVisibleEvents() {
        pruneExpiredEvents();
        return Collections.unmodifiableList(new ArrayList<>(recentEvents));
    }

    public synchronized void clear() {
        recentEvents.clear();
        deduplicationCache.clear();
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getMaxEvents() { return maxEvents; }
    public void setMaxEvents(int maxEvents) { this.maxEvents = Math.max(1, Math.min(20, maxEvents)); trimToMaxEvents(); }
    public float getEventDurationSeconds() { return eventDurationSeconds; }
    public void setEventDurationSeconds(float duration) { this.eventDurationSeconds = Math.max(1.0f, duration); }
    public boolean isShowTimestamps() { return showTimestamps; }
    public void setShowTimestamps(boolean showTimestamps) { this.showTimestamps = showTimestamps; }
    public boolean isShowIcons() { return showIcons; }
    public void setShowIcons(boolean showIcons) { this.showIcons = showIcons; }
    public boolean isFadeAnimation() { return fadeAnimation; }
    public void setFadeAnimation(boolean fadeAnimation) { this.fadeAnimation = fadeAnimation; }
}
