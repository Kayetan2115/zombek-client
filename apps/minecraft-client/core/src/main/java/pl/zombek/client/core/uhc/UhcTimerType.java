package pl.zombek.client.core.uhc;

public enum UhcTimerType {
    GAME("Game Timer", false, 0),
    PVP("PvP Timer", true, 600), // 10 min countdown default
    BORDER("Border Timer", true, 900), // 15 min countdown default
    MEETUP("Meetup Timer", true, 1800), // 30 min countdown default
    DEATHMATCH("Deathmatch Timer", true, 2400), // 40 min countdown default
    CUSTOM("Custom Timer", false, 0);

    private final String displayName;
    private final boolean countdown;
    private final long defaultDurationSeconds;

    UhcTimerType(String displayName, boolean countdown, long defaultDurationSeconds) {
        this.displayName = displayName;
        this.countdown = countdown;
        this.defaultDurationSeconds = defaultDurationSeconds;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isCountdown() {
        return countdown;
    }

    public long getDefaultDurationSeconds() {
        return defaultDurationSeconds;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
