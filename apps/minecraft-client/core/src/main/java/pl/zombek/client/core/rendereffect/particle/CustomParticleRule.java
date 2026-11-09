package pl.zombek.client.core.rendereffect.particle;

/**
 * Granular visual and spawn rule for a specific particle identifier.
 */
public class CustomParticleRule {
    private final String particleId;
    private final boolean enabled;
    private final float densityMultiplier;
    private final float scaleMultiplier;
    private final boolean customColor;
    private final int colorRgb;
    private final float opacity;

    public CustomParticleRule(
            String particleId,
            boolean enabled,
            float densityMultiplier,
            float scaleMultiplier,
            boolean customColor,
            int colorRgb,
            float opacity
    ) {
        this.particleId = particleId;
        this.enabled = enabled;
        this.densityMultiplier = densityMultiplier;
        this.scaleMultiplier = scaleMultiplier;
        this.customColor = customColor;
        this.colorRgb = colorRgb;
        this.opacity = opacity;
    }

    public static CustomParticleRule defaultFor(String particleId) {
        return new CustomParticleRule(particleId, true, 1.0f, 1.0f, false, 0xFFFFFF, 1.0f);
    }

    public String getParticleId() {
        return particleId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public float getDensityMultiplier() {
        return densityMultiplier;
    }

    public float getScaleMultiplier() {
        return scaleMultiplier;
    }

    public boolean isCustomColor() {
        return customColor;
    }

    public int getColorRgb() {
        return colorRgb;
    }

    public float getOpacity() {
        return opacity;
    }
}
