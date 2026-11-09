package pl.zombek.client.core.hud.editor;

/**
 * Grid, edge, and center line snapping calculations with visual guide support.
 */
public class HudSnapping {

    public enum GuideType {
        NONE,
        GRID,
        EDGE,
        CENTER
    }

    public static class SnapResult {
        private final int snappedX;
        private final int snappedY;
        private final boolean snappedXActive;
        private final boolean snappedYActive;
        private final Integer guideLineX;
        private final Integer guideLineY;
        private final GuideType guideTypeX;
        private final GuideType guideTypeY;

        public SnapResult(int snappedX, int snappedY, boolean snappedXActive, boolean snappedYActive,
                          Integer guideLineX, Integer guideLineY, GuideType guideTypeX, GuideType guideTypeY) {
            this.snappedX = snappedX;
            this.snappedY = snappedY;
            this.snappedXActive = snappedXActive;
            this.snappedYActive = snappedYActive;
            this.guideLineX = guideLineX;
            this.guideLineY = guideLineY;
            this.guideTypeX = guideTypeX;
            this.guideTypeY = guideTypeY;
        }

        public int getSnappedX() {
            return snappedX;
        }

        public int getSnappedY() {
            return snappedY;
        }

        public boolean isSnappedXActive() {
            return snappedXActive;
        }

        public boolean isSnappedYActive() {
            return snappedYActive;
        }

        public Integer getGuideLineX() {
            return guideLineX;
        }

        public Integer getGuideLineY() {
            return guideLineY;
        }

        public GuideType getGuideTypeX() {
            return guideTypeX;
        }

        public GuideType getGuideTypeY() {
            return guideTypeY;
        }
    }

    /**
     * Calculates snapped virtual position for a component box within virtual screen bounds.
     */
    public static SnapResult calculateSnap(int rawX, int rawY, int compW, int compH,
                                          int screenW, int screenH, int gridSpacing,
                                          boolean snapToGrid, boolean snapToCenter, boolean snapToEdges,
                                          int threshold) {
        int bestX = rawX;
        int bestY = rawY;
        boolean snapX = false;
        boolean snapY = false;
        Integer guideX = null;
        Integer guideY = null;
        GuideType typeX = GuideType.NONE;
        GuideType typeY = GuideType.NONE;

        int minDiffX = threshold + 1;
        int minDiffY = threshold + 1;

        // --- X SNAPPING ---
        // 1. Center of canvas
        if (snapToCenter) {
            int compCenterX = rawX + compW / 2;
            int screenCenterX = screenW / 2;
            int diff = Math.abs(compCenterX - screenCenterX);
            if (diff <= threshold && diff < minDiffX) {
                minDiffX = diff;
                bestX = screenCenterX - compW / 2;
                snapX = true;
                guideX = screenCenterX;
                typeX = GuideType.CENTER;
            }
        }

        // 2. Edges of canvas (Left = 0, Right = screenW)
        if (snapToEdges) {
            // Left edge
            int diffLeft = Math.abs(rawX);
            if (diffLeft <= threshold && diffLeft < minDiffX) {
                minDiffX = diffLeft;
                bestX = 0;
                snapX = true;
                guideX = 0;
                typeX = GuideType.EDGE;
            }
            // Right edge
            int diffRight = Math.abs((rawX + compW) - screenW);
            if (diffRight <= threshold && diffRight < minDiffX) {
                minDiffX = diffRight;
                bestX = screenW - compW;
                snapX = true;
                guideX = screenW;
                typeX = GuideType.EDGE;
            }
        }

        // 3. Grid line snapping
        if (snapToGrid && gridSpacing > 0) {
            // Snap left edge to grid
            int nearestGridX = Math.round((float) rawX / gridSpacing) * gridSpacing;
            int diffGrid = Math.abs(rawX - nearestGridX);
            if (diffGrid <= threshold && diffGrid < minDiffX) {
                minDiffX = diffGrid;
                bestX = nearestGridX;
                snapX = true;
                guideX = nearestGridX;
                typeX = GuideType.GRID;
            }

            // Snap right edge to grid
            int rightEdge = rawX + compW;
            int nearestRightGridX = Math.round((float) rightEdge / gridSpacing) * gridSpacing;
            int diffRightGrid = Math.abs(rightEdge - nearestRightGridX);
            if (diffRightGrid <= threshold && diffRightGrid < minDiffX) {
                minDiffX = diffRightGrid;
                bestX = nearestRightGridX - compW;
                snapX = true;
                guideX = nearestRightGridX;
                typeX = GuideType.GRID;
            }
        }

        // --- Y SNAPPING ---
        // 1. Center of canvas
        if (snapToCenter) {
            int compCenterY = rawY + compH / 2;
            int screenCenterY = screenH / 2;
            int diff = Math.abs(compCenterY - screenCenterY);
            if (diff <= threshold && diff < minDiffY) {
                minDiffY = diff;
                bestY = screenCenterY - compH / 2;
                snapY = true;
                guideY = screenCenterY;
                typeY = GuideType.CENTER;
            }
        }

        // 2. Edges of canvas (Top = 0, Bottom = screenH)
        if (snapToEdges) {
            // Top edge
            int diffTop = Math.abs(rawY);
            if (diffTop <= threshold && diffTop < minDiffY) {
                minDiffY = diffTop;
                bestY = 0;
                snapY = true;
                guideY = 0;
                typeY = GuideType.EDGE;
            }
            // Bottom edge
            int diffBottom = Math.abs((rawY + compH) - screenH);
            if (diffBottom <= threshold && diffBottom < minDiffY) {
                minDiffY = diffBottom;
                bestY = screenH - compH;
                snapY = true;
                guideY = screenH;
                typeY = GuideType.EDGE;
            }
        }

        // 3. Grid line snapping
        if (snapToGrid && gridSpacing > 0) {
            // Snap top edge to grid
            int nearestGridY = Math.round((float) rawY / gridSpacing) * gridSpacing;
            int diffGridY = Math.abs(rawY - nearestGridY);
            if (diffGridY <= threshold && diffGridY < minDiffY) {
                minDiffY = diffGridY;
                bestY = nearestGridY;
                snapY = true;
                guideY = nearestGridY;
                typeY = GuideType.GRID;
            }

            // Snap bottom edge to grid
            int bottomEdge = rawY + compH;
            int nearestBottomGridY = Math.round((float) bottomEdge / gridSpacing) * gridSpacing;
            int diffBottomGrid = Math.abs(bottomEdge - nearestBottomGridY);
            if (diffBottomGrid <= threshold && diffBottomGrid < minDiffY) {
                minDiffY = diffBottomGrid;
                bestY = nearestBottomGridY - compH;
                snapY = true;
                guideY = nearestBottomGridY;
                typeY = GuideType.GRID;
            }
        }

        return new SnapResult(bestX, bestY, snapX, snapY, guideX, guideY, typeX, typeY);
    }
}
