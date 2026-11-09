package pl.zombek.client.core.modules.visual.nametags;

/**
 * Display formats for entity health in NameTags.
 */
public enum HealthDisplayStyle {
    NUMERIC("Numeric (20 HP)"),
    HEARTS("Hearts (❤❤❤)"),
    BAR("Progress Bar"),
    COMPACT("Compact (20)"),
    CUSTOM("Custom Template");

    private final String label;

    HealthDisplayStyle(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
