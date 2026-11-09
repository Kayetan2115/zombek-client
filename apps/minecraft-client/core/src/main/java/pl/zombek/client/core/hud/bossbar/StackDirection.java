package pl.zombek.client.core.hud.bossbar;

/**
 * Direction for stacking multiple active boss bars.
 */
public enum StackDirection {
    DOWN("W dół"),
    UP("W górę");

    private final String displayName;

    StackDirection(String displayName) {
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
