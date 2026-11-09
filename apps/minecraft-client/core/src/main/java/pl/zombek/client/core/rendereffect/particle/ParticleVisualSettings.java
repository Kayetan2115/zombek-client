package pl.zombek.client.core.rendereffect.particle;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Immutable snapshot of particle changer settings.
 */
public class ParticleVisualSettings {
    private final float globalDensity;
    private final float globalScale;
    private final float globalOpacity;
    private final int maxParticleLimit;
    private final float maxDistanceCutoff;
    private final Map<ParticleCategory, Float> categoryMultipliers;
    private final Map<String, CustomParticleRule> customRules;

    public ParticleVisualSettings(
            float globalDensity,
            float globalScale,
            float globalOpacity,
            int maxParticleLimit,
            float maxDistanceCutoff,
            Map<ParticleCategory, Float> categoryMultipliers,
            Map<String, CustomParticleRule> customRules
    ) {
        this.globalDensity = globalDensity;
        this.globalScale = globalScale;
        this.globalOpacity = globalOpacity;
        this.maxParticleLimit = maxParticleLimit;
        this.maxDistanceCutoff = maxDistanceCutoff;
        Map<ParticleCategory, Float> catMap = new EnumMap<>(ParticleCategory.class);
        if (categoryMultipliers != null) {
            catMap.putAll(categoryMultipliers);
        }
        this.categoryMultipliers = Collections.unmodifiableMap(catMap);
        this.customRules = customRules != null
                ? Collections.unmodifiableMap(new HashMap<>(customRules))
                : Collections.emptyMap();
    }

    public static ParticleVisualSettings vanilla() {
        EnumMap<ParticleCategory, Float> map = new EnumMap<>(ParticleCategory.class);
        for (ParticleCategory cat : ParticleCategory.values()) {
            map.put(cat, 1.0f);
        }
        return new ParticleVisualSettings(1.0f, 1.0f, 1.0f, 2000, 64.0f, map, Collections.emptyMap());
    }

    public float getGlobalDensity() {
        return globalDensity;
    }

    public float getGlobalScale() {
        return globalScale;
    }

    public float getGlobalOpacity() {
        return globalOpacity;
    }

    public int getMaxParticleLimit() {
        return maxParticleLimit;
    }

    public float getMaxDistanceCutoff() {
        return maxDistanceCutoff;
    }

    public Map<ParticleCategory, Float> getCategoryMultipliers() {
        return categoryMultipliers;
    }

    public float getCategoryMultiplier(ParticleCategory category) {
        return categoryMultipliers.getOrDefault(category, 1.0f);
    }

    public Map<String, CustomParticleRule> getCustomRules() {
        return customRules;
    }

    public CustomParticleRule getRule(String particleId) {
        if (particleId == null) return null;
        String clean = particleId.toLowerCase().replace("minecraft:", "");
        return customRules.get(clean);
    }
}
