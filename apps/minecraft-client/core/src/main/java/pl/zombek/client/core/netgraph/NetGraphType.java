package pl.zombek.client.core.netgraph;

/**
 * Visual plot styles for Net Graph series.
 */
public enum NetGraphType {
    /**
     * Continuous linear / spline curve.
     */
    LINE("Line", "Wykres liniowy"),

    /**
     * Vertical discrete bars representing individual temporal samples.
     */
    BAR("Bar", "Wykres słupkowy"),

    /**
     * Semi-transparent filled polygon under the curve.
     */
    AREA("Filled Area", "Wypełniony wykres warstwowy");

    private final String label;
    private final String description;

    NetGraphType(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}
