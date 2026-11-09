package pl.zombek.client.core.benchmark;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Manages game performance benchmarking runs, recording FPS, frametimes,
 * memory usage, and computing statistical metrics including 1% Low FPS.
 */
public class PerformanceBenchmarkManager {
    private static final PerformanceBenchmarkManager INSTANCE = new PerformanceBenchmarkManager();

    public static PerformanceBenchmarkManager getInstance() {
        return INSTANCE;
    }

    private final List<BenchmarkResult> history = new CopyOnWriteArrayList<>();
    private final List<Consumer<BenchmarkResult>> completionListeners = new CopyOnWriteArrayList<>();

    private boolean benchmarking = false;
    private String currentProfile = "Default";
    private int targetDurationSeconds = 60;
    private long startTimeMillis = 0;
    private long lastSampleTimeMillis = 0;

    // Buffer for frametimes in milliseconds during current benchmark
    private double[] frameTimeBuffer = new double[36000]; // up to 600 FPS for 60s
    private int frameCount = 0;

    // Aggregates for memory
    private long memorySumMb = 0;
    private long peakMemoryMb = 0;
    private int memorySampleCount = 0;

    // FPS tracking
    private int minFps = Integer.MAX_VALUE;
    private int maxFps = 0;
    private int fpsAccumulator = 0;
    private int fpsSampleCount = 0;

    // World load time tracking
    private long worldLoadStartTime = 0;
    private long worldLoadDurationMs = 0;

    public PerformanceBenchmarkManager() {
    }

    /**
     * Start a timed benchmark session.
     */
    public synchronized void startBenchmark(String profileName, int durationSeconds) {
        this.currentProfile = (profileName == null || profileName.isBlank()) ? "Custom" : profileName;
        this.targetDurationSeconds = Math.max(5, durationSeconds);
        this.startTimeMillis = System.currentTimeMillis();
        this.lastSampleTimeMillis = this.startTimeMillis;
        this.frameCount = 0;
        this.frameTimeBuffer = new double[Math.max(10000, this.targetDurationSeconds * 500)];
        this.memorySumMb = 0;
        this.peakMemoryMb = 0;
        this.memorySampleCount = 0;
        this.minFps = Integer.MAX_VALUE;
        this.maxFps = 0;
        this.fpsAccumulator = 0;
        this.fpsSampleCount = 0;
        this.benchmarking = true;
    }

    /**
     * Stop the current benchmark and calculate the result.
     */
    public synchronized BenchmarkResult stopBenchmark() {
        if (!benchmarking) {
            return null;
        }

        benchmarking = false;
        long elapsedSeconds = Math.max(1, (System.currentTimeMillis() - startTimeMillis) / 1000L);

        BenchmarkResult result = computeResult((int) elapsedSeconds);
        history.add(result);

        for (Consumer<BenchmarkResult> listener : completionListeners) {
            try {
                listener.accept(result);
            } catch (Exception ignored) {
            }
        }

        return result;
    }

    /**
     * Records a single frame's duration and current FPS.
     *
     * @param frameTimeNanos render duration of the frame in nanoseconds
     * @param currentFps instant FPS value reported by client
     */
    public synchronized void recordFrame(long frameTimeNanos, int currentFps) {
        if (!benchmarking) {
            return;
        }

        long now = System.currentTimeMillis();
        double frameTimeMs = frameTimeNanos / 1_000_000.0;

        // Ensure buffer capacity
        if (frameCount >= frameTimeBuffer.length) {
            frameTimeBuffer = Arrays.copyOf(frameTimeBuffer, frameTimeBuffer.length * 2);
        }
        frameTimeBuffer[frameCount++] = frameTimeMs;

        if (currentFps > 0) {
            fpsAccumulator += currentFps;
            fpsSampleCount++;
            if (currentFps < minFps) minFps = currentFps;
            if (currentFps > maxFps) maxFps = currentFps;
        }

        // Sample memory roughly every 200ms
        if (now - lastSampleTimeMillis >= 200) {
            long usedMemMb = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
            memorySumMb += usedMemMb;
            memorySampleCount++;
            if (usedMemMb > peakMemoryMb) {
                peakMemoryMb = usedMemMb;
            }
            lastSampleTimeMillis = now;
        }

        // Auto-complete if target duration reached
        if (now - startTimeMillis >= targetDurationSeconds * 1000L) {
            stopBenchmark();
        }
    }

    /**
     * Computes statistical metrics from collected frame times.
     */
    private BenchmarkResult computeResult(int durationSec) {
        if (frameCount == 0) {
            return new BenchmarkResult(
                    currentProfile, durationSec, 0,
                    0.0, 0, 0, 0.0, 0.0, 0.0,
                    0L, 0L, worldLoadDurationMs, Instant.now()
            );
        }

        double totalFrameTime = 0;
        double peakFrameTime = 0;
        for (int i = 0; i < frameCount; i++) {
            double ft = frameTimeBuffer[i];
            totalFrameTime += ft;
            if (ft > peakFrameTime) {
                peakFrameTime = ft;
            }
        }

        double avgFrameTime = totalFrameTime / frameCount;
        double avgFps = avgFrameTime > 0 ? (1000.0 / avgFrameTime) : 0.0;

        if (minFps == Integer.MAX_VALUE) minFps = (int) Math.round(avgFps);
        if (maxFps == 0) maxFps = (int) Math.round(avgFps);

        // 1% Low FPS Calculation:
        // Sort frame times in ascending order. The worst 1% of frames have the highest frametimes.
        double[] sorted = Arrays.copyOf(frameTimeBuffer, frameCount);
        Arrays.sort(sorted);

        int onePercentCount = Math.max(1, (int) Math.ceil(frameCount * 0.01));
        int startIndex = frameCount - onePercentCount;
        double worstFrameTimeSum = 0;
        for (int i = startIndex; i < frameCount; i++) {
            worstFrameTimeSum += sorted[i];
        }
        double avgWorst1PercentFrameTime = worstFrameTimeSum / onePercentCount;
        double onePercentLowFps = avgWorst1PercentFrameTime > 0 ? (1000.0 / avgWorst1PercentFrameTime) : 0.0;

        long avgMem = memorySampleCount > 0 ? (memorySumMb / memorySampleCount) : 0;

        return new BenchmarkResult(
                currentProfile,
                durationSec,
                frameCount,
                Math.round(avgFps * 10.0) / 10.0,
                minFps,
                maxFps,
                Math.round(onePercentLowFps * 10.0) / 10.0,
                Math.round(avgFrameTime * 100.0) / 100.0,
                Math.round(peakFrameTime * 100.0) / 100.0,
                avgMem,
                peakMemoryMb,
                worldLoadDurationMs,
                Instant.now()
        );
    }

    public void markWorldLoadStart() {
        this.worldLoadStartTime = System.currentTimeMillis();
    }

    public void markWorldLoadFinished() {
        if (this.worldLoadStartTime > 0) {
            this.worldLoadDurationMs = System.currentTimeMillis() - this.worldLoadStartTime;
            this.worldLoadStartTime = 0;
        }
    }

    public void addCompletionListener(Consumer<BenchmarkResult> listener) {
        if (listener != null) {
            completionListeners.add(listener);
        }
    }

    public boolean isBenchmarking() {
        return benchmarking;
    }

    public double getProgressPercent() {
        if (!benchmarking || targetDurationSeconds <= 0) return 0.0;
        long elapsed = System.currentTimeMillis() - startTimeMillis;
        return Math.min(100.0, (elapsed / (double) (targetDurationSeconds * 1000L)) * 100.0);
    }

    public int getRemainingSeconds() {
        if (!benchmarking) return 0;
        long elapsed = (System.currentTimeMillis() - startTimeMillis) / 1000L;
        return (int) Math.max(0, targetDurationSeconds - elapsed);
    }

    public List<BenchmarkResult> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public void clearHistory() {
        history.clear();
    }
}
