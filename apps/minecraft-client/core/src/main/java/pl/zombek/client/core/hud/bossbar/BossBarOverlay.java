package pl.zombek.client.core.hud.bossbar;

/**
 * Standard Minecraft BossBar overlay styles (continuous progress or notched segments).
 */
public enum BossBarOverlay {
    PROGRESS("Ciągły (Progress)", 0),
    NOTCHED_6("6 Segmentów", 6),
    NOTCHED_10("10 Segmentów", 10),
    NOTCHED_12("12 Segmentów", 12),
    NOTCHED_20("20 Segmentów", 20);

    private final String displayName;
    private final int notches;

    BossBarOverlay(String displayName, int notches) {
        this.displayName = displayName;
        this.notches = notches;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getNotches() {
        return notches;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
