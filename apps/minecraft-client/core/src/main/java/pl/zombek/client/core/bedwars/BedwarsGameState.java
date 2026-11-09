package pl.zombek.client.core.bedwars;

/**
 * Game states of a Bedwars match.
 */
public enum BedwarsGameState {
    UNKNOWN("Nieznany"),
    NOT_IN_BEDWARS("Poza Bedwars"),
    LOBBY("Lobby"),
    WAITING("Oczekiwanie"),
    STARTING("Odliczanie"),
    IN_GAME("W grze"),
    BEDWARS_ACTIVE("Mecz aktywny"),
    SUDDEN_DEATH("Nagła śmierć"),
    GAME_ENDING("Koniec meczu"),
    GAME_ENDED("Zakończony");

    private final String displayName;

    BedwarsGameState(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isPlaying() {
        return this == IN_GAME || this == BEDWARS_ACTIVE || this == SUDDEN_DEATH;
    }
}
