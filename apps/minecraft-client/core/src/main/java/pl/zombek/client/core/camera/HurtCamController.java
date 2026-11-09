package pl.zombek.client.core.camera;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.zombek.client.core.modules.visual.HurtCamModule;

/**
 * Controller adjusting local damage tilt and camera shake effects.
 * Strictly local client presentation; zero impact on true damage, knockback, or server health.
 */
public class HurtCamController {
    private static final Logger LOGGER = LoggerFactory.getLogger(HurtCamController.class);
    private static final CameraFovController INSTANCE_FOV = CameraFovController.getInstance();
    private static final HurtCamController INSTANCE = new HurtCamController();

    private long lastDamageTime = 0L;

    private HurtCamController() {}

    public static HurtCamController getInstance() {
        return INSTANCE;
    }

    /**
     * Called whenever local player takes damage.
     */
    public synchronized void onDamageTaken() {
        this.lastDamageTime = System.currentTimeMillis();
    }

    /**
     * Checks if the hurt cam tilt/shake should be completely skipped.
     */
    public synchronized boolean shouldCancelHurtCam() {
        HurtCamModule mod = HurtCamModule.getInstance();
        if (mod == null || !mod.isEnabled()) return false;
        return mod.getModeSetting().getValue() == HurtCamMode.DISABLED;
    }

    /**
     * Returns the calculated tilt multiplier to apply to the camera roll matrix.
     */
    public synchronized float getHurtTiltMultiplier() {
        HurtCamModule mod = HurtCamModule.getInstance();
        if (mod == null || !mod.isEnabled()) return 1.0f;

        HurtCamMode mode = mod.getModeSetting().getValue();
        float baseMultiplier = switch (mode) {
            case VANILLA -> 1.0f;
            case DISABLED -> 0.0f;
            case REDUCED -> 0.5f;
            case CUSTOM -> (float) (mod.getCustomIntensitySetting().getValue() * mod.getCameraTiltSetting().getValue());
        };

        if (baseMultiplier <= 0.0f) return 0.0f;

        // Apply smooth recovery easing if enabled
        if (mod.getSmoothRecoverySetting().getValue() && lastDamageTime > 0L) {
            long elapsed = System.currentTimeMillis() - lastDamageTime;
            double duration = mod.getRecoveryDurationSetting().getValue();
            if (duration > 0.0) {
                if (elapsed >= duration) {
                    return 0.0f;
                }
                double progress = (double) elapsed / duration;
                HurtCamEasing easing = mod.getEasingSetting().getValue();
                double eased = easing != null ? easing.ease(progress) : progress;
                // Eased factor decays smoothly from 1.0 down to 0.0
                float decay = (float) Math.max(0.0, 1.0 - Math.min(1.0, eased));
                return baseMultiplier * decay;
            }
        }

        return baseMultiplier;
    }

    /**
     * Returns the calculated shake multiplier.
     */
    public synchronized float getHurtShakeMultiplier() {
        HurtCamModule mod = HurtCamModule.getInstance();
        if (mod == null || !mod.isEnabled()) return 1.0f;

        HurtCamMode mode = mod.getModeSetting().getValue();
        return switch (mode) {
            case VANILLA -> 1.0f;
            case DISABLED -> 0.0f;
            case REDUCED -> 0.5f;
            case CUSTOM -> (float) (mod.getCustomIntensitySetting().getValue() * mod.getCameraShakeSetting().getValue());
        };
    }

    public synchronized void resetForTesting() {
        this.lastDamageTime = 0L;
    }
}
