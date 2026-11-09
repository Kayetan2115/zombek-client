package pl.zombek.client.core.camera;

/**
 * Adapter interface implemented by platform rendering layers (Fabric, Quilt, Forge, NeoForge)
 * to perform actual hardware or framebuffer-level motion blur passes.
 */
public interface IMotionBlurRendererAdapter {

    /**
     * Checks whether the current graphics pipeline and GPU environment support motion blur.
     */
    boolean isSupported();

    /**
     * Called when the viewport or window resolution changes.
     */
    void onResolutionChanged(int width, int height);

    /**
     * Executes the accumulation or post-process motion blur pass.
     *
     * @param blurAlpha calculated blend factor in [0.0, 1.0]
     */
    void renderBlur(float blurAlpha);

    /**
     * Clears cached history frames upon world change or context loss.
     */
    void clearHistory();

    /**
     * Releases all allocated textures, framebuffers, or shaders.
     */
    void cleanup();
}
