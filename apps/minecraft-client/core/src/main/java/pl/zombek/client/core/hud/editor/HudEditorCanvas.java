package pl.zombek.client.core.hud.editor;

import pl.zombek.client.core.hud.IHudRenderer;

import java.util.Locale;

/**
 * Visual workspace for previewing, inspecting, dragging, and resizing HUD components.
 * Renders viewport zoom/pan, reference grid, safe area guides, snap lines, previews,
 * bounding boxes, and 8-point resize handles.
 */
public class HudEditorCanvas {

    private int canvasX;
    private int canvasY;
    private int canvasWidth;
    private int canvasHeight;

    private int virtWidth = 960;
    private int virtHeight = 540;

    public void updateBounds(int x, int y, int width, int height, int screenWidth, int screenHeight,
                             HudEditorState.ResolutionMode mode, HudCanvasViewport viewport) {
        this.canvasX = x;
        this.canvasY = y;
        this.canvasWidth = Math.max(100, width);
        this.canvasHeight = Math.max(100, height);

        int targetRefW = screenWidth;
        int targetRefH = screenHeight;

        if (mode == HudEditorState.ResolutionMode.RES_1080P) {
            targetRefW = 960; // 1920 with GUI scale 2 = 960x540
            targetRefH = 540;
        } else if (mode == HudEditorState.ResolutionMode.RES_720P) {
            targetRefW = 640; // 1280 with GUI scale 2 = 640x360
            targetRefH = 360;
        }

        this.virtWidth = targetRefW;
        this.virtHeight = targetRefH;

        if (viewport != null) {
            viewport.updateBounds(canvasX, canvasY, canvasWidth, canvasHeight, virtWidth, virtHeight);
        }
    }

    public void updateBounds(int x, int y, int width, int height, int screenWidth, int screenHeight,
                             HudEditorState.ResolutionMode mode) {
        updateBounds(x, y, width, height, screenWidth, screenHeight, mode, null);
    }

    public void render(IHudRenderer renderer, HudLayout layout, HudEditorState state,
                       HudSelectionManager selectionManager, int mouseX, int mouseY) {
        if (renderer == null || layout == null || state == null) return;

        HudCanvasViewport viewport = state.getViewport();
        viewport.updateBounds(canvasX, canvasY, canvasWidth, canvasHeight, virtWidth, virtHeight);

        // 1. Canvas background
        renderer.fill(canvasX, canvasY, canvasWidth, canvasHeight, 0xD012141A);

        // Calculate screen coordinates of virtual workspace
        int screenVirtX = viewport.virtualToScreenX(0);
        int screenVirtY = viewport.virtualToScreenY(0);
        int screenVirtW = viewport.virtualToScreenWidth(virtWidth);
        int screenVirtH = viewport.virtualToScreenHeight(virtHeight);

        // 2. Virtual screen background
        renderer.fill(screenVirtX, screenVirtY, screenVirtW, screenVirtH, 0xEE1A1C23);

        // If in clean Preview Mode, render clean components without editor overlays
        if (state.isPreviewMode()) {
            renderCleanPreviews(renderer, layout, viewport);
            return;
        }

        // 3. Grid rendering
        state.getGridManager().renderGrid(renderer, viewport);

        // 4. Safe area guides
        state.getSafeAreaManager().renderGuides(renderer, viewport);

        // 5. Virtual screen border & label
        renderer.drawBorder(screenVirtX, screenVirtY, screenVirtW, screenVirtH, 0x664A90E2, 1);
        String label = String.format("Obszar Gry: %dx%d (%s) • Zoom: %s",
                virtWidth, virtHeight, state.getResolutionMode().getTitle(), viewport.getZoomPercentString());
        renderer.drawText(label, screenVirtX + 6, screenVirtY + 6, 0x88AAAAAA, false, 0.75f, false);

        // 5b. Simulated Aspect Ratio letterboxing
        if (state.getResponsiveLayoutManager().isSimulateEnabled()) {
            renderSimulatedAspectRatio(renderer, viewport, state.getResponsiveLayoutManager(), screenVirtX, screenVirtY, screenVirtW, screenVirtH);
        }

        // 6. Snap guide lines (during dragging)
        if (state.getDragController().isDragging()) {
            HudSnapping.SnapResult snap = state.getDragController().getLastSnapResult();
            if (snap != null) {
                renderSnapGuides(renderer, viewport, snap, screenVirtX, screenVirtY, screenVirtW, screenVirtH);
            }
        }

        // 6b. Group Bounding Boxes
        renderGroups(renderer, layout, state, viewport);

        // 7. Render components previews & bounding boxes sorted by Z-Index
        HudComponentRegistry registry = HudComponentRegistry.getInstance();
        String selectedId = selectionManager.getSelectedComponentId();
        HudMultiSelectionManager multiSel = state.getMultiSelectionManager();

        java.util.List<HudComponentState> sortedComps = state.getLayerManager().getSortedComponents(layout);

        for (HudComponentState compState : sortedComps) {
            if (compState == null) continue;

            boolean isSelected = (selectedId != null && compState.getComponentId().equalsIgnoreCase(selectedId)) ||
                                 multiSel.isSelected(compState.getComponentId());
            boolean isVisible = compState.isVisible();

            // If not visible and not selected, don't clutter canvas
            if (!isVisible && !isSelected) {
                continue;
            }

            int virtCompX = compState.calculateScreenX(virtWidth);
            int virtCompY = compState.calculateScreenY(virtHeight);
            int virtCompW = compState.getEffectiveWidth();
            int virtCompH = compState.getEffectiveHeight();

            int drawX = viewport.virtualToScreenX(virtCompX);
            int drawY = viewport.virtualToScreenY(virtCompY);
            int drawW = viewport.virtualToScreenWidth(virtCompW);
            int drawH = viewport.virtualToScreenHeight(virtCompH);

            double effectiveScale = compState.getScale() * viewport.getZoom();

            // Render component preview
            registry.getDescriptor(compState.getComponentId()).ifPresent(desc -> {
                desc.getPreviewProvider().renderPreview(renderer, desc, drawX, drawY, drawW, drawH, effectiveScale);
            });

            int virtMouseX = viewport.screenToVirtualX(mouseX);
            int virtMouseY = viewport.screenToVirtualY(mouseY);
            boolean isHovered = compState.containsScreenPoint(virtMouseX, virtMouseY, virtWidth, virtHeight);

            // Bounding box overlay & Selection chrome
            if (isSelected) {
                renderer.drawBorder(drawX - 2, drawY - 2, drawW + 4, drawH + 4, 0xFFFFCC00, 2);
                renderer.fill(drawX - 2, drawY - 2, drawW + 4, drawH + 4, 0x22FFCC00);

                // Badge above box
                String lockIcon = compState.isLocked() ? "🔒 " : "";
                String groupTag = compState.getGroupId() != null ? " [📁 " + compState.getGroupId() + "]" : "";
                String tag = lockIcon + compState.getComponentId().toUpperCase(Locale.ROOT) + groupTag +
                        String.format(" (Z:%d | %dx%d | %.2fx)",
                                compState.getZIndex(), compState.getWidth(), compState.getHeight(), compState.getScale());
                int tagW = renderer.getTextWidth(tag) + 8;
                renderer.fill(drawX - 2, drawY - 14, tagW, 12, 0xDD111111);
                renderer.drawText(tag, drawX + 2, drawY - 12, 0xFFFFCC00, true, 0.75f, true);

                // 8-point Resize Handles (if SIZE capability is present, not locked, and only 1 selected)
                if (multiSel.getSelectedCount() <= 1) {
                    registry.getDescriptor(compState.getComponentId()).ifPresent(desc -> {
                        if (desc.hasCapability(HudComponentCapability.SIZE) && !compState.isLocked()) {
                            renderResizeHandles(renderer, viewport, virtCompX, virtCompY, virtCompW, virtCompH);
                        }
                    });
                }
            } else if (isHovered) {
                renderer.drawBorder(drawX - 1, drawY - 1, drawW + 2, drawH + 2, 0xAA4A90E2, 1);
            } else {
                renderer.drawBorder(drawX, drawY, drawW, drawH, 0x33FFFFFF, 1);
            }
        }

        // 8. Marquee Selection Box
        if (multiSel.isMarqueeActive()) {
            renderMarqueeBox(renderer, viewport, multiSel);
        }
    }

    private void renderCleanPreviews(IHudRenderer renderer, HudLayout layout, HudCanvasViewport viewport) {
        HudComponentRegistry registry = HudComponentRegistry.getInstance();
        for (HudComponentState compState : layout.getComponents().values()) {
            if (compState == null || !compState.isVisible()) continue;

            int virtCompX = compState.calculateScreenX(virtWidth);
            int virtCompY = compState.calculateScreenY(virtHeight);
            int virtCompW = compState.getEffectiveWidth();
            int virtCompH = compState.getEffectiveHeight();

            int drawX = viewport.virtualToScreenX(virtCompX);
            int drawY = viewport.virtualToScreenY(virtCompY);
            int drawW = viewport.virtualToScreenWidth(virtCompW);
            int drawH = viewport.virtualToScreenHeight(virtCompH);
            double effectiveScale = compState.getScale() * viewport.getZoom();

            registry.getDescriptor(compState.getComponentId()).ifPresent(desc -> {
                desc.getPreviewProvider().renderPreview(renderer, desc, drawX, drawY, drawW, drawH, effectiveScale);
            });
        }
    }

    private void renderResizeHandles(IHudRenderer renderer, HudCanvasViewport viewport,
                                     int virtBoxX, int virtBoxY, int virtBoxW, int virtBoxH) {
        int handleSize = HudResizeController.HANDLE_SIZE;
        int half = handleSize / 2;

        for (HudResizeHandle handle : HudResizeHandle.values()) {
            int cx = handle.getCenterX(virtBoxX, virtBoxW);
            int cy = handle.getCenterY(virtBoxY, virtBoxH);

            int screenCX = viewport.virtualToScreenX(cx);
            int screenCY = viewport.virtualToScreenY(cy);

            int hx = screenCX - half;
            int hy = screenCY - half;

            // Handle square: White fill with dark border
            renderer.fill(hx, hy, handleSize, handleSize, 0xFFFFFFFF);
            renderer.drawBorder(hx, hy, handleSize, handleSize, 0xFF222222, 1);
        }
    }

    private void renderSnapGuides(IHudRenderer renderer, HudCanvasViewport viewport,
                                  HudSnapping.SnapResult snap,
                                  int screenVirtX, int screenVirtY, int screenVirtW, int screenVirtH) {
        if (snap.getGuideLineX() != null) {
            int gx = viewport.virtualToScreenX(snap.getGuideLineX());
            int color = snap.getGuideTypeX() == HudSnapping.GuideType.CENTER ? 0xFFFFDD00 : 0xFF00EEFF;
            renderer.fill(gx, screenVirtY, 1, screenVirtH, color);
        }
        if (snap.getGuideLineY() != null) {
            int gy = viewport.virtualToScreenY(snap.getGuideLineY());
            int color = snap.getGuideTypeY() == HudSnapping.GuideType.CENTER ? 0xFFFFDD00 : 0xFF00EEFF;
            renderer.fill(screenVirtX, gy, screenVirtW, 1, color);
        }
    }

    private void renderGroups(IHudRenderer renderer, HudLayout layout, HudEditorState state, HudCanvasViewport viewport) {
        if (layout.getGroups().isEmpty()) return;
        HudGroupManager gm = state.getGroupManager();

        for (HudGroup group : layout.getGroups().values()) {
            if (!group.isVisible()) continue;
            HudGroupManager.GroupBounds bounds = gm.calculateGroupBounds(group, layout, virtWidth, virtHeight);
            if (bounds == null) continue;

            int drawX = viewport.virtualToScreenX(bounds.getX());
            int drawY = viewport.virtualToScreenY(bounds.getY());
            int drawW = viewport.virtualToScreenWidth(bounds.getWidth());
            int drawH = viewport.virtualToScreenHeight(bounds.getHeight());

            int color = group.getColor();
            // Draw group dashed/dim bounding box
            renderer.drawBorder(drawX, drawY, drawW, drawH, color, 1);

            // Group header pill
            String groupHeader = "📁 " + group.getName() + " (" + group.getComponentIds().size() + ")";
            int tw = renderer.getTextWidth(groupHeader) + 6;
            renderer.fill(drawX, Math.max(0, drawY - 11), tw, 10, 0xCC181822);
            renderer.drawText(groupHeader, drawX + 3, Math.max(0, drawY - 10), color, false, 0.7f, false);
        }
    }

    private void renderMarqueeBox(IHudRenderer renderer, HudCanvasViewport viewport, HudMultiSelectionManager multiSel) {
        int x = viewport.virtualToScreenX(multiSel.getMarqueeBoxX());
        int y = viewport.virtualToScreenY(multiSel.getMarqueeBoxY());
        int w = viewport.virtualToScreenWidth(multiSel.getMarqueeBoxW());
        int h = viewport.virtualToScreenHeight(multiSel.getMarqueeBoxH());

        if (w > 2 && h > 2) {
            renderer.fill(x, y, w, h, 0x2200E5FF);
            renderer.drawBorder(x, y, w, h, 0xAA00E5FF, 1);
        }
    }

    private void renderSimulatedAspectRatio(IHudRenderer renderer, HudCanvasViewport viewport,
                                           HudResponsiveLayoutManager respMgr,
                                           int screenVirtX, int screenVirtY, int screenVirtW, int screenVirtH) {
        int simW = respMgr.getSimulatedWidth(virtWidth, virtHeight);
        int simH = respMgr.getSimulatedHeight(virtWidth, virtHeight);

        int drawSimW = viewport.virtualToScreenWidth(simW);
        int drawSimH = viewport.virtualToScreenHeight(simH);

        int offsetX = (screenVirtW - drawSimW) / 2;
        int offsetY = (screenVirtH - drawSimH) / 2;

        int simBoxX = screenVirtX + offsetX;
        int simBoxY = screenVirtY + offsetY;

        // Draw pillarbox / letterbox bars
        if (offsetX > 0) {
            renderer.fill(screenVirtX, screenVirtY, offsetX, screenVirtH, 0x88000000);
            renderer.fill(screenVirtX + screenVirtW - offsetX, screenVirtY, offsetX, screenVirtH, 0x88000000);
        }
        if (offsetY > 0) {
            renderer.fill(screenVirtX, screenVirtY, screenVirtW, offsetY, 0x88000000);
            renderer.fill(screenVirtX, screenVirtY + screenVirtH - offsetY, screenVirtW, offsetY, 0x88000000);
        }

        renderer.drawBorder(simBoxX, simBoxY, drawSimW, drawSimH, 0x8800E5FF, 1);
        String aspLabel = "Symulacja: " + respMgr.getSimulatedAspectRatio().getTitle();
        renderer.drawText(aspLabel, simBoxX + 4, simBoxY + 4, 0xCC00E5FF, false, 0.7f, false);
    }

    public boolean isPointInCanvas(int mouseX, int mouseY) {
        return mouseX >= canvasX && mouseX <= canvasX + canvasWidth && mouseY >= canvasY && mouseY <= canvasY + canvasHeight;
    }

    public boolean isPointInVirtualScreen(int mouseX, int mouseY) {
        return mouseX >= canvasX && mouseX <= canvasX + canvasWidth && mouseY >= canvasY && mouseY <= canvasY + canvasHeight;
    }

    public int toVirtualX(int mouseX) {
        return mouseX - canvasX;
    }

    public int toVirtualY(int mouseY) {
        return mouseY - canvasY;
    }

    public int getVirtWidth() {
        return virtWidth;
    }

    public int getVirtHeight() {
        return virtHeight;
    }

    public int getCanvasX() {
        return canvasX;
    }

    public int getCanvasY() {
        return canvasY;
    }

    public int getCanvasWidth() {
        return canvasWidth;
    }

    public int getCanvasHeight() {
        return canvasHeight;
    }
}
