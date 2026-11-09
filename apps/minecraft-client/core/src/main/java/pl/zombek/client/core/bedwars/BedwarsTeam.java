package pl.zombek.client.core.bedwars;

/**
 * Model representing a Bedwars team and its current match state.
 */
public class BedwarsTeam {
    private final BedwarsTeamColor color;
    private String name;
    private BedStatus bedStatus;
    private int alivePlayers;
    private int eliminatedPlayers;
    private TeamState teamState;
    private String lastKnownEvent;

    public BedwarsTeam(BedwarsTeamColor color) {
        this.color = color;
        this.name = color.getEnglishName();
        this.bedStatus = BedStatus.UNKNOWN;
        this.alivePlayers = -1;
        this.eliminatedPlayers = -1;
        this.teamState = TeamState.UNKNOWN;
        this.lastKnownEvent = "";
    }

    public BedwarsTeam(BedwarsTeamColor color, String name, BedStatus bedStatus, int alivePlayers) {
        this.color = color;
        this.name = name != null ? name : color.getEnglishName();
        this.bedStatus = bedStatus != null ? bedStatus : BedStatus.UNKNOWN;
        this.alivePlayers = alivePlayers;
        this.eliminatedPlayers = 0;
        this.teamState = resolveTeamState(this.bedStatus, this.alivePlayers);
        this.lastKnownEvent = "";
    }

    public BedwarsTeamColor getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
    }

    public BedStatus getBedStatus() {
        return bedStatus;
    }

    public void setBedStatus(BedStatus bedStatus) {
        if (bedStatus != null) {
            this.bedStatus = bedStatus;
            updateTeamState();
        }
    }

    public int getAlivePlayers() {
        return alivePlayers;
    }

    public void setAlivePlayers(int alivePlayers) {
        this.alivePlayers = alivePlayers;
        updateTeamState();
    }

    public int getEliminatedPlayers() {
        return eliminatedPlayers;
    }

    public void setEliminatedPlayers(int eliminatedPlayers) {
        this.eliminatedPlayers = eliminatedPlayers;
    }

    public TeamState getTeamState() {
        return teamState;
    }

    public void setTeamState(TeamState teamState) {
        this.teamState = teamState;
    }

    public String getLastKnownEvent() {
        return lastKnownEvent;
    }

    public void setLastKnownEvent(String lastKnownEvent) {
        this.lastKnownEvent = lastKnownEvent != null ? lastKnownEvent : "";
    }

    private void updateTeamState() {
        this.teamState = resolveTeamState(this.bedStatus, this.alivePlayers);
    }

    public static TeamState resolveTeamState(BedStatus bedStatus, int alivePlayers) {
        if (bedStatus == BedStatus.TEAM_ELIMINATED || alivePlayers == 0) {
            return TeamState.ELIMINATED;
        }
        if (bedStatus == BedStatus.BED_DESTROYED) {
            return TeamState.BEDLESS;
        }
        if (bedStatus == BedStatus.BED_ALIVE) {
            return TeamState.ACTIVE;
        }
        return TeamState.UNKNOWN;
    }

    public boolean isEliminated() {
        return teamState == TeamState.ELIMINATED || bedStatus == BedStatus.TEAM_ELIMINATED || alivePlayers == 0;
    }

    public boolean isBedAlive() {
        return bedStatus == BedStatus.BED_ALIVE;
    }

    public String getFormattedPlayerCount() {
        if (alivePlayers < 0) {
            return "N/A";
        }
        return String.valueOf(alivePlayers);
    }

    public void reset() {
        this.bedStatus = BedStatus.UNKNOWN;
        this.alivePlayers = -1;
        this.eliminatedPlayers = -1;
        this.teamState = TeamState.UNKNOWN;
        this.lastKnownEvent = "";
    }
}
