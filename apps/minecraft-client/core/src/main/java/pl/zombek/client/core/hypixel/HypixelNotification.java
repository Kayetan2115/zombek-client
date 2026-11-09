package pl.zombek.client.core.hypixel;

import java.util.UUID;

/**
 * Individual HUD notification instance with fade lifecycle calculations.
 */
public class HypixelNotification {
    private final String id;
    private final HypixelNotificationType type;
    private final String title;
    private final String message;
    private final int accentColor;
    private final float durationSeconds;
    private final long createdAt;

    public HypixelNotification(HypixelNotificationType type, String title, String message, int accentColor, float durationSeconds) {
        this(UUID.randomUUID().toString(), type, title, message, accentColor, durationSeconds);
    }

    public HypixelNotification(String id, HypixelNotificationType type, String title, String message, int accentColor, float durationSeconds) {
        this.id = id;
        this.type = type != null ? type : HypixelNotificationType.MODULE_STATUS;
        this.title = title != null ? title : "";
        this.message = message != null ? message : "";
        this.accentColor = accentColor;
        this.durationSeconds = durationSeconds;
        this.createdAt = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public HypixelNotificationType getType() { return type; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public int getAccentColor() { return accentColor; }
    public float getDurationSeconds() { return durationSeconds; }
    public long getCreatedAt() { return createdAt; }

    public boolean isExpired() {
        return (System.currentTimeMillis() - createdAt) > (long) (durationSeconds * 1000.0f);
    }

    /**
     * Calculates alpha opacity for fade-in / fade-out animations (0.0 to 1.0).
     */
    public float getAlpha() {
        long elapsed = System.currentTimeMillis() - createdAt;
        float totalMs = durationSeconds * 1000.0f;
        if (elapsed > totalMs) return 0.0f;

        // Fade in: first 200ms
        if (elapsed <= 0L) {
            return 0.05f;
        }
        if (elapsed < 200L) {
            return Math.max(0.05f, elapsed / 200.0f);
        }

        // Fade out: last 500ms
        float remaining = totalMs - elapsed;
        if (remaining < 500.0f) {
            return remaining / 500.0f;
        }

        return 1.0f;
    }
}
