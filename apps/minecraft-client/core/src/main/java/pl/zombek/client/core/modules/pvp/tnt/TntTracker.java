package pl.zombek.client.core.modules.pvp.tnt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe registry and cache for active client-side Primed TNT entities.
 */
public class TntTracker {
    private static final TntTracker INSTANCE = new TntTracker();

    private final Map<Integer, TrackedEntry> entries = new ConcurrentHashMap<>();

    private TntTracker() {}

    public static TntTracker getInstance() {
        return INSTANCE;
    }

    public void updateTnt(int entityId, double x, double y, double z, int fuseTicks, double distance, boolean visible) {
        entries.put(entityId, new TrackedEntry(
                new TntEntityData(entityId, x, y, z, fuseTicks, distance, visible),
                System.currentTimeMillis()
        ));
    }

    public void removeTnt(int entityId) {
        entries.remove(entityId);
    }

    public void pruneStale(long maxAgeMs) {
        long now = System.currentTimeMillis();
        entries.entrySet().removeIf(e -> now - e.getValue().lastSeenMs > maxAgeMs);
    }

    public void clear() {
        entries.clear();
    }

    public List<TntEntityData> getActiveTntList() {
        List<TntEntityData> list = new ArrayList<>(entries.size());
        for (TrackedEntry entry : entries.values()) {
            list.add(entry.data);
        }
        return list;
    }

    public int getActiveCount() {
        return entries.size();
    }

    private static class TrackedEntry {
        final TntEntityData data;
        final long lastSeenMs;

        TrackedEntry(TntEntityData data, long lastSeenMs) {
            this.data = data;
            this.lastSeenMs = lastSeenMs;
        }
    }
}
