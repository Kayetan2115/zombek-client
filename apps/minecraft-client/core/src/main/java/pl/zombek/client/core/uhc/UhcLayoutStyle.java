package pl.zombek.client.core.uhc;

public enum UhcLayoutStyle {
    COMPACT("Compact"),
    DETAILED("Detailed"),
    HORIZONTAL("Horizontal"),
    VERTICAL("Vertical"),
    CUSTOM("Custom");

    private final String displayName;

    UhcLayoutStyle(String displayName) {
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
