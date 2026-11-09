package pl.zombek.client.core.modules.visual.oneseven;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FirstPersonAnimationRendererTest {

    private FirstPersonAnimationRenderer renderer;
    private AnimationSettings settings;

    @BeforeEach
    void setUp() {
        renderer = FirstPersonAnimationRenderer.getInstance();
        settings = new AnimationSettings();
        settings.applyPresetDefaults(AnimationPreset.MINECRAFT_1_7);
        BlockingAnimationController.getInstance().reset();
    }

    @Test
    void testCalculateCombinedTransformSwordBlocking() {
        // Holding sword, right click down
        TransformOffset transform = renderer.calculateCombinedTransform(
                "minecraft:diamond_sword",
                true,
                0.0f,
                1.0f,
                false,
                0,
                true,
                false,
                1.0f,
                settings
        );

        assertNotNull(transform);
        assertNotEquals(0.0f, transform.getRotX());
        assertNotEquals(0.0f, transform.getRotY());
        assertNotEquals(0.0f, transform.getRotZ());
    }

    @Test
    void testResourcePackOverrideCheck() {
        // When resource pack mode is USE_RESOURCE_PACK and custom model exists, returns identity
        settings.setResourcePackTransformMode(ResourcePackTransformMode.USE_RESOURCE_PACK);
        TransformOffset transform = renderer.calculateCombinedTransform(
                "minecraft:diamond_sword",
                true,
                0.0f,
                1.0f,
                false,
                0,
                true,
                true, // hasCustomResourcePackModel
                1.0f,
                settings
        );
        assertTrue(transform.isIdentity());

        // When mode is OVERRIDE_RESOURCE_PACK, applies 1.7 transforms even with custom model
        settings.setResourcePackTransformMode(ResourcePackTransformMode.OVERRIDE_RESOURCE_PACK);
        TransformOffset overrideTransform = renderer.calculateCombinedTransform(
                "minecraft:diamond_sword",
                true,
                0.0f,
                1.0f,
                false,
                0,
                true,
                true,
                1.0f,
                settings
        );
        assertFalse(overrideTransform.isIdentity());
    }
}
