package pl.zombek.client.core.inventory;

import java.util.Objects;

/**
 * Immutable DTO representing a single item stack within a specific inventory slot.
 */
public final class InventoryItemData {

    private final String itemId;
    private final String itemName;
    private final int count;
    private final int maxStackSize;
    private final int slotIndex;
    private final InventorySlotSource source;
    private final String componentsSummary;
    private final Object itemHandle;
    private final boolean block;
    private final boolean food;

    public InventoryItemData(String itemId,
                             String itemName,
                             int count,
                             int maxStackSize,
                             int slotIndex,
                             InventorySlotSource source,
                             String componentsSummary,
                             Object itemHandle,
                             boolean block,
                             boolean food) {
        this.itemId = itemId != null ? itemId : "minecraft:air";
        this.itemName = itemName != null ? itemName : this.itemId;
        this.count = Math.max(0, count);
        this.maxStackSize = Math.max(1, maxStackSize);
        this.slotIndex = slotIndex;
        this.source = source != null ? source : InventorySlotSource.MAIN_INVENTORY;
        this.componentsSummary = componentsSummary != null ? componentsSummary : "";
        this.itemHandle = itemHandle;
        this.block = block;
        this.food = food;
    }

    public String getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public int getCount() {
        return count;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public InventorySlotSource getSource() {
        return source;
    }

    public String getComponentsSummary() {
        return componentsSummary;
    }

    public Object getItemHandle() {
        return itemHandle;
    }

    public boolean isBlock() {
        return block;
    }

    public boolean isFood() {
        return food;
    }

    public boolean isSameVariant(InventoryItemData other) {
        if (other == null) return false;
        return Objects.equals(this.itemId, other.itemId)
                && Objects.equals(this.componentsSummary, other.componentsSummary);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InventoryItemData)) return false;
        InventoryItemData that = (InventoryItemData) o;
        return count == that.count &&
                slotIndex == that.slotIndex &&
                Objects.equals(itemId, that.itemId) &&
                source == that.source &&
                Objects.equals(componentsSummary, that.componentsSummary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, count, slotIndex, source, componentsSummary);
    }

    @Override
    public String toString() {
        return "InventoryItemData{" +
                "itemId='" + itemId + '\'' +
                ", count=" + count +
                ", slot=" + slotIndex +
                ", source=" + source +
                '}';
    }
}
