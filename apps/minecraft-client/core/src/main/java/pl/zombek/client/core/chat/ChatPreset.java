package pl.zombek.client.core.chat;

/**
 * Visual presets for ZombekClient Custom Chat Renderer.
 */
public enum ChatPreset {
    VANILLA("Vanilla", "Standardowy wygląd czatu Minecraft"),
    CLASSIC("Classic", "Klasyczny układ czatu z delikatnym tłem i standardowymi odstępami"),
    MINIMAL("Minimal", "Minimalistyczny wygląd, zredukowane dekoracje i maksymalna czytelność"),
    MODERN("Modern", "Nowoczesny panel, dopracowana typografia i zaokrąglone narożniki"),
    GLASS("Glass", "Półprzezroczyste tło, efekt frosted glass i subtelne obramowanie"),
    COMPACT("Compact", "Kompaktowy widok z mniejszymi odstępami dla dużej liczby wiadomości"),
    CUSTOM("Custom", "Pełna personalizacja wszystkich parametrów");

    private final String displayName;
    private final String description;

    ChatPreset(String displayName, String description) {
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
