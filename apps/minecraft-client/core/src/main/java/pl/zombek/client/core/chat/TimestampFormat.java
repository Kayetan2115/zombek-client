package pl.zombek.client.core.chat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Supported timestamp formats for chat messages.
 */
public enum TimestampFormat {
    NONE("Brak", ""),
    HH_MM("HH:mm", "HH:mm"),
    HH_MM_SS("HH:mm:ss", "HH:mm:ss"),
    HH_MM_SS_12H("12-godzinny (AM/PM)", "hh:mm:ss a"),
    RELATIVE("Względny", "relative"),
    CUSTOM("Niestandardowy", "");

    private final String displayName;
    private final String pattern;

    TimestampFormat(String displayName, String pattern) {
        this.displayName = displayName;
        this.pattern = pattern;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPattern() {
        return pattern;
    }

    public String formatTime(long epochMillis, String customPattern) {
        if (this == NONE) {
            return "";
        }
        if (this == RELATIVE) {
            long diffSec = (System.currentTimeMillis() - epochMillis) / 1000L;
            if (diffSec < 5) return "teraz";
            if (diffSec < 60) return diffSec + "s temu";
            long diffMin = diffSec / 60;
            if (diffMin < 60) return diffMin + "m temu";
            long diffHours = diffMin / 60;
            return diffHours + "h temu";
        }

        String pat = this == CUSTOM ? (customPattern != null && !customPattern.isEmpty() ? customPattern : "HH:mm") : this.pattern;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pat, Locale.ROOT);
            return sdf.format(new Date(epochMillis));
        } catch (Exception e) {
            return new SimpleDateFormat("HH:mm", Locale.ROOT).format(new Date(epochMillis));
        }
    }
}
