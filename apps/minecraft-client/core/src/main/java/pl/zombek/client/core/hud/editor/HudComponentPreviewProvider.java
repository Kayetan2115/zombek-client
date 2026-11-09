package pl.zombek.client.core.hud.editor;

import pl.zombek.client.core.hud.IHudRenderer;

/**
 * Provider interface supplying realistic preview rendering for HUD elements in the HUD Editor.
 * Decouples editor rendering from live world/network state so that all HUD elements
 * display realistic sample data in preview mode even when offline or disconnected.
 */
@FunctionalInterface
public interface HudComponentPreviewProvider {

    /**
     * Renders a preview representation of the HUD component.
     *
     * @param renderer   Platform renderer
     * @param descriptor Metadata descriptor of the component
     * @param x          Screen X position
     * @param y          Screen Y position
     * @param width      Component width
     * @param height     Component height
     * @param scale      Component scale
     */
    void renderPreview(IHudRenderer renderer, HudComponentDescriptor descriptor, int x, int y, int width, int height, double scale);
}
