package pl.zombek.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import pl.zombek.client.core.ClientConstants;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.ModuleManager;
import pl.zombek.client.core.settings.*;

import java.util.List;

/**
 * Primary in-game configuration menu for ZombekClient, opened via RIGHT SHIFT.
 * Features a festive dark theme, responsive modular layout, instant category filtering,
 * live search, interactive module cards, detailed settings editor, and keybind capture.
 */
public class ZombekMenuScreen extends Screen {
    public enum NavigationTab {
        PVP(ModuleCategory.PVP),
        HUD(ModuleCategory.HUD),
        VISUAL(ModuleCategory.VISUAL),
        SURVIVAL(ModuleCategory.SURVIVAL),
        OTHER(ModuleCategory.OTHER),
        CLIENT_SETTINGS(null);

        private final ModuleCategory category;

        NavigationTab(ModuleCategory category) {
            this.category = category;
        }

        public ModuleCategory getCategory() {
            return category;
        }
    }

    private final ModuleManager moduleManager;

    // Layout dimensions
    private int windowX;
    private int windowY;
    private int windowWidth;
    private int windowHeight;
    private int sidebarWidth;
    private int contentX;
    private int contentY;
    private int contentWidth;
    private int contentHeight;

    // State
    private NavigationTab currentTab = NavigationTab.OTHER;
    private AbstractModule selectedSettingsModule = null;
    private String searchQuery = "";
    private boolean isSearching = false;

    // Scrolling
    private int scrollOffset = 0;
    private int maxScroll = 0;

    // Keybind capturing
    private AbstractModule capturingModule = null;
    private KeybindSetting capturingSetting = null;

    // Slider dragging
    private NumberSetting draggingSlider = null;
    private int sliderDragMinX = 0;
    private int sliderDragWidth = 0;

    public ZombekMenuScreen() {
        super(Text.literal(ClientConstants.CLIENT_NAME + " Menu"));
        this.moduleManager = ModuleManager.getInstance();
    }

    @Override
    protected void init() {
        super.init();

        this.windowWidth = Math.min(760, Math.max(480, this.width - 40));
        this.windowHeight = Math.min(480, Math.max(340, this.height - 40));
        this.windowX = (this.width - windowWidth) / 2;
        this.windowY = (this.height - windowHeight) / 2;

        this.sidebarWidth = 175;
        this.contentX = windowX + sidebarWidth;
        this.contentY = windowY + 44; // Below topbar
        this.contentWidth = windowWidth - sidebarWidth;
        this.contentHeight = windowHeight - 44 - 28; // Above bottombar

        this.scrollOffset = 0;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 1. Dim background
        context.fill(0, 0, this.width, this.height, ZombekTheme.COLOR_OVERLAY);

        // 2. Main Window Background & Border
        context.fill(windowX, windowY, windowX + windowWidth, windowY + windowHeight, ZombekTheme.COLOR_WINDOW_BG);
        context.drawBorder(windowX, windowY, windowWidth, windowHeight, ZombekTheme.COLOR_CARD_BORDER);

        // 3. Render Sidebar
        renderSidebar(context, mouseX, mouseY);

        // 4. Render Topbar
        renderTopbar(context, mouseX, mouseY);

        // 5. Render Main Content Area
        context.enableScissor(contentX, contentY, contentX + contentWidth, contentY + contentHeight);
        if (selectedSettingsModule != null) {
            renderModuleSettingsView(context, mouseX, mouseY);
        } else if (currentTab == NavigationTab.CLIENT_SETTINGS) {
            renderClientSettingsView(context, mouseX, mouseY);
        } else {
            renderModuleListView(context, mouseX, mouseY);
        }
        context.disableScissor();

        // 6. Render Bottombar
        renderBottombar(context);

        // 7. Render Keybind Capture Modal if active
        if (capturingModule != null || capturingSetting != null) {
            renderKeybindCaptureModal(context);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    // ==========================================
    // SIDEBAR RENDERING
    // ==========================================
    private void renderSidebar(DrawContext context, int mouseX, int mouseY) {
        int sbX = windowX;
        int sbY = windowY;
        int sbW = sidebarWidth;
        int sbH = windowHeight;

        // Background
        context.fill(sbX, sbY, sbX + sbW, sbY + sbH, ZombekTheme.COLOR_SIDEBAR_BG);
        // Vertical divider line
        context.fill(sbX + sbW - 1, sbY, sbX + sbW, sbY + sbH, ZombekTheme.COLOR_CARD_BORDER);

        // Client Title / Branding
        TextRenderer tr = this.textRenderer;
        int brandY = sbY + 12;
        context.drawText(tr, "⚡ " + ClientConstants.CLIENT_NAME.toUpperCase(), sbX + 14, brandY, ZombekTheme.COLOR_ACCENT_GOLD, true);
        context.drawText(tr, "v" + ClientConstants.CLIENT_VERSION, sbX + 16, brandY + 12, ZombekTheme.TEXT_MUTED, false);

        // Category Tab Buttons
        int tabY = sbY + 46;
        int tabHeight = 32;

        NavigationTab[] tabs = NavigationTab.values();
        for (NavigationTab tab : tabs) {
            boolean isSelected = (currentTab == tab && selectedSettingsModule == null);
            boolean isHovered = mouseX >= sbX + 8 && mouseX <= sbX + sbW - 8 && mouseY >= tabY && mouseY < tabY + tabHeight;

            int bgCol = isSelected ? ZombekTheme.COLOR_CARD_BG : (isHovered ? ZombekTheme.COLOR_CARD_HOVER : 0x00000000);
            if (bgCol != 0) {
                context.fill(sbX + 8, tabY, sbX + sbW - 8, tabY + tabHeight, bgCol);
                context.drawBorder(sbX + 8, tabY, sbW - 16, tabHeight, isSelected ? ZombekTheme.COLOR_ACCENT_GREEN : ZombekTheme.COLOR_CARD_BORDER);
            }

            // Indicator pill on selected tab
            if (isSelected) {
                context.fill(sbX + 8, tabY + 4, sbX + 11, tabY + tabHeight - 4, ZombekTheme.COLOR_ACCENT_GREEN);
            }

            String tabLabel;
            int count = 0;
            if (tab == NavigationTab.CLIENT_SETTINGS) {
                tabLabel = "⚙ Opcje Klienta";
            } else {
                tabLabel = tab.getCategory().getIconSymbol() + " " + tab.getCategory().getDisplayName();
                count = moduleManager.getModulesByCategory(tab.getCategory()).size();
            }

            int textCol = isSelected ? ZombekTheme.TEXT_PRIMARY : (isHovered ? ZombekTheme.TEXT_PRIMARY : ZombekTheme.TEXT_SECONDARY);
            context.drawText(tr, tabLabel, sbX + 16, tabY + 11, textCol, false);

            if (tab != NavigationTab.CLIENT_SETTINGS && count > 0) {
                String countStr = String.valueOf(count);
                int countW = tr.getWidth(countStr);
                context.fill(sbX + sbW - 16 - countW - 6, tabY + 8, sbX + sbW - 14, tabY + 23, ZombekTheme.COLOR_INPUT_BG);
                context.drawText(tr, countStr, sbX + sbW - 16 - countW - 3, tabY + 11, ZombekTheme.COLOR_ACCENT_GOLD, false);
            }

            tabY += tabHeight + 4;
        }
    }

    // ==========================================
    // TOPBAR RENDERING
    // ==========================================
    private void renderTopbar(DrawContext context, int mouseX, int mouseY) {
        int tbX = contentX;
        int tbY = windowY;
        int tbW = contentWidth;
        int tbH = 44;

        context.fill(tbX, tbY, tbX + tbW, tbY + tbH, ZombekTheme.COLOR_TOPBAR_BG);
        context.fill(tbX, tbY + tbH - 1, tbX + tbW, tbY + tbH, ZombekTheme.COLOR_CARD_BORDER);

        TextRenderer tr = this.textRenderer;
        String headerTitle;
        if (selectedSettingsModule != null) {
            headerTitle = "← " + selectedSettingsModule.getName() + " - Ustawienia";
        } else if (currentTab == NavigationTab.CLIENT_SETTINGS) {
            headerTitle = "Opcje Klienta ZombekClient";
        } else {
            headerTitle = currentTab.getCategory().getDisplayName();
        }

        context.drawText(tr, headerTitle, tbX + 16, tbY + 17, ZombekTheme.TEXT_PRIMARY, true);

        // "EDIT HUD" Button
        int hudBtnW = 85;
        int hudBtnH = 22;
        int hudBtnX = (selectedSettingsModule == null && currentTab != NavigationTab.CLIENT_SETTINGS)
                ? (tbX + tbW - 160 - 16 - hudBtnW - 8)
                : (tbX + tbW - hudBtnW - 16);
        int hudBtnY = tbY + 11;
        boolean hudHover = mouseX >= hudBtnX && mouseX <= hudBtnX + hudBtnW && mouseY >= hudBtnY && mouseY <= hudBtnY + hudBtnH;
        context.fill(hudBtnX, hudBtnY, hudBtnX + hudBtnW, hudBtnY + hudBtnH, hudHover ? ZombekTheme.COLOR_CARD_HOVER : ZombekTheme.COLOR_INPUT_BG);
        context.drawBorder(hudBtnX, hudBtnY, hudBtnW, hudBtnH, hudHover ? ZombekTheme.COLOR_ACCENT_GOLD : ZombekTheme.COLOR_CARD_BORDER);
        context.drawText(tr, "🎨 EDIT HUD", hudBtnX + 8, hudBtnY + 7, hudHover ? ZombekTheme.COLOR_ACCENT_GOLD : ZombekTheme.TEXT_PRIMARY, false);

        // Search Input Box (only when in module list view)
        if (selectedSettingsModule == null && currentTab != NavigationTab.CLIENT_SETTINGS) {
            int searchW = 160;
            int searchH = 22;
            int searchX = tbX + tbW - searchW - 16;
            int searchY = tbY + 11;

            boolean isHovered = mouseX >= searchX && mouseX <= searchX + searchW && mouseY >= searchY && mouseY <= searchY + searchH;
            context.fill(searchX, searchY, searchX + searchW, searchY + searchH, ZombekTheme.COLOR_INPUT_BG);
            int borderCol = isSearching ? ZombekTheme.COLOR_ACCENT_GREEN : (isHovered ? ZombekTheme.COLOR_ACCENT_GOLD : ZombekTheme.COLOR_CARD_BORDER);
            context.drawBorder(searchX, searchY, searchW, searchH, borderCol);

            String displayText = searchQuery.isEmpty() ? (isSearching ? "_" : "🔍 Szukaj...") : searchQuery + (isSearching ? "_" : "");
            int textCol = searchQuery.isEmpty() && !isSearching ? ZombekTheme.TEXT_MUTED : ZombekTheme.TEXT_PRIMARY;
            context.drawText(tr, displayText, searchX + 8, searchY + 7, textCol, false);
        }
    }

    // ==========================================
    // MODULE LIST VIEW RENDERING
    // ==========================================
    private void renderModuleListView(DrawContext context, int mouseX, int mouseY) {
        TextRenderer tr = this.textRenderer;
        List<AbstractModule> list = (searchQuery.isEmpty())
                ? moduleManager.getModulesByCategory(currentTab.getCategory())
                : moduleManager.searchModules(searchQuery);

        if (list.isEmpty()) {
            // Render Festive Empty State
            int centerX = contentX + contentWidth / 2;
            int centerY = contentY + contentHeight / 2 - 20;

            context.drawCenteredTextWithShadow(tr, "❄ Brak modułów w tej kategorii ❄", centerX, centerY - 14, ZombekTheme.COLOR_ACCENT_GOLD);
            context.drawCenteredTextWithShadow(tr, "Kolejne moduły pojawią się w następnych etapach rozwoju klienta.", centerX, centerY + 4, ZombekTheme.TEXT_SECONDARY);
            context.drawCenteredTextWithShadow(tr, "Wybierz kategorię 'Other' aby przetestować framework.", centerX, centerY + 20, ZombekTheme.TEXT_MUTED);
            return;
        }

        int startY = contentY + 12 - scrollOffset;
        int cardHeight = 56;
        int cardGap = 8;
        int cardWidth = contentWidth - 32;

        int totalH = list.size() * (cardHeight + cardGap) + 16;
        this.maxScroll = Math.max(0, totalH - contentHeight);

        for (int i = 0; i < list.size(); i++) {
            AbstractModule module = list.get(i);
            int cardY = startY + i * (cardHeight + cardGap);

            // Skip rendering if outside view bounds
            if (cardY + cardHeight < contentY || cardY > contentY + contentHeight) {
                continue;
            }

            int cardX = contentX + 16;
            boolean isHovered = mouseX >= cardX && mouseX <= cardX + cardWidth && mouseY >= cardY && mouseY <= cardY + cardHeight;

            // Card background
            int bgCol = isHovered ? ZombekTheme.COLOR_CARD_HOVER : ZombekTheme.COLOR_CARD_BG;
            context.fill(cardX, cardY, cardX + cardWidth, cardY + cardHeight, bgCol);
            int borderCol = module.isEnabled() ? ZombekTheme.COLOR_ACCENT_GREEN : ZombekTheme.COLOR_CARD_BORDER;
            if (module.hasError()) borderCol = ZombekTheme.COLOR_ACCENT_RED;
            context.drawBorder(cardX, cardY, cardWidth, cardHeight, borderCol);

            // Left status bar
            int barCol = module.hasError() ? ZombekTheme.COLOR_ACCENT_RED : (module.isEnabled() ? ZombekTheme.COLOR_ACCENT_GREEN : ZombekTheme.COLOR_CARD_BORDER);
            context.fill(cardX, cardY, cardX + 4, cardY + cardHeight, barCol);

            // Module Name & Category
            int textX = cardX + 12;
            context.drawText(tr, module.getName(), textX, cardY + 10, ZombekTheme.TEXT_PRIMARY, true);
            if (module.hasError()) {
                context.drawText(tr, "[BŁĄD - Moduł wstrzymany]", textX + tr.getWidth(module.getName()) + 8, cardY + 10, ZombekTheme.TEXT_ERROR, false);
            }

            // Description
            String desc = module.getDescription();
            if (tr.getWidth(desc) > cardWidth - 210) {
                desc = tr.trimToWidth(desc, cardWidth - 220) + "...";
            }
            context.drawText(tr, desc, textX, cardY + 28, ZombekTheme.TEXT_SECONDARY, false);

            // Right-side interactive controls:
            // 1. Keybind pill button
            int keybindW = 60;
            int keybindH = 22;
            int keybindX = cardX + cardWidth - 195;
            int keybindY = cardY + 17;
            boolean keyHover = mouseX >= keybindX && mouseX <= keybindX + keybindW && mouseY >= keybindY && mouseY <= keybindY + keybindH;
            context.fill(keybindX, keybindY, keybindX + keybindW, keybindY + keybindH, ZombekTheme.COLOR_INPUT_BG);
            context.drawBorder(keybindX, keybindY, keybindW, keybindH, keyHover ? ZombekTheme.COLOR_ACCENT_GOLD : ZombekTheme.COLOR_CARD_BORDER);
            String keyStr = "[" + KeybindSetting.getKeyName(module.getKeybind()) + "]";
            context.drawCenteredTextWithShadow(tr, keyStr, keybindX + keybindW / 2, keybindY + 7, ZombekTheme.TEXT_GOLD);

            // 2. Settings gear button (if module has settings)
            int gearW = 56;
            int gearH = 22;
            int gearX = cardX + cardWidth - 125;
            int gearY = cardY + 17;
            if (module.hasSettings()) {
                boolean gearHover = mouseX >= gearX && mouseX <= gearX + gearW && mouseY >= gearY && mouseY <= gearY + gearH;
                context.fill(gearX, gearY, gearX + gearW, gearY + gearH, gearHover ? ZombekTheme.COLOR_CARD_HOVER : ZombekTheme.COLOR_INPUT_BG);
                context.drawBorder(gearX, gearY, gearW, gearH, gearHover ? ZombekTheme.COLOR_ACCENT_BLUE : ZombekTheme.COLOR_CARD_BORDER);
                context.drawCenteredTextWithShadow(tr, "⚙ Opcje", gearX + gearW / 2, gearY + 7, ZombekTheme.TEXT_PRIMARY);
            }

            // 3. Power ON / OFF toggle switch
            int toggleW = 52;
            int toggleH = 22;
            int toggleX = cardX + cardWidth - 62;
            int toggleY = cardY + 17;
            boolean toggleHover = mouseX >= toggleX && mouseX <= toggleX + toggleW && mouseY >= toggleY && mouseY <= toggleY + toggleH;
            int toggleBg = module.isEnabled()
                    ? (toggleHover ? ZombekTheme.COLOR_ACCENT_GREEN_HOVER : ZombekTheme.COLOR_ACCENT_GREEN)
                    : (toggleHover ? 0xFF475569 : ZombekTheme.COLOR_INPUT_BG);
            context.fill(toggleX, toggleY, toggleX + toggleW, toggleY + toggleH, toggleBg);
            context.drawBorder(toggleX, toggleY, toggleW, toggleH, module.isEnabled() ? ZombekTheme.COLOR_ACCENT_GREEN : ZombekTheme.COLOR_CARD_BORDER);
            String toggleText = module.isEnabled() ? "ON" : "OFF";
            int toggleCol = module.isEnabled() ? 0xFFFFFFFF : ZombekTheme.TEXT_MUTED;
            context.drawCenteredTextWithShadow(tr, toggleText, toggleX + toggleW / 2, toggleY + 7, toggleCol);
        }
    }

    // ==========================================
    // MODULE SETTINGS VIEW RENDERING
    // ==========================================
    private void renderModuleSettingsView(DrawContext context, int mouseX, int mouseY) {
        if (selectedSettingsModule == null) return;
        TextRenderer tr = this.textRenderer;

        // Back button header
        int backBtnW = 100;
        int backBtnH = 24;
        int backBtnX = contentX + 16;
        int backBtnY = contentY + 12;

        boolean backHover = mouseX >= backBtnX && mouseX <= backBtnX + backBtnW && mouseY >= backBtnY && mouseY <= backBtnY + backBtnH;
        context.fill(backBtnX, backBtnY, backBtnX + backBtnW, backBtnY + backBtnH, backHover ? ZombekTheme.COLOR_CARD_HOVER : ZombekTheme.COLOR_CARD_BG);
        context.drawBorder(backBtnX, backBtnY, backBtnW, backBtnH, backHover ? ZombekTheme.COLOR_ACCENT_GOLD : ZombekTheme.COLOR_CARD_BORDER);
        context.drawCenteredTextWithShadow(tr, "← Powrót", backBtnX + backBtnW / 2, backBtnY + 8, ZombekTheme.TEXT_PRIMARY);

        // Module details summary
        context.drawText(tr, "Kategoria: " + selectedSettingsModule.getCategory().getDisplayName(), backBtnX + backBtnW + 16, backBtnY + 4, ZombekTheme.COLOR_ACCENT_GOLD, false);
        context.drawText(tr, "Autor: " + selectedSettingsModule.getAuthor() + " | Wersja: " + selectedSettingsModule.getVersion(), backBtnX + backBtnW + 16, backBtnY + 15, ZombekTheme.TEXT_MUTED, false);

        // Divider
        context.fill(contentX + 16, contentY + 44, contentX + contentWidth - 16, contentY + 45, ZombekTheme.COLOR_CARD_BORDER);

        // Settings items
        List<Setting<?>> settings = selectedSettingsModule.getSettings();
        int itemY = contentY + 54 - scrollOffset;
        int itemH = 38;
        int itemW = contentWidth - 32;

        int visibleCount = 0;
        for (Setting<?> setting : settings) {
            if (!setting.isVisible()) continue;
            visibleCount++;

            if (itemY + itemH >= contentY && itemY <= contentY + contentHeight) {
                renderSettingRow(context, setting, contentX + 16, itemY, itemW, itemH, mouseX, mouseY);
            }
            itemY += itemH + 6;
        }

        int totalH = 54 + visibleCount * (itemH + 6) + 20;
        this.maxScroll = Math.max(0, totalH - contentHeight);
    }

    private void renderSettingRow(DrawContext context, Setting<?> setting, int x, int y, int w, int h, int mouseX, int mouseY) {
        TextRenderer tr = this.textRenderer;
        boolean isHovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;

        context.fill(x, y, x + w, y + h, isHovered ? ZombekTheme.COLOR_CARD_HOVER : ZombekTheme.COLOR_CARD_BG);
        context.drawBorder(x, y, w, h, ZombekTheme.COLOR_CARD_BORDER);

        // Name and description
        context.drawText(tr, setting.getName(), x + 10, y + 8, ZombekTheme.TEXT_PRIMARY, true);
        String desc = setting.getDescription();
        if (tr.getWidth(desc) > w - 210) {
            desc = tr.trimToWidth(desc, w - 220) + "...";
        }
        context.drawText(tr, desc, x + 10, y + 22, ZombekTheme.TEXT_SECONDARY, false);

        int controlX = x + w - 170;
        int controlY = y + 8;
        int controlW = 160;
        int controlH = 22;

        if (setting instanceof BooleanSetting) {
            BooleanSetting bs = (BooleanSetting) setting;
            boolean btnHover = mouseX >= controlX && mouseX <= controlX + controlW && mouseY >= controlY && mouseY <= controlY + controlH;
            int bg = bs.getValue()
                    ? (btnHover ? ZombekTheme.COLOR_ACCENT_GREEN_HOVER : ZombekTheme.COLOR_ACCENT_GREEN)
                    : (btnHover ? 0xFF475569 : ZombekTheme.COLOR_INPUT_BG);
            context.fill(controlX, controlY, controlX + controlW, controlY + controlH, bg);
            context.drawBorder(controlX, controlY, controlW, controlH, bs.getValue() ? ZombekTheme.COLOR_ACCENT_GREEN : ZombekTheme.COLOR_CARD_BORDER);
            context.drawCenteredTextWithShadow(tr, bs.getValue() ? "WŁĄCZONE" : "WYŁĄCZONE", controlX + controlW / 2, controlY + 7, bs.getValue() ? 0xFFFFFFFF : ZombekTheme.TEXT_MUTED);

        } else if (setting instanceof NumberSetting) {
            NumberSetting ns = (NumberSetting) setting;
            // Slider background
            context.fill(controlX, controlY, controlX + controlW, controlY + controlH, ZombekTheme.COLOR_INPUT_BG);
            context.drawBorder(controlX, controlY, controlW, controlH, ZombekTheme.COLOR_CARD_BORDER);

            // Filled track
            double progress = (ns.getValue() - ns.getMin()) / (ns.getMax() - ns.getMin());
            int fillW = (int) Math.round(progress * controlW);
            context.fill(controlX, controlY, controlX + fillW, controlY + controlH, 0x8810B981);
            context.fill(controlX + fillW - 2, controlY, controlX + fillW + 2, controlY + controlH, ZombekTheme.COLOR_ACCENT_GREEN);

            // Value text
            context.drawCenteredTextWithShadow(tr, ns.getFormattedValue(), controlX + controlW / 2, controlY + 7, ZombekTheme.TEXT_PRIMARY);

        } else if (setting instanceof ColorSetting) {
            ColorSetting cs = (ColorSetting) setting;
            // Color preview block
            int previewW = 32;
            context.fill(controlX, controlY, controlX + previewW, controlY + controlH, cs.getArgb());
            context.drawBorder(controlX, controlY, previewW, controlH, ZombekTheme.COLOR_CARD_BORDER);

            // Hex code display & click to cycle
            int hexX = controlX + previewW + 4;
            int hexW = controlW - previewW - 4;
            context.fill(hexX, controlY, hexX + hexW, controlY + controlH, ZombekTheme.COLOR_INPUT_BG);
            context.drawBorder(hexX, controlY, hexW, controlH, ZombekTheme.COLOR_CARD_BORDER);
            context.drawCenteredTextWithShadow(tr, cs.getHex(), hexX + hexW / 2, controlY + 7, ZombekTheme.TEXT_PRIMARY);

        } else if (setting instanceof EnumSetting<?>) {
            EnumSetting<?> es = (EnumSetting<?>) setting;
            boolean btnHover = mouseX >= controlX && mouseX <= controlX + controlW && mouseY >= controlY && mouseY <= controlY + controlH;
            context.fill(controlX, controlY, controlX + controlW, controlY + controlH, btnHover ? ZombekTheme.COLOR_CARD_HOVER : ZombekTheme.COLOR_INPUT_BG);
            context.drawBorder(controlX, controlY, controlW, controlH, btnHover ? ZombekTheme.COLOR_ACCENT_GOLD : ZombekTheme.COLOR_CARD_BORDER);
            context.drawCenteredTextWithShadow(tr, es.getFormattedValue(), controlX + controlW / 2, controlY + 7, ZombekTheme.TEXT_GOLD);

        } else if (setting instanceof KeybindSetting) {
            KeybindSetting ks = (KeybindSetting) setting;
            boolean isCapturingThis = (capturingSetting == ks);
            context.fill(controlX, controlY, controlX + controlW, controlY + controlH, ZombekTheme.COLOR_INPUT_BG);
            context.drawBorder(controlX, controlY, controlW, controlH, isCapturingThis ? ZombekTheme.COLOR_ACCENT_RED : ZombekTheme.COLOR_CARD_BORDER);
            String label = isCapturingThis ? "Naciśnij klawisz..." : "[" + ks.getKeyName() + "]";
            context.drawCenteredTextWithShadow(tr, label, controlX + controlW / 2, controlY + 7, isCapturingThis ? ZombekTheme.TEXT_ERROR : ZombekTheme.TEXT_GOLD);

        } else if (setting instanceof MultiSelectSetting) {
            MultiSelectSetting ms = (MultiSelectSetting) setting;
            context.fill(controlX, controlY, controlX + controlW, controlY + controlH, ZombekTheme.COLOR_INPUT_BG);
            context.drawBorder(controlX, controlY, controlW, controlH, ZombekTheme.COLOR_CARD_BORDER);
            String summary = ms.getValue().size() + "/" + ms.getAvailableOptions().size() + " wybrane";
            context.drawCenteredTextWithShadow(tr, summary, controlX + controlW / 2, controlY + 7, ZombekTheme.TEXT_PRIMARY);

        } else {
            // Generic / String setting
            context.fill(controlX, controlY, controlX + controlW, controlY + controlH, ZombekTheme.COLOR_INPUT_BG);
            context.drawBorder(controlX, controlY, controlW, controlH, ZombekTheme.COLOR_CARD_BORDER);
            String valStr = setting.serialize();
            if (valStr.length() > 18) valStr = valStr.substring(0, 16) + "...";
            context.drawCenteredTextWithShadow(tr, valStr, controlX + controlW / 2, controlY + 7, ZombekTheme.TEXT_PRIMARY);
        }
    }

    // ==========================================
    // CLIENT SETTINGS TAB RENDERING
    // ==========================================
    private void renderClientSettingsView(DrawContext context, int mouseX, int mouseY) {
        TextRenderer tr = this.textRenderer;
        int startX = contentX + 24;
        int startY = contentY + 20;

        context.drawText(tr, "Ustawienia Ogólne i Status ZombekClient", startX, startY, ZombekTheme.COLOR_ACCENT_GOLD, true);
        context.drawText(tr, "System konfiguracji: schemaVersion 1 (modules.json)", startX, startY + 16, ZombekTheme.TEXT_SECONDARY, false);
        context.drawText(tr, "Ścieżka pliku: config/zombekclient/modules.json", startX, startY + 30, ZombekTheme.TEXT_MUTED, false);

        int boxY = startY + 54;
        int boxW = contentWidth - 48;
        int boxH = 90;
        context.fill(startX, boxY, startX + boxW, boxY + boxH, ZombekTheme.COLOR_CARD_BG);
        context.drawBorder(startX, boxY, boxW, boxH, ZombekTheme.COLOR_CARD_BORDER);

        context.drawText(tr, "📊 Statystyki Modułów:", startX + 14, boxY + 12, ZombekTheme.TEXT_PRIMARY, true);
        context.drawText(tr, "• Łącznie zarejestrowanych modułów: " + moduleManager.getModuleCount(), startX + 24, boxY + 28, ZombekTheme.TEXT_SECONDARY, false);
        context.drawText(tr, "• Aktywne moduły: " + moduleManager.getEnabledModuleCount(), startX + 24, boxY + 42, ZombekTheme.COLOR_ACCENT_GREEN, false);
        context.drawText(tr, "• Domyślny klawisz menu: RIGHT SHIFT (zmień w Ustawieniach Sterowania MC)", startX + 24, boxY + 56, ZombekTheme.TEXT_SECONDARY, false);

        // Action button: Zapisz Konfigurację Teraz
        int btnW = 180;
        int btnH = 26;
        int btnX = startX;
        int btnY = boxY + boxH + 16;
        boolean btnHover = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        context.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnHover ? ZombekTheme.COLOR_ACCENT_GREEN_HOVER : ZombekTheme.COLOR_ACCENT_GREEN);
        context.drawBorder(btnX, btnY, btnW, btnH, ZombekTheme.COLOR_CARD_BORDER);
        context.drawCenteredTextWithShadow(tr, "💾 Zapisz konfigurację", btnX + btnW / 2, btnY + 9, 0xFFFFFFFF);
    }

    // ==========================================
    // BOTTOMBAR RENDERING
    // ==========================================
    private void renderBottombar(DrawContext context) {
        int bbX = contentX;
        int bbY = windowY + windowHeight - 28;
        int bbW = contentWidth;
        int bbH = 28;

        context.fill(bbX, bbY, bbX + bbW, bbY + bbH, ZombekTheme.COLOR_BOTTOMBAR_BG);
        context.fill(bbX, bbY, bbX + bbW, bbY + 1, ZombekTheme.COLOR_CARD_BORDER);

        TextRenderer tr = this.textRenderer;
        String activeProf = pl.zombek.client.core.hud.editor.HudProfileManager.getInstance().getActiveProfileName();
        String info = String.format("%s v%s | Profil HUD: [%s] | 1.20.4 Fabric",
                ClientConstants.CLIENT_NAME,
                ClientConstants.CLIENT_VERSION,
                activeProf);
        context.drawText(tr, info, bbX + 16, bbY + 10, ZombekTheme.TEXT_MUTED, false);

        String hint = "ESC / RIGHT SHIFT aby zamknąć";
        context.drawText(tr, hint, bbX + bbW - tr.getWidth(hint) - 16, bbY + 10, ZombekTheme.TEXT_GOLD, false);
    }

    // ==========================================
    // KEYBIND MODAL
    // ==========================================
    private void renderKeybindCaptureModal(DrawContext context) {
        // Semi-transparent overlay
        context.fill(windowX, windowY, windowX + windowWidth, windowY + windowHeight, 0xCC000000);

        int modalW = 320;
        int modalH = 130;
        int modalX = (this.width - modalW) / 2;
        int modalY = (this.height - modalH) / 2;

        context.fill(modalX, modalY, modalX + modalW, modalY + modalH, ZombekTheme.COLOR_CARD_BG);
        context.drawBorder(modalX, modalY, modalW, modalH, ZombekTheme.COLOR_ACCENT_GOLD);

        TextRenderer tr = this.textRenderer;
        context.drawCenteredTextWithShadow(tr, "Przypisz klawisz", modalX + modalW / 2, modalY + 18, ZombekTheme.COLOR_ACCENT_GOLD);

        String target = capturingModule != null ? capturingModule.getName() : (capturingSetting != null ? capturingSetting.getName() : "");
        context.drawCenteredTextWithShadow(tr, "Dla: " + target, modalX + modalW / 2, modalY + 36, ZombekTheme.TEXT_PRIMARY);
        context.drawCenteredTextWithShadow(tr, "Naciśnij dowolny klawisz lub przycisk myszy...", modalX + modalW / 2, modalY + 60, ZombekTheme.TEXT_SECONDARY);
        context.drawCenteredTextWithShadow(tr, "[ESC aby anulować / usunąć]", modalX + modalW / 2, modalY + 85, ZombekTheme.TEXT_MUTED);
    }

    // ==========================================
    // INPUT HANDLING: MOUSE & KEYBOARD
    // ==========================================
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle keybind capture via mouse button
        if (capturingModule != null || capturingSetting != null) {
            int mouseCode = 1000 + button; // Mouse codes mapped from 1000
            if (capturingModule != null) {
                capturingModule.setKeybind(mouseCode);
                capturingModule = null;
            } else if (capturingSetting != null) {
                capturingSetting.setValue(mouseCode);
                capturingSetting = null;
            }
            return true;
        }

        // 1. Sidebar tab clicks
        if (mouseX >= windowX && mouseX <= windowX + sidebarWidth && mouseY >= windowY && mouseY <= windowY + windowHeight) {
            int tabY = windowY + 46;
            int tabH = 32;
            for (NavigationTab tab : NavigationTab.values()) {
                if (mouseY >= tabY && mouseY < tabY + tabH) {
                    currentTab = tab;
                    selectedSettingsModule = null;
                    scrollOffset = 0;
                    return true;
                }
                tabY += tabH + 4;
            }
            return true;
        }

        // 2. Edit HUD button click
        int hudBtnW = 85;
        int hudBtnH = 22;
        int hudBtnX = (selectedSettingsModule == null && currentTab != NavigationTab.CLIENT_SETTINGS)
                ? (contentX + contentWidth - 160 - 16 - hudBtnW - 8)
                : (contentX + contentWidth - hudBtnW - 16);
        int hudBtnY = windowY + 11;
        if (mouseX >= hudBtnX && mouseX <= hudBtnX + hudBtnW && mouseY >= hudBtnY && mouseY <= hudBtnY + hudBtnH) {
            if (this.client != null) {
                this.client.setScreen(new ZombekFabricHudEditorScreen(this));
            }
            return true;
        }

        // 2b. Profile indicator click in bottombar
        int bbY = windowY + windowHeight - 28;
        if (mouseY >= bbY && mouseY <= bbY + 28 && mouseX >= contentX + 16 && mouseX <= contentX + 260) {
            pl.zombek.client.core.hud.editor.HudEditorManager.getInstance().switchProfileNext();
            return true;
        }

        // 3. Search box click
        if (selectedSettingsModule == null && currentTab != NavigationTab.CLIENT_SETTINGS) {
            int searchW = 160;
            int searchH = 22;
            int searchX = contentX + contentWidth - searchW - 16;
            int searchY = windowY + 11;
            if (mouseX >= searchX && mouseX <= searchX + searchW && mouseY >= searchY && mouseY <= searchY + searchH) {
                isSearching = true;
                return true;
            } else {
                isSearching = false;
            }
        }

        // 3. Main content area clicks
        if (mouseX >= contentX && mouseX <= contentX + contentWidth && mouseY >= contentY && mouseY <= contentY + contentHeight) {
            if (selectedSettingsModule != null) {
                return handleSettingsViewClick(mouseX, mouseY, button);
            } else if (currentTab == NavigationTab.CLIENT_SETTINGS) {
                return handleClientSettingsClick(mouseX, mouseY, button);
            } else {
                return handleModuleListClick(mouseX, mouseY, button);
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleModuleListClick(double mouseX, double mouseY, int button) {
        List<AbstractModule> list = searchQuery.isEmpty()
                ? moduleManager.getModulesByCategory(currentTab.getCategory())
                : moduleManager.searchModules(searchQuery);

        int startY = contentY + 12 - scrollOffset;
        int cardHeight = 56;
        int cardGap = 8;
        int cardWidth = contentWidth - 32;

        for (int i = 0; i < list.size(); i++) {
            AbstractModule module = list.get(i);
            int cardY = startY + i * (cardHeight + cardGap);
            int cardX = contentX + 16;

            if (mouseX < cardX || mouseX > cardX + cardWidth || mouseY < cardY || mouseY > cardY + cardHeight) {
                continue;
            }

            // Check Keybind button click
            int keybindW = 60;
            int keybindH = 22;
            int keybindX = cardX + cardWidth - 195;
            int keybindY = cardY + 17;
            if (mouseX >= keybindX && mouseX <= keybindX + keybindW && mouseY >= keybindY && mouseY <= keybindY + keybindH) {
                capturingModule = module;
                return true;
            }

            // Check Settings button click
            int gearW = 56;
            int gearH = 22;
            int gearX = cardX + cardWidth - 125;
            int gearY = cardY + 17;
            if (module.hasSettings() && mouseX >= gearX && mouseX <= gearX + gearW && mouseY >= gearY && mouseY <= gearY + gearH) {
                selectedSettingsModule = module;
                scrollOffset = 0;
                return true;
            }

            // Check Toggle switch click
            int toggleW = 52;
            int toggleH = 22;
            int toggleX = cardX + cardWidth - 62;
            int toggleY = cardY + 17;
            if (mouseX >= toggleX && mouseX <= toggleX + toggleW && mouseY >= toggleY && mouseY <= toggleY + toggleH) {
                module.toggle();
                return true;
            }
        }
        return false;
    }

    private boolean handleSettingsViewClick(double mouseX, double mouseY, int button) {
        // Back button
        int backBtnW = 100;
        int backBtnH = 24;
        int backBtnX = contentX + 16;
        int backBtnY = contentY + 12;
        if (mouseX >= backBtnX && mouseX <= backBtnX + backBtnW && mouseY >= backBtnY && mouseY <= backBtnY + backBtnH) {
            selectedSettingsModule = null;
            scrollOffset = 0;
            return true;
        }

        // Settings rows
        List<Setting<?>> settings = selectedSettingsModule.getSettings();
        int itemY = contentY + 54 - scrollOffset;
        int itemH = 38;
        int itemW = contentWidth - 32;

        for (Setting<?> setting : settings) {
            if (!setting.isVisible()) continue;

            int controlX = contentX + 16 + itemW - 170;
            int controlY = itemY + 8;
            int controlW = 160;
            int controlH = 22;

            if (mouseX >= controlX && mouseX <= controlX + controlW && mouseY >= controlY && mouseY <= controlY + controlH) {
                if (setting instanceof BooleanSetting) {
                    ((BooleanSetting) setting).toggle();
                    return true;
                } else if (setting instanceof NumberSetting) {
                    draggingSlider = (NumberSetting) setting;
                    sliderDragMinX = controlX;
                    sliderDragWidth = controlW;
                    updateSliderFromMouse(mouseX);
                    return true;
                } else if (setting instanceof ColorSetting) {
                    // Click to cycle common accent colors
                    ColorSetting cs = (ColorSetting) setting;
                    int curr = cs.getArgb();
                    if (curr == 0xFFF59E0B) cs.setValue(0xFF10B981);
                    else if (curr == 0xFF10B981) cs.setValue(0xFFEF4444);
                    else if (curr == 0xFFEF4444) cs.setValue(0xFF3B82F6);
                    else cs.setValue(0xFFF59E0B);
                    return true;
                } else if (setting instanceof EnumSetting<?>) {
                    ((EnumSetting<?>) setting).cycleNext();
                    return true;
                } else if (setting instanceof KeybindSetting) {
                    capturingSetting = (KeybindSetting) setting;
                    return true;
                } else if (setting instanceof MultiSelectSetting) {
                    MultiSelectSetting ms = (MultiSelectSetting) setting;
                    if (!ms.getAvailableOptions().isEmpty()) {
                        // Toggle first unselected or rotate
                        String next = ms.getAvailableOptions().get(0);
                        for (String opt : ms.getAvailableOptions()) {
                            if (!ms.isSelected(opt)) {
                                next = opt;
                                break;
                            }
                        }
                        ms.toggle(next);
                    }
                    return true;
                }
            }

            itemY += itemH + 6;
        }

        return false;
    }

    private boolean handleClientSettingsClick(double mouseX, double mouseY, int button) {
        int btnW = 180;
        int btnH = 26;
        int btnX = contentX + 24;
        int btnY = contentY + 20 + 54 + 90 + 16;
        if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
            moduleManager.notifyChange();
            return true;
        }
        return false;
    }

    private void updateSliderFromMouse(double mouseX) {
        if (draggingSlider == null || sliderDragWidth <= 0) return;
        double ratio = Math.max(0.0, Math.min(1.0, (mouseX - sliderDragMinX) / (double) sliderDragWidth));
        double newVal = draggingSlider.getMin() + ratio * (draggingSlider.getMax() - draggingSlider.getMin());
        draggingSlider.setValue(newVal);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingSlider != null) {
            updateSliderFromMouse(mouseX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingSlider = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (maxScroll > 0) {
            scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - verticalAmount * 20));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // 1. Keybind capture mode
        if (capturingModule != null || capturingSetting != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
                // Clear keybind
                if (capturingModule != null) capturingModule.setKeybind(KeybindSetting.KEY_NONE);
                if (capturingSetting != null) capturingSetting.setValue(KeybindSetting.KEY_NONE);
            } else {
                if (capturingModule != null) capturingModule.setKeybind(keyCode);
                if (capturingSetting != null) capturingSetting.setValue(keyCode);
            }
            capturingModule = null;
            capturingSetting = null;
            return true;
        }

        // 2. Searching text input
        if (isSearching) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !searchQuery.isEmpty()) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                scrollOffset = 0;
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
                isSearching = false;
                return true;
            }
        }

        // 3. ESC / Right Shift navigation
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (selectedSettingsModule != null) {
                selectedSettingsModule = null;
                scrollOffset = 0;
                return true;
            }
            this.close();
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT && !isSearching) {
            this.close();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (isSearching) {
            searchQuery += chr;
            scrollOffset = 0;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }
}
