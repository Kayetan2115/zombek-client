package pl.zombek.client.core.modules.visual.oneseven;

/**
 * Controls visual hand and item swing animation progression and matrix calculations.
 * Purely client-side visual effect; does not alter attack cooldown, reach, or attack rates.
 */
public class SwingAnimationController {
    private static SwingAnimationController instance;

    public static synchronized SwingAnimationController getInstance() {
        if (instance == null) {
            instance = new SwingAnimationController();
        }
        return instance;
    }

    /**
     * Re-maps swing progress according to 1.7/1.8 duration and style.
     */
    public float resolveSwingProgress(float rawSwingProgress, AnimationSettings settings) {
        if (!settings.isEnableOldSwing() || rawSwingProgress <= 0.001f || rawSwingProgress >= 0.999f) {
            return rawSwingProgress;
        }

        float speed = settings.getSwingSpeed();
        float scaled = rawSwingProgress * speed;
        return Math.min(1.0f, Math.max(0.0f, scaled));
    }

    /**
     * Calculates the 3D swing rotation and position transform.
     */
    public TransformOffset calculateSwingTransform(float swingProgress, AnimationSettings settings, boolean isMainHand) {
        TransformOffset transform = new TransformOffset();
        if (!settings.isEnableOldSwing() || swingProgress <= 0.001f) {
            return transform;
        }

        float sign = isMainHand ? 1.0f : -1.0f;
        float intensity = settings.getSwingIntensity();
        float angleMultiplier = settings.getSwingAngle();

        float f = (float) Math.sin(swingProgress * swingProgress * Math.PI);
        float f1 = (float) Math.sin(Math.sqrt(swingProgress) * Math.PI);
        float f2 = (float) Math.sin(Math.sqrt(swingProgress) * Math.PI * 2.0f);

        switch (settings.getSwingStyle()) {
            case VANILLA_1_8 -> {
                transform.setRotX(f2 * 18.0f * angleMultiplier * intensity);
                transform.setRotZ(sign * -f * 18.0f * angleMultiplier * intensity);
                transform.setRotY(sign * -f1 * 70.0f * angleMultiplier * intensity);
                transform.setPosX(sign * -f1 * 0.18f * intensity);
                transform.setPosY(f * 0.12f * intensity);
                transform.setPosZ(-f1 * 0.18f * intensity);
            }
            case MODERN -> {
                transform.setRotX(f2 * 20.0f * angleMultiplier * intensity);
                transform.setRotZ(sign * -f * 20.0f * angleMultiplier * intensity);
                transform.setRotY(sign * -f1 * 80.0f * angleMultiplier * intensity);
            }
            case CUSTOM -> {
                transform.setRotX(f2 * 25.0f * angleMultiplier * intensity);
                transform.setRotZ(sign * -f * 25.0f * angleMultiplier * intensity);
                transform.setRotY(sign * -f1 * 85.0f * angleMultiplier * intensity);
                transform.setPosX(sign * -f1 * 0.22f * intensity);
                transform.setPosY(f * 0.15f * intensity);
                transform.setPosZ(-f1 * 0.22f * intensity);
            }
            case VANILLA_1_7 -> {
                // Classic wide and crisp 1.7 swing arc
                transform.setRotX(f2 * 22.0f * angleMultiplier * intensity);
                transform.setRotZ(sign * -f * 22.0f * angleMultiplier * intensity);
                transform.setRotY(sign * -f1 * 80.0f * angleMultiplier * intensity);
                transform.setPosX(sign * -f1 * 0.2f * intensity);
                transform.setPosY(f * 0.14f * intensity);
                transform.setPosZ(-f1 * 0.2f * intensity);
            }
        }

        return transform;
    }
}
