package pl.zombek.client.core.bedwars;

import java.util.*;

/**
 * Central orchestrator for the Bedwars Helper system.
 * Coordinates match detection, scoreboard parsing, chat processing, team states,
 * player rosters, session stats, and event tracking.
 */
public class BedwarsManager {
    private static BedwarsManager instance;

    private final BedwarsGameDetector gameDetector;
    private final BedwarsScoreboardParser scoreboardParser;
    private final BedwarsChatParser chatParser;
    private final BedwarsSessionTracker sessionTracker;
    private final BedwarsEventTracker eventTracker;
    private final BedwarsTimerManager timerManager;

    // Tracked Players (Tablist and My Team)
    private final Map<String, BedwarsPlayer> trackedPlayers = new HashMap<>();

    // Internal tick counter for throttling updates
    private int tickCount = 0;

    public BedwarsManager() {
        this.gameDetector = new BedwarsGameDetector();
        this.scoreboardParser = new BedwarsScoreboardParser();
        this.chatParser = new BedwarsChatParser(this);
        this.sessionTracker = BedwarsSessionTracker.getInstance();
        this.eventTracker = BedwarsEventTracker.getInstance();
        this.timerManager = BedwarsTimerManager.getInstance();
    }

    public static synchronized BedwarsManager getInstance() {
        if (instance == null) {
            instance = new BedwarsManager();
        }
        return instance;
    }

    /**
     * Called on each client game tick (20 ticks/sec).
     */
    public synchronized void onTick() {
        tickCount++;

        // Once per second (every 20 ticks)
        if (tickCount % 20 == 0) {
            timerManager.tickSecond();
            eventTracker.pruneExpiredEvents();
            updateTabListPlayers();
        }
    }

    /**
     * Receives updated scoreboard lines from the active platform adapter.
     */
    public synchronized void onScoreboardUpdate(List<String> rawLines) {
        if (rawLines == null || rawLines.isEmpty()) {
            return;
        }

        String title = rawLines.get(0);
        gameDetector.evaluateScoreboard(title, rawLines);
        scoreboardParser.parseLines(rawLines);
        timerManager.updateFromScoreboardTimers(scoreboardParser.getParsedTimers());

        // Update player's team color if detected
        BedwarsTeamColor detectedColor = scoreboardParser.getPlayerTeamColor();
        if (detectedColor != null) {
            updateSelfTeam(detectedColor);
        }
    }

    /**
     * Receives incoming chat messages from the platform adapter.
     */
    public synchronized void onChatMessage(String rawMessage) {
        if (rawMessage != null) {
            chatParser.onChatMessage(rawMessage);
        }
    }

    /**
     * Synchronizes player list with tablist names from IBedwarsDataProvider.
     */
    private void updateTabListPlayers() {
        IBedwarsDataProvider provider = BedwarsDataProviderRegistry.getProvider();
        if (provider == null || !provider.isPlayerInGame()) {
            return;
        }

        List<String> tabNames = provider.getTabListPlayerNames();
        String selfName = provider.getPlayerName();
        BedwarsTeamColor selfColor = scoreboardParser.getPlayerTeamColor();

        // Mark all as currently not in tablist
        for (BedwarsPlayer p : trackedPlayers.values()) {
            p.setInTabList(false);
        }

        if (tabNames != null) {
            for (String name : tabNames) {
                if (name == null || name.trim().isEmpty()) continue;
                String cleanName = BedwarsScoreboardParser.stripFormatting(name).trim();
                boolean isSelf = cleanName.equalsIgnoreCase(selfName);
                BedwarsPlayer player = trackedPlayers.computeIfAbsent(cleanName, k -> new BedwarsPlayer(cleanName, selfColor, isSelf));
                player.setInTabList(true);
                if (isSelf && selfColor != null) {
                    player.setTeamColor(selfColor);
                }
            }
        }
    }

    private void updateSelfTeam(BedwarsTeamColor color) {
        IBedwarsDataProvider provider = BedwarsDataProviderRegistry.getProvider();
        String selfName = provider != null ? provider.getPlayerName() : null;
        if (selfName != null) {
            BedwarsPlayer self = trackedPlayers.computeIfAbsent(selfName, k -> new BedwarsPlayer(selfName, color, true));
            self.setTeamColor(color);
        }
    }

    /**
     * Returns the list of teammates (players sharing own team color).
     */
    public synchronized List<BedwarsPlayer> getMyTeammates() {
        BedwarsTeamColor myColor = scoreboardParser.getPlayerTeamColor();
        if (myColor == null) {
            return Collections.emptyList();
        }

        List<BedwarsPlayer> teammates = new ArrayList<>();
        for (BedwarsPlayer p : trackedPlayers.values()) {
            if (p.getTeamColor() == myColor) {
                teammates.add(p);
            }
        }
        return Collections.unmodifiableList(teammates);
    }

    /**
     * Total number of alive players across all teams.
     */
    public synchronized int getTotalAlivePlayers() {
        int total = 0;
        boolean anyKnown = false;
        for (BedwarsTeam team : scoreboardParser.getParsedTeams().values()) {
            if (!team.isEliminated()) {
                if (team.getAlivePlayers() > 0) {
                    total += team.getAlivePlayers();
                    anyKnown = true;
                } else if (team.isBedAlive()) {
                    total += 1; // At least 1 if bed is alive
                    anyKnown = true;
                }
            }
        }
        return anyKnown ? total : -1;
    }

    public synchronized void reset() {
        gameDetector.reset();
        scoreboardParser.reset();
        sessionTracker.resetAll();
        eventTracker.clear();
        timerManager.reset();
        trackedPlayers.clear();
    }

    public BedwarsGameDetector getGameDetector() { return gameDetector; }
    public BedwarsScoreboardParser getScoreboardParser() { return scoreboardParser; }
    public BedwarsChatParser getChatParser() { return chatParser; }
    public BedwarsSessionTracker getSessionTracker() { return sessionTracker; }
    public BedwarsEventTracker getEventTracker() { return eventTracker; }
    public BedwarsTimerManager getTimerManager() { return timerManager; }
    public Map<String, BedwarsPlayer> getTrackedPlayers() { return Collections.unmodifiableMap(trackedPlayers); }
}
