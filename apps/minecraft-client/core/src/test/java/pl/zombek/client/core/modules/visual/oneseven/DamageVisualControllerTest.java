package pl.zombek.client.core.modules.visual.oneseven;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DamageVisualControllerTest {

    private DamageVisualController controller;
    private AnimationSettings settings;

    @BeforeEach
    void setUp() {
        if (pl.zombek.client.core.modules.visual.HitColorModule.getInstance() != null) {
            pl.zombek.client.core.modules.visual.HitColorModule.getInstance().setEnabled(false);
        }
        controller = DamageVisualController.getInstance();
        settings = new AnimationSettings();
        settings.applyPresetDefaults(AnimationPreset.MINECRAFT_1_7);
    }

    @Test
    void testShouldTintArmorWhenHurt() {
        assertTrue(controller.shouldTintArmor(5, settings));
        assertFalse(controller.shouldTintArmor(0, settings));

        settings.setEnableRedArmor(false);
        assertFalse(controller.shouldTintArmor(5, settings));
    }

    @Test
    void testCalculateArmorTintColor() {
        int color = controller.calculateArmorTintColor(8, 10, 0.0f, settings);
        assertNotEquals(0, color);

        // Alpha channel must be present
        int alpha = (color >> 24) & 0xFF;
        assertTrue(alpha > 0);

        // Red component must be prominent
        int red = (color >> 16) & 0xFF;
        assertEquals(255, red);
    }
}
