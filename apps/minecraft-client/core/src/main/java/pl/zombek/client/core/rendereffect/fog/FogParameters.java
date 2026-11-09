package pl.zombek.client.core.rendereffect.fog;

/**
 * Immutable container representing calculated fog rendering parameters for a frame.
 */
public record FogParameters(
        float startDistance,
        float endDistance,
        float red,
        float green,
        float blue,
        float alpha,
        float density,
        boolean modified
) {
    public static FogParameters vanilla(float start, float end, float r, float g, float b, float a) {
        return new FogParameters(start, end, r, g, b, a, 1.0f, false);
    }
}
