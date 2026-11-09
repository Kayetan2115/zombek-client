package pl.zombek.client.core.rendereffect.particle;

/**
 * Functional categories of Minecraft particle effects.
 */
public enum ParticleCategory {
    COMBAT("Combat", "Cząsteczki krytyków, obrażeń, zamachu miecza i trafień"),
    ENVIRONMENT("Environment", "Deszcz, rozbryzgi wody, dym, ogniska, pyłki roślin"),
    MAGIC("Magic", "Efekty mikstur, zaklęć, portali, oddechu smoka"),
    BLOCKS("Blocks", "Cząstki niszczenia, stawiania i sypania się bloków"),
    ITEMS("Items", "Cząstki pękających przedmiotów, kul śniegu, pereł"),
    OTHER("Other", "Serca, nutki, złość/zadowolenie wieśniaków, totem nieśmiertelności");

    private final String displayName;
    private final String description;

    ParticleCategory(String displayName, String description) {
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
