package pl.zombek.client.core.hypixel;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts and displays player levels from already visible in-game text (tablist or scoreboard).
 * Strictly complies with Hypixel Fair Play safety policies:
 * - Disabled by default on Hypixel Network.
 * - Locked in Strict Safety mode.
 * - Operates purely on locally received data without polling any external API.
 */
public class HypixelLevelDisplay {
    private static HypixelLevelDisplay instance;

    // Matches standard network level tags: e.g. "[120★]", "[45✫]", "[80]"
    private static final Pattern LEVEL_TAG_PATTERN = Pattern.compile(
            "(?iu)\\[([0-9]{1,4})[★✫*]?\\]"
    );

    private boolean enabled = false;
    private boolean displayAbovePlayer = false;
    private boolean displayInTab = true;
    private int levelTextColor = 0xFFFFAA00;
    private int numberColor = 0xFFFFFFFF;
    private boolean background = true;
    private float opacity = 0.8f;
    private float scale = 1.0f;

    // Cache of player name -> detected level string
    private final Map<String, Integer> playerLevelCache = new HashMap<>();

    public HypixelLevelDisplay() {}

    public static synchronized HypixelLevelDisplay getInstance() {
        if (instance == null) {
            instance = new HypixelLevelDisplay();
        }
        return instance;
    }

    /**
     * Checks if Level Display is permitted in the current server environment.
     * On Hypixel Network, it is locked in Strict Safety mode.
     */
    public boolean isLevelDisplayPermitted() {
        if (HypixelServerDetector.getInstance().isHypixelConnected()) {
            SafetyPreset preset = HypixelSafetyController.getInstance().getPreset();
            if (preset == SafetyPreset.STRICT_SAFETY) {
                return false;
            }
        }
        return true;
    }

    /**
     * Extracts an integer level from a formatted name or scoreboard line.
     */
    public Integer extractLevelFromText(String rawText) {
        if (rawText == null || rawText.isEmpty()) {
            return null;
        }
        String clean = rawText.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
        Matcher m = LEVEL_TAG_PATTERN.matcher(clean);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    public synchronized void recordPlayerLevel(String playerName, int level) {
        if (playerName != null && !playerName.isEmpty()) {
            playerLevelCache.put(playerName.toLowerCase(), level);
        }
    }

    public synchronized Integer getPlayerLevel(String playerName) {
        if (playerName == null) return null;
        return playerLevelCache.get(playerName.toLowerCase());
    }

    public synchronized void clearCache() {
        playerLevelCache.clear();
    }

    // Getters and Setters
    public boolean isEnabled() { return enabled && isLevelDisplayPermitted(); }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public boolean isDisplayAbovePlayer() { return displayAbovePlayer; }
    public void setDisplayAbovePlayer(boolean display) { this.displayAbovePlayer = display; }
    public boolean isDisplayInTab() { return displayInTab; }
    public void setDisplayInTab(boolean display) { this.displayInTab = display; }
    public int getLevelTextColor() { return levelTextColor; }
    public void setLevelTextColor(int color) { this.levelTextColor = color; }
    public int getNumberColor() { return numberColor; }
    public void setNumberColor(int color) { this.numberColor = color; }
    public boolean isBackground() { return background; }
    public void setBackground(boolean background) { this.background = background; }
    public float getOpacity() { return opacity; }
    public void setOpacity(float opacity) { this.opacity = Math.max(0.1f, Math.min(1.0f, opacity)); }
    public float getScale() { return scale; }
    public void setScale(float scale) { this.scale = Math.max(0.5f, Math.min(2.0f, scale)); }
}
