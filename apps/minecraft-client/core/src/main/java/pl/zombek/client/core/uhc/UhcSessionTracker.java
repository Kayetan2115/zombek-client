package pl.zombek.client.core.uhc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Coordinates a live UHC match session, including timers, match mode,
 * player status, item counts, scoreboard state, and chat-based event triggers.
 * Thread-safe singleton.
 */
public class UhcSessionTracker {
    private static final Logger LOGGER = LoggerFactory.getLogger(UhcSessionTracker.class);
    private static final UhcSessionTracker INSTANCE = new UhcSessionTracker();

    private UhcMode mode = UhcMode.SOLO;
    private long sessionStartTime = 0L;
    private boolean sessionActive = false;

    private float playerHealth = 20.0f;
    private float playerAbsorption = 0.0f;
    private int playerHunger = 20;
    private float playerSaturation = 5.0f;

    private final Map<UhcTrackedItem, Integer> itemCounts = new ConcurrentHashMap<>();
    private final Map<UhcTimerType, UhcTimer> timers = new EnumMap<>(UhcTimerType.class);
    private final UhcScoreboardParser scoreboardParser;
    private final UhcChatParser chatParser;

    private UhcSessionTracker() {
        for (UhcTimerType type : UhcTimerType.values()) {
            this.timers.put(type, new UhcTimer(type));
        }
        for (UhcTrackedItem item : UhcTrackedItem.values()) {
            this.itemCounts.put(item, 0);
        }
        this.scoreboardParser = new UhcScoreboardParser();
        this.chatParser = new UhcChatParser(this);
    }

    public static UhcSessionTracker getInstance() {
        return INSTANCE;
    }

    public synchronized void startSession() {
        this.sessionStartTime = System.currentTimeMillis();
        this.sessionActive = true;
        getTimer(UhcTimerType.GAME).start();
        LOGGER.info("[UHC] Rozpoczęto nową sesję UHC (Tryb: {}).", this.mode);
    }

    public synchronized void startSession(UhcMode mode) {
        if (mode != null) {
            this.mode = mode;
        }
        startSession();
    }

    public synchronized void resetSession() {
        this.sessionActive = false;
        this.sessionStartTime = 0L;
        for (UhcTimer timer : this.timers.values()) {
            timer.reset();
        }
        this.scoreboardParser.reset();
        this.playerHealth = 20.0f;
        this.playerAbsorption = 0.0f;
        this.playerHunger = 20;
        this.playerSaturation = 5.0f;
        for (UhcTrackedItem item : UhcTrackedItem.values()) {
            this.itemCounts.put(item, 0);
        }
        LOGGER.info("[UHC] Zresetowano sesję UHC.");
    }

    public void reset() {
        resetSession();
    }

    public void tick() {
        // Heartbeat for live updates
    }

    public synchronized void updatePlayerStatus(float health, float absorption, int hunger, float saturation) {
        this.playerHealth = health;
        this.playerAbsorption = absorption;
        this.playerHunger = hunger;
        this.playerSaturation = saturation;
    }

    public float getPlayerHealth() {
        return playerHealth;
    }

    public float getPlayerAbsorption() {
        return playerAbsorption;
    }

    public int getPlayerHunger() {
        return playerHunger;
    }

    public float getPlayerSaturation() {
        return playerSaturation;
    }

    public void setTrackedItemCount(UhcTrackedItem item, int count) {
        if (item != null) {
            this.itemCounts.put(item, Math.max(0, count));
        }
    }

    public int getTrackedItemCount(UhcTrackedItem item) {
        if (item == null) return 0;
        return this.itemCounts.getOrDefault(item, 0);
    }

    public String getFormattedSessionTime() {
        long seconds = getSessionDurationSeconds();
        long m = seconds / 60;
        long s = seconds % 60;
        return String.format("%02d:%02d", m, s);
    }

    public synchronized void onMatchStart() {
        if (!sessionActive) {
            startSession();
        } else {
            getTimer(UhcTimerType.GAME).start();
        }
        getTimer(UhcTimerType.PVP).start();
        getTimer(UhcTimerType.BORDER).start();
    }

    public synchronized void onPvpEnabled() {
        UhcTimer pvp = getTimer(UhcTimerType.PVP);
        pvp.stop();
        LOGGER.info("[UHC] Faza PvP aktywna.");
    }

    public synchronized void onBorderShrinking() {
        UhcTimer border = getTimer(UhcTimerType.BORDER);
        border.start();
        LOGGER.info("[UHC] Granica świata w ruchu.");
    }

    public synchronized void onDeathmatch() {
        getTimer(UhcTimerType.DEATHMATCH).start();
        LOGGER.info("[UHC] Faza Deathmatch wystartowała.");
    }

    public synchronized void onGameOver() {
        for (UhcTimer timer : this.timers.values()) {
            timer.pause();
        }
        LOGGER.info("[UHC] Mecz zakończony.");
    }

    public synchronized long getSessionDurationSeconds() {
        if (!sessionActive || sessionStartTime == 0L) {
            return 0L;
        }
        return (System.currentTimeMillis() - sessionStartTime) / 1000L;
    }

    public UhcTimer getTimer(UhcTimerType type) {
        return timers.get(type);
    }

    public Map<UhcTimerType, UhcTimer> getTimers() {
        return timers;
    }

    public UhcScoreboardParser getScoreboardParser() {
        return scoreboardParser;
    }

    public UhcChatParser getChatParser() {
        return chatParser;
    }

    public synchronized UhcMode getMode() {
        return mode;
    }

    public synchronized void setMode(UhcMode mode) {
        if (mode != null) {
            this.mode = mode;
        }
    }

    public synchronized boolean isSessionActive() {
        return sessionActive;
    }
}
