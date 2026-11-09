package pl.zombek.client.core.titles;

/**
 * Animation styles for Title, Subtitle, and Action Bar presentations.
 */
public enum TitleAnimationStyle {
    FADE("Fade (Płynne Przejście)", "Płynne pojawianie się i znikanie przezroczystością"),
    SLIDE("Slide (Przesunięcie)", "Wejście z góry lub dołu ekranu"),
    SCALE("Scale (Skalowanie)", "Subtelne powiększenie podczas pojawiania się"),
    STATIC("Static (Brak Animacji)", "Natychmiastowe wyświetlenie bez przejść"),
    CUSTOM("Custom", "Niestandardowe parametry animacji");

    private final String displayName;
    private final String description;

    TitleAnimationStyle(String displayName, String description) {
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
