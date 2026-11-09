package pl.zombek.client.core.info;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FpsHistoryTrackerTest {

    @Test
    public void testEmptyTrackerDefaults() {
        FpsHistoryTracker tracker = new FpsHistoryTracker(50);
        assertEquals(60, tracker.getCurrentFps());
        assertEquals(60, tracker.getMinFps());
        assertEquals(60, tracker.getMaxFps());
        assertEquals(60.0, tracker.getAvgFps(), 0.001);
        assertEquals(0, tracker.getSize());
    }

    @Test
    public void testRecordingSamplesAndStatistics() {
        FpsHistoryTracker tracker = new FpsHistoryTracker(10);
        tracker.recordSample(100);
        tracker.recordSample(60);
        tracker.recordSample(80);

        assertEquals(80, tracker.getCurrentFps());
        assertEquals(60, tracker.getMinFps());
        assertEquals(100, tracker.getMaxFps());
        assertEquals(80.0, tracker.getAvgFps(), 0.001);
        assertEquals(3, tracker.getSize());
    }

    @Test
    public void testRingBufferOverflow() {
        FpsHistoryTracker tracker = new FpsHistoryTracker(3);
        tracker.recordSample(50);
        tracker.recordSample(60);
        tracker.recordSample(70);
        assertEquals(3, tracker.getSize());
        assertEquals(60.0, tracker.getAvgFps(), 0.001);

        // Overflow: 50 is pushed out
        tracker.recordSample(80);
        assertEquals(3, tracker.getSize());
        assertEquals(80, tracker.getCurrentFps());
        assertEquals(60, tracker.getMinFps());
        assertEquals(80, tracker.getMaxFps());
        assertEquals(70.0, tracker.getAvgFps(), 0.001); // (60+70+80)/3 = 70.0
    }

    @Test
    public void testChronologicalExtraction() {
        FpsHistoryTracker tracker = new FpsHistoryTracker(4);
        tracker.recordSample(10);
        tracker.recordSample(20);
        tracker.recordSample(30);
        tracker.recordSample(40);
        tracker.recordSample(50); // buffer now holds 20, 30, 40, 50

        int[] dest = new int[4];
        int count = tracker.getChronologicalSamples(dest);
        assertEquals(4, count);
        assertArrayEquals(new int[]{20, 30, 40, 50}, dest);
    }

    @Test
    public void testReset() {
        FpsHistoryTracker tracker = new FpsHistoryTracker(10);
        tracker.recordSample(144);
        tracker.reset();
        assertEquals(0, tracker.getSize());
        assertEquals(60, tracker.getCurrentFps());
    }
}
