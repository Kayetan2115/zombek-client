package pl.zombek.client.core.uhc;

public enum UhcMode {
    SOLO("Solo UHC"),
    TEAMS("Teams UHC"),
    CUSTOM("Custom UHC");

    private final String displayName;

    UhcMode(String displayName) {
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
