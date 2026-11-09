package pl.zombek.client.core.hud.scoreboard;

/**
 * Immutable data transfer object representing a single line in a Minecraft Scoreboard.
 */
public class ScoreboardLineData {
    private final int score;
    private final String playerName;
    private final String prefix;
    private final String suffix;
    private final String formattedText;
    private final boolean empty;

    public ScoreboardLineData(int score, String playerName, String prefix, String suffix, String formattedText) {
        this.score = score;
        this.playerName = playerName != null ? playerName : "";
        this.prefix = prefix != null ? prefix : "";
        this.suffix = suffix != null ? suffix : "";
        this.formattedText = formattedText != null ? formattedText : (this.prefix + this.playerName + this.suffix);
        this.empty = this.formattedText.trim().isEmpty();
    }

    public ScoreboardLineData(int score, String formattedText) {
        this(score, formattedText, "", "", formattedText);
    }

    public int getScore() {
        return score;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getFormattedText() {
        return formattedText;
    }

    public boolean isEmpty() {
        return empty;
    }

    @Override
    public String toString() {
        return "ScoreboardLineData{score=" + score + ", text='" + formattedText + "'}";
    }
}
