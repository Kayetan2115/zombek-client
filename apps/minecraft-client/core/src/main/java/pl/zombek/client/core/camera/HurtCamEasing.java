package pl.zombek.client.core.camera;

/**
 * Easing animation curves for damage tilt recovery.
 */
public enum HurtCamEasing {
    SPRING("Sprężyste (Spring)"),
    EASE_OUT("Wygładzenie wyjścia (Ease Out)"),
    DAMPED("Tłumione (Damped Oscillation)"),
    LINEAR("Liniowe (Linear)");

    private final String displayName;

    HurtCamEasing(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double ease(double t) {
        double clamped = Math.max(0.0, Math.min(1.0, t));
        return switch (this) {
            case LINEAR -> clamped;
            case EASE_OUT -> 1.0 - Math.pow(1.0 - clamped, 2);
            case SPRING -> 1.0 - Math.cos(clamped * Math.PI * 2.5) * Math.exp(-clamped * 3.5);
            case DAMPED -> 1.0 - Math.cos(clamped * Math.PI * 4.0) * Math.exp(-clamped * 4.0);
        };
    }

    @Override
    public String toString() {
        return displayName;
    }
}
