package pl.zombek.client.core.modules.visual.oneseven;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BlockingAnimationControllerTest {

    private BlockingAnimationController controller;
    private AnimationSettings settings;

    @BeforeEach
    void setUp() {
        controller = BlockingAnimationController.getInstance();
        controller.reset();
        settings = new AnimationSettings();
        settings.applyPresetDefaults(AnimationPreset.MINECRAFT_1_7);
    }

    @Test
    void testBlockingActivation() {
        assertFalse(controller.isBlockingActive());

        // Right clicking with sword activates blocking
        controller.update(true, false, true, settings);
        assertTrue(controller.isBlockingActive());

        // Releasing RMB deactivates
        controller.update(true, false, false, settings);
        // Gradually decays
        controller.reset();
        assertFalse(controller.isBlockingActive());
    }

    @Test
    void testBlockingIgnoredIfNotHoldingSword() {
        controller.update(false, false, true, settings);
        assertFalse(controller.isBlockingActive());
    }

    @Test
    void testCalculateBlockingTransform17() {
        controller.update(true, false, true, settings);
        // Force full progress
        for (int i = 0; i < 10; i++) {
            controller.update(true, false, true, settings);
        }

        TransformOffset transform = controller.calculateBlockingTransform(1.0f, settings, true);
        assertEquals(-0.14f, transform.getPosX(), 0.01f);
        assertEquals(0.15f, transform.getPosY(), 0.01f);
        assertEquals(-0.2f, transform.getPosZ(), 0.01f);
        assertEquals(-102.0f, transform.getRotX(), 0.1f);
        assertEquals(-59.0f, transform.getRotY(), 0.1f);
        assertEquals(79.0f, transform.getRotZ(), 0.1f);
    }

    @Test
    void testCalculateBlockhitOffset() {
        controller.update(true, false, true, settings);
        for (int i = 0; i < 5; i++) {
            controller.update(true, false, true, settings);
        }

        TransformOffset hitOffset = controller.calculateBlockhitOffset(0.5f, settings, true);
        assertNotEquals(0.0f, hitOffset.getRotX());
        assertNotEquals(0.0f, hitOffset.getRotY());
        assertNotEquals(0.0f, hitOffset.getRotZ());
    }
}
