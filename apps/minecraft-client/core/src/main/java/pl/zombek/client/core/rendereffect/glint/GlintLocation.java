package pl.zombek.client.core.rendereffect.glint;

/**
 * Rendering contexts / locations where items are rendered with glint.
 */
public enum GlintLocation {
    INVENTORY("Inventory"),
    HOTBAR("Hotbar"),
    HELD_ITEMS("Held Items"),
    DROPPED_ITEMS("Dropped Items"),
    CONTAINERS("Containers"),
    ARMOR_RENDERING("Armor Rendering");

    private final String displayName;

    GlintLocation(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
