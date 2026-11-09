package pl.zombek.client.core.netgraph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.zombek.client.core.info.IInfoHudDataProvider;
import pl.zombek.client.core.info.InfoHudDataProviderRegistry;

/**
 * Central orchestrator collecting, aggregating, and windowing network metrics.
 * Runs on client tick, gathers metrics from providers and Netty events,
 * and maintains circular history buffers and session statistics.
 */
public class NetworkMetricsCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkMetricsCollector.class);
    private static final NetworkMetricsCollector INSTANCE = new NetworkMetricsCollector();

    private final LatencyTracker latencyTracker = new LatencyTracker();
    private final NetworkTrafficTracker trafficTracker = new NetworkTrafficTracker();
    private final PacketRateTracker packetRateTracker = new PacketRateTracker();
    private final NetworkHistoryBuffer historyBuffer = new NetworkHistoryBuffer(300);
    private final ConnectionStatusTracker statusTracker = new ConnectionStatusTracker();
    private final NetworkStatistics statistics = new NetworkStatistics();

    private long lastSampleTime = System.currentTimeMillis();
    private ConnectionStatus lastStatus = ConnectionStatus.UNKNOWN;

    public NetworkMetricsCollector() {}

    public static NetworkMetricsCollector getInstance() {
        return INSTANCE;
    }

    /**
     * Periodic tick callback invoked from ZombekCore.onClientTick() on the game thread.
     */
    public synchronized void onClientTick() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastSampleTime;

        INetworkDataProvider netProvider = NetworkDataProviderRegistry.getInstance().getProvider();
        IInfoHudDataProvider infoProvider = InfoHudDataProviderRegistry.getInstance().getProvider();

        boolean isSingleplayer = netProvider != null ? netProvider.isSingleplayer() :
                (infoProvider != null && infoProvider.isSingleplayer());

        int ping = -1;
        if (!isSingleplayer) {
            if (netProvider != null && netProvider.getPingMs() >= 0) {
                ping = netProvider.getPingMs();
            } else if (infoProvider != null && infoProvider.getPingMs() >= 0) {
                ping = infoProvider.getPingMs();
            }
        }
        latencyTracker.recordPing(ping, now);

        // Fetch bytes/packets from provider if available
        if (netProvider != null) {
            long rxBytes = netProvider.getIncomingBytes();
            if (rxBytes > 0) trafficTracker.recordIncomingBytes(rxBytes);
            long txBytes = netProvider.getOutgoingBytes();
            if (txBytes > 0) trafficTracker.recordOutgoingBytes(txBytes);

            int rxPackets = netProvider.getIncomingPackets();
            for (int i = 0; i < rxPackets; i++) packetRateTracker.recordIncomingPacket();
            int txPackets = netProvider.getOutgoingPackets();
            for (int i = 0; i < txPackets; i++) packetRateTracker.recordOutgoingPacket();

            trafficTracker.setTrafficSupported(netProvider.isTrafficTrackingSupported() && !isSingleplayer);
        }

        // Commit snapshot sample once per second (approx. 1000ms)
        if (elapsed >= 1000) {
            trafficTracker.update(elapsed);
            packetRateTracker.update(elapsed);

            ConnectionStatus status = netProvider != null ? netProvider.getConnectionStatus() :
                    (isSingleplayer ? ConnectionStatus.SINGLEPLAYER : ConnectionStatus.UNKNOWN);

            statusTracker.update(
                    status,
                    latencyTracker.getCurrentPing(),
                    latencyTracker.getCurrentJitter(),
                    isSingleplayer
            );

            // Detect new connection establishment
            if (status != lastStatus) {
                if (status == ConnectionStatus.CONNECTED && lastStatus != ConnectionStatus.CONNECTED) {
                    statistics.startSession();
                    LOGGER.info("[NetworkMetricsCollector] New connection session started.");
                }
                lastStatus = status;
            }

            long rxBps = trafficTracker.isTrafficSupported() ? trafficTracker.getIncomingBytesPerSecond() : -1;
            long txBps = trafficTracker.isTrafficSupported() ? trafficTracker.getOutgoingBytesPerSecond() : -1;

            NetworkMetricSample sample = new NetworkMetricSample(
                    now,
                    latencyTracker.getCurrentPing(),
                    latencyTracker.getCurrentJitter(),
                    rxBps,
                    txBps,
                    packetRateTracker.getIncomingPps(),
                    packetRateTracker.getOutgoingPps(),
                    statusTracker.getCurrentStatus(),
                    statusTracker.getCurrentQuality()
            );

            historyBuffer.addSample(sample);
            statistics.update(sample);
            lastSampleTime = now;
        }
    }

    public void recordIncomingBytes(long bytes) {
        trafficTracker.recordIncomingBytes(bytes);
    }

    public void recordOutgoingBytes(long bytes) {
        trafficTracker.recordOutgoingBytes(bytes);
    }

    public void recordIncomingPacket() {
        packetRateTracker.recordIncomingPacket();
    }

    public void recordOutgoingPacket() {
        packetRateTracker.recordOutgoingPacket();
    }

    public void recordPing(int pingMs) {
        latencyTracker.recordPing(pingMs);
    }

    public synchronized void onWorldLeave() {
        statusTracker.update(ConnectionStatus.DISCONNECTED, -1, -1.0, false);
        lastStatus = ConnectionStatus.DISCONNECTED;
        latencyTracker.recordPing(-1);
    }

    public synchronized void shutdown() {
        onWorldLeave();
    }

    public synchronized void resetSession() {
        latencyTracker.reset();
        trafficTracker.reset();
        packetRateTracker.reset();
        historyBuffer.clear();
        statusTracker.reset();
        statistics.reset();
        lastSampleTime = System.currentTimeMillis();
    }

    public static synchronized void resetForTesting() {
        INSTANCE.resetSession();
    }

    public LatencyTracker getLatencyTracker() {
        return latencyTracker;
    }

    public NetworkTrafficTracker getTrafficTracker() {
        return trafficTracker;
    }

    public PacketRateTracker getPacketRateTracker() {
        return packetRateTracker;
    }

    public NetworkHistoryBuffer getHistoryBuffer() {
        return historyBuffer;
    }

    public ConnectionStatusTracker getStatusTracker() {
        return statusTracker;
    }

    public NetworkStatistics getStatistics() {
        return statistics;
    }
}
