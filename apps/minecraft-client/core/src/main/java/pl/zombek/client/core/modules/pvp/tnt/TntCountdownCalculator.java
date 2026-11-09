package pl.zombek.client.core.modules.pvp.tnt;

import java.util.Locale;

/**
 * Calculates countdown timing, formatting, and dynamic ARGB colors for primed TNT.
 */
public final class TntCountdownCalculator {

    public static final int COLOR_GREEN = 0xFF55FF55;   // Safe (> 2.5s)
    public static final int COLOR_YELLOW = 0xFFFFFF55;  // Warning (1.0s - 2.5s)
    public static final int COLOR_RED = 0xFFFF5555;     // Critical (< 1.0s)

    private TntCountdownCalculator() {}

    /**
     * Converts fuse ticks (with visual offset) to seconds.
     */
    public static float calculateSeconds(int fuseTicks, int offsetTicks) {
        int effectiveTicks = Math.max(0, fuseTicks + offsetTicks);
        return effectiveTicks / 20.0f;
    }

    /**
     * Formats the TNT countdown string according to settings.
     */
    public static String formatLabel(int fuseTicks, int offsetTicks, boolean showPrefix,
                                     boolean showDecimalPlaces, int decimalPrecision,
                                     TntDisplayFormat format) {
        float seconds = calculateSeconds(fuseTicks, offsetTicks);

        String timeStr;
        if (showDecimalPlaces && decimalPrecision > 0) {
            String formatStr = "%." + Math.min(3, Math.max(1, decimalPrecision)) + "fs";
            timeStr = String.format(Locale.ROOT, formatStr, seconds);
        } else {
            timeStr = Math.round(seconds) + "s";
        }

        if (format == TntDisplayFormat.TIME_ONLY) {
            return timeStr;
        }

        if (showPrefix) {
            return "TNT " + timeStr;
        }

        return timeStr;
    }

    /**
     * Resolves the text color for the countdown label.
     */
    public static int resolveColor(int fuseTicks, boolean dynamicColor, int staticColor) {
        if (!dynamicColor) {
            return staticColor;
        }

        if (fuseTicks > 50) {
            return COLOR_GREEN;
        } else if (fuseTicks >= 20) {
            return COLOR_YELLOW;
        } else {
            return COLOR_RED;
        }
    }
}
