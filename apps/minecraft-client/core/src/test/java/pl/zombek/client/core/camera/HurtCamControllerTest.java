package pl.zombek.client.core.camera;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.modules.visual.HurtCamModule;

import static org.junit.jupiter.api.Assertions.*;

public class HurtCamControllerTest {

    private HurtCamController controller;
    private HurtCamModule module;

    @BeforeEach
    void setUp() {
        controller = HurtCamController.getInstance();
        controller.resetForTesting();
        module = HurtCamModule.getInstance();
        module.resetToDefaults();
        module.setEnabled(true);
    }

    @Test
    void testVanillaMode() {
        module.getModeSetting().setValue(HurtCamMode.VANILLA);
        module.getSmoothRecoverySetting().setValue(false);

        assertFalse(controller.shouldCancelHurtCam());
        assertEquals(1.0f, controller.getHurtTiltMultiplier(), 0.001f);
        assertEquals(1.0f, controller.getHurtShakeMultiplier(), 0.001f);
    }

    @Test
    void testDisabledMode() {
        module.getModeSetting().setValue(HurtCamMode.DISABLED);

        assertTrue(controller.shouldCancelHurtCam());
        assertEquals(0.0f, controller.getHurtTiltMultiplier(), 0.001f);
        assertEquals(0.0f, controller.getHurtShakeMultiplier(), 0.001f);
    }

    @Test
    void testReducedMode() {
        module.getModeSetting().setValue(HurtCamMode.REDUCED);
        module.getSmoothRecoverySetting().setValue(false);

        assertFalse(controller.shouldCancelHurtCam());
        assertEquals(0.5f, controller.getHurtTiltMultiplier(), 0.001f);
        assertEquals(0.5f, controller.getHurtShakeMultiplier(), 0.001f);
    }

    @Test
    void testCustomMode() {
        module.getModeSetting().setValue(HurtCamMode.CUSTOM);
        module.getSmoothRecoverySetting().setValue(false);
        module.getCustomIntensitySetting().setValue(0.8);
        module.getCameraTiltSetting().setValue(0.5);
        module.getCameraShakeSetting().setValue(0.25);

        assertFalse(controller.shouldCancelHurtCam());
        assertEquals(0.8f * 0.5f, controller.getHurtTiltMultiplier(), 0.001f);
        assertEquals(0.8f * 0.25f, controller.getHurtShakeMultiplier(), 0.001f);
    }

    @Test
    void testSmoothRecoveryDecay() throws InterruptedException {
        module.getModeSetting().setValue(HurtCamMode.REDUCED);
        module.getSmoothRecoverySetting().setValue(true);
        module.getRecoveryDurationSetting().setValue(80.0); // 80ms duration
        module.getEasingSetting().setValue(HurtCamEasing.LINEAR);

        controller.onDamageTaken();
        float initialTilt = controller.getHurtTiltMultiplier();
        assertTrue(initialTilt > 0.0f && initialTilt <= 0.5f, "Initial tilt should be active");

        // Wait past recovery duration
        Thread.sleep(100);
        float decayedTilt = controller.getHurtTiltMultiplier();
        assertEquals(0.0f, decayedTilt, 0.001f, "Tilt multiplier must decay to 0.0 after recovery duration");
    }

    @Test
    void testDisabledModuleBehavesAsVanilla() {
        module.setEnabled(false);
        module.getModeSetting().setValue(HurtCamMode.DISABLED);

        // When module is disabled, it should not cancel hurt cam and use 1.0f multiplier
        assertFalse(controller.shouldCancelHurtCam());
        assertEquals(1.0f, controller.getHurtTiltMultiplier(), 0.001f);
    }
}
