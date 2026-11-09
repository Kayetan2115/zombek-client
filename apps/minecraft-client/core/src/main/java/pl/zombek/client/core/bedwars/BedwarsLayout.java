package pl.zombek.client.core.bedwars;

/**
 * General HUD layout styles for the Bedwars Helper panel.
 */
public enum BedwarsLayout {
    COMPACT("Kompaktowy"),
    DETAILED("Szczegółowy"),
    MINIMAL("Minimalny"),
    VERTICAL("Pionowy"),
    HORIZONTAL("Poziomy");

    private final String displayName;

    BedwarsLayout(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
