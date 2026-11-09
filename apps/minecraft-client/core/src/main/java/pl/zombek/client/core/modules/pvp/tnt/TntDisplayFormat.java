package pl.zombek.client.core.modules.pvp.tnt;

/**
 * Display format for the TNT Countdown overhead label.
 */
public enum TntDisplayFormat {
    PREFIX_AND_TIME("TNT + Czas (np. TNT 3.8s)"),
    TIME_ONLY("Tylko Czas (np. 3.8s)");

    private final String displayName;

    TntDisplayFormat(String displayName) {
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
