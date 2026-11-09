package pl.zombek.client.core.netgraph;

/**
 * Tracks and evaluates live network connection state and connection quality grade.
 */
public class ConnectionStatusTracker {

    private ConnectionStatus currentStatus = ConnectionStatus.UNKNOWN;
    private ConnectionQuality currentQuality = ConnectionQuality.UNAVAILABLE;

    // Configurable quality thresholds
    private int excellentPingThreshold = 35;
    private int goodPingThreshold = 75;
    private int moderatePingThreshold = 150;
    private double jitterWarningThreshold = 15.0;

    public synchronized void update(
            ConnectionStatus status,
            int pingMs,
            double jitterMs,
            boolean isSingleplayer
    ) {
        if (isSingleplayer) {
            this.currentStatus = ConnectionStatus.SINGLEPLAYER;
            this.currentQuality = ConnectionQuality.UNAVAILABLE;
            return;
        }

        this.currentStatus = status != null ? status : ConnectionStatus.UNKNOWN;

        if (currentStatus != ConnectionStatus.CONNECTED && currentStatus != ConnectionStatus.LAN) {
            this.currentQuality = ConnectionQuality.UNAVAILABLE;
            return;
        }

        if (pingMs < 0) {
            this.currentQuality = ConnectionQuality.UNAVAILABLE;
            return;
        }

        // Evaluate Quality Grade
        if (jitterMs > jitterWarningThreshold * 2.0 || pingMs > moderatePingThreshold) {
            this.currentQuality = ConnectionQuality.POOR;
        } else if (jitterMs > jitterWarningThreshold || pingMs > goodPingThreshold) {
            this.currentQuality = ConnectionQuality.MODERATE;
        } else if (jitterMs > jitterWarningThreshold / 2.0 || pingMs > excellentPingThreshold) {
            this.currentQuality = ConnectionQuality.GOOD;
        } else {
            this.currentQuality = ConnectionQuality.EXCELLENT;
        }
    }

    public synchronized ConnectionStatus getCurrentStatus() {
        return currentStatus;
    }

    public synchronized ConnectionQuality getCurrentQuality() {
        return currentQuality;
    }

    public synchronized void setThresholds(int excellent, int good, int moderate, double jitterWarning) {
        this.excellentPingThreshold = Math.max(5, excellent);
        this.goodPingThreshold = Math.max(this.excellentPingThreshold, good);
        this.moderatePingThreshold = Math.max(this.goodPingThreshold, moderate);
        this.jitterWarningThreshold = Math.max(1.0, jitterWarning);
    }

    public synchronized int getExcellentPingThreshold() {
        return excellentPingThreshold;
    }

    public synchronized int getGoodPingThreshold() {
        return goodPingThreshold;
    }

    public synchronized int getModeratePingThreshold() {
        return moderatePingThreshold;
    }

    public synchronized double getJitterWarningThreshold() {
        return jitterWarningThreshold;
    }

    public synchronized void reset() {
        this.currentStatus = ConnectionStatus.UNKNOWN;
        this.currentQuality = ConnectionQuality.UNAVAILABLE;
    }
}
