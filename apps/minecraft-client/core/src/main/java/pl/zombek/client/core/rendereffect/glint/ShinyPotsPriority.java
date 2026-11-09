package pl.zombek.client.core.rendereffect.glint;

/**
 * Resolution priority between Glint Colorizer and Shiny Pots module.
 */
public enum ShinyPotsPriority {
    USE_GLOBAL("Use Global", "Używaj globalnego koloru Glint Colorizer dla wszystkich mikstur"),
    USE_POTION_SPECIFIC("Use Potion Category", "Używaj ustawień kategorii mikstur z Glint Colorizer"),
    PRESERVE_SHINY_POTS("Preserve Shiny Pots", "Zachowaj kolory i reguły z modułu Shiny Pots jeśli jest włączony"),
    OVERRIDE_SHINY_POTS("Override Shiny Pots", "Wymuś kolorystykę Glint Colorizer ignorując Shiny Pots");

    private final String displayName;
    private final String description;

    ShinyPotsPriority(String displayName, String description) {
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
