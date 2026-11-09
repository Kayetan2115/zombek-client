package pl.zombek.client.core.hud.editor;

import pl.zombek.client.core.hud.IHudRenderer;

import java.util.*;

/**
 * Top-level renderer for the HUD Editor interface.
 * Orchestrates toolbar (Undo, Redo, Profiles, Presets, Import/Export, Tools, Viewport, Aspect Ratio),
 * Canvas workspace, left structural sidebar, right property inspector, bottom status bar,
 * and comprehensive modal dialogs (Profiles, Presets, Import/Export, Warnings, Unsaved changes).
 */
public class HudEditorRenderer {

    private final HudEditorCanvas canvas = new HudEditorCanvas();
    private final HudEditorComponentList componentList = new HudEditorComponentList();
    private final HudEditorInspector inspector = new HudEditorInspector();

    public void render(IHudRenderer renderer, HudLayout layout, HudEditorState state,
                       HudSelectionManager selectionManager, int mouseX, int mouseY) {
        if (renderer == null || layout == null || state == null) return;

        int screenW = renderer.getScreenWidth();
        int screenH = renderer.getScreenHeight();

        int topBarH = 34;
        int bottomBarH = 34;
        int sidePanelH = screenH - topBarH - bottomBarH;

        int leftW = state.isLeftPanelCollapsed() ? 24 : 190;
        int rightW = state.isRightPanelCollapsed() ? 24 : 220;
        int centerW = screenW - leftW - rightW;

        // Update bounds
        componentList.updateBounds(0, topBarH, leftW, sidePanelH);
        inspector.updateBounds(screenW - rightW, topBarH, rightW, sidePanelH);
        canvas.updateBounds(leftW, topBarH, centerW, sidePanelH, screenW, screenH, state.getResolutionMode(), state.getViewport());

        // 1. Render Canvas Workspace
        canvas.render(renderer, layout, state, selectionManager, mouseX, mouseY);

        // 2. Render Left Component List and Right Inspector (if not in preview mode)
        if (!state.isPreviewMode()) {
            componentList.render(renderer, layout, state, selectionManager, mouseX, mouseY);
            inspector.render(renderer, layout, state, selectionManager, mouseX, mouseY);
        }

        // 3. Render Topbar & Toolbar
        renderTopbar(renderer, layout, state, screenW, topBarH, mouseX, mouseY);

        // 4. Render Bottombar
        renderBottombar(renderer, layout, state, screenW, screenH, bottomBarH, mouseX, mouseY);

        // 5. Render Modals if active
        renderActiveModal(renderer, layout, state, screenW, screenH, mouseX, mouseY);
    }

    private void renderTopbar(IHudRenderer renderer, HudLayout layout, HudEditorState state,
                              int screenW, int topBarH, int mouseX, int mouseY) {
        renderer.fill(0, 0, screenW, topBarH, 0xFA16181F);
        renderer.fill(0, topBarH - 1, screenW, 1, 0xFF2A2E3D);

        // Branding
        renderer.drawText("⚡ EDYTOR HUD", 10, 11, 0xFFFFCC00, true, 0.95f, true);

        int curX = 98;
        int btnY = 7;
        int btnH = 20;

        // --- UNDO / REDO BUTTONS ---
        boolean canUndo = state.getUndoRedoManager().canUndo();
        renderUndoRedoButton(renderer, curX, btnY, 22, btnH, "↺", canUndo, mouseX, mouseY);
        curX += 24;

        boolean canRedo = state.getUndoRedoManager().canRedo();
        renderUndoRedoButton(renderer, curX, btnY, 22, btnH, "↻", canRedo, mouseX, mouseY);
        curX += 28;

        // --- PROFILE DROPDOWN BUTTON ---
        String profName = state.getProfileManager().getActiveProfileName();
        String profLabel = "Profil: " + (profName.length() > 8 ? profName.substring(0, 8) + ".." : profName) + " ▾";
        int profBtnW = renderer.getTextWidth(profLabel) + 12;
        renderButton(renderer, curX, btnY, profBtnW, btnH, profLabel, state.getActiveModal() == HudEditorState.ActiveModal.PROFILES, mouseX, mouseY);
        curX += profBtnW + 4;

        // --- PRESET BUTTON ---
        renderButton(renderer, curX, btnY, 68, btnH, "Presety ▾", state.getActiveModal() == HudEditorState.ActiveModal.PRESETS, mouseX, mouseY);
        curX += 72;

        // --- IMPORT / EXPORT BUTTON ---
        renderButton(renderer, curX, btnY, 56, btnH, "⇅ Pliki", state.getActiveModal() == HudEditorState.ActiveModal.IMPORT_EXPORT, mouseX, mouseY);
        curX += 62;

        // --- TOOLS ---
        renderToolButton(renderer, curX, btnY, 24, btnH, "↖", state.getActiveTool() == EditorTool.SELECT, mouseX, mouseY);
        curX += 26;
        renderToolButton(renderer, curX, btnY, 24, btnH, "✥", state.getActiveTool() == EditorTool.MOVE, mouseX, mouseY);
        curX += 26;
        renderToolButton(renderer, curX, btnY, 24, btnH, "⤢", state.getActiveTool() == EditorTool.RESIZE, mouseX, mouseY);
        curX += 26;
        renderToolButton(renderer, curX, btnY, 24, btnH, "✋", state.getActiveTool() == EditorTool.PAN, mouseX, mouseY);
        curX += 30;

        // --- VIEWPORT CONTROLS ---
        renderButton(renderer, curX, btnY, 18, btnH, "-", false, mouseX, mouseY);
        curX += 20;
        renderButton(renderer, curX, btnY, 18, btnH, "+", false, mouseX, mouseY);
        curX += 20;
        renderButton(renderer, curX, btnY, 26, btnH, "1:1", false, mouseX, mouseY);
        curX += 28;
        renderButton(renderer, curX, btnY, 26, btnH, "⛶", false, mouseX, mouseY);
        curX += 30;

        // Grid [#] & Snap [🧲]
        boolean gridActive = state.getGridManager().isVisible();
        renderButton(renderer, curX, btnY, 26, btnH, "#", gridActive, mouseX, mouseY);
        curX += 28;
        boolean snapActive = state.getGridManager().isSnapToGrid();
        renderButton(renderer, curX, btnY, 28, btnH, "🧲", snapActive, mouseX, mouseY);
        curX += 32;

        // Aspect ratio simulation
        HudResponsiveLayoutManager respMgr = state.getResponsiveLayoutManager();
        String ratioLabel = respMgr.isSimulateEnabled() ? respMgr.getActiveRatio().getDisplayName() : "Format: Oryg.";
        renderButton(renderer, curX, btnY, 82, btnH, ratioLabel, respMgr.isSimulateEnabled(), mouseX, mouseY);
        curX += 86;

        // Preview [👁]
        boolean prevActive = state.isPreviewMode();
        renderButton(renderer, curX, btnY, 64, btnH, prevActive ? "👁 Gra" : "👁 Edytor", prevActive, mouseX, mouseY);

        // --- RIGHT-ALIGNED BUTTONS & WARNINGS ---
        List<LayoutValidationWarning> warnings = respMgr.validateLayout(layout);
        if (!warnings.isEmpty()) {
            int warnBtnW = 86;
            int warnBtnX = screenW - 190;
            boolean wHover = mouseX >= warnBtnX && mouseX <= warnBtnX + warnBtnW && mouseY >= btnY && mouseY <= btnY + btnH;
            renderer.fill(warnBtnX, btnY, warnBtnW, btnH, wHover ? 0xFF553311 : 0xFF3D230B);
            renderer.drawBorder(warnBtnX, btnY, warnBtnW, btnH, 0xFFFF9900, 1);
            renderer.drawCenteredTextWithShadow("⚠️ " + warnings.size() + " uwag", warnBtnX + warnBtnW / 2, btnY + 5, 0xFFFFBB33);
        }

        // Resolution Mode Toggle
        int resBtnW = 80;
        int resBtnX = screenW - 90;
        boolean resHover = mouseX >= resBtnX && mouseX <= resBtnX + resBtnW && mouseY >= btnY && mouseY <= btnY + btnH;
        renderer.fill(resBtnX, btnY, resBtnW, btnH, resHover ? 0xFF353A4C : 0xFF232734);
        renderer.drawBorder(resBtnX, btnY, resBtnW, btnH, 0xFF4A90E2, 1);
        renderer.drawCenteredTextWithShadow(state.getResolutionMode().getTitle(), resBtnX + resBtnW / 2, btnY + 5, 0xFF4A90E2);
    }

    private void renderUndoRedoButton(IHudRenderer renderer, int bx, int by, int bw, int bh,
                                      String icon, boolean enabled, int mouseX, int mouseY) {
        boolean hover = enabled && mouseX >= bx && mouseX <= bx + bw && mouseY >= by && mouseY <= by + bh;
        int bg = hover ? 0xFF353A4C : (enabled ? 0xFF232734 : 0xFF181A22);
        int border = enabled ? 0xFF4A90E2 : 0xFF20232E;
        renderer.fill(bx, by, bw, bh, bg);
        renderer.drawBorder(bx, by, bw, bh, border, 1);
        renderer.drawCenteredTextWithShadow(icon, bx + bw / 2, by + 5, enabled ? 0xFFFFFFFF : 0xFF555555);
    }

    private void renderToolButton(IHudRenderer renderer, int bx, int by, int bw, int bh,
                                  String icon, boolean isActive, int mouseX, int mouseY) {
        boolean hover = mouseX >= bx && mouseX <= bx + bw && mouseY >= by && mouseY <= by + bh;
        int bg = isActive ? 0xFF4A90E2 : (hover ? 0xFF353A4C : 0xFF232734);
        int border = isActive ? 0xFF88CCFF : 0xFF2A2E3D;
        renderer.fill(bx, by, bw, bh, bg);
        renderer.drawBorder(bx, by, bw, bh, border, 1);
        renderer.drawCenteredTextWithShadow(icon, bx + bw / 2, by + 5, isActive ? 0xFFFFFFFF : 0xFFCCCCCC);
    }

    private void renderButton(IHudRenderer renderer, int bx, int by, int bw, int bh,
                              String text, boolean isActive, int mouseX, int mouseY) {
        boolean hover = mouseX >= bx && mouseX <= bx + bw && mouseY >= by && mouseY <= by + bh;
        int bg = isActive ? 0xFF355E3B : (hover ? 0xFF353A4C : 0xFF232734);
        int border = isActive ? 0xFF55FF55 : 0xFF2A2E3D;
        renderer.fill(bx, by, bw, bh, bg);
        renderer.drawBorder(bx, by, bw, bh, border, 1);
        renderer.drawCenteredTextWithShadow(text, bx + bw / 2, by + 5, isActive ? 0xFF55FF55 : 0xFFCCCCCC);
    }

    private void renderBottombar(IHudRenderer renderer, HudLayout layout, HudEditorState state,
                                 int screenW, int screenH, int bottomBarH, int mouseX, int mouseY) {
        int bbY = screenH - bottomBarH;
        renderer.fill(0, bbY, screenW, bottomBarH, 0xFA16181F);
        renderer.fill(0, bbY, screenW, 1, 0xFF2A2E3D);

        // Status pill
        boolean hasUnsaved = HudLayoutManager.getInstance().hasUnsavedChanges();
        renderer.fill(14, bbY + 11, 8, 8, hasUnsaved ? 0xFFFF9900 : 0xFF55FF55);

        String undoStatus = state.getUndoRedoManager().canUndo()
                ? "Ostatnia akcja: " + state.getUndoRedoManager().getUndoHistory().get(state.getUndoRedoManager().getUndoHistory().size() - 1).getDescription()
                : "Układ zsynchronizowany";
        String statusText = hasUnsaved
                ? "Niezapisane zmiany • Profil: [" + state.getProfileManager().getActiveProfileName() + "]"
                : undoStatus + " • Profil: [" + state.getProfileManager().getActiveProfileName() + "]";
        renderer.drawText(statusText, 28, bbY + 9, hasUnsaved ? 0xFFFFBB33 : 0xFF88AA88, false, 0.85f, false);

        // Action Buttons
        int btnW = 90;
        int btnH = 22;
        int btnY = bbY + 6;

        // Reset to Defaults
        int resetX = screenW - 320;
        boolean resetHover = mouseX >= resetX && mouseX <= resetX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        renderer.fill(resetX, btnY, btnW, btnH, resetHover ? 0xFF442222 : 0xFF2B1D1D);
        renderer.drawBorder(resetX, btnY, btnW, btnH, 0xFFFF5555, 1);
        renderer.drawCenteredTextWithShadow("↺ Domyślne", resetX + btnW / 2, btnY + 6, 0xFFFF8888);

        // Cancel Button
        int cancelX = screenW - 220;
        boolean cancelHover = mouseX >= cancelX && mouseX <= cancelX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        renderer.fill(cancelX, btnY, btnW, btnH, cancelHover ? 0xFF353A4C : 0xFF232734);
        renderer.drawBorder(cancelX, btnY, btnW, btnH, 0xFF2A2E3D, 1);
        renderer.drawCenteredTextWithShadow("✕ Anuluj", cancelX + btnW / 2, btnY + 6, 0xFFCCCCCC);

        // Save Button
        int saveX = screenW - 120;
        boolean saveHover = mouseX >= saveX && mouseX <= saveX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        renderer.fill(saveX, btnY, btnW, btnH, saveHover ? 0xFF2E6930 : 0xFF1E4620);
        renderer.drawBorder(saveX, btnY, btnW, btnH, 0xFF55FF55, 1);
        renderer.drawCenteredTextWithShadow("💾 Zapisz", saveX + btnW / 2, btnY + 6, 0xFFFFFFFF);
    }

    private void renderActiveModal(IHudRenderer renderer, HudLayout layout, HudEditorState state,
                                   int screenW, int screenH, int mouseX, int mouseY) {
        HudEditorState.ActiveModal modal = state.getActiveModal();
        if (state.isShowUnsavedDialog() || modal == HudEditorState.ActiveModal.UNSAVED_CHANGES) {
            renderUnsavedDialog(renderer, screenW, screenH, mouseX, mouseY);
        } else if (modal == HudEditorState.ActiveModal.PROFILES) {
            renderProfilesModal(renderer, state, screenW, screenH, mouseX, mouseY);
        } else if (modal == HudEditorState.ActiveModal.PRESETS) {
            renderPresetsModal(renderer, layout, state, screenW, screenH, mouseX, mouseY);
        } else if (modal == HudEditorState.ActiveModal.IMPORT_EXPORT) {
            renderImportExportModal(renderer, layout, state, screenW, screenH, mouseX, mouseY);
        } else if (modal == HudEditorState.ActiveModal.WARNINGS) {
            renderWarningsModal(renderer, layout, state, screenW, screenH, mouseX, mouseY);
        }
    }

    private void renderUnsavedDialog(IHudRenderer renderer, int screenW, int screenH, int mouseX, int mouseY) {
        renderer.fill(0, 0, screenW, screenH, 0xAA000000);
        int modalW = 340;
        int modalH = 140;
        int modalX = (screenW - modalW) / 2;
        int modalY = (screenH - modalH) / 2;

        renderer.fill(modalX, modalY, modalW, modalH, 0xFA1E212B);
        renderer.drawBorder(modalX, modalY, modalW, modalH, 0xFF4A90E2, 2);

        renderer.drawCenteredTextWithShadow("Niezapisane zmiany", modalX + modalW / 2, modalY + 16, 0xFFFFCC00);
        renderer.drawCenteredTextWithShadow("Posiadasz zmodyfikowany układ HUD.", modalX + modalW / 2, modalY + 40, 0xFFE0E0E0);
        renderer.drawCenteredTextWithShadow("Czy chcesz zapisać zmiany przed wyjściem?", modalX + modalW / 2, modalY + 54, 0xFFAAAAAA);

        int btnW = 95;
        int btnH = 22;
        int btnY = modalY + 95;

        // Save
        int saveBtnX = modalX + 16;
        boolean saveHover = mouseX >= saveBtnX && mouseX <= saveBtnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        renderer.fill(saveBtnX, btnY, btnW, btnH, saveHover ? 0xFF2E6930 : 0xFF1E4620);
        renderer.drawBorder(saveBtnX, btnY, btnW, btnH, 0xFF55FF55, 1);
        renderer.drawCenteredTextWithShadow("Zapisz", saveBtnX + btnW / 2, btnY + 6, 0xFFFFFFFF);

        // Discard
        int discardBtnX = modalX + 122;
        boolean discardHover = mouseX >= discardBtnX && mouseX <= discardBtnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        renderer.fill(discardBtnX, btnY, btnW, btnH, discardHover ? 0xFF553333 : 0xFF332222);
        renderer.drawBorder(discardBtnX, btnY, btnW, btnH, 0xFFFF5555, 1);
        renderer.drawCenteredTextWithShadow("Odrzuć", discardBtnX + btnW / 2, btnY + 6, 0xFFFF8888);

        // Back
        int backBtnX = modalX + 228;
        boolean backHover = mouseX >= backBtnX && mouseX <= backBtnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        renderer.fill(backBtnX, btnY, btnW, btnH, backHover ? 0xFF353A4C : 0xFF232734);
        renderer.drawBorder(backBtnX, btnY, btnW, btnH, 0xFF2A2E3D, 1);
        renderer.drawCenteredTextWithShadow("Wróć", backBtnX + btnW / 2, btnY + 6, 0xFFCCCCCC);
    }

    private void renderProfilesModal(IHudRenderer renderer, HudEditorState state, int screenW, int screenH, int mouseX, int mouseY) {
        renderer.fill(0, 0, screenW, screenH, 0xAA000000);
        int modalW = 400;
        int modalH = 280;
        int modalX = (screenW - modalW) / 2;
        int modalY = (screenH - modalH) / 2;

        renderer.fill(modalX, modalY, modalW, modalH, 0xFA1E212B);
        renderer.drawBorder(modalX, modalY, modalW, modalH, 0xFF4A90E2, 2);

        renderer.drawCenteredTextWithShadow("Menedżer Profili HUD", modalX + modalW / 2, modalY + 14, 0xFFFFCC00);

        List<HudProfileMetadata> profiles = state.getProfileManager().listProfiles();
        int curY = modalY + 40;
        int itemH = 24;
        String activeProf = state.getProfileManager().getActiveProfileName();

        for (HudProfileMetadata p : profiles) {
            boolean isActive = p.getName().equalsIgnoreCase(activeProf);
            boolean hover = mouseX >= modalX + 16 && mouseX <= modalX + modalW - 16 && mouseY >= curY && mouseY <= curY + itemH;
            renderer.fill(modalX + 16, curY, modalW - 32, itemH, isActive ? 0xFF2D384E : (hover ? 0xFF2A2E3D : 0xFF1C1E27));
            renderer.drawBorder(modalX + 16, curY, modalW - 32, itemH, isActive ? 0xFF55FF55 : 0xFF2A2E3D, 1);

            String icon = isActive ? "✔ " : "  ";
            renderer.drawText(icon + p.getName(), modalX + 24, curY + 6, isActive ? 0xFF55FF55 : 0xFFE0E0E0, true, 0.85f, true);
            String desc = p.isReadOnly() ? "[Domyślny]" : "[Własny]";
            renderer.drawText(desc, modalX + modalW - 80, curY + 6, 0xFF888888, false, 0.75f, false);

            curY += itemH + 4;
        }

        // Close button
        int closeBtnW = 90;
        int closeBtnY = modalY + modalH - 32;
        int closeBtnX = modalX + modalW - closeBtnW - 16;
        boolean closeHover = mouseX >= closeBtnX && mouseX <= closeBtnX + closeBtnW && mouseY >= closeBtnY && mouseY <= closeBtnY + 22;
        renderer.fill(closeBtnX, closeBtnY, closeBtnW, 22, closeHover ? 0xFF353A4C : 0xFF232734);
        renderer.drawBorder(closeBtnX, closeBtnY, closeBtnW, 22, 0xFF2A2E3D, 1);
        renderer.drawCenteredTextWithShadow("Zamknij", closeBtnX + closeBtnW / 2, closeBtnY + 6, 0xFFCCCCCC);
    }

    private void renderPresetsModal(IHudRenderer renderer, HudLayout layout, HudEditorState state, int screenW, int screenH, int mouseX, int mouseY) {
        renderer.fill(0, 0, screenW, screenH, 0xAA000000);
        int modalW = 440;
        int modalH = 320;
        int modalX = (screenW - modalW) / 2;
        int modalY = (screenH - modalH) / 2;

        renderer.fill(modalX, modalY, modalW, modalH, 0xFA1E212B);
        renderer.drawBorder(modalX, modalY, modalW, modalH, 0xFF4A90E2, 2);

        renderer.drawCenteredTextWithShadow("Wybierz Gotowy Preset HUD", modalX + modalW / 2, modalY + 14, 0xFFFFCC00);

        Collection<HudPreset> presets = state.getPresetManager().getAllPresets();
        int curY = modalY + 38;
        int itemH = 28;

        for (HudPreset p : presets) {
            boolean hover = mouseX >= modalX + 16 && mouseX <= modalX + modalW - 16 && mouseY >= curY && mouseY <= curY + itemH;
            renderer.fill(modalX + 16, curY, modalW - 32, itemH, hover ? 0xFF2D384E : 0xFF1C1E27);
            renderer.drawBorder(modalX + 16, curY, modalW - 32, itemH, hover ? 0xFF4A90E2 : 0xFF2A2E3D, 1);

            renderer.drawText(p.getName(), modalX + 24, curY + 6, 0xFFE0E0E0, true, 0.85f, true);
            renderer.drawText(p.getDescription(), modalX + 24, curY + 18, 0xFF888888, false, 0.7f, false);

            curY += itemH + 3;
        }

        // Close button
        int closeBtnW = 90;
        int closeBtnY = modalY + modalH - 32;
        int closeBtnX = modalX + modalW - closeBtnW - 16;
        boolean closeHover = mouseX >= closeBtnX && mouseX <= closeBtnX + closeBtnW && mouseY >= closeBtnY && mouseY <= closeBtnY + 22;
        renderer.fill(closeBtnX, closeBtnY, closeBtnW, 22, closeHover ? 0xFF353A4C : 0xFF232734);
        renderer.drawBorder(closeBtnX, closeBtnY, closeBtnW, 22, 0xFF2A2E3D, 1);
        renderer.drawCenteredTextWithShadow("Zamknij", closeBtnX + closeBtnW / 2, closeBtnY + 6, 0xFFCCCCCC);
    }

    private void renderImportExportModal(IHudRenderer renderer, HudLayout layout, HudEditorState state, int screenW, int screenH, int mouseX, int mouseY) {
        renderer.fill(0, 0, screenW, screenH, 0xAA000000);
        int modalW = 400;
        int modalH = 200;
        int modalX = (screenW - modalW) / 2;
        int modalY = (screenH - modalH) / 2;

        renderer.fill(modalX, modalY, modalW, modalH, 0xFA1E212B);
        renderer.drawBorder(modalX, modalY, modalW, modalH, 0xFF4A90E2, 2);

        renderer.drawCenteredTextWithShadow("Import / Eksport Układu", modalX + modalW / 2, modalY + 14, 0xFFFFCC00);
        renderer.drawCenteredTextWithShadow("Zarządzaj plikami konfiguracji HUD (JSON)", modalX + modalW / 2, modalY + 36, 0xFFCCCCCC);

        int btnW = 160;
        int btnH = 24;

        // Export button
        int expX = modalX + 24;
        int expY = modalY + 70;
        boolean expHover = mouseX >= expX && mouseX <= expX + btnW && mouseY >= expY && mouseY <= expY + btnH;
        renderer.fill(expX, expY, btnW, btnH, expHover ? 0xFF2E6930 : 0xFF1E4620);
        renderer.drawBorder(expX, expY, btnW, btnH, 0xFF55FF55, 1);
        renderer.drawCenteredTextWithShadow("⬇ Eksportuj do pliku", expX + btnW / 2, expY + 7, 0xFFFFFFFF);

        // Import Replace button
        int impX = modalX + modalW - btnW - 24;
        boolean impHover = mouseX >= impX && mouseX <= impX + btnW && mouseY >= expY && mouseY <= expY + btnH;
        renderer.fill(impX, expY, btnW, btnH, impHover ? 0xFF353A4C : 0xFF232734);
        renderer.drawBorder(impX, expY, btnW, btnH, 0xFF4A90E2, 1);
        renderer.drawCenteredTextWithShadow("⬆ Importuj (Zastąp)", impX + btnW / 2, expY + 7, 0xFFE0E0E0);

        // Close button
        int closeBtnW = 90;
        int closeBtnY = modalY + modalH - 36;
        int closeBtnX = modalX + (modalW - closeBtnW) / 2;
        boolean closeHover = mouseX >= closeBtnX && mouseX <= closeBtnX + closeBtnW && mouseY >= closeBtnY && mouseY <= closeBtnY + 22;
        renderer.fill(closeBtnX, closeBtnY, closeBtnW, 22, closeHover ? 0xFF353A4C : 0xFF232734);
        renderer.drawBorder(closeBtnX, closeBtnY, closeBtnW, 22, 0xFF2A2E3D, 1);
        renderer.drawCenteredTextWithShadow("Zamknij", closeBtnX + closeBtnW / 2, closeBtnY + 6, 0xFFCCCCCC);
    }

    private void renderWarningsModal(IHudRenderer renderer, HudLayout layout, HudEditorState state, int screenW, int screenH, int mouseX, int mouseY) {
        renderer.fill(0, 0, screenW, screenH, 0xAA000000);
        int modalW = 460;
        int modalH = 280;
        int modalX = (screenW - modalW) / 2;
        int modalY = (screenH - modalH) / 2;

        renderer.fill(modalX, modalY, modalW, modalH, 0xFA1E212B);
        renderer.drawBorder(modalX, modalY, modalW, modalH, 0xFFFF9900, 2);

        renderer.drawCenteredTextWithShadow("Walidacja Układu HUD", modalX + modalW / 2, modalY + 14, 0xFFFFCC00);

        List<LayoutValidationWarning> warnings = state.getResponsiveLayoutManager().validateLayout(layout);
        int curY = modalY + 40;
        int itemH = 26;

        for (LayoutValidationWarning w : warnings) {
            renderer.fill(modalX + 16, curY, modalW - 32, itemH, 0xFF2A2218);
            renderer.drawBorder(modalX + 16, curY, modalW - 32, itemH, 0xFFFF9900, 1);
            renderer.drawText("⚠️ [" + w.getType().name() + "] " + w.getMessage(), modalX + 22, curY + 7, 0xFFFFDD66, false, 0.75f, false);
            curY += itemH + 4;
        }

        // Close button
        int closeBtnW = 90;
        int closeBtnY = modalY + modalH - 32;
        int closeBtnX = modalX + modalW - closeBtnW - 16;
        boolean closeHover = mouseX >= closeBtnX && mouseX <= closeBtnX + closeBtnW && mouseY >= closeBtnY && mouseY <= closeBtnY + 22;
        renderer.fill(closeBtnX, closeBtnY, closeBtnW, 22, closeHover ? 0xFF353A4C : 0xFF232734);
        renderer.drawBorder(closeBtnX, closeBtnY, closeBtnW, 22, 0xFF2A2E3D, 1);
        renderer.drawCenteredTextWithShadow("Zamknij", closeBtnX + closeBtnW / 2, closeBtnY + 6, 0xFFCCCCCC);
    }

    public HudEditorCanvas getCanvas() {
        return canvas;
    }

    public HudEditorComponentList getComponentList() {
        return componentList;
    }

    public HudEditorInspector getInspector() {
        return inspector;
    }
}
