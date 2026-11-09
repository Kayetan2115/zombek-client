package pl.zombek.client.core.uhc;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser extracting publicly visible match information from Minecraft scoreboard lines.
 * Strictly avoids guessing; fields that cannot be matched return "N/A".
 */
public class UhcScoreboardParser {
    private static final Pattern PLAYERS_PATTERN = Pattern.compile("(?iu)(?:players|alive|żywi|zywi|gracze)[:\\s]+([0-9]+(?:/[0-9]+)?)");
    private static final Pattern TEAMS_PATTERN = Pattern.compile("(?iu)(?:teams|drużyny|druzyny)[:\\s]+([0-9]+)");
    private static final Pattern BORDER_PATTERN = Pattern.compile("(?iu)(?:border|granica)[:\\s]+([±0-9xX]+(?:\\s*m)?)");
    private static final Pattern TIME_PATTERN = Pattern.compile("(?iu)(?:time|czas)[:\\s]+([0-9]{1,2}:[0-9]{2}(?::[0-9]{2})?)");
    private static final Pattern STATUS_PATTERN = Pattern.compile("(?iu)(?:status|phase|faza)[:\\s]+([a-zA-Z0-9_ -]+)");

    private boolean enabled = true;
    private String matchName = "N/A";
    private String matchStatus = "N/A";
    private String matchTime = "N/A";
    private String alivePlayers = "N/A";
    private String teamCount = "N/A";
    private String borderStatus = "N/A";

    public UhcScoreboardParser() {}

    public synchronized void parseLines(List<String> rawLines) {
        if (!enabled || rawLines == null || rawLines.isEmpty()) {
            return;
        }

        // Reset to N/A prior to parse pass
        String parsedName = "N/A";
        String parsedStatus = "N/A";
        String parsedTime = "N/A";
        String parsedAlive = "N/A";
        String parsedTeams = "N/A";
        String parsedBorder = "N/A";

        for (int i = 0; i < rawLines.size(); i++) {
            String line = stripFormatting(rawLines.get(i)).trim();
            if (line.isEmpty()) continue;

            // First line might be the match or server title
            if (i == 0 && (line.contains("UHC") || line.contains("uhc") || line.contains("Match"))) {
                parsedName = line;
            }

            Matcher mAlive = PLAYERS_PATTERN.matcher(line);
            if (mAlive.find()) {
                parsedAlive = mAlive.group(1);
            }

            Matcher mTeams = TEAMS_PATTERN.matcher(line);
            if (mTeams.find()) {
                parsedTeams = mTeams.group(1);
            }

            Matcher mBorder = BORDER_PATTERN.matcher(line);
            if (mBorder.find()) {
                parsedBorder = mBorder.group(1);
            }

            Matcher mTime = TIME_PATTERN.matcher(line);
            if (mTime.find()) {
                parsedTime = mTime.group(1);
            }

            Matcher mStatus = STATUS_PATTERN.matcher(line);
            if (mStatus.find()) {
                parsedStatus = mStatus.group(1).trim();
            }
        }

        this.matchName = parsedName;
        this.matchStatus = parsedStatus;
        this.matchTime = parsedTime;
        this.alivePlayers = parsedAlive;
        this.teamCount = parsedTeams;
        this.borderStatus = parsedBorder;
    }

    public static String stripFormatting(String input) {
        if (input == null) return "";
        return input.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
    }

    public synchronized void reset() {
        this.matchName = "N/A";
        this.matchStatus = "N/A";
        this.matchTime = "N/A";
        this.alivePlayers = "N/A";
        this.teamCount = "N/A";
        this.borderStatus = "N/A";
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getMatchName() { return matchName; }
    public String getMatchStatus() { return matchStatus; }
    public String getMatchTime() { return matchTime; }
    public String getAlivePlayers() { return alivePlayers; }
    public String getTeamCount() { return teamCount; }
    public String getBorderStatus() { return borderStatus; }
}
