package pl.zombek.client.core.modules.visual.tabeditor;

/**
 * Sorting modes for Tab list players.
 */
public enum TabSortingMode {
    VANILLA_ORDER("Vanilla Server Order"),
    ALPHABETICAL("Alphabetical (A-Z)"),
    TEAM("Group by Team"),
    FRIENDS_FIRST("Friends at Top"),
    CUSTOM_ORDER("Custom");

    private final String label;

    TabSortingMode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
