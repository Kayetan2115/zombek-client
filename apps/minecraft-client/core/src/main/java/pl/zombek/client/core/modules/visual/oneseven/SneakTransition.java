package pl.zombek.client.core.modules.visual.oneseven;

/**
 * Sneak camera animation transition styles.
 */
public enum SneakTransition {
    INSTANT_DOWN_SMOOTH_UP("Instant Down, Smooth Up", "Błyskawiczne opuszczenie kamery przy kucaniu i płynny powrót w górę (klasyk 1.7)"),
    FULL_INSTANT("Full Instant", "Całkowicie natychmiastowa zmiana wysokości oczu (w dół i w górę)"),
    SMOOTH_1_7("Smooth 1.7", "Szybka, klasyczna interpolacja liniowa"),
    MODERN("Modern", "Domyślna wolna interpolacja z nowoczesnego Minecrafta");

    private final String displayName;
    private final String description;

    SneakTransition(String displayName, String description) {
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
