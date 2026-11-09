package pl.zombek.client.core.hypixel;

/**
 * Server connection status representing the detected Minecraft network.
 */
public enum HypixelServerStatus {
    HYPIXEL_DETECTED("Hypixel wykryty", true),
    CUSTOM_HYPIXEL_PROFILE("Profil ręczny", true),
    OTHER_SERVER("Inny serwer", false),
    UNKNOWN_SERVER("Nieznany serwer", false),
    SINGLEPLAYER("Tryb jednoosobowy", false);

    private final String displayName;
    private final boolean isHypixelEnvironment;

    HypixelServerStatus(String displayName, boolean isHypixelEnvironment) {
        this.displayName = displayName;
        this.isHypixelEnvironment = isHypixelEnvironment;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isHypixelEnvironment() {
        return isHypixelEnvironment;
    }
}
