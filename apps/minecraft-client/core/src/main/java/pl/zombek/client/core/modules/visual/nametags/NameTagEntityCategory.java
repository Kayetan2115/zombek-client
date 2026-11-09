package pl.zombek.client.core.modules.visual.nametags;

/**
 * Category of in-game entities for NameTag customization.
 */
public enum NameTagEntityCategory {
    PLAYERS("Players"),
    LIVING_ENTITIES("Living Entities"),
    OTHER_ENTITIES("Other Entities");

    private final String label;

    NameTagEntityCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
