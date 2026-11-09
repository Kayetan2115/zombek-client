package pl.zombek.client.core.hud.editor;

/**
 * Classification of HUD component rendering modes and spatial behaviors.
 */
public enum HudComponentType {
    /** Classic 2D HUD element drawn on the screen viewport (e.g. Coordinates, FPS, Keystrokes). */
    SCREEN_SPACE("Screen Space", "Element 2D renderowany w przestrzeni ekranu"),
    /** Screen element centered on the player's crosshair / center viewport. */
    CENTER_SCREEN("Center Screen", "Element wycentrowany na celowniku gracza"),
    /** 3D element anchored to entities or blocks in the game world (e.g. NameTags, TNT Countdown, TeamView). */
    WORLD_SPACE("World Space", "Element 3D renderowany w przestrzeni świata nad encjami"),
    /** Game interface overlay (e.g. Chat, Tablist, Titles, Scoreboard, BossBar). */
    GUI_OVERLAY("GUI Overlay", "Główna nakładka interfejsu gry");

    private final String title;
    private final String description;

    HudComponentType(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
