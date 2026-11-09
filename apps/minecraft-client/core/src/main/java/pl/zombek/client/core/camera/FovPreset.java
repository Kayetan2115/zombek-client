package pl.zombek.client.core.camera;

/**
 * Predefined FOV profiles for quick configuration.
 */
public enum FovPreset {
    DEFAULT("Domyślny (70°)", 70.0),
    LOW("Niski (60°)", 60.0),
    MEDIUM("Średni (80°)", 80.0),
    HIGH("Wysoki (90°)", 90.0),
    PRO_PVP("PvP Pro (100°)", 100.0),
    QUAKE_PRO("Quake Pro (110°)", 110.0),
    CUSTOM("Niestandardowy (Własny)", -1.0);

    private final String displayName;
    private final double fovValue;

    FovPreset(String displayName, double fovValue) {
        this.displayName = displayName;
        this.fovValue = fovValue;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getFovValue() {
        return fovValue;
    }

    public boolean isCustom() {
        return this == CUSTOM;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
