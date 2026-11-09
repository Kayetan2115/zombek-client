package pl.zombek.client.core.netgraph;

/**
 * Fallback provider used in headless tests and uninitialized environments.
 */
public class FallbackNetworkDataProvider implements INetworkDataProvider {

    private boolean connected = false;
    private boolean singleplayer = false;
    private ConnectionStatus status = ConnectionStatus.DISCONNECTED;
    private int pingMs = -1;
    private long incomingBytes = -1;
    private long outgoingBytes = -1;
    private int incomingPackets = 0;
    private int outgoingPackets = 0;
    private boolean trafficSupported = false;

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public boolean isSingleplayer() {
        return singleplayer;
    }

    @Override
    public ConnectionStatus getConnectionStatus() {
        return status;
    }

    @Override
    public int getPingMs() {
        return pingMs;
    }

    @Override
    public long getIncomingBytes() {
        return incomingBytes;
    }

    @Override
    public long getOutgoingBytes() {
        return outgoingBytes;
    }

    @Override
    public int getIncomingPackets() {
        return incomingPackets;
    }

    @Override
    public int getOutgoingPackets() {
        return outgoingPackets;
    }

    @Override
    public boolean isTrafficTrackingSupported() {
        return trafficSupported;
    }

    // Setters for unit testing
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public void setSingleplayer(boolean singleplayer) {
        this.singleplayer = singleplayer;
        if (singleplayer) {
            this.status = ConnectionStatus.SINGLEPLAYER;
            this.pingMs = -1;
        }
    }

    public void setStatus(ConnectionStatus status) {
        this.status = status;
    }

    public void setPingMs(int pingMs) {
        this.pingMs = pingMs;
    }

    public void setIncomingBytes(long incomingBytes) {
        this.incomingBytes = incomingBytes;
    }

    public void setOutgoingBytes(long outgoingBytes) {
        this.outgoingBytes = outgoingBytes;
    }

    public void setIncomingPackets(int incomingPackets) {
        this.incomingPackets = incomingPackets;
    }

    public void setOutgoingPackets(int outgoingPackets) {
        this.outgoingPackets = outgoingPackets;
    }

    public void setTrafficSupported(boolean trafficSupported) {
        this.trafficSupported = trafficSupported;
    }
}
