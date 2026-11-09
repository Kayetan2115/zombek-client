package pl.zombek.client.core.titles;

/**
 * Visual styling presets for Titles, Subtitles, and Action Bar.
 */
public enum TitlePreset {
    VANILLA("Vanilla", "Standardowy wygląd i proporcje Minecraft"),
    CLASSIC("Classic", "Klasyczny tekst z delikatnym cieniem i tradycyjnymi odstępami"),
    MODERN("Modern", "Nowoczesny panel, dopracowane skalowanie i płynne animacje"),
    MINIMAL("Minimal", "Prosty tekst o stonowanych efektach i maksymalnej czytelności"),
    CUSTOM("Custom", "Pełna personalizacja wszystkich parametrów");

    private final String displayName;
    private final String description;

    TitlePreset(String displayName, String description) {
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
