package pl.zombek.client.core.netgraph;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.util.Locale;

/**
 * Aggregates lifetime / session-level network statistics since connection establishment.
 * Strictly adheres to privacy constraints: zero IP addresses, zero chat logs, zero credentials.
 */
public class NetworkStatistics {

    private long sessionStartTime = 0;
    private long sessionDurationMillis = 0;

    // Ping stats
    private int minPing = -1;
    private int maxPing = -1;
    private long sumPing = 0;
    private int pingSampleCount = 0;

    // Jitter stats
    private double maxJitter = -1.0;
    private double sumJitter = 0.0;
    private int jitterSampleCount = 0;

    // Traffic stats
    private long peakIncomingBps = 0;
    private long peakOutgoingBps = 0;
    private long totalBytesReceived = 0;
    private long totalBytesSent = 0;

    // Packet stats
    private int maxIncomingPps = 0;
    private int maxOutgoingPps = 0;
    private long sumIncomingPps = 0;
    private long sumOutgoingPps = 0;
    private int packetSampleCount = 0;
    private long totalIncomingPackets = 0;
    private long totalOutgoingPackets = 0;

    public NetworkStatistics() {
        reset();
    }

    public synchronized void startSession() {
        reset();
        this.sessionStartTime = System.currentTimeMillis();
    }

    public synchronized void update(NetworkMetricSample sample) {
        if (sample == null) return;
        if (sessionStartTime == 0) {
            sessionStartTime = sample.getTimestamp();
        }
        sessionDurationMillis = Math.max(0, sample.getTimestamp() - sessionStartTime);

        // Update Ping
        if (sample.isPingAvailable()) {
            int p = sample.getPingMs();
            if (minPing == -1 || p < minPing) minPing = p;
            if (p > maxPing) maxPing = p;
            sumPing += p;
            pingSampleCount++;
        }

        // Update Jitter
        if (sample.isJitterAvailable()) {
            double j = sample.getJitterMs();
            if (j > maxJitter) maxJitter = j;
            sumJitter += j;
            jitterSampleCount++;
        }

        // Update Traffic
        if (sample.getIncomingBps() > peakIncomingBps) peakIncomingBps = sample.getIncomingBps();
        if (sample.getOutgoingBps() > peakOutgoingBps) peakOutgoingBps = sample.getOutgoingBps();
        totalBytesReceived += Math.max(0, sample.getIncomingBps());
        totalBytesSent += Math.max(0, sample.getOutgoingBps());

        // Update Packets
        if (sample.getIncomingPps() > maxIncomingPps) maxIncomingPps = sample.getIncomingPps();
        if (sample.getOutgoingPps() > maxOutgoingPps) maxOutgoingPps = sample.getOutgoingPps();
        sumIncomingPps += sample.getIncomingPps();
        sumOutgoingPps += sample.getOutgoingPps();
        packetSampleCount++;
        totalIncomingPackets += sample.getIncomingPps();
        totalOutgoingPackets += sample.getOutgoingPps();
    }

    public synchronized void reset() {
        sessionStartTime = System.currentTimeMillis();
        sessionDurationMillis = 0;
        minPing = -1;
        maxPing = -1;
        sumPing = 0;
        pingSampleCount = 0;
        maxJitter = -1.0;
        sumJitter = 0.0;
        jitterSampleCount = 0;
        peakIncomingBps = 0;
        peakOutgoingBps = 0;
        totalBytesReceived = 0;
        totalBytesSent = 0;
        maxIncomingPps = 0;
        maxOutgoingPps = 0;
        sumIncomingPps = 0;
        sumOutgoingPps = 0;
        packetSampleCount = 0;
        totalIncomingPackets = 0;
        totalOutgoingPackets = 0;
    }

    public synchronized long getSessionDurationSeconds() {
        return sessionDurationMillis / 1000;
    }

    public synchronized String getFormattedDuration() {
        long sec = getSessionDurationSeconds();
        long hours = sec / 3600;
        long minutes = (sec % 3600) / 60;
        long seconds = sec % 60;
        if (hours > 0) {
            return String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds);
        }
    }

    public synchronized int getMinPing() {
        return minPing;
    }

    public synchronized int getMaxPing() {
        return maxPing;
    }

    public synchronized double getAvgPing() {
        return pingSampleCount > 0 ? (double) sumPing / pingSampleCount : -1.0;
    }

    public synchronized double getMaxJitter() {
        return maxJitter;
    }

    public synchronized double getAvgJitter() {
        return jitterSampleCount > 0 ? sumJitter / jitterSampleCount : -1.0;
    }

    public synchronized long getPeakIncomingBps() {
        return peakIncomingBps;
    }

    public synchronized long getPeakOutgoingBps() {
        return peakOutgoingBps;
    }

    public synchronized long getTotalBytesReceived() {
        return totalBytesReceived;
    }

    public synchronized long getTotalBytesSent() {
        return totalBytesSent;
    }

    public synchronized int getMaxIncomingPps() {
        return maxIncomingPps;
    }

    public synchronized int getMaxOutgoingPps() {
        return maxOutgoingPps;
    }

    public synchronized double getAvgIncomingPps() {
        return packetSampleCount > 0 ? (double) sumIncomingPps / packetSampleCount : 0.0;
    }

    public synchronized double getAvgOutgoingPps() {
        return packetSampleCount > 0 ? (double) sumOutgoingPps / packetSampleCount : 0.0;
    }

    public synchronized long getTotalIncomingPackets() {
        return totalIncomingPackets;
    }

    public synchronized long getTotalOutgoingPackets() {
        return totalOutgoingPackets;
    }

    /**
     * Exports anonymized session metrics as a JSON string.
     */
    public synchronized String exportToJson() {
        JsonObject root = new JsonObject();
        root.addProperty("version", "0.1.0-alpha");
        root.addProperty("timestamp", System.currentTimeMillis());
        root.addProperty("sessionDurationSeconds", getSessionDurationSeconds());

        JsonObject latencyObj = new JsonObject();
        latencyObj.addProperty("minPingMs", minPing);
        latencyObj.addProperty("maxPingMs", maxPing);
        latencyObj.addProperty("avgPingMs", getAvgPing() >= 0 ? Math.round(getAvgPing() * 10.0) / 10.0 : -1.0);
        latencyObj.addProperty("avgJitterMs", getAvgJitter() >= 0 ? Math.round(getAvgJitter() * 10.0) / 10.0 : -1.0);
        latencyObj.addProperty("maxJitterMs", maxJitter);
        root.add("latency", latencyObj);

        JsonObject trafficObj = new JsonObject();
        trafficObj.addProperty("totalReceivedBytes", totalBytesReceived);
        trafficObj.addProperty("totalSentBytes", totalBytesSent);
        trafficObj.addProperty("peakIncomingBps", peakIncomingBps);
        trafficObj.addProperty("peakOutgoingBps", peakOutgoingBps);
        root.add("traffic", trafficObj);

        JsonObject packetObj = new JsonObject();
        packetObj.addProperty("totalIncomingPackets", totalIncomingPackets);
        packetObj.addProperty("totalOutgoingPackets", totalOutgoingPackets);
        packetObj.addProperty("maxIncomingPps", maxIncomingPps);
        packetObj.addProperty("maxOutgoingPps", maxOutgoingPps);
        packetObj.addProperty("avgIncomingPps", Math.round(getAvgIncomingPps() * 10.0) / 10.0);
        packetObj.addProperty("avgOutgoingPps", Math.round(getAvgOutgoingPps() * 10.0) / 10.0);
        root.add("packets", packetObj);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(root);
    }

    /**
     * Exports anonymized session metrics as formatted plain text.
     */
    public synchronized String exportToText() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ZombekClient Net Graph Diagnostics ===\n");
        sb.append("Session Duration: ").append(getFormattedDuration()).append("\n");
        sb.append("------------------------------------------\n");
        sb.append("Latency (Ping):\n");
        sb.append("  Min Ping: ").append(minPing >= 0 ? minPing + " ms" : "N/A").append("\n");
        sb.append("  Max Ping: ").append(maxPing >= 0 ? maxPing + " ms" : "N/A").append("\n");
        sb.append("  Avg Ping: ").append(getAvgPing() >= 0 ? String.format(Locale.ROOT, "%.1f ms", getAvgPing()) : "N/A").append("\n");
        sb.append("  Avg Jitter: ").append(getAvgJitter() >= 0 ? String.format(Locale.ROOT, "%.1f ms", getAvgJitter()) : "N/A").append("\n");
        sb.append("  Max Jitter: ").append(maxJitter >= 0 ? String.format(Locale.ROOT, "%.1f ms", maxJitter) : "N/A").append("\n");
        sb.append("Traffic Throughput:\n");
        sb.append("  Peak In:  ").append(NetworkTrafficTracker.formatBytesPerSecond(peakIncomingBps, TrafficUnit.AUTO)).append("\n");
        sb.append("  Peak Out: ").append(NetworkTrafficTracker.formatBytesPerSecond(peakOutgoingBps, TrafficUnit.AUTO)).append("\n");
        sb.append("  Total In: ").append(NetworkTrafficTracker.formatTotalBytes(totalBytesReceived)).append("\n");
        sb.append("  Total Out:").append(NetworkTrafficTracker.formatTotalBytes(totalBytesSent)).append("\n");
        sb.append("Packet Rates (PPS):\n");
        sb.append("  Max In:   ").append(maxIncomingPps).append(" PPS\n");
        sb.append("  Max Out:  ").append(maxOutgoingPps).append(" PPS\n");
        sb.append("  Avg In:   ").append(String.format(Locale.ROOT, "%.1f PPS", getAvgIncomingPps())).append("\n");
        sb.append("  Avg Out:  ").append(String.format(Locale.ROOT, "%.1f PPS", getAvgOutgoingPps())).append("\n");
        sb.append("==========================================");
        return sb.toString();
    }
}
