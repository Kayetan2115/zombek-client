package pl.zombek.client.core.netgraph;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LatencyTrackerTest {

    private LatencyTracker tracker;

    @BeforeEach
    public void setup() {
        tracker = new LatencyTracker(30);
    }

    @Test
    public void testInitialStateUnavailable() {
        assertEquals(-1, tracker.getCurrentPing());
        assertEquals(-1, tracker.getLastKnownPing());
        assertEquals(-1, tracker.getMinPing());
        assertEquals(-1, tracker.getMaxPing());
        assertEquals(-1.0, tracker.getAvgPing());
        assertEquals(-1.0, tracker.getCurrentJitter());
        assertEquals(-1.0, tracker.getAvgJitter());
        assertEquals(-1.0, tracker.getMaxJitter());
        assertFalse(tracker.isAvailable());
        assertFalse(tracker.isJitterAvailable());
        assertEquals(0, tracker.getSampleCount());
    }

    @Test
    public void testSingleplayerDoesNotPolluteSamples() {
        tracker.recordPing(-1);
        assertEquals(-1, tracker.getCurrentPing());
        assertFalse(tracker.isAvailable());
        assertEquals(0, tracker.getSampleCount());

        tracker.recordPing(45);
        assertEquals(45, tracker.getCurrentPing());
        assertEquals(45, tracker.getLastKnownPing());
        assertEquals(1, tracker.getSampleCount());

        // Negative ping (singleplayer) should keep lastKnownPing but mark current unavailable
        tracker.recordPing(-1);
        assertEquals(-1, tracker.getCurrentPing());
        assertEquals(45, tracker.getLastKnownPing());
        assertEquals(1, tracker.getSampleCount());
        assertEquals(45.0, tracker.getAvgPing());
    }

    @Test
    public void testJitterCalculationRFC3550() {
        // First sample: Jitter not available yet (requires >= 2 samples)
        tracker.recordPing(30);
        assertFalse(tracker.isJitterAvailable());
        assertEquals(-1.0, tracker.getCurrentJitter());

        // Second sample: Difference = |40 - 30| = 10
        tracker.recordPing(40);
        assertTrue(tracker.isJitterAvailable());
        assertEquals(10.0, tracker.getCurrentJitter(), 0.001);
        assertEquals(10.0, tracker.getAvgJitter(), 0.001);
        assertEquals(10.0, tracker.getMaxJitter(), 0.001);

        // Third sample: Difference = |32 - 40| = 8
        // EWMA: 10 + (8 - 10) / 16.0 = 10 - 0.125 = 9.875
        tracker.recordPing(32);
        assertEquals(9.875, tracker.getCurrentJitter(), 0.001);
        // Average jitter: (10 + 8) / 2 = 9.0
        assertEquals(9.0, tracker.getAvgJitter(), 0.001);
        assertEquals(10.0, tracker.getMaxJitter(), 0.001);
    }

    @Test
    public void testMinMaxAndAveragePing() {
        tracker.recordPing(50);
        tracker.recordPing(20);
        tracker.recordPing(80);

        assertEquals(20, tracker.getMinPing());
        assertEquals(80, tracker.getMaxPing());
        assertEquals(50.0, tracker.getAvgPing(), 0.001);
        assertEquals(3, tracker.getSampleCount());
    }

    @Test
    public void testCapacityEviction() {
        LatencyTracker smallTracker = new LatencyTracker(3);
        smallTracker.recordPing(10);
        smallTracker.recordPing(20);
        smallTracker.recordPing(30);
        assertEquals(3, smallTracker.getSampleCount());

        smallTracker.recordPing(100);
        assertEquals(3, smallTracker.getSampleCount());
        // sum should be 20 + 30 + 100 = 150 / 3 = 50.0
        assertEquals(50.0, smallTracker.getAvgPing(), 0.001);
        assertEquals(20, smallTracker.getMinPing());
        assertEquals(100, smallTracker.getMaxPing());
    }

    @Test
    public void testReset() {
        tracker.recordPing(50);
        tracker.recordPing(75);
        tracker.reset();

        assertEquals(-1, tracker.getCurrentPing());
        assertEquals(-1, tracker.getLastKnownPing());
        assertFalse(tracker.isAvailable());
        assertFalse(tracker.isJitterAvailable());
        assertEquals(0, tracker.getSampleCount());
    }

    @Test
    public void testSamplesWithinDuration() {
        long now = System.currentTimeMillis();
        tracker.recordPing(30, now - 5000);
        tracker.recordPing(40, now - 2000);
        tracker.recordPing(50, now);

        List<Integer> recent = tracker.getSamplesWithin(3000);
        assertEquals(2, recent.size());
        assertEquals(40, recent.get(0));
        assertEquals(50, recent.get(1));
    }
}
