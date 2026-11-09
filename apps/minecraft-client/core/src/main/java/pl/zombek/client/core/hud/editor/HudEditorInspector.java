package pl.zombek.client.core.hud.editor;

import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.IHudRenderer;

import java.util.*;

/**
 * Property inspector panel for HUD components.
 * Supports:
 * - Single-component fine-grained geometry editing, step buttons, aspect ratio lock, auto-size.
 * - Multi-selection inspection (bulk lock, bulk visibility, bulk scaling, group creation, bulk alignment).
 * - Layer assignment and Z-index depth controls (bring to front, send to back, move up/down).
 * - Component clipboard operations (copy, duplicate).
 */
public class HudEditorInspector {

    public enum ActiveField {
        NONE,
        POS_X,
        POS_Y,
        WIDTH,
        HEIGHT,
        SCALE
    }

    private int x;
    private int y;
    private int width;
    private int height;

    private ActiveField activeField = ActiveField.NONE;
    private String inputBuffer = "";

    public void updateBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void render(IHudRenderer renderer, HudLayout layout, HudEditorState state,
                       HudSelectionManager selectionManager, int mouseX, int mouseY) {
        if (state.isRightPanelCollapsed()) {
            renderCollapsedStrip(renderer, mouseX, mouseY);
            return;
        }

        // Background & left border
        renderer.fill(x, y, width, height, 0xEE16181F);
        renderer.fill(x, y, 1, height, 0xFF2A2E3D);

        // Header & Collapse Button
        renderer.drawText("INSPEKTOR WŁAŚCIWOŚCI", x + 12, y + 10, 0xFFE0E0E0, true, 0.9f, true);

        int collapseBtnX = x + width - 24;
        int collapseBtnY = y + 8;
        boolean collapseHover = mouseX >= collapseBtnX && mouseX <= collapseBtnX + 16 && mouseY >= collapseBtnY && mouseY <= collapseBtnY + 16;
        renderer.fill(collapseBtnX, collapseBtnY, 16, 16, collapseHover ? 0xFF353A4C : 0xFF232734);
        renderer.drawText("▶", collapseBtnX + 4, collapseBtnY + 4, 0xFFCCCCCC, false, 0.8f, false);

        HudMultiSelectionManager multiSel = state.getMultiSelectionManager();
        if (multiSel.getSelectedCount() > 1) {
            renderMultiSelectionProperties(renderer, layout, state, multiSel, mouseX, mouseY);
            return;
        }

        String selectedId = selectionManager.getSelectedComponentId();
        if (selectedId == null) {
            renderNoSelection(renderer);
            return;
        }

        HudComponentRegistry registry = HudComponentRegistry.getInstance();
        Optional<HudComponentDescriptor> optDesc = registry.getDescriptor(selectedId);
        HudComponentState compState = layout.getComponentState(selectedId);

        if (!optDesc.isPresent() || compState == null) {
            renderNoSelection(renderer);
            return;
        }

        HudComponentDescriptor desc = optDesc.get();
        renderComponentProperties(renderer, desc, compState, state, layout, mouseX, mouseY);
    }

    private void renderCollapsedStrip(IHudRenderer renderer, int mouseX, int mouseY) {
        renderer.fill(x, y, 24, height, 0xEE16181F);
        renderer.fill(x, y, 1, height, 0xFF2A2E3D);

        boolean hover = mouseX >= x && mouseX <= x + 24 && mouseY >= y + 8 && mouseY <= y + 24;
        renderer.fill(x + 4, y + 8, 16, 16, hover ? 0xFF353A4C : 0xFF232734);
        renderer.drawText("◀", x + 9, y + 12, 0xFFCCCCCC, false, 0.8f, false);
    }

    private void renderNoSelection(IHudRenderer renderer) {
        int cy = y + 40;
        renderer.drawText("Brak zaznaczenia", x + 12, cy, 0xFF888888, false, 0.85f, true);
        renderer.drawText("Wybierz element z listy", x + 12, cy + 18, 0xFF666666, false, 0.75f, false);
        renderer.drawText("lub kliknij na Canvasie,", x + 12, cy + 30, 0xFF666666, false, 0.75f, false);
        renderer.drawText("aby edytować jego geometrię.", x + 12, cy + 42, 0xFF666666, false, 0.75f, false);
    }

    private void renderMultiSelectionProperties(IHudRenderer renderer, HudLayout layout, HudEditorState state,
                                               HudMultiSelectionManager multiSel, int mouseX, int mouseY) {
        int curY = y + 32;
        int count = multiSel.getSelectedCount();

        renderer.drawText("WIELOKROTNE ZAZNACZENIE", x + 12, curY, 0xFFFFCC00, true, 0.9f, true);
        curY += 16;
        renderer.drawText("Zaznaczono: " + count + " elementów", x + 12, curY, 0xFF4A90E2, false, 0.8f, false);
        curY += 16;

        // Display summary of selected IDs
        StringBuilder sb = new StringBuilder();
        int shown = 0;
        for (String id : multiSel.getSelectedIds()) {
            if (shown > 0) sb.append(", ");
            sb.append(id);
            shown++;
            if (shown >= 4) {
                if (count > 4) sb.append("...");
                break;
            }
        }
        renderer.drawText(sb.toString(), x + 12, curY, 0xFF888888, false, 0.7f, false);
        curY += 20;

        int btnW = width - 24;
        int btnH = 20;
        int halfW = (width - 28) / 2;

        // Row 1: Bulk Lock & Visibility
        boolean lockHover = mouseX >= x + 12 && mouseX <= x + 12 + halfW && mouseY >= curY && mouseY <= curY + btnH;
        renderer.fill(x + 12, curY, halfW, btnH, lockHover ? 0xFF442222 : 0xFF232734);
        renderer.drawBorder(x + 12, curY, halfW, btnH, 0xFF2A2E3D, 1);
        renderer.drawCenteredTextWithShadow("🔒 Przełącz blokadę", x + 12 + halfW / 2, curY + 6, 0xFFCCCCCC);

        boolean visHover = mouseX >= x + 16 + halfW && mouseX <= x + 16 + 2 * halfW && mouseY >= curY && mouseY <= curY + btnH;
        renderer.fill(x + 16 + halfW, curY, halfW, btnH, visHover ? 0xFF353A4C : 0xFF232734);
        renderer.drawBorder(x + 16 + halfW, curY, halfW, btnH, 0xFF2A2E3D, 1);
        renderer.drawCenteredTextWithShadow("👁 Przełącz widoczność", x + 16 + halfW + halfW / 2, curY + 6, 0xFF55FF55);
        curY += 26;

        // Row 2: Create Group button
        boolean grpHover = mouseX >= x + 12 && mouseX <= x + 12 + btnW && mouseY >= curY && mouseY <= curY + btnH;
        renderer.fill(x + 12, curY, btnW, btnH, grpHover ? 0xFF2E6930 : 0xFF1E4620);
        renderer.drawBorder(x + 12, curY, btnW, btnH, 0xFF55FF55, 1);
        renderer.drawCenteredTextWithShadow("📁 Utwórz grupę (Ctrl+G)", x + 12 + btnW / 2, curY + 6, 0xFFFFFFFF);
        curY += 26;

        // Row 3: Bulk Scaling
        renderer.drawText("Skala zaznaczenia:", x + 12, curY, 0xFFCCCCCC, false, 0.75f, true);
        curY += 12;
        renderScaleButtons(renderer, x + 12, curY, mouseX, mouseY);
        curY += 26;

        // Row 4: Bulk Alignment
        renderer.drawText("Wyrównaj elementy do ekranu:", x + 12, curY, 0xFFCCCCCC, false, 0.75f, true);
        curY += 12;
        renderAlignmentButtons(renderer, x + 12, curY, width - 24, mouseX, mouseY);
        curY += 28;

        // Row 5: Clipboard (Duplicate / Copy)
        boolean dupHover = mouseX >= x + 12 && mouseX <= x + 12 + halfW && mouseY >= curY && mouseY <= curY + btnH;
        renderer.fill(x + 12, curY, halfW, btnH, dupHover ? 0xFF353A4C : 0xFF232734);
        renderer.drawBorder(x + 12, curY, halfW, btnH, 0xFF4A90E2, 1);
        renderer.drawCenteredTextWithShadow("📋 Duplikuj (Ctrl+D)", x + 12 + halfW / 2, curY + 6, 0xFFE0E0E0);

        boolean copyHover = mouseX >= x + 16 + halfW && mouseX <= x + 16 + 2 * halfW && mouseY >= curY && mouseY <= curY + btnH;
        renderer.fill(x + 16 + halfW, curY, halfW, btnH, copyHover ? 0xFF353A4C : 0xFF232734);
        renderer.drawBorder(x + 16 + halfW, curY, halfW, btnH, 0xFF4A90E2, 1);
        renderer.drawCenteredTextWithShadow("📄 Kopiuj (Ctrl+C)", x + 16 + halfW + halfW / 2, curY + 6, 0xFFE0E0E0);
        curY += 28;

        // Row 6: Clear selection
        curY = Math.max(curY + 6, y + height - 32);
        boolean clearHover = mouseX >= x + 12 && mouseX <= x + 12 + btnW && mouseY >= curY && mouseY <= curY + btnH;
        renderer.fill(x + 12, curY, btnW, btnH, clearHover ? 0xFF442222 : 0xFF2B1D1D);
        renderer.drawBorder(x + 12, curY, btnW, btnH, 0xFFFF5555, 1);
        renderer.drawCenteredTextWithShadow("✕ Odznacz wszystko (Esc)", x + 12 + btnW / 2, curY + 6, 0xFFFF8888);
    }

    private void renderComponentProperties(IHudRenderer renderer, HudComponentDescriptor desc,
                                           HudComponentState compState, HudEditorState editorState,
                                           HudLayout layout, int mouseX, int mouseY) {
        int curY = y + 28;

        // Title and Category
        renderer.drawText(desc.getDisplayName(), x + 12, curY, 0xFFFFCC00, true, 0.95f, true);
        curY += 14;
        renderer.drawText("Typ: " + desc.getType().getTitle(), x + 12, curY, 0xFF4A90E2, false, 0.75f, false);
        curY += 14;

        // WORLD_SPACE Warning
        if (desc.getType() == HudComponentType.WORLD_SPACE) {
            renderer.fill(x + 10, curY, width - 20, 24, 0x334A90E2);
            renderer.drawBorder(x + 10, curY, width - 20, 24, 0x664A90E2, 1);
            renderer.drawText("ⓘ Element świata gry 3D (Pozycja z encji)", x + 14, curY + 6, 0xFF55FFFF, false, 0.7f, false);
            curY += 28;
        }

        // Row 1: Lock & Editor Visibility
        int halfW = (width - 28) / 2;
        int btnH = 18;

        // Lock button
        boolean lockHover = mouseX >= x + 12 && mouseX <= x + 12 + halfW && mouseY >= curY && mouseY <= curY + btnH;
        int lockColor = compState.isLocked() ? 0xFF883333 : 0xFF232734;
        renderer.fill(x + 12, curY, halfW, btnH, lockHover ? 0xFF442222 : lockColor);
        renderer.drawBorder(x + 12, curY, halfW, btnH, compState.isLocked() ? 0xFFFF5555 : 0xFF2A2E3D, 1);
        String lockLabel = compState.isLocked() ? "🔒 Zablokuj" : "🔓 Odblokuj";
        renderer.drawCenteredTextWithShadow(lockLabel, x + 12 + halfW / 2, curY + 5, compState.isLocked() ? 0xFFFF7777 : 0xFFCCCCCC);

        // Visibility button
        boolean visHover = mouseX >= x + 16 + halfW && mouseX <= x + 16 + 2 * halfW && mouseY >= curY && mouseY <= curY + btnH;
        renderer.fill(x + 16 + halfW, curY, halfW, btnH, visHover ? 0xFF353A4C : 0xFF232734);
        renderer.drawBorder(x + 16 + halfW, curY, halfW, btnH, compState.isVisible() ? 0xFF55FF55 : 0xFF555555, 1);
        String visLabel = compState.isVisible() ? "👁 Widoczny" : "🚫 Ukryty";
        renderer.drawCenteredTextWithShadow(visLabel, x + 16 + halfW + halfW / 2, curY + 5, compState.isVisible() ? 0xFF55FF55 : 0xFF888888);
        curY += 22;

        // Section: Layer & Z-Index
        renderer.drawText("Warstwa & Kolejność (Z: " + compState.getZIndex() + "):", x + 12, curY, 0xFFCCCCCC, false, 0.75f, true);
        curY += 12;
        renderLayerControls(renderer, x + 12, curY, width - 24, compState, layout, mouseX, mouseY);
        curY += 24;

        // Section: Position
        if (desc.hasCapability(HudComponentCapability.POSITION)) {
            renderer.drawText("Pozycja (X / Y):", x + 12, curY, 0xFFCCCCCC, false, 0.75f, true);
            curY += 12;

            // X Box with input
            renderInputField(renderer, x + 12, curY, 65, 18, "X:",
                    activeField == ActiveField.POS_X ? inputBuffer : String.valueOf(compState.getPosX()),
                    activeField == ActiveField.POS_X, mouseX, mouseY);
            renderStepButtons(renderer, x + 82, curY, "X", mouseX, mouseY);
            curY += 22;

            // Y Box with input
            renderInputField(renderer, x + 12, curY, 65, 18, "Y:",
                    activeField == ActiveField.POS_Y ? inputBuffer : String.valueOf(compState.getPosY()),
                    activeField == ActiveField.POS_Y, mouseX, mouseY);
            renderStepButtons(renderer, x + 82, curY, "Y", mouseX, mouseY);
            curY += 24;
        }

        // Section: Dimensions (Width & Height)
        if (desc.hasCapability(HudComponentCapability.SIZE)) {
            renderer.drawText("Wymiary (Szerokość / Wysokość):", x + 12, curY, 0xFFCCCCCC, false, 0.75f, true);
            curY += 12;

            renderInputField(renderer, x + 12, curY, 65, 18, "W:",
                    activeField == ActiveField.WIDTH ? inputBuffer : String.valueOf(compState.getWidth()),
                    activeField == ActiveField.WIDTH, mouseX, mouseY);
            renderStepButtons(renderer, x + 82, curY, "W", mouseX, mouseY);
            curY += 22;

            renderInputField(renderer, x + 12, curY, 65, 18, "H:",
                    activeField == ActiveField.HEIGHT ? inputBuffer : String.valueOf(compState.getHeight()),
                    activeField == ActiveField.HEIGHT, mouseX, mouseY);
            renderStepButtons(renderer, x + 82, curY, "H", mouseX, mouseY);
            curY += 24;

            // Lock Aspect Ratio & Auto Size
            boolean aspectHover = mouseX >= x + 12 && mouseX <= x + 12 + halfW && mouseY >= curY && mouseY <= curY + btnH;
            renderer.fill(x + 12, curY, halfW, btnH, aspectHover ? 0xFF353A4C : 0xFF232734);
            renderer.drawBorder(x + 12, curY, halfW, btnH, compState.isLockAspectRatio() ? 0xFF4A90E2 : 0xFF2A2E3D, 1);
            String aspectLabel = compState.isLockAspectRatio() ? "🔒 Proporcje" : "🔓 Wolny";
            renderer.drawCenteredTextWithShadow(aspectLabel, x + 12 + halfW / 2, curY + 5, compState.isLockAspectRatio() ? 0xFF55FFFF : 0xFFAAAAAA);

            boolean autoHover = mouseX >= x + 16 + halfW && mouseX <= x + 16 + 2 * halfW && mouseY >= curY && mouseY <= curY + btnH;
            renderer.fill(x + 16 + halfW, curY, halfW, btnH, autoHover ? 0xFF353A4C : 0xFF232734);
            renderer.drawBorder(x + 16 + halfW, curY, halfW, btnH, compState.isAutoSize() ? 0xFF4A90E2 : 0xFF2A2E3D, 1);
            String autoLabel = compState.isAutoSize() ? "Auto Rozmiar" : "Ręczny";
            renderer.drawCenteredTextWithShadow(autoLabel, x + 16 + halfW + halfW / 2, curY + 5, compState.isAutoSize() ? 0xFF55FFFF : 0xFFAAAAAA);
            curY += 26;
        }

        // Section: Scale
        if (desc.hasCapability(HudComponentCapability.SCALE)) {
            renderer.drawText("Skala powiększenia:", x + 12, curY, 0xFFCCCCCC, false, 0.75f, true);
            curY += 12;

            renderInputField(renderer, x + 12, curY, 65, 18, "S:",
                    activeField == ActiveField.SCALE ? inputBuffer : String.format(Locale.ROOT, "%.2f", compState.getScale()),
                    activeField == ActiveField.SCALE, mouseX, mouseY);
            renderScaleButtons(renderer, x + 82, curY, mouseX, mouseY);
            curY += 24;
        }

        // Section: Anchor Point
        if (desc.hasCapability(HudComponentCapability.ANCHOR)) {
            renderer.drawText("Punkt zakotwiczenia:", x + 12, curY, 0xFFCCCCCC, false, 0.75f, true);
            curY += 12;

            int anchorBtnW = width - 24;
            int anchorBtnH = 20;
            boolean anchorHover = mouseX >= x + 12 && mouseX <= x + 12 + anchorBtnW && mouseY >= curY && mouseY <= curY + anchorBtnH;
            renderer.fill(x + 12, curY, anchorBtnW, anchorBtnH, anchorHover ? 0xFF353A4C : 0xFF232734);
            renderer.drawBorder(x + 12, curY, anchorBtnW, anchorBtnH, 0xFF2A2E3D, 1);
            renderer.drawCenteredTextWithShadow("⚓ " + compState.getAnchor().getDisplayName(), x + 12 + anchorBtnW / 2, curY + 6, 0xFFE0E0E0);
            curY += 24;
        }

        // Section: Alignment Shortcuts
        if (desc.hasCapability(HudComponentCapability.POSITION)) {
            renderer.drawText("Wyrównaj do ekranu:", x + 12, curY, 0xFFCCCCCC, false, 0.75f, true);
            curY += 12;
            renderAlignmentButtons(renderer, x + 12, curY, width - 24, mouseX, mouseY);
            curY += 26;
        }

        // Reset to Default button
        curY = Math.max(curY + 6, y + height - 32);
        int resetBtnW = width - 24;
        int resetBtnH = 20;
        boolean resetHover = mouseX >= x + 12 && mouseX <= x + 12 + resetBtnW && mouseY >= curY && mouseY <= curY + resetBtnH;
        renderer.fill(x + 12, curY, resetBtnW, resetBtnH, resetHover ? 0xFF553333 : 0xFF332222);
        renderer.drawBorder(x + 12, curY, resetBtnW, resetBtnH, 0xFFFF5555, 1);
        renderer.drawCenteredTextWithShadow("↺ Przywróć Domyślne", x + 12 + resetBtnW / 2, curY + 6, 0xFFFF8888);
    }

    private void renderLayerControls(IHudRenderer renderer, int bx, int by, int bw,
                                     HudComponentState compState, HudLayout layout, int mouseX, int mouseY) {
        int segW = bw / 5;
        // Button 1: Layer Name Cycle
        int layerBtnW = segW * 2;
        boolean lHover = mouseX >= bx && mouseX <= bx + layerBtnW && mouseY >= by && mouseY <= by + 18;
        renderer.fill(bx, by, layerBtnW, 18, lHover ? 0xFF353A4C : 0xFF232734);
        renderer.drawBorder(bx, by, layerBtnW, 18, 0xFF2A2E3D, 1);
        String lName = compState.getLayerId() != null ? compState.getLayerId() : "main_hud";
        renderer.drawCenteredTextWithShadow("Warstwa: " + lName, bx + layerBtnW / 2, by + 5, 0xFFE0E0E0);

        // Buttons 2-5: Bring to Front (▲▲), Move Up (▲), Move Down (▼), Send to Back (▼▼)
        String[] zIcons = {"▲▲", "▲", "▼", "▼▼"};
        for (int i = 0; i < 4; i++) {
            int cx = bx + layerBtnW + 4 + i * ((bw - layerBtnW - 4) / 4);
            int cw = (bw - layerBtnW - 4) / 4 - 2;
            boolean bHover = mouseX >= cx && mouseX <= cx + cw && mouseY >= by && mouseY <= by + 18;
            renderer.fill(cx, by, cw, 18, bHover ? 0xFF353A4C : 0xFF232734);
            renderer.drawBorder(cx, by, cw, 18, 0xFF2A2E3D, 1);
            renderer.drawCenteredTextWithShadow(zIcons[i], cx + cw / 2, by + 5, 0xFFCCCCCC);
        }
    }

    private void renderInputField(IHudRenderer renderer, int bx, int by, int bw, int bh,
                                  String label, String value, boolean isFocused, int mouseX, int mouseY) {
        boolean hover = mouseX >= bx && mouseX <= bx + bw && mouseY >= by && mouseY <= by + bh;
        renderer.fill(bx, by, bw, bh, isFocused ? 0xFF1C2230 : (hover ? 0xFF282C3A : 0xFF1E212B));
        renderer.drawBorder(bx, by, bw, bh, isFocused ? 0xFF4A90E2 : (hover ? 0xFF555555 : 0xFF2A2E3D), 1);
        renderer.drawText(label, bx + 4, by + 5, 0xFF888888, false, 0.75f, false);
        int labelW = renderer.getTextWidth(label);
        String cursor = isFocused ? ((System.currentTimeMillis() / 500) % 2 == 0 ? "_" : "") : "";
        renderer.drawText(value + cursor, bx + 6 + labelW, by + 5, isFocused ? 0xFFFFFFFF : 0xFFE0E0E0, false, 0.75f, true);
    }

    private void renderStepButtons(IHudRenderer renderer, int btnX, int btnY, String axis, int mouseX, int mouseY) {
        // [-10]
        boolean h1 = mouseX >= btnX && mouseX <= btnX + 18 && mouseY >= btnY && mouseY <= btnY + 18;
        renderer.fill(btnX, btnY, 18, 18, h1 ? 0xFF353A4C : 0xFF232734);
        renderer.drawBorder(btnX, btnY, 18, 18, 0xFF2A2E3D, 1);
        renderer.drawCenteredTextWithShadow("-10", btnX + 9, btnY + 5, 0xFFAAAAAA);

        // [-1]
        boolean h2 = mouseX >= btnX + 20 && mouseX <= btnX + 38 && mouseY >= btnY && mouseY <= btnY + 18;
        renderer.fill(btnX + 20, btnY, 18, 18, h2 ? 0xFF353A4C : 0xFF232734);
        renderer.drawBorder(btnX + 20, btnY, 18, 18, 0xFF2A2E3D, 1);
        renderer.drawCenteredTextWithShadow("-1", btnX + 29, btnY + 5, 0xFFCCCCCC);

        // [+1]
        boolean h3 = mouseX >= btnX + 40 && mouseX <= btnX + 58 && mouseY >= btnY && mouseY <= btnY + 18;
        renderer.fill(btnX + 40, btnY, 18, 18, h3 ? 0xFF353A4C : 0xFF232734);
        renderer.drawBorder(btnX + 40, btnY, 18, 18, 0xFF2A2E3D, 1);
        renderer.drawCenteredTextWithShadow("+1", btnX + 49, btnY + 5, 0xFFCCCCCC);

        // [+10]
        boolean h4 = mouseX >= btnX + 60 && mouseX <= btnX + 78 && mouseY >= btnY && mouseY <= btnY + 18;
        renderer.fill(btnX + 60, btnY, 18, 18, h4 ? 0xFF353A4C : 0xFF232734);
        renderer.drawBorder(btnX + 60, btnY, 18, 18, 0xFF2A2E3D, 1);
        renderer.drawCenteredTextWithShadow("+10", btnX + 69, btnY + 5, 0xFFAAAAAA);
    }

    private void renderScaleButtons(IHudRenderer renderer, int btnX, int btnY, int mouseX, int mouseY) {
        // [-0.05]
        boolean h1 = mouseX >= btnX && mouseX <= btnX + 24 && mouseY >= btnY && mouseY <= btnY + 18;
        renderer.fill(btnX, btnY, 24, 18, h1 ? 0xFF353A4C : 0xFF232734);
        renderer.drawBorder(btnX, btnY, 24, 18, 0xFF2A2E3D, 1);
        renderer.drawCenteredTextWithShadow("-0.05", btnX + 12, btnY + 5, 0xFFCCCCCC);

        // [+0.05]
        boolean h2 = mouseX >= btnX + 28 && mouseX <= btnX + 52 && mouseY >= btnY && mouseY <= btnY + 18;
        renderer.fill(btnX + 28, btnY, 24, 18, h2 ? 0xFF353A4C : 0xFF232734);
        renderer.drawBorder(btnX + 28, btnY, 24, 18, 0xFF2A2E3D, 1);
        renderer.drawCenteredTextWithShadow("+0.05", btnX + 40, btnY + 5, 0xFFCCCCCC);

        // [1.0x Reset]
        boolean h3 = mouseX >= btnX + 56 && mouseX <= btnX + 78 && mouseY >= btnY && mouseY <= btnY + 18;
        renderer.fill(btnX + 56, btnY, 22, 18, h3 ? 0xFF353A4C : 0xFF232734);
        renderer.drawBorder(btnX + 56, btnY, 22, 18, 0xFF2A2E3D, 1);
        renderer.drawCenteredTextWithShadow("1.0", btnX + 67, btnY + 5, 0xFF55FFFF);
    }

    private void renderAlignmentButtons(IHudRenderer renderer, int bx, int by, int bw, int mouseX, int mouseY) {
        int segW = bw / 6;
        String[] icons = {"⇤", "↔", "⇥", "⤒", "↕", "⤓"};
        for (int i = 0; i < 6; i++) {
            int curX = bx + i * segW;
            boolean hover = mouseX >= curX && mouseX <= curX + segW - 2 && mouseY >= by && mouseY <= by + 18;
            renderer.fill(curX, by, segW - 2, 18, hover ? 0xFF353A4C : 0xFF232734);
            renderer.drawBorder(curX, by, segW - 2, 18, 0xFF2A2E3D, 1);
            renderer.drawCenteredTextWithShadow(icons[i], curX + (segW - 2) / 2, by + 4, 0xFFE0E0E0);
        }
    }

    public boolean handleClick(int mouseX, int mouseY, int button, HudLayout layout,
                               HudEditorState state, HudSelectionManager selectionManager) {
        if (state.isRightPanelCollapsed()) {
            if (mouseX >= x && mouseX <= x + 24 && mouseY >= y + 8 && mouseY <= y + 24) {
                state.setRightPanelCollapsed(false);
                return true;
            }
            return false;
        }

        if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height) {
            commitActiveField(layout, selectionManager, state);
            return false;
        }

        // Collapse button click
        int collapseBtnX = x + width - 24;
        int collapseBtnY = y + 8;
        if (mouseX >= collapseBtnX && mouseX <= collapseBtnX + 16 && mouseY >= collapseBtnY && mouseY <= collapseBtnY + 16) {
            commitActiveField(layout, selectionManager, state);
            state.setRightPanelCollapsed(true);
            return true;
        }

        HudMultiSelectionManager multiSel = state.getMultiSelectionManager();
        if (multiSel.getSelectedCount() > 1) {
            return handleMultiSelectionClick(mouseX, mouseY, layout, state, multiSel);
        }

        String selectedId = selectionManager.getSelectedComponentId();
        if (selectedId == null) return true;

        HudComponentState compState = layout.getComponentState(selectedId);
        HudComponentRegistry registry = HudComponentRegistry.getInstance();
        Optional<HudComponentDescriptor> optDesc = registry.getDescriptor(selectedId);
        if (compState == null || !optDesc.isPresent()) return true;

        HudComponentDescriptor desc = optDesc.get();
        int curY = y + 28 + 14 + 14;
        if (desc.getType() == HudComponentType.WORLD_SPACE) {
            curY += 28;
        }

        int halfW = (width - 28) / 2;
        int btnH = 18;

        // Lock button click
        if (mouseX >= x + 12 && mouseX <= x + 12 + halfW && mouseY >= curY && mouseY <= curY + btnH) {
            compState.setLocked(!compState.isLocked());
            return true;
        }

        // Visibility button click
        if (mouseX >= x + 16 + halfW && mouseX <= x + 16 + 2 * halfW && mouseY >= curY && mouseY <= curY + btnH) {
            boolean newVis = !compState.isVisible();
            state.getUndoRedoManager().executeCommand(new ChangeVisibilityCommand(selectedId, compState.isVisible(), newVis, layout));
            return true;
        }
        curY += 22;

        // Layer & Z-Index controls
        int bw = width - 24;
        int segW = bw / 5;
        int layerBtnW = segW * 2;
        curY += 12;

        if (mouseY >= curY && mouseY <= curY + 18) {
            // Layer cycle
            if (mouseX >= x + 12 && mouseX <= x + 12 + layerBtnW) {
                List<String> layerIds = new ArrayList<>(layout.getLayers().keySet());
                int idx = layerIds.indexOf(compState.getLayerId());
                String nextLayer = layerIds.get((idx + 1) % layerIds.size());
                state.getUndoRedoManager().executeCommand(new ChangeLayerCommand(selectedId, compState.getLayerId(), nextLayer, layout));
                return true;
            }

            // Z-Index buttons
            int zStart = x + 12 + layerBtnW + 4;
            int cw = (bw - layerBtnW - 4) / 4;
            if (mouseX >= zStart && mouseX <= zStart + cw) {
                state.getLayerManager().bringToFront(layout, selectedId);
                return true;
            }
            if (mouseX >= zStart + cw && mouseX <= zStart + 2 * cw) {
                state.getLayerManager().moveForward(layout, selectedId);
                return true;
            }
            if (mouseX >= zStart + 2 * cw && mouseX <= zStart + 3 * cw) {
                state.getLayerManager().moveBackward(layout, selectedId);
                return true;
            }
            if (mouseX >= zStart + 3 * cw && mouseX <= zStart + 4 * cw) {
                state.getLayerManager().sendToBack(layout, selectedId);
                return true;
            }
        }
        curY += 24;

        // Position Section Clicks
        if (desc.hasCapability(HudComponentCapability.POSITION)) {
            curY += 12;

            // X Input Box
            if (mouseX >= x + 12 && mouseX <= x + 77 && mouseY >= curY && mouseY <= curY + 18) {
                commitActiveField(layout, selectionManager, state);
                activeField = ActiveField.POS_X;
                inputBuffer = String.valueOf(compState.getPosX());
                return true;
            }

            // X Step Buttons
            int bx = x + 82;
            if (mouseY >= curY && mouseY <= curY + 18) {
                int oldX = compState.getPosX();
                if (mouseX >= bx && mouseX <= bx + 18) {
                    compState.setPosX(Math.max(0, compState.getPosX() - 10));
                    recordMoveUndo(selectedId, oldX, compState.getPosY(), compState.getPosX(), compState.getPosY(), state);
                    return true;
                }
                if (mouseX >= bx + 20 && mouseX <= bx + 38) {
                    compState.setPosX(Math.max(0, compState.getPosX() - 1));
                    recordMoveUndo(selectedId, oldX, compState.getPosY(), compState.getPosX(), compState.getPosY(), state);
                    return true;
                }
                if (mouseX >= bx + 40 && mouseX <= bx + 58) {
                    compState.setPosX(compState.getPosX() + 1);
                    recordMoveUndo(selectedId, oldX, compState.getPosY(), compState.getPosX(), compState.getPosY(), state);
                    return true;
                }
                if (mouseX >= bx + 60 && mouseX <= bx + 78) {
                    compState.setPosX(compState.getPosX() + 10);
                    recordMoveUndo(selectedId, oldX, compState.getPosY(), compState.getPosX(), compState.getPosY(), state);
                    return true;
                }
            }
            curY += 22;

            // Y Input Box
            if (mouseX >= x + 12 && mouseX <= x + 77 && mouseY >= curY && mouseY <= curY + 18) {
                commitActiveField(layout, selectionManager, state);
                activeField = ActiveField.POS_Y;
                inputBuffer = String.valueOf(compState.getPosY());
                return true;
            }

            // Y Step Buttons
            if (mouseY >= curY && mouseY <= curY + 18) {
                int oldY = compState.getPosY();
                if (mouseX >= bx && mouseX <= bx + 18) {
                    compState.setPosY(Math.max(0, compState.getPosY() - 10));
                    recordMoveUndo(selectedId, compState.getPosX(), oldY, compState.getPosX(), compState.getPosY(), state);
                    return true;
                }
                if (mouseX >= bx + 20 && mouseX <= bx + 38) {
                    compState.setPosY(Math.max(0, compState.getPosY() - 1));
                    recordMoveUndo(selectedId, compState.getPosX(), oldY, compState.getPosX(), compState.getPosY(), state);
                    return true;
                }
                if (mouseX >= bx + 40 && mouseX <= bx + 58) {
                    compState.setPosY(compState.getPosY() + 1);
                    recordMoveUndo(selectedId, compState.getPosX(), oldY, compState.getPosX(), compState.getPosY(), state);
                    return true;
                }
                if (mouseX >= bx + 60 && mouseX <= bx + 78) {
                    compState.setPosY(compState.getPosY() + 10);
                    recordMoveUndo(selectedId, compState.getPosX(), oldY, compState.getPosX(), compState.getPosY(), state);
                    return true;
                }
            }
            curY += 24;
        }

        // Size Section Clicks
        if (desc.hasCapability(HudComponentCapability.SIZE)) {
            curY += 12;

            // Width Box
            if (mouseX >= x + 12 && mouseX <= x + 77 && mouseY >= curY && mouseY <= curY + 18) {
                commitActiveField(layout, selectionManager, state);
                activeField = ActiveField.WIDTH;
                inputBuffer = String.valueOf(compState.getWidth());
                return true;
            }

            int bx = x + 82;
            if (mouseY >= curY && mouseY <= curY + 18) {
                if (mouseX >= bx && mouseX <= bx + 18) {
                    compState.setWidth(Math.max(desc.getMinWidth(), compState.getWidth() - 10));
                    return true;
                }
                if (mouseX >= bx + 20 && mouseX <= bx + 38) {
                    compState.setWidth(Math.max(desc.getMinWidth(), compState.getWidth() - 1));
                    return true;
                }
                if (mouseX >= bx + 40 && mouseX <= bx + 58) {
                    compState.setWidth(Math.min(desc.getMaxWidth(), compState.getWidth() + 1));
                    return true;
                }
                if (mouseX >= bx + 60 && mouseX <= bx + 78) {
                    compState.setWidth(Math.min(desc.getMaxWidth(), compState.getWidth() + 10));
                    return true;
                }
            }
            curY += 22;

            // Height Box
            if (mouseX >= x + 12 && mouseX <= x + 77 && mouseY >= curY && mouseY <= curY + 18) {
                commitActiveField(layout, selectionManager, state);
                activeField = ActiveField.HEIGHT;
                inputBuffer = String.valueOf(compState.getHeight());
                return true;
            }

            if (mouseY >= curY && mouseY <= curY + 18) {
                if (mouseX >= bx && mouseX <= bx + 18) {
                    compState.setHeight(Math.max(desc.getMinHeight(), compState.getHeight() - 10));
                    return true;
                }
                if (mouseX >= bx + 20 && mouseX <= bx + 38) {
                    compState.setHeight(Math.max(desc.getMinHeight(), compState.getHeight() - 1));
                    return true;
                }
                if (mouseX >= bx + 40 && mouseX <= bx + 58) {
                    compState.setHeight(Math.min(desc.getMaxHeight(), compState.getHeight() + 1));
                    return true;
                }
                if (mouseX >= bx + 60 && mouseX <= bx + 78) {
                    compState.setHeight(Math.min(desc.getMaxHeight(), compState.getHeight() + 10));
                    return true;
                }
            }
            curY += 24;

            // Lock Aspect Ratio
            if (mouseX >= x + 12 && mouseX <= x + 12 + halfW && mouseY >= curY && mouseY <= curY + btnH) {
                compState.setLockAspectRatio(!compState.isLockAspectRatio());
                return true;
            }

            // Auto Size
            if (mouseX >= x + 16 + halfW && mouseX <= x + 16 + 2 * halfW && mouseY >= curY && mouseY <= curY + btnH) {
                compState.setAutoSize(!compState.isAutoSize());
                return true;
            }
            curY += 26;
        }

        // Scale Section Clicks
        if (desc.hasCapability(HudComponentCapability.SCALE)) {
            curY += 12;

            // Scale input box
            if (mouseX >= x + 12 && mouseX <= x + 77 && mouseY >= curY && mouseY <= curY + 18) {
                commitActiveField(layout, selectionManager, state);
                activeField = ActiveField.SCALE;
                inputBuffer = String.format(Locale.ROOT, "%.2f", compState.getScale());
                return true;
            }

            int bx = x + 82;
            if (mouseY >= curY && mouseY <= curY + 18) {
                double oldS = compState.getScale();
                if (mouseX >= bx && mouseX <= bx + 24) {
                    HudScaleController.stepScale(compState, desc, -0.05);
                    recordScaleUndo(selectedId, oldS, compState.getScale(), state);
                    return true;
                }
                if (mouseX >= bx + 28 && mouseX <= bx + 52) {
                    HudScaleController.stepScale(compState, desc, 0.05);
                    recordScaleUndo(selectedId, oldS, compState.getScale(), state);
                    return true;
                }
                if (mouseX >= bx + 56 && mouseX <= bx + 78) {
                    HudScaleController.resetScale(compState, desc);
                    recordScaleUndo(selectedId, oldS, compState.getScale(), state);
                    return true;
                }
            }
            curY += 24;
        }

        // Anchor Section Clicks
        if (desc.hasCapability(HudComponentCapability.ANCHOR)) {
            curY += 12;
            int anchorBtnW = width - 24;
            int anchorBtnH = 20;
            if (mouseX >= x + 12 && mouseX <= x + 12 + anchorBtnW && mouseY >= curY && mouseY <= curY + anchorBtnH) {
                HudAnchor[] anchors = HudAnchor.values();
                int idx = compState.getAnchor().ordinal();
                HudAnchor nextAnchor = anchors[(idx + 1) % anchors.length];
                state.getUndoRedoManager().executeCommand(new ChangeAnchorCommand(selectedId, compState.getAnchor(), nextAnchor, layout));
                return true;
            }
            curY += 24;
        }

        // Alignment Buttons Clicks
        if (desc.hasCapability(HudComponentCapability.POSITION)) {
            curY += 12;
            int bwAlign = width - 24;
            int segWAlign = bwAlign / 6;
            int virtW = state.getViewport().getVirtWidth();
            int virtH = state.getViewport().getVirtHeight();

            if (mouseY >= curY && mouseY <= curY + 18) {
                int col = (mouseX - (x + 12)) / segWAlign;
                if (col == 0) {
                    HudAlignmentManager.alignLeft(compState, desc, virtW, 0);
                    return true;
                } else if (col == 1) {
                    HudAlignmentManager.centerHorizontally(compState, desc, virtW);
                    return true;
                } else if (col == 2) {
                    HudAlignmentManager.alignRight(compState, desc, virtW, 0);
                    return true;
                } else if (col == 3) {
                    HudAlignmentManager.alignTop(compState, desc, virtH, 0);
                    return true;
                } else if (col == 4) {
                    HudAlignmentManager.centerVertically(compState, desc, virtH);
                    return true;
                } else if (col == 5) {
                    HudAlignmentManager.alignBottom(compState, desc, virtH, 0);
                    return true;
                }
            }
            curY += 26;
        }

        // Reset to Default Click
        curY = Math.max(curY + 6, y + height - 32);
        int resetBtnW = width - 24;
        int resetBtnH = 20;
        if (mouseX >= x + 12 && mouseX <= x + 12 + resetBtnW && mouseY >= curY && mouseY <= curY + resetBtnH) {
            HudLayoutManager.getInstance().resetComponentToDefault(selectedId);
            return true;
        }

        commitActiveField(layout, selectionManager, state);
        return true;
    }

    private boolean handleMultiSelectionClick(int mouseX, int mouseY, HudLayout layout,
                                              HudEditorState state, HudMultiSelectionManager multiSel) {
        int curY = y + 32 + 16 + 16 + 20;
        int btnW = width - 24;
        int btnH = 20;
        int halfW = (width - 28) / 2;

        // Row 1: Bulk Lock & Visibility
        if (mouseY >= curY && mouseY <= curY + btnH) {
            // Bulk Lock
            if (mouseX >= x + 12 && mouseX <= x + 12 + halfW) {
                boolean targetLock = false;
                for (String id : multiSel.getSelectedIds()) {
                    HudComponentState cs = layout.getComponentState(id);
                    if (cs != null && !cs.isLocked()) { targetLock = true; break; }
                }
                for (String id : multiSel.getSelectedIds()) {
                    HudComponentState cs = layout.getComponentState(id);
                    if (cs != null) cs.setLocked(targetLock);
                }
                return true;
            }
            // Bulk Visibility
            if (mouseX >= x + 16 + halfW && mouseX <= x + 16 + 2 * halfW) {
                boolean targetVis = false;
                for (String id : multiSel.getSelectedIds()) {
                    HudComponentState cs = layout.getComponentState(id);
                    if (cs != null && !cs.isVisible()) { targetVis = true; break; }
                }
                for (String id : multiSel.getSelectedIds()) {
                    HudComponentState cs = layout.getComponentState(id);
                    if (cs != null) cs.setVisible(targetVis);
                }
                return true;
            }
        }
        curY += 26;

        // Row 2: Create Group button
        if (mouseX >= x + 12 && mouseX <= x + 12 + btnW && mouseY >= curY && mouseY <= curY + btnH) {
            String grpId = "group_" + (layout.getGroups().size() + 1);
            String grpName = "Grupa " + (layout.getGroups().size() + 1);
            HudGroup grp = state.getGroupManager().createGroup(layout, grpId, grpName, multiSel.getSelectedIds());
            if (grp != null) {
                state.getUndoRedoManager().recordExecutedCommand(new CreateGroupCommand(grp, layout));
            }
            return true;
        }
        curY += 26;

        // Row 3: Bulk Scale
        curY += 12;
        int bx = x + 12;
        if (mouseY >= curY && mouseY <= curY + 18) {
            HudComponentRegistry reg = HudComponentRegistry.getInstance();
            if (mouseX >= bx && mouseX <= bx + 24) {
                for (String id : multiSel.getSelectedIds()) {
                    HudComponentState cs = layout.getComponentState(id);
                    reg.getDescriptor(id).ifPresent(d -> HudScaleController.stepScale(cs, d, -0.05));
                }
                return true;
            }
            if (mouseX >= bx + 28 && mouseX <= bx + 52) {
                for (String id : multiSel.getSelectedIds()) {
                    HudComponentState cs = layout.getComponentState(id);
                    reg.getDescriptor(id).ifPresent(d -> HudScaleController.stepScale(cs, d, 0.05));
                }
                return true;
            }
            if (mouseX >= bx + 56 && mouseX <= bx + 78) {
                for (String id : multiSel.getSelectedIds()) {
                    HudComponentState cs = layout.getComponentState(id);
                    reg.getDescriptor(id).ifPresent(d -> HudScaleController.resetScale(cs, d));
                }
                return true;
            }
        }
        curY += 26;

        // Row 4: Bulk Alignment
        curY += 12;
        int bwAlign = width - 24;
        int segW = bwAlign / 6;
        int virtW = state.getViewport().getVirtWidth();
        int virtH = state.getViewport().getVirtHeight();
        HudComponentRegistry reg = HudComponentRegistry.getInstance();

        if (mouseY >= curY && mouseY <= curY + 18) {
            int col = (mouseX - (x + 12)) / segW;
            for (String id : multiSel.getSelectedIds()) {
                HudComponentState cs = layout.getComponentState(id);
                reg.getDescriptor(id).ifPresent(d -> {
                    if (d.hasCapability(HudComponentCapability.POSITION)) {
                        if (col == 0) HudAlignmentManager.alignLeft(cs, d, virtW, 0);
                        else if (col == 1) HudAlignmentManager.centerHorizontally(cs, d, virtW);
                        else if (col == 2) HudAlignmentManager.alignRight(cs, d, virtW, 0);
                        else if (col == 3) HudAlignmentManager.alignTop(cs, d, virtH, 0);
                        else if (col == 4) HudAlignmentManager.centerVertically(cs, d, virtH);
                        else if (col == 5) HudAlignmentManager.alignBottom(cs, d, virtH, 0);
                    }
                });
            }
            return true;
        }
        curY += 28;

        // Row 5: Clipboard (Duplicate / Copy)
        if (mouseY >= curY && mouseY <= curY + btnH) {
            if (mouseX >= x + 12 && mouseX <= x + 12 + halfW) {
                state.getClipboard().duplicateComponents(layout, multiSel.getSelectedIds(), 20, 20);
                return true;
            }
            if (mouseX >= x + 16 + halfW && mouseX <= x + 16 + 2 * halfW) {
                state.getClipboard().copyComponents(layout, multiSel.getSelectedIds());
                return true;
            }
        }
        curY += 28;

        // Row 6: Clear Selection
        curY = Math.max(curY + 6, y + height - 32);
        if (mouseX >= x + 12 && mouseX <= x + 12 + btnW && mouseY >= curY && mouseY <= curY + btnH) {
            multiSel.clearSelection();
            return true;
        }

        return true;
    }

    private void recordMoveUndo(String compId, int oldX, int oldY, int newX, int newY, HudEditorState state) {
        HudLayout layout = HudLayoutManager.getInstance().getStagedLayout();
        state.getUndoRedoManager().recordExecutedCommand(new MoveComponentsCommand(
                Collections.singletonMap(compId, new MoveComponentsCommand.Point(oldX, oldY)),
                Collections.singletonMap(compId, new MoveComponentsCommand.Point(newX, newY)),
                layout
        ));
    }

    private void recordScaleUndo(String compId, double oldScale, double newScale, HudEditorState state) {
        HudLayout layout = HudLayoutManager.getInstance().getStagedLayout();
        state.getUndoRedoManager().recordExecutedCommand(new ScaleComponentsCommand(
                Collections.singletonMap(compId, oldScale),
                Collections.singletonMap(compId, newScale),
                layout
        ));
    }

    public boolean handleCharTyped(char chr, HudLayout layout, HudSelectionManager selectionManager) {
        if (activeField == ActiveField.NONE) return false;

        if (Character.isDigit(chr) || chr == '.' || chr == '-') {
            inputBuffer += chr;
            return true;
        }
        return false;
    }

    public boolean handleKeyPressed(int keyCode, HudLayout layout, HudSelectionManager selectionManager) {
        if (activeField == ActiveField.NONE) return false;

        // Enter key (GLFW_KEY_ENTER = 257, GLFW_KEY_KP_ENTER = 335)
        if (keyCode == 257 || keyCode == 335) {
            commitActiveField(layout, selectionManager, null);
            return true;
        }

        // Escape key (GLFW_KEY_ESCAPE = 256)
        if (keyCode == 256) {
            activeField = ActiveField.NONE;
            inputBuffer = "";
            return true;
        }

        // Backspace (GLFW_KEY_BACKSPACE = 259)
        if (keyCode == 259) {
            if (!inputBuffer.isEmpty()) {
                inputBuffer = inputBuffer.substring(0, inputBuffer.length() - 1);
            }
            return true;
        }

        return false;
    }

    public void commitActiveField(HudLayout layout, HudSelectionManager selectionManager, HudEditorState state) {
        if (activeField == ActiveField.NONE) return;

        String selectedId = selectionManager.getSelectedComponentId();
        if (selectedId != null && layout != null) {
            HudComponentState compState = layout.getComponentState(selectedId);
            Optional<HudComponentDescriptor> optDesc = HudComponentRegistry.getInstance().getDescriptor(selectedId);
            if (compState != null && optDesc.isPresent()) {
                HudComponentDescriptor desc = optDesc.get();
                try {
                    switch (activeField) {
                        case POS_X:
                            compState.setPosX(Integer.parseInt(inputBuffer.trim()));
                            break;
                        case POS_Y:
                            compState.setPosY(Integer.parseInt(inputBuffer.trim()));
                            break;
                        case WIDTH:
                            int w = Integer.parseInt(inputBuffer.trim());
                            compState.setWidth(Math.max(desc.getMinWidth(), Math.min(desc.getMaxWidth(), w)));
                            break;
                        case HEIGHT:
                            int h = Integer.parseInt(inputBuffer.trim());
                            compState.setHeight(Math.max(desc.getMinHeight(), Math.min(desc.getMaxHeight(), h)));
                            break;
                        case SCALE:
                            double s = Double.parseDouble(inputBuffer.trim());
                            HudScaleController.setScale(compState, desc, s);
                            break;
                        default:
                            break;
                    }
                } catch (NumberFormatException ignored) {
                    // Ignore malformed input, keeps existing value
                }
            }
        }

        this.activeField = ActiveField.NONE;
        this.inputBuffer = "";
    }

    public boolean isEditingField() {
        return activeField != ActiveField.NONE;
    }

    public int getWidth(HudEditorState state) {
        return state.isRightPanelCollapsed() ? 24 : width;
    }
}
