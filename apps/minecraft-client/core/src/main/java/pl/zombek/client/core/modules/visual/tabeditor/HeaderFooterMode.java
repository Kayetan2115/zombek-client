package pl.zombek.client.core.modules.visual.tabeditor;

/**
 * Display mode for Tab list header and footer.
 */
public enum HeaderFooterMode {
    ORIGINAL("Original Server Data"),
    CUSTOM("Custom Local Text"),
    HIDDEN("Hidden");

    private final String label;

    HeaderFooterMode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
