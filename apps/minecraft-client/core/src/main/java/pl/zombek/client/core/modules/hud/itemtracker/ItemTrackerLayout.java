package pl.zombek.client.core.modules.hud.itemtracker;

/**
 * Presentation layouts for the Item Tracker HUD module.
 */
public enum ItemTrackerLayout {
    FEED("Event Feed"),
    STATISTICS("Session Statistics"),
    COMPACT("Compact Feed"),
    DETAILED("Detailed Feed with Timestamps");

    private final String displayName;

    ItemTrackerLayout(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
