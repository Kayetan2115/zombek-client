package pl.zombek.client.core.modules.visual.nametags;

/**
 * Format styles for entity distance display.
 */
public enum DistanceFormat {
    BLOCKS("Blocks (24m)"),
    DECIMAL("Decimal (24.3m)"),
    ROUNDED("Rounded (20m)");

    private final String label;

    DistanceFormat(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
