package pl.zombek.client.core.modules.hud.itemcounter;

/**
 * Visual layout modes for the Item Counter HUD module.
 */
public enum ItemCounterLayout {
    COMPACT("Compact (Icon + Count)"),
    DETAILED("Detailed (Icon + Name + Count)"),
    ICONS_ONLY("Icons Only"),
    LIST("Vertical List"),
    HORIZONTAL("Horizontal Bar"),
    CUSTOM("Custom Layout");

    private final String displayName;

    ItemCounterLayout(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
