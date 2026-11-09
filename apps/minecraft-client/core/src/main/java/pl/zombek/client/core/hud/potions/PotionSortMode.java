package pl.zombek.client.core.hud.potions;

/**
 * Ordering options for active potion effects in the HUD.
 */
public enum PotionSortMode {
    DURATION_SHORTEST("Czas: od najkrótszego"),
    DURATION_LONGEST("Czas: od najdłuższego"),
    BENEFICIAL_FIRST("Pozytywne najpierw"),
    HARMFUL_FIRST("Negatywne najpierw"),
    ALPHABETICAL("Alfabetycznie (A-Z)"),
    CUSTOM_ORDER("Kolejność gry");

    private final String displayName;

    PotionSortMode(String displayName) {
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
