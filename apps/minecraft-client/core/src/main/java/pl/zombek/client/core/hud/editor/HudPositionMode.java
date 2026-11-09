package pl.zombek.client.core.hud.editor;

/**
 * Coordinate positioning mode for HUD components.
 */
public enum HudPositionMode {
    ABSOLUTE_PIXEL("Piksele (px)"),
    RELATIVE("Względna (%)"),
    NORMALIZED("Znormalizowana (0.0-1.0)");

    private final String displayName;

    HudPositionMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
