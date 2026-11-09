package pl.zombek.client.core.hud.editor;

import java.util.*;

/**
 * Dispatches and coordinates mouse interactions across:
 * - Viewport pan/zoom.
 * - Marquee (box) selection on empty canvas area.
 * - Multi-component dragging with relative offset preservation.
 * - Single-component dragging & 8-point resize handles.
 * - Undo/Redo command generation on gesture completion.
 * - Cursor feedback according to context.
 */
public class HudEditorMouseController {

    private final HudDragController dragController;
    private final HudResizeController resizeController;

    private final Map<String, MoveComponentsCommand.Point> initialDragPositions = new HashMap<>();
    private String resizeCompId = null;
    private int resizeInitialX;
    private int resizeInitialY;
    private int resizeInitialW;
    private int resizeInitialH;

    public HudEditorMouseController(HudDragController dragController, HudResizeController resizeController) {
        this.dragController = dragController;
        this.resizeController = resizeController;
    }

    public boolean handleMouseClick(int mouseX, int mouseY, int button,
                                    HudEditorState state, HudLayout layout,
                                    HudSelectionManager selectionManager,
                                    HudCanvasViewport viewport) {
        return handleMouseClick(mouseX, mouseY, button, state, layout, selectionManager, viewport, false, false);
    }

    public boolean handleMouseClick(int mouseX, int mouseY, int button,
                                    HudEditorState state, HudLayout layout,
                                    HudSelectionManager selectionManager,
                                    HudCanvasViewport viewport,
                                    boolean isCtrlDown, boolean isShiftDown) {
        if (state == null || layout == null || viewport == null) return false;

        // Middle click = Pan canvas
        if (button == 2 && viewport.isPointInCanvas(mouseX, mouseY)) {
            viewport.startPan(mouseX, mouseY);
            state.setCursorMode(CursorMode.PAN);
            return true;
        }

        // Left click interactions
        if (button == 0 && viewport.isPointInCanvas(mouseX, mouseY)) {
            // Pan Tool active
            if (state.getActiveTool() == EditorTool.PAN) {
                viewport.startPan(mouseX, mouseY);
                state.setCursorMode(CursorMode.PAN);
                return true;
            }

            int virtX = viewport.screenToVirtualX(mouseX);
            int virtY = viewport.screenToVirtualY(mouseY);
            int virtW = viewport.getVirtWidth();
            int virtH = viewport.getVirtHeight();

            HudMultiSelectionManager multiSel = state.getMultiSelectionManager();
            String selectedId = selectionManager.getSelectedComponentId();

            // 1. Check Resize Handles of currently selected single component
            if (selectedId != null && multiSel.getSelectedCount() <= 1 &&
                    (state.getActiveTool() == EditorTool.RESIZE || state.getActiveTool() == EditorTool.SELECT)) {
                HudComponentState selectedState = layout.getComponentState(selectedId);
                Optional<HudComponentDescriptor> optDesc = HudComponentRegistry.getInstance().getDescriptor(selectedId);
                if (selectedState != null && optDesc.isPresent()) {
                    HudComponentDescriptor desc = optDesc.get();
                    if (desc.hasCapability(HudComponentCapability.SIZE) && !selectedState.isLocked()) {
                        int boxX = selectedState.calculateScreenX(virtW);
                        int boxY = selectedState.calculateScreenY(virtH);
                        int boxW = selectedState.getEffectiveWidth();
                        int boxH = selectedState.getEffectiveHeight();

                        HudResizeHandle handle = resizeController.getHandleAt(virtX, virtY, boxX, boxY, boxW, boxH);
                        if (handle != null) {
                            resizeController.startResize(selectedId, handle, virtX, virtY, selectedState, desc, virtW, virtH);
                            resizeCompId = selectedId;
                            resizeInitialX = selectedState.getPosX();
                            resizeInitialY = selectedState.getPosY();
                            resizeInitialW = selectedState.getWidth();
                            resizeInitialH = selectedState.getHeight();
                            state.setCursorMode(getResizeCursor(handle));
                            return true;
                        }
                    }
                }
            }

            // 2. Check if clicking on an already selected component to start dragging (single or multi)
            if (!multiSel.getSelectedIds().isEmpty()) {
                for (String id : multiSel.getSelectedIds()) {
                    HudComponentState cs = layout.getComponentState(id);
                    if (cs != null && cs.containsScreenPoint(virtX, virtY, virtW, virtH)) {
                        if (isCtrlDown || isShiftDown) {
                            // Toggle selection
                            multiSel.toggleSelection(id);
                            return true;
                        }
                        if (cs.isLocked()) {
                            state.setCursorMode(CursorMode.FORBIDDEN);
                            return true;
                        }
                        startMultiDrag(multiSel.getSelectedIds(), virtX, virtY, layout, virtW, virtH, state);
                        return true;
                    }
                }
            }

            // 3. Check for clicking on any other component on canvas
            String hitId = findHitComponent(virtX, virtY, layout, virtW, virtH);
            if (hitId != null) {
                if (isCtrlDown || isShiftDown) {
                    multiSel.toggleSelection(hitId);
                    selectionManager.select(multiSel.getPrimarySelectedId());
                    return true;
                }

                selectionManager.select(hitId);
                multiSel.selectSingle(hitId);
                HudComponentState cs = layout.getComponentState(hitId);
                if (cs != null && cs.isLocked()) {
                    state.setCursorMode(CursorMode.FORBIDDEN);
                    return true;
                }
                startMultiDrag(Collections.singleton(hitId), virtX, virtY, layout, virtW, virtH, state);
                return true;
            }

            // 4. Clicked on canvas background -> Start Marquee selection or Deselect
            if (isCtrlDown || isShiftDown) {
                multiSel.startMarquee(virtX, virtY);
                return true;
            } else {
                selectionManager.clearSelection();
                multiSel.clearSelection();
                multiSel.startMarquee(virtX, virtY);
                return true;
            }
        }

        return false;
    }

    private void startMultiDrag(Collection<String> compIds, int virtX, int virtY, HudLayout layout,
                                int virtW, int virtH, HudEditorState state) {
        initialDragPositions.clear();
        String primaryId = compIds.iterator().next();
        HudComponentState primaryState = layout.getComponentState(primaryId);
        Optional<HudComponentDescriptor> optDesc = HudComponentRegistry.getInstance().getDescriptor(primaryId);

        if (primaryState != null && optDesc.isPresent()) {
            for (String id : compIds) {
                HudComponentState cs = layout.getComponentState(id);
                if (cs != null) {
                    initialDragPositions.put(id, new MoveComponentsCommand.Point(cs.getPosX(), cs.getPosY()));
                }
            }
            dragController.startDrag(primaryId, virtX, virtY, primaryState, optDesc.get(), virtW, virtH);
            state.setCursorMode(CursorMode.MOVE);
        }
    }

    private String findHitComponent(int virtX, int virtY, HudLayout layout, int virtW, int virtH) {
        List<HudComponentState> sorted = new ArrayList<>(layout.getComponents().values());
        sorted.sort((a, b) -> Integer.compare(b.getZIndex(), a.getZIndex())); // top to bottom

        for (HudComponentState state : sorted) {
            if (state != null && state.isVisible() && state.containsScreenPoint(virtX, virtY, virtW, virtH)) {
                return state.getComponentId();
            }
        }
        return null;
    }

    public boolean handleMouseDrag(int mouseX, int mouseY, int button,
                                   double deltaX, double deltaY,
                                   HudEditorState state, HudLayout layout,
                                   HudCanvasViewport viewport) {
        if (state == null || layout == null || viewport == null) return false;

        // Viewport panning
        if (viewport.isPanning()) {
            viewport.updatePan(mouseX, mouseY);
            return true;
        }

        int virtX = viewport.screenToVirtualX(mouseX);
        int virtY = viewport.screenToVirtualY(mouseY);
        int virtW = viewport.getVirtWidth();
        int virtH = viewport.getVirtHeight();

        // Marquee selection drag
        HudMultiSelectionManager multiSel = state.getMultiSelectionManager();
        if (multiSel.isMarqueeActive()) {
            multiSel.updateMarquee(virtX, virtY);
            return true;
        }

        // Handle Resizing
        if (resizeController.isResizing()) {
            String compId = resizeController.getResizingComponentId();
            HudComponentState compState = layout.getComponentState(compId);
            Optional<HudComponentDescriptor> optDesc = HudComponentRegistry.getInstance().getDescriptor(compId);
            if (compState != null && optDesc.isPresent()) {
                resizeController.updateResize(virtX, virtY, compState, optDesc.get(), virtW, virtH, false);
                return true;
            }
        }

        // Handle Dragging (with collective relative movement)
        if (dragController.isDragging()) {
            String primaryId = dragController.getDraggedComponentId();
            HudComponentState primaryState = layout.getComponentState(primaryId);
            Optional<HudComponentDescriptor> optDesc = HudComponentRegistry.getInstance().getDescriptor(primaryId);

            if (primaryState != null && optDesc.isPresent()) {
                int oldPrimaryX = primaryState.getPosX();
                int oldPrimaryY = primaryState.getPosY();

                dragController.updateDrag(virtX, virtY, primaryState, optDesc.get(),
                        virtW, virtH, state.getGridManager(), state.getGridManager().isSnapToGrid());

                int diffX = primaryState.getPosX() - oldPrimaryX;
                int diffY = primaryState.getPosY() - oldPrimaryY;

                // Move other selected components by identical delta
                for (String otherId : initialDragPositions.keySet()) {
                    if (!otherId.equalsIgnoreCase(primaryId)) {
                        HudComponentState otherState = layout.getComponentState(otherId);
                        if (otherState != null && !otherState.isLocked()) {
                            otherState.setPosX(Math.max(0, otherState.getPosX() + diffX));
                            otherState.setPosY(Math.max(0, otherState.getPosY() + diffY));
                        }
                    }
                }
                return true;
            }
        }

        return false;
    }

    public boolean handleMouseRelease(int mouseX, int mouseY, int button,
                                      HudEditorState state, HudLayout layout) {
        return handleMouseRelease(mouseX, mouseY, button, state, layout, false, false);
    }

    public boolean handleMouseRelease(int mouseX, int mouseY, int button,
                                      HudEditorState state, HudLayout layout,
                                      boolean isCtrlDown, boolean isShiftDown) {
        if (state == null) return false;
        boolean consumed = false;

        if (state.getViewport().isPanning()) {
            state.getViewport().endPan();
            state.setCursorMode(CursorMode.DEFAULT);
            consumed = true;
        }

        // End Marquee
        HudMultiSelectionManager multiSel = state.getMultiSelectionManager();
        if (multiSel.isMarqueeActive()) {
            int virtW = state.getViewport().getVirtWidth();
            int virtH = state.getViewport().getVirtHeight();
            multiSel.endMarquee(layout, virtW, virtH, isCtrlDown || isShiftDown);
            consumed = true;
        }

        // End Resize & commit command
        if (resizeController.isResizing()) {
            String compId = resizeController.getResizingComponentId();
            resizeController.endResize();
            state.setCursorMode(CursorMode.DEFAULT);

            if (layout != null && compId != null) {
                HudComponentState cs = layout.getComponentState(compId);
                if (cs != null && (cs.getPosX() != resizeInitialX || cs.getPosY() != resizeInitialY ||
                        cs.getWidth() != resizeInitialW || cs.getHeight() != resizeInitialH)) {
                    state.getUndoRedoManager().recordExecutedCommand(new ResizeComponentCommand(
                            compId,
                            resizeInitialX, resizeInitialY, resizeInitialW, resizeInitialH,
                            cs.getPosX(), cs.getPosY(), cs.getWidth(), cs.getHeight(),
                            layout
                    ));
                }
            }
            consumed = true;
        }

        // End Drag & commit collective move command
        if (dragController.isDragging()) {
            dragController.endDrag();
            state.setCursorMode(CursorMode.DEFAULT);

            if (layout != null && !initialDragPositions.isEmpty()) {
                Map<String, MoveComponentsCommand.Point> finalPositions = new HashMap<>();
                boolean changed = false;

                for (Map.Entry<String, MoveComponentsCommand.Point> entry : initialDragPositions.entrySet()) {
                    HudComponentState cs = layout.getComponentState(entry.getKey());
                    if (cs != null) {
                        finalPositions.put(entry.getKey(), new MoveComponentsCommand.Point(cs.getPosX(), cs.getPosY()));
                        if (cs.getPosX() != entry.getValue().x || cs.getPosY() != entry.getValue().y) {
                            changed = true;
                        }
                    }
                }

                if (changed) {
                    state.getUndoRedoManager().recordExecutedCommand(new MoveComponentsCommand(
                            new HashMap<>(initialDragPositions),
                            finalPositions,
                            layout
                    ));
                }
                initialDragPositions.clear();
            }
            consumed = true;
        }

        return consumed;
    }

    public void handleMouseMove(int mouseX, int mouseY,
                                HudEditorState state, HudLayout layout,
                                HudSelectionManager selectionManager,
                                HudCanvasViewport viewport) {
        if (state == null || layout == null || viewport == null) return;

        if (viewport.isPanning()) {
            state.setCursorMode(CursorMode.PAN);
            return;
        }

        if (resizeController.isResizing()) {
            state.setCursorMode(getResizeCursor(resizeController.getActiveHandle()));
            return;
        }

        if (dragController.isDragging()) {
            state.setCursorMode(CursorMode.MOVE);
            return;
        }

        if (!viewport.isPointInCanvas(mouseX, mouseY)) {
            state.setCursorMode(CursorMode.DEFAULT);
            return;
        }

        if (state.getActiveTool() == EditorTool.PAN) {
            state.setCursorMode(CursorMode.HAND);
            return;
        }

        int virtX = viewport.screenToVirtualX(mouseX);
        int virtY = viewport.screenToVirtualY(mouseY);
        int virtW = viewport.getVirtWidth();
        int virtH = viewport.getVirtHeight();

        HudMultiSelectionManager multiSel = state.getMultiSelectionManager();
        String selectedId = selectionManager.getSelectedComponentId();

        if (selectedId != null && multiSel.getSelectedCount() <= 1) {
            HudComponentState selectedState = layout.getComponentState(selectedId);
            Optional<HudComponentDescriptor> optDesc = HudComponentRegistry.getInstance().getDescriptor(selectedId);
            if (selectedState != null && optDesc.isPresent()) {
                HudComponentDescriptor desc = optDesc.get();
                if (desc.hasCapability(HudComponentCapability.SIZE) && !selectedState.isLocked()) {
                    int boxX = selectedState.calculateScreenX(virtW);
                    int boxY = selectedState.calculateScreenY(virtH);
                    int boxW = selectedState.getEffectiveWidth();
                    int boxH = selectedState.getEffectiveHeight();

                    HudResizeHandle handle = resizeController.getHandleAt(virtX, virtY, boxX, boxY, boxW, boxH);
                    if (handle != null) {
                        state.setCursorMode(getResizeCursor(handle));
                        return;
                    }
                }
            }
        }

        // Hover over any component
        for (HudComponentState compState : layout.getComponents().values()) {
            if (compState != null && compState.isVisible()) {
                if (compState.containsScreenPoint(virtX, virtY, virtW, virtH)) {
                    if (compState.isLocked()) {
                        state.setCursorMode(CursorMode.FORBIDDEN);
                    } else {
                        state.setCursorMode(CursorMode.HAND);
                    }
                    return;
                }
            }
        }

        state.setCursorMode(CursorMode.DEFAULT);
    }

    public boolean handleMouseScroll(double mouseX, double mouseY, double scrollAmount,
                                     HudEditorState state, HudCanvasViewport viewport,
                                     boolean isCtrlDown, boolean isShiftDown) {
        if (state == null || viewport == null) return false;

        if (viewport.isPointInCanvas(mouseX, mouseY)) {
            if (isCtrlDown) {
                double delta = scrollAmount > 0 ? 0.15 : -0.15;
                viewport.zoomAt(viewport.getZoom() + delta, mouseX, mouseY);
                return true;
            } else if (isShiftDown) {
                viewport.setPan(viewport.getPanX() + scrollAmount * 20.0, viewport.getPanY());
                return true;
            } else {
                viewport.setPan(viewport.getPanX(), viewport.getPanY() + scrollAmount * 20.0);
                return true;
            }
        }

        return false;
    }

    public static CursorMode getResizeCursor(HudResizeHandle handle) {
        if (handle == null) return CursorMode.DEFAULT;
        switch (handle) {
            case TOP_LEFT:
            case BOTTOM_RIGHT:
                return CursorMode.RESIZE_NWSE;
            case TOP_RIGHT:
            case BOTTOM_LEFT:
                return CursorMode.RESIZE_NESW;
            case MIDDLE_LEFT:
            case MIDDLE_RIGHT:
                return CursorMode.RESIZE_EW;
            case TOP_CENTER:
            case BOTTOM_CENTER:
                return CursorMode.RESIZE_NS;
            default:
                return CursorMode.DEFAULT;
        }
    }
}
