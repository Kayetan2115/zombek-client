package pl.zombek.client.core.modules.visual.oneseven;

/**
 * Handles third-person player model arm poses and model posture adjustments.
 * Purely client-side presentation; zero modifications to bounding boxes or server packets.
 */
public class ThirdPersonAnimationRenderer {
    private static ThirdPersonAnimationRenderer instance;

    public static synchronized ThirdPersonAnimationRenderer getInstance() {
        if (instance == null) {
            instance = new ThirdPersonAnimationRenderer();
        }
        return instance;
    }

    /**
     * Checks if third person blocking arm pose should be rendered.
     */
    public boolean shouldRenderThirdPersonBlocking(boolean isHoldingSword, boolean isBlocking, AnimationSettings settings) {
        if (!settings.isEnableOldBlocking() || !settings.isThirdPersonBlocking()) {
            return false;
        }
        return isHoldingSword && isBlocking;
    }

    /**
     * Checks if third person classic sneak model pose should be rendered.
     */
    public boolean shouldRenderClassicSneakPose(boolean isSneaking, AnimationSettings settings) {
        if (!settings.isEnableOldSneak() || !settings.isSneakThirdPerson() || !settings.isSneakModelPose()) {
            return false;
        }
        return isSneaking;
    }

    /**
     * Calculates the right arm pitch adjustment for third-person sword blocking.
     */
    public float getBlockingArmPitch(float defaultPitch) {
        // Classic 1.7 / 1.8 arm blocking pose: arm angled inward across chest
        return -0.8f;
    }

    /**
     * Calculates the right arm yaw adjustment for third-person sword blocking.
     */
    public float getBlockingArmYaw(float defaultYaw) {
        return -0.5f;
    }
}
