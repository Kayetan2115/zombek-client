package pl.zombek.client.core.bedwars;

import java.util.*;

/**
 * Tracks match and session statistics for the local player in Bedwars.
 * Implements strict deduplication to avoid double-counting across callbacks and chat.
 */
public class BedwarsSessionTracker {
    private static BedwarsSessionTracker instance;

    private int observedKills = 0;
    private int confirmedFinalKills = 0;
    private int observedDeaths = 0;
    private int confirmedBedsBroken = 0;
    private int confirmedWins = 0;
    private int confirmedLosses = 0;
    private long sessionStartTime = 0L;
    private boolean autoResetOnNewMatch = true;
    private int maxStoredSessions = 10;

    // Deduplication window: Map of event signature -> timestampMs
    private final Map<String, Long> deduplicationCache = new HashMap<>();
    private static final long DEDUP_WINDOW_MS = 3000L;

    // Match history
    private final List<BedwarsMatchSummary> matchHistory = new ArrayList<>();

    public BedwarsSessionTracker() {
        this.sessionStartTime = System.currentTimeMillis();
    }

    public static synchronized BedwarsSessionTracker getInstance() {
        if (instance == null) {
            instance = new BedwarsSessionTracker();
        }
        return instance;
    }

    public synchronized void recordObservedKill(String targetName) {
        String signature = "kill:" + (targetName != null ? targetName.toLowerCase(Locale.ROOT) : "unknown");
        if (isDuplicate(signature)) {
            return;
        }
        this.observedKills++;
    }

    public synchronized void recordConfirmedFinalKill(String targetName) {
        String signature = "final_kill:" + (targetName != null ? targetName.toLowerCase(Locale.ROOT) : "unknown");
        if (isDuplicate(signature)) {
            return;
        }
        this.confirmedFinalKills++;
        // Final kills are also counted in total kills
        this.observedKills++;
    }

    public synchronized void recordObservedDeath() {
        String signature = "death:" + (System.currentTimeMillis() / 2000L);
        if (isDuplicate(signature)) {
            return;
        }
        this.observedDeaths++;
    }

    public synchronized void recordConfirmedBedBroken(BedwarsTeamColor team) {
        String signature = "bed_broken:" + (team != null ? team.name() : "unknown");
        if (isDuplicate(signature)) {
            return;
        }
        this.confirmedBedsBroken++;
    }

    public synchronized void recordVictory() {
        String signature = "game_victory:" + (System.currentTimeMillis() / 10000L);
        if (isDuplicate(signature)) {
            return;
        }
        this.confirmedWins++;
        archiveCurrentMatch(true);
    }

    public synchronized void recordDefeat() {
        String signature = "game_defeat:" + (System.currentTimeMillis() / 10000L);
        if (isDuplicate(signature)) {
            return;
        }
        this.confirmedLosses++;
        archiveCurrentMatch(false);
    }

    private boolean isDuplicate(String signature) {
        long now = System.currentTimeMillis();
        pruneDeduplicationCache(now);
        Long lastTime = deduplicationCache.get(signature);
        if (lastTime != null && (now - lastTime) < DEDUP_WINDOW_MS) {
            return true;
        }
        deduplicationCache.put(signature, now);
        return false;
    }

    private void pruneDeduplicationCache(long now) {
        deduplicationCache.entrySet().removeIf(entry -> (now - entry.getValue()) > DEDUP_WINDOW_MS * 2);
    }

    public synchronized void resetCurrentMatch() {
        this.observedKills = 0;
        this.confirmedFinalKills = 0;
        this.observedDeaths = 0;
        this.confirmedBedsBroken = 0;
        this.sessionStartTime = System.currentTimeMillis();
        this.deduplicationCache.clear();
    }

    public synchronized void resetAll() {
        resetCurrentMatch();
        this.confirmedWins = 0;
        this.confirmedLosses = 0;
        this.matchHistory.clear();
    }

    private void archiveCurrentMatch(boolean victory) {
        long duration = getSessionDurationSeconds();
        BedwarsMatchSummary summary = new BedwarsMatchSummary(
                victory, observedKills, confirmedFinalKills, observedDeaths, confirmedBedsBroken, duration, System.currentTimeMillis()
        );
        matchHistory.add(summary);
        while (matchHistory.size() > maxStoredSessions) {
            matchHistory.remove(0);
        }
        if (autoResetOnNewMatch) {
            resetCurrentMatch();
        }
    }

    public long getSessionDurationSeconds() {
        if (sessionStartTime <= 0L) return 0L;
        return Math.max(0L, (System.currentTimeMillis() - sessionStartTime) / 1000L);
    }

    public String getFormattedSessionTime() {
        long totalSec = getSessionDurationSeconds();
        long min = totalSec / 60;
        long sec = totalSec % 60;
        return String.format("%02d:%02d", min, sec);
    }

    public int getObservedKills() { return observedKills; }
    public int getConfirmedFinalKills() { return confirmedFinalKills; }
    public int getObservedDeaths() { return observedDeaths; }
    public int getConfirmedBedsBroken() { return confirmedBedsBroken; }
    public int getConfirmedWins() { return confirmedWins; }
    public int getConfirmedLosses() { return confirmedLosses; }
    public boolean isAutoResetOnNewMatch() { return autoResetOnNewMatch; }
    public void setAutoResetOnNewMatch(boolean autoReset) { this.autoResetOnNewMatch = autoReset; }
    public int getMaxStoredSessions() { return maxStoredSessions; }
    public void setMaxStoredSessions(int max) { this.maxStoredSessions = Math.max(1, max); }
    public List<BedwarsMatchSummary> getMatchHistory() { return Collections.unmodifiableList(matchHistory); }

    public static class BedwarsMatchSummary {
        private final boolean victory;
        private final int kills;
        private final int finalKills;
        private final int deaths;
        private final int bedsBroken;
        private final long durationSeconds;
        private final long timestamp;

        public BedwarsMatchSummary(boolean victory, int kills, int finalKills, int deaths, int bedsBroken, long durationSeconds, long timestamp) {
            this.victory = victory;
            this.kills = kills;
            this.finalKills = finalKills;
            this.deaths = deaths;
            this.bedsBroken = bedsBroken;
            this.durationSeconds = durationSeconds;
            this.timestamp = timestamp;
        }

        public boolean isVictory() { return victory; }
        public int getKills() { return kills; }
        public int getFinalKills() { return finalKills; }
        public int getDeaths() { return deaths; }
        public int getBedsBroken() { return bedsBroken; }
        public long getDurationSeconds() { return durationSeconds; }
        public long getTimestamp() { return timestamp; }
    }
}
