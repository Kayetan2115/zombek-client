package pl.zombek.client.core.camera;

/**
 * Easing animation curves for smooth Zoom transitions.
 */
public enum ZoomEasing {
    LINEAR("Liniowe (Linear)"),
    EASE_IN("Wygładzenie wejścia (Ease In)"),
    EASE_OUT("Wygładzenie wyjścia (Ease Out)"),
    EASE_IN_OUT("Wygładzenie wejścia i wyjścia (Ease In-Out)"),
    EXPONENTIAL("Wykładnicze (Exponential)");

    private final String displayName;

    ZoomEasing(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Applies the easing curve to normalized progress t in [0.0, 1.0].
     */
    public double ease(double t) {
        double clamped = Math.max(0.0, Math.min(1.0, t));
        return switch (this) {
            case LINEAR -> clamped;
            case EASE_IN -> clamped * clamped;
            case EASE_OUT -> 1.0 - Math.pow(1.0 - clamped, 2);
            case EASE_IN_OUT -> clamped < 0.5
                    ? 2.0 * clamped * clamped
                    : 1.0 - Math.pow(-2.0 * clamped + 2.0, 2) / 2.0;
            case EXPONENTIAL -> clamped == 0.0 ? 0.0 : Math.pow(2.0, 10.0 * (clamped - 1.0));
        };
    }

    @Override
    public String toString() {
        return displayName;
    }
}
