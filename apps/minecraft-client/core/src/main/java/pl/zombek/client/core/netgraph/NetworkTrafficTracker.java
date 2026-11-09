package pl.zombek.client.core.netgraph;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks aggregated incoming and outgoing network traffic volume (bytes/sec and total bytes).
 * Thread-safe: Netty network threads increment atomic accumulators without locking or allocations.
 * Never inspects, decodes, buffers, or retains packet payload data.
 */
public class NetworkTrafficTracker {

    private final AtomicLong incomingByteAccumulator = new AtomicLong(0);
    private final AtomicLong outgoingByteAccumulator = new AtomicLong(0);

    private volatile long incomingBytesPerSecond = 0;
    private volatile long outgoingBytesPerSecond = 0;
    private volatile long totalBytesReceived = 0;
    private volatile long totalBytesSent = 0;
    private volatile long peakIncomingBps = 0;
    private volatile long peakOutgoingBps = 0;

    private volatile boolean trafficSupported = true;

    /**
     * Called by platform Netty handlers when raw wire bytes arrive.
     */
    public void recordIncomingBytes(long bytes) {
        if (bytes > 0) {
            incomingByteAccumulator.addAndGet(bytes);
            trafficSupported = true;
        }
    }

    /**
     * Called by platform Netty handlers when raw wire bytes are dispatched.
     */
    public void recordOutgoingBytes(long bytes) {
        if (bytes > 0) {
            outgoingByteAccumulator.addAndGet(bytes);
            trafficSupported = true;
        }
    }

    /**
     * Updates throughput rates based on elapsed milliseconds since previous tick cycle.
     */
    public synchronized void update(long elapsedMillis) {
        if (elapsedMillis <= 0) return;

        long rxBytes = incomingByteAccumulator.getAndSet(0);
        long txBytes = outgoingByteAccumulator.getAndSet(0);

        incomingBytesPerSecond = (long) (rxBytes * 1000.0 / elapsedMillis);
        outgoingBytesPerSecond = (long) (txBytes * 1000.0 / elapsedMillis);

        totalBytesReceived += rxBytes;
        totalBytesSent += txBytes;

        if (incomingBytesPerSecond > peakIncomingBps) {
            peakIncomingBps = incomingBytesPerSecond;
        }
        if (outgoingBytesPerSecond > peakOutgoingBps) {
            peakOutgoingBps = outgoingBytesPerSecond;
        }
    }

    public synchronized void reset() {
        incomingByteAccumulator.set(0);
        outgoingByteAccumulator.set(0);
        incomingBytesPerSecond = 0;
        outgoingBytesPerSecond = 0;
        totalBytesReceived = 0;
        totalBytesSent = 0;
        peakIncomingBps = 0;
        peakOutgoingBps = 0;
    }

    public long getIncomingBytesPerSecond() {
        return incomingBytesPerSecond;
    }

    public long getOutgoingBytesPerSecond() {
        return outgoingBytesPerSecond;
    }

    public long getTotalBytesReceived() {
        return totalBytesReceived;
    }

    public long getTotalBytesSent() {
        return totalBytesSent;
    }

    public long getPeakIncomingBps() {
        return peakIncomingBps;
    }

    public long getPeakOutgoingBps() {
        return peakOutgoingBps;
    }

    public boolean isTrafficSupported() {
        return trafficSupported;
    }

    public void setTrafficSupported(boolean supported) {
        this.trafficSupported = supported;
    }

    /**
     * Formats throughput in human-readable B/s, KB/s, or MB/s.
     */
    public static String formatBytesPerSecond(long bytesPerSec, TrafficUnit unit) {
        if (bytesPerSec < 0) return "N/A";
        TrafficUnit effectiveUnit = unit != null ? unit : TrafficUnit.AUTO;

        if (effectiveUnit == TrafficUnit.AUTO) {
            if (bytesPerSec < 1024) {
                return bytesPerSec + " B/s";
            } else if (bytesPerSec < 1024 * 1024) {
                return String.format(Locale.ROOT, "%.1f KB/s", bytesPerSec / 1024.0);
            } else {
                return String.format(Locale.ROOT, "%.2f MB/s", bytesPerSec / (1024.0 * 1024.0));
            }
        } else if (effectiveUnit == TrafficUnit.B_S) {
            return bytesPerSec + " B/s";
        } else if (effectiveUnit == TrafficUnit.KB_S) {
            return String.format(Locale.ROOT, "%.1f KB/s", bytesPerSec / 1024.0);
        } else {
            return String.format(Locale.ROOT, "%.2f MB/s", bytesPerSec / (1024.0 * 1024.0));
        }
    }

    /**
     * Formats cumulative data transfer in human-readable bytes, KB, MB, or GB.
     */
    public static String formatTotalBytes(long bytes) {
        if (bytes < 0) return "N/A";
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024L * 1024L * 1024L) {
            return String.format(Locale.ROOT, "%.2f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format(Locale.ROOT, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
