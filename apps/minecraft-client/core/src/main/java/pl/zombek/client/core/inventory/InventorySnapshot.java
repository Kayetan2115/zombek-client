package pl.zombek.client.core.inventory;

import java.util.*;

/**
 * Immutable snapshot of player inventory at a specific point in time.
 */
public final class InventorySnapshot {

    private final long timestampEpoch;
    private final String worldId;
    private final List<InventoryItemData> items;

    public InventorySnapshot(long timestampEpoch, String worldId, List<InventoryItemData> items) {
        this.timestampEpoch = timestampEpoch;
        this.worldId = worldId;
        this.items = items != null ? Collections.unmodifiableList(new ArrayList<>(items)) : Collections.emptyList();
    }

    public long getTimestampEpoch() {
        return timestampEpoch;
    }

    public String getWorldId() {
        return worldId;
    }

    public List<InventoryItemData> getItems() {
        return items;
    }

    public List<InventoryItemData> getItemsFromSources(Set<InventorySlotSource> sources) {
        if (sources == null || sources.isEmpty()) {
            return Collections.emptyList();
        }
        List<InventoryItemData> filtered = new ArrayList<>();
        for (InventoryItemData item : items) {
            if (sources.contains(item.getSource())) {
                filtered.add(item);
            }
        }
        return Collections.unmodifiableList(filtered);
    }

    /**
     * Calculates total quantity of a specific item ID across allowed inventory sources.
     */
    public int getItemCount(String itemId, Set<InventorySlotSource> sources) {
        if (itemId == null || sources == null || sources.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (InventoryItemData item : items) {
            if (sources.contains(item.getSource()) && matchesId(item.getItemId(), itemId)) {
                total += item.getCount();
            }
        }
        return total;
    }

    /**
     * Calculates total stack count for a specific item (e.g. 1.5 stacks).
     */
    public int getFullStacksCount(String itemId, Set<InventorySlotSource> sources) {
        if (itemId == null || sources == null || sources.isEmpty()) {
            return 0;
        }
        int total = 0;
        int maxStack = 64;
        for (InventoryItemData item : items) {
            if (sources.contains(item.getSource()) && matchesId(item.getItemId(), itemId)) {
                total += item.getCount();
                maxStack = item.getMaxStackSize();
            }
        }
        return maxStack > 0 ? total / maxStack : 0;
    }

    /**
     * Finds the first matching item data for metadata queries (e.g. display name, icon handle).
     */
    public InventoryItemData findFirst(String itemId) {
        if (itemId == null) return null;
        for (InventoryItemData item : items) {
            if (matchesId(item.getItemId(), itemId)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Aggregates counts by Item ID for the given inventory sources.
     */
    public Map<String, Integer> getCountsByItemId(Set<InventorySlotSource> sources) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        if (sources == null || sources.isEmpty()) {
            return counts;
        }
        for (InventoryItemData item : items) {
            if (sources.contains(item.getSource())) {
                counts.put(item.getItemId(), counts.getOrDefault(item.getItemId(), 0) + item.getCount());
            }
        }
        return counts;
    }

    private boolean matchesId(String a, String b) {
        if (a == null || b == null) return false;
        String idA = a.contains(":") ? a : "minecraft:" + a;
        String idB = b.contains(":") ? b : "minecraft:" + b;
        return idA.equalsIgnoreCase(idB);
    }
}
