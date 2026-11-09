package pl.zombek.client.core.rendereffect.particle;

/**
 * Predefined particle density and performance presets.
 */
public enum ParticlePreset {
    VANILLA("Vanilla", "Standardowa ilość i zachowanie cząsteczek"),
    LOW("Low", "Zredukowana ilość cząsteczek dla słabszych konfiguracji sprzętowych"),
    MEDIUM("Medium", "Zrównoważona ilość cząsteczek dla stabilnej rozgrywki"),
    HIGH("High", "Maksymalna widowiskowość cząsteczek"),
    REDUCED_PARTICLES("Reduced Particles", "Minimalne cząstki otoczenia, skupienie na cząstkach walki"),
    MINIMAL_PARTICLES("Minimal Particles", "Agresywna redukcja do absolutnego minimum"),
    CUSTOM("Custom", "Ręczna konfiguracja każdej kategorii i reguł indywidualnych");

    private final String displayName;
    private final String description;

    ParticlePreset(String displayName, String description) {
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
