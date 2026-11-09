package pl.zombek.client.core.bedwars;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Immutable representation of a game event in Bedwars.
 */
public class BedwarsEvent {
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss", Locale.ROOT);

    private final String id;
    private final BedwarsEventType type;
    private final String title;
    private final String description;
    private final int color;
    private final long timestampMs;
    private final String icon;

    public BedwarsEvent(String id, BedwarsEventType type, String title, String description, int color, long timestampMs, String icon) {
        this.id = id != null ? id : (type.name() + ":" + timestampMs);
        this.type = type;
        this.title = title != null ? title : type.getDisplayName();
        this.description = description != null ? description : "";
        this.color = color;
        this.timestampMs = timestampMs;
        this.icon = icon != null ? icon : type.getDefaultIcon();
    }

    public BedwarsEvent(BedwarsEventType type, String title, String description, int color) {
        this(null, type, title, description, color, System.currentTimeMillis(), type.getDefaultIcon());
    }

    public String getId() {
        return id;
    }

    public BedwarsEventType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getColor() {
        return color;
    }

    public long getTimestampMs() {
        return timestampMs;
    }

    public String getIcon() {
        return icon;
    }

    public String getFormattedTimestamp() {
        return TIME_FORMAT.format(new Date(timestampMs));
    }

    public long getAgeMs() {
        return Math.max(0, System.currentTimeMillis() - timestampMs);
    }

    /**
     * Calculates the transparency alpha multiplier for fade animations.
     * @param durationSeconds total display duration in seconds
     * @return alpha between 0.0f (fully faded) and 1.0f (fully visible)
     */
    public float getFadeAlpha(float durationSeconds) {
        float totalMs = durationSeconds * 1000.0f;
        long age = getAgeMs();
        if (age >= totalMs) {
            return 0.0f;
        }
        float fadeStartMs = totalMs * 0.75f;
        if (age < fadeStartMs) {
            return 1.0f;
        }
        return Math.max(0.0f, Math.min(1.0f, 1.0f - ((age - fadeStartMs) / (totalMs - fadeStartMs))));
    }
}
