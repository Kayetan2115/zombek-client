package pl.zombek.client.core.modules.visual.oneseven;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SneakAnimationControllerTest {

    private SneakAnimationController controller;
    private AnimationSettings settings;

    @BeforeEach
    void setUp() {
        controller = SneakAnimationController.getInstance();
        controller.reset();
        settings = new AnimationSettings();
        settings.applyPresetDefaults(AnimationPreset.MINECRAFT_1_7);
    }

    @Test
    void testInstantDownTransition() {
        settings.setSneakTransition(SneakTransition.INSTANT_DOWN_SMOOTH_UP);
        controller.update(true, settings);

        // In 1.7 instant down mode, progress snaps to 1.0f on the first tick
        assertEquals(1.0f, controller.getInterpolatedProgress(1.0f), 0.01f);
    }

    @Test
    void testSmoothUpTransition() {
        settings.setSneakTransition(SneakTransition.INSTANT_DOWN_SMOOTH_UP);
        controller.update(true, settings);
        assertEquals(1.0f, controller.getInterpolatedProgress(1.0f), 0.01f);

        // Released sneak: transitions smoothly back to 0.0f
        controller.update(false, settings);
        assertTrue(controller.getInterpolatedProgress(1.0f) < 1.0f);
        assertTrue(controller.getInterpolatedProgress(1.0f) >= 0.0f);
    }

    @Test
    void testCalculateEyeHeightDelta() {
        float delta = controller.calculateEyeHeightDelta(true, 0.5f, settings);
        assertNotNull(delta);
    }
}
