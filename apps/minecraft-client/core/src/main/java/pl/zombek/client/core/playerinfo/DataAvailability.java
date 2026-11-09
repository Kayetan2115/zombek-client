package pl.zombek.client.core.playerinfo;

/**
 * Indicates the availability and origin state of player metadata.
 * Strict Fair Play: unknown fields are never replaced by fictitious assumptions.
 */
public enum DataAvailability {
    AVAILABLE("Available"),
    UNKNOWN("Unknown"),
    UNAVAILABLE("Unavailable");

    private final String label;

    DataAvailability(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isAvailable() {
        return this == AVAILABLE;
    }
}
