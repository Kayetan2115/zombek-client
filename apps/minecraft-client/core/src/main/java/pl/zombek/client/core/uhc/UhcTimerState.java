package pl.zombek.client.core.uhc;

public enum UhcTimerState {
    STOPPED("Stopped"),
    RUNNING("Running"),
    PAUSED("Paused"),
    EXPIRED("Expired");

    private final String displayName;

    UhcTimerState(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
