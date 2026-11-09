package pl.zombek.client.core.bedwars;

/**
 * Model representing a countdown or elapsed timer in a Bedwars match.
 */
public class BedwarsTimer {
    private final String id;
    private String name;
    private int remainingSeconds;
    private int totalSeconds;
    private TimerState state;
    private boolean isCountUp;

    public BedwarsTimer(String id, String name, int remainingSeconds, int totalSeconds, TimerState state, boolean isCountUp) {
        this.id = id;
        this.name = name;
        this.remainingSeconds = Math.max(0, remainingSeconds);
        this.totalSeconds = Math.max(0, totalSeconds);
        this.state = state != null ? state : TimerState.AVAILABLE;
        this.isCountUp = isCountUp;
    }

    public BedwarsTimer(String id, String name, int seconds) {
        this(id, name, seconds, seconds, TimerState.RUNNING, false);
    }

    public static BedwarsTimer createElapsed(String id, String name) {
        return new BedwarsTimer(id, name, 0, 0, TimerState.RUNNING, true);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(int remainingSeconds) {
        this.remainingSeconds = Math.max(0, remainingSeconds);
    }

    public int getTotalSeconds() {
        return totalSeconds;
    }

    public void setTotalSeconds(int totalSeconds) {
        this.totalSeconds = Math.max(0, totalSeconds);
    }

    public TimerState getState() {
        return state;
    }

    public void setState(TimerState state) {
        this.state = state;
    }

    public boolean isCountUp() {
        return isCountUp;
    }

    public void tick() {
        if (state != TimerState.RUNNING) {
            return;
        }
        if (isCountUp) {
            remainingSeconds++;
        } else {
            if (remainingSeconds > 0) {
                remainingSeconds--;
                if (remainingSeconds == 0) {
                    state = TimerState.FINISHED;
                }
            } else {
                state = TimerState.FINISHED;
            }
        }
    }

    public String getFormattedTime() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public float getProgress() {
        if (isCountUp || totalSeconds <= 0) {
            return 1.0f;
        }
        return Math.max(0.0f, Math.min(1.0f, (float) remainingSeconds / (float) totalSeconds));
    }
}
