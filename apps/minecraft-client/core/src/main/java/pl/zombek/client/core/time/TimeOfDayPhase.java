package pl.zombek.client.core.time;

/**
 * Minecraft day/night cycle phases derived from in-game ticks [0..24000).
 */
public enum TimeOfDayPhase {
    DAWN("Świt", "Dawn", "🌅"),
    MORNING("Poranek", "Morning", "☀"),
    NOON("Południe", "Noon", "☀"),
    AFTERNOON("Popołudnie", "Afternoon", "🌤"),
    DUSK("Zmierzch", "Dusk", "🌇"),
    NIGHT("Noc", "Night", "🌙"),
    MIDNIGHT("Północ", "Midnight", "🌙");

    private final String polishName;
    private final String englishName;
    private final String icon;

    TimeOfDayPhase(String polishName, String englishName, String icon) {
        this.polishName = polishName;
        this.englishName = englishName;
        this.icon = icon;
    }

    public String getPolishName() {
        return polishName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getIcon() {
        return icon;
    }

    /**
     * Determines phase from Minecraft world day time ticks [0..24000).
     */
    public static TimeOfDayPhase fromTicks(long ticks) {
        long normalized = Math.floorMod(ticks, 24000L);
        if (normalized >= 23000L || normalized < 1000L) {
            return DAWN;
        } else if (normalized < 5500L) {
            return MORNING;
        } else if (normalized <= 6500L) {
            return NOON;
        } else if (normalized < 12000L) {
            return AFTERNOON;
        } else if (normalized < 14000L) {
            return DUSK;
        } else if (normalized >= 17500L && normalized <= 18500L) {
            return MIDNIGHT;
        } else {
            return NIGHT;
        }
    }

    public static boolean isDay(long ticks) {
        long normalized = Math.floorMod(ticks, 24000L);
        return normalized < 12000L || normalized >= 23000L;
    }

    public static boolean isSunrise(long ticks) {
        long normalized = Math.floorMod(ticks, 24000L);
        return normalized >= 23000L || normalized < 1500L;
    }

    public static boolean isSunset(long ticks) {
        long normalized = Math.floorMod(ticks, 24000L);
        return normalized >= 11500L && normalized < 14000L;
    }
}
