package pl.zombek.client.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.tracking.ComboTracker;

import static org.junit.jupiter.api.Assertions.*;

public class ComboTrackerTest {

    private ComboTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = ComboTracker.getInstance();
        tracker.resetAll();
    }

    @Test
    void testInitialState() {
        assertEquals(0, tracker.getCurrentCombo());
        assertEquals(0, tracker.getBestCombo());
        assertEquals(-1, tracker.getTargetEntityId());
        assertFalse(tracker.isComboActive());
    }

    @Test
    void testRecordHitsOnSameTarget() throws InterruptedException {
        int entityId = 101;

        tracker.recordHit(entityId, true);
        assertEquals(1, tracker.getCurrentCombo());
        assertEquals(1, tracker.getBestCombo());
        assertEquals(entityId, tracker.getTargetEntityId());
        assertTrue(tracker.isComboActive());

        // Wait over 50ms interval to register second hit
        Thread.sleep(60);

        tracker.recordHit(entityId, true);
        assertEquals(2, tracker.getCurrentCombo());
        assertEquals(2, tracker.getBestCombo());
    }

    @Test
    void testDuplicateHitSuppressionWithin50ms() {
        int entityId = 202;

        tracker.recordHit(entityId, true);
        assertEquals(1, tracker.getCurrentCombo());

        // Immediate subsequent call (same tick / < 50ms) should be suppressed
        tracker.recordHit(entityId, true);
        assertEquals(1, tracker.getCurrentCombo());
    }

    @Test
    void testTargetChangeReset() throws InterruptedException {
        int entityA = 301;
        int entityB = 302;

        tracker.recordHit(entityA, true);
        Thread.sleep(60);
        tracker.recordHit(entityA, true);
        assertEquals(2, tracker.getCurrentCombo());
        assertEquals(2, tracker.getBestCombo());

        Thread.sleep(60);
        // Hit new entity with resetOnTargetChange = true -> resets to 1
        tracker.recordHit(entityB, true);
        assertEquals(1, tracker.getCurrentCombo());
        assertEquals(2, tracker.getBestCombo()); // Best combo remains 2
        assertEquals(entityB, tracker.getTargetEntityId());
    }

    @Test
    void testMissReset() {
        int entityId = 401;

        tracker.recordHit(entityId, false);
        assertEquals(1, tracker.getCurrentCombo());
        assertTrue(tracker.isComboActive());

        // Miss with resetOnMiss = true -> combo breaks
        tracker.recordMiss(true);
        assertEquals(0, tracker.getCurrentCombo());
        assertFalse(tracker.isComboActive());
    }

    @Test
    void testTimeoutExpiration() throws InterruptedException {
        tracker.recordHit(501, false);
        assertEquals(1, tracker.getCurrentCombo());
        assertTrue(tracker.isComboActive());

        // Wait 150ms and tick with 100ms timeout -> combo should expire
        Thread.sleep(150);
        tracker.onTick(100);

        assertEquals(0, tracker.getCurrentCombo());
        assertFalse(tracker.isComboActive());
    }
}
