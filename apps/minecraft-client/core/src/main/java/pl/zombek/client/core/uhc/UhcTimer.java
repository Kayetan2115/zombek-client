package pl.zombek.client.core.uhc;

/**
 * High-precision timer for UHC events based on monotonic time (System.nanoTime).
 * Supports both count-up (Game time) and countdown (PvP, Border, Meetup, Deathmatch).
 */
public class UhcTimer {
    private final UhcTimerType type;
    private String name;
    private boolean countdown;
    private long initialDurationSeconds;

    private UhcTimerState state = UhcTimerState.STOPPED;
    private long startNanoTime = 0L;
    private long elapsedNanosBeforePause = 0L;

    public UhcTimer(UhcTimerType type) {
        this.type = type;
        this.name = type.getDisplayName();
        this.countdown = type.isCountdown();
        this.initialDurationSeconds = type.getDefaultDurationSeconds();
    }

    public UhcTimer(String name, boolean countdown, long initialDurationSeconds) {
        this.type = UhcTimerType.CUSTOM;
        this.name = name;
        this.countdown = countdown;
        this.initialDurationSeconds = initialDurationSeconds;
    }

    public synchronized void start() {
        if (state == UhcTimerState.PAUSED) {
            resume();
            return;
        }
        this.startNanoTime = System.nanoTime();
        this.elapsedNanosBeforePause = 0L;
        this.state = UhcTimerState.RUNNING;
    }

    public synchronized void pause() {
        if (this.state == UhcTimerState.RUNNING) {
            this.elapsedNanosBeforePause += (System.nanoTime() - this.startNanoTime);
            this.state = UhcTimerState.PAUSED;
        }
    }

    public synchronized void resume() {
        if (this.state == UhcTimerState.PAUSED) {
            this.startNanoTime = System.nanoTime();
            this.state = UhcTimerState.RUNNING;
        }
    }

    public synchronized void stop() {
        this.state = UhcTimerState.STOPPED;
        this.elapsedNanosBeforePause = 0L;
        this.startNanoTime = 0L;
    }

    public synchronized void reset() {
        this.state = UhcTimerState.STOPPED;
        this.elapsedNanosBeforePause = 0L;
        this.startNanoTime = 0L;
    }

    public synchronized long getElapsedSeconds() {
        if (this.state == UhcTimerState.STOPPED) {
            return 0L;
        }
        long totalNanos = this.elapsedNanosBeforePause;
        if (this.state == UhcTimerState.RUNNING) {
            totalNanos += (System.nanoTime() - this.startNanoTime);
        }
        return totalNanos / 1_000_000_000L;
    }

    public synchronized long getRemainingSeconds() {
        if (!this.countdown) {
            return 0L;
        }
        long elapsed = getElapsedSeconds();
        long rem = this.initialDurationSeconds - elapsed;
        if (rem <= 0) {
            if (this.state == UhcTimerState.RUNNING) {
                this.state = UhcTimerState.EXPIRED;
            }
            return 0L;
        }
        return rem;
    }

    public synchronized boolean isExpired() {
        if (this.state == UhcTimerState.EXPIRED) return true;
        if (this.countdown && this.state == UhcTimerState.RUNNING) {
            return getRemainingSeconds() <= 0;
        }
        return false;
    }

    public synchronized String getFormattedTime() {
        long sec = this.countdown ? getRemainingSeconds() : getElapsedSeconds();
        return formatTime(sec);
    }

    public static String formatTime(long sec) {
        long hours = sec / 3600;
        long minutes = (sec % 3600) / 60;
        long seconds = sec % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    // Getters and Setters
    public UhcTimerType getType() { return type; }
    public synchronized String getName() { return name; }
    public synchronized void setName(String name) { this.name = name; }
    public synchronized boolean isCountdown() { return countdown; }
    public synchronized void setCountdown(boolean countdown) { this.countdown = countdown; }
    public synchronized long getInitialDurationSeconds() { return initialDurationSeconds; }
    public synchronized void setInitialDurationSeconds(long seconds) { this.initialDurationSeconds = Math.max(0, seconds); }
    public synchronized UhcTimerState getState() { return state; }
}
