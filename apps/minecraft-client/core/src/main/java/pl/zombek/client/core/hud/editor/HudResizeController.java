package pl.zombek.client.core.hud.editor;

/**
 * Manages resizing of HUD components using 8 bounding-box resize handles.
 * Handles aspect ratio locking, opposite edge anchoring, and descriptor size limits.
 */
public class HudResizeController {

    public static final int HANDLE_SIZE = 8;

    private boolean resizing = false;
    private String resizingComponentId;
    private HudResizeHandle activeHandle;

    private int initialVirtX;
    private int initialVirtY;
    private int initialWidth;
    private int initialHeight;
    private int initialMouseVirtX;
    private int initialMouseVirtY;

    /**
     * Checks if a virtual coordinate hits any of the 8 resize handles for the given box.
     */
    public HudResizeHandle getHandleAt(int mouseVirtX, int mouseVirtY,
                                       int boxX, int boxY, int boxW, int boxH) {
        for (HudResizeHandle handle : HudResizeHandle.values()) {
            if (handle.contains(mouseVirtX, mouseVirtY, boxX, boxY, boxW, boxH, HANDLE_SIZE)) {
                return handle;
            }
        }
        return null;
    }

    /**
     * Begins an interactive resize gesture using the specified handle.
     */
    public boolean startResize(String compId, HudResizeHandle handle,
                               int mouseVirtX, int mouseVirtY,
                               HudComponentState compState, HudComponentDescriptor desc,
                               int virtW, int virtH) {
        if (compId == null || handle == null || compState == null || desc == null) return false;

        // Protection: Must support SIZE capability and not be locked
        if (!desc.hasCapability(HudComponentCapability.SIZE) || compState.isLocked()) {
            return false;
        }

        this.resizing = true;
        this.resizingComponentId = compId;
        this.activeHandle = handle;

        this.initialVirtX = compState.calculateScreenX(virtW);
        this.initialVirtY = compState.calculateScreenY(virtH);
        this.initialWidth = compState.getWidth();
        this.initialHeight = compState.getHeight();
        this.initialMouseVirtX = mouseVirtX;
        this.initialMouseVirtY = mouseVirtY;
        return true;
    }

    /**
     * Updates dimensions and position as the resize handle is dragged.
     */
    public void updateResize(int mouseVirtX, int mouseVirtY,
                             HudComponentState compState, HudComponentDescriptor desc,
                             int virtW, int virtH, boolean forceLockAspect) {
        if (!resizing || compState == null || desc == null || activeHandle == null) return;

        double scale = Math.max(0.1, compState.getScale());
        int deltaX = (int) Math.round((mouseVirtX - initialMouseVirtX) / scale);
        int deltaY = (int) Math.round((mouseVirtY - initialMouseVirtY) / scale);

        boolean lockAspect = forceLockAspect || compState.isLockAspectRatio();
        double initialAspect = (double) initialWidth / Math.max(1, initialHeight);

        int newW = initialWidth;
        int newH = initialHeight;
        int newScreenX = initialVirtX;
        int newScreenY = initialVirtY;

        // Horizontal change
        if (activeHandle.affectsRight()) {
            newW = initialWidth + deltaX;
        } else if (activeHandle.affectsLeft()) {
            newW = initialWidth - deltaX;
        }

        // Vertical change
        if (activeHandle.affectsBottom()) {
            newH = initialHeight + deltaY;
        } else if (activeHandle.affectsTop()) {
            newH = initialHeight - deltaY;
        }

        // Apply aspect ratio constraint
        if (lockAspect && initialAspect > 0) {
            if (activeHandle.affectsHorizontal() && !activeHandle.affectsVertical()) {
                newH = (int) Math.round(newW / initialAspect);
            } else if (activeHandle.affectsVertical() && !activeHandle.affectsHorizontal()) {
                newW = (int) Math.round(newH * initialAspect);
            } else {
                // Corner handle: choose dimension with larger proportional change
                double wRatio = (double) newW / initialWidth;
                double hRatio = (double) newH / initialHeight;
                if (Math.abs(wRatio - 1.0) > Math.abs(hRatio - 1.0)) {
                    newH = (int) Math.round(newW / initialAspect);
                } else {
                    newW = (int) Math.round(newH * initialAspect);
                }
            }
        }

        // Enforce min and max limits from descriptor
        int minW = Math.max(10, desc.getMinWidth());
        int maxW = Math.max(minW, desc.getMaxWidth());
        int minH = Math.max(10, desc.getMinHeight());
        int maxH = Math.max(minH, desc.getMaxHeight());

        newW = Math.max(minW, Math.min(maxW, newW));
        newH = Math.max(minH, Math.min(maxH, newH));

        // When dragging Left or Top handles, shift the position so opposite edge stays stationary
        if (activeHandle.affectsLeft()) {
            int effectiveDeltaW = (int) Math.round((newW - initialWidth) * scale);
            newScreenX = initialVirtX - effectiveDeltaW;
        }
        if (activeHandle.affectsTop()) {
            int effectiveDeltaH = (int) Math.round((newH - initialHeight) * scale);
            newScreenY = initialVirtY - effectiveDeltaH;
        }

        // Boundary clamp
        int effW = (int) Math.ceil(newW * scale);
        int effH = (int) Math.ceil(newH * scale);
        newScreenX = Math.max(0, Math.min(newScreenX, Math.max(0, virtW - effW)));
        newScreenY = Math.max(0, Math.min(newScreenY, Math.max(0, virtH - effH)));

        // Update component state
        compState.setWidth(newW);
        compState.setHeight(newH);
        compState.setPosX(compState.getAnchor().screenXToOffsetX(newScreenX, effW, virtW));
        compState.setPosY(compState.getAnchor().screenYToOffsetY(newScreenY, effH, virtH));
    }

    /**
     * Completes resize operation.
     */
    public void endResize() {
        this.resizing = false;
        this.resizingComponentId = null;
        this.activeHandle = null;
    }

    /**
     * Cancels resize and restores original geometry.
     */
    public void cancelResize(HudComponentState compState, int virtW, int virtH) {
        if (compState != null && resizing) {
            compState.setWidth(initialWidth);
            compState.setHeight(initialHeight);
            int effW = compState.getEffectiveWidth();
            int effH = compState.getEffectiveHeight();
            compState.setPosX(compState.getAnchor().screenXToOffsetX(initialVirtX, effW, virtW));
            compState.setPosY(compState.getAnchor().screenYToOffsetY(initialVirtY, effH, virtH));
        }
        endResize();
    }

    public boolean isResizing() {
        return resizing;
    }

    public String getResizingComponentId() {
        return resizingComponentId;
    }

    public HudResizeHandle getActiveHandle() {
        return activeHandle;
    }

    public int getInitialWidth() {
        return initialWidth;
    }

    public int getInitialHeight() {
        return initialHeight;
    }
}
