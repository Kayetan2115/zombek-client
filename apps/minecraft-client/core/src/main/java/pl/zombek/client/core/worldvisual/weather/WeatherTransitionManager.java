package pl.zombek.client.core.worldvisual.weather;

/**
 * Manages smooth fading of rain and thunder gradients.
 */
public class WeatherTransitionManager {
    private boolean smoothTransition = true;
    private long transitionDurationMs = 1000L;

    private float currentRainGradient = 0.0f;
    private float targetRainGradient = 0.0f;
    private float startRainGradient = 0.0f;

    private float currentThunderGradient = 0.0f;
    private float targetThunderGradient = 0.0f;
    private float startThunderGradient = 0.0f;

    private long animationStartTime = 0L;

    public WeatherTransitionManager() {}

    public void configure(boolean smoothTransition, long transitionDurationMs) {
        this.smoothTransition = smoothTransition;
        this.transitionDurationMs = Math.max(50L, Math.min(5000L, transitionDurationMs));
    }

    public void setTargetGradients(float targetRain, float targetThunder, boolean instant) {
        this.targetRainGradient = Math.max(0.0f, Math.min(1.0f, targetRain));
        this.targetThunderGradient = Math.max(0.0f, Math.min(1.0f, targetThunder));

        if (instant || !smoothTransition) {
            this.currentRainGradient = this.targetRainGradient;
            this.startRainGradient = this.targetRainGradient;
            this.currentThunderGradient = this.targetThunderGradient;
            this.startThunderGradient = this.targetThunderGradient;
            this.animationStartTime = 0L;
        } else {
            this.animationStartTime = System.currentTimeMillis();
            this.startRainGradient = this.currentRainGradient;
            this.startThunderGradient = this.currentThunderGradient;
        }
    }

    public void update() {
        if (!smoothTransition || animationStartTime <= 0) {
            return;
        }

        long elapsed = System.currentTimeMillis() - animationStartTime;
        if (elapsed >= transitionDurationMs) {
            currentRainGradient = targetRainGradient;
            currentThunderGradient = targetThunderGradient;
            return;
        }

        float progress = Math.min(1.0f, Math.max(0.0f, (float) elapsed / (float) transitionDurationMs));
        // Easing (EASE_IN_OUT)
        float eased = progress < 0.5f ? 2.0f * progress * progress : 1.0f - (float) Math.pow(-2.0f * progress + 2.0f, 2.0) / 2.0f;

        currentRainGradient = startRainGradient + (targetRainGradient - startRainGradient) * eased;
        currentThunderGradient = startThunderGradient + (targetThunderGradient - startThunderGradient) * eased;
    }

    public float getCurrentRainGradient() {
        return currentRainGradient;
    }

    public float getCurrentThunderGradient() {
        return currentThunderGradient;
    }

    public void reset() {
        currentRainGradient = 0.0f;
        targetRainGradient = 0.0f;
        startRainGradient = 0.0f;
        currentThunderGradient = 0.0f;
        targetThunderGradient = 0.0f;
        startThunderGradient = 0.0f;
        animationStartTime = 0L;
    }
}
