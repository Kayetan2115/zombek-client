package pl.zombek.client.forge.integration;

import net.minecraft.client.Minecraft;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import pl.zombek.client.core.hud.scoreboard.IScoreboardDataProvider;
import pl.zombek.client.core.hud.scoreboard.ScoreboardLineData;
import pl.zombek.client.core.hud.scoreboard.ScoreboardObjectiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Forge platform provider supplying active Scoreboard sidebar data.
 */
public class ForgeScoreboardDataProvider implements IScoreboardDataProvider {

    @Override
    public boolean hasActiveSidebarObjective() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null) return false;
        Scoreboard scoreboard = mc.level.getScoreboard();
        if (scoreboard == null) return false;
        return scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) != null;
    }

    @Override
    public ScoreboardObjectiveData getSidebarObjective() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null) return null;
        Scoreboard scoreboard = mc.level.getScoreboard();
        if (scoreboard == null) return null;
        Objective objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (objective == null) return null;

        String title = objective.getDisplayName().getString();
        List<ScoreboardLineData> lines = new ArrayList<>();

        try {
            // In 1.20.4 Mojmap listPlayerScores(objective)
            var scores = scoreboard.listPlayerScores(objective);
            if (scores != null) {
                for (var score : scores) {
                    if (score == null) continue;
                    String owner = score.owner();
                    int val = score.value();

                    String prefix = "";
                    String suffix = "";
                    PlayerTeam team = scoreboard.getPlayersTeam(owner);
                    if (team != null) {
                        prefix = team.getPlayerPrefix().getString();
                        suffix = team.getPlayerSuffix().getString();
                    }

                    String formatted = prefix + owner + suffix;
                    lines.add(new ScoreboardLineData(val, owner, prefix, suffix, formatted));
                }
            }
        } catch (Throwable ignored) {}

        return new ScoreboardObjectiveData(objective.getName(), title, lines);
    }
}
