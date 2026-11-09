package pl.zombek.client.core.rendereffect.fog;

/**
 * Atmospheric environments supported by the Fog Customizer.
 */
public enum FogEnvironmentType {
    AIR("Air Fog", "Standardowa mgła atmosferyczna w powietrzu"),
    WATER("Water Fog", "Mgła podwodna podczas zanurzenia gracza"),
    LAVA("Lava Fog", "Gęsta mgła w lawie"),
    POWDER_SNOW("Powder Snow Fog", "Oślepiająca mgła śnieżna w sypkim śniegu"),
    DIMENSION("Dimension Fog", "Mgła wynikająca ze specyfiki wymiaru (Nether/The End)"),
    BIOME("Biome Fog", "Mgła przypisana do konkretnego biomu (bagna, lasy mroczne)"),
    BOSS("Boss Fog", "Mgła generowana podczas walk z bossami (np. Ender Dragon, Wither)");

    private final String displayName;
    private final String description;

    FogEnvironmentType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
