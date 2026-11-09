package pl.zombek.client.core.netgraph;

/**
 * Immutable time-stamped snapshot of network metrics captured in a discrete time bucket (e.g. 1 second).
 */
public final class NetworkMetricSample {

    private final long timestamp;
    private final int pingMs;
    private final double jitterMs;
    private final long incomingBps;
    private final long outgoingBps;
    private final int incomingPps;
    private final int outgoingPps;
    private final ConnectionStatus status;
    private final ConnectionQuality quality;

    public NetworkMetricSample(
            long timestamp,
            int pingMs,
            double jitterMs,
            long incomingBps,
            long outgoingBps,
            int incomingPps,
            int outgoingPps,
            ConnectionStatus status,
            ConnectionQuality quality
    ) {
        this.timestamp = timestamp;
        this.pingMs = pingMs;
        this.jitterMs = jitterMs;
        this.incomingBps = incomingBps;
        this.outgoingBps = outgoingBps;
        this.incomingPps = incomingPps;
        this.outgoingPps = outgoingPps;
        this.status = status != null ? status : ConnectionStatus.UNKNOWN;
        this.quality = quality != null ? quality : ConnectionQuality.UNAVAILABLE;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getPingMs() {
        return pingMs;
    }

    public double getJitterMs() {
        return jitterMs;
    }

    public long getIncomingBps() {
        return incomingBps;
    }

    public long getOutgoingBps() {
        return outgoingBps;
    }

    public int getIncomingPps() {
        return incomingPps;
    }

    public int getOutgoingPps() {
        return outgoingPps;
    }

    public ConnectionStatus getStatus() {
        return status;
    }

    public ConnectionQuality getQuality() {
        return quality;
    }

    public boolean isPingAvailable() {
        return pingMs >= 0;
    }

    public boolean isJitterAvailable() {
        return jitterMs >= 0.0;
    }

    public boolean isTrafficAvailable() {
        return incomingBps >= 0 && outgoingBps >= 0;
    }
}
