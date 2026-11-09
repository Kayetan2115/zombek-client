package pl.zombek.client.core.time;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages monotonic timing state, duration calculations, and lap records
 * for a named stopwatch profile.
 */
public class StopwatchProfile {

    public static final int MAX_STORED_LAPS = 50;

    private final String name;
    private final StopwatchProfileType type;
    private ITimeSource timeSource;

    private StopwatchState state = StopwatchState.STOPPED;
    private long startNanos = 0L;
    private long elapsedNanosBeforePause = 0L;

    private final List<StopwatchLap> laps = new ArrayList<>();
    private long lastLapTotalNanos = 0L;

    public StopwatchProfile(String name, StopwatchProfileType type, ITimeSource timeSource) {
        this.name = name;
        this.type = type != null ? type : StopwatchProfileType.CUSTOM;
        this.timeSource = timeSource != null ? timeSource : new SystemTimeSource();
    }

    public synchronized void setTimeSource(ITimeSource timeSource) {
        this.timeSource = timeSource != null ? timeSource : new SystemTimeSource();
    }

    public String getName() {
        return name;
    }

    public StopwatchProfileType getType() {
        return type;
    }

    public synchronized StopwatchState getState() {
        return state;
    }

    public synchronized boolean isRunning() {
        return state == StopwatchState.RUNNING;
    }

    public synchronized boolean isPaused() {
        return state == StopwatchState.PAUSED;
    }

    public synchronized boolean isStopped() {
        return state == StopwatchState.STOPPED;
    }

    public synchronized void start() {
        if (state == StopwatchState.PAUSED) {
            resume();
            return;
        }
        if (state == StopwatchState.STOPPED) {
            this.startNanos = timeSource.getMonotonicNanos();
            this.elapsedNanosBeforePause = 0L;
            this.lastLapTotalNanos = 0L;
            this.state = StopwatchState.RUNNING;
        }
    }

    public synchronized void pause() {
        if (state == StopwatchState.RUNNING) {
            this.elapsedNanosBeforePause += (timeSource.getMonotonicNanos() - startNanos);
            this.state = StopwatchState.PAUSED;
        }
    }

    public synchronized void resume() {
        if (state == StopwatchState.PAUSED) {
            this.startNanos = timeSource.getMonotonicNanos();
            this.state = StopwatchState.RUNNING;
        }
    }

    public synchronized void toggle() {
        if (state == StopwatchState.RUNNING) {
            pause();
        } else {
            start();
        }
    }

    public synchronized void reset() {
        this.state = StopwatchState.STOPPED;
        this.startNanos = 0L;
        this.elapsedNanosBeforePause = 0L;
        this.lastLapTotalNanos = 0L;
    }

    public synchronized StopwatchLap recordLap() {
        long currentTotal = getElapsedNanos();
        if (currentTotal <= 0L) {
            return null;
        }

        long lapDuration = currentTotal - lastLapTotalNanos;
        if (lapDuration <= 0L) {
            lapDuration = 0L;
        }

        long previousLapDuration = 0L;
        if (!laps.isEmpty()) {
            previousLapDuration = laps.get(laps.size() - 1).getLapTimeNanos();
        }

        long delta = (!laps.isEmpty()) ? (lapDuration - previousLapDuration) : 0L;
        int nextLapNumber = laps.size() + 1;

        StopwatchLap lap = new StopwatchLap(nextLapNumber, lapDuration, currentTotal, delta);
        if (laps.size() >= MAX_STORED_LAPS) {
            laps.remove(0); // Bound memory
        }
        laps.add(lap);
        lastLapTotalNanos = currentTotal;
        return lap;
    }

    public synchronized void clearLaps() {
        laps.clear();
        lastLapTotalNanos = 0L;
    }

    public synchronized List<StopwatchLap> getLaps() {
        return Collections.unmodifiableList(new ArrayList<>(laps));
    }

    public synchronized long getElapsedNanos() {
        if (state == StopwatchState.STOPPED) {
            return 0L;
        }
        if (state == StopwatchState.PAUSED) {
            return elapsedNanosBeforePause;
        }
        return elapsedNanosBeforePause + (timeSource.getMonotonicNanos() - startNanos);
    }

    public synchronized StopwatchLap getBestLap() {
        if (laps.isEmpty()) return null;
        StopwatchLap best = laps.get(0);
        for (StopwatchLap lap : laps) {
            if (lap.getLapTimeNanos() < best.getLapTimeNanos()) {
                best = lap;
            }
        }
        return best;
    }

    public synchronized long getAverageLapNanos() {
        if (laps.isEmpty()) return 0L;
        long sum = 0L;
        for (StopwatchLap lap : laps) {
            sum += lap.getLapTimeNanos();
        }
        return sum / laps.size();
    }

    public synchronized String getFormattedElapsed(boolean showMillis, boolean showHours) {
        return StopwatchLap.formatNanos(getElapsedNanos(), showMillis, showHours);
    }

    // State restoration methods for persistence
    public synchronized void restoreState(StopwatchState state, long elapsedNanos) {
        this.state = state != null ? state : StopwatchState.STOPPED;
        this.elapsedNanosBeforePause = elapsedNanos;
        if (this.state == StopwatchState.RUNNING) {
            this.startNanos = timeSource.getMonotonicNanos();
        } else {
            this.startNanos = 0L;
        }
        this.lastLapTotalNanos = elapsedNanos;
    }
}
