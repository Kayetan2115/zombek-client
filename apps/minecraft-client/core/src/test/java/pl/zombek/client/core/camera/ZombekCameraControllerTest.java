package pl.zombek.client.core.camera;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.modules.visual.FovChangerModule;
import pl.zombek.client.core.modules.visual.HurtCamModule;
import pl.zombek.client.core.modules.visual.MotionBlurModule;
import pl.zombek.client.core.modules.visual.ZoomModule;

import static org.junit.jupiter.api.Assertions.*;

public class ZombekCameraControllerTest {

    private ZombekCameraController cameraController;

    @BeforeEach
    void setUp() {
        cameraController = ZombekCameraController.getInstance();
        cameraController.resetAll();

        ZoomModule.getInstance().resetToDefaults();
        FovChangerModule.getInstance().resetToDefaults();
        MotionBlurModule.getInstance().resetToDefaults();
        HurtCamModule.getInstance().resetToDefaults();
    }

    @Test
    void testCompositeCameraStateSnapshot() {
        // Base state
        CameraState state = cameraController.getCameraState(70.0, 180.0f, 0.0f, 0);
        assertNotNull(state);
        assertEquals(70.0, state.getVanillaFov(), 0.001);
        assertEquals(180.0f, state.getVanillaYaw(), 0.001f);
        assertEquals(0.0f, state.getVanillaPitch(), 0.001f);
        assertFalse(state.isZoomActive());
        assertEquals(1.0, state.getZoomFactor(), 0.001);
        assertFalse(state.isMotionBlurActive());
    }

    @Test
    void testCameraEffectPriorityHierarchy() {
        // Step 1: Base FOV 70.0
        // Step 2: FOV Changer set to 90.0
        FovChangerModule fovMod = FovChangerModule.getInstance();
        fovMod.setEnabled(true);
        fovMod.getInstantTransitionSetting().setValue(true);
        fovMod.getDisableDynamicFovSetting().setValue(true);
        fovMod.getPresetSetting().setValue(FovPreset.CUSTOM);
        fovMod.getCustomFovSetting().setValue(90.0);

        // Step 3: Zoom set to 3.0x
        ZoomModule zoomMod = ZoomModule.getInstance();
        zoomMod.setEnabled(true);
        zoomMod.getInstantZoomSetting().setValue(true);
        zoomMod.getDefaultZoomLevelSetting().setValue(3.0);
        cameraController.getZoomController().setActive(true);

        // Effective FOV must be (90.0 / 3.0) = 30.0
        CameraState state = cameraController.getCameraState(70.0, 0.0f, 0.0f, 0);
        assertEquals(30.0, state.getEffectiveFov(), 0.01, "FOV Changer (90.0) divided by Zoom (3.0x) must yield 30.0");
    }

    @Test
    void testLifecycleRestoration() {
        ZoomModule.getInstance().setEnabled(true);
        ZoomModule.getInstance().getResetOnFocusLostSetting().setValue(true);
        ZoomModule.getInstance().getResetOnWorldChangeSetting().setValue(true);
        cameraController.getZoomController().setActive(true);
        assertTrue(cameraController.getZoomController().isActive());

        // Focus lost resets zoom
        cameraController.onFocusLost();
        assertFalse(cameraController.getZoomController().isActive());

        // Reactivate and test world change
        cameraController.getZoomController().setActive(true);
        cameraController.onWorldChanged();
        assertFalse(cameraController.getZoomController().isActive());

        // Reactivate and test respawn
        cameraController.getZoomController().setActive(true);
        cameraController.onPlayerRespawn();
        assertFalse(cameraController.getZoomController().isActive());
    }
}
