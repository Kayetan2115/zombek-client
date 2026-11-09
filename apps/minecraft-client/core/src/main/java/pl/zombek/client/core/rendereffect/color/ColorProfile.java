package pl.zombek.client.core.rendereffect.color;

/**
 * Predefined color grading and saturation presets.
 */
public enum ColorProfile {
    VANILLA("Vanilla", "Standardowe waniliowe odwzorowanie barw Minecraft"),
    VIBRANT("Vibrant", "Podwyższone nasycenie (150%) z dynamicznym balansem vibrance"),
    NATURAL("Natural", "Umiarkowane, naturalne ocieplenie tonacji i stonowane barwy"),
    SOFT("Soft", "Pastelowe, łagodne nasycenie redukujące zmęczenie wzroku"),
    CINEMATIC("Cinematic", "Filmowy kontrast z subtelnym chłodem w cieniach i ciepłem w światłach"),
    MONOCHROME("Monochrome", "Całkowity brak nasycenia (skala szarości)"),
    CUSTOM("Custom", "Pełna personalizacja suwaków nasycenia, kontrastu i kanałów RGB");

    private final String displayName;
    private final String description;

    ColorProfile(String displayName, String description) {
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
