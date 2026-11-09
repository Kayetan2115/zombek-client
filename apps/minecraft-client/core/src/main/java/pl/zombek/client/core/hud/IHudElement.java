package pl.zombek.client.core.hud;

/**
 * Contract for renderable HUD elements managed by HudElementRegistry.
 */
public interface IHudElement {

    /**
     * Unique identifier matching the associated module.
     */
    String getElementId();

    /**
     * Whether this HUD element should currently be rendered.
     */
    boolean isVisible();

    /**
     * Renders the HUD element using the provided platform renderer.
     *
     * @param renderer Platform-agnostic graphics context
     */
    void render(IHudRenderer renderer);
}
