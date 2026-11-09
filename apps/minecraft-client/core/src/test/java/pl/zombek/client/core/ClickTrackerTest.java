package pl.zombek.client.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.tracking.ClickTracker;

import static org.junit.jupiter.api.Assertions.*;

public class ClickTrackerTest {

    private ClickTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = ClickTracker.getInstance();
        tracker.reset();
    }

    @Test
    void testClickRegistrationAndCpsCount() {
        assertEquals(0, tracker.getCpsLmb());
        assertEquals(0, tracker.getCpsRmb());

        tracker.recordLeftClick();
        tracker.recordLeftClick();
        tracker.recordLeftClick();

        tracker.recordRightClick();

        assertEquals(3, tracker.getCpsLmb());
        assertEquals(1, tracker.getCpsRmb());
    }

    @Test
    void testMaxCpsTrackingAndReset() {
        assertEquals(0, tracker.getMaxLeftCps());
        assertEquals(0, tracker.getMaxRightCps());

        for (int i = 0; i < 15; i++) {
            tracker.recordLeftClick();
        }
        for (int i = 0; i < 8; i++) {
            tracker.recordRightClick();
        }

        assertEquals(15, tracker.getMaxLeftCps());
        assertEquals(8, tracker.getMaxRightCps());

        tracker.resetMaxCps();
        assertEquals(0, tracker.getMaxLeftCps());
        assertEquals(0, tracker.getMaxRightCps());
    }

    @Test
    void testFullResetClearsState() {
        tracker.recordLeftClick();
        tracker.recordRightClick();
        assertTrue(tracker.getCpsLmb() > 0);

        tracker.reset();
        assertEquals(0, tracker.getCpsLmb());
        assertEquals(0, tracker.getCpsRmb());
        assertEquals(0, tracker.getMaxLeftCps());
        assertEquals(0, tracker.getMaxRightCps());
    }
}
