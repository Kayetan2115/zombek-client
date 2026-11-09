package pl.zombek.client.core.worldvisual.weather;

/**
 * Visual weather presets for client-side atmosphere override.
 */
public enum WeatherPreset {
    VANILLA("Vanilla"),
    CLEAR("Clear"),
    RAIN("Rain"),
    SNOW("Snow"),
    THUNDER("Thunder"),
    CUSTOM("Custom");

    private final String displayName;

    WeatherPreset(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
