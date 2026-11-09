package pl.zombek.client.core.camera;

/**
 * Quality levels for Motion Blur accumulation and sampling.
 */
public enum MotionBlurQuality {
    LOW("Niska (2 Próbki)", 2, 0.5f),
    MEDIUM("Średnia (4 Próbki)", 4, 0.75f),
    HIGH("Wysoka (6 Próbek)", 6, 1.0f),
    ULTRA("Ultra (8 Próbek)", 8, 1.0f);

    private final String displayName;
    private final int sampleCount;
    private final float defaultResolutionScale;

    MotionBlurQuality(String displayName, int sampleCount, float defaultResolutionScale) {
        this.displayName = displayName;
        this.sampleCount = sampleCount;
        this.defaultResolutionScale = defaultResolutionScale;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public float getDefaultResolutionScale() {
        return defaultResolutionScale;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
