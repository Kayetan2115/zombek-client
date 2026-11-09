package pl.zombek.client.core.hud.editor;

/**
 * Feature flags denoting customizable spatial and visual properties for a HUD component.
 */
public enum HudComponentCapability {
    /** Component can have its X and Y screen offsets repositioned. */
    POSITION("Pozycja"),
    /** Component supports scale magnification factor (e.g. 0.5x - 2.5x). */
    SCALE("Skala"),
    /** Component supports explicit width and height dimension changes. */
    SIZE("Rozmiar"),
    /** Component visibility can be toggled on/off in the HUD. */
    VISIBILITY("Widoczność"),
    /** Component supports layer priority / Z-Index ordering. */
    Z_INDEX("Warstwa (Z-Index)"),
    /** Component can anchor to different screen edges or corners. */
    ANCHOR("Kotwica");

    private final String displayName;

    HudComponentCapability(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
