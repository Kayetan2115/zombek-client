package pl.zombek.client.core.data;

/**
 * Enumeration representing player armor slots with their standard inventory indices.
 */
public enum ArmorSlot {
    HELMET(3, "Hełm"),
    CHESTPLATE(2, "Napierśnik"),
    LEGGINGS(1, "Nogawice"),
    BOOTS(0, "Buty");

    private final int inventoryIndex;
    private final String displayName;

    ArmorSlot(int inventoryIndex, String displayName) {
        this.inventoryIndex = inventoryIndex;
        this.displayName = displayName;
    }

    public int getInventoryIndex() {
        return inventoryIndex;
    }

    public String getDisplayName() {
        return displayName;
    }
}
