package pl.zombek.client.core.hud.editor;

/**
 * Controls interactive dragging of a selected HUD component across the virtual canvas.
 * Preserves initial grab offset, applies grid/edge snapping, and clamps within screen limits.
 */
public class HudDragController {

    private boolean dragging = false;
    private String draggedComponentId;
    private int dragOffsetVirtX;
    private int dragOffsetVirtY;
    private int initialVirtX;
    private int initialVirtY;
    private HudSnapping.SnapResult lastSnapResult;

    /**
     * Attempts to initiate a drag operation on the specified component.
     *
     * @return true if drag was successfully started, false if component cannot be moved
     */
    public boolean startDrag(String compId, int cursorVirtX, int cursorVirtY,
                             HudComponentState compState, HudComponentDescriptor desc,
                             int virtW, int virtH) {
        if (compId == null || compState == null || desc == null) return false;

        // Protection: WORLD_SPACE elements cannot be repositioned in 2D screen space
        if (desc.getType() == HudComponentType.WORLD_SPACE) {
            return false;
        }

        // Protection: Locked components cannot be dragged
        if (compState.isLocked()) {
            return false;
        }

        // Capability check: Must support POSITION
        if (!desc.hasCapability(HudComponentCapability.POSITION)) {
            return false;
        }

        int compScreenX = compState.calculateScreenX(virtW);
        int compScreenY = compState.calculateScreenY(virtH);

        this.dragging = true;
        this.draggedComponentId = compId;
        this.dragOffsetVirtX = cursorVirtX - compScreenX;
        this.dragOffsetVirtY = cursorVirtY - compScreenY;
        this.initialVirtX = compScreenX;
        this.initialVirtY = compScreenY;
        this.lastSnapResult = null;
        return true;
    }

    /**
     * Updates component coordinates as the mouse is moved while dragging.
     */
    public void updateDrag(int cursorVirtX, int cursorVirtY,
                           HudComponentState compState, HudComponentDescriptor desc,
                           int virtW, int virtH, HudGridManager gridManager, boolean snapEnabled) {
        if (!dragging || compState == null || desc == null) return;

        int effW = compState.getEffectiveWidth();
        int effH = compState.getEffectiveHeight();

        // 1. Raw candidate target position
        int rawX = cursorVirtX - dragOffsetVirtX;
        int rawY = cursorVirtY - dragOffsetVirtY;

        // 2. Snap calculation
        int targetX = rawX;
        int targetY = rawY;
        if (snapEnabled && gridManager != null) {
            HudSnapping.SnapResult snap = gridManager.calculateSnap(rawX, rawY, effW, effH, virtW, virtH);
            targetX = snap.getSnappedX();
            targetY = snap.getSnappedY();
            this.lastSnapResult = snap;
        } else {
            this.lastSnapResult = null;
        }

        // 3. Viewport boundary clamping
        targetX = Math.max(0, Math.min(targetX, Math.max(0, virtW - effW)));
        targetY = Math.max(0, Math.min(targetY, Math.max(0, virtH - effH)));

        // 4. Update anchor-relative offsets
        int newPosX = compState.getAnchor().screenXToOffsetX(targetX, effW, virtW);
        int newPosY = compState.getAnchor().screenYToOffsetY(targetY, effH, virtH);

        compState.setPosX(newPosX);
        compState.setPosY(newPosY);
    }

    /**
     * Concludes the drag operation, preserving the final position in the staged layout.
     */
    public void endDrag() {
        this.dragging = false;
        this.draggedComponentId = null;
        this.lastSnapResult = null;
    }

    /**
     * Cancels an ongoing drag and rolls back component coordinates to drag-start.
     */
    public void cancelDrag(HudComponentState compState, int virtW, int virtH) {
        if (compState != null && dragging) {
            int effW = compState.getEffectiveWidth();
            int effH = compState.getEffectiveHeight();
            int origPosX = compState.getAnchor().screenXToOffsetX(initialVirtX, effW, virtW);
            int origPosY = compState.getAnchor().screenYToOffsetY(initialVirtY, effH, virtH);
            compState.setPosX(origPosX);
            compState.setPosY(origPosY);
        }
        endDrag();
    }

    public boolean isDragging() {
        return dragging;
    }

    public String getDraggedComponentId() {
        return draggedComponentId;
    }

    public int getDragOffsetVirtX() {
        return dragOffsetVirtX;
    }

    public int getDragOffsetVirtY() {
        return dragOffsetVirtY;
    }

    public int getInitialVirtX() {
        return initialVirtX;
    }

    public int getInitialVirtY() {
        return initialVirtY;
    }

    public HudSnapping.SnapResult getLastSnapResult() {
        return lastSnapResult;
    }
}
