package pl.zombek.client.core.modules.visual.tabeditor;

/**
 * Display formats for latency / ping in Tab list.
 */
public enum TabPingMode {
    NUMERIC("Numeric (24ms)"),
    BARS("Vanilla Bars (📶)"),
    COLORED_TEXT("Colored ms (Green/Yellow/Red)"),
    MINIMAL_ICON("Minimal Dot (●)");

    private final String label;

    TabPingMode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
