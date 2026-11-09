package pl.zombek.client.core.info;

import java.util.Arrays;

/**
 * High-performance, zero-allocation ring buffer for tracking FPS statistics and graph plotting.
 */
public class FpsHistoryTracker {
    public static final int DEFAULT_CAPACITY = 100;

    private final int[] samples;
    private int capacity;
    private int head = 0;
    private int size = 0;

    private int currentFps = 60;
    private int minFps = 60;
    private int maxFps = 60;
    private double avgFps = 60.0;
    private long sum = 0;

    public FpsHistoryTracker() {
        this(DEFAULT_CAPACITY);
    }

    public FpsHistoryTracker(int capacity) {
        this.capacity = Math.max(2, Math.min(1000, capacity));
        this.samples = new int[this.capacity];
        reset();
    }

    /**
     * Records a new FPS sample.
     */
    public synchronized void recordSample(int fps) {
        if (fps < 0) fps = 0;
        currentFps = fps;

        if (size < capacity) {
            samples[head] = fps;
            sum += fps;
            size++;
        } else {
            sum -= samples[head];
            samples[head] = fps;
            sum += fps;
        }

        head = (head + 1) % capacity;

        // Recalculate min, max, avg
        avgFps = (double) sum / (double) Math.max(1, size);

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < size; i++) {
            int val = samples[i];
            if (val < min) min = val;
            if (val > max) max = val;
        }
        minFps = min != Integer.MAX_VALUE ? min : currentFps;
        maxFps = max != Integer.MIN_VALUE ? max : currentFps;
    }

    /**
     * Resets accumulated statistics and history buffer.
     */
    public synchronized void reset() {
        Arrays.fill(samples, 0);
        head = 0;
        size = 0;
        sum = 0;
        currentFps = 60;
        minFps = 60;
        maxFps = 60;
        avgFps = 60.0;
    }

    public synchronized int getCurrentFps() {
        return currentFps;
    }

    public synchronized int getMinFps() {
        return size > 0 ? minFps : currentFps;
    }

    public synchronized int getMaxFps() {
        return size > 0 ? maxFps : currentFps;
    }

    public synchronized double getAvgFps() {
        return size > 0 ? avgFps : (double) currentFps;
    }

    public synchronized int getSize() {
        return size;
    }

    public synchronized int getCapacity() {
        return capacity;
    }

    /**
     * Copies chronological samples into the target array to avoid heap allocation.
     * Returns the count of copied elements.
     */
    public synchronized int getChronologicalSamples(int[] dest) {
        if (dest == null || dest.length == 0 || size == 0) return 0;
        int count = Math.min(dest.length, size);
        int startIndex = (head - size + capacity) % capacity;
        for (int i = 0; i < count; i++) {
            int idx = (startIndex + (size - count + i)) % capacity;
            dest[i] = samples[idx];
        }
        return count;
    }
}
