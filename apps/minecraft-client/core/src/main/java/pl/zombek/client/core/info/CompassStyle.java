package pl.zombek.client.core.info;

/**
 * Rendering presentation styles for the Direction HUD compass.
 */
public enum CompassStyle {
    CLASSIC("Klasyczny (Pasek poziomy)"),
    MINIMAL("Minimalny (Tylko kierunek)"),
    DETAILED("Szczegółowy (Pasek + Stopnie + Tagi)"),
    CIRCULAR("Okrągły (Róża wiatrów)");

    private final String displayName;

    CompassStyle(String displayName) {
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
