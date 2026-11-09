package pl.zombek.client.core.hud.editor;

/**
 * Interactive tools available in the HUD Editor toolbar.
 */
public enum EditorTool {
    SELECT("Wskaźnik (Select)", "↖", "Wybieraj i konfiguruj elementy w inspektorze"),
    MOVE("Przesuwanie (Move)", "✥", "Przeciągaj elementy po ekranie (Drag & Drop)"),
    RESIZE("Rozmiar (Resize)", "⤢", "Zmieniaj rozmiar elementów za pomocą uchwytów"),
    PAN("Przesuwanie widoku (Pan)", "✋", "Przesuwaj widok Canvasu bez zmiany pozycji elementów");

    private final String name;
    private final String icon;
    private final String description;

    EditorTool(String name, String icon, String description) {
        this.name = name;
        this.icon = icon;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public String getDescription() {
        return description;
    }
}
