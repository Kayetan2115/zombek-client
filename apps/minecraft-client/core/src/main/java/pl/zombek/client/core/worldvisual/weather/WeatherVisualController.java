package pl.zombek.client.core.worldvisual.weather;

/**
 * Controls client-side weather presentation (Clear, Rain, Snow, Thunder).
 * Modifies only local renderer particle and sky gradients without altering server weather.
 */
public class WeatherVisualController {
    private static final WeatherVisualController INSTANCE = new WeatherVisualController();

    private boolean enabled = false;
    private WeatherPreset preset = WeatherPreset.CLEAR;

    // Rain settings
    private boolean enableRain = true;
    private float rainIntensity = 1.0f;
    private float rainSpeed = 1.0f;
    private float rainOpacity = 1.0f;

    // Snow settings
    private boolean enableSnow = true;
    private float snowIntensity = 1.0f;
    private float snowSize = 1.0f;
    private float snowOpacity = 1.0f;

    // Cloud settings
    private boolean showClouds = true;
    private float cloudDensity = 1.0f;
    private float cloudOpacity = 1.0f;
    private float cloudSpeed = 1.0f;

    // Thunder settings
    private boolean enableVisualThunder = false;
    private float thunderIntensity = 0.8f;
    private float flashIntensity = 0.5f;

    // Performance
    private int maxWeatherParticles = 500;

    // Dimensions & Climate
    private boolean enableInOverworld = true;
    private boolean enableInNether = false;
    private boolean enableInEnd = false;
    private boolean respectBiomeClimate = true;
    private boolean overrideBiomeRestrictions = false;

    private final WeatherTransitionManager transitionManager = new WeatherTransitionManager();

    private WeatherVisualController() {}

    public static WeatherVisualController getInstance() {
        return INSTANCE;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        updateTargetGradients();
    }

    public WeatherPreset getPreset() {
        return preset;
    }

    public void setPreset(WeatherPreset preset) {
        this.preset = preset != null ? preset : WeatherPreset.CLEAR;
        updateTargetGradients();
    }

    public boolean isEnableRain() {
        return enableRain;
    }

    public void setEnableRain(boolean enableRain) {
        this.enableRain = enableRain;
        updateTargetGradients();
    }

    public float getRainIntensity() {
        return rainIntensity;
    }

    public void setRainIntensity(float rainIntensity) {
        this.rainIntensity = Math.max(0.0f, Math.min(1.0f, rainIntensity));
        updateTargetGradients();
    }

    public float getRainSpeed() {
        return rainSpeed;
    }

    public void setRainSpeed(float rainSpeed) {
        this.rainSpeed = Math.max(0.1f, Math.min(3.0f, rainSpeed));
    }

    public float getRainOpacity() {
        return rainOpacity;
    }

    public void setRainOpacity(float rainOpacity) {
        this.rainOpacity = Math.max(0.1f, Math.min(1.0f, rainOpacity));
    }

    public boolean isEnableSnow() {
        return enableSnow;
    }

    public void setEnableSnow(boolean enableSnow) {
        this.enableSnow = enableSnow;
        updateTargetGradients();
    }

    public float getSnowIntensity() {
        return snowIntensity;
    }

    public void setSnowIntensity(float snowIntensity) {
        this.snowIntensity = Math.max(0.0f, Math.min(1.0f, snowIntensity));
        updateTargetGradients();
    }

    public float getSnowSize() {
        return snowSize;
    }

    public void setSnowSize(float snowSize) {
        this.snowSize = Math.max(0.2f, Math.min(3.0f, snowSize));
    }

    public float getSnowOpacity() {
        return snowOpacity;
    }

    public void setSnowOpacity(float snowOpacity) {
        this.snowOpacity = Math.max(0.1f, Math.min(1.0f, snowOpacity));
    }

    public boolean isShowClouds() {
        return showClouds;
    }

    public void setShowClouds(boolean showClouds) {
        this.showClouds = showClouds;
    }

    public float getCloudDensity() {
        return cloudDensity;
    }

    public void setCloudDensity(float cloudDensity) {
        this.cloudDensity = Math.max(0.1f, Math.min(3.0f, cloudDensity));
    }

    public float getCloudOpacity() {
        return cloudOpacity;
    }

    public void setCloudOpacity(float cloudOpacity) {
        this.cloudOpacity = Math.max(0.0f, Math.min(1.0f, cloudOpacity));
    }

    public float getCloudSpeed() {
        return cloudSpeed;
    }

    public void setCloudSpeed(float cloudSpeed) {
        this.cloudSpeed = Math.max(0.1f, Math.min(3.0f, cloudSpeed));
    }

    public boolean isEnableVisualThunder() {
        return enableVisualThunder;
    }

    public void setEnableVisualThunder(boolean enableVisualThunder) {
        this.enableVisualThunder = enableVisualThunder;
        updateTargetGradients();
    }

    public float getThunderIntensity() {
        return thunderIntensity;
    }

    public void setThunderIntensity(float thunderIntensity) {
        this.thunderIntensity = Math.max(0.0f, Math.min(1.0f, thunderIntensity));
        updateTargetGradients();
    }

    public float getFlashIntensity() {
        return flashIntensity;
    }

    public void setFlashIntensity(float flashIntensity) {
        this.flashIntensity = Math.max(0.0f, Math.min(1.0f, flashIntensity));
    }

    public int getMaxWeatherParticles() {
        return maxWeatherParticles;
    }

    public void setMaxWeatherParticles(int maxWeatherParticles) {
        this.maxWeatherParticles = Math.max(50, Math.min(2000, maxWeatherParticles));
    }

    public boolean isEnableInOverworld() {
        return enableInOverworld;
    }

    public void setEnableInOverworld(boolean enableInOverworld) {
        this.enableInOverworld = enableInOverworld;
    }

    public boolean isEnableInNether() {
        return enableInNether;
    }

    public void setEnableInNether(boolean enableInNether) {
        this.enableInNether = enableInNether;
    }

    public boolean isEnableInEnd() {
        return enableInEnd;
    }

    public void setEnableInEnd(boolean enableInEnd) {
        this.enableInEnd = enableInEnd;
    }

    public boolean isRespectBiomeClimate() {
        return respectBiomeClimate;
    }

    public void setRespectBiomeClimate(boolean respectBiomeClimate) {
        this.respectBiomeClimate = respectBiomeClimate;
    }

    public boolean isOverrideBiomeRestrictions() {
        return overrideBiomeRestrictions;
    }

    public void setOverrideBiomeRestrictions(boolean overrideBiomeRestrictions) {
        this.overrideBiomeRestrictions = overrideBiomeRestrictions;
    }

    public WeatherTransitionManager getTransitionManager() {
        return transitionManager;
    }

    private void updateTargetGradients() {
        if (!enabled || preset == WeatherPreset.VANILLA) {
            return;
        }

        float targetRain = 0.0f;
        float targetThunder = 0.0f;

        switch (preset) {
            case CLEAR -> {
                targetRain = 0.0f;
                targetThunder = 0.0f;
            }
            case RAIN -> {
                targetRain = rainIntensity;
                targetThunder = 0.0f;
            }
            case SNOW -> {
                targetRain = snowIntensity;
                targetThunder = 0.0f;
            }
            case THUNDER -> {
                targetRain = rainIntensity;
                targetThunder = thunderIntensity;
            }
            case CUSTOM -> {
                targetRain = enableRain ? rainIntensity : (enableSnow ? snowIntensity : 0.0f);
                targetThunder = enableVisualThunder ? thunderIntensity : 0.0f;
            }
            case VANILLA -> {}
        }

        transitionManager.setTargetGradients(targetRain, targetThunder, false);
    }

    public boolean shouldApplyInDimension(String dimensionKey) {
        if (!enabled || preset == WeatherPreset.VANILLA) {
            return false;
        }
        if (dimensionKey == null) {
            return enableInOverworld;
        }
        String lower = dimensionKey.toLowerCase();
        if (lower.contains("overworld")) {
            return enableInOverworld;
        } else if (lower.contains("the_nether") || lower.contains("nether")) {
            return enableInNether;
        } else if (lower.contains("the_end") || lower.contains("end")) {
            return enableInEnd;
        }
        return false;
    }

    /**
     * Resolves the visual rain gradient for rendering.
     * @param vanillaRain real server rain gradient [0.0, 1.0]
     * @param dimensionKey current dimension id
     * @param biomeCanRain whether the biome climate naturally supports rain
     * @return visual rain gradient between 0.0 and 1.0
     */
    public float getVisualRainGradient(float vanillaRain, String dimensionKey, boolean biomeCanRain) {
        if (!shouldApplyInDimension(dimensionKey)) {
            return vanillaRain;
        }
        if (respectBiomeClimate && !overrideBiomeRestrictions && !biomeCanRain && preset != WeatherPreset.CLEAR) {
            // Respect desert/savanna/badlands no-rain climate
            return 0.0f;
        }
        transitionManager.update();
        return transitionManager.getCurrentRainGradient();
    }

    /**
     * Resolves the visual thunder gradient for rendering.
     */
    public float getVisualThunderGradient(float vanillaThunder, String dimensionKey) {
        if (!shouldApplyInDimension(dimensionKey)) {
            return vanillaThunder;
        }
        transitionManager.update();
        return transitionManager.getCurrentThunderGradient();
    }

    public void resetForTesting() {
        enabled = false;
        preset = WeatherPreset.CLEAR;
        enableRain = true;
        rainIntensity = 1.0f;
        rainSpeed = 1.0f;
        rainOpacity = 1.0f;
        enableSnow = true;
        snowIntensity = 1.0f;
        snowSize = 1.0f;
        snowOpacity = 1.0f;
        showClouds = true;
        cloudDensity = 1.0f;
        cloudOpacity = 1.0f;
        cloudSpeed = 1.0f;
        enableVisualThunder = false;
        thunderIntensity = 0.8f;
        flashIntensity = 0.5f;
        maxWeatherParticles = 500;
        enableInOverworld = true;
        enableInNether = false;
        enableInEnd = false;
        respectBiomeClimate = true;
        overrideBiomeRestrictions = false;
        transitionManager.reset();
    }
}
