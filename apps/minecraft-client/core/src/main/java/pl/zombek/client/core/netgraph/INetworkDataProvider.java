package pl.zombek.client.core.netgraph;

/**
 * Platform provider contract supplying network connection state, transport bytes,
 * and packet metrics across Fabric, Forge, NeoForge, and Quilt.
 */
public interface INetworkDataProvider {

    /**
     * Checks if the client has an active open world connection.
     */
    boolean isConnected();

    /**
     * Checks if the client is currently playing in a local singleplayer game.
     */
    boolean isSingleplayer();

    /**
     * Returns the current classified network connection status.
     */
    ConnectionStatus getConnectionStatus();

    /**
     * Network latency in milliseconds, or -1 if unavailable / singleplayer.
     */
    int getPingMs();

    /**
     * Raw inbound wire bytes received since last poll, or -1 if unsupported.
     */
    long getIncomingBytes();

    /**
     * Raw outbound wire bytes transmitted since last poll, or -1 if unsupported.
     */
    long getOutgoingBytes();

    /**
     * Inbound packets received since last poll.
     */
    int getIncomingPackets();

    /**
     * Outbound packets dispatched since last poll.
     */
    int getOutgoingPackets();

    /**
     * Indicates whether the platform adapter can measure raw wire byte counts.
     */
    boolean isTrafficTrackingSupported();
}
