package pl.zombek.client.core.rendereffect.fog;

/**
 * Configuration profile for a specific fog environment (e.g. Air, Water, Lava).
 */
public class FogEnvironmentProfile {
    private final FogEnvironmentType type;
    private boolean enabled;
    private boolean customColorEnabled;
    private int customColorArgb;
    private float customStartDistance;
    private float customEndDistance;
    private float intensity;

    public FogEnvironmentProfile(FogEnvironmentType type, float defaultStart, float defaultEnd, int defaultColor) {
        this.type = type;
        this.enabled = true;
        this.customColorEnabled = false;
        this.customColorArgb = defaultColor;
        this.customStartDistance = defaultStart;
        this.customEndDistance = defaultEnd;
        this.intensity = 1.0f;
    }

    public FogEnvironmentType getType() {
        return type;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isCustomColorEnabled() {
        return customColorEnabled;
    }

    public void setCustomColorEnabled(boolean customColorEnabled) {
        this.customColorEnabled = customColorEnabled;
    }

    public int getCustomColorArgb() {
        return customColorArgb;
    }

    public void setCustomColorArgb(int customColorArgb) {
        this.customColorArgb = customColorArgb;
    }

    public float getCustomStartDistance() {
        return customStartDistance;
    }

    public void setCustomStartDistance(float customStartDistance) {
        this.customStartDistance = Math.max(0.0f, customStartDistance);
    }

    public float getCustomEndDistance() {
        return customEndDistance;
    }

    public void setCustomEndDistance(float customEndDistance) {
        this.customEndDistance = Math.max(customStartDistance + 0.5f, customEndDistance);
    }

    public float getIntensity() {
        return intensity;
    }

    public void setIntensity(float intensity) {
        this.intensity = Math.max(0.0f, Math.min(3.0f, intensity));
    }
}
