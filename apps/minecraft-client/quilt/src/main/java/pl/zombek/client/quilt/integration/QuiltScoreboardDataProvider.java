package pl.zombek.client.quilt.integration;

import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import pl.zombek.client.core.hud.scoreboard.IScoreboardDataProvider;
import pl.zombek.client.core.hud.scoreboard.ScoreboardLineData;
import pl.zombek.client.core.hud.scoreboard.ScoreboardObjectiveData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Quilt platform data provider supplying active sidebar Scoreboard objective and lines.
 */
public class QuiltScoreboardDataProvider implements IScoreboardDataProvider {

    @Override
    public boolean hasActiveSidebarObjective() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) {
            return false;
        }
        Scoreboard scoreboard = client.world.getScoreboard();
        if (scoreboard == null) {
            return false;
        }
        return scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR) != null;
    }

    @Override
    public ScoreboardObjectiveData getSidebarObjective() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) {
            return null;
        }
        Scoreboard scoreboard = client.world.getScoreboard();
        if (scoreboard == null) {
            return null;
        }
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) {
            return null;
        }

        String title = objective.getDisplayName().getString();
        List<ScoreboardLineData> lines = new ArrayList<>();

        try {
            Collection<ScoreboardEntry> entries = scoreboard.getScoreboardEntries(objective);
            if (entries != null) {
                for (ScoreboardEntry entry : entries) {
                    if (entry == null) continue;
                    String owner = entry.owner();
                    int score = entry.value();

                    String prefix = "";
                    String suffix = "";
                    Team team = scoreboard.getScoreHolderTeam(owner);
                    if (team != null) {
                        prefix = team.getPrefix().getString();
                        suffix = team.getSuffix().getString();
                    }

                    String formatted = prefix + owner + suffix;
                    lines.add(new ScoreboardLineData(score, owner, prefix, suffix, formatted));
                }
            }
        } catch (Throwable ignored) {}

        return new ScoreboardObjectiveData(objective.getName(), title, lines);
    }
}
