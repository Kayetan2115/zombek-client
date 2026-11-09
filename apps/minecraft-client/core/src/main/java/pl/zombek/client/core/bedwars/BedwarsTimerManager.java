package pl.zombek.client.core.bedwars;

import java.util.*;

/**
 * Manages active match timers, upgrade countdowns, bed destruction, and sudden death.
 * Synchronizes with visible scoreboard timers with fallback local tick countdowns.
 */
public class BedwarsTimerManager {
    private static BedwarsTimerManager instance;

    private final BedwarsTimer matchTimer;
    private final BedwarsTimer startCountdownTimer;
    private final BedwarsTimer bedDestructionTimer;
    private final BedwarsTimer suddenDeathTimer;
    private final List<BedwarsTimer> upgradeTimers = new ArrayList<>();

    public BedwarsTimerManager() {
        this.matchTimer = BedwarsTimer.createElapsed("match_time", "Czas meczu");
        this.startCountdownTimer = new BedwarsTimer("start_countdown", "Start gry", 0, 0, TimerState.UNKNOWN, false);
        this.bedDestructionTimer = new BedwarsTimer("bed_destruction", "Zniszczenie łóżek", 0, 0, TimerState.UNKNOWN, false);
        this.suddenDeathTimer = new BedwarsTimer("sudden_death", "Nagła śmierć", 0, 0, TimerState.UNKNOWN, false);
    }

    public static synchronized BedwarsTimerManager getInstance() {
        if (instance == null) {
            instance = new BedwarsTimerManager();
        }
        return instance;
    }

    /**
     * Updates internal timer values based on timers detected by the scoreboard parser.
     */
    public synchronized void updateFromScoreboardTimers(List<BedwarsTimer> parsedTimers) {
        if (parsedTimers == null) return;

        upgradeTimers.clear();
        for (BedwarsTimer timer : parsedTimers) {
            String nameLower = timer.getName().toLowerCase(Locale.ROOT);
            if (nameLower.contains("bed") || nameLower.contains("łóż")) {
                bedDestructionTimer.setName(timer.getName());
                bedDestructionTimer.setRemainingSeconds(timer.getRemainingSeconds());
                bedDestructionTimer.setTotalSeconds(Math.max(bedDestructionTimer.getTotalSeconds(), timer.getRemainingSeconds()));
                bedDestructionTimer.setState(TimerState.RUNNING);
            } else if (nameLower.contains("sudden death") || nameLower.contains("dragon") || nameLower.contains("nagła śmierć")) {
                suddenDeathTimer.setName(timer.getName());
                suddenDeathTimer.setRemainingSeconds(timer.getRemainingSeconds());
                suddenDeathTimer.setTotalSeconds(Math.max(suddenDeathTimer.getTotalSeconds(), timer.getRemainingSeconds()));
                suddenDeathTimer.setState(TimerState.RUNNING);
            } else if (nameLower.contains("diamond") || nameLower.contains("emerald") || nameLower.contains("diament") || nameLower.contains("szmaragd")) {
                upgradeTimers.add(timer);
            }
        }
    }

    /**
     * Ticks local timers once per second.
     */
    public synchronized void tickSecond() {
        matchTimer.tick();
        if (startCountdownTimer.getState() == TimerState.RUNNING) {
            startCountdownTimer.tick();
        }
        if (bedDestructionTimer.getState() == TimerState.RUNNING) {
            bedDestructionTimer.tick();
        }
        if (suddenDeathTimer.getState() == TimerState.RUNNING) {
            suddenDeathTimer.tick();
        }
        for (BedwarsTimer upgrade : upgradeTimers) {
            if (upgrade.getState() == TimerState.RUNNING) {
                upgrade.tick();
            }
        }
    }

    public synchronized void reset() {
        matchTimer.setRemainingSeconds(0);
        matchTimer.setState(TimerState.RUNNING);
        startCountdownTimer.setRemainingSeconds(0);
        startCountdownTimer.setState(TimerState.UNKNOWN);
        bedDestructionTimer.setRemainingSeconds(0);
        bedDestructionTimer.setState(TimerState.UNKNOWN);
        suddenDeathTimer.setRemainingSeconds(0);
        suddenDeathTimer.setState(TimerState.UNKNOWN);
        upgradeTimers.clear();
    }

    public BedwarsTimer getMatchTimer() {
        return matchTimer;
    }

    public BedwarsTimer getStartCountdownTimer() {
        return startCountdownTimer;
    }

    public BedwarsTimer getBedDestructionTimer() {
        return bedDestructionTimer;
    }

    public BedwarsTimer getSuddenDeathTimer() {
        return suddenDeathTimer;
    }

    public List<BedwarsTimer> getUpgradeTimers() {
        return Collections.unmodifiableList(upgradeTimers);
    }
}
