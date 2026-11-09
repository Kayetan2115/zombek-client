package pl.zombek.client.core.modules.visual;

import pl.zombek.client.core.data.HitboxEntityData;
import pl.zombek.client.core.data.IHitboxRenderer;

/**
 * Mathematical calculations and vertex generation for 3D entity bounding boxes.
 */
public final class HitboxGeometryCalculator {

    private HitboxGeometryCalculator() {
    }

    public static void renderEntityHitbox(IHitboxRenderer renderer,
                                         HitboxEntityData data,
                                         double camX, double camY, double camZ,
                                         HitboxRenderStyle style,
                                         int outlineColor,
                                         int fillColor,
                                         float lineWidth,
                                         boolean showEyePos,
                                         boolean showLookDir,
                                         boolean showName,
                                         boolean showDistance,
                                         boolean showId) {
        if (renderer == null || data == null) return;

        // Camera-relative box coordinates
        double x0 = data.getMinX() - camX;
        double y0 = data.getMinY() - camY;
        double z0 = data.getMinZ() - camZ;
        double x1 = data.getMaxX() - camX;
        double y1 = data.getMaxY() - camY;
        double z1 = data.getMaxZ() - camZ;

        // 1. Filled faces
        if (style == HitboxRenderStyle.FILLED || style == HitboxRenderStyle.OUTLINE_AND_FILLED) {
            renderBoxFilled(renderer, x0, y0, z0, x1, y1, z1, fillColor);
        }

        // 2. Wireframe / Outline
        if (style == HitboxRenderStyle.OUTLINE || style == HitboxRenderStyle.OUTLINE_AND_FILLED) {
            renderBoxOutline(renderer, x0, y0, z0, x1, y1, z1, outlineColor, lineWidth, false);
        } else if (style == HitboxRenderStyle.WIREFRAME) {
            renderBoxOutline(renderer, x0, y0, z0, x1, y1, z1, outlineColor, lineWidth, true);
        }

        // 3. Eye position line / cross
        if (showEyePos) {
            double eyeRelY = data.getEyeY() - camY;
            renderEyePlane(renderer, x0, x1, eyeRelY, z0, z1, 0xFF00FFFF, Math.max(1.0f, lineWidth * 0.8f));
        }

        // 4. Look direction vector
        if (showLookDir) {
            double eyeCenterX = data.getCenterX() - camX;
            double eyeRelY = data.getEyeY() - camY;
            double eyeCenterZ = data.getCenterZ() - camZ;

            double lookDist = 2.0;
            double endX = eyeCenterX + data.getLookX() * lookDist;
            double endY = eyeRelY + data.getLookY() * lookDist;
            double endZ = eyeCenterZ + data.getLookZ() * lookDist;

            renderer.drawLine(eyeCenterX, eyeRelY, eyeCenterZ, endX, endY, endZ, 0xFF0000FF, Math.max(1.5f, lineWidth * 1.2f));
        }

        // 5. Floating text info
        if (showName || showDistance || showId) {
            StringBuilder sb = new StringBuilder();
            if (showName) {
                sb.append(data.getName());
            }
            if (showDistance) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(String.format("%.1fm", data.getDistanceToCamera()));
            }
            if (showId) {
                if (sb.length() > 0) sb.append(" ");
                sb.append("[#").append(data.getEntityId()).append("]");
            }
            double labelX = (x0 + x1) / 2.0;
            double labelY = y1 + 0.3;
            double labelZ = (z0 + z1) / 2.0;
            renderer.drawLabel(labelX, labelY, labelZ, sb.toString(), 0xFFFFFFFF);
        }
    }

    public static void renderBoxOutline(IHitboxRenderer renderer,
                                        double x0, double y0, double z0,
                                        double x1, double y1, double z1,
                                        int color, float lineWidth, boolean wireframe) {
        // Bottom 4 edges
        renderer.drawLine(x0, y0, z0, x1, y0, z0, color, lineWidth);
        renderer.drawLine(x1, y0, z0, x1, y0, z1, color, lineWidth);
        renderer.drawLine(x1, y0, z1, x0, y0, z1, color, lineWidth);
        renderer.drawLine(x0, y0, z1, x0, y0, z0, color, lineWidth);

        // Top 4 edges
        renderer.drawLine(x0, y1, z0, x1, y1, z0, color, lineWidth);
        renderer.drawLine(x1, y1, z0, x1, y1, z1, color, lineWidth);
        renderer.drawLine(x1, y1, z1, x0, y1, z1, color, lineWidth);
        renderer.drawLine(x0, y1, z1, x0, y1, z0, color, lineWidth);

        // Vertical 4 pillars
        renderer.drawLine(x0, y0, z0, x0, y1, z0, color, lineWidth);
        renderer.drawLine(x1, y0, z0, x1, y1, z0, color, lineWidth);
        renderer.drawLine(x1, y0, z1, x1, y1, z1, color, lineWidth);
        renderer.drawLine(x0, y0, z1, x0, y1, z1, color, lineWidth);

        // Optional wireframe face diagonals
        if (wireframe) {
            renderer.drawLine(x0, y0, z0, x1, y1, z0, color, lineWidth * 0.5f);
            renderer.drawLine(x1, y0, z0, x1, y1, z1, color, lineWidth * 0.5f);
            renderer.drawLine(x1, y0, z1, x0, y1, z1, color, lineWidth * 0.5f);
            renderer.drawLine(x0, y0, z1, x0, y1, z0, color, lineWidth * 0.5f);
        }
    }

    public static void renderBoxFilled(IHitboxRenderer renderer,
                                       double x0, double y0, double z0,
                                       double x1, double y1, double z1,
                                       int color) {
        // Bottom face
        renderer.drawQuad(x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, color);
        // Top face
        renderer.drawQuad(x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, color);
        // North face (z0)
        renderer.drawQuad(x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, color);
        // South face (z1)
        renderer.drawQuad(x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, color);
        // West face (x0)
        renderer.drawQuad(x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, color);
        // East face (x1)
        renderer.drawQuad(x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0, color);
    }

    public static void renderEyePlane(IHitboxRenderer renderer,
                                      double x0, double x1, double eyeY,
                                      double z0, double z1,
                                      int color, float lineWidth) {
        renderer.drawLine(x0, eyeY, z0, x1, eyeY, z0, color, lineWidth);
        renderer.drawLine(x1, eyeY, z0, x1, eyeY, z1, color, lineWidth);
        renderer.drawLine(x1, eyeY, z1, x0, eyeY, z1, color, lineWidth);
        renderer.drawLine(x0, eyeY, z1, x0, eyeY, z0, color, lineWidth);
    }
}
