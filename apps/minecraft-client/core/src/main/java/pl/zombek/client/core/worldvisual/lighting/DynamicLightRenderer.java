package pl.zombek.client.core.worldvisual.lighting;

import java.util.List;

/**
 * Calculates dynamic light contribution and falloff across world space.
 */
public class DynamicLightRenderer {

    private double lightIntensity = 1.0;
    private int maxLightLevel = 15;
    private double sourceStrength = 1.0;
    private double lightFalloff = 1.0;
    private boolean flickerEnabled = false;
    private double flickerStrength = 0.15;

    public DynamicLightRenderer() {}

    public void configure(double lightIntensity, int maxLightLevel, double sourceStrength, double lightFalloff, boolean flicker, double flickerStrength) {
        this.lightIntensity = Math.max(0.1, Math.min(2.0, lightIntensity));
        this.maxLightLevel = Math.max(1, Math.min(15, maxLightLevel));
        this.sourceStrength = Math.max(0.1, Math.min(3.0, sourceStrength));
        this.lightFalloff = Math.max(0.1, Math.min(5.0, lightFalloff));
        this.flickerEnabled = flicker;
        this.flickerStrength = Math.max(0.0, Math.min(1.0, flickerStrength));
    }

    /**
     * Calculates the dynamic light level at the specified world coordinate.
     * @param targetX world coordinate X
     * @param targetY world coordinate Y
     * @param targetZ world coordinate Z
     * @param sources active dynamic light sources
     * @return dynamic light level in range [0, 15]
     */
    public int calculateDynamicLight(double targetX, double targetY, double targetZ, List<DynamicLightSource> sources) {
        if (sources == null || sources.isEmpty()) {
            return 0;
        }

        int maxComputedLight = 0;
        long time = System.currentTimeMillis();

        for (DynamicLightSource source : sources) {
            if (!source.isActive()) continue;

            double dist = source.distance(targetX, targetY, targetZ);
            double baseStrength = source.getEffectiveLightLevel() * sourceStrength;

            // Optional subtle atmospheric flicker
            if (flickerEnabled && source.getSourceType() != DynamicLightSourceType.CUSTOM) {
                double flickerOffset = Math.sin((time + source.hashCode()) * 0.015) * flickerStrength * baseStrength;
                baseStrength = Math.max(1.0, baseStrength + flickerOffset);
            }

            double decayedLight = (baseStrength - (dist * lightFalloff)) * lightIntensity;
            int level = (int) Math.round(decayedLight);

            if (level > maxComputedLight) {
                maxComputedLight = level;
            }
        }

        return Math.max(0, Math.min(maxLightLevel, maxComputedLight));
    }
}
