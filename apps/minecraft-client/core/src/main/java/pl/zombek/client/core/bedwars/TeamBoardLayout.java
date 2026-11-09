package pl.zombek.client.core.bedwars;

/**
 * Layout formats for the Team Board sub-panel.
 */
public enum TeamBoardLayout {
    COMPACT("Kompaktowy"),
    DETAILED("Szczegółowy"),
    HORIZONTAL("Poziomy"),
    VERTICAL("Pionowy"),
    ICON("Ikony"),
    TEXT("Tekstowy");

    private final String displayName;

    TeamBoardLayout(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
