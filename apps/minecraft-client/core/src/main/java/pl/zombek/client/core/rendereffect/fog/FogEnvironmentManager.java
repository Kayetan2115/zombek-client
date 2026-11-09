package pl.zombek.client.core.rendereffect.fog;

import java.util.EnumMap;
import java.util.Map;

/**
 * Manages specialized fog profiles per environmental condition.
 */
public class FogEnvironmentManager {

    private final Map<FogEnvironmentType, FogEnvironmentProfile> profiles = new EnumMap<>(FogEnvironmentType.class);

    public FogEnvironmentManager() {
        initDefaultProfiles();
    }

    private void initDefaultProfiles() {
        profiles.put(FogEnvironmentType.AIR, new FogEnvironmentProfile(FogEnvironmentType.AIR, 16.0f, 128.0f, 0xFFC0D8FF));
        profiles.put(FogEnvironmentType.WATER, new FogEnvironmentProfile(FogEnvironmentType.WATER, -8.0f, 64.0f, 0xFF051530));
        profiles.put(FogEnvironmentType.LAVA, new FogEnvironmentProfile(FogEnvironmentType.LAVA, 0.25f, 2.0f, 0xFF661100));
        profiles.put(FogEnvironmentType.POWDER_SNOW, new FogEnvironmentProfile(FogEnvironmentType.POWDER_SNOW, 0.05f, 2.5f, 0xFFE0F0FF));
        profiles.put(FogEnvironmentType.DIMENSION, new FogEnvironmentProfile(FogEnvironmentType.DIMENSION, 8.0f, 96.0f, 0xFF330011));
        profiles.put(FogEnvironmentType.BIOME, new FogEnvironmentProfile(FogEnvironmentType.BIOME, 12.0f, 80.0f, 0xFF88A077));
        profiles.put(FogEnvironmentType.BOSS, new FogEnvironmentProfile(FogEnvironmentType.BOSS, 4.0f, 48.0f, 0xFF1A0026));
    }

    public FogEnvironmentProfile getProfile(FogEnvironmentType type) {
        return profiles.getOrDefault(type, profiles.get(FogEnvironmentType.AIR));
    }

    public void setProfile(FogEnvironmentType type, FogEnvironmentProfile profile) {
        if (type != null && profile != null) {
            profiles.put(type, profile);
        }
    }

    public void resetToDefaults() {
        profiles.clear();
        initDefaultProfiles();
    }
}
