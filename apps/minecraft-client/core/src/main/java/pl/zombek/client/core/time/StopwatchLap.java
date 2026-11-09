package pl.zombek.client.core.time;

/**
 * Value object representing an individual recorded stopwatch lap.
 */
public class StopwatchLap {

    private final int lapNumber;
    private final long lapTimeNanos;
    private final long totalTimeNanos;
    private final long deltaNanos;

    public StopwatchLap(int lapNumber, long lapTimeNanos, long totalTimeNanos, long deltaNanos) {
        this.lapNumber = lapNumber;
        this.lapTimeNanos = lapTimeNanos;
        this.totalTimeNanos = totalTimeNanos;
        this.deltaNanos = deltaNanos;
    }

    public int getLapNumber() {
        return lapNumber;
    }

    public long getLapTimeNanos() {
        return lapTimeNanos;
    }

    public long getTotalTimeNanos() {
        return totalTimeNanos;
    }

    public long getDeltaNanos() {
        return deltaNanos;
    }

    /**
     * Formats the lap duration as MM:SS.cc or HH:MM:SS.cc.
     */
    public String getFormattedLapTime(boolean showMillis, boolean showHours) {
        return formatNanos(lapTimeNanos, showMillis, showHours);
    }

    /**
     * Formats the delta against previous lap (+SS.cc or -SS.cc).
     */
    public String getFormattedDelta() {
        if (lapNumber == 1 || deltaNanos == 0L) {
            return "±0.00";
        }
        boolean positive = deltaNanos > 0;
        long absNanos = Math.abs(deltaNanos);
        long seconds = absNanos / 1_000_000_000L;
        long millis = (absNanos / 10_000_000L) % 100;
        return String.format("%s%d.%02d", positive ? "+" : "-", seconds, millis);
    }

    public static String formatNanos(long nanos, boolean showMillis, boolean showHours) {
        if (nanos < 0) nanos = 0;
        long totalSeconds = nanos / 1_000_000_000L;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        long hundredths = (nanos / 10_000_000L) % 100;

        StringBuilder sb = new StringBuilder();
        if (showHours || hours > 0) {
            sb.append(String.format("%02d:", hours));
        }
        sb.append(String.format("%02d:%02d", minutes, seconds));
        if (showMillis) {
            sb.append(String.format(".%02d", hundredths));
        }
        return sb.toString();
    }
}
