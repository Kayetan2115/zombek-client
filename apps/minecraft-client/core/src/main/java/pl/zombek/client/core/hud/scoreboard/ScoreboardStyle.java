package pl.zombek.client.core.hud.scoreboard;

/**
 * Styling presets for the Scoreboard HUD.
 */
public enum ScoreboardStyle {
    VANILLA("Vanilla (Standardowy Minecraft)"),
    MINIMAL("Minimal (Kompaktowy, bez obramowania)"),
    MODERN("Modern (Ciemne tło, zaokrąglona ramka)"),
    GLASS("Glass (Eleganckie półprzezroczyste szkło)"),
    CUSTOM("Custom (Własne kolory i odstępy)");

    private final String displayName;

    ScoreboardStyle(String displayName) {
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
