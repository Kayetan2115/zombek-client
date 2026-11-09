package pl.zombek.client.core.info;

/**
 * Display formatting styles for Coordinates module.
 */
public enum CoordinateFormat {
    FULL("Pełne (X: 120.542, Y: 64.000, Z: -230.121)"),
    BLOCK("Blokowe (X: 120, Y: 64, Z: -231)"),
    COMPACT("Kompaktowe (XYZ: 120, 64, -230)"),
    SEPARATE("Oddzielne wiersze (X, Y, Z)");

    private final String displayName;

    CoordinateFormat(String displayName) {
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
