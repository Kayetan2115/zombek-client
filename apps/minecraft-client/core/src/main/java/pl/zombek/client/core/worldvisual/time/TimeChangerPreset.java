package pl.zombek.client.core.worldvisual.time;

/**
 * Visual time presets for local client sky and sun cycle rendering.
 * Minecraft day cycle: 24000 ticks.
 * 0 / 24000: Dawn / Sunrise
 * 1000: Early Morning
 * 6000: Solar Noon (sun directly overhead)
 * 9000: Afternoon
 * 12000: Sunset (dusk)
 * 14000: Night
 * 18000: Midnight (moon directly overhead)
 */
public enum TimeChangerPreset {
    VANILLA(-1L, "Vanilla"),
    SUNRISE(23000L, "Sunrise"),
    MORNING(1000L, "Morning"),
    NOON(6000L, "Noon"),
    AFTERNOON(9000L, "Afternoon"),
    SUNSET(12000L, "Sunset"),
    NIGHT(14000L, "Night"),
    MIDNIGHT(18000L, "Midnight"),
    CUSTOM(6000L, "Custom");

    private final long ticks;
    private final String displayName;

    TimeChangerPreset(long ticks, String displayName) {
        this.ticks = ticks;
        this.displayName = displayName;
    }

    public long getTicks() {
        return ticks;
    }

    public String getDisplayName() {
        return displayName;
    }
}
