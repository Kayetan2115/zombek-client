package pl.zombek.client.core.serveraddress;

/**
 * Platform provider interface querying active server connection metadata across mod loaders.
 */
public interface IServerAddressDataProvider {

    /**
     * Retrieves current server connection information.
     */
    ServerConnectionData getConnectionData();
}
