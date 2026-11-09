package pl.zombek.client.core.inventory;

/**
 * Enumeration of player inventory regions.
 */
public enum InventorySlotSource {
    HOTBAR("Hotbar"),
    MAIN_INVENTORY("Main Inventory"),
    OFFHAND("Offhand"),
    ARMOR("Armor Slots");

    private final String displayName;

    InventorySlotSource(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
