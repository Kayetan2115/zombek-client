package pl.zombek.client.core.bedwars;

/**
 * State of a match or event timer in Bedwars.
 */
public enum TimerState {
    AVAILABLE("Dostępny"),
    RUNNING("Odlicza"),
    PAUSED("Wstrzymany"),
    FINISHED("Zakończony"),
    UNKNOWN("Nieznany");

    private final String displayName;

    TimerState(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
