package pl.zombek.client.core.camera;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.modules.visual.ZoomModule;

import static org.junit.jupiter.api.Assertions.*;

public class ZoomControllerTest {

    private ZoomController controller;
    private ZoomModule module;

    @BeforeEach
    void setUp() {
        controller = ZoomController.getInstance();
        controller.resetForTesting();
        module = ZoomModule.getInstance();
        module.resetToDefaults();
        module.setEnabled(true);
    }

    @Test
    void testHoldMode() {
        module.getActivationModeSetting().setValue(ZoomActivationMode.HOLD);

        controller.onKeyPressed();
        assertTrue(controller.isActive());

        controller.onKeyReleased();
        assertFalse(controller.isActive());
    }

    @Test
    void testToggleMode() {
        module.getActivationModeSetting().setValue(ZoomActivationMode.TOGGLE);

        controller.onKeyPressed();
        assertTrue(controller.isActive());

        controller.onKeyReleased();
        assertTrue(controller.isActive(), "Release should not deactivate in TOGGLE mode");

        controller.onKeyPressed();
        assertFalse(controller.isActive(), "Second press should deactivate in TOGGLE mode");
    }

    @Test
    void testToggleHoldModeShortPress() {
        module.getActivationModeSetting().setValue(ZoomActivationMode.TOGGLE_HOLD);

        controller.onKeyPressed();
        assertTrue(controller.isActive());

        // Fast release under 250ms -> acts as toggle (stays active)
        controller.onKeyReleased();
        assertTrue(controller.isActive(), "Short release in TOGGLE_HOLD should keep zoom active");

        controller.onKeyPressed();
        assertFalse(controller.isActive(), "Subsequent press should toggle zoom off");
    }

    @Test
    void testScrollZoom() {
        module.getScrollZoomSetting().setValue(true);
        module.getDefaultZoomLevelSetting().setValue(4.0);
        module.getMinZoomLevelSetting().setValue(1.5);
        module.getMaxZoomLevelSetting().setValue(10.0);
        module.getScrollStepSetting().setValue(1.0);
        module.getScrollSensitivitySetting().setValue(1.0);

        controller.setActive(true);
        assertEquals(4.0, controller.getTargetZoomFactor(), 0.001);

        // Scroll up
        controller.onMouseScroll(1.0);
        assertEquals(5.0, controller.getTargetZoomFactor(), 0.001);

        // Scroll down
        controller.onMouseScroll(-2.0);
        assertEquals(3.0, controller.getTargetZoomFactor(), 0.001);

        // Clamping to min
        controller.onMouseScroll(-10.0);
        assertEquals(1.5, controller.getTargetZoomFactor(), 0.001);

        // Clamping to max
        controller.onMouseScroll(20.0);
        assertEquals(10.0, controller.getTargetZoomFactor(), 0.001);
    }

    @Test
    void testScrollZoomDisabledWhenConfigured() {
        module.getScrollZoomSetting().setValue(false);
        module.getDefaultZoomLevelSetting().setValue(4.0);
        controller.setActive(true);

        controller.onMouseScroll(5.0);
        assertEquals(4.0, controller.getTargetZoomFactor(), 0.001, "Scroll must be ignored when scroll zoom is disabled");
    }

    @Test
    void testResetZoom() {
        module.getDefaultZoomLevelSetting().setValue(4.0);
        controller.setActive(true);
        controller.onMouseScroll(3.0);
        assertNotEquals(4.0, controller.getTargetZoomFactor());

        controller.resetZoom();
        assertEquals(4.0, controller.getTargetZoomFactor(), 0.001);
    }

    @Test
    void testLifecycleHandlers() {
        controller.setActive(true);
        assertTrue(controller.isActive());

        // Focus lost with reset enabled
        module.getResetOnFocusLostSetting().setValue(true);
        controller.onFocusLost();
        assertFalse(controller.isActive());

        // World change
        controller.setActive(true);
        module.getResetOnWorldChangeSetting().setValue(true);
        controller.onWorldChanged();
        assertFalse(controller.isActive());

        // GUI opened
        controller.setActive(true);
        module.getDisableInGuiSetting().setValue(true);
        controller.onGuiOpened();
        assertFalse(controller.isActive());
    }

    @Test
    void testInstantZoomVsSmoothInterpolation() {
        module.getInstantZoomSetting().setValue(true);
        module.getDefaultZoomLevelSetting().setValue(5.0);

        controller.setActive(true);
        controller.updateAnimation();
        assertEquals(5.0, controller.getCurrentZoomFactor(), 0.001, "Instant zoom jumps immediately to target");

        // Now with smooth zoom
        module.getInstantZoomSetting().setValue(false);
        module.getSmoothZoomSetting().setValue(true);
        controller.setActive(false);
        controller.resetForTesting();
        controller.setActive(true);
        // Initially before animation completes, current zoom starts at 1.0 and increases
        assertTrue(controller.getCurrentZoomFactor() < 5.0);
    }
}
