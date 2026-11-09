package pl.zombek.client.core.benchmark;

import java.time.Instant;
import java.util.Locale;

/**
 * Consolidated results from a completed benchmark run.
 */
public record BenchmarkResult(
        String profileName,
        int durationSeconds,
        int sampleCount,
        double avgFps,
        int minFps,
        int maxFps,
        double onePercentLowFps,
        double avgFrameTimeMs,
        double peakFrameTimeMs,
        long avgMemoryMb,
        long peakMemoryMb,
        long worldLoadTimeMs,
        Instant completedAt
) {
    public String getFormattedSummary() {
        return String.format(Locale.US,
                "Benchmark [%s] - Avg FPS: %.1f | 1%% Low: %.1f | Min/Max: %d/%d | Frametime: %.2f ms | RAM: %d MB",
                profileName, avgFps, onePercentLowFps, minFps, maxFps, avgFrameTimeMs, avgMemoryMb);
    }
}
