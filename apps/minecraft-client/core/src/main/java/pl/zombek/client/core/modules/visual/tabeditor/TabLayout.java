package pl.zombek.client.core.modules.visual.tabeditor;

/**
 * Visual layout presets for Tab Player List.
 */
public enum TabLayout {
    VANILLA("Vanilla"),
    MINIMAL("Minimal"),
    MODERN("Modern"),
    CARD("Player Cards"),
    COLUMNS("Multi-Column Grid"),
    CLASSIC("Classic 1.7"),
    CUSTOM("Custom Layout");

    private final String label;

    TabLayout(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
