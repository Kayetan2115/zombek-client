package pl.zombek.client.core.benchmark;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class PerformanceBenchmarkTest {

    private PerformanceBenchmarkManager benchmarkManager;

    @BeforeEach
    void setUp() {
        benchmarkManager = new PerformanceBenchmarkManager();
    }

    @Test
    void testBenchmarkCalculatesAccurateAveragesAndOnePercentLow() {
        benchmarkManager.startBenchmark("MAX_FPS", 60);
        assertTrue(benchmarkManager.isBenchmarking());

        // Simulate 100 frames:
        // 99 frames at 16.666 ms (~60 FPS) = 16_666_666 ns
        // 1 frame spike at 100.0 ms (10 FPS) = 100_000_000 ns
        for (int i = 0; i < 99; i++) {
            benchmarkManager.recordFrame(16_666_666L, 60);
        }
        benchmarkManager.recordFrame(100_000_000L, 10);

        BenchmarkResult result = benchmarkManager.stopBenchmark();
        assertFalse(benchmarkManager.isBenchmarking());
        assertNotNull(result);

        assertEquals("MAX_FPS", result.profileName());
        assertEquals(100, result.sampleCount());
        assertEquals(10, result.minFps());
        assertEquals(60, result.maxFps());

        // Peak frametime should be 100.0 ms
        assertEquals(100.0, result.peakFrameTimeMs(), 0.1);

        // 1% worst of 100 frames is exactly the 100ms frame
        // 1000 / 100ms = 10.0 FPS for 1% Low
        assertEquals(10.0, result.onePercentLowFps(), 0.5);

        // Avg frametime: (99 * 16.666 + 100) / 100 = ~17.5 ms -> ~57 FPS
        assertTrue(result.avgFps() > 50.0 && result.avgFps() < 60.0);
    }

    @Test
    void testWorldLoadTrackingAndListener() {
        AtomicReference<BenchmarkResult> completed = new AtomicReference<>();
        benchmarkManager.addCompletionListener(completed::set);

        benchmarkManager.markWorldLoadStart();
        try {
            Thread.sleep(10);
        } catch (InterruptedException ignored) {}
        benchmarkManager.markWorldLoadFinished();

        benchmarkManager.startBenchmark("BALANCED", 10);
        benchmarkManager.recordFrame(10_000_000L, 100);
        BenchmarkResult result = benchmarkManager.stopBenchmark();

        assertNotNull(result);
        assertEquals(result, completed.get());
        assertTrue(result.worldLoadTimeMs() >= 10);
    }
}
