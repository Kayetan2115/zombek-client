package pl.zombek.client.core.modules.pvp.snaplook;

/**
 * Interpolation curve for smooth camera perspective transitions.
 */
public enum SnaplookEasing {
    LINEAR("Liniowe (Linear)"),
    EASE_OUT("Płynne wyhamowanie (Ease Out)"),
    SMOOTH_STEP("Krzywa sferyczna (Smooth Step)");

    private final String displayName;

    SnaplookEasing(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public float interpolate(float t) {
        float clamped = Math.max(0.0f, Math.min(1.0f, t));
        return switch (this) {
            case LINEAR -> clamped;
            case EASE_OUT -> 1.0f - (1.0f - clamped) * (1.0f - clamped);
            case SMOOTH_STEP -> clamped * clamped * (3.0f - 2.0f * clamped);
        };
    }
}
