package pl.zombek.client.core.netgraph;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks network latency (ping) and jitter using an RFC 3550 / IPDV-compliant statistical model.
 * Does not send active ping probes or alter network timing.
 */
public class LatencyTracker {

    private static final LatencyTracker INSTANCE = new LatencyTracker();

    public static final int DEFAULT_MAX_SAMPLES = 120;

    private final int capacity;
    private final int[] sampleBuffer;
    private final long[] timestampBuffer;
    private int head = 0;
    private int size = 0;

    private int currentPing = -1;
    private int lastKnownPing = -1;
    private int minPing = -1;
    private int maxPing = -1;
    private double avgPing = -1.0;
    private long sumPing = 0;

    // Jitter metrics (RFC 3550 Mean Absolute Delay Variation & EWMA)
    private double currentJitter = -1.0;
    private double avgJitter = -1.0;
    private double maxJitter = -1.0;

    public LatencyTracker() {
        this(DEFAULT_MAX_SAMPLES);
    }

    public LatencyTracker(int capacity) {
        this.capacity = Math.max(2, Math.min(1000, capacity));
        this.sampleBuffer = new int[this.capacity];
        this.timestampBuffer = new long[this.capacity];
        reset();
    }

    public static LatencyTracker getInstance() {
        return INSTANCE;
    }

    /**
     * Records a new ping sample in milliseconds.
     * Passing -1 indicates unavailable measurement or Singleplayer mode.
     */
    public synchronized void recordPing(int pingMs) {
        recordPing(pingMs, System.currentTimeMillis());
    }

    public synchronized void recordPing(int pingMs, long timestamp) {
        this.currentPing = pingMs;
        if (pingMs >= 0) {
            this.lastKnownPing = pingMs;
        } else {
            // Unavailable / Singleplayer: do not pollute statistical samples
            return;
        }

        int previousPing = size > 0 ? sampleBuffer[(head - 1 + capacity) % capacity] : -1;

        if (size < capacity) {
            sampleBuffer[head] = pingMs;
            timestampBuffer[head] = timestamp;
            sumPing += pingMs;
            size++;
        } else {
            sumPing -= sampleBuffer[head];
            sampleBuffer[head] = pingMs;
            timestampBuffer[head] = timestamp;
            sumPing += pingMs;
        }

        head = (head + 1) % capacity;

        // Recalculate Ping aggregates
        avgPing = (double) sumPing / (double) Math.max(1, size);

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < size; i++) {
            int val = sampleBuffer[i];
            if (val < min) min = val;
            if (val > max) max = val;
        }
        minPing = min != Integer.MAX_VALUE ? min : currentPing;
        maxPing = max != Integer.MIN_VALUE ? max : currentPing;

        // Recalculate Jitter (requires at least 2 consecutive samples)
        if (size >= 2 && previousPing >= 0) {
            double instantDiff = Math.abs(pingMs - previousPing);

            // RFC 3550 EWMA algorithm for current instantaneous jitter
            if (currentJitter < 0.0) {
                currentJitter = instantDiff;
            } else {
                currentJitter = currentJitter + (instantDiff - currentJitter) / 16.0;
            }

            // Window-wide average and maximum jitter
            double sumDiff = 0.0;
            double peakDiff = 0.0;
            int transitions = 0;

            for (int i = 1; i < size; i++) {
                int idxCur = (head - size + i + capacity) % capacity;
                int idxPrev = (head - size + i - 1 + capacity) % capacity;
                double diff = Math.abs(sampleBuffer[idxCur] - sampleBuffer[idxPrev]);
                sumDiff += diff;
                if (diff > peakDiff) {
                    peakDiff = diff;
                }
                transitions++;
            }

            avgJitter = transitions > 0 ? sumDiff / transitions : currentJitter;
            maxJitter = peakDiff;
        } else {
            currentJitter = -1.0;
            avgJitter = -1.0;
            maxJitter = -1.0;
        }
    }

    public synchronized void reset() {
        head = 0;
        size = 0;
        sumPing = 0;
        currentPing = -1;
        lastKnownPing = -1;
        minPing = -1;
        maxPing = -1;
        avgPing = -1.0;
        currentJitter = -1.0;
        avgJitter = -1.0;
        maxJitter = -1.0;
    }

    public synchronized int getCurrentPing() {
        return currentPing;
    }

    public synchronized int getLastKnownPing() {
        return lastKnownPing;
    }

    public synchronized int getMinPing() {
        return minPing;
    }

    public synchronized int getMaxPing() {
        return maxPing;
    }

    public synchronized double getAvgPing() {
        return avgPing;
    }

    public synchronized double getCurrentJitter() {
        return currentJitter;
    }

    public synchronized double getAvgJitter() {
        return avgJitter;
    }

    public synchronized double getMaxJitter() {
        return maxJitter;
    }

    public synchronized boolean isAvailable() {
        return currentPing >= 0;
    }

    public synchronized boolean isJitterAvailable() {
        return currentJitter >= 0.0 && size >= 2;
    }

    public synchronized int getSampleCount() {
        return size;
    }

    /**
     * Retrieves ordered ping samples recorded within the last specified duration in milliseconds.
     */
    public synchronized List<Integer> getSamplesWithin(long durationMillis) {
        List<Integer> result = new ArrayList<>();
        if (size == 0) return result;

        long cutoff = System.currentTimeMillis() - durationMillis;
        for (int i = 0; i < size; i++) {
            int idx = (head - size + i + capacity) % capacity;
            if (timestampBuffer[idx] >= cutoff) {
                result.add(sampleBuffer[idx]);
            }
        }
        return result;
    }
}
