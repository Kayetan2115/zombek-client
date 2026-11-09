package pl.zombek.client.core.rendereffect.fog;

/**
 * Manages Fog Customizer active presets and user presets.
 */
public class FogProfileManager {

    private FogPreset activePreset = FogPreset.VANILLA;

    public FogProfileManager() {}

    public FogPreset getActivePreset() {
        return activePreset;
    }

    public void setActivePreset(FogPreset preset) {
        this.activePreset = preset != null ? preset : FogPreset.VANILLA;
    }

    public void reset() {
        this.activePreset = FogPreset.VANILLA;
    }
}
