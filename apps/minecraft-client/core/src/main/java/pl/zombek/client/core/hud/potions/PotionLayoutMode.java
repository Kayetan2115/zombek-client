package pl.zombek.client.core.hud.potions;

/**
 * Layout presentation styles for Potion Effects HUD.
 */
public enum PotionLayoutMode {
    CLASSIC("Klasyczny (Ikona, nazwa i czas)"),
    COMPACT("Kompaktowy (Ikona z małym czasem)"),
    DETAILED("Szczegółowy (Ikona, nazwa, poziom i pasek)"),
    ICONS_ONLY("Tylko ikony"),
    CUSTOM("Własny");

    private final String displayName;

    PotionLayoutMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
