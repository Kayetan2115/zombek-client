package pl.zombek.client.core.hud.component;

/**
 * Universal styling presets for in-game HUD modules.
 */
public enum HudComponentStyle {
    VANILLA("Vanilla (Standardowy Minecraft)"),
    CLASSIC("Klasyczny (Półprzezroczysty ciemny)"),
    MINIMAL("Minimalistyczny (Tylko tekst)"),
    MODERN("Nowoczesny (Festive Dark z akcentem)"),
    CUSTOM("Własny (Pełna konfiguracja)");

    private final String displayName;

    HudComponentStyle(String displayName) {
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
