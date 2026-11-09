package pl.zombek.client.core.modules.hud.daycounter;

public enum DayCounterMode {
    WORLD_DAY("Dzień świata (World Day)"),
    WORLD_AGE("Łączny wiek świata (World Age)"),
    SESSION_DAY("Dzień sesji (Session Day)"),
    CUSTOM("Własny (Custom)");

    private final String displayName;

    DayCounterMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
