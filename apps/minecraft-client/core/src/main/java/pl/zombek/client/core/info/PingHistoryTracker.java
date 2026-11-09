package pl.zombek.client.core.info;

import java.util.Arrays;

/**
 * Ring buffer and statistics tracker for network latency (ping).
 */
public class PingHistoryTracker {
    public enum PingStatus {
        GOOD,
        MEDIUM,
        HIGH,
        CRITICAL,
        UNAVAILABLE
    }

    public static final int DEFAULT_CAPACITY = 60;

    private final int[] samples;
    private final int capacity;
    private int head = 0;
    private int size = 0;

    private int currentPing = -1;
    private int minPing = -1;
    private int maxPing = -1;
    private double avgPing = -1.0;
    private long sum = 0;

    public PingHistoryTracker() {
        this(DEFAULT_CAPACITY);
    }

    public PingHistoryTracker(int capacity) {
        this.capacity = Math.max(2, Math.min(1000, capacity));
        this.samples = new int[this.capacity];
        reset();
    }

    /**
     * Records a new ping sample in milliseconds.
     * Use -1 for singleplayer or when latency is unavailable.
     */
    public synchronized void recordSample(int pingMs) {
        this.currentPing = pingMs;
        if (pingMs < 0) {
            // Unavailable / Singleplayer: do not pollute statistical average
            return;
        }

        if (size < capacity) {
            samples[head] = pingMs;
            sum += pingMs;
            size++;
        } else {
            sum -= samples[head];
            samples[head] = pingMs;
            sum += pingMs;
        }

        head = (head + 1) % capacity;

        avgPing = (double) sum / (double) Math.max(1, size);

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < size; i++) {
            int val = samples[i];
            if (val < min) min = val;
            if (val > max) max = val;
        }
        minPing = min != Integer.MAX_VALUE ? min : currentPing;
        maxPing = max != Integer.MIN_VALUE ? max : currentPing;
    }

    public synchronized void reset() {
        Arrays.fill(samples, 0);
        head = 0;
        size = 0;
        sum = 0;
        currentPing = -1;
        minPing = -1;
        maxPing = -1;
        avgPing = -1.0;
    }

    public synchronized int getCurrentPing() {
        return currentPing;
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

    public synchronized boolean isAvailable() {
        return currentPing >= 0;
    }

    /**
     * Classifies current ping against configured thresholds.
     */
    public synchronized PingStatus getStatus(int goodMax, int mediumMax, int highMax) {
        if (currentPing < 0) return PingStatus.UNAVAILABLE;
        if (currentPing <= goodMax) return PingStatus.GOOD;
        if (currentPing <= mediumMax) return PingStatus.MEDIUM;
        if (currentPing <= highMax) return PingStatus.HIGH;
        return PingStatus.CRITICAL;
    }
}
