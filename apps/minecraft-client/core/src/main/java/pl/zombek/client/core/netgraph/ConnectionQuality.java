package pl.zombek.client.core.netgraph;

/**
 * Subjective client-side classification of network connection stability and latency.
 * Based purely on observed Ping and Jitter thresholds; not an objective server rating.
 */
public enum ConnectionQuality {
    EXCELLENT("Excellent", 0xFF55FF55, "Znakomite opóźnienie i stabilność"),
    GOOD("Good", 0xFF55FFFF, "Dobre, stabilne połączenie"),
    MODERATE("Moderate", 0xFFFFFF55, "Umiarkowane opóźnienie lub sporadyczny jitter"),
    POOR("Poor", 0xFFFF5555, "Wysoki ping lub niestabilne łącze"),
    UNAVAILABLE("Unavailable", 0xFFAAAAAA, "Metryka niedostępna (Singleplayer / Disconnected)");

    private final String label;
    private final int defaultColor;
    private final String description;

    ConnectionQuality(String label, int defaultColor, String description) {
        this.label = label;
        this.defaultColor = defaultColor;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public int getDefaultColor() {
        return defaultColor;
    }

    public String getDescription() {
        return description;
    }
}
