package pl.zombek.client.core.rendereffect.color;

import java.util.EnumMap;
import java.util.Map;

/**
 * Manages predefined color grading settings for presets.
 */
public final class ColorProfileManager {
    private static final Map<ColorProfile, ColorSettings> PRESETS = new EnumMap<>(ColorProfile.class);

    static {
        // Vanilla: Identity
        PRESETS.put(ColorProfile.VANILLA, ColorSettings.vanilla());

        // Vibrant: Boosted saturation + vibrance, slight warmth
        PRESETS.put(ColorProfile.VIBRANT, new ColorSettings(
                1.45f, 0.35f, 1.08f, 1.02f, 0.0f, 0.05f,
                1.02f, 1.02f, 1.05f,
                true, true, true, true, true
        ));

        // Natural: Gentle warmth, moderate contrast
        PRESETS.put(ColorProfile.NATURAL, new ColorSettings(
                1.15f, 0.15f, 1.02f, 1.00f, 0.0f, 0.10f,
                1.03f, 1.01f, 0.98f,
                true, true, true, true, true
        ));

        // Soft: Pastel tones, reduced eye strain
        PRESETS.put(ColorProfile.SOFT, new ColorSettings(
                0.85f, -0.10f, 0.95f, 1.05f, 0.0f, 0.02f,
                1.00f, 1.00f, 1.00f,
                true, true, true, true, true
        ));

        // Cinematic: Rich contrast, cool shadows, cinematic atmosphere
        PRESETS.put(ColorProfile.CINEMATIC, new ColorSettings(
                1.10f, 0.10f, 1.20f, 0.95f, 0.0f, -0.05f,
                0.98f, 1.00f, 1.05f,
                true, true, true, true, true
        ));

        // Monochrome: Grayscale
        PRESETS.put(ColorProfile.MONOCHROME, new ColorSettings(
                0.00f, -1.00f, 1.05f, 1.00f, 0.0f, 0.00f,
                1.00f, 1.00f, 1.00f,
                true, true, true, true, true
        ));
    }

    private ColorProfileManager() {
    }

    public static ColorSettings getSettings(ColorProfile profile) {
        if (profile == null || profile == ColorProfile.CUSTOM) {
            return PRESETS.get(ColorProfile.VANILLA);
        }
        return PRESETS.getOrDefault(profile, PRESETS.get(ColorProfile.VANILLA));
    }
}
