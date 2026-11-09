package pl.zombek.client.core.serveraddress;

/**
 * Null Object fallback for server address queries in menus or unit tests.
 */
public class FallbackServerAddressDataProvider implements IServerAddressDataProvider {

    private ServerConnectionData data = new ServerConnectionData(
            ServerConnectionType.DISCONNECTED,
            null,
            25565,
            null,
            null,
            "minecraft:overworld",
            false
    );

    @Override
    public ServerConnectionData getConnectionData() {
        return data;
    }

    public void setConnectionData(ServerConnectionData data) {
        if (data != null) {
            this.data = data;
        }
    }
}
