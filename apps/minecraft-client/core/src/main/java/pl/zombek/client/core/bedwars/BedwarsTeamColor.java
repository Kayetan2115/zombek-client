package pl.zombek.client.core.bedwars;

/**
 * Standard Bedwars team colors with RGB hex values and letter representations.
 */
public enum BedwarsTeamColor {
    RED("Red", "Czerwoni", 'R', 0xFFFF5555, 0x88FF5555),
    BLUE("Blue", "Niebiescy", 'B', 0xFF5555FF, 0x885555FF),
    GREEN("Green", "Zieloni", 'G', 0xFF55FF55, 0x8855FF55),
    YELLOW("Yellow", "Żółci", 'Y', 0xFFFFFF55, 0x88FFFF55),
    AQUA("Aqua", "Błękitni", 'A', 0xFF55FFFF, 0x8855FFFF),
    WHITE("White", "Biali", 'W', 0xFFFFFFFF, 0x88FFFFFF),
    PINK("Pink", "Różowi", 'P', 0xFFFF55FF, 0x88FF55FF),
    GRAY("Gray", "Szary", 'S', 0xFFAAAAAA, 0x88AAAAAA);

    private final String englishName;
    private final String polishName;
    private final char code;
    private final int argb;
    private final int backgroundArgb;

    BedwarsTeamColor(String englishName, String polishName, char code, int argb, int backgroundArgb) {
        this.englishName = englishName;
        this.polishName = polishName;
        this.code = code;
        this.argb = argb;
        this.backgroundArgb = backgroundArgb;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getPolishName() {
        return polishName;
    }

    public char getCode() {
        return code;
    }

    public int getArgb() {
        return argb;
    }

    public int getBackgroundArgb() {
        return backgroundArgb;
    }

    public static BedwarsTeamColor fromLetter(char letter) {
        char upper = Character.toUpperCase(letter);
        for (BedwarsTeamColor color : values()) {
            if (color.code == upper) {
                return color;
            }
        }
        if (upper == 'G') return GREEN;
        return null;
    }

    public static BedwarsTeamColor fromName(String name) {
        if (name == null) return null;
        String lower = name.toLowerCase().trim();
        for (BedwarsTeamColor color : values()) {
            if (color.englishName.equalsIgnoreCase(lower) || color.polishName.equalsIgnoreCase(lower)) {
                return color;
            }
        }
        if (lower.startsWith("r") || lower.contains("czerw")) return RED;
        if (lower.startsWith("bl") || lower.contains("nieb")) return BLUE;
        if ((lower.startsWith("g") && (lower.contains("ree") || lower.contains("ziel"))) || lower.contains("ziel")) return GREEN;
        if (lower.startsWith("y") || lower.contains("zolt") || lower.contains("żółt") || lower.contains("żółci") || lower.contains("zolci")) return YELLOW;
        if (lower.startsWith("a") || lower.contains("cyan") || lower.contains("blekit") || lower.contains("błękit")) return AQUA;
        if (lower.startsWith("w") || lower.contains("bial") || lower.contains("biał")) return WHITE;
        if (lower.startsWith("p") || lower.contains("roz") || lower.contains("róż")) return PINK;
        if (lower.startsWith("gr") || lower.contains("szar")) return GRAY;
        return null;
    }
}
