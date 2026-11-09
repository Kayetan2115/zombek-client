package pl.zombek.client.core.worldvisual.fullbright;

/**
 * Controls client-side brightness enhancement (Full Bright).
 * Operates purely on local rendering and lightmap textures without hacking gamma
 * out of bounds or altering server world data.
 */
public class FullBrightController {
    private static final FullBrightController INSTANCE = new FullBrightController();

    private boolean enabled = false;
    private FullBrightMode mode = FullBrightMode.FULL_BRIGHT;
    private double customBrightness = 10.0; // 0.0 to 15.0
    private double brightnessStrength = 1.0;

    // Color preservation
    private boolean preserveColors = true;
    private boolean preserveLightmapBehavior = true;
    private boolean colorCompensation = true;
    private boolean reduceWashedOutEffect = true;
    private boolean shadowPreservation = true;

    // Smoothing & animation
    private boolean smoothBrightness = true;
    private long transitionDurationMs = 200L;
    private double currentBrightness = 0.0;
    private double targetBrightness = 0.0;
    private long animationStartTime = 0L;
    private double animationStartBrightness = 0.0;

    private FullBrightController() {}

    public static FullBrightController getInstance() {
        return INSTANCE;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            updateTargetBrightness();
        }
    }

    public FullBrightMode getMode() {
        return mode;
    }

    public void setMode(FullBrightMode mode) {
        this.mode = mode != null ? mode : FullBrightMode.FULL_BRIGHT;
        updateTargetBrightness();
    }

    public double getCustomBrightness() {
        return customBrightness;
    }

    public void setCustomBrightness(double customBrightness) {
        this.customBrightness = Math.max(0.0, Math.min(15.0, customBrightness));
        if (mode == FullBrightMode.CUSTOM) {
            updateTargetBrightness();
        }
    }

    public double getBrightnessStrength() {
        return brightnessStrength;
    }

    public void setBrightnessStrength(double brightnessStrength) {
        this.brightnessStrength = Math.max(0.1, Math.min(2.0, brightnessStrength));
        updateTargetBrightness();
    }

    public boolean isPreserveColors() {
        return preserveColors;
    }

    public void setPreserveColors(boolean preserveColors) {
        this.preserveColors = preserveColors;
    }

    public boolean isPreserveLightmapBehavior() {
        return preserveLightmapBehavior;
    }

    public void setPreserveLightmapBehavior(boolean preserveLightmapBehavior) {
        this.preserveLightmapBehavior = preserveLightmapBehavior;
    }

    public boolean isColorCompensation() {
        return colorCompensation;
    }

    public void setColorCompensation(boolean colorCompensation) {
        this.colorCompensation = colorCompensation;
    }

    public boolean isReduceWashedOutEffect() {
        return reduceWashedOutEffect;
    }

    public void setReduceWashedOutEffect(boolean reduceWashedOutEffect) {
        this.reduceWashedOutEffect = reduceWashedOutEffect;
    }

    public boolean isShadowPreservation() {
        return shadowPreservation;
    }

    public void setShadowPreservation(boolean shadowPreservation) {
        this.shadowPreservation = shadowPreservation;
    }

    public boolean isSmoothBrightness() {
        return smoothBrightness;
    }

    public void setSmoothBrightness(boolean smoothBrightness) {
        this.smoothBrightness = smoothBrightness;
    }

    public long getTransitionDurationMs() {
        return transitionDurationMs;
    }

    public void setTransitionDurationMs(long transitionDurationMs) {
        this.transitionDurationMs = Math.max(10L, Math.min(2000L, transitionDurationMs));
    }

    private void updateTargetBrightness() {
        if (!enabled) {
            targetBrightness = 0.0;
        } else {
            switch (mode) {
                case VANILLA -> targetBrightness = 0.0;
                case FULL_BRIGHT -> targetBrightness = 15.0 * brightnessStrength;
                case CUSTOM -> targetBrightness = customBrightness * brightnessStrength;
                case NIGHT_VISION_STYLE -> targetBrightness = 12.0 * brightnessStrength;
            }
        }
        targetBrightness = Math.max(0.0, Math.min(15.0, targetBrightness));

        if (!smoothBrightness) {
            currentBrightness = targetBrightness;
        } else {
            animationStartTime = System.currentTimeMillis();
            animationStartBrightness = currentBrightness;
        }
    }

    /**
     * Updates interpolation state each frame.
     */
    public void updateAnimation() {
        if (!smoothBrightness || currentBrightness == targetBrightness) {
            return;
        }

        long now = System.currentTimeMillis();
        long elapsed = now - animationStartTime;
        if (elapsed >= transitionDurationMs) {
            currentBrightness = targetBrightness;
            return;
        }

        float progress = Math.min(1.0f, Math.max(0.0f, (float) elapsed / (float) transitionDurationMs));
        // Smooth easing (EASE_IN_OUT)
        float eased = progress < 0.5f ? 2.0f * progress * progress : 1.0f - (float) Math.pow(-2.0f * progress + 2.0f, 2.0) / 2.0f;
        currentBrightness = animationStartBrightness + (targetBrightness - animationStartBrightness) * eased;
    }

    /**
     * Computes the effective visual light boost factor for a given block light level.
     * @param originalBlockLight original light level between 0 and 15
     * @return boosted light level in range [original, 15]
     */
    public int computeBoostedLightLevel(int originalBlockLight) {
        if (!enabled && currentBrightness <= 0.01) {
            return originalBlockLight;
        }

        updateAnimation();
        double boost = currentBrightness;

        if (shadowPreservation && originalBlockLight == 0) {
            // Retain subtle depth in total darkness
            boost = Math.min(boost, 14.0);
        }

        int result = (int) Math.round(Math.max(originalBlockLight, boost));
        return Math.min(15, Math.max(0, result));
    }

    /**
     * Computes gamma multiplier adjustment for lightmap generation.
     * Keeps gamma within safe bounds and preserves color fidelity.
     */
    public double computeEffectiveGamma(double vanillaGamma) {
        if (!enabled && currentBrightness <= 0.01) {
            return vanillaGamma;
        }

        updateAnimation();
        double factor = currentBrightness / 15.0; // 0.0 to 1.0

        if (reduceWashedOutEffect) {
            // Non-linear gamma curve that prevents color bleaching
            factor = Math.pow(factor, 0.85);
        }

        return Math.max(vanillaGamma, factor * 1.0);
    }

    public double getCurrentBrightness() {
        return currentBrightness;
    }

    public double getTargetBrightness() {
        return targetBrightness;
    }

    public void resetForTesting() {
        enabled = false;
        mode = FullBrightMode.FULL_BRIGHT;
        customBrightness = 10.0;
        brightnessStrength = 1.0;
        preserveColors = true;
        preserveLightmapBehavior = true;
        colorCompensation = true;
        reduceWashedOutEffect = true;
        shadowPreservation = true;
        smoothBrightness = true;
        transitionDurationMs = 200L;
        currentBrightness = 0.0;
        targetBrightness = 0.0;
        animationStartTime = 0L;
        animationStartBrightness = 0.0;
    }
}
