package pl.zombek.client.core.worldvisual.time;

/**
 * Manages smooth visual time transitions, preventing celestial sun/moon jumps.
 */
public class TimeTransitionManager {
    private boolean smoothTransition = true;
    private long transitionDurationMs = 500L;
    private double currentVisualTime = 6000.0;
    private double targetVisualTime = 6000.0;
    private double startVisualTime = 6000.0;
    private long animationStartTime = 0L;

    public TimeTransitionManager() {}

    public void configure(boolean smoothTransition, long transitionDurationMs) {
        this.smoothTransition = smoothTransition;
        this.transitionDurationMs = Math.max(50L, Math.min(5000L, transitionDurationMs));
    }

    public void setTargetTime(long targetTicks, boolean instant) {
        long normalized = Math.floorMod(targetTicks, 24000L);
        this.targetVisualTime = normalized;

        if (instant || !smoothTransition) {
            this.currentVisualTime = normalized;
            this.startVisualTime = normalized;
            this.animationStartTime = 0L;
        } else {
            this.animationStartTime = System.currentTimeMillis();
            this.startVisualTime = this.currentVisualTime;
        }
    }

    public void update() {
        if (!smoothTransition || currentVisualTime == targetVisualTime || animationStartTime <= 0) {
            return;
        }

        long elapsed = System.currentTimeMillis() - animationStartTime;
        if (elapsed >= transitionDurationMs) {
            currentVisualTime = targetVisualTime;
            return;
        }

        float progress = Math.min(1.0f, Math.max(0.0f, (float) elapsed / (float) transitionDurationMs));
        // Shortest circular path on 24000 ticks cycle
        double diff = targetVisualTime - startVisualTime;
        if (diff > 12000.0) {
            diff -= 24000.0;
        } else if (diff < -12000.0) {
            diff += 24000.0;
        }

        float eased = progress < 0.5f ? 2.0f * progress * progress : 1.0f - (float) Math.pow(-2.0f * progress + 2.0f, 2.0) / 2.0f;
        double interpolated = startVisualTime + diff * eased;
        currentVisualTime = Math.floorMod(Math.round(interpolated), 24000L);
    }

    public long getCurrentVisualTicks() {
        return Math.round(currentVisualTime);
    }

    public long getTargetVisualTicks() {
        return Math.round(targetVisualTime);
    }

    public void reset() {
        currentVisualTime = 6000.0;
        targetVisualTime = 6000.0;
        startVisualTime = 6000.0;
        animationStartTime = 0L;
    }
}
