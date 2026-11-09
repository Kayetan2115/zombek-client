package pl.zombek.client.core.hypixel;

/**
 * Types of local Hypixel HUD notifications.
 */
public enum HypixelNotificationType {
    SERVER_DETECTION("Detekcja serwera", 0xFF55FF55),
    SAFETY_WARNING("Ostrzeżenie bezpieczeństwa", 0xFFFF5555),
    MANUAL_SUGGESTION("Sugestia ręczna", 0xFFFFFF55),
    MODULE_STATUS("Status modułu", 0xFF55FFFF);

    private final String displayName;
    private final int defaultAccentColor;

    HypixelNotificationType(String displayName, int defaultAccentColor) {
        this.displayName = displayName;
        this.defaultAccentColor = defaultAccentColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getDefaultAccentColor() {
        return defaultAccentColor;
    }
}
