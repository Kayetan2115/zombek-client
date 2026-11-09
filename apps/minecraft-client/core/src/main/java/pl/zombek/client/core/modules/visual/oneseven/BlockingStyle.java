package pl.zombek.client.core.modules.visual.oneseven;

/**
 * Visual blocking pose styles for sword blocking.
 */
public enum BlockingStyle {
    VANILLA_1_7("Vanilla 1.7", "Klasyczna pozycja blokowania z Minecraft 1.7.10"),
    VANILLA_1_8("Vanilla 1.8", "Domyślna pozycja blokowania z Minecraft 1.8"),
    LUNAR("Lunar", "Wygładzona pozycja blokowania w stylu Lunar Client"),
    CUSTOM("Custom", "Własne współrzędne translacji i rotacji");

    private final String displayName;
    private final String description;

    BlockingStyle(String displayName, String description) {
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
