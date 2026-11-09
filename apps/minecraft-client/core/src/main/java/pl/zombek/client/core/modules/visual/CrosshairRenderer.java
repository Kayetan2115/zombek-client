package pl.zombek.client.core.modules.visual;

import pl.zombek.client.core.hud.IHudRenderer;

/**
 * Procedural rendering helper for all crosshair styles.
 */
public final class CrosshairRenderer {

    private CrosshairRenderer() {
    }

    public static void render(IHudRenderer renderer,
                              int cx,
                              int cy,
                              CrosshairStyle style,
                              int color,
                              int outlineColor,
                              int dotColor,
                              int bgColor,
                              boolean showCenterDot,
                              boolean showOutline,
                              boolean showBackground,
                              int width,
                              int height,
                              int thickness,
                              int gap,
                              int dotSize,
                              int outlineThickness,
                              int circleRadius,
                              int circleThickness,
                              int squareWidth,
                              int squareHeight,
                              int squareThickness,
                              boolean squareFilled,
                              float scale) {
        if (renderer == null) return;

        // Apply scale to dimensions
        int w = Math.max(1, Math.round(width * scale));
        int h = Math.max(1, Math.round(height * scale));
        int th = Math.max(1, Math.round(thickness * scale));
        int g = Math.max(0, Math.round(gap * scale));
        int ds = Math.max(1, Math.round(dotSize * scale));
        int ot = Math.max(1, Math.round(outlineThickness * scale));
        int cr = Math.max(2, Math.round(circleRadius * scale));
        int ct = Math.max(1, Math.round(circleThickness * scale));
        int sw = Math.max(2, Math.round(squareWidth * scale));
        int sh = Math.max(2, Math.round(squareHeight * scale));
        int sth = Math.max(1, Math.round(squareThickness * scale));

        // 1. Optional background box
        if (showBackground) {
            int maxExtent = Math.max(w + g, Math.max(h + g, Math.max(cr, Math.max(sw / 2, sh / 2)))) + 4;
            renderer.fill(cx - maxExtent, cy - maxExtent, maxExtent * 2, maxExtent * 2, bgColor);
        }

        // 2. Render specific style
        switch (style) {
            case DOT -> renderDot(renderer, cx, cy, ds, color, showOutline, outlineColor, ot);
            case CIRCLE -> {
                renderCircle(renderer, cx, cy, cr, ct, color, showOutline, outlineColor, ot);
                if (showCenterDot) {
                    renderDot(renderer, cx, cy, ds, dotColor, showOutline, outlineColor, ot);
                }
            }
            case SQUARE -> {
                renderSquare(renderer, cx, cy, sw, sh, sth, squareFilled, color, showOutline, outlineColor, ot);
                if (showCenterDot) {
                    renderDot(renderer, cx, cy, ds, dotColor, showOutline, outlineColor, ot);
                }
            }
            case T_SHAPE -> {
                renderCrossLines(renderer, cx, cy, w, h, th, g, color, showOutline, outlineColor, ot, true, false, true, true);
                if (showCenterDot) {
                    renderDot(renderer, cx, cy, ds, dotColor, showOutline, outlineColor, ot);
                }
            }
            case SIMPLE_CROSS -> {
                renderCrossLines(renderer, cx, cy, w, h, th, Math.max(0, g / 2), color, showOutline, outlineColor, ot, true, true, true, true);
                if (showCenterDot) {
                    renderDot(renderer, cx, cy, ds, dotColor, showOutline, outlineColor, ot);
                }
            }
            case DYNAMIC_CROSS -> {
                renderCrossLines(renderer, cx, cy, w, h, th, g, color, showOutline, outlineColor, ot, true, true, true, true);
                if (showCenterDot) {
                    renderDot(renderer, cx, cy, ds, dotColor, showOutline, outlineColor, ot);
                }
            }
            case CUSTOM, CLASSIC_CROSS -> {
                renderCrossLines(renderer, cx, cy, w, h, th, g, color, showOutline, outlineColor, ot, true, true, true, true);
                if (showCenterDot) {
                    renderDot(renderer, cx, cy, ds, dotColor, showOutline, outlineColor, ot);
                }
            }
        }
    }

    private static void renderDot(IHudRenderer renderer, int cx, int cy, int size, int color, boolean outline, int outlineColor, int ot) {
        int half = size / 2;
        int x = cx - half;
        int y = cy - half;
        if (outline) {
            renderer.fill(x - ot, y - ot, size + ot * 2, size + ot * 2, outlineColor);
        }
        renderer.fill(x, y, size, size, color);
    }

    private static void renderSquare(IHudRenderer renderer, int cx, int cy, int w, int h, int thickness, boolean filled,
                                     int color, boolean outline, int outlineColor, int ot) {
        int x = cx - w / 2;
        int y = cy - h / 2;
        if (filled) {
            if (outline) {
                renderer.drawBorder(x - ot, y - ot, w + ot * 2, h + ot * 2, outlineColor, ot);
            }
            renderer.fill(x, y, w, h, color);
        } else {
            if (outline) {
                renderer.drawBorder(x - ot, y - ot, w + ot * 2, h + ot * 2, outlineColor, ot);
                renderer.drawBorder(x + thickness, y + thickness, w - thickness * 2, h - thickness * 2, outlineColor, ot);
            }
            renderer.drawBorder(x, y, w, h, color, thickness);
        }
    }

    private static void renderCircle(IHudRenderer renderer, int cx, int cy, int radius, int thickness,
                                     int color, boolean outline, int outlineColor, int ot) {
        int numSegments = Math.max(24, radius * 4);
        double angleStep = (2 * Math.PI) / numSegments;

        for (int i = 0; i < numSegments; i++) {
            double angle = i * angleStep;
            int px = (int) Math.round(cx + radius * Math.cos(angle));
            int py = (int) Math.round(cy + radius * Math.sin(angle));

            if (outline) {
                renderer.fill(px - thickness / 2 - ot, py - thickness / 2 - ot,
                        thickness + ot * 2, thickness + ot * 2, outlineColor);
            }
            renderer.fill(px - thickness / 2, py - thickness / 2, thickness, thickness, color);
        }
    }

    private static void renderCrossLines(IHudRenderer renderer, int cx, int cy,
                                         int w, int h, int th, int gap,
                                         int color, boolean outline, int outlineColor, int ot,
                                         boolean top, boolean bottom, boolean left, boolean right) {
        int thHalf = th / 2;

        // Top line
        if (top) {
            int lx = cx - thHalf;
            int ly = cy - gap - h;
            renderLineWithOutline(renderer, lx, ly, th, h, color, outline, outlineColor, ot);
        }
        // Bottom line
        if (bottom) {
            int lx = cx - thHalf;
            int ly = cy + gap;
            renderLineWithOutline(renderer, lx, ly, th, h, color, outline, outlineColor, ot);
        }
        // Left line
        if (left) {
            int lx = cx - gap - w;
            int ly = cy - thHalf;
            renderLineWithOutline(renderer, lx, ly, w, th, color, outline, outlineColor, ot);
        }
        // Right line
        if (right) {
            int lx = cx + gap;
            int ly = cy - thHalf;
            renderLineWithOutline(renderer, lx, ly, w, th, color, outline, outlineColor, ot);
        }
    }

    private static void renderLineWithOutline(IHudRenderer renderer, int x, int y, int w, int h,
                                              int color, boolean outline, int outlineColor, int ot) {
        if (outline) {
            renderer.fill(x - ot, y - ot, w + ot * 2, h + ot * 2, outlineColor);
        }
        renderer.fill(x, y, w, h, color);
    }
}
