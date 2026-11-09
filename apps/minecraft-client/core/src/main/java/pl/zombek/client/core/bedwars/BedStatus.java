package pl.zombek.client.core.bedwars;

/**
 * Status of a team's bed in a Bedwars match.
 */
public enum BedStatus {
    BED_ALIVE("Łóżko całe", "✓", 0xFF55FF55),
    BED_DESTROYED("Łóżko zniszczone", "✕", 0xFFFF5555),
    TEAM_ELIMINATED("Drużyna wyeliminowana", "E", 0xFFAAAAAA),
    UNKNOWN("Nieznany", "?", 0xFFFFFF55);

    private final String displayName;
    private final String symbol;
    private final int defaultColor;

    BedStatus(String displayName, String symbol, int defaultColor) {
        this.displayName = displayName;
        this.symbol = symbol;
        this.defaultColor = defaultColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getDefaultColor() {
        return defaultColor;
    }

    public boolean isBedIntact() {
        return this == BED_ALIVE;
    }

    public boolean isEliminated() {
        return this == TEAM_ELIMINATED;
    }
}
