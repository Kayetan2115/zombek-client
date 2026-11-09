package pl.zombek.client.core.inventory;

import java.util.*;

/**
 * Service comparing successive inventory snapshots to detect true item additions,
 * subtractions, and pure internal slot movements.
 */
public class InventorySnapshotService {

    /**
     * Compares previous and current snapshots across the specified inventory sources.
     */
    public SnapshotDiffResult compare(InventorySnapshot previous,
                                      InventorySnapshot current,
                                      Set<InventorySlotSource> allowedSources) {
        SnapshotDiffResult result = new SnapshotDiffResult();

        if (current == null) {
            return result;
        }

        Set<InventorySlotSource> sources = (allowedSources != null && !allowedSources.isEmpty())
                ? allowedSources
                : EnumSet.allOf(InventorySlotSource.class);

        if (previous == null) {
            // Initial snapshot: everything present is considered initial state, no net delta
            return result;
        }

        // 1. Gather all item IDs from both snapshots
        Map<String, Integer> prevCounts = previous.getCountsByItemId(sources);
        Map<String, Integer> currCounts = current.getCountsByItemId(sources);

        Set<String> allItemIds = new LinkedHashSet<>();
        allItemIds.addAll(prevCounts.keySet());
        allItemIds.addAll(currCounts.keySet());

        for (String itemId : allItemIds) {
            int prev = prevCounts.getOrDefault(itemId, 0);
            int curr = currCounts.getOrDefault(itemId, 0);
            int delta = curr - prev;

            if (delta != 0) {
                InventoryItemData sample = current.findFirst(itemId);
                if (sample == null) {
                    sample = previous.findFirst(itemId);
                }
                String name = sample != null ? sample.getItemName() : itemId;
                result.recordNetChange(itemId, name, delta);
            }
        }

        // 2. Detect slot transfers when total count did not change
        List<InventoryItemData> prevItems = previous.getItemsFromSources(sources);
        List<InventoryItemData> currItems = current.getItemsFromSources(sources);

        Map<Integer, InventoryItemData> prevSlotMap = new HashMap<>();
        for (InventoryItemData item : prevItems) {
            prevSlotMap.put(item.getSlotIndex(), item);
        }

        Map<Integer, InventoryItemData> currSlotMap = new HashMap<>();
        for (InventoryItemData item : currItems) {
            currSlotMap.put(item.getSlotIndex(), item);
        }

        // If a slot changed its item and the previous item moved to another slot:
        for (Map.Entry<Integer, InventoryItemData> entry : prevSlotMap.entrySet()) {
            int slot = entry.getKey();
            InventoryItemData prevItem = entry.getValue();
            InventoryItemData currAtSameSlot = currSlotMap.get(slot);

            // Slot was vacated or replaced
            if (currAtSameSlot == null || !currAtSameSlot.getItemId().equals(prevItem.getItemId())) {
                // Find where prevItem moved
                for (Map.Entry<Integer, InventoryItemData> otherEntry : currSlotMap.entrySet()) {
                    if (otherEntry.getKey() != slot && otherEntry.getValue().getItemId().equals(prevItem.getItemId())) {
                        // Found a matching item in another slot
                        result.recordTransfer(new SnapshotDiffResult.SlotTransfer(
                                prevItem.getItemId(),
                                slot,
                                prevItem.getSource(),
                                otherEntry.getKey(),
                                otherEntry.getValue().getSource(),
                                otherEntry.getValue().getCount()
                        ));
                        break;
                    }
                }
            }
        }

        return result;
    }
}
