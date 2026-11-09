package pl.zombek.client.core.itemtracker;

/**
 * Categories of item inventory change events.
 */
public enum ItemEventType {
    COLLECTED("Collected", "+", 0xFF55FF55),
    USED("Used", "-", 0xFF55FFFF),
    CONSUMED("Consumed", "-", 0xFFFFAA00),
    DROPPED("Dropped", "-", 0xFFFF5555),
    CRAFTED("Crafted", "+", 0xFFFFFF55),
    PLACED("Placed", "-", 0xFFFF88FF),
    PICKED_UP("Picked Up", "+", 0xFF55FF55),
    TRANSFERRED("Transferred", "~", 0xFFAAAAAA),
    UNKNOWN_CHANGE("Unknown Change", "?", 0xFFCCCCCC);

    private final String displayName;
    private final String prefixSymbol;
    private final int defaultColor;

    ItemEventType(String displayName, String prefixSymbol, int defaultColor) {
        this.displayName = displayName;
        this.prefixSymbol = prefixSymbol;
        this.defaultColor = defaultColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPrefixSymbol() {
        return prefixSymbol;
    }

    public int getDefaultColor() {
        return defaultColor;
    }
}
