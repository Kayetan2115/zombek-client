package pl.zombek.client.core.worldvisual.lighting;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry holding known and user-customized dynamic light emission mappings.
 */
public class DynamicLightSourceRegistry {
    private static final DynamicLightSourceRegistry INSTANCE = new DynamicLightSourceRegistry();

    private final Map<String, Integer> customItemLights = new ConcurrentHashMap<>();
    private final Map<String, Integer> customEntityLights = new ConcurrentHashMap<>();

    private DynamicLightSourceRegistry() {}

    public static DynamicLightSourceRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Resolves light level taking custom rules first, then falling back to defaults.
     */
    public int getLightLevelForItem(String itemId) {
        if (itemId == null) return 0;
        String normalized = LightSourceClassifier.normalizeId(itemId);
        Integer custom = customItemLights.get(normalized);
        if (custom != null) {
            return custom;
        }
        return LightSourceClassifier.resolveItemLight(normalized);
    }

    /**
     * Resolves light level for an entity taking custom rules first, then defaults.
     */
    public int getLightLevelForEntity(String entityTypeName) {
        if (entityTypeName == null) return 0;
        String normalized = LightSourceClassifier.normalizeId(entityTypeName);
        Integer custom = customEntityLights.get(normalized);
        if (custom != null) {
            return custom;
        }
        return LightSourceClassifier.resolveEntityLight(normalized);
    }

    public void registerCustomItemLight(String itemId, int lightLevel) {
        if (itemId == null) return;
        customItemLights.put(LightSourceClassifier.normalizeId(itemId), Math.max(0, Math.min(15, lightLevel)));
    }

    public void removeCustomItemLight(String itemId) {
        if (itemId == null) return;
        customItemLights.remove(LightSourceClassifier.normalizeId(itemId));
    }

    public void registerCustomEntityLight(String entityTypeName, int lightLevel) {
        if (entityTypeName == null) return;
        customEntityLights.put(LightSourceClassifier.normalizeId(entityTypeName), Math.max(0, Math.min(15, lightLevel)));
    }

    public void removeCustomEntityLight(String entityTypeName) {
        if (entityTypeName == null) return;
        customEntityLights.remove(LightSourceClassifier.normalizeId(entityTypeName));
    }

    public void resetToDefaults() {
        customItemLights.clear();
        customEntityLights.clear();
    }

    public Map<String, Integer> getCustomItemLights() {
        return Collections.unmodifiableMap(customItemLights);
    }

    public Map<String, Integer> getCustomEntityLights() {
        return Collections.unmodifiableMap(customEntityLights);
    }
}
