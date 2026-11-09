package pl.zombek.client.core.titles;

/**
 * Text alignment options for Titles and Subtitles.
 */
public enum TitleAlignment {
    CENTER("Środek"),
    LEFT("Do lewej"),
    RIGHT("Do prawej");

    private final String displayName;

    TitleAlignment(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
