package pl.zombek.client.core.modules.visual.oneseven;

import pl.zombek.client.core.modules.visual.HitColorModule;

/**
 * Controls visual damage flash tinting on armor layers (Red Armor on Hit).
 * Purely visual client effect; does not alter health, damage taken, or server state.
 */
public class DamageVisualController {
    private static DamageVisualController instance;

    public static synchronized DamageVisualController getInstance() {
        if (instance == null) {
            instance = new DamageVisualController();
        }
        return instance;
    }

    public boolean shouldTintArmor(int hurtTime, AnimationSettings settings) {
        if (!settings.isEnableRedArmor() || hurtTime <= 0) {
            return false;
        }
        return settings.isBodyAndArmor() || settings.isArmorOnly();
    }

    /**
     * Calculates the ARGB tint color to apply to armor model rendering during hurt animation.
     */
    public int calculateArmorTintColor(int hurtTime, int maxHurtTime, float partialTicks, AnimationSettings settings) {
        if (!shouldTintArmor(hurtTime, settings)) {
            return 0x00000000;
        }

        int baseColor = settings.getDamageTintColor();
        // Check if HitColorModule has a custom color configured
        HitColorModule hitColorMod = HitColorModule.getInstance();
        if (hitColorMod != null && hitColorMod.isEnabled()) {
            baseColor = hitColorMod.getHitColorSetting().getValue();
        }

        float max = Math.max(1.0f, maxHurtTime > 0 ? maxHurtTime : settings.getFlashDuration());
        float progress = Math.max(0.0f, Math.min(1.0f, (hurtTime - partialTicks) / max));

        float alphaMultiplier = settings.getTintIntensity();
        if (settings.isSmoothFade()) {
            // Smooth quadratic fade out
            alphaMultiplier *= (progress * progress);
        } else {
            alphaMultiplier *= progress;
        }

        int r = (baseColor >> 16) & 0xFF;
        int g = (baseColor >> 8) & 0xFF;
        int b = baseColor & 0xFF;
        int a = (int) (255 * Math.max(0.0f, Math.min(1.0f, alphaMultiplier)));

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
