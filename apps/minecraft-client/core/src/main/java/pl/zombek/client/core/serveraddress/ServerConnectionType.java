package pl.zombek.client.core.serveraddress;

/**
 * Types of connection state for the Minecraft client.
 */
public enum ServerConnectionType {
    SINGLEPLAYER("Singleplayer", 0xFF55FF55),
    MULTIPLAYER("Multiplayer", 0xFF55FFFF),
    LAN("LAN World", 0xFFFFAA00),
    DISCONNECTED("Disconnected", 0xFFFF5555);

    private final String label;
    private final int defaultColor;

    ServerConnectionType(String label, int defaultColor) {
        this.label = label;
        this.defaultColor = defaultColor;
    }

    public String getLabel() {
        return label;
    }

    public int getDefaultColor() {
        return defaultColor;
    }
}
