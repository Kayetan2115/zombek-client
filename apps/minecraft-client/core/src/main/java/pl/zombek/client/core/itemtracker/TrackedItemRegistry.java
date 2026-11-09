package pl.zombek.client.core.itemtracker;

import java.util.*;

/**
 * Predefined and customizable registry of items available for counting and tracking.
 */
public final class TrackedItemRegistry {

    public static final class ItemEntry {
        private final String itemId;
        private final String defaultDisplayName;
        private final String category;
        private final int defaultWarningThreshold;
        private final int defaultCriticalThreshold;

        public ItemEntry(String itemId, String defaultDisplayName, String category, int defaultWarningThreshold, int defaultCriticalThreshold) {
            this.itemId = itemId;
            this.defaultDisplayName = defaultDisplayName;
            this.category = category;
            this.defaultWarningThreshold = defaultWarningThreshold;
            this.defaultCriticalThreshold = defaultCriticalThreshold;
        }

        public String getItemId() { return itemId; }
        public String getDefaultDisplayName() { return defaultDisplayName; }
        public String getCategory() { return category; }
        public int getDefaultWarningThreshold() { return defaultWarningThreshold; }
        public int getDefaultCriticalThreshold() { return defaultCriticalThreshold; }
    }

    private static final Map<String, ItemEntry> PRESETS = new LinkedHashMap<>();

    static {
        // Combat
        registerPreset("minecraft:ender_pearl", "Ender Pearls", "Combat", 4, 1);
        registerPreset("minecraft:golden_apple", "Golden Apples", "Combat", 2, 0);
        registerPreset("minecraft:enchanted_golden_apple", "God Apples", "Combat", 1, 0);
        registerPreset("minecraft:arrow", "Arrows", "Combat", 16, 4);
        registerPreset("minecraft:spectral_arrow", "Spectral Arrows", "Combat", 16, 4);
        registerPreset("minecraft:shield", "Shield", "Combat", 1, 0);
        registerPreset("minecraft:totem_of_undying", "Totem of Undying", "Combat", 1, 0);

        // Building
        registerPreset("minecraft:obsidian", "Obsidian", "Building", 16, 4);
        registerPreset("minecraft:white_wool", "Wool", "Building", 32, 8);
        registerPreset("minecraft:oak_planks", "Planks", "Building", 32, 8);
        registerPreset("minecraft:glass", "Glass", "Building", 16, 4);
        registerPreset("minecraft:cobblestone", "Cobblestone", "Building", 64, 16);

        // Survival
        registerPreset("minecraft:cooked_beef", "Steak", "Survival", 10, 2);
        registerPreset("minecraft:bread", "Bread", "Survival", 10, 2);
        registerPreset("minecraft:water_bucket", "Water Bucket", "Survival", 1, 0);
        registerPreset("minecraft:lava_bucket", "Lava Bucket", "Survival", 1, 0);
        registerPreset("minecraft:torch", "Torches", "Survival", 16, 4);
        registerPreset("minecraft:diamond_pickaxe", "Diamond Pickaxe", "Survival", 1, 0);
    }

    private static void registerPreset(String id, String name, String cat, int warn, int crit) {
        PRESETS.put(id.toLowerCase(Locale.ROOT), new ItemEntry(id, name, cat, warn, crit));
    }

    private TrackedItemRegistry() {}

    public static List<ItemEntry> getAllPresets() {
        return new ArrayList<>(PRESETS.values());
    }

    public static List<ItemEntry> getCategoryPresets(String category) {
        List<ItemEntry> result = new ArrayList<>();
        for (ItemEntry e : PRESETS.values()) {
            if (e.getCategory().equalsIgnoreCase(category)) {
                result.add(e);
            }
        }
        return result;
    }

    public static ItemEntry getPreset(String itemId) {
        if (itemId == null) return null;
        String clean = itemId.contains(":") ? itemId : "minecraft:" + itemId;
        return PRESETS.get(clean.toLowerCase(Locale.ROOT));
    }

    public static String getFriendlyName(String itemId) {
        ItemEntry e = getPreset(itemId);
        if (e != null) return e.getDefaultDisplayName();
        // Convert "minecraft:ender_pearl" -> "Ender Pearl"
        String raw = itemId.contains(":") ? itemId.substring(itemId.indexOf(':') + 1) : itemId;
        String[] parts = raw.split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!p.isEmpty()) {
                sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }
}
