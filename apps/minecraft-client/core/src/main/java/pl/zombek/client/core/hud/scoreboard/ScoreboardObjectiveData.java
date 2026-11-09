package pl.zombek.client.core.hud.scoreboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data container representing a Scoreboard objective with its title and lines.
 */
public class ScoreboardObjectiveData {
    private final String name;
    private final String title;
    private final List<ScoreboardLineData> lines;

    public ScoreboardObjectiveData(String name, String title, List<ScoreboardLineData> lines) {
        this.name = name != null ? name : "";
        this.title = title != null ? title : "";
        this.lines = lines != null ? Collections.unmodifiableList(new ArrayList<>(lines)) : Collections.emptyList();
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public List<ScoreboardLineData> getLines() {
        return lines;
    }

    public boolean hasLines() {
        return !lines.isEmpty();
    }
}
