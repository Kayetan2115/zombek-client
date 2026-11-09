package pl.zombek.client.core.modules.visual.shinypots;

/**
 * Categorization of potion item types for Shiny Pots filtering.
 */
public enum PotionType {
    REGULAR("Standardowa Mikstura"),
    SPLASH("Mikstura Miotana (Splash)"),
    LINGERING("Mikstura Trwała (Lingering)"),
    TIPPED_ARROW("Strzała z Miksturą (Tipped Arrow)"),
    OTHER("Inny Przedmiot");

    private final String displayName;

    PotionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static PotionType fromRegistryId(String registryId) {
        if (registryId == null) return OTHER;
        String id = registryId.toLowerCase().trim();
        if (id.contains("splash_potion")) {
            return SPLASH;
        } else if (id.contains("lingering_potion")) {
            return LINGERING;
        } else if (id.contains("tipped_arrow")) {
            return TIPPED_ARROW;
        } else if (id.contains("potion")) {
            return REGULAR;
        }
        return OTHER;
    }
}
