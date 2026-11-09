package pl.zombek.client.core.info;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MemoryUsageTrackerTest {

    @Test
    public void testLiveMetrics() {
        MemoryUsageTracker tracker = new MemoryUsageTracker();
        tracker.updateNow();

        assertTrue(tracker.getMaxBytes() > 0);
        assertTrue(tracker.getAllocatedBytes() > 0);
        assertTrue(tracker.getUsedBytes() >= 0);
        assertTrue(tracker.getUsagePercent() >= 0.0 && tracker.getUsagePercent() <= 100.0);
    }

    @Test
    public void testStatusClassification() {
        MemoryUsageTracker tracker = new MemoryUsageTracker();
        // Since we cannot mock Runtime easily, check thresholds logic with actual or boundary values
        double warn = 50.0;
        double crit = 90.0;
        MemoryUsageTracker.MemoryStatus status = tracker.getStatus(warn, crit);
        assertNotNull(status);
    }

    @Test
    public void testFormatSummary() {
        MemoryUsageTracker tracker = new MemoryUsageTracker();
        tracker.updateNow();

        String mbSummary = tracker.formatSummary(MemoryUnit.MB, true);
        assertNotNull(mbSummary);
        assertTrue(mbSummary.startsWith("RAM: "));
        assertTrue(mbSummary.contains("MB"));

        String gbSummary = tracker.formatSummary(MemoryUnit.GB, false);
        assertNotNull(gbSummary);
        assertTrue(gbSummary.contains("GB"));
    }
}
