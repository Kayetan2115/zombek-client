package pl.zombek.client.core.tracking;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * High-precision, monotonic click rate tracker for CPS Counter and Keystrokes.
 * Uses System.nanoTime() to prevent OS system clock tampering or timezone shifts from distorting rates.
 * Employs a sliding time window (default 1000 ms) and maintains peak CPS metrics.
 */
public class ClickTracker {
    private static final ClickTracker INSTANCE = new ClickTracker();

    public static final long DEFAULT_WINDOW_MS = 1000L;
    public static final long NANOS_PER_MS = 1_000_000L;

    private final Deque<Long> leftClicks = new ArrayDeque<>();
    private final Deque<Long> rightClicks = new ArrayDeque<>();

    private int maxLeftCps = 0;
    private int maxRightCps = 0;

    private ClickTracker() {}

    public static ClickTracker getInstance() {
        return INSTANCE;
    }

    /**
     * Records an authentic left mouse button click event.
     */
    public synchronized void recordLeftClick() {
        long now = System.nanoTime();
        leftClicks.addLast(now);
        int currentCps = getCpsLmb(DEFAULT_WINDOW_MS);
        if (currentCps > maxLeftCps) {
            maxLeftCps = currentCps;
        }
    }

    /**
     * Records an authentic right mouse button click event.
     */
    public synchronized void recordRightClick() {
        long now = System.nanoTime();
        rightClicks.addLast(now);
        int currentCps = getCpsRmb(DEFAULT_WINDOW_MS);
        if (currentCps > maxRightCps) {
            maxRightCps = currentCps;
        }
    }

    /**
     * Calculates the current LMB clicks-per-second within the specified window in milliseconds.
     */
    public synchronized int getCpsLmb(long windowMs) {
        long now = System.nanoTime();
        long threshold = now - Math.max(100L, windowMs) * NANOS_PER_MS;
        cleanExpired(leftClicks, threshold);
        return leftClicks.size();
    }

    /**
     * Calculates the current RMB clicks-per-second within the specified window in milliseconds.
     */
    public synchronized int getCpsRmb(long windowMs) {
        long now = System.nanoTime();
        long threshold = now - Math.max(100L, windowMs) * NANOS_PER_MS;
        cleanExpired(rightClicks, threshold);
        return rightClicks.size();
    }

    public synchronized int getCpsLmb() {
        return getCpsLmb(DEFAULT_WINDOW_MS);
    }

    public synchronized int getCpsRmb() {
        return getCpsRmb(DEFAULT_WINDOW_MS);
    }

    public synchronized int getMaxLeftCps() {
        return maxLeftCps;
    }

    public synchronized int getMaxRightCps() {
        return maxRightCps;
    }

    public synchronized void resetMaxCps() {
        this.maxLeftCps = 0;
        this.maxRightCps = 0;
    }

    /**
     * Resets all click queues and maximum stats (e.g. On disconnect, world change).
     */
    public synchronized void reset() {
        leftClicks.clear();
        rightClicks.clear();
        maxLeftCps = 0;
        maxRightCps = 0;
    }

    private void cleanExpired(Deque<Long> queue, long thresholdNanos) {
        while (!queue.isEmpty() && queue.peekFirst() < thresholdNanos) {
            queue.pollFirst();
        }
    }
}
