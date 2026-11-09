package pl.zombek.client.core.time;

/**
 * Summary metrics computed from local playtime history.
 */
public class PlaytimeStats {

    private final long totalClientPlaytimeSeconds;
    private final long averageSessionSeconds;
    private final long longestSessionSeconds;
    private final int sessionCount;
    private final long currentSessionSeconds;

    public PlaytimeStats(long totalClientPlaytimeSeconds,
                         long averageSessionSeconds,
                         long longestSessionSeconds,
                         int sessionCount,
                         long currentSessionSeconds) {
        this.totalClientPlaytimeSeconds = totalClientPlaytimeSeconds;
        this.averageSessionSeconds = averageSessionSeconds;
        this.longestSessionSeconds = longestSessionSeconds;
        this.sessionCount = sessionCount;
        this.currentSessionSeconds = currentSessionSeconds;
    }

    public long getTotalClientPlaytimeSeconds() {
        return totalClientPlaytimeSeconds;
    }

    public long getAverageSessionSeconds() {
        return averageSessionSeconds;
    }

    public long getLongestSessionSeconds() {
        return longestSessionSeconds;
    }

    public int getSessionCount() {
        return sessionCount;
    }

    public long getCurrentSessionSeconds() {
        return currentSessionSeconds;
    }

    public static String formatDuration(long seconds, boolean showSeconds, boolean humanReadable) {
        if (seconds < 0) seconds = 0;
        long days = seconds / 86400L;
        long hours = (seconds % 86400L) / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long sec = seconds % 60L;

        if (humanReadable) {
            StringBuilder sb = new StringBuilder();
            if (days > 0) sb.append(days).append("d ");
            if (hours > 0 || days > 0) sb.append(hours).append("h ");
            sb.append(minutes).append("m");
            if (showSeconds) {
                sb.append(" ").append(sec).append("s");
            }
            return sb.toString();
        }

        if (days > 0) {
            if (showSeconds) {
                return String.format("%dd %02d:%02d:%02d", days, hours, minutes, sec);
            } else {
                return String.format("%dd %02d:%02d", days, hours, minutes);
            }
        } else {
            if (showSeconds) {
                return String.format("%02d:%02d:%02d", hours, minutes, sec);
            } else {
                return String.format("%02d:%02d", hours, minutes);
            }
        }
    }
}
