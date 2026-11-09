package pl.zombek.client.forge.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import pl.zombek.client.core.ClientConstants;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.ModuleManager;
import pl.zombek.client.core.settings.*;
import pl.zombek.client.forge.render.ForgeTheme;

import java.util.List;

/**
 * Primary in-game configuration menu for ZombekClient on Minecraft Forge.
 * Opened via RIGHT SHIFT. Features identical festive dark aesthetics,
 * responsive layout, live search, module cards, settings drawer, and keybind capture.
 */
public class ZombekForgeScreen extends Screen {
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

    private int windowX;
    private int windowY;
    private int windowWidth;
    private int windowHeight;
    private int sidebarWidth;
    private int contentX;
    private int contentY;
    private int contentWidth;
    private int contentHeight;

    private NavigationTab currentTab = NavigationTab.OTHER;
    private AbstractModule selectedSettingsModule = null;
    private String searchQuery = "";
    private boolean isSearching = false;

    private int scrollOffset = 0;
    private int maxScroll = 0;

    private AbstractModule capturingModule = null;
    private KeybindSetting capturingSetting = null;

    private NumberSetting draggingSlider = null;
    private int sliderDragMinX = 0;
    private int sliderDragWidth = 0;

    public ZombekForgeScreen() {
        super(Component.literal(ClientConstants.CLIENT_NAME + " Menu"));
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
        this.contentY = windowY + 44;
        this.contentWidth = windowWidth - sidebarWidth;
        this.contentHeight = windowHeight - 44 - 28;

        this.scrollOffset = 0;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // 1. Overlay Dim
        graphics.fill(0, 0, this.width, this.height, ForgeTheme.COLOR_OVERLAY);

        // 2. Window Background
        graphics.fill(windowX, windowY, windowX + windowWidth, windowY + windowHeight, ForgeTheme.COLOR_WINDOW_BG);
        graphics.renderOutline(windowX, windowY, windowWidth, windowHeight, ForgeTheme.COLOR_CARD_BORDER);

        // 3. Sidebar
        renderSidebar(graphics, mouseX, mouseY);

        // 4. Topbar
        renderTopbar(graphics, mouseX, mouseY);

        // 5. Main Content Area with Scissor
        graphics.enableScissor(contentX, contentY, contentX + contentWidth, contentY + contentHeight);
        if (selectedSettingsModule != null) {
            renderModuleSettingsView(graphics, mouseX, mouseY);
        } else if (currentTab == NavigationTab.CLIENT_SETTINGS) {
            renderClientSettingsView(graphics, mouseX, mouseY);
        } else {
            renderModuleListView(graphics, mouseX, mouseY);
        }
        graphics.disableScissor();

        // 6. Bottombar
        renderBottombar(graphics);

        // 7. Modal if active
        if (capturingModule != null || capturingSetting != null) {
            renderKeybindCaptureModal(graphics);
        }

        super.render(graphics, mouseX, mouseY, delta);
    }

    private void renderSidebar(GuiGraphics graphics, int mouseX, int mouseY) {
        int sbX = windowX;
        int sbY = windowY;
        int sbW = sidebarWidth;
        int sbH = windowHeight;

        graphics.fill(sbX, sbY, sbX + sbW, sbY + sbH, ForgeTheme.COLOR_SIDEBAR_BG);
        graphics.fill(sbX + sbW - 1, sbY, sbX + sbW, sbY + sbH, ForgeTheme.COLOR_CARD_BORDER);

        Font font = this.font;
        int brandY = sbY + 12;
        graphics.drawString(font, "⚡ " + ClientConstants.CLIENT_NAME.toUpperCase(), sbX + 14, brandY, ForgeTheme.COLOR_ACCENT_GOLD, true);
        graphics.drawString(font, "v" + ClientConstants.CLIENT_VERSION + " (Forge)", sbX + 16, brandY + 12, ForgeTheme.TEXT_MUTED, false);

        int tabY = sbY + 46;
        int tabHeight = 32;

        for (NavigationTab tab : NavigationTab.values()) {
            boolean isSelected = (currentTab == tab && selectedSettingsModule == null);
            boolean isHovered = mouseX >= sbX + 8 && mouseX <= sbX + sbW - 8 && mouseY >= tabY && mouseY < tabY + tabHeight;

            int bgCol = isSelected ? ForgeTheme.COLOR_CARD_BG : (isHovered ? ForgeTheme.COLOR_CARD_HOVER : 0x00000000);
            if (bgCol != 0) {
                graphics.fill(sbX + 8, tabY, sbX + sbW - 8, tabY + tabHeight, bgCol);
                graphics.renderOutline(sbX + 8, tabY, sbW - 16, tabHeight, isSelected ? ForgeTheme.COLOR_ACCENT_GREEN : ForgeTheme.COLOR_CARD_BORDER);
            }

            if (isSelected) {
                graphics.fill(sbX + 8, tabY + 4, sbX + 11, tabY + tabHeight - 4, ForgeTheme.COLOR_ACCENT_GREEN);
            }

            String tabLabel;
            int count = 0;
            if (tab == NavigationTab.CLIENT_SETTINGS) {
                tabLabel = "⚙ Opcje Klienta";
            } else {
                tabLabel = tab.getCategory().getIconSymbol() + " " + tab.getCategory().getDisplayName();
                count = moduleManager.getModulesByCategory(tab.getCategory()).size();
            }

            int textCol = isSelected ? ForgeTheme.TEXT_PRIMARY : (isHovered ? ForgeTheme.TEXT_PRIMARY : ForgeTheme.TEXT_SECONDARY);
            graphics.drawString(font, tabLabel, sbX + 16, tabY + 11, textCol, false);

            if (tab != NavigationTab.CLIENT_SETTINGS && count > 0) {
                String countStr = String.valueOf(count);
                int countW = font.width(countStr);
                graphics.fill(sbX + sbW - 16 - countW - 6, tabY + 8, sbX + sbW - 14, tabY + 23, ForgeTheme.COLOR_INPUT_BG);
                graphics.drawString(font, countStr, sbX + sbW - 16 - countW - 3, tabY + 11, ForgeTheme.COLOR_ACCENT_GOLD, false);
            }

            tabY += tabHeight + 4;
        }
    }

    private void renderTopbar(GuiGraphics graphics, int mouseX, int mouseY) {
        int tbX = contentX;
        int tbY = windowY;
        int tbW = contentWidth;
        int tbH = 44;

        graphics.fill(tbX, tbY, tbX + tbW, tbY + tbH, ForgeTheme.COLOR_TOPBAR_BG);
        graphics.fill(tbX, tbY + tbH - 1, tbX + tbW, tbY + tbH, ForgeTheme.COLOR_CARD_BORDER);

        Font font = this.font;
        String headerTitle;
        if (selectedSettingsModule != null) {
            headerTitle = "← " + selectedSettingsModule.getName() + " - Ustawienia";
        } else if (currentTab == NavigationTab.CLIENT_SETTINGS) {
            headerTitle = "Opcje Klienta ZombekClient (Forge)";
        } else {
            headerTitle = currentTab.getCategory().getDisplayName();
        }

        graphics.drawString(font, headerTitle, tbX + 16, tbY + 17, ForgeTheme.TEXT_PRIMARY, true);

        // "EDIT HUD" Button
        int hudBtnW = 85;
        int hudBtnH = 22;
        int hudBtnX = (selectedSettingsModule == null && currentTab != NavigationTab.CLIENT_SETTINGS)
                ? (tbX + tbW - 160 - 16 - hudBtnW - 8)
                : (tbX + tbW - hudBtnW - 16);
        int hudBtnY = tbY + 11;
        boolean hudHover = mouseX >= hudBtnX && mouseX <= hudBtnX + hudBtnW && mouseY >= hudBtnY && mouseY <= hudBtnY + hudBtnH;
        graphics.fill(hudBtnX, hudBtnY, hudBtnX + hudBtnW, hudBtnY + hudBtnH, hudHover ? ForgeTheme.COLOR_CARD_HOVER : ForgeTheme.COLOR_INPUT_BG);
        graphics.renderOutline(hudBtnX, hudBtnY, hudBtnW, hudBtnH, hudHover ? ForgeTheme.COLOR_ACCENT_GOLD : ForgeTheme.COLOR_CARD_BORDER);
        graphics.drawString(font, "🎨 EDIT HUD", hudBtnX + 8, hudBtnY + 7, hudHover ? ForgeTheme.COLOR_ACCENT_GOLD : ForgeTheme.TEXT_PRIMARY, false);

        if (selectedSettingsModule == null && currentTab != NavigationTab.CLIENT_SETTINGS) {
            int searchW = 160;
            int searchH = 22;
            int searchX = tbX + tbW - searchW - 16;
            int searchY = tbY + 11;

            boolean isHovered = mouseX >= searchX && mouseX <= searchX + searchW && mouseY >= searchY && mouseY <= searchY + searchH;
            graphics.fill(searchX, searchY, searchX + searchW, searchY + searchH, ForgeTheme.COLOR_INPUT_BG);
            int borderCol = isSearching ? ForgeTheme.COLOR_ACCENT_GREEN : (isHovered ? ForgeTheme.COLOR_ACCENT_GOLD : ForgeTheme.COLOR_CARD_BORDER);
            graphics.renderOutline(searchX, searchY, searchW, searchH, borderCol);

            String displayText = searchQuery.isEmpty() ? (isSearching ? "_" : "🔍 Szukaj...") : searchQuery + (isSearching ? "_" : "");
            int textCol = searchQuery.isEmpty() && !isSearching ? ForgeTheme.TEXT_MUTED : ForgeTheme.TEXT_PRIMARY;
            graphics.drawString(font, displayText, searchX + 8, searchY + 7, textCol, false);
        }
    }

    private void renderModuleListView(GuiGraphics graphics, int mouseX, int mouseY) {
        Font font = this.font;
        List<AbstractModule> list = (searchQuery.isEmpty())
                ? moduleManager.getModulesByCategory(currentTab.getCategory())
                : moduleManager.searchModules(searchQuery);

        if (list.isEmpty()) {
            int centerX = contentX + contentWidth / 2;
            int centerY = contentY + contentHeight / 2 - 20;

            graphics.drawCenteredString(font, "❄ Brak modułów w tej kategorii ❄", centerX, centerY - 14, ForgeTheme.COLOR_ACCENT_GOLD);
            graphics.drawCenteredString(font, "Kolejne moduły pojawią się w następnych etapach rozwoju klienta.", centerX, centerY + 4, ForgeTheme.TEXT_SECONDARY);
            graphics.drawCenteredString(font, "Wybierz kategorię 'Other' aby przetestować framework.", centerX, centerY + 20, ForgeTheme.TEXT_MUTED);
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

            if (cardY + cardHeight < contentY || cardY > contentY + contentHeight) {
                continue;
            }

            int cardX = contentX + 16;
            boolean isHovered = mouseX >= cardX && mouseX <= cardX + cardWidth && mouseY >= cardY && mouseY <= cardY + cardHeight;

            int bgCol = isHovered ? ForgeTheme.COLOR_CARD_HOVER : ForgeTheme.COLOR_CARD_BG;
            graphics.fill(cardX, cardY, cardX + cardWidth, cardY + cardHeight, bgCol);
            int borderCol = module.isEnabled() ? ForgeTheme.COLOR_ACCENT_GREEN : ForgeTheme.COLOR_CARD_BORDER;
            if (module.hasError()) borderCol = ForgeTheme.COLOR_ACCENT_RED;
            graphics.renderOutline(cardX, cardY, cardWidth, cardHeight, borderCol);

            int barCol = module.hasError() ? ForgeTheme.COLOR_ACCENT_RED : (module.isEnabled() ? ForgeTheme.COLOR_ACCENT_GREEN : ForgeTheme.COLOR_CARD_BORDER);
            graphics.fill(cardX, cardY, cardX + 4, cardY + cardHeight, barCol);

            int textX = cardX + 12;
            graphics.drawString(font, module.getName(), textX, cardY + 10, ForgeTheme.TEXT_PRIMARY, true);
            if (module.hasError()) {
                graphics.drawString(font, "[BŁĄD - Moduł wstrzymany]", textX + font.width(module.getName()) + 8, cardY + 10, ForgeTheme.TEXT_ERROR, false);
            }

            String desc = module.getDescription();
            if (font.width(desc) > cardWidth - 210) {
                desc = font.plainSubstrByWidth(desc, cardWidth - 220) + "...";
            }
            graphics.drawString(font, desc, textX, cardY + 28, ForgeTheme.TEXT_SECONDARY, false);

            // Controls
            int keybindW = 60;
            int keybindH = 22;
            int keybindX = cardX + cardWidth - 195;
            int keybindY = cardY + 17;
            boolean keyHover = mouseX >= keybindX && mouseX <= keybindX + keybindW && mouseY >= keybindY && mouseY <= keybindY + keybindH;
            graphics.fill(keybindX, keybindY, keybindX + keybindW, keybindY + keybindH, ForgeTheme.COLOR_INPUT_BG);
            graphics.renderOutline(keybindX, keybindY, keybindW, keybindH, keyHover ? ForgeTheme.COLOR_ACCENT_GOLD : ForgeTheme.COLOR_CARD_BORDER);
            String keyStr = "[" + KeybindSetting.getKeyName(module.getKeybind()) + "]";
            graphics.drawCenteredString(font, keyStr, keybindX + keybindW / 2, keybindY + 7, ForgeTheme.TEXT_GOLD);

            int gearW = 56;
            int gearH = 22;
            int gearX = cardX + cardWidth - 125;
            int gearY = cardY + 17;
            if (module.hasSettings()) {
                boolean gearHover = mouseX >= gearX && mouseX <= gearX + gearW && mouseY >= gearY && mouseY <= gearY + gearH;
                graphics.fill(gearX, gearY, gearX + gearW, gearY + gearH, gearHover ? ForgeTheme.COLOR_CARD_HOVER : ForgeTheme.COLOR_INPUT_BG);
                graphics.renderOutline(gearX, gearY, gearW, gearH, gearHover ? ForgeTheme.COLOR_ACCENT_BLUE : ForgeTheme.COLOR_CARD_BORDER);
                graphics.drawCenteredString(font, "⚙ Opcje", gearX + gearW / 2, gearY + 7, ForgeTheme.TEXT_PRIMARY);
            }

            int toggleW = 52;
            int toggleH = 22;
            int toggleX = cardX + cardWidth - 62;
            int toggleY = cardY + 17;
            boolean toggleHover = mouseX >= toggleX && mouseX <= toggleX + toggleW && mouseY >= toggleY && mouseY <= toggleY + toggleH;
            int toggleBg = module.isEnabled()
                    ? (toggleHover ? ForgeTheme.COLOR_ACCENT_GREEN_HOVER : ForgeTheme.COLOR_ACCENT_GREEN)
                    : (toggleHover ? 0xFF475569 : ForgeTheme.COLOR_INPUT_BG);
            graphics.fill(toggleX, toggleY, toggleX + toggleW, toggleY + toggleH, toggleBg);
            graphics.renderOutline(toggleX, toggleY, toggleW, toggleH, module.isEnabled() ? ForgeTheme.COLOR_ACCENT_GREEN : ForgeTheme.COLOR_CARD_BORDER);
            String toggleText = module.isEnabled() ? "ON" : "OFF";
            int toggleCol = module.isEnabled() ? 0xFFFFFFFF : ForgeTheme.TEXT_MUTED;
            graphics.drawCenteredString(font, toggleText, toggleX + toggleW / 2, toggleY + 7, toggleCol);
        }
    }

    private void renderModuleSettingsView(GuiGraphics graphics, int mouseX, int mouseY) {
        if (selectedSettingsModule == null) return;
        Font font = this.font;

        int backBtnW = 100;
        int backBtnH = 24;
        int backBtnX = contentX + 16;
        int backBtnY = contentY + 12;

        boolean backHover = mouseX >= backBtnX && mouseX <= backBtnX + backBtnW && mouseY >= backBtnY && mouseY <= backBtnY + backBtnH;
        graphics.fill(backBtnX, backBtnY, backBtnX + backBtnW, backBtnY + backBtnH, backHover ? ForgeTheme.COLOR_CARD_HOVER : ForgeTheme.COLOR_CARD_BG);
        graphics.renderOutline(backBtnX, backBtnY, backBtnW, backBtnH, backHover ? ForgeTheme.COLOR_ACCENT_GOLD : ForgeTheme.COLOR_CARD_BORDER);
        graphics.drawCenteredString(font, "← Powrót", backBtnX + backBtnW / 2, backBtnY + 8, ForgeTheme.TEXT_PRIMARY);

        graphics.drawString(font, "Kategoria: " + selectedSettingsModule.getCategory().getDisplayName(), backBtnX + backBtnW + 16, backBtnY + 4, ForgeTheme.COLOR_ACCENT_GOLD, false);
        graphics.drawString(font, "Autor: " + selectedSettingsModule.getAuthor() + " | Wersja: " + selectedSettingsModule.getVersion(), backBtnX + backBtnW + 16, backBtnY + 15, ForgeTheme.TEXT_MUTED, false);

        graphics.fill(contentX + 16, contentY + 44, contentX + contentWidth - 16, contentY + 45, ForgeTheme.COLOR_CARD_BORDER);

        List<Setting<?>> settings = selectedSettingsModule.getSettings();
        int itemY = contentY + 54 - scrollOffset;
        int itemH = 38;
        int itemW = contentWidth - 32;

        int visibleCount = 0;
        for (Setting<?> setting : settings) {
            if (!setting.isVisible()) continue;
            visibleCount++;

            if (itemY + itemH >= contentY && itemY <= contentY + contentHeight) {
                renderSettingRow(graphics, setting, contentX + 16, itemY, itemW, itemH, mouseX, mouseY);
            }
            itemY += itemH + 6;
        }

        int totalH = 54 + visibleCount * (itemH + 6) + 20;
        this.maxScroll = Math.max(0, totalH - contentHeight);
    }

    private void renderSettingRow(GuiGraphics graphics, Setting<?> setting, int x, int y, int w, int h, int mouseX, int mouseY) {
        Font font = this.font;
        boolean isHovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;

        graphics.fill(x, y, x + w, y + h, isHovered ? ForgeTheme.COLOR_CARD_HOVER : ForgeTheme.COLOR_CARD_BG);
        graphics.renderOutline(x, y, w, h, ForgeTheme.COLOR_CARD_BORDER);

        graphics.drawString(font, setting.getName(), x + 10, y + 8, ForgeTheme.TEXT_PRIMARY, true);
        String desc = setting.getDescription();
        if (font.width(desc) > w - 210) {
            desc = font.plainSubstrByWidth(desc, w - 220) + "...";
        }
        graphics.drawString(font, desc, x + 10, y + 22, ForgeTheme.TEXT_SECONDARY, false);

        int controlX = x + w - 170;
        int controlY = y + 8;
        int controlW = 160;
        int controlH = 22;

        if (setting instanceof BooleanSetting) {
            BooleanSetting bs = (BooleanSetting) setting;
            boolean btnHover = mouseX >= controlX && mouseX <= controlX + controlW && mouseY >= controlY && mouseY <= controlY + controlH;
            int bg = bs.getValue()
                    ? (btnHover ? ForgeTheme.COLOR_ACCENT_GREEN_HOVER : ForgeTheme.COLOR_ACCENT_GREEN)
                    : (btnHover ? 0xFF475569 : ForgeTheme.COLOR_INPUT_BG);
            graphics.fill(controlX, controlY, controlX + controlW, controlY + controlH, bg);
            graphics.renderOutline(controlX, controlY, controlW, controlH, bs.getValue() ? ForgeTheme.COLOR_ACCENT_GREEN : ForgeTheme.COLOR_CARD_BORDER);
            graphics.drawCenteredString(font, bs.getValue() ? "WŁĄCZONE" : "WYŁĄCZONE", controlX + controlW / 2, controlY + 7, bs.getValue() ? 0xFFFFFFFF : ForgeTheme.TEXT_MUTED);

        } else if (setting instanceof NumberSetting) {
            NumberSetting ns = (NumberSetting) setting;
            graphics.fill(controlX, controlY, controlX + controlW, controlY + controlH, ForgeTheme.COLOR_INPUT_BG);
            graphics.renderOutline(controlX, controlY, controlW, controlH, ForgeTheme.COLOR_CARD_BORDER);

            double progress = (ns.getValue() - ns.getMin()) / (ns.getMax() - ns.getMin());
            int fillW = (int) Math.round(progress * controlW);
            graphics.fill(controlX, controlY, controlX + fillW, controlY + controlH, 0x8810B981);
            graphics.fill(controlX + fillW - 2, controlY, controlX + fillW + 2, controlY + controlH, ForgeTheme.COLOR_ACCENT_GREEN);

            graphics.drawCenteredString(font, ns.getFormattedValue(), controlX + controlW / 2, controlY + 7, ForgeTheme.TEXT_PRIMARY);

        } else if (setting instanceof ColorSetting) {
            ColorSetting cs = (ColorSetting) setting;
            int previewW = 32;
            graphics.fill(controlX, controlY, controlX + previewW, controlY + controlH, cs.getArgb());
            graphics.renderOutline(controlX, controlY, previewW, controlH, ForgeTheme.COLOR_CARD_BORDER);

            int hexX = controlX + previewW + 4;
            int hexW = controlW - previewW - 4;
            graphics.fill(hexX, controlY, hexX + hexW, controlY + controlH, ForgeTheme.COLOR_INPUT_BG);
            graphics.renderOutline(hexX, controlY, hexW, controlH, ForgeTheme.COLOR_CARD_BORDER);
            graphics.drawCenteredString(font, cs.getHex(), hexX + hexW / 2, controlY + 7, ForgeTheme.TEXT_PRIMARY);

        } else if (setting instanceof EnumSetting<?>) {
            EnumSetting<?> es = (EnumSetting<?>) setting;
            boolean btnHover = mouseX >= controlX && mouseX <= controlX + controlW && mouseY >= controlY && mouseY <= controlY + controlH;
            graphics.fill(controlX, controlY, controlX + controlW, controlY + controlH, btnHover ? ForgeTheme.COLOR_CARD_HOVER : ForgeTheme.COLOR_INPUT_BG);
            graphics.renderOutline(controlX, controlY, controlW, controlH, btnHover ? ForgeTheme.COLOR_ACCENT_GOLD : ForgeTheme.COLOR_CARD_BORDER);
            graphics.drawCenteredString(font, es.getFormattedValue(), controlX + controlW / 2, controlY + 7, ForgeTheme.TEXT_GOLD);

        } else if (setting instanceof KeybindSetting) {
            KeybindSetting ks = (KeybindSetting) setting;
            boolean isCapturingThis = (capturingSetting == ks);
            graphics.fill(controlX, controlY, controlX + controlW, controlY + controlH, ForgeTheme.COLOR_INPUT_BG);
            graphics.renderOutline(controlX, controlY, controlW, controlH, isCapturingThis ? ForgeTheme.COLOR_ACCENT_RED : ForgeTheme.COLOR_CARD_BORDER);
            String label = isCapturingThis ? "Naciśnij klawisz..." : "[" + ks.getKeyName() + "]";
            graphics.drawCenteredString(font, label, controlX + controlW / 2, controlY + 7, isCapturingThis ? ForgeTheme.TEXT_ERROR : ForgeTheme.TEXT_GOLD);

        } else if (setting instanceof MultiSelectSetting) {
            MultiSelectSetting ms = (MultiSelectSetting) setting;
            graphics.fill(controlX, controlY, controlX + controlW, controlY + controlH, ForgeTheme.COLOR_INPUT_BG);
            graphics.renderOutline(controlX, controlY, controlW, controlH, ForgeTheme.COLOR_CARD_BORDER);
            String summary = ms.getValue().size() + "/" + ms.getAvailableOptions().size() + " wybrane";
            graphics.drawCenteredString(font, summary, controlX + controlW / 2, controlY + 7, ForgeTheme.TEXT_PRIMARY);

        } else {
            graphics.fill(controlX, controlY, controlX + controlW, controlY + controlH, ForgeTheme.COLOR_INPUT_BG);
            graphics.renderOutline(controlX, controlY, controlW, controlH, ForgeTheme.COLOR_CARD_BORDER);
            String valStr = setting.serialize();
            if (valStr.length() > 18) valStr = valStr.substring(0, 16) + "...";
            graphics.drawCenteredString(font, valStr, controlX + controlW / 2, controlY + 7, ForgeTheme.TEXT_PRIMARY);
        }
    }

    private void renderClientSettingsView(GuiGraphics graphics, int mouseX, int mouseY) {
        Font font = this.font;
        int startX = contentX + 24;
        int startY = contentY + 20;

        graphics.drawString(font, "Ustawienia Ogólne i Status ZombekClient (Forge)", startX, startY, ForgeTheme.COLOR_ACCENT_GOLD, true);
        graphics.drawString(font, "System konfiguracji: schemaVersion 1 (modules.json)", startX, startY + 16, ForgeTheme.TEXT_SECONDARY, false);
        graphics.drawString(font, "Katalog: config/zombekclient/modules.json", startX, startY + 30, ForgeTheme.TEXT_MUTED, false);

        int boxY = startY + 54;
        int boxW = contentWidth - 48;
        int boxH = 90;
        graphics.fill(startX, boxY, startX + boxW, boxY + boxH, ForgeTheme.COLOR_CARD_BG);
        graphics.renderOutline(startX, boxY, boxW, boxH, ForgeTheme.COLOR_CARD_BORDER);

        graphics.drawString(font, "📊 Statystyki Modułów:", startX + 14, boxY + 12, ForgeTheme.TEXT_PRIMARY, true);
        graphics.drawString(font, "• Łącznie zarejestrowanych modułów: " + moduleManager.getModuleCount(), startX + 24, boxY + 28, ForgeTheme.TEXT_SECONDARY, false);
        graphics.drawString(font, "• Aktywne moduły: " + moduleManager.getEnabledModuleCount(), startX + 24, boxY + 42, ForgeTheme.COLOR_ACCENT_GREEN, false);
        graphics.drawString(font, "• Domyślny klawisz menu: RIGHT SHIFT (zmień w Sterowaniu Forge)", startX + 24, boxY + 56, ForgeTheme.TEXT_SECONDARY, false);

        int btnW = 180;
        int btnH = 26;
        int btnX = startX;
        int btnY = boxY + boxH + 16;
        boolean btnHover = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnHover ? ForgeTheme.COLOR_ACCENT_GREEN_HOVER : ForgeTheme.COLOR_ACCENT_GREEN);
        graphics.renderOutline(btnX, btnY, btnW, btnH, ForgeTheme.COLOR_CARD_BORDER);
        graphics.drawCenteredString(font, "💾 Zapisz konfigurację", btnX + btnW / 2, btnY + 9, 0xFFFFFFFF);
    }

    private void renderBottombar(GuiGraphics graphics) {
        int bbX = contentX;
        int bbY = windowY + windowHeight - 28;
        int bbW = contentWidth;
        int bbH = 28;

        graphics.fill(bbX, bbY, bbX + bbW, bbY + bbH, ForgeTheme.COLOR_BOTTOMBAR_BG);
        graphics.fill(bbX, bbY, bbX + bbW, bbY + 1, ForgeTheme.COLOR_CARD_BORDER);

        Font font = this.font;
        String info = String.format("%s v%s | Minecraft %s | Loader: Forge",
                ClientConstants.CLIENT_NAME,
                ClientConstants.CLIENT_VERSION,
                "1.20.4");
        graphics.drawString(font, info, bbX + 16, bbY + 10, ForgeTheme.TEXT_MUTED, false);

        String hint = "ESC / RIGHT SHIFT aby zamknąć";
        graphics.drawString(font, hint, bbX + bbW - font.width(hint) - 16, bbY + 10, ForgeTheme.TEXT_GOLD, false);
    }

    private void renderKeybindCaptureModal(GuiGraphics graphics) {
        graphics.fill(windowX, windowY, windowX + windowWidth, windowY + windowHeight, 0xCC000000);

        int modalW = 320;
        int modalH = 130;
        int modalX = (this.width - modalW) / 2;
        int modalY = (this.height - modalH) / 2;

        graphics.fill(modalX, modalY, modalX + modalW, modalY + modalH, ForgeTheme.COLOR_CARD_BG);
        graphics.renderOutline(modalX, modalY, modalW, modalH, ForgeTheme.COLOR_ACCENT_GOLD);

        Font font = this.font;
        graphics.drawCenteredString(font, "Przypisz klawisz", modalX + modalW / 2, modalY + 18, ForgeTheme.COLOR_ACCENT_GOLD);

        String target = capturingModule != null ? capturingModule.getName() : (capturingSetting != null ? capturingSetting.getName() : "");
        graphics.drawCenteredString(font, "Dla: " + target, modalX + modalW / 2, modalY + 36, ForgeTheme.TEXT_PRIMARY);
        graphics.drawCenteredString(font, "Naciśnij dowolny klawisz lub przycisk myszy...", modalX + modalW / 2, modalY + 60, ForgeTheme.TEXT_SECONDARY);
        graphics.drawCenteredString(font, "[ESC aby anulować / usunąć]", modalX + modalW / 2, modalY + 85, ForgeTheme.TEXT_MUTED);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (capturingModule != null || capturingSetting != null) {
            int mouseCode = 1000 + button;
            if (capturingModule != null) {
                capturingModule.setKeybind(mouseCode);
                capturingModule = null;
            } else if (capturingSetting != null) {
                capturingSetting.setValue(mouseCode);
                capturingSetting = null;
            }
            return true;
        }

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

        // Edit HUD button click
        int hudBtnW = 85;
        int hudBtnH = 22;
        int hudBtnX = (selectedSettingsModule == null && currentTab != NavigationTab.CLIENT_SETTINGS)
                ? (contentX + contentWidth - 160 - 16 - hudBtnW - 8)
                : (contentX + contentWidth - hudBtnW - 16);
        int hudBtnY = windowY + 11;
        if (mouseX >= hudBtnX && mouseX <= hudBtnX + hudBtnW && mouseY >= hudBtnY && mouseY <= hudBtnY + hudBtnH) {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new ZombekForgeHudEditorScreen(this));
            }
            return true;
        }

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

            int keybindW = 60;
            int keybindH = 22;
            int keybindX = cardX + cardWidth - 195;
            int keybindY = cardY + 17;
            if (mouseX >= keybindX && mouseX <= keybindX + keybindW && mouseY >= keybindY && mouseY <= keybindY + keybindH) {
                capturingModule = module;
                return true;
            }

            int gearW = 56;
            int gearH = 22;
            int gearX = cardX + cardWidth - 125;
            int gearY = cardY + 17;
            if (module.hasSettings() && mouseX >= gearX && mouseX <= gearX + gearW && mouseY >= gearY && mouseY <= gearY + gearH) {
                selectedSettingsModule = module;
                scrollOffset = 0;
                return true;
            }

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
        int backBtnW = 100;
        int backBtnH = 24;
        int backBtnX = contentX + 16;
        int backBtnY = contentY + 12;
        if (mouseX >= backBtnX && mouseX <= backBtnX + backBtnW && mouseY >= backBtnY && mouseY <= backBtnY + backBtnH) {
            selectedSettingsModule = null;
            scrollOffset = 0;
            return true;
        }

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
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (maxScroll > 0) {
            scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - scrollY * 20));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (capturingModule != null || capturingSetting != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
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

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (selectedSettingsModule != null) {
                selectedSettingsModule = null;
                scrollOffset = 0;
                return true;
            }
            this.onClose();
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT && !isSearching) {
            this.onClose();
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
