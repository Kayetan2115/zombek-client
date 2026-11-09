package pl.zombek.client.core.netgraph;

/**
 * Real-time connection states of the Minecraft client.
 */
public enum ConnectionStatus {
    CONNECTED("Connected", 0xFF55FF55, "Aktywne połączenie sieciowe z serwerem"),
    CONNECTING("Connecting", 0xFFFFFF55, "Nawiązywanie połączenia / logowanie"),
    DISCONNECTED("Disconnected", 0xFFFF5555, "Brak aktywnego połączenia"),
    RECONNECTING("Reconnecting", 0xFFFFAA00, "Ponowne nawiązywanie połączenia"),
    SINGLEPLAYER("Singleplayer", 0xFF55FFFF, "Lokalna rozgrywka jednoosobowa"),
    LAN("LAN", 0xFFFFAA00, "Lokalna sieć bezprzewodowa / przewodowa"),
    UNKNOWN("Unknown", 0xFFAAAAAA, "Nieznany stan połączenia");

    private final String label;
    private final int defaultColor;
    private final String description;

    ConnectionStatus(String label, int defaultColor, String description) {
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

    public boolean isConnected() {
        return this == CONNECTED || this == SINGLEPLAYER || this == LAN;
    }
}
