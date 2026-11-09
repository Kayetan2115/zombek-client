package pl.zombek.client.core.bedwars;

/**
 * Confidence level of Bedwars match detection.
 */
public enum DetectionConfidence {
    HIGH("Wysoki"),
    MEDIUM("Średni"),
    LOW("Niski"),
    UNKNOWN("Nieznany");

    private final String displayName;

    DetectionConfidence(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isReliable() {
        return this == HIGH || this == MEDIUM;
    }
}
