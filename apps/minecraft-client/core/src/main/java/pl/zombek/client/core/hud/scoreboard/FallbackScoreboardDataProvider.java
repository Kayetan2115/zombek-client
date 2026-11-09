package pl.zombek.client.core.hud.scoreboard;

import java.util.Arrays;
import java.util.List;

/**
 * Fallback provider used in headless tests or when no platform provider is registered.
 * Supports generating sample preview data when requested.
 */
public class FallbackScoreboardDataProvider implements IScoreboardDataProvider {

    private boolean previewMode = false;
    private ScoreboardObjectiveData customObjective = null;

    public void setPreviewMode(boolean previewMode) {
        this.previewMode = previewMode;
    }

    public void setActiveObjective(ScoreboardObjectiveData objective) {
        this.customObjective = objective;
    }

    @Override
    public boolean hasActiveSidebarObjective() {
        return customObjective != null || previewMode;
    }

    @Override
    public ScoreboardObjectiveData getSidebarObjective() {
        if (customObjective != null) {
            return customObjective;
        }
        if (!previewMode) {
            return null;
        }

        List<ScoreboardLineData> sampleLines = Arrays.asList(
                new ScoreboardLineData(10, "line_top", "§724/12/2026 ", "§8m101", "§724/12/2026 §8m101"),
                new ScoreboardLineData(9, "blank_1", "", "", " "),
                new ScoreboardLineData(8, "stat_coins", "§fMonety: ", "§61,450", "§fMonety: §61,450"),
                new ScoreboardLineData(7, "stat_kills", "§fZabójstwa: ", "§a12", "§fZabójstwa: §a12"),
                new ScoreboardLineData(6, "stat_streak", "§fSeria: ", "§c5", "§fSeria: §c5"),
                new ScoreboardLineData(5, "blank_2", "", "", "  "),
                new ScoreboardLineData(4, "server_ip", "§ezombekclient.pl", "", "§ezombekclient.pl")
        );

        return new ScoreboardObjectiveData("preview_objective", "§6§lZOMBEK§f§lCLIENT", sampleLines);
    }
}
