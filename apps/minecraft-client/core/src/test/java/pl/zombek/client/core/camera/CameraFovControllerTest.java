package pl.zombek.client.core.camera;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.modules.visual.FovChangerModule;
import pl.zombek.client.core.modules.visual.ZoomModule;

import static org.junit.jupiter.api.Assertions.*;

public class CameraFovControllerTest {

    private CameraFovController controller;
    private FovChangerModule fovModule;
    private ZoomModule zoomModule;

    @BeforeEach
    void setUp() {
        controller = CameraFovController.getInstance();
        controller.resetForTesting();
        fovModule = FovChangerModule.getInstance();
        fovModule.resetToDefaults();
        zoomModule = ZoomModule.getInstance();
        zoomModule.resetToDefaults();
        ZoomController.getInstance().resetForTesting();
    }

    @Test
    void testVanillaFallbackWhenDisabled() {
        fovModule.setEnabled(false);
        ZoomController.getInstance().setActive(false);

        double result = controller.computeFov(75.0, 70.0, 1.0f, false, false, false);
        assertEquals(75.0, result, 0.001, "When FOV Changer is disabled and Zoom is inactive, current FOV is preserved");
    }

    @Test
    void testPresetFov() {
        fovModule.setEnabled(true);
        fovModule.getInstantTransitionSetting().setValue(true);
        fovModule.getDisableDynamicFovSetting().setValue(true);

        fovModule.getPresetSetting().setValue(FovPreset.PRO_PVP);
        double pvpFov = controller.computeFov(70.0, 70.0, 1.0f, false, false, false);
        assertEquals(100.0, pvpFov, 0.001);

        fovModule.getPresetSetting().setValue(FovPreset.QUAKE_PRO);
        double quakeFov = controller.computeFov(70.0, 70.0, 1.0f, false, false, false);
        assertEquals(110.0, quakeFov, 0.001);
    }

    @Test
    void testCustomFov() {
        fovModule.setEnabled(true);
        fovModule.getInstantTransitionSetting().setValue(true);
        fovModule.getDisableDynamicFovSetting().setValue(true);
        fovModule.getPresetSetting().setValue(FovPreset.CUSTOM);
        fovModule.getCustomFovSetting().setValue(85.0);

        double customFov = controller.computeFov(70.0, 70.0, 1.0f, false, false, false);
        assertEquals(85.0, customFov, 0.001);
    }

    @Test
    void testDynamicFovMultipliers() {
        fovModule.setEnabled(true);
        fovModule.getInstantTransitionSetting().setValue(true);
        fovModule.getDisableDynamicFovSetting().setValue(false);
        fovModule.getPreserveVanillaDynamicFovSetting().setValue(false);
        fovModule.getPresetSetting().setValue(FovPreset.CUSTOM);
        fovModule.getCustomFovSetting().setValue(80.0);
        fovModule.getCustomSprintFovMultiplierSetting().setValue(1.15);
        fovModule.getCustomItemUseFovMultiplierSetting().setValue(0.80);

        // Sprinting
        double sprintFov = controller.computeFov(70.0, 70.0, 1.0f, true, true, false);
        assertEquals(80.0 * 1.15, sprintFov, 0.01);

        // Item use (e.g. pulling bow)
        double bowFov = controller.computeFov(70.0, 70.0, 1.0f, false, false, true);
        assertEquals(80.0 * 0.80, bowFov, 0.01);
    }

    @Test
    void testDisableDynamicFov() {
        fovModule.setEnabled(true);
        fovModule.getInstantTransitionSetting().setValue(true);
        fovModule.getDisableDynamicFovSetting().setValue(true);
        fovModule.getCustomFovSetting().setValue(90.0);
        fovModule.getPresetSetting().setValue(FovPreset.CUSTOM);

        // Even when sprinting, multiplier remains 1.0
        double sprintFov = controller.computeFov(70.0, 70.0, 1.0f, true, true, false);
        assertEquals(90.0, sprintFov, 0.001);
    }

    @Test
    void testZoomLayerIntegration() {
        fovModule.setEnabled(true);
        fovModule.getInstantTransitionSetting().setValue(true);
        fovModule.getDisableDynamicFovSetting().setValue(true);
        fovModule.getPresetSetting().setValue(FovPreset.CUSTOM);
        fovModule.getCustomFovSetting().setValue(100.0);

        zoomModule.setEnabled(true);
        zoomModule.getInstantZoomSetting().setValue(true);
        zoomModule.getDefaultZoomLevelSetting().setValue(4.0);

        ZoomController zoom = ZoomController.getInstance();
        zoom.setActive(true);

        double zoomedFov = controller.computeFov(70.0, 70.0, 1.0f, false, false, false);
        assertEquals(25.0, zoomedFov, 0.01, "100.0 FOV divided by 4.0x zoom factor should yield 25.0 FOV");
    }

    @Test
    void testSafeProjectionBoundsClamp() {
        fovModule.setEnabled(true);
        fovModule.getInstantTransitionSetting().setValue(true);
        fovModule.getPresetSetting().setValue(FovPreset.CUSTOM);

        // Extreme low (e.g., massive zoom factor on tiny base FOV)
        fovModule.getCustomFovSetting().setValue(30.0);
        zoomModule.setEnabled(true);
        zoomModule.getInstantZoomSetting().setValue(true);
        zoomModule.getDefaultZoomLevelSetting().setValue(30.0);
        ZoomController.getInstance().setActive(true);

        double minClamped = controller.computeFov(70.0, 70.0, 1.0f, false, false, false);
        assertTrue(minClamped >= CameraFovController.MIN_SAFE_FOV, "FOV must never fall below MIN_SAFE_FOV (5.0)");

        // Extreme high
        ZoomController.getInstance().setActive(false);
        fovModule.getCustomFovSetting().setValue(150.0);
        fovModule.getCustomSprintFovMultiplierSetting().setValue(1.5);
        double maxClamped = controller.computeFov(70.0, 70.0, 1.0f, true, true, false);
        assertTrue(maxClamped <= CameraFovController.MAX_SAFE_FOV, "FOV must never exceed MAX_SAFE_FOV (170.0)");
    }
}
