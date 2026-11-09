package pl.zombek.client.core.rendereffect.fog;

/**
 * Predefined fog density and distance profiles.
 */
public enum FogPreset {
    VANILLA("Vanilla", "Standardowa waniliowa mgła gry"),
    REDUCED_FOG("Reduced Fog", "Ograniczona intensywność mgły dla lepszej widoczności"),
    CLEAR_VIEW("Clear View", "Maksymalnie oddalona mgła w dozwolonym zasięgu renderowania"),
    DENSE_FOG("Dense Fog", "Gęsta, klimatyczna mgła o zmniejszonym zasięgu widzenia"),
    SOFT_FOG("Soft Fog", "Łagodna, subtelna mgła z wydłużonym łagodnym spadkiem"),
    CUSTOM("Custom", "Pełna manualna konfiguracja parametrów mgły");

    private final String displayName;
    private final String description;

    FogPreset(String displayName, String description) {
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
