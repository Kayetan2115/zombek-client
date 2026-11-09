package pl.zombek.client.core.uhc;

/**
 * Mode of scaling applied to dropped items in UHC Overlay.
 */
public enum UhcScaleMode {
    PER_ITEM("Per Item", "Używaj indywidualnej skali dla każdego typu przedmiotu."),
    GLOBAL_ONLY("Global Only", "Używaj jednolitej skali globalnej dla wszystkich wyróżnionych przedmiotów."),
    COMBINED("Combined", "Mnóż indywidualną skalę przedmiotu przez globalny mnożnik skali.");

    private final String displayName;
    private final String description;

    UhcScaleMode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
