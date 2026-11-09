package pl.zombek.client.core.data;

/**
 * Type of target currently under the crosshair.
 */
public enum CrosshairTargetType {
    NONE("Brak celu"),
    BLOCK("Blok"),
    ENTITY("Encja");

    private final String displayName;

    CrosshairTargetType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
