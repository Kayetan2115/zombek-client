package pl.zombek.client.core.modules.visual.oneseven;

/**
 * Controls visual sneak camera eye height transitions and third-person model pose offsets.
 * Purely client-side presentation; zero modifications to player hitbox, bounding box, or server packets.
 * Fully compatible with ToggleSneakModule.
 */
public class SneakAnimationController {
    private static SneakAnimationController instance;

    // Range: 0.0f (standing) to 1.0f (fully sneaked)
    private float currentProgress = 0.0f;
    private float previousProgress = 0.0f;

    public static synchronized SneakAnimationController getInstance() {
        if (instance == null) {
            instance = new SneakAnimationController();
        }
        return instance;
    }

    public void update(boolean isSneaking, AnimationSettings settings) {
        if (!settings.isEnableOldSneak()) {
            currentProgress = isSneaking ? 1.0f : 0.0f;
            previousProgress = currentProgress;
            return;
        }

        previousProgress = currentProgress;
        float speed = Math.max(0.1f, settings.getSneakTransitionSpeed());

        if (isSneaking) {
            switch (settings.getSneakTransition()) {
                case FULL_INSTANT, INSTANT_DOWN_SMOOTH_UP -> {
                    // Instant camera drop (authentic 1.7)
                    currentProgress = 1.0f;
                }
                case SMOOTH_1_7 -> {
                    currentProgress = Math.min(1.0f, currentProgress + 0.35f * speed);
                }
                case MODERN -> {
                    currentProgress = Math.min(1.0f, currentProgress + 0.15f * speed);
                }
            }
        } else {
            switch (settings.getSneakTransition()) {
                case FULL_INSTANT -> {
                    currentProgress = 0.0f;
                }
                case INSTANT_DOWN_SMOOTH_UP, SMOOTH_1_7, MODERN -> {
                    // Smooth return upward
                    currentProgress = Math.max(0.0f, currentProgress - 0.2f * speed);
                }
            }
        }
    }

    public float getInterpolatedProgress(float partialTicks) {
        return previousProgress + (currentProgress - previousProgress) * partialTicks;
    }

    /**
     * Calculates the vertical camera eye-height offset delta (in meters) to apply to Camera.
     * Standing eye height in MC is ~1.62m, sneaking is ~1.27m (delta = -0.35m).
     */
    public float calculateEyeHeightDelta(boolean isSneaking, float partialTicks, AnimationSettings settings) {
        if (!settings.isEnableOldSneak() || !settings.isSneakCameraAnimation()) {
            return 0.0f;
        }

        float progress = getInterpolatedProgress(partialTicks);
        // Modern MC already moves the camera when sneaking; this delta tunes the curve to be instant/faster
        if (settings.isInstantCameraDown() && isSneaking) {
            // Apply instant drop offset compensation
            return -0.08f * (1.0f - progress);
        }
        return 0.0f;
    }

    public boolean shouldApplyModelPose(AnimationSettings settings) {
        return settings.isEnableOldSneak() && settings.isSneakModelPose();
    }

    public void reset() {
        this.currentProgress = 0.0f;
        this.previousProgress = 0.0f;
    }
}
