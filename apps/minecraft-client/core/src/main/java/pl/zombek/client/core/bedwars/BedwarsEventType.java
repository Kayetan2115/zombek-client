package pl.zombek.client.core.bedwars;

/**
 * Recognizable events in a Bedwars match.
 */
public enum BedwarsEventType {
    BED_DESTROYED("Zniszczenie łóżka", "⚑", 0xFFFF5555),
    PLAYER_ELIMINATED("Eliminacja gracza", "☠", 0xFFAAAAAA),
    FINAL_KILL("Final Kill", "⚔", 0xFFFF5555),
    TEAM_ELIMINATED("Drużyna wyeliminowana", "✕", 0xFFAAAAAA),
    GAME_STARTED("Mecz rozpoczęty", "▶", 0xFF55FF55),
    GAME_ENDED("Koniec meczu", "⏹", 0xFFFFFF55),
    SUDDEN_DEATH("Nagła śmierć", "⚡", 0xFFFF55FF),
    VICTORY("Zwycięstwo", "★", 0xFF55FF55),
    DEFEAT("Przegrana", "✕", 0xFFFF5555);

    private final String displayName;
    private final String defaultIcon;
    private final int defaultColor;

    BedwarsEventType(String displayName, String defaultIcon, int defaultColor) {
        this.displayName = displayName;
        this.defaultIcon = defaultIcon;
        this.defaultColor = defaultColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDefaultIcon() {
        return defaultIcon;
    }

    public int getDefaultColor() {
        return defaultColor;
    }
}
