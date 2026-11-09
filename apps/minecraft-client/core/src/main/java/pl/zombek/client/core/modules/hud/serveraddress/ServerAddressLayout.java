package pl.zombek.client.core.modules.hud.serveraddress;

/**
 * Layout modes for the Server Address HUD module.
 */
public enum ServerAddressLayout {
    MINIMAL("Minimal (Host only)"),
    DETAILED("Detailed (Host, Port, Status, Dimension)"),
    COMPACT("Compact (Server: host)"),
    CONNECTION_INFO("Connection Info (Full State)"),
    CUSTOM("Custom Layout");

    private final String displayName;

    ServerAddressLayout(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
