package pl.zombek.client.core.info;

/**
 * Universal dimension classification for Minecraft worlds.
 */
public enum DimensionType {
    OVERWORLD("Overworld", "minecraft:overworld"),
    NETHER("Nether", "minecraft:the_nether"),
    END("The End", "minecraft:the_end"),
    CUSTOM("Inny", "custom");

    private final String displayName;
    private final String identifier;

    DimensionType(String displayName, String identifier) {
        this.displayName = displayName;
        this.identifier = identifier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIdentifier() {
        return identifier;
    }

    /**
     * Resolves DimensionType from raw dimension string (e.g. from RegistryKey or ResourceLocation).
     */
    public static DimensionType fromString(String raw) {
        if (raw == null) return OVERWORLD;
        String lower = raw.toLowerCase().trim();
        if (lower.contains("nether")) return NETHER;
        if (lower.contains("end")) return END;
        if (lower.contains("overworld")) return OVERWORLD;
        return CUSTOM;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
