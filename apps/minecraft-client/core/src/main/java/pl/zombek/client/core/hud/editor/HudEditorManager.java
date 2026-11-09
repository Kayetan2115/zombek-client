package pl.zombek.client.core.hud.editor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.zombek.client.core.hud.IHudRenderer;

import java.util.*;
import java.util.function.Consumer;

/**
 * Central coordinator and lifecycle manager for the HUD Editor.
 * Dispatches render loops, input events (mouse clicks, drags, releases, wheels),
 * keyboard shortcuts (Undo/Redo, Copy/Paste/Duplicate, Select All, Grouping, Profile Switching),
 * and manages modal dialog flows (Profiles, Presets, Import/Export, Warnings, Confirmation).
 */
public class HudEditorManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(HudEditorManager.class);
    private static final HudEditorManager INSTANCE = new HudEditorManager();

    private final HudEditorState state = new HudEditorState();
    private final HudSelectionManager selectionManager = HudSelectionManager.getInstance();
    private final HudLayoutManager layoutManager = HudLayoutManager.getInstance();
    private final HudEditorRenderer editorRenderer = new HudEditorRenderer();
    private final HudEditorMouseController mouseController;

    private Consumer<Boolean> editorCloseListener;
    private boolean ctrlDown = false;
    private boolean shiftDown = false;

    private HudEditorManager() {
        this.mouseController = new HudEditorMouseController(state.getDragController(), state.getResizeController());
    }

    public static HudEditorManager getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes and opens the HUD Editor session.
     */
    public synchronized void openEditor() {
        if (state.isActive()) return;
        state.setActive(true);
        state.setSearchQuery("");
        selectionManager.clearSelection();
        state.getMultiSelectionManager().clearSelection();
        layoutManager.beginEditingSession();

        state.getViewport().resetZoom();
        LOGGER.info("Opened HUD Editor session. Active profile: {}", state.getProfileManager().getActiveProfileName());
    }

    /**
     * Closes the HUD Editor and restores previous screen.
     */
    public synchronized void closeEditor() {
        if (!state.isActive()) return;
        state.setActive(false);
        selectionManager.clearSelection();
        state.getMultiSelectionManager().clearSelection();
        state.reset();

        if (editorCloseListener != null) {
            editorCloseListener.accept(false);
        }
        LOGGER.info("Closed HUD Editor session.");
    }

    public boolean isEditorOpen() {
        return state.isActive();
    }

    public void setEditorCloseListener(Consumer<Boolean> listener) {
        this.editorCloseListener = listener;
    }

    public HudEditorState getState() {
        return state;
    }

    public HudSelectionManager getSelectionManager() {
        return selectionManager;
    }

    public HudLayoutManager getLayoutManager() {
        return layoutManager;
    }

    public HudEditorRenderer getEditorRenderer() {
        return editorRenderer;
    }

    public HudEditorMouseController getMouseController() {
        return mouseController;
    }

    /**
     * Cycles to the next available HUD profile and activates it immediately.
     */
    public synchronized void switchProfileNext() {
        state.getProfileManager().switchProfileNext();
        layoutManager.loadFromDisk();
        LOGGER.info("Switched to HUD profile: {}", state.getProfileManager().getActiveProfileName());
    }

    /**
     * Renders the complete HUD Editor screen.
     */
    public void render(IHudRenderer renderer, int mouseX, int mouseY) {
        if (!state.isActive()) return;
        editorRenderer.render(renderer, layoutManager.getStagedLayout(), state, selectionManager, mouseX, mouseY);
    }

    /**
     * Handles mouse click input within the HUD Editor.
     */
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (!state.isActive()) return false;

        int screenW = state.getViewport().getCanvasWidth() + 410;
        int screenH = state.getViewport().getCanvasHeight() + 68;

        // 1. Check Modals if active
        if (state.getActiveModal() != HudEditorState.ActiveModal.NONE || state.isShowUnsavedDialog()) {
            return handleModalClick(mouseX, mouseY, screenW, screenH);
        }

        int topBarH = 34;
        int bottomBarH = 34;

        // 2. Topbar & Toolbar Clicks
        if (mouseY <= topBarH) {
            return handleTopbarClick(mouseX, mouseY, screenW);
        }

        // 3. Bottombar Clicks
        if (mouseY >= screenH - bottomBarH) {
            return handleBottombarClick(mouseX, mouseY, screenW, screenH, bottomBarH);
        }

        // If in preview mode, clicks only toggle preview mode or canvas pan
        if (state.isPreviewMode()) {
            return mouseController.handleMouseClick(mouseX, mouseY, button, state,
                    layoutManager.getStagedLayout(), selectionManager, state.getViewport(), ctrlDown, shiftDown);
        }

        // 4. Left Sidebar (Components, Layers, Groups)
        if (editorRenderer.getComponentList().handleClick(mouseX, mouseY, button,
                layoutManager.getStagedLayout(), state, selectionManager)) {
            return true;
        }

        // 5. Right Inspector
        if (editorRenderer.getInspector().handleClick(mouseX, mouseY, button,
                layoutManager.getStagedLayout(), state, selectionManager)) {
            return true;
        }

        // 6. Canvas Workspace via MouseController (handles handles, marquee, multi-drag, and pan)
        return mouseController.handleMouseClick(mouseX, mouseY, button, state,
                layoutManager.getStagedLayout(), selectionManager, state.getViewport(), ctrlDown, shiftDown);
    }

    /**
     * Handles mouse drag events.
     */
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!state.isActive()) return false;
        return mouseController.handleMouseDrag((int) mouseX, (int) mouseY, button, deltaX, deltaY,
                state, layoutManager.getStagedLayout(), state.getViewport());
    }

    /**
     * Handles mouse release events.
     */
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!state.isActive()) return false;
        return mouseController.handleMouseRelease((int) mouseX, (int) mouseY, button,
                state, layoutManager.getStagedLayout(), ctrlDown, shiftDown);
    }

    /**
     * Handles cursor movement for hover detection and cursor feedback.
     */
    public void mouseMoved(double mouseX, double mouseY) {
        if (!state.isActive()) return;
        mouseController.handleMouseMove((int) mouseX, (int) mouseY,
                state, layoutManager.getStagedLayout(), selectionManager, state.getViewport());
    }

    /**
     * Handles mouse wheel scrolling (zoom or pan in canvas, scroll in list).
     */
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!state.isActive()) return false;

        // Check if inside canvas for viewport scroll/zoom
        if (state.getViewport().isPointInCanvas(mouseX, mouseY)) {
            return mouseController.handleMouseScroll(mouseX, mouseY, amount,
                    state, state.getViewport(), ctrlDown, shiftDown);
        }

        // Otherwise scroll sidebar list
        return editorRenderer.getComponentList().handleScroll(amount, state);
    }

    private boolean handleModalClick(int mouseX, int mouseY, int screenW, int screenH) {
        HudEditorState.ActiveModal modal = state.getActiveModal();

        // 1. Unsaved Dialog
        if (state.isShowUnsavedDialog() || modal == HudEditorState.ActiveModal.UNSAVED_CHANGES) {
            int modalW = 340;
            int modalH = 140;
            int modalX = (screenW - modalW) / 2;
            int modalY = (screenH - modalH) / 2;
            int btnW = 95;
            int btnH = 22;
            int btnY = modalY + 95;

            // Save & Exit
            int saveBtnX = modalX + 16;
            if (mouseX >= saveBtnX && mouseX <= saveBtnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                layoutManager.commitStagedChanges();
                state.setShowUnsavedDialog(false);
                closeEditor();
                return true;
            }
            // Discard & Exit
            int discardBtnX = modalX + 122;
            if (mouseX >= discardBtnX && mouseX <= discardBtnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                layoutManager.discardStagedChanges();
                state.setShowUnsavedDialog(false);
                closeEditor();
                return true;
            }
            // Back
            int backBtnX = modalX + 228;
            if (mouseX >= backBtnX && mouseX <= backBtnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                state.setShowUnsavedDialog(false);
                state.setActiveModal(HudEditorState.ActiveModal.NONE);
                return true;
            }
            return true;
        }

        // 2. Profiles Modal
        if (modal == HudEditorState.ActiveModal.PROFILES) {
            int modalW = 400;
            int modalH = 280;
            int modalX = (screenW - modalW) / 2;
            int modalY = (screenH - modalH) / 2;

            List<HudProfileMetadata> profiles = state.getProfileManager().listProfiles();
            int curY = modalY + 40;
            int itemH = 24;

            for (HudProfileMetadata p : profiles) {
                if (mouseX >= modalX + 16 && mouseX <= modalX + modalW - 16 && mouseY >= curY && mouseY <= curY + itemH) {
                    state.getProfileManager().activateProfile(p.getName());
                    layoutManager.loadFromDisk();
                    layoutManager.beginEditingSession();
                    return true;
                }
                curY += itemH + 4;
            }

            // Close button
            int closeBtnW = 90;
            int closeBtnY = modalY + modalH - 32;
            int closeBtnX = modalX + modalW - closeBtnW - 16;
            if (mouseX >= closeBtnX && mouseX <= closeBtnX + closeBtnW && mouseY >= closeBtnY && mouseY <= closeBtnY + 22) {
                state.setActiveModal(HudEditorState.ActiveModal.NONE);
                return true;
            }
            return true;
        }

        // 3. Presets Modal
        if (modal == HudEditorState.ActiveModal.PRESETS) {
            int modalW = 440;
            int modalH = 320;
            int modalX = (screenW - modalW) / 2;
            int modalY = (screenH - modalH) / 2;

            Collection<HudPreset> presets = state.getPresetManager().getAllPresets();
            int curY = modalY + 38;
            int itemH = 28;

            for (HudPreset p : presets) {
                if (mouseX >= modalX + 16 && mouseX <= modalX + modalW - 16 && mouseY >= curY && mouseY <= curY + itemH) {
                    HudLayout staged = layoutManager.getStagedLayout();
                    state.getUndoRedoManager().executeCommand(new ApplyPresetCommand(p, staged));
                    state.setActiveModal(HudEditorState.ActiveModal.NONE);
                    return true;
                }
                curY += itemH + 3;
            }

            // Close button
            int closeBtnW = 90;
            int closeBtnY = modalY + modalH - 32;
            int closeBtnX = modalX + modalW - closeBtnW - 16;
            if (mouseX >= closeBtnX && mouseX <= closeBtnX + closeBtnW && mouseY >= closeBtnY && mouseY <= closeBtnY + 22) {
                state.setActiveModal(HudEditorState.ActiveModal.NONE);
                return true;
            }
            return true;
        }

        // 4. Import / Export Modal
        if (modal == HudEditorState.ActiveModal.IMPORT_EXPORT) {
            int modalW = 400;
            int modalH = 200;
            int modalX = (screenW - modalW) / 2;
            int modalY = (screenH - modalH) / 2;
            int btnW = 160;
            int btnH = 24;

            // Export
            int expX = modalX + 24;
            int expY = modalY + 70;
            if (mouseX >= expX && mouseX <= expX + btnW && mouseY >= expY && mouseY <= expY + btnH) {
                java.io.File expFile = new java.io.File("hud-layout-export.json");
                HudLayoutImportExportManager.getInstance().exportLayout(layoutManager.getStagedLayout(), expFile, "Exported Layout");
                LOGGER.info("Exported layout to {}", expFile.getAbsolutePath());
                state.setActiveModal(HudEditorState.ActiveModal.NONE);
                return true;
            }

            // Import
            int impX = modalX + modalW - btnW - 24;
            if (mouseX >= impX && mouseX <= impX + btnW && mouseY >= expY && mouseY <= expY + btnH) {
                java.io.File impFile = new java.io.File("hud-layout-export.json");
                if (impFile.exists()) {
                    ImportResult res = HudLayoutImportExportManager.getInstance().importLayout(impFile, layoutManager.getStagedLayout(), HudLayoutImportExportManager.ImportMode.REPLACE);
                    LOGGER.info("Imported layout: success={}", res.isSuccess());
                }
                state.setActiveModal(HudEditorState.ActiveModal.NONE);
                return true;
            }

            // Close
            int closeBtnW = 90;
            int closeBtnY = modalY + modalH - 36;
            int closeBtnX = modalX + (modalW - closeBtnW) / 2;
            if (mouseX >= closeBtnX && mouseX <= closeBtnX + closeBtnW && mouseY >= closeBtnY && mouseY <= closeBtnY + 22) {
                state.setActiveModal(HudEditorState.ActiveModal.NONE);
                return true;
            }
            return true;
        }

        // 5. Warnings Modal
        if (modal == HudEditorState.ActiveModal.WARNINGS) {
            int modalW = 460;
            int modalH = 280;
            int modalX = (screenW - modalW) / 2;
            int modalY = (screenH - modalH) / 2;

            int closeBtnW = 90;
            int closeBtnY = modalY + modalH - 32;
            int closeBtnX = modalX + modalW - closeBtnW - 16;
            if (mouseX >= closeBtnX && mouseX <= closeBtnX + closeBtnW && mouseY >= closeBtnY && mouseY <= closeBtnY + 22) {
                state.setActiveModal(HudEditorState.ActiveModal.NONE);
                return true;
            }
            return true;
        }

        return true;
    }

    private boolean handleTopbarClick(int mouseX, int mouseY, int screenW) {
        int btnY = 7;
        int btnH = 20;

        int curX = 98;

        // Undo (↺)
        if (mouseX >= curX && mouseX <= curX + 22 && mouseY >= btnY && mouseY <= btnY + btnH) {
            if (state.getUndoRedoManager().canUndo()) {
                state.getUndoRedoManager().undo();
            }
            return true;
        }
        curX += 24;

        // Redo (↻)
        if (mouseX >= curX && mouseX <= curX + 22 && mouseY >= btnY && mouseY <= btnY + btnH) {
            if (state.getUndoRedoManager().canRedo()) {
                state.getUndoRedoManager().redo();
            }
            return true;
        }
        curX += 28;

        // Profile button
        String profName = state.getProfileManager().getActiveProfileName();
        String profLabel = "Profil: " + (profName.length() > 8 ? profName.substring(0, 8) + ".." : profName) + " ▾";
        int profBtnW = profLabel.length() * 6 + 12;
        if (mouseX >= curX && mouseX <= curX + profBtnW && mouseY >= btnY && mouseY <= btnY + btnH) {
            state.setActiveModal(HudEditorState.ActiveModal.PROFILES);
            return true;
        }
        curX += profBtnW + 4;

        // Presets button
        if (mouseX >= curX && mouseX <= curX + 68 && mouseY >= btnY && mouseY <= btnY + btnH) {
            state.setActiveModal(HudEditorState.ActiveModal.PRESETS);
            return true;
        }
        curX += 72;

        // Import / Export button
        if (mouseX >= curX && mouseX <= curX + 56 && mouseY >= btnY && mouseY <= btnY + btnH) {
            state.setActiveModal(HudEditorState.ActiveModal.IMPORT_EXPORT);
            return true;
        }
        curX += 62;

        // Tool buttons
        if (mouseY >= btnY && mouseY <= btnY + btnH) {
            // Select (↖)
            if (mouseX >= curX && mouseX <= curX + 24) {
                state.setActiveTool(EditorTool.SELECT);
                return true;
            }
            curX += 26;
            // Move (✥)
            if (mouseX >= curX && mouseX <= curX + 24) {
                state.setActiveTool(EditorTool.MOVE);
                return true;
            }
            curX += 26;
            // Resize (⤢)
            if (mouseX >= curX && mouseX <= curX + 24) {
                state.setActiveTool(EditorTool.RESIZE);
                return true;
            }
            curX += 26;
            // Pan (✋)
            if (mouseX >= curX && mouseX <= curX + 24) {
                state.setActiveTool(EditorTool.PAN);
                return true;
            }
            curX += 30;

            // Viewport helpers
            // Zoom Out [-]
            if (mouseX >= curX && mouseX <= curX + 18) {
                state.getViewport().zoomOut();
                return true;
            }
            curX += 20;
            // Zoom In [+]
            if (mouseX >= curX && mouseX <= curX + 18) {
                state.getViewport().zoomIn();
                return true;
            }
            curX += 20;
            // Zoom 1:1
            if (mouseX >= curX && mouseX <= curX + 26) {
                state.getViewport().resetZoom();
                return true;
            }
            curX += 28;
            // Fit Canvas [⛶]
            if (mouseX >= curX && mouseX <= curX + 26) {
                state.getViewport().fitCanvas();
                return true;
            }
            curX += 30;

            // Grid Toggle [#]
            if (mouseX >= curX && mouseX <= curX + 26) {
                state.getGridManager().setVisible(!state.getGridManager().isVisible());
                return true;
            }
            curX += 28;

            // Snap Toggle [🧲]
            if (mouseX >= curX && mouseX <= curX + 28) {
                state.getGridManager().setSnapToGrid(!state.getGridManager().isSnapToGrid());
                return true;
            }
            curX += 32;

            // Aspect Ratio simulation button
            if (mouseX >= curX && mouseX <= curX + 82) {
                HudResponsiveLayoutManager resp = state.getResponsiveLayoutManager();
                if (!resp.isSimulateEnabled()) {
                    resp.setSimulateEnabled(true);
                    resp.setActiveRatio(HudAspectRatio.RATIO_16_9);
                } else {
                    HudAspectRatio[] ratios = HudAspectRatio.values();
                    int idx = resp.getActiveRatio().ordinal();
                    if (idx + 1 >= ratios.length) {
                        resp.setSimulateEnabled(false);
                    } else {
                        resp.setActiveRatio(ratios[idx + 1]);
                    }
                }
                return true;
            }
            curX += 86;

            // Preview Mode [👁]
            if (mouseX >= curX && mouseX <= curX + 64) {
                state.setPreviewMode(!state.isPreviewMode());
                return true;
            }
        }

        // Warnings button
        int warnBtnW = 86;
        int warnBtnX = screenW - 190;
        if (mouseX >= warnBtnX && mouseX <= warnBtnX + warnBtnW && mouseY >= btnY && mouseY <= btnY + btnH) {
            state.setActiveModal(HudEditorState.ActiveModal.WARNINGS);
            return true;
        }

        // Resolution Mode Toggle (Right)
        int resBtnW = 80;
        int resBtnX = screenW - 90;
        if (mouseX >= resBtnX && mouseX <= resBtnX + resBtnW && mouseY >= btnY && mouseY <= btnY + btnH) {
            HudEditorState.ResolutionMode[] modes = HudEditorState.ResolutionMode.values();
            int idx = state.getResolutionMode().ordinal();
            state.setResolutionMode(modes[(idx + 1) % modes.length]);
            return true;
        }

        return false;
    }

    private boolean handleBottombarClick(int mouseX, int mouseY, int screenW, int screenH, int bottomBarH) {
        int bbY = screenH - bottomBarH;
        int btnW = 90;
        int btnH = 22;
        int btnY = bbY + 6;

        // Reset to Defaults
        int resetX = screenW - 320;
        if (mouseX >= resetX && mouseX <= resetX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
            layoutManager.resetToDefaults();
            return true;
        }

        // Cancel
        int cancelX = screenW - 220;
        if (mouseX >= cancelX && mouseX <= cancelX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
            if (layoutManager.hasUnsavedChanges()) {
                state.setShowUnsavedDialog(true);
            } else {
                closeEditor();
            }
            return true;
        }

        // Save
        int saveX = screenW - 120;
        if (mouseX >= saveX && mouseX <= saveX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
            layoutManager.commitStagedChanges();
            closeEditor();
            return true;
        }

        return false;
    }

    /**
     * Handles keyboard key press events.
     */
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!state.isActive()) return false;

        this.ctrlDown = (modifiers & 2) != 0;
        this.shiftDown = (modifiers & 1) != 0;

        // 1. Inspector active text field input (Enter, Esc, Backspace) - HIGHEST PRIORITY
        if (editorRenderer.getInspector().isEditingField()) {
            if (editorRenderer.getInspector().handleKeyPressed(keyCode, layoutManager.getStagedLayout(), selectionManager)) {
                return true;
            }
        }

        // ESC key (GLFW_KEY_ESCAPE = 256)
        if (keyCode == 256) {
            if (state.isSearching()) {
                state.setSearching(false);
                return true;
            }
            if (state.getActiveModal() != HudEditorState.ActiveModal.NONE) {
                state.setActiveModal(HudEditorState.ActiveModal.NONE);
                return true;
            }
            if (state.isShowUnsavedDialog()) {
                state.setShowUnsavedDialog(false);
                return true;
            }
            if (state.getMultiSelectionManager().getSelectedCount() > 0) {
                state.getMultiSelectionManager().clearSelection();
                selectionManager.clearSelection();
                return true;
            }
            if (layoutManager.hasUnsavedChanges()) {
                state.setShowUnsavedDialog(true);
            } else {
                closeEditor();
            }
            return true;
        }

        // Backspace while searching (GLFW_KEY_BACKSPACE = 259)
        if (keyCode == 259 && state.isSearching()) {
            String q = state.getSearchQuery();
            if (!q.isEmpty()) {
                state.setSearchQuery(q.substring(0, q.length() - 1));
            }
            return true;
        }

        HudLayout layout = layoutManager.getStagedLayout();
        HudMultiSelectionManager multiSel = state.getMultiSelectionManager();

        // Ctrl + Z: Undo (GLFW_KEY_Z = 90)
        if (keyCode == 90 && ctrlDown && !shiftDown) {
            if (state.getUndoRedoManager().canUndo()) {
                state.getUndoRedoManager().undo();
            }
            return true;
        }

        // Ctrl + Y or Ctrl + Shift + Z: Redo (GLFW_KEY_Y = 89)
        if ((keyCode == 89 && ctrlDown) || (keyCode == 90 && ctrlDown && shiftDown)) {
            if (state.getUndoRedoManager().canRedo()) {
                state.getUndoRedoManager().redo();
            }
            return true;
        }

        // Ctrl + S: Save layout (GLFW_KEY_S = 83)
        if (keyCode == 83 && ctrlDown) {
            layoutManager.commitStagedChanges();
            return true;
        }

        // Ctrl + A: Select All (GLFW_KEY_A = 65)
        if (keyCode == 65 && ctrlDown) {
            multiSel.selectAll(layout.getComponents().keySet());
            return true;
        }

        // Ctrl + C: Copy (GLFW_KEY_C = 67)
        if (keyCode == 67 && ctrlDown) {
            if (!multiSel.getSelectedIds().isEmpty()) {
                state.getClipboard().copyComponents(layout, multiSel.getSelectedIds());
                return true;
            } else if (selectionManager.hasSelection()) {
                state.getClipboard().copyComponents(layout, Collections.singleton(selectionManager.getSelectedComponentId()));
                return true;
            }
        }

        // Ctrl + V: Paste (GLFW_KEY_V = 86)
        if (keyCode == 86 && ctrlDown) {
            state.getClipboard().pasteFromClipboard(layout, HudComponentClipboard.PasteMode.NEAR_CURSOR, 20, 20);
            return true;
        }

        // Ctrl + D: Duplicate (GLFW_KEY_D = 68)
        if (keyCode == 68 && ctrlDown) {
            if (!multiSel.getSelectedIds().isEmpty()) {
                state.getClipboard().duplicateComponents(layout, multiSel.getSelectedIds(), 20, 20);
                return true;
            } else if (selectionManager.hasSelection()) {
                state.getClipboard().duplicateComponents(layout, Collections.singleton(selectionManager.getSelectedComponentId()), 20, 20);
                return true;
            }
        }

        // Ctrl + G: Group components (GLFW_KEY_G = 71)
        if (keyCode == 71 && ctrlDown && !shiftDown) {
            if (multiSel.getSelectedCount() > 1) {
                String grpId = "group_" + (layout.getGroups().size() + 1);
                String grpName = "Grupa " + (layout.getGroups().size() + 1);
                HudGroup grp = state.getGroupManager().createGroup(layout, grpId, grpName, multiSel.getSelectedIds());
                if (grp != null) {
                    state.getUndoRedoManager().recordExecutedCommand(new CreateGroupCommand(grp, layout));
                }
                return true;
            }
        }

        // Ctrl + Shift + G: Ungroup components
        if (keyCode == 71 && ctrlDown && shiftDown) {
            for (String id : multiSel.getSelectedIds()) {
                HudComponentState cs = layout.getComponentState(id);
                if (cs != null && cs.getGroupId() != null) {
                    HudGroup g = layout.getGroup(cs.getGroupId());
                    if (g != null) {
                        state.getUndoRedoManager().executeCommand(new DeleteGroupCommand(g, layout));
                        return true;
                    }
                }
            }
        }

        // Arrow keys: Move selected component(s) (±1px, with Shift: ±10px)
        Set<String> movingIds = multiSel.getSelectedIds().isEmpty() && selectionManager.hasSelection()
                ? Collections.singleton(selectionManager.getSelectedComponentId())
                : multiSel.getSelectedIds();

        if (!movingIds.isEmpty()) {
            int step = shiftDown ? 10 : 1;
            int dx = 0;
            int dy = 0;

            if (keyCode == 263) dx = -step; // Left
            else if (keyCode == 262) dx = step;  // Right
            else if (keyCode == 265) dy = -step; // Up
            else if (keyCode == 264) dy = step;  // Down

            if (dx != 0 || dy != 0) {
                Map<String, MoveComponentsCommand.Point> oldPos = new HashMap<>();
                Map<String, MoveComponentsCommand.Point> newPos = new HashMap<>();

                for (String id : movingIds) {
                    HudComponentState cs = layout.getComponentState(id);
                    if (cs != null && !cs.isLocked()) {
                        oldPos.put(id, new MoveComponentsCommand.Point(cs.getPosX(), cs.getPosY()));
                        cs.setPosX(Math.max(0, cs.getPosX() + dx));
                        cs.setPosY(Math.max(0, cs.getPosY() + dy));
                        newPos.put(id, new MoveComponentsCommand.Point(cs.getPosX(), cs.getPosY()));
                    }
                }
                if (!oldPos.isEmpty()) {
                    state.getUndoRedoManager().recordExecutedCommand(new MoveComponentsCommand(oldPos, newPos, layout));
                }
                return true;
            }

            // Delete key = 261: Toggle visibility of selection
            if (keyCode == 261) {
                for (String id : movingIds) {
                    HudComponentState cs = layout.getComponentState(id);
                    if (cs != null) {
                        cs.setVisible(!cs.isVisible());
                    }
                }
                return true;
            }
        }

        return false;
    }

    /**
     * Handles keyboard key release events.
     */
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        this.ctrlDown = (modifiers & 2) != 0;
        this.shiftDown = (modifiers & 1) != 0;
        return false;
    }

    /**
     * Handles character typed in search or inspector input.
     */
    public boolean charTyped(char codePoint) {
        if (!state.isActive()) return false;

        // 1. Inspector active text field input
        if (editorRenderer.getInspector().isEditingField()) {
            if (editorRenderer.getInspector().handleCharTyped(codePoint, layoutManager.getStagedLayout(), selectionManager)) {
                return true;
            }
        }

        // 2. Search query input
        if (state.isSearching()) {
            if (Character.isLetterOrDigit(codePoint) || Character.isWhitespace(codePoint) || codePoint == '_' || codePoint == '-') {
                state.setSearchQuery(state.getSearchQuery() + codePoint);
                return true;
            }
        }

        return false;
    }

    public synchronized void resetForTesting() {
        state.reset();
        selectionManager.resetForTesting();
        layoutManager.resetForTesting();
        editorCloseListener = null;
        ctrlDown = false;
        shiftDown = false;
    }
}
