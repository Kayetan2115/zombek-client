package pl.zombek.client.core.rendereffect.fog;

/**
 * Resolves effective fog color including dimension tinting, custom palettes,
 * biome blending, and smooth color transitions.
 */
public class FogColorController {

    private boolean useCustomColor = false;
    private int customColorArgb = 0xFFC0D8FF; // Default light blue atmospheric fog
    private float colorBlend = 1.0f; // 0.0 = 100% vanilla, 1.0 = 100% custom

    // Current interpolated colors for smooth transition
    private float currentRed = 0.75f;
    private float currentGreen = 0.85f;
    private float currentBlue = 1.0f;
    private float currentAlpha = 1.0f;

    public FogColorController() {}

    /**
     * Resolves effective RGBA fog color for rendering.
     *
     * @param vanillaR vanilla red channel [0.0, 1.0]
     * @param vanillaG vanilla green channel [0.0, 1.0]
     * @param vanillaB vanilla blue channel [0.0, 1.0]
     * @param vanillaA vanilla alpha channel [0.0, 1.0]
     * @param dimensionId current dimension ID
     * @param envProfile environment override profile (if any)
     * @return 4-element float array: [r, g, b, a]
     */
    public float[] computeColor(
            float vanillaR, float vanillaG, float vanillaB, float vanillaA,
            String dimensionId, FogEnvironmentProfile envProfile
    ) {
        if (!useCustomColor && (envProfile == null || !envProfile.isCustomColorEnabled())) {
            this.currentRed = vanillaR;
            this.currentGreen = vanillaG;
            this.currentBlue = vanillaB;
            this.currentAlpha = vanillaA;
            return new float[] { vanillaR, vanillaG, vanillaB, vanillaA };
        }

        int targetArgb = customColorArgb;
        if (envProfile != null && envProfile.isCustomColorEnabled()) {
            targetArgb = envProfile.getCustomColorArgb();
        }

        float targetA = ((targetArgb >> 24) & 0xFF) / 255.0f;
        float targetR = ((targetArgb >> 16) & 0xFF) / 255.0f;
        float targetG = ((targetArgb >> 8) & 0xFF) / 255.0f;
        float targetB = (targetArgb & 0xFF) / 255.0f;

        // Dimension tint modulation if needed
        if (dimensionId != null) {
            String lower = dimensionId.toLowerCase();
            if (lower.contains("nether")) {
                // Subtle fiery/crimson warmth preservation
                targetR = Math.min(1.0f, targetR * 1.15f);
                targetB = Math.max(0.0f, targetB * 0.75f);
            } else if (lower.contains("the_end") || lower.contains("end")) {
                // Ender void / violet tint preservation
                targetG = Math.max(0.0f, targetG * 0.8f);
                targetB = Math.min(1.0f, targetB * 1.1f);
            }
        }

        // Apply blend factor
        float effectiveR = vanillaR + (targetR - vanillaR) * colorBlend;
        float effectiveG = vanillaG + (targetG - vanillaG) * colorBlend;
        float effectiveB = vanillaB + (targetB - vanillaB) * colorBlend;
        float effectiveA = vanillaA + (targetA - vanillaA) * colorBlend;

        this.currentRed = Math.max(0.0f, Math.min(1.0f, effectiveR));
        this.currentGreen = Math.max(0.0f, Math.min(1.0f, effectiveG));
        this.currentBlue = Math.max(0.0f, Math.min(1.0f, effectiveB));
        this.currentAlpha = Math.max(0.0f, Math.min(1.0f, effectiveA));

        return new float[] { currentRed, currentGreen, currentBlue, currentAlpha };
    }

    public boolean isUseCustomColor() {
        return useCustomColor;
    }

    public void setUseCustomColor(boolean useCustomColor) {
        this.useCustomColor = useCustomColor;
    }

    public int getCustomColorArgb() {
        return customColorArgb;
    }

    public void setCustomColorArgb(int customColorArgb) {
        this.customColorArgb = customColorArgb;
    }

    public float getColorBlend() {
        return colorBlend;
    }

    public void setColorBlend(float colorBlend) {
        this.colorBlend = Math.max(0.0f, Math.min(1.0f, colorBlend));
    }

    public float getCurrentRed() {
        return currentRed;
    }

    public float getCurrentGreen() {
        return currentGreen;
    }

    public float getCurrentBlue() {
        return currentBlue;
    }

    public float getCurrentAlpha() {
        return currentAlpha;
    }
}
