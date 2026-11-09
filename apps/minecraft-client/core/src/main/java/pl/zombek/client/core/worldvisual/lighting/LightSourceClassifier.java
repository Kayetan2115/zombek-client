package pl.zombek.client.core.worldvisual.lighting;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Classifies Minecraft items and entities to determine their natural luminous intensity.
 */
public final class LightSourceClassifier {

    private static final Map<String, Integer> DEFAULT_ITEM_LIGHTS;
    private static final Map<String, Integer> DEFAULT_ENTITY_LIGHTS;

    static {
        Map<String, Integer> items = new HashMap<>();
        // Torch & Lanterns
        items.put("minecraft:torch", 14);
        items.put("minecraft:soul_torch", 10);
        items.put("minecraft:lantern", 15);
        items.put("minecraft:soul_lantern", 10);
        items.put("minecraft:redstone_torch", 7);
        items.put("minecraft:campfire", 15);
        items.put("minecraft:soul_campfire", 10);

        // Blocks that emit light
        items.put("minecraft:glowstone", 15);
        items.put("minecraft:sea_lantern", 15);
        items.put("minecraft:shroomlight", 15);
        items.put("minecraft:beacon", 15);
        items.put("minecraft:end_rod", 14);
        items.put("minecraft:crying_obsidian", 10);
        items.put("minecraft:magma_block", 3);
        items.put("minecraft:ochre_froglight", 15);
        items.put("minecraft:pearlescent_froglight", 15);
        items.put("minecraft:verdant_froglight", 15);

        // Buckets and tools
        items.put("minecraft:lava_bucket", 15);

        // Dust & items
        items.put("minecraft:glowstone_dust", 8);
        items.put("minecraft:glow_berries", 12);
        items.put("minecraft:glow_ink_sac", 6);
        items.put("minecraft:blaze_rod", 7);
        items.put("minecraft:blaze_powder", 7);

        DEFAULT_ITEM_LIGHTS = Collections.unmodifiableMap(items);

        Map<String, Integer> entities = new HashMap<>();
        entities.put("blaze", 12);
        entities.put("magma_cube", 8);
        entities.put("glow_squid", 11);
        entities.put("allay", 7);
        entities.put("spectral_arrow", 6);
        entities.put("tnt", 8);
        entities.put("fireball", 10);
        entities.put("small_fireball", 8);
        entities.put("dragon_fireball", 8);

        DEFAULT_ENTITY_LIGHTS = Collections.unmodifiableMap(entities);
    }

    private LightSourceClassifier() {}

    /**
     * Resolves the default light emission level for a given item identifier.
     * @param itemId normalized item identifier (e.g. "minecraft:torch" or "torch")
     * @return light level from 0 to 15
     */
    public static int resolveItemLight(String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            return 0;
        }
        String normalized = normalizeId(itemId);
        Integer level = DEFAULT_ITEM_LIGHTS.get(normalized);
        if (level != null) {
            return level;
        }
        // Fallback without namespace
        if (normalized.startsWith("minecraft:")) {
            level = DEFAULT_ITEM_LIGHTS.get(normalized.substring(10));
            if (level != null) return level;
        } else {
            level = DEFAULT_ITEM_LIGHTS.get("minecraft:" + normalized);
            if (level != null) return level;
        }
        return 0;
    }

    /**
     * Resolves the default light emission level for a given entity type name.
     * @param entityTypeName normalized entity type (e.g. "blaze", "magma_cube")
     * @return light level from 0 to 15
     */
    public static int resolveEntityLight(String entityTypeName) {
        if (entityTypeName == null || entityTypeName.isEmpty()) {
            return 0;
        }
        String normalized = normalizeId(entityTypeName);
        if (normalized.startsWith("minecraft:")) {
            normalized = normalized.substring(10);
        }
        Integer level = DEFAULT_ENTITY_LIGHTS.get(normalized);
        return level != null ? level : 0;
    }

    public static String normalizeId(String id) {
        return id.trim().toLowerCase(Locale.ROOT);
    }

    public static Map<String, Integer> getDefaultItemLights() {
        return DEFAULT_ITEM_LIGHTS;
    }

    public static Map<String, Integer> getDefaultEntityLights() {
        return DEFAULT_ENTITY_LIGHTS;
    }
}
