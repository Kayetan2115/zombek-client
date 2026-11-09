package pl.zombek.client.hud;

/**
 * Contract for renderable in-game HUD overlay elements.
 * Implementation planned for subsequent HUD architecture stages.
 */
public interface IHudElement {
    String getId();
    int getX();
    int getY();
    int getWidth();
    int getHeight();
    boolean isVisible();
    void render();
}
