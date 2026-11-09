package pl.zombek.client.core.hud.bossbar;

/**
 * Visual styling options for the custom Boss Bar HUD.
 */
public enum BossBarStyle {
    VANILLA("Vanilla (Standardowy styl gry)"),
    CLASSIC("Classic (Prosty solidny pasek)"),
    MODERN("Modern (Zaokrąglone rogi i akcent)"),
    MINIMAL("Minimal (Wąski pasek z kompaktowym tekstem)"),
    SEGMENTED("Segmented (Wizualne nacięcia segmentów)"),
    CUSTOM("Custom (Pełna personalizacja)");

    private final String displayName;

    BossBarStyle(String displayName) {
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
