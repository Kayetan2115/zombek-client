package pl.zombek.client.core.modules.visual.oneseven;

/**
 * Item categories for custom 1.7 item position transformations.
 */
public enum ItemCategory {
    SWORD("Sword", "Miecze"),
    AXE("Axe", "Topory"),
    PICKAXE("Pickaxe", "Kilofy"),
    SHOVEL("Shovel", "Łopaty"),
    HOE("Hoe", "Motyki"),
    FISHING_ROD("Fishing Rod", "Wędka"),
    BOW("Bow", "Łuk"),
    CROSSBOW("Crossbow", "Kusza"),
    FOOD("Food", "Jedzenie"),
    POTION("Potion", "Mikstury"),
    SHIELD("Shield", "Tarcza"),
    OTHER("Other Items", "Pozostałe przedmioty");

    private final String displayName;
    private final String description;

    ItemCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Determines item category based on item registry ID string (e.g. "minecraft:diamond_sword").
     */
    public static ItemCategory fromItemId(String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            return OTHER;
        }
        String lower = itemId.toLowerCase();
        if (lower.endsWith("_sword") || lower.equals("minecraft:sword")) {
            return SWORD;
        }
        if (lower.endsWith("_axe") && !lower.contains("pickaxe")) {
            return AXE;
        }
        if (lower.contains("pickaxe")) {
            return PICKAXE;
        }
        if (lower.contains("shovel")) {
            return SHOVEL;
        }
        if (lower.contains("hoe")) {
            return HOE;
        }
        if (lower.contains("fishing_rod")) {
            return FISHING_ROD;
        }
        if (lower.equals("minecraft:bow")) {
            return BOW;
        }
        if (lower.contains("crossbow")) {
            return CROSSBOW;
        }
        if (lower.contains("potion") || lower.contains("bottle")) {
            return POTION;
        }
        if (lower.equals("minecraft:shield")) {
            return SHIELD;
        }
        if (isFoodItem(lower)) {
            return FOOD;
        }
        return OTHER;
    }

    private static boolean isFoodItem(String lower) {
        return lower.contains("apple") || lower.contains("bread") || lower.contains("porkchop")
                || lower.contains("beef") || lower.contains("chicken") || lower.contains("mutton")
                || lower.contains("rabbit") || lower.contains("fish") || lower.contains("salmon")
                || lower.contains("cookie") || lower.contains("melon") || lower.contains("carrot")
                || lower.contains("potato") || lower.contains("stew") || lower.contains("soup")
                || lower.contains("pie") || lower.contains("berry") || lower.contains("berries")
                || lower.contains("honey_bottle") || lower.contains("chorus_fruit");
    }
}
