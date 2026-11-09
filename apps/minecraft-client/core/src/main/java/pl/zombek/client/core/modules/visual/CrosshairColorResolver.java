package pl.zombek.client.core.modules.visual;

import pl.zombek.client.core.data.CrosshairTargetType;

/**
 * Handles crosshair color resolution and smooth transitions between targeting states.
 */
public class CrosshairColorResolver {
    private int currentColor;
    private int previousColor;
    private int targetColor;
    private long transitionStartMs;
    private long transitionDurationMs;

    public CrosshairColorResolver(int initialColor) {
        this.currentColor = initialColor;
        this.previousColor = initialColor;
        this.targetColor = initialColor;
        this.transitionStartMs = 0;
        this.transitionDurationMs = 150;
    }

    public int resolveColor(CrosshairTargetType targetType,
                            float attackCooldown,
                            boolean dynamicColor,
                            boolean targetDetection,
                            boolean attackReadyColor,
                            boolean animate,
                            long animationDurationMs,
                            int mainColor,
                            int targetColorParam,
                            int blockColor,
                            int entityColor,
                            long now) {
        int goalColor = mainColor;

        if (dynamicColor) {
            if (targetDetection) {
                if (targetType == CrosshairTargetType.ENTITY) {
                    goalColor = entityColor;
                } else if (targetType == CrosshairTargetType.BLOCK) {
                    goalColor = blockColor;
                } else if (attackReadyColor && attackCooldown < 1.0f) {
                    // Blend main color towards targetColorParam based on cooldown progress
                    goalColor = interpolateColor(targetColorParam, mainColor, attackCooldown);
                }
            } else if (attackReadyColor && attackCooldown < 1.0f) {
                goalColor = interpolateColor(targetColorParam, mainColor, attackCooldown);
            }
        }

        if (!animate || animationDurationMs <= 0) {
            this.currentColor = goalColor;
            this.previousColor = goalColor;
            this.targetColor = goalColor;
            return goalColor;
        }

        if (goalColor != this.targetColor) {
            this.previousColor = this.currentColor;
            this.targetColor = goalColor;
            this.transitionStartMs = now;
            this.transitionDurationMs = animationDurationMs;
        }

        if (this.currentColor == this.targetColor) {
            return this.currentColor;
        }

        long elapsed = now - this.transitionStartMs;
        if (elapsed >= this.transitionDurationMs) {
            this.currentColor = this.targetColor;
        } else {
            float progress = Math.min(1.0f, Math.max(0.0f, (float) elapsed / this.transitionDurationMs));
            this.currentColor = interpolateColor(this.previousColor, this.targetColor, progress);
        }

        return this.currentColor;
    }

    public static int interpolateColor(int colorA, int colorB, float t) {
        float clampedT = Math.min(1.0f, Math.max(0.0f, t));
        int a1 = (colorA >> 24) & 0xFF;
        int r1 = (colorA >> 16) & 0xFF;
        int g1 = (colorA >> 8) & 0xFF;
        int b1 = colorA & 0xFF;

        int a2 = (colorB >> 24) & 0xFF;
        int r2 = (colorB >> 16) & 0xFF;
        int g2 = (colorB >> 8) & 0xFF;
        int b2 = colorB & 0xFF;

        int a = (int) (a1 + (a2 - a1) * clampedT);
        int r = (int) (r1 + (r2 - r1) * clampedT);
        int g = (int) (g1 + (g2 - g1) * clampedT);
        int b = (int) (b1 + (b2 - b1) * clampedT);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public int getCurrentColor() {
        return currentColor;
    }
}
