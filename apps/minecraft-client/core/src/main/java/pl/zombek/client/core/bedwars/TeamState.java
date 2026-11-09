package pl.zombek.client.core.bedwars;

/**
 * Overall state of a Bedwars team.
 */
public enum TeamState {
    ACTIVE("Aktywna", 0xFF55FF55),
    BEDLESS("Bez łóżka", 0xFFFF5555),
    ELIMINATED("Wyeliminowana", 0xFFAAAAAA),
    UNKNOWN("Nieznana", 0xFFFFFF55);

    private final String displayName;
    private final int color;

    TeamState(String displayName, int color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColor() {
        return color;
    }

    public boolean isPlaying() {
        return this == ACTIVE || this == BEDLESS;
    }
}
