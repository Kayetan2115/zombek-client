package pl.zombek.client.core.hud.scoreboard;

/**
 * Text alignment options for HUD rendering.
 */
public enum TextAlignment {
    LEFT("Do lewej"),
    CENTER("Wyśrodkowane"),
    RIGHT("Do prawej");

    private final String displayName;

    TextAlignment(String displayName) {
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
