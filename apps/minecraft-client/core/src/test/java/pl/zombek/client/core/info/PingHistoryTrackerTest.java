package pl.zombek.client.core.info;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PingHistoryTrackerTest {

    @Test
    public void testInitialStateUnavailable() {
        PingHistoryTracker tracker = new PingHistoryTracker(30);
        assertEquals(-1, tracker.getCurrentPing());
        assertFalse(tracker.isAvailable());
        assertEquals(PingHistoryTracker.PingStatus.UNAVAILABLE, tracker.getStatus(50, 100, 200));
    }

    @Test
    public void testRecordingSamplesAndStatusClassification() {
        PingHistoryTracker tracker = new PingHistoryTracker(10);

        // Good
        tracker.recordSample(30);
        assertTrue(tracker.isAvailable());
        assertEquals(30, tracker.getCurrentPing());
        assertEquals(PingHistoryTracker.PingStatus.GOOD, tracker.getStatus(50, 100, 200));

        // Medium
        tracker.recordSample(75);
        assertEquals(PingHistoryTracker.PingStatus.MEDIUM, tracker.getStatus(50, 100, 200));

        // High
        tracker.recordSample(150);
        assertEquals(PingHistoryTracker.PingStatus.HIGH, tracker.getStatus(50, 100, 200));

        // Critical
        tracker.recordSample(250);
        assertEquals(PingHistoryTracker.PingStatus.CRITICAL, tracker.getStatus(50, 100, 200));

        // Check statistics
        assertEquals(30, tracker.getMinPing());
        assertEquals(250, tracker.getMaxPing());
        assertEquals((30 + 75 + 150 + 250) / 4.0, tracker.getAvgPing(), 0.001);
    }

    @Test
    public void testSingleplayerDoesNotPolluteAverage() {
        PingHistoryTracker tracker = new PingHistoryTracker(10);
        tracker.recordSample(40);
        tracker.recordSample(-1); // Singleplayer / disconnected

        assertEquals(-1, tracker.getCurrentPing());
        assertFalse(tracker.isAvailable());
        assertEquals(40.0, tracker.getAvgPing(), 0.001); // remains 40.0 from valid sample
    }

    @Test
    public void testReset() {
        PingHistoryTracker tracker = new PingHistoryTracker(10);
        tracker.recordSample(45);
        tracker.reset();
        assertEquals(-1, tracker.getCurrentPing());
        assertFalse(tracker.isAvailable());
    }
}
