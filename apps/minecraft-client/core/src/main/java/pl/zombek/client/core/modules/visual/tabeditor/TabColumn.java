package pl.zombek.client.core.modules.visual.tabeditor;

/**
 * Columns available in the Tab Player List.
 */
public enum TabColumn {
    AVATAR("Avatar", 12),
    PREFIX("Prefix / Rank", 30),
    NAME("Player Name", 60),
    SUFFIX("Suffix", 20),
    TEAM("Team", 35),
    GAMEMODE("Game Mode", 35),
    SCORE("Score", 25),
    PING("Latency", 25);

    private final String label;
    private final int defaultWidth;

    TabColumn(String label, int defaultWidth) {
        this.label = label;
        this.defaultWidth = defaultWidth;
    }

    public String getLabel() {
        return label;
    }

    public int getDefaultWidth() {
        return defaultWidth;
    }
}
