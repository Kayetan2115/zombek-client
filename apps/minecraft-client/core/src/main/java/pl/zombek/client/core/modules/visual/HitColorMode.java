package pl.zombek.client.core.modules.visual;

/**
 * Visual rendering mode for the Hit Color effect.
 */
public enum HitColorMode {
    SOLID_COLOR("Solid Color", "Jednolicie barwiony model encji"),
    COLOR_OVERLAY("Color Overlay", "Przezroczysta nakładka barwna na teksturę"),
    OUTLINE_GLOW("Outline Glow", "Świetlisty obrys / poświata encji"),
    SOFT_TINT("Soft Tint", "Subtelne, delikatne zabarwienie modelu"),
    CUSTOM_COLOR("Custom Color", "Dowolny odcień z pełną kontrolą kanału alfa");

    private final String displayName;
    private final String description;

    HitColorMode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
