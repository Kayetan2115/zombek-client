package pl.zombek.client.core.modules.visual.oneseven;

/**
 * Evaluates visual sword blocking and blockhit animation states and transformations.
 * Purely visual; zero combat mechanics, zero server packets, zero damage reduction changes.
 */
public class BlockingAnimationController {
    private static BlockingAnimationController instance;

    private boolean blockingActive = false;
    private float currentProgress = 0.0f;
    private float previousProgress = 0.0f;

    public static synchronized BlockingAnimationController getInstance() {
        if (instance == null) {
            instance = new BlockingAnimationController();
        }
        return instance;
    }

    public void update(boolean isHoldingSword, boolean isUsingItem, boolean isRightMouseDown, AnimationSettings settings) {
        if (!settings.isEnableOldBlocking() || !isHoldingSword) {
            blockingActive = false;
        } else {
            // Player is blocking if using item (e.g. food/shield) or right-clicking with sword
            blockingActive = isUsingItem || isRightMouseDown;
        }

        previousProgress = currentProgress;
        float speed = Math.max(0.1f, settings.getBlockingSpeed()) * 0.25f;
        if (blockingActive) {
            currentProgress = Math.min(1.0f, currentProgress + speed);
        } else {
            currentProgress = Math.max(0.0f, currentProgress - speed);
        }
    }

    public float getInterpolatedProgress(float partialTicks) {
        return previousProgress + (currentProgress - previousProgress) * partialTicks;
    }

    public boolean isBlockingActive() {
        return blockingActive || currentProgress > 0.01f;
    }

    /**
     * Calculates the 3D matrix offset to apply for sword blocking.
     */
    public TransformOffset calculateBlockingTransform(float partialTicks, AnimationSettings settings, boolean isMainHand) {
        TransformOffset transform = new TransformOffset();
        float progress = getInterpolatedProgress(partialTicks);
        if (progress <= 0.001f) {
            return transform;
        }

        float sign = isMainHand ? 1.0f : -1.0f;

        switch (settings.getBlockingStyle()) {
            case VANILLA_1_8 -> {
                transform.setPosX(sign * -0.15f * progress);
                transform.setPosY(0.14f * progress);
                transform.setPosZ(-0.18f * progress);
                transform.setRotX(-98.0f * progress);
                transform.setRotY(sign * -55.0f * progress);
                transform.setRotZ(sign * 74.0f * progress);
            }
            case LUNAR -> {
                transform.setPosX(sign * -0.16f * progress);
                transform.setPosY(0.16f * progress);
                transform.setPosZ(-0.22f * progress);
                transform.setRotX(-105.0f * progress);
                transform.setRotY(sign * -62.0f * progress);
                transform.setRotZ(sign * 82.0f * progress);
            }
            case CUSTOM -> {
                transform.setPosX(sign * settings.getBlockingPosX() * progress);
                transform.setPosY(settings.getBlockingPosY() * progress);
                transform.setPosZ(settings.getBlockingPosZ() * progress);
                transform.setRotX(settings.getBlockingRotX() * progress);
                transform.setRotY(sign * settings.getBlockingRotY() * progress);
                transform.setRotZ(sign * settings.getBlockingRotZ() * progress);
                transform.setScaleX(1.0f + (settings.getBlockingScale() - 1.0f) * progress);
                transform.setScaleY(1.0f + (settings.getBlockingScale() - 1.0f) * progress);
                transform.setScaleZ(1.0f + (settings.getBlockingScale() - 1.0f) * progress);
            }
            case VANILLA_1_7 -> {
                transform.setPosX(sign * -0.14f * progress);
                transform.setPosY(0.15f * progress);
                transform.setPosZ(-0.2f * progress);
                transform.setRotX(-102.0f * progress);
                transform.setRotY(sign * -59.0f * progress);
                transform.setRotZ(sign * 79.0f * progress);
            }
        }

        return transform;
    }

    /**
     * Calculates the blockhit swing offset to add when player swings while blocking.
     */
    public TransformOffset calculateBlockhitOffset(float swingProgress, AnimationSettings settings, boolean isMainHand) {
        TransformOffset offset = new TransformOffset();
        if (!settings.isEnableOldBlockhit() || swingProgress <= 0.001f || !isBlockingActive()) {
            return offset;
        }

        float sign = isMainHand ? 1.0f : -1.0f;
        float intensity = settings.getBlockhitIntensity();

        float f = (float) Math.sin(swingProgress * swingProgress * Math.PI);
        float f1 = (float) Math.sin(Math.sqrt(swingProgress) * Math.PI);

        switch (settings.getBlockhitStyle()) {
            case CLASSIC_1_8 -> {
                offset.setRotX(-f * 18.0f * intensity);
                offset.setRotZ(sign * -f1 * 18.0f * intensity);
                offset.setRotY(sign * -f1 * 70.0f * intensity);
                offset.setPosY(f * 0.05f * intensity);
            }
            case SMOOTH -> {
                float smoothF = (float) Math.sin(swingProgress * Math.PI);
                offset.setRotX(-smoothF * 22.0f * intensity);
                offset.setRotY(sign * -smoothF * 60.0f * intensity);
                offset.setRotZ(sign * -smoothF * 20.0f * intensity);
                offset.setPosZ(-smoothF * 0.08f * intensity);
            }
            case AGGRESSIVE -> {
                offset.setRotX(-f * 28.0f * intensity);
                offset.setRotZ(sign * -f1 * 26.0f * intensity);
                offset.setRotY(sign * -f1 * 95.0f * intensity);
                offset.setPosY(f * 0.08f * intensity);
            }
            case CUSTOM -> {
                float customAngle = settings.getBlockhitSwingAngle();
                offset.setRotX(-f * 20.0f * customAngle * intensity);
                offset.setRotZ(sign * -f1 * 20.0f * customAngle * intensity);
                offset.setRotY(sign * -f1 * 80.0f * customAngle * intensity + settings.getBlockhitRotation());
                offset.setPosX(sign * settings.getBlockhitOffsetX() * f);
                offset.setPosY(settings.getBlockhitOffsetY() * f);
                float sc = 1.0f + (settings.getBlockhitScale() - 1.0f) * f;
                offset.setScaleX(sc);
                offset.setScaleY(sc);
                offset.setScaleZ(sc);
            }
            case CLASSIC_1_7 -> {
                offset.setRotX(-f * 20.0f * intensity);
                offset.setRotZ(sign * -f1 * 20.0f * intensity);
                offset.setRotY(sign * -f1 * 80.0f * intensity);
            }
        }

        return offset;
    }

    public void reset() {
        this.blockingActive = false;
        this.currentProgress = 0.0f;
        this.previousProgress = 0.0f;
    }
}
