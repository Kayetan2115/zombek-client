package pl.zombek.client.core.modules.hud.teamview;

/**
 * Detection mode for identifying teammates.
 */
public enum TeamDetectionMode {
    VANILLA_TEAMS("Vanilla Scoreboard Teams"),
    MANUAL_TEAM("Manualna Lista Drużyny"),
    BOTH("Oba Źródła (Vanilla + Manualna)");

    private final String displayName;

    TeamDetectionMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
