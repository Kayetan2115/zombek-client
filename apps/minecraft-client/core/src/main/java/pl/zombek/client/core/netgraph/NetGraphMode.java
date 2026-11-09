package pl.zombek.client.core.netgraph;

/**
 * Operating modes for the Net Graph HUD module.
 */
public enum NetGraphMode {
    /**
     * Latency mode: displays ping history, current ping, average, peak, and jitter.
     */
    LATENCY("Latency", "Wykres opóźnienia sieciowego (Ping / Jitter)"),

    /**
     * Traffic mode: displays incoming and outgoing bandwidth (B/s, KB/s, MB/s).
     */
    TRAFFIC("Traffic", "Wykres przepustowości łącza (Incoming / Outgoing)"),

    /**
     * Packet rate mode: displays incoming and outgoing packets per second (PPS).
     */
    PACKETS("Packets", "Wykres częstotliwości pakietów (RX / TX PPS)"),

    /**
     * Combined mode: presents latency, bandwidth, and packet rates simultaneously.
     */
    COMBINED("Combined", "Wielowarstwowy wykres wszystkich metryk sieciowych"),

    /**
     * Custom mode: user selects visible metrics via module settings.
     */
    CUSTOM("Custom", "Niestandardowy zestaw wybranych serii pomiarowych");

    private final String label;
    private final String description;

    NetGraphMode(String label, String description) {
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
