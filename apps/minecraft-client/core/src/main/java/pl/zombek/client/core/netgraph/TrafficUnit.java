package pl.zombek.client.core.netgraph;

/**
 * Display units for bandwidth and network throughput.
 */
public enum TrafficUnit {
    AUTO("Auto", "Automatyczne dopasowanie (B/s, KB/s, MB/s)"),
    B_S("B/s", "Bajty na sekundę"),
    KB_S("KB/s", "Kilobajty na sekundę"),
    MB_S("MB/s", "Megabajty na sekundę");

    private final String label;
    private final String description;

    TrafficUnit(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}
