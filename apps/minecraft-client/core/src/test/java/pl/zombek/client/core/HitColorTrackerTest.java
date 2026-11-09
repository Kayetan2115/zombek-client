package pl.zombek.client.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.data.TargetEntityType;
import pl.zombek.client.core.modules.visual.FadeCurve;
import pl.zombek.client.core.modules.visual.HitColorModule;
import pl.zombek.client.core.tracking.HitColorRecord;
import pl.zombek.client.core.tracking.HitColorTracker;

import static org.junit.jupiter.api.Assertions.*;

public class HitColorTrackerTest {

    private HitColorTracker tracker;
    private HitColorModule module;

    @BeforeEach
    void setUp() {
        tracker = HitColorTracker.getInstance();
        tracker.resetForTesting();
        module = new HitColorModule();
        module.setEnabled(true);
    }

    @AfterEach
    void tearDown() {
        if (module != null) {
            module.setEnabled(false);
        }
        tracker.resetForTesting();
    }

    @Test
    void testBasicHitRecordingAndAlphaDecay() {
        long startTime = 10_000L;
        tracker.recordHit(
                42,
                TargetEntityType.PLAYER,
                true,
                true,
                startTime,
                200L, // 200ms duration
                100L, // 100ms fade
                0xFFFF0000,
                FadeCurve.LINEAR,
                1.0f,
                1.0f
        );

        assertEquals(1, tracker.getActiveEffectCount());

        // At t = 10_100ms (within duration) -> full opacity
        Integer c1 = tracker.getActiveColor(42, 10_100L);
        assertNotNull(c1);
        int a1 = (c1 >> 24) & 0xFF;
        assertEquals(255, a1);

        // At t = 10_250ms (50ms into 100ms fade) -> ~50% opacity
        Integer c2 = tracker.getActiveColor(42, 10_250L);
        assertNotNull(c2);
        int a2 = (c2 >> 24) & 0xFF;
        assertTrue(a2 > 100 && a2 < 150, "Alpha should be roughly half in the middle of linear fade");

        // At t = 10_350ms (after duration + fade) -> expired and null
        Integer c3 = tracker.getActiveColor(42, 10_350L);
        assertNull(c3);
        assertEquals(0, tracker.getActiveEffectCount(), "Expired effect should be removed");
    }

    @Test
    void testOnlyTargetedEntityFilter() {
        module.getOnlyTargetedEntitySetting().setValue(true);

        long now = 1000L;
        // Entity 1: targeted
        tracker.recordHit(1, TargetEntityType.PLAYER, true, true, now, 200L, 100L, 0xFFFF0000, FadeCurve.LINEAR, 1.0f, 1.0f);
        // Entity 2: not targeted
        tracker.recordHit(2, TargetEntityType.PLAYER, true, false, now, 200L, 100L, 0xFFFF0000, FadeCurve.LINEAR, 1.0f, 1.0f);

        assertNotNull(tracker.getActiveColor(1, now));
        assertNull(tracker.getActiveColor(2, now), "Non-targeted entity should be filtered out");
    }

    @Test
    void testOnlyLocalPlayerDamageFilter() {
        module.getOnlyLocalPlayerDamageSetting().setValue(true);

        long now = 1000L;
        // Entity 1: hit by local player
        tracker.recordHit(1, TargetEntityType.HOSTILE, true, true, now, 200L, 100L, 0xFFFF0000, FadeCurve.LINEAR, 1.0f, 1.0f);
        // Entity 2: hit by environment/another mob
        tracker.recordHit(2, TargetEntityType.HOSTILE, false, true, now, 200L, 100L, 0xFFFF0000, FadeCurve.LINEAR, 1.0f, 1.0f);

        assertNotNull(tracker.getActiveColor(1, now));
        assertNull(tracker.getActiveColor(2, now), "Damage not caused by local player should be filtered out");
    }

    @Test
    void testEntityTypeFilter() {
        long now = 1000L;
        tracker.recordHit(1, TargetEntityType.PLAYER, true, true, now, 200L, 100L, 0xFFFF0000, FadeCurve.LINEAR, 1.0f, 1.0f);
        tracker.recordHit(2, TargetEntityType.HOSTILE, true, true, now, 200L, 100L, 0xFFFF0000, FadeCurve.LINEAR, 1.0f, 1.0f);

        module.getPlayersSetting().setValue(false);
        assertNull(tracker.getActiveColor(1, now), "Player entity should be filtered when players=false");
        assertNotNull(tracker.getActiveColor(2, now), "Hostile mob should still be shown");
    }

    @Test
    void testMaxActiveEffectsCapping() {
        module.getMaxActiveEffectsSetting().setValue(3.0);
        long now = 5000L;

        tracker.recordHit(1, TargetEntityType.PLAYER, true, true);
        tracker.recordHit(2, TargetEntityType.PLAYER, true, true);
        tracker.recordHit(3, TargetEntityType.PLAYER, true, true);
        tracker.recordHit(4, TargetEntityType.PLAYER, true, true); // Should be rejected / capped at 3

        assertTrue(tracker.getActiveEffectCount() <= 3);
    }
}
