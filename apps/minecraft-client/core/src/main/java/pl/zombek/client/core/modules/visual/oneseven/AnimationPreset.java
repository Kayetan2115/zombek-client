package pl.zombek.client.core.modules.visual.oneseven;

/**
 * Built-in animation presets for 1.7 Visuals.
 */
public enum AnimationPreset {
    MODERN("Modern", "Standardowe animacje i zachowanie nowoczesnego Minecrafta"),
    MINECRAFT_1_8("Minecraft 1.8", "Zestaw klasycznych animacji charakterystycznych dla wersji 1.8"),
    MINECRAFT_1_7("Minecraft 1.7", "Klasyczne zachowania i animacje z wersji 1.7 (w tym Red Armor i Instant Sneak)"),
    CUSTOM("Custom", "Własna, w pełni spersonalizowana konfiguracja użytkownika");

    private final String displayName;
    private final String description;

    AnimationPreset(String displayName, String description) {
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
