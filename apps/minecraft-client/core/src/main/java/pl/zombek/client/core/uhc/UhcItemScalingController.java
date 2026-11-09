package pl.zombek.client.core.uhc;

import pl.zombek.client.core.modules.survival.UHCOverlayModule;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller calculating rendering scale multipliers for dropped items on the ground.
 * Thread-safe, cached, decoupled from Minecraft classes.
 */
public class UhcItemScalingController {
    private static final UhcItemScalingController INSTANCE = new UhcItemScalingController();

    private final Map<String, Float> customRules = new ConcurrentHashMap<>();

    private UhcItemScalingController() {}

    public static UhcItemScalingController getInstance() {
        return INSTANCE;
    }

    /**
     * Resolves the scaling multiplier for a dropped item.
     * Returns 1.0f if the module is disabled or item is unscaled.
     *
     * @param registryName item registry id (e.g. "minecraft:golden_apple")
     * @param displayName item custom name or hover name (can be null)
     * @return scale multiplier (>= 0.1f)
     */
    public float resolveScale(String registryName, String displayName) {
        UHCOverlayModule module = UHCOverlayModule.getInstance();
        if (module == null || !module.isEnabled()) {
            return 1.0f;
        }

        if (registryName == null) {
            return 1.0f;
        }

        String normId = registryName.toLowerCase().trim();
        if (!normId.contains(":")) {
            normId = "minecraft:" + normId;
        }

        // 1. Check custom rules first
        if (customRules.containsKey(normId)) {
            float customScale = customRules.get(normId);
            return applyScaleMode(module, customScale);
        }

        // 2. Check Golden Head (special item)
        if (isGoldenHead(normId, displayName)) {
            float itemScale = module.getGoldenHeadsScaleSetting().getValue().floatValue();
            return applyScaleMode(module, itemScale);
        }

        // 3. Golden Apple / Enchanted Golden Apple
        if (normId.equals("minecraft:golden_apple") || normId.equals("minecraft:enchanted_golden_apple")) {
            float itemScale = module.getGoldenApplesScaleSetting().getValue().floatValue();
            return applyScaleMode(module, itemScale);
        }

        // 4. Ender Pearl
        if (normId.equals("minecraft:ender_pearl")) {
            float itemScale = module.getEnderPearlsScaleSetting().getValue().floatValue();
            return applyScaleMode(module, itemScale);
        }

        // 5. Arrow / Spectral / Tipped
        if (normId.equals("minecraft:arrow") || normId.equals("minecraft:spectral_arrow") || normId.equals("minecraft:tipped_arrow")) {
            float itemScale = module.getArrowsScaleSetting().getValue().floatValue();
            return applyScaleMode(module, itemScale);
        }

        // 6. Healing items (Health / Regen potions)
        if (normId.contains("potion") && displayName != null) {
            String nameLower = displayName.toLowerCase();
            if (nameLower.contains("heal") || nameLower.contains("regen") || nameLower.contains("lecz")) {
                float itemScale = module.getHealingItemsScaleSetting().getValue().floatValue();
                return applyScaleMode(module, itemScale);
            }
        }

        // 7. Buckets (water/lava)
        if (normId.equals("minecraft:water_bucket") || normId.equals("minecraft:lava_bucket")) {
            float itemScale = module.getBucketsScaleSetting().getValue().floatValue();
            return applyScaleMode(module, itemScale);
        }

        // 8. Flint and Steel
        if (normId.equals("minecraft:flint_and_steel")) {
            float itemScale = module.getFlintAndSteelScaleSetting().getValue().floatValue();
            return applyScaleMode(module, itemScale);
        }

        // 9. Building blocks
        if (isCommonBuildingBlock(normId)) {
            float itemScale = module.getBlocksScaleSetting().getValue().floatValue();
            return applyScaleMode(module, itemScale);
        }

        return 1.0f;
    }

    private float applyScaleMode(UHCOverlayModule module, float perItemScale) {
        UhcScaleMode mode = module.getScaleModeSetting().getValue();
        float globalScale = module.getGlobalScaleSetting().getValue().floatValue();

        return switch (mode) {
            case GLOBAL_ONLY -> Math.max(0.1f, globalScale);
            case COMBINED -> Math.max(0.1f, perItemScale * globalScale);
            case PER_ITEM -> Math.max(0.1f, perItemScale);
        };
    }

    public boolean isGoldenHead(String registryName, String displayName) {
        if (!registryName.equals("minecraft:player_head")) {
            return false;
        }
        if (displayName == null) {
            return false;
        }
        String lower = displayName.toLowerCase();
        return lower.contains("golden head") || lower.contains("ghead") || lower.contains("złota głowa") || lower.contains("zlota glowa");
    }

    public boolean isCommonBuildingBlock(String registryName) {
        return registryName.equals("minecraft:cobblestone")
                || registryName.equals("minecraft:oak_planks")
                || registryName.equals("minecraft:stone")
                || registryName.equals("minecraft:obsidian")
                || registryName.equals("minecraft:dirt")
                || registryName.equals("minecraft:wood");
    }

    public void addCustomRule(String itemIdentifier, float scale) {
        if (itemIdentifier == null || itemIdentifier.isBlank()) return;
        String norm = itemIdentifier.toLowerCase().trim();
        if (!norm.contains(":")) {
            norm = "minecraft:" + norm;
        }
        customRules.put(norm, Math.max(0.1f, scale));
    }

    public void removeCustomRule(String itemIdentifier) {
        if (itemIdentifier == null) return;
        String norm = itemIdentifier.toLowerCase().trim();
        if (!norm.contains(":")) {
            norm = "minecraft:" + norm;
        }
        customRules.remove(norm);
    }

    public void clearCustomRules() {
        customRules.clear();
    }

    public Map<String, Float> getCustomRules() {
        return Collections.unmodifiableMap(customRules);
    }
}
