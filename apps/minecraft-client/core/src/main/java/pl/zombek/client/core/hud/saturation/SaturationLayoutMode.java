package pl.zombek.client.core.hud.saturation;

/**
 * Layout styling presets for the Saturation & Hunger HUD module.
 */
public enum SaturationLayoutMode {
    COMPACT("Kompaktowy (Tekst: Głód i Nasycenie)"),
    DETAILED("Szczegółowy (Paski postępu z wartościami)"),
    MINIMAL("Minimalistyczny (Zwięzłe wartości)"),
    CLASSIC("Klasyczny (Ikony) ");

    private final String displayName;

    SaturationLayoutMode(String displayName) {
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
