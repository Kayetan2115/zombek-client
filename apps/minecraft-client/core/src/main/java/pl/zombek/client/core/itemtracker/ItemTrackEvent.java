package pl.zombek.client.core.itemtracker;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Immutable record of a single item tracking event during a gameplay session.
 */
public final class ItemTrackEvent {

    private final long timestampEpoch;
    private final String itemId;
    private final String itemName;
    private final int delta;
    private final ItemEventType eventType;
    private final EventConfidence confidence;
    private final String description;

    public ItemTrackEvent(long timestampEpoch,
                          String itemId,
                          String itemName,
                          int delta,
                          ItemEventType eventType,
                          EventConfidence confidence,
                          String description) {
        this.timestampEpoch = timestampEpoch;
        this.itemId = itemId != null ? itemId : "minecraft:air";
        this.itemName = itemName != null ? itemName : this.itemId;
        this.delta = delta;
        this.eventType = eventType != null ? eventType : ItemEventType.UNKNOWN_CHANGE;
        this.confidence = confidence != null ? confidence : EventConfidence.UNKNOWN;
        this.description = description != null ? description : buildDefaultDescription();
    }

    private String buildDefaultDescription() {
        String sign = delta > 0 ? "+" + delta : String.valueOf(delta);
        return sign + " " + itemName + " — " + eventType.getDisplayName();
    }

    public long getTimestampEpoch() {
        return timestampEpoch;
    }

    public String getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public int getDelta() {
        return delta;
    }

    public ItemEventType getEventType() {
        return eventType;
    }

    public EventConfidence getConfidence() {
        return confidence;
    }

    public String getDescription() {
        return description;
    }

    public String getFormattedTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date(timestampEpoch));
    }

    public String getFeedDisplayLine(boolean showTimestamp, boolean showConfidence) {
        StringBuilder sb = new StringBuilder();
        if (showTimestamp) {
            sb.append("[").append(getFormattedTimestamp()).append("] ");
        }
        sb.append(description);
        if (showConfidence) {
            sb.append(" (").append(confidence.getLabel()).append(")");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return getFeedDisplayLine(true, true);
    }
}
