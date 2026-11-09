package pl.zombek.client.core.modules.visual.oneseven;

/**
 * Strategy for reconciling custom resource pack item transforms with 1.7 Visuals.
 */
public enum ResourcePackTransformMode {
    USE_RESOURCE_PACK("Use Resource Pack", "Modele z resource packa mają bezwzględny priorytet (1.7 Visuals nie modyfikuje niestandardowych modeli)"),
    OVERRIDE_RESOURCE_PACK("Override Resource Pack", "1.7 Visuals nadpisuje transformacje modeli z resource packa"),
    CUSTOM_PRIORITY("Custom Priority (Additive)", "Transformacje 1.7 Visuals są aplikowane jako addytywne przesunięcie (offset) do modelu resource packa");

    private final String displayName;
    private final String description;

    ResourcePackTransformMode(String displayName, String description) {
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
