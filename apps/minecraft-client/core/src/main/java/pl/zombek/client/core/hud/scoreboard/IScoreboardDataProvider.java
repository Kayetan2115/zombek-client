package pl.zombek.client.core.hud.scoreboard;

/**
 * Platform provider interface for querying active Scoreboard sidebar data.
 */
public interface IScoreboardDataProvider {

    /**
     * Checks if there is an active Scoreboard objective displayed in the sidebar slot.
     */
    boolean hasActiveSidebarObjective();

    /**
     * Retrieves the current sidebar objective data including title and ordered lines.
     * Returns null if no sidebar objective is active.
     */
    ScoreboardObjectiveData getSidebarObjective();
}
