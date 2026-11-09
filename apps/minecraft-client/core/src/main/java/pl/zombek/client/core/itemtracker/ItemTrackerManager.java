package pl.zombek.client.core.itemtracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.zombek.client.core.inventory.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Coordinator for tracking inventory changes across gameplay sessions.
 * Compares snapshots from ZombekInventoryService and classifies events by source and confidence.
 */
public final class ItemTrackerManager implements ZombekInventoryService.InventoryChangeListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemTrackerManager.class);
    private static final ItemTrackerManager INSTANCE = new ItemTrackerManager();

    public static ItemTrackerManager getInstance() {
        return INSTANCE;
    }

    private static class PendingGameAction {
        final String itemId;
        final int delta;
        final ItemEventType type;
        final long timestampEpoch;

        PendingGameAction(String itemId, int delta, ItemEventType type) {
            this.itemId = itemId;
            this.delta = delta;
            this.type = type;
            this.timestampEpoch = System.currentTimeMillis();
        }
    }

    private final Queue<PendingGameAction> pendingActions = new ConcurrentLinkedQueue<>();
    private final Set<InventorySlotSource> activeSources = EnumSet.of(
            InventorySlotSource.HOTBAR,
            InventorySlotSource.MAIN_INVENTORY,
            InventorySlotSource.OFFHAND
    );

    private ItemTrackingSession currentSession = new ItemTrackingSession("default", null, true, 100);
    private boolean trackingEnabled = true;

    private ItemTrackerManager() {
        ZombekInventoryService.getInstance().addChangeListener(this);
    }

    public synchronized ItemTrackingSession getCurrentSession() {
        return currentSession;
    }

    public synchronized void startNewSession(String worldId, boolean singleplayer) {
        LOGGER.info("[ItemTrackerManager] Starting new tracking session for world: {} (singleplayer={})", worldId, singleplayer);
        if (currentSession != null) {
            currentSession.stop();
        }
        currentSession = new ItemTrackingSession(UUID.randomUUID().toString(), worldId, singleplayer, 100);
        pendingActions.clear();
    }

    public synchronized void resetSession() {
        if (currentSession != null) {
            startNewSession(currentSession.getWorldId(), currentSession.isSingleplayer());
        }
    }

    public void pauseTracking() {
        if (currentSession != null) {
            currentSession.pause();
        }
    }

    public void resumeTracking() {
        if (currentSession != null) {
            currentSession.resume();
        }
    }

    public void setTrackingEnabled(boolean trackingEnabled) {
        this.trackingEnabled = trackingEnabled;
    }

    public boolean isTrackingEnabled() {
        return trackingEnabled;
    }

    public void setSourceTracked(InventorySlotSource source, boolean tracked) {
        if (source != null) {
            if (tracked) {
                activeSources.add(source);
            } else {
                activeSources.remove(source);
            }
        }
    }

    public Set<InventorySlotSource> getActiveSources() {
        return Collections.unmodifiableSet(activeSources);
    }

    /**
     * Registers a verified in-game client event (e.g. eating, throwing item, placing block).
     * Used to promote detected quantity changes to CONFIRMED status.
     */
    public void recordVerifiedAction(String itemId, int delta, ItemEventType type) {
        if (itemId == null || delta == 0 || type == null) return;
        pendingActions.add(new PendingGameAction(itemId, delta, type));
    }

    @Override
    public void onInventoryChanged(InventorySnapshot previous, InventorySnapshot current) {
        if (!trackingEnabled || currentSession == null || !currentSession.isActive() || currentSession.isPaused()) {
            return;
        }

        SnapshotDiffResult diff = ZombekInventoryService.getInstance().getSnapshotService()
                .compare(previous, current, activeSources);

        // Update current counts for all items in statistics
        for (Map.Entry<String, Integer> countEntry : current.getCountsByItemId(activeSources).entrySet()) {
            ItemTrackingStatistics stats = currentSession.getStatistics(countEntry.getKey());
            if (stats != null) {
                stats.updateCurrentAmount(countEntry.getValue());
            }
        }

        if (!diff.hasNetChanges()) {
            // Pure internal movement (slot transfer, split or merge without count change).
            // Do NOT record gain or loss!
            return;
        }

        long now = System.currentTimeMillis();
        cleanStalePendingActions(now);

        for (Map.Entry<String, Integer> change : diff.getNetChanges().entrySet()) {
            String itemId = change.getKey();
            int delta = change.getValue();
            String itemName = diff.getItemDisplayName(itemId);

            // Attempt to pair with a verified game action within recent window
            PendingGameAction matchedAction = matchPendingAction(itemId, delta);

            ItemEventType eventType;
            EventConfidence confidence;

            if (matchedAction != null) {
                eventType = matchedAction.type;
                confidence = EventConfidence.CONFIRMED;
            } else {
                eventType = ItemEventType.UNKNOWN_CHANGE;
                confidence = EventConfidence.UNKNOWN;
            }

            ItemTrackEvent event = new ItemTrackEvent(
                    now,
                    itemId,
                    itemName,
                    delta,
                    eventType,
                    confidence,
                    null // uses default formatted description
            );

            currentSession.recordEvent(event);
        }
    }

    private PendingGameAction matchPendingAction(String itemId, int delta) {
        Iterator<PendingGameAction> it = pendingActions.iterator();
        while (it.hasNext()) {
            PendingGameAction action = it.next();
            if (matchesId(action.itemId, itemId) && (Integer.signum(action.delta) == Integer.signum(delta))) {
                it.remove();
                return action;
            }
        }
        return null;
    }

    private void cleanStalePendingActions(long now) {
        pendingActions.removeIf(action -> (now - action.timestampEpoch > 2500L));
    }

    private boolean matchesId(String a, String b) {
        if (a == null || b == null) return false;
        String idA = a.contains(":") ? a : "minecraft:" + a;
        String idB = b.contains(":") ? b : "minecraft:" + b;
        return idA.equalsIgnoreCase(idB);
    }

    public synchronized void resetForTesting() {
        pendingActions.clear();
        currentSession = new ItemTrackingSession("test", null, true, 50);
        trackingEnabled = true;
        ZombekInventoryService.getInstance().removeChangeListener(this);
        ZombekInventoryService.getInstance().addChangeListener(this);
    }
}
