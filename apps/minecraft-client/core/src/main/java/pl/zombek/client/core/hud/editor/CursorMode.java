package pl.zombek.client.core.hud.editor;

/**
 * Visual cursor feedback modes representing current mouse state/action in the HUD Editor.
 */
public enum CursorMode {
    DEFAULT("Standardowy"),
    MOVE("Przesuwanie"),
    RESIZE_NWSE("Rozmiar ↖↘"),
    RESIZE_NESW("Rozmiar ↗↙"),
    RESIZE_EW("Rozmiar ↔"),
    RESIZE_NS("Rozmiar ↕"),
    PAN("Przesuwanie widoku"),
    HAND("Chwytak"),
    FORBIDDEN("Zablokowany");

    private final String title;

    CursorMode(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
