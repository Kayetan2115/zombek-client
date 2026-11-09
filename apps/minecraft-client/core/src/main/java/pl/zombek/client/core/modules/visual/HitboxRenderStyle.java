package pl.zombek.client.core.modules.visual;

/**
 * Visual geometry style for rendering entity hitboxes.
 */
public enum HitboxRenderStyle {
    OUTLINE("Outline", "Krawędzie obwiedni kolizyjnej"),
    FILLED("Filled", "Półprzezroczyste ściany prostopadłościanu"),
    OUTLINE_AND_FILLED("Outline + Filled", "Krawędzie z półprzezroczystym wypełnieniem"),
    WIREFRAME("Wireframe", "Krawędzie z przekątnymi ścian bocznych");

    private final String displayName;
    private final String description;

    HitboxRenderStyle(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
