package pl.zombek.client.core.rendereffect.fog;

/**
 * Computes safe, clamp-guarded fog start and end distances.
 * Prevents wallhack / X-ray exploits, negative values, and chunk-range overflows.
 */
public class FogDistanceController {

    private float customFogStart = 16.0f;
    private float customFogEnd = 128.0f;
    private float fogStrength = 1.0f;
    private float fogDensity = 1.0f;
    private float fogFalloff = 1.0f;
    private float minFogDistance = 0.0f;
    private float maxFogDistance = 512.0f;

    public FogDistanceController() {}

    /**
     * Resolves effective fog start and end distances for the current frame.
     *
     * @param vanillaStart vanilla near plane distance
     * @param vanillaEnd vanilla far plane distance (view distance based)
     * @param preset active fog preset
     * @param envProfile active environment profile
     * @return 2-element float array: [effectiveStart, effectiveEnd]
     */
    public float[] computeDistances(float vanillaStart, float vanillaEnd, FogPreset preset, FogEnvironmentProfile envProfile) {
        float effectiveMax = Math.max(vanillaEnd, maxFogDistance);

        if (preset == FogPreset.VANILLA) {
            return new float[] {
                    Math.max(0.0f, vanillaStart),
                    Math.max(vanillaStart + 1.0f, vanillaEnd)
            };
        }

        float computedStart;
        float computedEnd;

        switch (preset) {
            case REDUCED_FOG -> {
                // Pushes start further out and stretches end to edge of view distance
                computedStart = vanillaEnd * 0.75f;
                computedEnd = vanillaEnd * 1.25f;
            }
            case CLEAR_VIEW -> {
                // Maximizes clear vision zone without exceeding loaded chunks
                computedStart = vanillaEnd * 0.95f;
                computedEnd = vanillaEnd * 1.5f;
            }
            case DENSE_FOG -> {
                // Brings fog close for intense atmosphere
                computedStart = Math.max(2.0f, vanillaEnd * 0.1f);
                computedEnd = Math.max(12.0f, vanillaEnd * 0.45f);
            }
            case SOFT_FOG -> {
                // Wide, gradual fade
                computedStart = Math.max(4.0f, vanillaEnd * 0.25f);
                computedEnd = vanillaEnd * 1.1f;
            }
            case CUSTOM -> {
                computedStart = customFogStart;
                computedEnd = customFogEnd;
            }
            default -> {
                computedStart = vanillaStart;
                computedEnd = vanillaEnd;
            }
        }

        // Apply environment override if defined
        if (envProfile != null && envProfile.isEnabled()) {
            if (preset == FogPreset.CUSTOM) {
                computedStart = envProfile.getCustomStartDistance();
                computedEnd = envProfile.getCustomEndDistance();
            }
            computedStart *= envProfile.getIntensity();
            computedEnd *= envProfile.getIntensity();
        }

        // Apply fog strength multiplier (lower strength = further fog)
        if (fogStrength > 0.01f) {
            float strengthInv = 1.0f / fogStrength;
            computedStart *= strengthInv;
            computedEnd *= strengthInv;
        }

        // Apply falloff modulation
        if (Math.abs(fogFalloff - 1.0f) > 0.01f) {
            float span = Math.max(1.0f, computedEnd - computedStart);
            computedEnd = computedStart + span * fogFalloff;
        }

        // Strict clamp guard: start >= 0, start < end, end <= effectiveMax
        computedStart = Math.max(minFogDistance, Math.max(0.0f, computedStart));
        computedEnd = Math.max(computedStart + 1.0f, Math.min(effectiveMax, computedEnd));

        return new float[] { computedStart, computedEnd };
    }

    public float getCustomFogStart() {
        return customFogStart;
    }

    public void setCustomFogStart(float customFogStart) {
        this.customFogStart = Math.max(0.0f, Math.min(512.0f, customFogStart));
    }

    public float getCustomFogEnd() {
        return customFogEnd;
    }

    public void setCustomFogEnd(float customFogEnd) {
        this.customFogEnd = Math.max(this.customFogStart + 1.0f, Math.min(1024.0f, customFogEnd));
    }

    public float getFogStrength() {
        return fogStrength;
    }

    public void setFogStrength(float fogStrength) {
        this.fogStrength = Math.max(0.1f, Math.min(3.0f, fogStrength));
    }

    public float getFogDensity() {
        return fogDensity;
    }

    public void setFogDensity(float fogDensity) {
        this.fogDensity = Math.max(0.0f, Math.min(2.0f, fogDensity));
    }

    public float getFogFalloff() {
        return fogFalloff;
    }

    public void setFogFalloff(float fogFalloff) {
        this.fogFalloff = Math.max(0.2f, Math.min(3.0f, fogFalloff));
    }

    public float getMinFogDistance() {
        return minFogDistance;
    }

    public void setMinFogDistance(float minFogDistance) {
        this.minFogDistance = Math.max(0.0f, minFogDistance);
    }

    public float getMaxFogDistance() {
        return maxFogDistance;
    }

    public void setMaxFogDistance(float maxFogDistance) {
        this.maxFogDistance = Math.max(this.minFogDistance + 1.0f, maxFogDistance);
    }
}
