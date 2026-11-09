package pl.zombek.client.core.modules.visual.oneseven;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.modules.pvp.snaplook.SnaplookController;
import pl.zombek.client.core.modules.visual.FreelookCameraController;
import pl.zombek.client.core.modules.visual.HitColorModule;
import pl.zombek.client.core.movement.MovementToggleController;

import static org.junit.jupiter.api.Assertions.*;

public class AnimationCompatibilityTest {

    private AnimationSettings settings;

    @BeforeEach
    void setUp() {
        FreelookCameraController.getInstance().stopFreelook(false, 0, null, 0.0f, 0.0f);
        SnaplookController.getInstance().reset();
        if (HitColorModule.getInstance() != null) {
            HitColorModule.getInstance().setEnabled(false);
        }
        settings = new AnimationSettings();
        settings.applyPresetDefaults(AnimationPreset.MINECRAFT_1_7);
    }

    @Test
    void testFreelookAndSnaplookCameraNonInterference() {
        FreelookCameraController freelook = FreelookCameraController.getInstance();
        SnaplookController snaplook = SnaplookController.getInstance();

        // 1.7 Sneak only calculates eye-height delta and never modifies camera yaw/pitch angles
        float delta = SneakAnimationController.getInstance().calculateEyeHeightDelta(true, 1.0f, settings);
        assertNotNull(delta);

        // Verify camera controllers remain unaffected
        assertFalse(freelook.isControllingCamera());
        assertFalse(snaplook.isActive());
    }

    @Test
    void testToggleSneakCoexistence() {
        MovementToggleController movement = MovementToggleController.getInstance();
        movement.resetSneak();

        // SneakAnimationController does not alter movement sneaking flags
        SneakAnimationController sneakAnim = SneakAnimationController.getInstance();
        sneakAnim.update(true, settings);

        // Physical and toggle sneak state in movement controller is not corrupted
        assertFalse(movement.isPhysicalSneakDown());
    }

    @Test
    void testHitColorCoordinationWithRedArmor() {
        DamageVisualController dmgController = DamageVisualController.getInstance();
        HitColorModule hitColor = new HitColorModule();
        hitColor.setEnabled(true);
        hitColor.getHitColorSetting().setValue(0xFF00FF00); // Green custom hit color

        // DamageVisualController respects HitColorModule custom tint color when active
        int tint = dmgController.calculateArmorTintColor(5, 10, 0.0f, settings);
        int greenComponent = (tint >> 8) & 0xFF;
        assertEquals(255, greenComponent);
        hitColor.setEnabled(false);
    }
}
