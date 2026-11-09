package pl.zombek.client.core.time;

public enum StopwatchProfileType {
    SPEEDRUN("Speedrun"),
    BUILDING("Budowanie"),
    MINING("Kopanie"),
    CUSTOM("Własny");

    private final String displayName;

    StopwatchProfileType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
