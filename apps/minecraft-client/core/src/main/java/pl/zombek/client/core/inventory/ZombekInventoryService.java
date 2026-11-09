package pl.zombek.client.core.inventory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Central inventory coordination engine.
 * Serves as the single shared source of truth for both Item Counter and Item Tracker,
 * providing cached, throttled snapshot capture to avoid reading inventory on every render frame.
 */
public final class ZombekInventoryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZombekInventoryService.class);
    private static final ZombekInventoryService INSTANCE = new ZombekInventoryService();

    public interface InventoryChangeListener {
        void onInventoryChanged(InventorySnapshot previous, InventorySnapshot current);
    }

    private final List<InventoryChangeListener> changeListeners = new CopyOnWriteArrayList<>();
    private final InventorySnapshotService snapshotService = new InventorySnapshotService();

    private volatile InventorySnapshot currentSnapshot = null;
    private volatile InventorySnapshot previousSnapshot = null;
    private volatile boolean dirty = true;
    private long lastCaptureTimeMillis = 0L;
    private static final long MIN_CAPTURE_INTERVAL_MS = 50L; // Max 20 captures per sec (1 per tick)

    private ZombekInventoryService() {}

    public static ZombekInventoryService getInstance() {
        return INSTANCE;
    }

    public void addChangeListener(InventoryChangeListener listener) {
        if (listener != null) {
            changeListeners.add(listener);
        }
    }

    public void removeChangeListener(InventoryChangeListener listener) {
        changeListeners.remove(listener);
    }

    public void invalidateCache() {
        this.dirty = true;
    }

    /**
     * Gets the current inventory snapshot, capturing on-demand if cache is invalid or dirty.
     */
    public synchronized InventorySnapshot getSnapshot() {
        if (currentSnapshot == null || dirty) {
            refreshSnapshot(System.currentTimeMillis());
        }
        return currentSnapshot != null ? currentSnapshot : createEmptySnapshot();
    }

    public synchronized void onClientTick() {
        refreshSnapshot(System.currentTimeMillis());
    }

    private void refreshSnapshot(long now) {
        IInventoryDataProvider provider = InventoryDataProviderRegistry.getInstance().getProvider();
        if (!provider.isPlayerAvailable()) {
            if (currentSnapshot != null && !currentSnapshot.getItems().isEmpty()) {
                previousSnapshot = currentSnapshot;
                currentSnapshot = createEmptySnapshot();
                notifyListeners(previousSnapshot, currentSnapshot);
            }
            dirty = false;
            lastCaptureTimeMillis = now;
            return;
        }

        List<InventoryItemData> items = provider.captureInventoryItems();
        String worldId = provider.getCurrentWorldId();

        previousSnapshot = currentSnapshot;
        currentSnapshot = new InventorySnapshot(now, worldId, items);
        dirty = false;
        lastCaptureTimeMillis = now;

        if (previousSnapshot != null) {
            notifyListeners(previousSnapshot, currentSnapshot);
        }
    }

    private void notifyListeners(InventorySnapshot prev, InventorySnapshot curr) {
        for (InventoryChangeListener listener : changeListeners) {
            try {
                listener.onInventoryChanged(prev, curr);
            } catch (Throwable t) {
                LOGGER.error("Error notifying inventory change listener: {}", t.getMessage(), t);
            }
        }
    }

    public synchronized void onWorldLeave() {
        LOGGER.info("[ZombekInventoryService] Player left world. Clearing inventory cache.");
        this.previousSnapshot = null;
        this.currentSnapshot = null;
        this.dirty = true;
    }

    public synchronized void onWorldJoin(String worldId) {
        LOGGER.info("[ZombekInventoryService] Player joined world: {}. Invalidating inventory cache.", worldId);
        this.previousSnapshot = null;
        this.currentSnapshot = null;
        this.dirty = true;
    }

    public int getItemCount(String itemId, Set<InventorySlotSource> sources) {
        return getSnapshot().getItemCount(itemId, sources);
    }

    public InventoryItemData findItemData(String itemId) {
        return getSnapshot().findFirst(itemId);
    }

    public InventorySnapshotService getSnapshotService() {
        return snapshotService;
    }

    private InventorySnapshot createEmptySnapshot() {
        return new InventorySnapshot(System.currentTimeMillis(), null, Collections.emptyList());
    }

    public synchronized void resetForTesting() {
        this.currentSnapshot = null;
        this.previousSnapshot = null;
        this.dirty = true;
        this.lastCaptureTimeMillis = 0L;
        this.changeListeners.clear();
    }
}
