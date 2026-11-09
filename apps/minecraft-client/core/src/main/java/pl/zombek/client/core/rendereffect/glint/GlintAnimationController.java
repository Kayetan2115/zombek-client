package pl.zombek.client.core.rendereffect.glint;

import pl.zombek.client.core.rendereffect.color.ColorAdjustmentPipeline;

/**
 * High-performance animation calculator for enchantment glint without RenderType churn.
 */
public final class GlintAnimationController {

    public static final int VANILLA_GLINT_ARGB = 0xFF8040CC;

    private GlintAnimationController() {
    }

    /**
     * Calculates the active ARGB color for a glint configuration at the given time.
     */
    public static int calculateColor(
            GlintMode mode,
            int primaryColor,
            int secondaryColor,
            float speed,
            float opacity,
            long timeMs
    ) {
        if (mode == null || mode == GlintMode.VANILLA) {
            int alpha = Math.round(opacity * 255.0f);
            return (alpha << 24) | (VANILLA_GLINT_ARGB & 0x00FFFFFF);
        }

        int targetAlpha = Math.min(255, Math.max(0, Math.round(opacity * 255.0f)));

        return switch (mode) {
            case STATIC -> {
                int rgb = primaryColor & 0x00FFFFFF;
                yield (targetAlpha << 24) | rgb;
            }
            case RAINBOW -> {
                // Hue cycle: speed modifies rate of rotation
                float hue = (float) ((timeMs * (speed * 0.12)) % 360.0);
                if (hue < 0) hue += 360.0f;
                float[] rgb = ColorAdjustmentPipeline.hsvToRgb(hue, 0.92f, 1.0f);
                int r = Math.round(rgb[0] * 255.0f);
                int g = Math.round(rgb[1] * 255.0f);
                int b = Math.round(rgb[2] * 255.0f);
                yield (targetAlpha << 24) | (r << 16) | (g << 8) | b;
            }
            case GRADIENT -> {
                // Interpolate primary and secondary using sine wave
                double phase = (timeMs * 0.003 * speed);
                float t = (float) (Math.sin(phase) * 0.5 + 0.5);

                int r1 = (primaryColor >> 16) & 0xFF;
                int g1 = (primaryColor >> 8) & 0xFF;
                int b1 = primaryColor & 0xFF;

                int r2 = (secondaryColor >> 16) & 0xFF;
                int g2 = (secondaryColor >> 8) & 0xFF;
                int b2 = secondaryColor & 0xFF;

                int r = Math.round(r1 + (r2 - r1) * t);
                int g = Math.round(g1 + (g2 - g1) * t);
                int b = Math.round(b1 + (b2 - b1) * t);
                yield (targetAlpha << 24) | (r << 16) | (g << 8) | b;
            }
            case PULSE -> {
                // Modulate brightness and alpha using smooth pulse
                double pulse = 0.65 + 0.35 * Math.sin(timeMs * 0.005 * speed);
                int r = Math.min(255, (int) (((primaryColor >> 16) & 0xFF) * pulse));
                int g = Math.min(255, (int) (((primaryColor >> 8) & 0xFF) * pulse));
                int b = Math.min(255, (int) ((primaryColor & 0xFF) * pulse));
                int dynamicAlpha = (int) (targetAlpha * pulse);
                yield (dynamicAlpha << 24) | (r << 16) | (g << 8) | b;
            }
            case CUSTOM -> {
                int rgb = primaryColor & 0x00FFFFFF;
                yield (targetAlpha << 24) | rgb;
            }
            default -> (targetAlpha << 24) | (VANILLA_GLINT_ARGB & 0x00FFFFFF);
        };
    }
}
