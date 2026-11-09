package pl.zombek.client.core.bedwars;

/**
 * Recognized Bedwars game modes.
 */
public enum BedwarsMode {
    SOLO("Solo", 1, 8),
    DOUBLES("Doubles", 2, 8),
    THREE_V_THREE_V_THREE_V_THREE("3v3v3v3", 3, 4),
    FOUR_V_FOUR_V_FOUR_V_FOUR("4v4v4v4", 4, 4),
    FOUR_V_FOUR("4v4", 4, 2),
    OTHER("Inny", -1, -1),
    CUSTOM("Własny", -1, -1);

    private final String displayName;
    private final int teamSize;
    private final int maxTeams;

    BedwarsMode(String displayName, int teamSize, int maxTeams) {
        this.displayName = displayName;
        this.teamSize = teamSize;
        this.maxTeams = maxTeams;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getTeamSize() {
        return teamSize;
    }

    public int getMaxTeams() {
        return maxTeams;
    }

    public static BedwarsMode parse(String text) {
        if (text == null) return OTHER;
        String lower = text.toLowerCase().trim();
        if (lower.contains("solo") || lower.equals("1s") || lower.equals("1v1")) {
            return SOLO;
        }
        if (lower.contains("doubles") || lower.contains("double") || lower.equals("2s") || lower.equals("2v2")) {
            return DOUBLES;
        }
        if (lower.contains("3v3v3v3") || lower.contains("3s") || lower.contains("trio") || lower.contains("trios")) {
            return THREE_V_THREE_V_THREE_V_THREE;
        }
        if (lower.contains("4v4v4v4") || lower.contains("squad") || lower.contains("squads") || lower.contains("4s")) {
            return FOUR_V_FOUR_V_FOUR_V_FOUR;
        }
        if (lower.contains("4v4") || lower.contains("duel 4v4")) {
            return FOUR_V_FOUR;
        }
        return OTHER;
    }
}
