package pl.zombek.client.core.netgraph;

import java.util.ArrayList;
import java.util.List;

/**
 * High-performance, thread-safe circular ring buffer storing temporal snapshots of network metrics.
 * Limits memory consumption and automatically discards samples older than the allocated capacity.
 */
public class NetworkHistoryBuffer {

    public static final int DEFAULT_CAPACITY = 300; // e.g. 5 minutes at 1 sample/sec

    private final int capacity;
    private final NetworkMetricSample[] ring;
    private int head = 0;
    private int size = 0;

    public NetworkHistoryBuffer() {
        this(DEFAULT_CAPACITY);
    }

    public NetworkHistoryBuffer(int capacity) {
        this.capacity = Math.max(2, Math.min(3600, capacity));
        this.ring = new NetworkMetricSample[this.capacity];
    }

    /**
     * Appends a new sample to the circular buffer.
     */
    public synchronized void addSample(NetworkMetricSample sample) {
        if (sample == null) return;
        ring[head] = sample;
        head = (head + 1) % capacity;
        if (size < capacity) {
            size++;
        }
    }

    /**
     * Iterates all active samples in chronological order without allocating a new List.
     */
    public synchronized void forEachSample(java.util.function.Consumer<NetworkMetricSample> consumer) {
        if (consumer == null) return;
        for (int i = 0; i < size; i++) {
            int idx = (head - size + i + capacity) % capacity;
            consumer.accept(ring[idx]);
        }
    }

    /**
     * Copies samples into provided array without allocation.
     * @return count of samples copied
     */
    public synchronized int copyChronological(NetworkMetricSample[] dest) {
        if (dest == null) return 0;
        int count = Math.min(size, dest.length);
        for (int i = 0; i < count; i++) {
            int idx = (head - size + i + capacity) % capacity;
            dest[i] = ring[idx];
        }
        return count;
    }

    /**
     * Retrieves all active samples in chronological order (oldest to newest).
     */
    public synchronized List<NetworkMetricSample> getSamples() {
        List<NetworkMetricSample> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            int idx = (head - size + i + capacity) % capacity;
            list.add(ring[idx]);
        }
        return list;
    }

    /**
     * Retrieves samples recorded within the last specified duration in milliseconds.
     */
    public synchronized List<NetworkMetricSample> getSamplesWithin(long durationMillis) {
        List<NetworkMetricSample> list = new ArrayList<>();
        if (size == 0) return list;

        long cutoff = System.currentTimeMillis() - durationMillis;
        for (int i = 0; i < size; i++) {
            int idx = (head - size + i + capacity) % capacity;
            NetworkMetricSample sample = ring[idx];
            if (sample != null && sample.getTimestamp() >= cutoff) {
                list.add(sample);
            }
        }
        return list;
    }

    public synchronized int getSampleCount() {
        return size;
    }

    public synchronized NetworkMetricSample getLatestSample() {
        if (size == 0) return null;
        int idx = (head - 1 + capacity) % capacity;
        return ring[idx];
    }

    public synchronized void clear() {
        for (int i = 0; i < capacity; i++) {
            ring[i] = null;
        }
        head = 0;
        size = 0;
    }

    public synchronized int getMaxPing(long durationMillis) {
        List<NetworkMetricSample> samples = getSamplesWithin(durationMillis);
        int max = -1;
        for (NetworkMetricSample s : samples) {
            if (s.getPingMs() > max) {
                max = s.getPingMs();
            }
        }
        return max;
    }

    public synchronized int getMinPing(long durationMillis) {
        List<NetworkMetricSample> samples = getSamplesWithin(durationMillis);
        int min = Integer.MAX_VALUE;
        for (NetworkMetricSample s : samples) {
            if (s.getPingMs() >= 0 && s.getPingMs() < min) {
                min = s.getPingMs();
            }
        }
        return min != Integer.MAX_VALUE ? min : -1;
    }

    public synchronized double getAvgPing(long durationMillis) {
        List<NetworkMetricSample> samples = getSamplesWithin(durationMillis);
        long sum = 0;
        int count = 0;
        for (NetworkMetricSample s : samples) {
            if (s.getPingMs() >= 0) {
                sum += s.getPingMs();
                count++;
            }
        }
        return count > 0 ? (double) sum / count : -1.0;
    }

    public synchronized long getMaxTraffic(long durationMillis) {
        List<NetworkMetricSample> samples = getSamplesWithin(durationMillis);
        long max = 0;
        for (NetworkMetricSample s : samples) {
            long total = s.getIncomingBps() + s.getOutgoingBps();
            if (total > max) {
                max = total;
            }
        }
        return max;
    }

    public synchronized int getMaxPps(long durationMillis) {
        List<NetworkMetricSample> samples = getSamplesWithin(durationMillis);
        int max = 0;
        for (NetworkMetricSample s : samples) {
            int total = s.getIncomingPps() + s.getOutgoingPps();
            if (total > max) {
                max = total;
            }
        }
        return max;
    }
}
