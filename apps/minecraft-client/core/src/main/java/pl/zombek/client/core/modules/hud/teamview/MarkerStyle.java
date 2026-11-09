package pl.zombek.client.core.modules.hud.teamview;

/**
 * Visual marker shape drawn above teammate player models.
 */
public enum MarkerStyle {
    ARROW_DOWN("Strzałka w dół (▼)", "▼"),
    DIAMOND("Romb (◆)", "◆"),
    DOT("Kropka (●)", "●"),
    SHIELD("Tarcza (🛡)", "🛡"),
    CHEVRON("Krokiew (▲)", "▲");

    private final String displayName;
    private final String symbol;

    MarkerStyle(String displayName, String symbol) {
        this.displayName = displayName;
        this.symbol = symbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
