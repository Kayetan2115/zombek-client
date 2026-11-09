package pl.zombek.client.core.rendereffect.glint;

/**
 * Item category for specific enchantment glint coloring overrides.
 */
public enum GlintItemCategory {
    WEAPONS("Weapons", "Miecze, topory bojowe, łuki, kusze, trójzęby"),
    TOOLS("Tools", "Kilofy, łopaty, siekiery robocze, motyki, wędki, nożyce"),
    ARMOR("Armor", "Hełmy, napierśniki, nogawice, buty"),
    BOOKS("Books", "Zaklęte księgi"),
    POTIONS("Potions", "Mikstury standardowe, miotane, trwałe i strzały z miksturą"),
    ELYTRA("Elytra", "Elitry"),
    SHIELDS("Shields", "Tarcze"),
    OTHER("Other", "Pozostałe przedmioty z efektem zaklęcia");

    private final String displayName;
    private final String description;

    GlintItemCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static GlintItemCategory fromRegistryId(String registryId) {
        if (registryId == null || registryId.isEmpty()) {
            return OTHER;
        }

        String id = registryId.toLowerCase();
        if (id.startsWith("minecraft:")) {
            id = id.substring("minecraft:".length());
        }

        if (id.contains("sword") || id.contains("bow") || id.contains("crossbow") || id.contains("trident")) {
            return WEAPONS;
        }
        if (id.endsWith("_axe")) {
            return WEAPONS;
        }
        if (id.contains("pickaxe") || id.contains("shovel") || id.contains("hoe") || id.contains("fishing_rod") || id.contains("shears")) {
            return TOOLS;
        }
        if (id.contains("helmet") || id.contains("chestplate") || id.contains("leggings") || id.contains("boots") || id.contains("turtle_shell")) {
            return ARMOR;
        }
        if (id.contains("book")) {
            return BOOKS;
        }
        if (id.contains("potion") || id.contains("tipped_arrow")) {
            return POTIONS;
        }
        if (id.contains("elytra")) {
            return ELYTRA;
        }
        if (id.contains("shield")) {
            return SHIELDS;
        }

        return OTHER;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
