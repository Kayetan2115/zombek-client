package pl.zombek.client.core.movement;

/**
 * Display styles for movement toggle HUD indicators.
 */
public enum MovementIndicatorStyle {
    BRACKETED("[Sprint (Toggled)]"),
    COMPACT("Sprint"),
    DETAILED("Sprint: Active"),
    TEXT_ONLY("SPRINT");

    private final String displayName;

    MovementIndicatorStyle(String displayName) {
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
