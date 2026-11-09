package pl.zombek.client.core.hud.saturation;

/**
 * Fallback provider used in tests or when no player is active.
 * When preview mode is toggled, simulates standard available food metrics.
 */
public class FallbackSaturationDataProvider implements ISaturationDataProvider {

    private boolean previewMode = false;
    private int foodLevel = 20;
    private boolean saturationAvailable = false;
    private float saturationLevel = 5.0f;
    private boolean exhaustionAvailable = false;
    private float exhaustionLevel = 0.0f;
    private Boolean playerAvailableOverride = null;

    public void setPreviewMode(boolean previewMode) {
        this.previewMode = previewMode;
        if (previewMode) {
            this.foodLevel = 18;
            this.saturationAvailable = true;
            this.saturationLevel = 12.5f;
            this.exhaustionAvailable = true;
            this.exhaustionLevel = 1.25f;
        } else {
            this.saturationAvailable = false;
        }
    }

    public void setFoodLevel(int foodLevel) {
        this.foodLevel = foodLevel;
    }

    public void setSaturationLevel(float saturationLevel) {
        this.saturationLevel = saturationLevel;
    }

    public void setSaturationAvailable(boolean saturationAvailable) {
        this.saturationAvailable = saturationAvailable;
    }

    public void setExhaustionLevel(float exhaustionLevel) {
        this.exhaustionLevel = exhaustionLevel;
        this.exhaustionAvailable = true;
    }

    public void setExhaustionAvailable(boolean exhaustionAvailable) {
        this.exhaustionAvailable = exhaustionAvailable;
    }

    public void setPlayerAvailable(boolean available) {
        this.playerAvailableOverride = available;
    }

    public void setSaturation(boolean available, float level) {
        this.saturationAvailable = available;
        this.saturationLevel = level;
    }

    public void setExhaustion(boolean available, float level) {
        this.exhaustionAvailable = available;
        this.exhaustionLevel = level;
    }

    @Override
    public boolean isPlayerAvailable() {
        if (playerAvailableOverride != null) {
            return playerAvailableOverride;
        }
        return previewMode || saturationAvailable || foodLevel > 0;
    }

    @Override
    public int getFoodLevel() {
        return foodLevel;
    }

    @Override
    public boolean isSaturationAvailable() {
        return saturationAvailable;
    }

    @Override
    public float getSaturationLevel() {
        return saturationLevel;
    }

    @Override
    public boolean isExhaustionAvailable() {
        return exhaustionAvailable;
    }

    @Override
    public float getExhaustionLevel() {
        return exhaustionLevel;
    }
}
