package pl.zombek.client.core.data;

/**
 * Platform-independent abstraction for world-space 3D hitbox rendering.
 */
public interface IHitboxRenderer {
    /**
     * Draws a line segment between two 3D camera-relative coordinates.
     */
    void drawLine(double x1, double y1, double z1, double x2, double y2, double z2, int colorArgb, float lineWidth);

    /**
     * Draws a filled 4-vertex quadrilateral in 3D camera-relative coordinates.
     */
    void drawQuad(double x1, double y1, double z1,
                  double x2, double y2, double z2,
                  double x3, double y3, double z3,
                  double x4, double y4, double z4,
                  int colorArgb);

    /**
     * Draws an optional floating text label at 3D camera-relative coordinates.
     */
    default void drawLabel(double x, double y, double z, String text, int colorArgb) {
        // Default no-op for tests / platforms without world-space text
    }
}
