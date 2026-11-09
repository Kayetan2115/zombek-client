package pl.zombek.client.core.rendereffect.fog;

/**
 * Platform adapter abstraction connecting FogCustomizer with Minecraft's FogRenderer/BackgroundRenderer.
 */
public interface IFogRendererAdapter {

    /**
     * Reports whether the platform renderer supports custom fog color manipulation.
     */
    boolean supportsCustomFogColor();

    /**
     * Reports whether the platform renderer supports custom fog distance manipulation.
     */
    boolean supportsCustomFogDistance();

    /**
     * Applies the computed fog parameters to the platform rendering pipeline.
     */
    void applyFogParameters(FogParameters parameters);
}
