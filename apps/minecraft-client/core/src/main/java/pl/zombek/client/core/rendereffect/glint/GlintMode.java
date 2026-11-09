package pl.zombek.client.core.rendereffect.glint;

/**
 * Rendering modes for enchantment glint.
 */
public enum GlintMode {
    VANILLA("Vanilla", "Standardowy fioletowy waniliowy glint Minecraft"),
    STATIC("Static", "Pojedynczy, stały kolor glintu"),
    RAINBOW("Rainbow", "Płynnie animowana tęcza barw bez alokacji RenderType"),
    GRADIENT("Gradient", "Płynne przejście tonalne pomiędzy dwoma wybranymi barwami"),
    PULSE("Pulse", "Pulsacja jasności i nasycenia wybranego koloru"),
    CUSTOM("Custom", "Niezależna konfiguracja kolorystyczna dla poszczególnych kategorii");

    private final String displayName;
    private final String description;

    GlintMode(String displayName, String description) {
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
