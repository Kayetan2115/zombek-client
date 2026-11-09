package pl.zombek.client.core.bedwars;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses incoming chat messages for Bedwars game events: bed destruction, final kills,
 * team elimination, match start, victory, and defeat.
 * Strictly operates on displayed messages without packet interception or chat modification.
 */
public class BedwarsChatParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(BedwarsChatParser.class);

    // Bed Destruction Patterns
    private static final Pattern BED_BROKEN_HYPIXEL = Pattern.compile(
            "(?iu)(?:BED DESTRUCTION > )?(\\S+)\\s+Bed was destroyed by\\s+([^!.]+)"
    );
    private static final Pattern BED_BROKEN_POLISH = Pattern.compile(
            "(?iu)Łóżko drużyny\\s+(\\S+)\\s+zostało zniszczone przez\\s+([^!.]+)"
    );

    // Team Elimination Patterns
    private static final Pattern TEAM_ELIMINATED_HYPIXEL = Pattern.compile(
            "(?iu)(?:TEAM ELIMINATED > )?(\\S+)\\s+Team has been eliminated"
    );
    private static final Pattern TEAM_ELIMINATED_POLISH = Pattern.compile(
            "(?iu)Drużyna\\s+(\\S+)\\s+została wyeliminowana"
    );

    // Final Kill Patterns
    private static final Pattern FINAL_KILL_HYPIXEL = Pattern.compile(
            "(?iu)(?:FINAL KILL > )?(.+?)\\s+(?:was killed by|was knocked into the void by|was struck down by)\\s+(.+?)(?:\\.?\\s*FINAL KILL!?)"
    );
    private static final Pattern FINAL_KILL_EXPLICIT = Pattern.compile(
            "(?iu)FINAL KILL >\\s*(.+?)\\s+was eliminated by\\s+([^!.]+)"
    );
    private static final Pattern FINAL_KILL_POLISH = Pattern.compile(
            "(?iu)(?:OSTATECZNE ZABÓJSTWO > )?(.+?)\\s+został wyeliminowany przez\\s+([^!.]+)"
    );

    // Regular Kill Patterns
    private static final Pattern KILL_HYPIXEL = Pattern.compile(
            "(?iu)(.+?)\\s+(?:was killed by|was knocked into the void by|was struck down by|was thrown into the void by)\\s+([^!.]+)"
    );
    private static final Pattern KILL_POLISH = Pattern.compile(
            "(?iu)(.+?)\\s+został zabity przez\\s+([^!.]+)"
    );

    private boolean enabled = true;
    private String language = "ALL";
    private boolean debugMode = false;
    private String customPatterns = "";

    private final BedwarsManager manager;

    public BedwarsChatParser(BedwarsManager manager) {
        this.manager = manager;
    }

    public synchronized void onChatMessage(String rawMessage) {
        if (!enabled || rawMessage == null || manager == null) {
            return;
        }

        String msg = stripFormatting(rawMessage).trim();
        if (msg.isEmpty()) return;

        if (debugMode) {
            LOGGER.info("[BedwarsChatParser] Incoming line: {}", msg);
        }

        // 1. Bed Destruction
        Matcher mBedEng = BED_BROKEN_HYPIXEL.matcher(msg);
        if (mBedEng.find()) {
            handleBedBroken(mBedEng.group(1), mBedEng.group(2));
            return;
        }
        Matcher mBedPl = BED_BROKEN_POLISH.matcher(msg);
        if (mBedPl.find()) {
            handleBedBroken(mBedPl.group(1), mBedPl.group(2));
            return;
        }

        // 2. Team Elimination
        Matcher mTeamEng = TEAM_ELIMINATED_HYPIXEL.matcher(msg);
        if (mTeamEng.find()) {
            handleTeamEliminated(mTeamEng.group(1));
            return;
        }
        Matcher mTeamPl = TEAM_ELIMINATED_POLISH.matcher(msg);
        if (mTeamPl.find()) {
            handleTeamEliminated(mTeamPl.group(1));
            return;
        }

        // 3. Final Kill
        Matcher mFinal1 = FINAL_KILL_HYPIXEL.matcher(msg);
        if (mFinal1.find()) {
            handleFinalKill(mFinal1.group(1), mFinal1.group(2));
            return;
        }
        Matcher mFinal2 = FINAL_KILL_EXPLICIT.matcher(msg);
        if (mFinal2.find()) {
            handleFinalKill(mFinal2.group(1), mFinal2.group(2));
            return;
        }
        Matcher mFinalPl = FINAL_KILL_POLISH.matcher(msg);
        if (mFinalPl.find()) {
            handleFinalKill(mFinalPl.group(1), mFinalPl.group(2));
            return;
        }

        // 4. Regular Kill
        Matcher mKillEng = KILL_HYPIXEL.matcher(msg);
        if (mKillEng.find()) {
            handleKill(mKillEng.group(1), mKillEng.group(2));
            return;
        }
        Matcher mKillPl = KILL_POLISH.matcher(msg);
        if (mKillPl.find()) {
            handleKill(mKillPl.group(1), mKillPl.group(2));
            return;
        }

        // 5. Match Phase & Victory / Defeat
        String lower = msg.toLowerCase(Locale.ROOT);
        if (lower.contains("the game has started") || lower.contains("gra wystartowała") || lower.contains("the game starts in")) {
            manager.getGameDetector().evaluateChatMessage(rawMessage);
            manager.getEventTracker().addEvent(new BedwarsEvent(BedwarsEventType.GAME_STARTED, "MECZ ROZPOCZĘTY", "Bedwars start", 0xFF55FF55));
        } else if (lower.contains("victory!") || lower.contains("zwycięstwo!")) {
            manager.getGameDetector().evaluateChatMessage(rawMessage);
            manager.getSessionTracker().recordVictory();
            manager.getEventTracker().addEvent(new BedwarsEvent(BedwarsEventType.VICTORY, "ZWYCIĘSTWO!", "Mecz wygrany", 0xFF55FF55));
        } else if (lower.contains("game over!") || lower.contains("koniec gry!")) {
            manager.getGameDetector().evaluateChatMessage(rawMessage);
            manager.getSessionTracker().recordDefeat();
            manager.getEventTracker().addEvent(new BedwarsEvent(BedwarsEventType.DEFEAT, "KONIEC GRY", "Mecz zakończony", 0xFFFF5555));
        } else if (lower.contains("sudden death has begun") || lower.contains("nagła śmierć rozpoczęta")) {
            manager.getGameDetector().evaluateChatMessage(rawMessage);
            manager.getEventTracker().addEvent(new BedwarsEvent(BedwarsEventType.SUDDEN_DEATH, "NAGŁA ŚMIERĆ", "Smoki wkraczają", 0xFFFF55FF));
        }
    }

    private void handleBedBroken(String teamStr, String killerStr) {
        BedwarsTeamColor color = BedwarsTeamColor.fromName(teamStr);
        String killer = killerStr != null ? killerStr.trim() : "Unknown";
        if (color != null) {
            BedwarsTeam team = manager.getScoreboardParser().getParsedTeams().get(color);
            if (team != null) {
                team.setBedStatus(BedStatus.BED_DESTROYED);
                team.setLastKnownEvent("Łóżko zniszczone przez " + killer);
            }

            String selfName = BedwarsDataProviderRegistry.getProvider().getPlayerName();
            if (selfName != null && killer.equalsIgnoreCase(selfName)) {
                manager.getSessionTracker().recordConfirmedBedBroken(color);
            }

            manager.getEventTracker().addEvent(new BedwarsEvent(
                    "bed_broken:" + color.name(),
                    BedwarsEventType.BED_DESTROYED,
                    color.getEnglishName().toUpperCase(Locale.ROOT) + " BED DESTROYED",
                    "Zniszczone przez " + killer,
                    color.getArgb(),
                    System.currentTimeMillis(),
                    "⚑"
            ));
        }
    }

    private void handleTeamEliminated(String teamStr) {
        BedwarsTeamColor color = BedwarsTeamColor.fromName(teamStr);
        if (color != null) {
            BedwarsTeam team = manager.getScoreboardParser().getParsedTeams().get(color);
            if (team != null) {
                team.setBedStatus(BedStatus.TEAM_ELIMINATED);
                team.setAlivePlayers(0);
                team.setLastKnownEvent("Drużyna wyeliminowana");
            }

            manager.getEventTracker().addEvent(new BedwarsEvent(
                    "team_elim:" + color.name(),
                    BedwarsEventType.TEAM_ELIMINATED,
                    color.getEnglishName().toUpperCase(Locale.ROOT) + " ELIMINATED",
                    "Wszyscy gracze wyeliminowani",
                    color.getArgb(),
                    System.currentTimeMillis(),
                    "✕"
            ));
        }
    }

    private void handleFinalKill(String victim, String killer) {
        String selfName = BedwarsDataProviderRegistry.getProvider().getPlayerName();
        boolean isSelfKiller = selfName != null && killer.trim().equalsIgnoreCase(selfName);
        boolean isSelfVictim = selfName != null && victim.trim().equalsIgnoreCase(selfName);

        if (isSelfKiller) {
            manager.getSessionTracker().recordConfirmedFinalKill(victim.trim());
        }
        if (isSelfVictim) {
            manager.getSessionTracker().recordObservedDeath();
        }

        manager.getEventTracker().addEvent(new BedwarsEvent(
                "final_kill:" + victim.trim().toLowerCase(Locale.ROOT),
                BedwarsEventType.FINAL_KILL,
                "FINAL KILL: " + victim.trim(),
                "Zabity przez " + killer.trim(),
                0xFFFF5555,
                System.currentTimeMillis(),
                "⚔"
            ));
    }

    private void handleKill(String victim, String killer) {
        String selfName = BedwarsDataProviderRegistry.getProvider().getPlayerName();
        boolean isSelfKiller = selfName != null && killer.trim().equalsIgnoreCase(selfName);
        boolean isSelfVictim = selfName != null && victim.trim().equalsIgnoreCase(selfName);

        if (isSelfKiller) {
            manager.getSessionTracker().recordObservedKill(victim.trim());
        }
        if (isSelfVictim) {
            manager.getSessionTracker().recordObservedDeath();
        }

        manager.getEventTracker().addEvent(new BedwarsEvent(
                "kill:" + victim.trim().toLowerCase(Locale.ROOT),
                BedwarsEventType.PLAYER_ELIMINATED,
                victim.trim() + " zabity",
                "Przez " + killer.trim(),
                0xFFAAAAAA,
                System.currentTimeMillis(),
                "☠"
            ));
    }

    public static String stripFormatting(String input) {
        if (input == null) return "";
        return input.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language != null ? language : "ALL"; }
    public boolean isDebugMode() { return debugMode; }
    public void setDebugMode(boolean debugMode) { this.debugMode = debugMode; }
    public String getCustomPatterns() { return customPatterns; }
    public void setCustomPatterns(String patterns) { this.customPatterns = patterns != null ? patterns : ""; }
}
