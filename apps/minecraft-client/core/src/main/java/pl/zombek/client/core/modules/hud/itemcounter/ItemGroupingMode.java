package pl.zombek.client.core.modules.hud.itemcounter;

/**
 * Grouping strategies for counting items in player inventory.
 */
public enum ItemGroupingMode {
    BY_ITEM_ID("By Item ID"),
    BY_VARIANT("By Item Variant"),
    BY_CUSTOM_RULE("By Custom Rule");

    private final String displayName;

    ItemGroupingMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
