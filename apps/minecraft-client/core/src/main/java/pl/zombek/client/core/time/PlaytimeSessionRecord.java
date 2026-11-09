package pl.zombek.client.core.time;

/**
 * Record representing a completed gameplay session for local statistics.
 * Strictly avoids storing private player credentials, passwords, or tokens.
 */
public class PlaytimeSessionRecord {

    private final String sessionId;
    private final long startTimeEpoch;
    private final long endTimeEpoch;
    private final long durationSeconds;
    private final boolean singleplayer;
    private final String worldName;
    private final String serverAddress;

    public PlaytimeSessionRecord(String sessionId,
                                 long startTimeEpoch,
                                 long endTimeEpoch,
                                 long durationSeconds,
                                 boolean singleplayer,
                                 String worldName,
                                 String serverAddress) {
        this.sessionId = sessionId;
        this.startTimeEpoch = startTimeEpoch;
        this.endTimeEpoch = endTimeEpoch;
        this.durationSeconds = durationSeconds;
        this.singleplayer = singleplayer;
        this.worldName = worldName;
        this.serverAddress = serverAddress;
    }

    public String getSessionId() {
        return sessionId;
    }

    public long getStartTimeEpoch() {
        return startTimeEpoch;
    }

    public long getEndTimeEpoch() {
        return endTimeEpoch;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public boolean isSingleplayer() {
        return singleplayer;
    }

    public String getWorldName() {
        return worldName;
    }

    public String getServerAddress() {
        return serverAddress;
    }
}
