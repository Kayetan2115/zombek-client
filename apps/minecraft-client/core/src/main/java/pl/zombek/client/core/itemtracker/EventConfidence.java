package pl.zombek.client.core.itemtracker;

/**
 * Level of confidence regarding the determined source of an item quantity change.
 * In accordance with ZombekClient Fair Play, UNKNOWN/INFERRED/OBSERVED events are NEVER
 * represented as CONFIRMED.
 */
public enum EventConfidence {
    CONFIRMED("Confirmed", 0xFF55FF55),
    OBSERVED("Observed", 0xFF55FFFF),
    INFERRED("Inferred", 0xFFFFAA00),
    UNKNOWN("Unknown", 0xFFAAAAAA);

    private final String label;
    private final int color;

    EventConfidence(String label, int color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public int getColor() {
        return color;
    }
}
