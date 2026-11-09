package pl.zombek.client.core.modules.visual;

/**
 * Available crosshair render styles.
 */
public enum CrosshairStyle {
    CLASSIC_CROSS("Classic Cross", "Klasyczny krzyżyk z 4 ramionami i odstępem"),
    SIMPLE_CROSS("Simple Cross", "Prosty ciągły krzyżyk"),
    DOT("Dot", "Centralny punkt / kropka celownika"),
    CIRCLE("Circle", "Okrągły pierścień celownika"),
    SQUARE("Square", "Kwadratowy celownik (obrys lub wypełnienie)"),
    T_SHAPE("T-Shape", "Celownik w kształcie litery T (bez dolnego ramienia)"),
    DYNAMIC_CROSS("Dynamic Cross", "Dynamiczny krzyżyk reagujący na cel i cooldown"),
    CUSTOM("Custom", "Pełna personalizacja każdego ramienia i kształtu");

    private final String displayName;
    private final String description;

    CrosshairStyle(String displayName, String description) {
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
