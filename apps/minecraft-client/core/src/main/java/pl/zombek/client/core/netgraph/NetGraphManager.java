package pl.zombek.client.core.netgraph;

/**
 * High-level manager coordinating the Net Graph diagnostics subsystem.
 */
public class NetGraphManager {

    private static final NetGraphManager INSTANCE = new NetGraphManager();

    private NetGraphManager() {}

    public static NetGraphManager getInstance() {
        return INSTANCE;
    }

    public NetworkMetricsCollector getCollector() {
        return NetworkMetricsCollector.getInstance();
    }

    public LatencyTracker getLatencyTracker() {
        return getCollector().getLatencyTracker();
    }

    public NetworkTrafficTracker getTrafficTracker() {
        return getCollector().getTrafficTracker();
    }

    public PacketRateTracker getPacketRateTracker() {
        return getCollector().getPacketRateTracker();
    }

    public NetworkHistoryBuffer getHistoryBuffer() {
        return getCollector().getHistoryBuffer();
    }

    public ConnectionStatusTracker getStatusTracker() {
        return getCollector().getStatusTracker();
    }

    public NetworkStatistics getStatistics() {
        return getCollector().getStatistics();
    }

    public void resetSession() {
        getCollector().resetSession();
    }

    public String exportSessionToJson() {
        return getStatistics().exportToJson();
    }

    public String exportSessionToText() {
        return getStatistics().exportToText();
    }
}
