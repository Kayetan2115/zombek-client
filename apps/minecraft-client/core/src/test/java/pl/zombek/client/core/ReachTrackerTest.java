package pl.zombek.client.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.tracking.ReachEventType;
import pl.zombek.client.core.tracking.ReachRecord;
import pl.zombek.client.core.tracking.ReachTracker;

import static org.junit.jupiter.api.Assertions.*;

public class ReachTrackerTest {
    private ReachTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = ReachTracker.getInstance();
        tracker.resetStatistics();
        tracker.setMaxHistoryEntries(10);
    }

    @Test
    void testDistanceToAABBClamping() {
        // Target box from (10, 0, 10) to (12, 2, 12)
        double minX = 10.0, minY = 0.0, minZ = 10.0;
        double maxX = 12.0, maxY = 2.0, maxZ = 12.0;

        // 1. Eye directly aligned with +X face at x=15 (distance = 15 - 12 = 3.0)
        double d1 = ReachTracker.calculateDistanceToAABB(15.0, 1.0, 11.0, minX, minY, minZ, maxX, maxY, maxZ);
        assertEquals(3.0, d1, 0.001);

        // 2. Eye inside box -> distance = 0.0
        double d2 = ReachTracker.calculateDistanceToAABB(11.0, 1.0, 11.0, minX, minY, minZ, maxX, maxY, maxZ);
        assertEquals(0.0, d2, 0.001);

        // 3. Eye diagonally offset from corner (12, 2, 12) at (15, 6, 12): dx=3, dy=4, dz=0 -> dist=5.0
        double d3 = ReachTracker.calculateDistanceToAABB(15.0, 6.0, 12.0, minX, minY, minZ, maxX, maxY, maxZ);
        assertEquals(5.0, d3, 0.001);
    }

    @Test
    void testRecordAttackAttemptAndObservedHit() {
        tracker.recordAttackAttempt(2.85, 101, "Zombie");
        assertEquals(2.85, tracker.getLastReach(), 0.001);
        assertEquals(0.0, tracker.getLastObservedHitDistance(), 0.001); // No observed hit yet

        tracker.recordObservedHit(3.15, 102, "Player1");
        assertEquals(3.15, tracker.getLastReach(), 0.001);
        assertEquals(3.15, tracker.getLastObservedHitDistance(), 0.001);

        // Record another attempt without hit
        tracker.recordAttackAttempt(2.50, 103, "Skeleton");
        assertEquals(2.50, tracker.getLastReach(), 0.001);
        assertEquals(3.15, tracker.getLastObservedHitDistance(), 0.001); // Retains last verified hit
    }

    @Test
    void testAggregatesAndHistoryCapping() {
        tracker.setMaxHistoryEntries(3);

        tracker.recordObservedHit(2.0, 1, "E1");
        tracker.recordObservedHit(4.0, 2, "E2");
        tracker.recordObservedHit(3.0, 3, "E3");

        assertEquals(3, tracker.getHistory().size());
        assertEquals(3.0, tracker.getAverageReach(), 0.001);
        assertEquals(4.0, tracker.getMaximumReach(), 0.001);
        assertEquals(2.0, tracker.getMinimumReach(), 0.001);

        // Add 4th entry -> oldest (2.0) evicted
        tracker.recordObservedHit(5.0, 4, "E4");
        assertEquals(3, tracker.getHistory().size());
        assertEquals(4.0, tracker.getAverageReach(), 0.001); // (4+3+5)/3 = 4.0
        assertEquals(5.0, tracker.getMaximumReach(), 0.001);
        assertEquals(3.0, tracker.getMinimumReach(), 0.001);
    }

    @Test
    void testResetStatistics() {
        tracker.recordObservedHit(3.0, 1, "E1");
        tracker.setCurrentTargetDistance(2.5);

        tracker.resetStatistics();
        assertEquals(0, tracker.getHistory().size());
        assertEquals(0.0, tracker.getLastReach(), 0.001);
        assertEquals(0.0, tracker.getCurrentTargetDistance(), 0.001);
    }
}
