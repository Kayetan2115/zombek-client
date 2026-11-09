package pl.zombek.client.core.benchmark;

/**
 * Single frame sample containing rendering and memory metrics.
 */
public record BenchmarkSample(
        long timestampMillis,
        int fps,
        double frameTimeMs,
        long usedMemoryMb,
        long maxMemoryMb
) {}
