package pl.zombek.client.core.hud.potions;

/**
 * Categorization of status effects for sorting and color highlighting.
 */
public enum StatusEffectCategory {
    BENEFICIAL("Pozytywny (Buff)", 0xFF55FF55),
    NEUTRAL("Neutralny", 0xFFFFFFFF),
    HARMFUL("Negatywny (Debuff)", 0xFFFF5555);

    private final String displayName;
    private final int defaultColor;

    StatusEffectCategory(String displayName, int defaultColor) {
        this.displayName = displayName;
        this.defaultColor = defaultColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getDefaultColor() {
        return defaultColor;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
