package pl.zombek.client.core.modules.visual;

/**
 * Mathematical curve controlling the fading of hit color over time.
 */
public enum FadeCurve {
    LINEAR("Liniowa", "Stałe tempo wygaszania koloru"),
    EASE_OUT("Ease-Out", "Szybki start, płynne zwalniające wygaszanie"),
    EXPONENTIAL("Wykładnicza", "Intensywne podświetlenie z gwałtownym spadkiem");

    private final String displayName;
    private final String description;

    FadeCurve(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public float calculateFactor(float progress) {
        float p = Math.min(1.0f, Math.max(0.0f, progress));
        return switch (this) {
            case LINEAR -> 1.0f - p;
            case EASE_OUT -> 1.0f - (float) Math.sin(p * Math.PI / 2.0);
            case EXPONENTIAL -> (float) Math.pow(1.0 - p, 2.0);
        };
    }
}
