package pl.zombek.client.core.hypixel;

/**
 * Safety presets controlling the level of feature restrictions on Hypixel and other servers.
 */
public enum SafetyPreset {
    STRICT_SAFETY("Strict Safety", "Ścisłe bezpieczeństwo: tylko funkcje kosmetyczne i lokalne UI"),
    CUSTOM("Custom", "Niestandardowy: wybór funkcji z blokadą niedozwolonych kategorii"),
    OTHER_SERVERS("Other Servers", "Profil dla innych serwerów (blokada automatyzacji nadal aktywna)");

    private final String displayName;
    private final String description;

    SafetyPreset(String displayName, String description) {
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
