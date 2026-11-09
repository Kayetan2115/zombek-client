package pl.zombek.client.core.hud.bossbar;

/**
 * Standard Minecraft BossBar colors mapped to display names and default ARGB values.
 */
public enum BossBarColor {
    PINK("Różowy", 0xFFFF55FF),
    BLUE("Niebieski", 0xFF55FFFF),
    RED("Czerwony", 0xFFFF5555),
    GREEN("Zielony", 0xFF55FF55),
    YELLOW("Żółty", 0xFFFFFF55),
    PURPLE("Fioletowy", 0xFFAA00AA),
    WHITE("Biały", 0xFFFFFFFF);

    private final String displayName;
    private final int argb;

    BossBarColor(String displayName, int argb) {
        this.displayName = displayName;
        this.argb = argb;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getArgb() {
        return argb;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
