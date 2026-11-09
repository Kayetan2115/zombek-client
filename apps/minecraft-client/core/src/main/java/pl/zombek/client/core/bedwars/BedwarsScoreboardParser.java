package pl.zombek.client.core.bedwars;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Scoreboard parser extracting game mode, map, team bed statuses, player counts,
 * and event timers from Minecraft scoreboard lines.
 */
public class BedwarsScoreboardParser {
    // Standard Hypixel Patterns
    private static final Pattern HYPIXEL_TEAM_PATTERN = Pattern.compile(
            "(?iu)^([RBGYAWPS])(?:\\s+([A-Za-z]+))?:?\\s+([✓✕✔xX\\?]|\\d+|\\[E\\])(?:\\s*\\(you\\))?"
    );
    private static final Pattern TIMER_PATTERN = Pattern.compile(
            "(?iu)(Diamond|Emerald|Beds? Destroy|Sudden Death|Dragon Spawn|Koniec|Ulepszenie)(?:\\s+([IVX0-9]+))?\\s+(?:in|za)?\\s*([0-9]{1,2}:[0-9]{2})"
    );
    private static final Pattern STATS_KILLS_PATTERN = Pattern.compile("(?iu)(?<!final\\s)(?<!ostateczne\\s)\\b(?:Kills|Zabójstwa)[:\\s]+([0-9]+)");
    private static final Pattern STATS_FINALS_PATTERN = Pattern.compile("(?iu)(?:Final Kills|Ostateczne)[:\\s]+([0-9]+)");
    private static final Pattern STATS_BEDS_PATTERN = Pattern.compile("(?iu)(?:Beds Broken|Zniszczone łóżka)[:\\s]+([0-9]+)");

    private ParserProfile profile = ParserProfile.HYPIXEL_ENGLISH;
    private String customPatterns = "";
    private boolean debugMode = false;
    private int parserPriority = 1;

    // Parsed Data Cache
    private List<String> lastRawLines = Collections.emptyList();
    private String parsedMap = "N/A";
    private BedwarsMode parsedMode = BedwarsMode.OTHER;
    private final Map<BedwarsTeamColor, BedwarsTeam> parsedTeams = new EnumMap<>(BedwarsTeamColor.class);
    private final List<BedwarsTimer> parsedTimers = new ArrayList<>();
    private int scoreboardKills = -1;
    private int scoreboardFinalKills = -1;
    private int scoreboardBedsBroken = -1;
    private BedwarsTeamColor playerTeamColor = null;

    public BedwarsScoreboardParser() {
        for (BedwarsTeamColor c : BedwarsTeamColor.values()) {
            parsedTeams.put(c, new BedwarsTeam(c));
        }
    }

    public synchronized void parseLines(List<String> rawLines) {
        if (rawLines == null || rawLines.isEmpty()) {
            return;
        }

        // Cache check: if identical lines received, reuse parsed state
        if (rawLines.equals(lastRawLines)) {
            return;
        }
        this.lastRawLines = new ArrayList<>(rawLines);

        parsedTimers.clear();
        String candidateMap = "N/A";
        BedwarsMode candidateMode = BedwarsMode.OTHER;
        BedwarsTeamColor detectedSelfColor = null;

        for (int i = 0; i < rawLines.size(); i++) {
            String raw = rawLines.get(i);
            String line = stripFormatting(raw).trim();
            if (line.isEmpty()) continue;

            // 1. Map & Mode
            if (line.toLowerCase(Locale.ROOT).startsWith("map:") || line.toLowerCase(Locale.ROOT).startsWith("mapa:")) {
                int colon = line.indexOf(':');
                if (colon >= 0 && colon < line.length() - 1) {
                    candidateMap = line.substring(colon + 1).trim();
                }
            }
            if (line.toLowerCase(Locale.ROOT).startsWith("mode:") || line.toLowerCase(Locale.ROOT).startsWith("tryb:")) {
                int colon = line.indexOf(':');
                if (colon >= 0 && colon < line.length() - 1) {
                    candidateMode = BedwarsMode.parse(line.substring(colon + 1).trim());
                }
            }

            // 2. Scoreboard statistics
            Matcher mFinals = STATS_FINALS_PATTERN.matcher(line);
            if (mFinals.find()) {
                try { scoreboardFinalKills = Integer.parseInt(mFinals.group(1)); } catch (NumberFormatException ignored) {}
            } else {
                Matcher mKills = STATS_KILLS_PATTERN.matcher(line);
                if (mKills.find()) {
                    try { scoreboardKills = Integer.parseInt(mKills.group(1)); } catch (NumberFormatException ignored) {}
                }
            }
            Matcher mBeds = STATS_BEDS_PATTERN.matcher(line);
            if (mBeds.find()) {
                try { scoreboardBedsBroken = Integer.parseInt(mBeds.group(1)); } catch (NumberFormatException ignored) {}
            }

            // 3. Timers
            Matcher mTimer = TIMER_PATTERN.matcher(line);
            if (mTimer.find()) {
                String timerName = mTimer.group(1);
                String roman = mTimer.group(2);
                String timeStr = mTimer.group(3);
                String fullName = roman != null && !roman.isEmpty() ? (timerName + " " + roman) : timerName;
                int seconds = parseTimeSeconds(timeStr);
                parsedTimers.add(new BedwarsTimer("timer_" + parsedTimers.size(), fullName, seconds));
            }

            // 4. Team bed status lines
            Matcher mTeam = HYPIXEL_TEAM_PATTERN.matcher(line);
            if (mTeam.find()) {
                char code = mTeam.group(1).charAt(0);
                String teamName = mTeam.group(2);
                String statusStr = mTeam.group(3);

                BedwarsTeamColor color = BedwarsTeamColor.fromLetter(code);
                if (color != null) {
                    BedwarsTeam team = parsedTeams.get(color);
                    if (teamName != null && !teamName.isEmpty()) {
                        team.setName(teamName);
                    }

                    if (statusStr.equals("✓") || statusStr.equals("✔")) {
                        team.setBedStatus(BedStatus.BED_ALIVE);
                        if (team.getAlivePlayers() <= 0) {
                            team.setAlivePlayers(1); // At least 1 player alive when bed is alive
                        }
                    } else if (statusStr.equals("✕") || statusStr.equalsIgnoreCase("x")) {
                        team.setBedStatus(BedStatus.BED_DESTROYED);
                    } else if (statusStr.equalsIgnoreCase("[E]")) {
                        team.setBedStatus(BedStatus.TEAM_ELIMINATED);
                        team.setAlivePlayers(0);
                    } else {
                        // Might be a remaining player count: e.g. "2"
                        try {
                            int players = Integer.parseInt(statusStr);
                            if (players <= 0) {
                                team.setBedStatus(BedStatus.TEAM_ELIMINATED);
                                team.setAlivePlayers(0);
                            } else {
                                team.setBedStatus(BedStatus.BED_DESTROYED);
                                team.setAlivePlayers(players);
                            }
                        } catch (NumberFormatException ignored) {
                            team.setBedStatus(BedStatus.UNKNOWN);
                        }
                    }

                    if (line.toLowerCase(Locale.ROOT).contains("(you)") || line.toLowerCase(Locale.ROOT).contains("(ty)")) {
                        detectedSelfColor = color;
                    }
                }
            }

            // 5. Custom regex patterns if active
            if (profile == ParserProfile.CUSTOM && customPatterns != null && !customPatterns.isEmpty()) {
                parseCustomLine(line);
            }
        }

        if (!candidateMap.equals("N/A")) {
            this.parsedMap = candidateMap;
        }
        if (candidateMode != BedwarsMode.OTHER) {
            this.parsedMode = candidateMode;
        }
        if (detectedSelfColor != null) {
            this.playerTeamColor = detectedSelfColor;
        }
    }

    private void parseCustomLine(String line) {
        // Safe evaluation of semicolon-delimited custom patterns: e.g. "team=([A-Z]):(\\w+)"
        String[] rules = customPatterns.split(";");
        for (String rule : rules) {
            String trimmed = rule.trim();
            if (trimmed.isEmpty()) continue;
            try {
                Pattern p = Pattern.compile(trimmed);
                Matcher m = p.matcher(line);
                if (m.find() && m.groupCount() >= 2) {
                    BedwarsTeamColor color = BedwarsTeamColor.fromName(m.group(1));
                    if (color != null) {
                        BedwarsTeam team = parsedTeams.get(color);
                        String val = m.group(2);
                        if (val.equals("✓") || val.equalsIgnoreCase("alive")) {
                            team.setBedStatus(BedStatus.BED_ALIVE);
                        } else if (val.equals("✕") || val.equalsIgnoreCase("dead") || val.equalsIgnoreCase("destroyed")) {
                            team.setBedStatus(BedStatus.BED_DESTROYED);
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    public static int parseTimeSeconds(String timeStr) {
        if (timeStr == null || !timeStr.contains(":")) return 0;
        try {
            String[] parts = timeStr.trim().split(":");
            int min = Integer.parseInt(parts[0]);
            int sec = Integer.parseInt(parts[1]);
            return min * 60 + sec;
        } catch (Exception e) {
            return 0;
        }
    }

    public static String stripFormatting(String input) {
        if (input == null) return "";
        return input.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
    }

    public synchronized void reset() {
        this.lastRawLines = Collections.emptyList();
        this.parsedMap = "N/A";
        this.parsedMode = BedwarsMode.OTHER;
        this.scoreboardKills = -1;
        this.scoreboardFinalKills = -1;
        this.scoreboardBedsBroken = -1;
        this.playerTeamColor = null;
        for (BedwarsTeam t : parsedTeams.values()) {
            t.reset();
        }
        parsedTimers.clear();
    }

    public ParserProfile getProfile() {
        return profile;
    }

    public void setProfile(ParserProfile profile) {
        if (profile != null) this.profile = profile;
    }

    public String getCustomPatterns() {
        return customPatterns;
    }

    public void setCustomPatterns(String customPatterns) {
        this.customPatterns = customPatterns != null ? customPatterns : "";
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public int getParserPriority() {
        return parserPriority;
    }

    public void setParserPriority(int parserPriority) {
        this.parserPriority = parserPriority;
    }

    public String getParsedMap() {
        return parsedMap;
    }

    public BedwarsMode getParsedMode() {
        return parsedMode;
    }

    public Map<BedwarsTeamColor, BedwarsTeam> getParsedTeams() {
        return parsedTeams;
    }

    public List<BedwarsTimer> getParsedTimers() {
        return parsedTimers;
    }

    public int getScoreboardKills() {
        return scoreboardKills;
    }

    public int getScoreboardFinalKills() {
        return scoreboardFinalKills;
    }

    public int getScoreboardBedsBroken() {
        return scoreboardBedsBroken;
    }

    public BedwarsTeamColor getPlayerTeamColor() {
        return playerTeamColor;
    }

    public void setPlayerTeamColor(BedwarsTeamColor color) {
        this.playerTeamColor = color;
    }
}
