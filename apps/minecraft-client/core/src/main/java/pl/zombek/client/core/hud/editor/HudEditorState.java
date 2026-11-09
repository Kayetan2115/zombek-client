package pl.zombek.client.core.hud.editor;

/**
 * Encapsulates the UI and session state of the HUD Editor.
 */
public class HudEditorState {

    public enum ResolutionMode {
        CURRENT("Bieżąca rozdzielczość"),
        RES_1080P("1920x1080 (1080p)"),
        RES_720P("1280x720 (720p)");

        private final String title;

        ResolutionMode(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }

    public enum LeftPanelTab {
        COMPONENTS("Komponenty"),
        LAYERS("Warstwy"),
        GROUPS("Grupy");

        private final String title;

        LeftPanelTab(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }

    public enum ActiveModal {
        NONE,
        UNSAVED_CHANGES,
        PROFILES,
        PRESETS,
        IMPORT_EXPORT,
        WARNINGS
    }

    private boolean active = false;
    private String searchQuery = "";
    private boolean searching = false;
    private String selectedCategory = "ALL";
    private LeftPanelTab leftPanelTab = LeftPanelTab.COMPONENTS;
    private ActiveModal activeModal = ActiveModal.NONE;
    private boolean leftPanelCollapsed = false;
    private boolean rightPanelCollapsed = false;
    private boolean showGrid = true;
    private int gridSpacing = 20;
    private ResolutionMode resolutionMode = ResolutionMode.CURRENT;
    private boolean showUnsavedDialog = false;
    private int listScrollOffset = 0;

    private final HudCanvasViewport viewport = new HudCanvasViewport();
    private final HudGridManager gridManager = new HudGridManager();
    private final HudSafeAreaManager safeAreaManager = new HudSafeAreaManager();
    private final HudDragController dragController = new HudDragController();
    private final HudResizeController resizeController = new HudResizeController();
    private final HudMultiSelectionManager multiSelectionManager = HudMultiSelectionManager.getInstance();
    private final HudUndoRedoManager undoRedoManager = HudUndoRedoManager.getInstance();
    private final HudGroupManager groupManager = HudGroupManager.getInstance();
    private final HudLayerManager layerManager = HudLayerManager.getInstance();
    private final HudProfileManager profileManager = HudProfileManager.getInstance();
    private final HudPresetManager presetManager = HudPresetManager.getInstance();
    private final HudComponentClipboard clipboard = HudComponentClipboard.getInstance();
    private final HudResponsiveLayoutManager responsiveLayoutManager = HudResponsiveLayoutManager.getInstance();

    private EditorTool activeTool = EditorTool.SELECT;
    private CursorMode cursorMode = CursorMode.DEFAULT;
    private boolean previewMode = false;

    public HudCanvasViewport getViewport() {
        return viewport;
    }

    public HudGridManager getGridManager() {
        return gridManager;
    }

    public HudSafeAreaManager getSafeAreaManager() {
        return safeAreaManager;
    }

    public HudDragController getDragController() {
        return dragController;
    }

    public HudResizeController getResizeController() {
        return resizeController;
    }

    public HudMultiSelectionManager getMultiSelectionManager() {
        return multiSelectionManager;
    }

    public HudUndoRedoManager getUndoRedoManager() {
        return undoRedoManager;
    }

    public HudGroupManager getGroupManager() {
        return groupManager;
    }

    public HudLayerManager getLayerManager() {
        return layerManager;
    }

    public HudProfileManager getProfileManager() {
        return profileManager;
    }

    public HudPresetManager getPresetManager() {
        return presetManager;
    }

    public HudComponentClipboard getClipboard() {
        return clipboard;
    }

    public HudResponsiveLayoutManager getResponsiveLayoutManager() {
        return responsiveLayoutManager;
    }

    public LeftPanelTab getLeftPanelTab() {
        return leftPanelTab != null ? leftPanelTab : LeftPanelTab.COMPONENTS;
    }

    public void setLeftPanelTab(LeftPanelTab leftPanelTab) {
        this.leftPanelTab = leftPanelTab != null ? leftPanelTab : LeftPanelTab.COMPONENTS;
    }

    public ActiveModal getActiveModal() {
        return activeModal != null ? activeModal : ActiveModal.NONE;
    }

    public void setActiveModal(ActiveModal activeModal) {
        this.activeModal = activeModal != null ? activeModal : ActiveModal.NONE;
    }

    public EditorTool getActiveTool() {
        return activeTool != null ? activeTool : EditorTool.SELECT;
    }

    public void setActiveTool(EditorTool activeTool) {
        this.activeTool = activeTool != null ? activeTool : EditorTool.SELECT;
    }

    public CursorMode getCursorMode() {
        return cursorMode != null ? cursorMode : CursorMode.DEFAULT;
    }

    public void setCursorMode(CursorMode cursorMode) {
        this.cursorMode = cursorMode != null ? cursorMode : CursorMode.DEFAULT;
    }

    public boolean isPreviewMode() {
        return previewMode;
    }

    public void setPreviewMode(boolean previewMode) {
        this.previewMode = previewMode;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery != null ? searchQuery : "";
    }

    public boolean isSearching() {
        return searching;
    }

    public void setSearching(boolean searching) {
        this.searching = searching;
    }

    public String getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedCategory(String selectedCategory) {
        this.selectedCategory = selectedCategory != null ? selectedCategory : "ALL";
    }

    public boolean isLeftPanelCollapsed() {
        return leftPanelCollapsed;
    }

    public void setLeftPanelCollapsed(boolean leftPanelCollapsed) {
        this.leftPanelCollapsed = leftPanelCollapsed;
    }

    public boolean isRightPanelCollapsed() {
        return rightPanelCollapsed;
    }

    public void setRightPanelCollapsed(boolean rightPanelCollapsed) {
        this.rightPanelCollapsed = rightPanelCollapsed;
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }

    public int getGridSpacing() {
        return gridSpacing;
    }

    public void setGridSpacing(int gridSpacing) {
        this.gridSpacing = Math.max(5, gridSpacing);
    }

    public ResolutionMode getResolutionMode() {
        return resolutionMode;
    }

    public void setResolutionMode(ResolutionMode resolutionMode) {
        this.resolutionMode = resolutionMode != null ? resolutionMode : ResolutionMode.CURRENT;
    }

    public boolean isShowUnsavedDialog() {
        return showUnsavedDialog;
    }

    public void setShowUnsavedDialog(boolean showUnsavedDialog) {
        this.showUnsavedDialog = showUnsavedDialog;
    }

    public int getListScrollOffset() {
        return listScrollOffset;
    }

    public void setListScrollOffset(int listScrollOffset) {
        this.listScrollOffset = Math.max(0, listScrollOffset);
    }

    public void reset() {
        this.active = false;
        this.searchQuery = "";
        this.searching = false;
        this.selectedCategory = "ALL";
        this.leftPanelTab = LeftPanelTab.COMPONENTS;
        this.activeModal = ActiveModal.NONE;
        this.leftPanelCollapsed = false;
        this.rightPanelCollapsed = false;
        this.showGrid = true;
        this.showUnsavedDialog = false;
        this.listScrollOffset = 0;
        this.activeTool = EditorTool.SELECT;
        this.cursorMode = CursorMode.DEFAULT;
        this.previewMode = false;
        this.viewport.resetZoom();
        this.dragController.endDrag();
        this.resizeController.endResize();
        this.multiSelectionManager.clearSelection();
    }
}
