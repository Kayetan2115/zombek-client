package pl.zombek.client.core.modules.hud.playtime;

import pl.zombek.client.core.time.PlaytimeStats;

public enum PlaytimeFormat {
    HH_MM_SS("Godziny:Minuty:Sekundy (02:35:41)"),
    DD_HH_MM_SS("Dni Godziny:Minuty:Sekundy (0d 02:35:41)"),
    HUMAN_READABLE("Czytelny (2h 35m 41s)"),
    COMPACT("Kompaktowy (2h 35m)");

    private final String displayName;

    PlaytimeFormat(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String format(long seconds, boolean showSeconds) {
        switch (this) {
            case HH_MM_SS:
                return PlaytimeStats.formatDuration(seconds, showSeconds, false);
            case DD_HH_MM_SS:
                return PlaytimeStats.formatDuration(seconds, showSeconds, false);
            case HUMAN_READABLE:
                return PlaytimeStats.formatDuration(seconds, showSeconds, true);
            case COMPACT:
                return PlaytimeStats.formatDuration(seconds, false, true);
            default:
                return PlaytimeStats.formatDuration(seconds, showSeconds, false);
        }
    }
}
