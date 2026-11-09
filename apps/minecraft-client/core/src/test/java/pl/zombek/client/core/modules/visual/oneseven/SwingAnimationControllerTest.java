package pl.zombek.client.core.modules.visual.oneseven;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SwingAnimationControllerTest {

    private SwingAnimationController controller;
    private AnimationSettings settings;

    @BeforeEach
    void setUp() {
        controller = SwingAnimationController.getInstance();
        settings = new AnimationSettings();
        settings.applyPresetDefaults(AnimationPreset.MINECRAFT_1_7);
    }

    @Test
    void testResolveSwingProgress() {
        assertEquals(0.0f, controller.resolveSwingProgress(0.0f, settings));
        assertEquals(0.5f, controller.resolveSwingProgress(0.5f, settings));

        settings.setSwingSpeed(1.5f);
        assertEquals(0.75f, controller.resolveSwingProgress(0.5f, settings));
    }

    @Test
    void testCalculateSwingTransform() {
        TransformOffset transform = controller.calculateSwingTransform(0.5f, settings, true);
        assertNotEquals(0.0f, transform.getRotX());
        assertNotEquals(0.0f, transform.getRotY());
        assertNotEquals(0.0f, transform.getRotZ());
    }

    @Test
    void testDisabledSwingReturnsIdentity() {
        settings.setEnableOldSwing(false);
        TransformOffset transform = controller.calculateSwingTransform(0.5f, settings, true);
        assertTrue(transform.isIdentity());
    }
}
