package pl.zombek.client.core.tracking;

import pl.zombek.client.core.data.TargetEntityType;
import pl.zombek.client.core.modules.visual.FadeCurve;

/**
 * State snapshot of a single entity undergoing hit-color damage tint.
 */
public class HitColorRecord {
    private final int entityId;
    private final long hitTimestampMs;
    private final long durationMs;
    private final long fadeDurationMs;
    private final int colorArgb;
    private final FadeCurve curve;
    private final float intensity;
    private final float opacity;
    private final TargetEntityType entityType;
    private final boolean isLocalPlayerDamage;
    private final boolean isTargeted;

    public HitColorRecord(int entityId,
                          long hitTimestampMs,
                          long durationMs,
                          long fadeDurationMs,
                          int colorArgb,
                          FadeCurve curve,
                          float intensity,
                          float opacity,
                          TargetEntityType entityType,
                          boolean isLocalPlayerDamage,
                          boolean isTargeted) {
        this.entityId = entityId;
        this.hitTimestampMs = hitTimestampMs;
        this.durationMs = Math.max(10, durationMs);
        this.fadeDurationMs = Math.max(0, fadeDurationMs);
        this.colorArgb = colorArgb;
        this.curve = curve != null ? curve : FadeCurve.LINEAR;
        this.intensity = Math.min(1.0f, Math.max(0.0f, intensity));
        this.opacity = Math.min(1.0f, Math.max(0.0f, opacity));
        this.entityType = entityType != null ? entityType : TargetEntityType.OTHER;
        this.isLocalPlayerDamage = isLocalPlayerDamage;
        this.isTargeted = isTargeted;
    }

    public boolean isExpired(long now) {
        return now >= (hitTimestampMs + durationMs + fadeDurationMs);
    }

    /**
     * Calculates the active ARGB color at the given point in time.
     * Returns null if the effect has fully expired.
     */
    public Integer computeColor(long now) {
        if (isExpired(now)) {
            return null;
        }

        long elapsed = now - hitTimestampMs;
        float alphaFactor;

        if (elapsed <= durationMs) {
            alphaFactor = intensity * opacity;
        } else {
            long fadeElapsed = elapsed - durationMs;
            float progress = (float) fadeElapsed / (float) fadeDurationMs;
            alphaFactor = curve.calculateFactor(progress) * intensity * opacity;
        }

        int baseA = (colorArgb >> 24) & 0xFF;
        int newA = Math.round(baseA * Math.min(1.0f, Math.max(0.0f, alphaFactor)));
        if (newA <= 0) {
            return null;
        }

        return (newA << 24) | (colorArgb & 0x00FFFFFF);
    }

    public int getEntityId() {
        return entityId;
    }

    public TargetEntityType getEntityType() {
        return entityType;
    }

    public boolean isLocalPlayerDamage() {
        return isLocalPlayerDamage;
    }

    public boolean isTargeted() {
        return isTargeted;
    }

    public long getHitTimestampMs() {
        return hitTimestampMs;
    }
}
