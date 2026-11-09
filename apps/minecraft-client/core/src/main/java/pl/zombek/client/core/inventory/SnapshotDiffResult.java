package pl.zombek.client.core.inventory;

import java.util.*;

/**
 * Result of comparing two consecutive inventory snapshots.
 * Distinguishes slot transfers, splits, and merges from true item acquisitions or losses.
 */
public final class SnapshotDiffResult {

    public static final class SlotTransfer {
        private final String itemId;
        private final int fromSlot;
        private final InventorySlotSource fromSource;
        private final int toSlot;
        private final InventorySlotSource toSource;
        private final int count;

        public SlotTransfer(String itemId, int fromSlot, InventorySlotSource fromSource, int toSlot, InventorySlotSource toSource, int count) {
            this.itemId = itemId;
            this.fromSlot = fromSlot;
            this.fromSource = fromSource;
            this.toSlot = toSlot;
            this.toSource = toSource;
            this.count = count;
        }

        public String getItemId() { return itemId; }
        public int getFromSlot() { return fromSlot; }
        public InventorySlotSource getFromSource() { return fromSource; }
        public int getToSlot() { return toSlot; }
        public InventorySlotSource getToSource() { return toSource; }
        public int getCount() { return count; }
    }

    private final Map<String, Integer> netChanges = new LinkedHashMap<>();
    private final List<SlotTransfer> slotTransfers = new ArrayList<>();
    private final Set<String> modifiedItemNames = new LinkedHashMap<String, String>().keySet();
    private final Map<String, String> itemDisplayNames = new HashMap<>();

    public SnapshotDiffResult() {}

    public void recordNetChange(String itemId, String itemName, int delta) {
        if (itemId != null && delta != 0) {
            netChanges.put(itemId, netChanges.getOrDefault(itemId, 0) + delta);
            if (itemName != null) {
                itemDisplayNames.put(itemId, itemName);
            }
        }
    }

    public void recordTransfer(SlotTransfer transfer) {
        if (transfer != null) {
            slotTransfers.add(transfer);
        }
    }

    public Map<String, Integer> getNetChanges() {
        return Collections.unmodifiableMap(netChanges);
    }

    public List<SlotTransfer> getSlotTransfers() {
        return Collections.unmodifiableList(slotTransfers);
    }

    public boolean hasNetChanges() {
        for (int delta : netChanges.values()) {
            if (delta != 0) return true;
        }
        return false;
    }

    public boolean isPureTransferOnly() {
        return !hasNetChanges() && !slotTransfers.isEmpty();
    }

    public int getNetChange(String itemId) {
        return netChanges.getOrDefault(itemId, 0);
    }

    public String getItemDisplayName(String itemId) {
        return itemDisplayNames.getOrDefault(itemId, itemId);
    }
}
