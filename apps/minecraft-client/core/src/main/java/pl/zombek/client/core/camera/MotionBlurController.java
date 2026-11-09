package pl.zombek.client.core.camera;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.zombek.client.core.modules.visual.MotionBlurModule;

/**
 * Controller calculating camera rotation motion deltas and determining
 * dynamic temporal blend / blur accumulation parameters for Motion Blur.
 */
public class MotionBlurController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MotionBlurController.class);
    private static final MotionBlurController INSTANCE = new MotionBlurController();

    private IMotionBlurRendererAdapter adapter = null;

    private float previousYaw = 0.0f;
    private float previousPitch = 0.0f;
    private boolean hasPreviousSample = false;

    private float currentBlurAlpha = 0.0f;
    private float targetBlurAlpha = 0.0f;
    private long lastSampleTime = 0L;
    private boolean fallbackEngaged = false;

    private MotionBlurController() {}

    public static MotionBlurController getInstance() {
        return INSTANCE;
    }

    public synchronized void setAdapter(IMotionBlurRendererAdapter adapter) {
        this.adapter = adapter;
        this.fallbackEngaged = false;
    }

    public synchronized IMotionBlurRendererAdapter getAdapter() {
        return adapter;
    }

    public synchronized boolean isFallbackEngaged() {
        return fallbackEngaged;
    }

    public synchronized float getCurrentBlurAlpha() {
        return currentBlurAlpha;
    }

    /**
     * Updates camera rotation samples and computes required motion blur factor.
     *
     * @param yaw   current camera yaw angle in degrees
     * @param pitch current camera pitch angle in degrees
     */
    public synchronized void updateCameraRotation(float yaw, float pitch) {
        MotionBlurModule mod = MotionBlurModule.getInstance();
        if (mod == null || !mod.isEnabled()) {
            this.currentBlurAlpha = 0.0f;
            this.targetBlurAlpha = 0.0f;
            this.hasPreviousSample = false;
            return;
        }

        long now = System.currentTimeMillis();
        long deltaMs = (lastSampleTime > 0 && now > lastSampleTime) ? (now - lastSampleTime) : 16L;
        this.lastSampleTime = now;

        if (!hasPreviousSample) {
            this.previousYaw = yaw;
            this.previousPitch = pitch;
            this.hasPreviousSample = true;
            this.targetBlurAlpha = 0.0f;
            return;
        }

        // Calculate angular deltas with 360-degree wrapping
        float deltaYaw = Math.abs(wrapDegrees(yaw - previousYaw));
        float deltaPitch = Math.abs(pitch - previousPitch);
        this.previousYaw = yaw;
        this.previousPitch = pitch;

        double threshold = mod.getCameraMovementThresholdSetting().getValue();
        if (deltaYaw < threshold && deltaPitch < threshold) {
            // Decay blur when camera stops or moves below threshold
            double decay = mod.getBlurDecaySetting().getValue();
            this.targetBlurAlpha *= (float) (1.0 - decay * Math.min(1.0, deltaMs / 50.0));
            if (this.targetBlurAlpha < 0.01f) {
                this.targetBlurAlpha = 0.0f;
            }
        } else {
            double hSens = mod.getHorizontalSensitivitySetting().getValue();
            double vSens = mod.getVerticalSensitivitySetting().getValue();
            double intensity = mod.getBlurIntensitySetting().getValue();
            double strength = mod.getBlurStrengthSetting().getValue();
            double maxBlur = mod.getMaxBlurSetting().getValue();

            double motionScore = (deltaYaw * hSens + deltaPitch * vSens) * 0.05;
            double calculated = motionScore * intensity * strength;

            this.targetBlurAlpha = (float) Math.max(0.0, Math.min(maxBlur, calculated));
        }

        // Smooth interpolation
        if (mod.getBlurSmoothingSetting().getValue()) {
            float lerpSpeed = Math.min(1.0f, (float) (deltaMs / 30.0f));
            this.currentBlurAlpha += (targetBlurAlpha - currentBlurAlpha) * lerpSpeed;
        } else {
            this.currentBlurAlpha = targetBlurAlpha;
        }
    }

    /**
     * Executes the rendering pass if adapter is available and active.
     */
    public synchronized void renderBlurPass() {
        MotionBlurModule mod = MotionBlurModule.getInstance();
        if (mod == null || !mod.isEnabled() || currentBlurAlpha < 0.005f) {
            return;
        }

        if (adapter == null || !adapter.isSupported()) {
            if (!fallbackEngaged) {
                LOGGER.warn("[MotionBlur] Renderer adapter unsupported or null; engaging safe fallback.");
                fallbackEngaged = true;
            }
            return;
        }

        try {
            adapter.renderBlur(currentBlurAlpha);
        } catch (Exception e) {
            LOGGER.error("[MotionBlur] Render pass failed: {}", e.getMessage());
            this.fallbackEngaged = true;
            if (adapter != null) {
                adapter.clearHistory();
            }
        }
    }

    /**
     * Called on window resolution or GUI scale changes.
     */
    public synchronized void onResolutionChanged(int width, int height) {
        if (adapter != null) {
            adapter.onResolutionChanged(width, height);
            adapter.clearHistory();
        }
        this.currentBlurAlpha = 0.0f;
        this.targetBlurAlpha = 0.0f;
        this.hasPreviousSample = false;
    }

    /**
     * Called on world/dimension change or disconnect.
     */
    public synchronized void onWorldChanged() {
        if (adapter != null) {
            adapter.clearHistory();
        }
        this.currentBlurAlpha = 0.0f;
        this.targetBlurAlpha = 0.0f;
        this.hasPreviousSample = false;
    }

    public synchronized void cleanup() {
        if (adapter != null) {
            adapter.cleanup();
        }
        this.currentBlurAlpha = 0.0f;
        this.targetBlurAlpha = 0.0f;
        this.hasPreviousSample = false;
    }

    private static float wrapDegrees(float value) {
        float wrapped = value % 360.0f;
        if (wrapped >= 180.0f) wrapped -= 360.0f;
        if (wrapped < -180.0f) wrapped += 360.0f;
        return wrapped;
    }

    public synchronized void resetForTesting() {
        this.previousYaw = 0.0f;
        this.previousPitch = 0.0f;
        this.hasPreviousSample = false;
        this.currentBlurAlpha = 0.0f;
        this.targetBlurAlpha = 0.0f;
        this.fallbackEngaged = false;
    }
}
