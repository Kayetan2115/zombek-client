package pl.zombek.client.core.modules.visual.nametags;

/**
 * Visual layout modes for in-game NameTags.
 */
public enum NameTagLayout {
    VANILLA("Vanilla"),
    MINIMAL("Minimal"),
    MODERN("Modern"),
    DETAILED("Detailed"),
    COMPACT("Compact"),
    CUSTOM("Custom");

    private final String label;

    NameTagLayout(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
