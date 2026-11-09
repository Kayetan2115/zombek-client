package pl.zombek.client.core.serveraddress;

/**
 * Immutable DTO representing current connection status and server metadata.
 */
public final class ServerConnectionData {

    private final ServerConnectionType connectionType;
    private final String serverAddress;
    private final int serverPort;
    private final String serverName;
    private final String worldName;
    private final String dimension;
    private final boolean connected;

    public ServerConnectionData(ServerConnectionType connectionType,
                                String serverAddress,
                                int serverPort,
                                String serverName,
                                String worldName,
                                String dimension,
                                boolean connected) {
        this.connectionType = connectionType != null ? connectionType : ServerConnectionType.DISCONNECTED;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort > 0 ? serverPort : 25565;
        this.serverName = serverName;
        this.worldName = worldName;
        this.dimension = dimension != null ? dimension : "minecraft:overworld";
        this.connected = connected;
    }

    public ServerConnectionType getConnectionType() {
        return connectionType;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getServerName() {
        return serverName;
    }

    public String getWorldName() {
        return worldName;
    }

    public String getDimension() {
        return dimension;
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isSingleplayer() {
        return connectionType == ServerConnectionType.SINGLEPLAYER;
    }

    public boolean isMultiplayer() {
        return connectionType == ServerConnectionType.MULTIPLAYER || connectionType == ServerConnectionType.LAN;
    }

    public static ServerConnectionData singleplayer(String worldName, String dimension) {
        return new ServerConnectionData(ServerConnectionType.SINGLEPLAYER, null, 0, null, worldName, dimension, true);
    }

    public static ServerConnectionData multiplayer(String serverName, String serverAddress, int serverPort, String dimension) {
        return new ServerConnectionData(ServerConnectionType.MULTIPLAYER, serverAddress, serverPort, serverName, null, dimension, true);
    }

    public static ServerConnectionData disconnected() {
        return new ServerConnectionData(ServerConnectionType.DISCONNECTED, null, 0, null, null, null, false);
    }
}
