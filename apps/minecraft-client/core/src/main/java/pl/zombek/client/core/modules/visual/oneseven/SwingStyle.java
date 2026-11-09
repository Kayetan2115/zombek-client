package pl.zombek.client.core.modules.visual.oneseven;

/**
 * Hand swing animation styles.
 */
public enum SwingStyle {
    VANILLA_1_7("Vanilla 1.7", "Szybki, szeroki zamach ręki niezależny od attack cooldownu"),
    VANILLA_1_8("Vanilla 1.8", "Klasyczny zamach z wersji 1.8 z zachowaniem proporcji"),
    MODERN("Modern", "Domyślna animacja zamachu współczesnego Minecrafta"),
    CUSTOM("Custom", "Własna prędkość, kąt i intensywność zamachu");

    private final String displayName;
    private final String description;

    SwingStyle(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
