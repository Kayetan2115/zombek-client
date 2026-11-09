package pl.zombek.client.core.rendereffect.color;

/**
 * High performance mathematical pipeline for color grading adjustments.
 */
public final class ColorAdjustmentPipeline {

    private ColorAdjustmentPipeline() {
    }

    /**
     * Applies full color adjustment pipeline to RGB floats [0.0 - 1.0].
     *
     * @param r Red component (0.0 - 1.0)
     * @param g Green component (0.0 - 1.0)
     * @param b Blue component (0.0 - 1.0)
     * @param settings Active settings
     * @param out Output array of length at least 3
     */
    public static void applyAdjustments(float r, float g, float b, ColorSettings settings, float[] out) {
        if (settings == null) {
            out[0] = r;
            out[1] = g;
            out[2] = b;
            return;
        }

        // 1. Channel multipliers
        float cr = r * settings.getRedMultiplier();
        float cg = g * settings.getGreenMultiplier();
        float cb = b * settings.getBlueMultiplier();

        // 2. Color temperature (-1.0 to 1.0)
        float temp = settings.getTemperature();
        if (temp != 0.0f) {
            if (temp > 0.0f) {
                // Warm: boost red, slight green boost, reduce blue
                cr += temp * 0.15f;
                cg += temp * 0.05f;
                cb -= temp * 0.15f;
            } else {
                // Cool: boost blue, reduce red
                cr += temp * 0.15f;
                cb -= temp * 0.15f;
            }
        }

        // 3. Hue shift (-180 to 180)
        float hueShift = settings.getHueShift();
        if (hueShift != 0.0f) {
            float[] hsv = rgbToHsv(cr, cg, cb);
            hsv[0] = (hsv[0] + hueShift) % 360.0f;
            if (hsv[0] < 0.0f) hsv[0] += 360.0f;
            float[] rgb = hsvToRgb(hsv[0], hsv[1], hsv[2]);
            cr = rgb[0];
            cg = rgb[1];
            cb = rgb[2];
        }

        // 4. Rec. 709 Luminance
        float lum = 0.2126f * cr + 0.7152f * cg + 0.0722f * cb;

        // 5. Vibrance (smart saturation targeting desaturated colors first)
        float vibrance = settings.getVibrance();
        if (vibrance != 0.0f) {
            float max = Math.max(cr, Math.max(cg, cb));
            float min = Math.min(cr, Math.min(cg, cb));
            float delta = max - min;
            float factor = (1.0f - delta) * vibrance;
            cr += (cr - lum) * factor;
            cg += (cg - lum) * factor;
            cb += (cb - lum) * factor;
        }

        // 6. Saturation
        float sat = settings.getSaturation();
        if (sat != 1.0f) {
            cr = lum + (cr - lum) * sat;
            cg = lum + (cg - lum) * sat;
            cb = lum + (cb - lum) * sat;
        }

        // 7. Contrast
        float contrast = settings.getContrast();
        if (contrast != 1.0f) {
            cr = (cr - 0.5f) * contrast + 0.5f;
            cg = (cg - 0.5f) * contrast + 0.5f;
            cb = (cb - 0.5f) * contrast + 0.5f;
        }

        // 8. Brightness
        float brightness = settings.getBrightness();
        if (brightness != 1.0f) {
            cr *= brightness;
            cg *= brightness;
            cb *= brightness;
        }

        // Clamp outputs to [0.0, 1.0]
        out[0] = clamp(cr, 0.0f, 1.0f);
        out[1] = clamp(cg, 0.0f, 1.0f);
        out[2] = clamp(cb, 0.0f, 1.0f);
    }

    /**
     * Transforms a 32-bit ARGB packed integer using the color grading pipeline.
     */
    public static int transformPackedArgb(int argb, ColorSettings settings) {
        if (settings == null) {
            return argb;
        }
        int a = (argb >> 24) & 0xFF;
        float r = ((argb >> 16) & 0xFF) / 255.0f;
        float g = ((argb >> 8) & 0xFF) / 255.0f;
        float b = (argb & 0xFF) / 255.0f;

        float[] adjusted = new float[3];
        applyAdjustments(r, g, b, settings, adjusted);

        int ir = Math.round(adjusted[0] * 255.0f);
        int ig = Math.round(adjusted[1] * 255.0f);
        int ib = Math.round(adjusted[2] * 255.0f);

        return (a << 24) | (ir << 16) | (ig << 8) | ib;
    }

    public static float[] rgbToHsv(float r, float g, float b) {
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        float h = 0.0f;
        float s = (max == 0.0f) ? 0.0f : (delta / max);
        float v = max;

        if (delta != 0.0f) {
            if (max == r) {
                h = 60.0f * (((g - b) / delta) % 6.0f);
            } else if (max == g) {
                h = 60.0f * (((b - r) / delta) + 2.0f);
            } else {
                h = 60.0f * (((r - g) / delta) + 4.0f);
            }
            if (h < 0.0f) {
                h += 360.0f;
            }
        }

        return new float[]{h, s, v};
    }

    public static float[] hsvToRgb(float h, float s, float v) {
        float c = v * s;
        float x = c * (1.0f - Math.abs((h / 60.0f) % 2.0f - 1.0f));
        float m = v - c;

        float r = 0, g = 0, b = 0;
        int sextant = (int) (h / 60.0f) % 6;
        switch (sextant) {
            case 0 -> { r = c; g = x; b = 0; }
            case 1 -> { r = x; g = c; b = 0; }
            case 2 -> { r = 0; g = c; b = x; }
            case 3 -> { r = 0; g = x; b = c; }
            case 4 -> { r = x; g = 0; b = c; }
            case 5 -> { r = c; g = 0; b = x; }
        }

        return new float[]{r + m, g + m, b + m};
    }

    private static float clamp(float val, float min, float max) {
        if (val < min) return min;
        if (val > max) return max;
        return val;
    }
}
