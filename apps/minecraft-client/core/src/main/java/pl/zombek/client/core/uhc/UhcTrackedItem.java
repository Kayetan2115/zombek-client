package pl.zombek.client.core.uhc;

public enum UhcTrackedItem {
    GOLDEN_APPLE("G-Apples", "GAP", "minecraft:golden_apple"),
    GOLDEN_HEAD("G-Heads", "GHD", "minecraft:player_head"),
    ARROW("Arrows", "ARR", "minecraft:arrow"),
    ENDER_PEARL("Pearls", "PRL", "minecraft:ender_pearl"),
    HEALING("Healing", "HLG", "minecraft:potion"),
    BLOCKS("Blocks", "BLK", "minecraft:cobblestone"),
    FLINT_AND_STEEL("F&S", "F&S", "minecraft:flint_and_steel"),
    WATER_BUCKET("Water", "H2O", "minecraft:water_bucket"),
    LAVA_BUCKET("Lava", "LAV", "minecraft:lava_bucket");

    private final String displayName;
    private final String shortName;
    private final String defaultItemId;

    UhcTrackedItem(String displayName, String shortName, String defaultItemId) {
        this.displayName = displayName;
        this.shortName = shortName;
        this.defaultItemId = defaultItemId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getShortName() {
        return shortName;
    }

    public String getDefaultItemId() {
        return defaultItemId;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
