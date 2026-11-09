package pl.zombek.client.quilt.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import pl.zombek.client.core.ClientConstants;
import pl.zombek.client.core.bootstrap.ZombekCore;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.modules.ModuleManager;
import pl.zombek.client.core.settings.*;
import pl.zombek.client.quilt.render.QuiltTheme;

import java.util.List;

/**
 * Primary in-game configuration menu for ZombekClient on Minecraft Quilt 1.20.4.
 * Opened via RIGHT SHIFT. Features identical festive dark aesthetics,
 * responsive layout, live search, module cards, settings drawer, and keybind capture.
 */
public class ZombekQuiltScreen extends Screen {
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

    public ZombekQuiltScreen() {
        super(Text.literal(ClientConstants.CLIENT_NAME + " Menu"));
        this.moduleManager = ZombekCore.getInstance().getModuleManager();
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
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 1. Background dim overlay
        context.fill(0, 0, this.width, this.height, QuiltTheme.BG_OVERLAY);

        // 2. Main Container
        context.fill(windowX, windowY, windowX + windowWidth, windowY + windowHeight, QuiltTheme.PANEL_BG);
        drawOutline(context, windowX, windowY, windowWidth, windowHeight, QuiltTheme.PANEL_BORDER);

        // 3. Render Sidebar
        renderSidebar(context, mouseX, mouseY);

        // 4. Render Topbar
        renderTopbar(context, mouseX, mouseY);

        // 5. Render Content Area
        context.enableScissor(contentX, contentY, contentX + contentWidth, contentY + contentHeight);
        if (selectedSettingsModule != null) {
            renderModuleSettingsView(context, mouseX, mouseY);
        } else if (currentTab == NavigationTab.CLIENT_SETTINGS) {
            renderClientSettingsView(context, mouseX, mouseY);
        } else {
            renderModuleListView(context, mouseX, mouseY);
        }
        context.disableScissor();

        // 6. Bottombar
        renderBottombar(context);

        // 7. Keybind Modal
        if (capturingModule != null || capturingSetting != null) {
            renderKeybindCaptureModal(context);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderSidebar(DrawContext context, int mouseX, int mouseY) {
        int sbX = windowX;
        int sbY = windowY;
        int sbW = sidebarWidth;
        int sbH = windowHeight;

        // Divider line
        context.fill(sbX + sbW - 1, sbY + 44, sbX + sbW, sbY + sbH - 28, QuiltTheme.CARD_BORDER);

        int tabY = sbY + 54;
        int tabHeight = 32;
        int tabSpacing = 4;

        NavigationTab[] tabs = NavigationTab.values();
        for (NavigationTab tab : tabs) {
            boolean isSelected = (currentTab == tab && selectedSettingsModule == null);
            boolean isHovered = mouseX >= sbX + 8 && mouseX <= sbX + sbW - 8 &&
                               mouseY >= tabY && mouseY <= tabY + tabHeight;

            int bg = isSelected ? QuiltTheme.CARD_BG_ENABLED : (isHovered ? QuiltTheme.CARD_BG_HOVER : 0);
            if (bg != 0) {
                context.fill(sbX + 8, tabY, sbX + sbW - 8, tabY + tabHeight, bg);
                if (isSelected) {
                    context.fill(sbX + 8, tabY, sbX + 11, tabY + tabHeight, QuiltTheme.ACCENT_CYAN);
                }
            }

            int count = (tab.getCategory() != null) ? moduleManager.getModulesByCategory(tab.getCategory()).size() : 0;
            String label = getTabDisplayName(tab);
            int textColor = isSelected ? QuiltTheme.ACCENT_CYAN : (isHovered ? QuiltTheme.TEXT_PRIMARY : QuiltTheme.TEXT_MUTED);

            context.drawTextWithShadow(textRenderer, label, sbX + 16, tabY + 11, textColor);

            if (tab.getCategory() != null) {
                String countStr = String.valueOf(count);
                int countW = textRenderer.getWidth(countStr);
                context.drawTextWithShadow(textRenderer, countStr, sbX + sbW - 20 - countW, tabY + 11, QuiltTheme.TEXT_DIM);
            }

            tabY += tabHeight + tabSpacing;
        }
    }

    private void renderTopbar(DrawContext context, int mouseX, int mouseY) {
        int tbX = windowX;
        int tbY = windowY;
        int tbW = windowWidth;
        int tbH = 44;

        // Bottom separator
        context.fill(tbX, tbY + tbH - 1, tbX + tbW, tbY + tbH, QuiltTheme.CARD_BORDER);

        // Logo
        context.drawTextWithShadow(textRenderer, "❄ " + ClientConstants.CLIENT_NAME.toUpperCase(), tbX + 16, tbY + 16, QuiltTheme.ACCENT_CYAN);

        // Version badge
        String ver = "v" + ClientConstants.CLIENT_VERSION + " [Quilt]";
        int verW = textRenderer.getWidth(ver);
        context.drawTextWithShadow(textRenderer, ver, tbX + 16 + textRenderer.getWidth("❄ " + ClientConstants.CLIENT_NAME.toUpperCase()) + 8, tbY + 16, QuiltTheme.TEXT_DIM);

        // "EDIT HUD" Button
        int hudBtnW = 85;
        int hudBtnH = 24;
        int hudBtnX = tbX + tbW - 180 - 16 - hudBtnW - 8;
        int hudBtnY = tbY + 10;
        boolean hudHover = mouseX >= hudBtnX && mouseX <= hudBtnX + hudBtnW && mouseY >= hudBtnY && mouseY <= hudBtnY + hudBtnH;
        context.fill(hudBtnX, hudBtnY, hudBtnX + hudBtnW, hudBtnY + hudBtnH, hudHover ? QuiltTheme.CARD_BG_HOVER : QuiltTheme.CARD_BG);
        drawOutline(context, hudBtnX, hudBtnY, hudBtnW, hudBtnH, hudHover ? QuiltTheme.ACCENT_CYAN : QuiltTheme.CARD_BORDER);
        context.drawTextWithShadow(textRenderer, "🎨 EDIT HUD", hudBtnX + 8, hudBtnY + 8, hudHover ? QuiltTheme.ACCENT_CYAN : QuiltTheme.TEXT_PRIMARY);

        // Search bar
        int sbW = 180;
        int sbH = 24;
        int sbX = tbX + tbW - sbW - 16;
        int sbY = tbY + 10;

        int sbBg = isSearching ? QuiltTheme.CARD_BG_HOVER : QuiltTheme.CARD_BG;
        int sbBorder = isSearching ? QuiltTheme.ACCENT_CYAN : QuiltTheme.CARD_BORDER;

        context.fill(sbX, sbY, sbX + sbW, sbY + sbH, sbBg);
        drawOutline(context, sbX, sbY, sbW, sbH, sbBorder);

        String displayText = searchQuery.isEmpty() ? (isSearching ? "" : "Szukaj modułów...") : searchQuery;
        int textCol = searchQuery.isEmpty() ? QuiltTheme.TEXT_DIM : QuiltTheme.TEXT_PRIMARY;
        context.drawTextWithShadow(textRenderer, displayText, sbX + 8, sbY + 8, textCol);
    }

    private void renderModuleListView(DrawContext context, int mouseX, int mouseY) {
        List<AbstractModule> modules;
        if (!searchQuery.trim().isEmpty()) {
            modules = moduleManager.searchModules(searchQuery.trim());
        } else if (currentTab.getCategory() != null) {
            modules = moduleManager.getModulesByCategory(currentTab.getCategory());
        } else {
            modules = List.of();
        }

        if (modules.isEmpty()) {
            String emptyMsg = searchQuery.trim().isEmpty()
                    ? "Brak modułów w kategorii " + getTabDisplayName(currentTab) + " ❄"
                    : "Brak wyników wyszukiwania dla '" + searchQuery + "'";
            int textW = textRenderer.getWidth(emptyMsg);
            context.drawTextWithShadow(textRenderer, emptyMsg, contentX + (contentWidth - textW) / 2, contentY + contentHeight / 2 - 10, QuiltTheme.TEXT_MUTED);
            context.drawTextWithShadow(textRenderer, "Gotowe moduły pojawią się w kolejnych etapach rozwoju.", contentX + (contentWidth - textRenderer.getWidth("Gotowe moduły pojawią się w kolejnych etapach rozwoju.")) / 2, contentY + contentHeight / 2 + 6, QuiltTheme.TEXT_DIM);
            maxScroll = 0;
            return;
        }

        int cardW = contentWidth - 24;
        int cardH = 54;
        int cardSpacing = 8;
        int startY = contentY + 12 - scrollOffset;
        int totalHeight = modules.size() * (cardH + cardSpacing) + 16;
        maxScroll = Math.max(0, totalHeight - contentHeight);

        for (AbstractModule module : modules) {
            int cardY = startY;
            startY += cardH + cardSpacing;

            if (cardY + cardH < contentY || cardY > contentY + contentHeight) {
                continue;
            }

            boolean isHovered = mouseX >= contentX + 12 && mouseX <= contentX + 12 + cardW &&
                               mouseY >= cardY && mouseY <= cardY + cardH;

            int bg = module.isEnabled() ? QuiltTheme.CARD_BG_ENABLED : (isHovered ? QuiltTheme.CARD_BG_HOVER : QuiltTheme.CARD_BG);
            int border = module.isEnabled() ? QuiltTheme.CARD_BORDER_ENABLED : QuiltTheme.CARD_BORDER;

            context.fill(contentX + 12, cardY, contentX + 12 + cardW, cardY + cardH, bg);
            drawOutline(context, contentX + 12, cardY, cardW, cardH, border);

            // Module Name & error badge
            context.drawTextWithShadow(textRenderer, module.getMetadata().getName(), contentX + 22, cardY + 10, QuiltTheme.TEXT_PRIMARY);
            if (module.hasError()) {
                context.drawTextWithShadow(textRenderer, "⚠ BŁĄD", contentX + 26 + textRenderer.getWidth(module.getMetadata().getName()), cardY + 10, QuiltTheme.ACCENT_RED);
            }

            // Description
            String desc = module.getMetadata().getDescription();
            if (textRenderer.getWidth(desc) > cardW - 140) {
                desc = textRenderer.trimToWidth(desc, cardW - 150) + "...";
            }
            context.drawTextWithShadow(textRenderer, desc, contentX + 22, cardY + 24, QuiltTheme.TEXT_MUTED);

            // Keybind pill
            int bindBtnW = 54;
            int bindBtnH = 20;
            int bindBtnX = contentX + 12 + cardW - 100 - bindBtnW;
            int bindBtnY = cardY + (cardH - bindBtnH) / 2;

            boolean bindHovered = mouseX >= bindBtnX && mouseX <= bindBtnX + bindBtnW &&
                                 mouseY >= bindBtnY && mouseY <= bindBtnY + bindBtnH;
            context.fill(bindBtnX, bindBtnY, bindBtnX + bindBtnW, bindBtnY + bindBtnH, bindHovered ? QuiltTheme.BUTTON_BG_HOVER : QuiltTheme.BUTTON_BG);
            drawOutline(context, bindBtnX, bindBtnY, bindBtnW, bindBtnH, QuiltTheme.BUTTON_BORDER);

            String bindText = module.getKeybindSetting().getKeyName();
            int bindTextW = textRenderer.getWidth(bindText);
            context.drawTextWithShadow(textRenderer, bindText, bindBtnX + (bindBtnW - bindTextW) / 2, bindBtnY + 6, QuiltTheme.TEXT_DIM);

            // Settings gear button
            int gearBtnW = 24;
            int gearBtnH = 20;
            int gearBtnX = contentX + 12 + cardW - 44 - gearBtnW;
            int gearBtnY = cardY + (cardH - gearBtnH) / 2;

            boolean gearHovered = mouseX >= gearBtnX && mouseX <= gearBtnX + gearBtnW &&
                                 mouseY >= gearBtnY && mouseY <= gearBtnY + gearBtnH;
            context.fill(gearBtnX, gearBtnY, gearBtnX + gearBtnW, gearBtnY + gearBtnH, gearHovered ? QuiltTheme.BUTTON_BG_HOVER : QuiltTheme.BUTTON_BG);
            drawOutline(context, gearBtnX, gearBtnY, gearBtnW, gearBtnH, QuiltTheme.BUTTON_BORDER);
            context.drawTextWithShadow(textRenderer, "⚙", gearBtnX + 7, gearBtnY + 6, gearHovered ? QuiltTheme.ACCENT_CYAN : QuiltTheme.TEXT_MUTED);

            // Toggle switch
            int toggleW = 34;
            int toggleH = 18;
            int toggleX = contentX + 12 + cardW - toggleW - 10;
            int toggleY = cardY + (cardH - toggleH) / 2;

            int trackCol = module.isEnabled() ? QuiltTheme.TOGGLE_TRACK_ON : QuiltTheme.TOGGLE_TRACK_OFF;
            int knobCol = module.isEnabled() ? QuiltTheme.TOGGLE_KNOB_ON : QuiltTheme.TOGGLE_KNOB_OFF;
            int knobX = module.isEnabled() ? toggleX + toggleW - 16 : toggleX + 2;

            context.fill(toggleX, toggleY, toggleX + toggleW, toggleY + toggleH, trackCol);
            context.fill(knobX, toggleY + 2, knobX + 14, toggleY + toggleH - 2, knobCol);
        }
    }

    private void renderModuleSettingsView(DrawContext context, int mouseX, int mouseY) {
        int startY = contentY + 12 - scrollOffset;

        // Back button
        int backBtnW = 120;
        int backBtnH = 22;
        int backBtnX = contentX + 12;
        int backBtnY = startY;

        boolean backHovered = mouseX >= backBtnX && mouseX <= backBtnX + backBtnW &&
                             mouseY >= backBtnY && mouseY <= backBtnY + backBtnH;
        context.fill(backBtnX, backBtnY, backBtnX + backBtnW, backBtnY + backBtnH, backHovered ? QuiltTheme.BUTTON_BG_HOVER : QuiltTheme.BUTTON_BG);
        drawOutline(context, backBtnX, backBtnY, backBtnW, backBtnH, QuiltTheme.BUTTON_BORDER);
        context.drawTextWithShadow(textRenderer, "← Wróć do listy", backBtnX + 12, backBtnY + 7, backHovered ? QuiltTheme.ACCENT_CYAN : QuiltTheme.TEXT_PRIMARY);

        startY += backBtnH + 14;

        // Title and description
        context.drawTextWithShadow(textRenderer, selectedSettingsModule.getMetadata().getName() + " — Ustawienia", contentX + 12, startY, QuiltTheme.ACCENT_CYAN);
        startY += 16;
        context.drawTextWithShadow(textRenderer, selectedSettingsModule.getMetadata().getDescription(), contentX + 12, startY, QuiltTheme.TEXT_MUTED);
        startY += 20;

        List<Setting<?>> settings = selectedSettingsModule.getSettings();
        if (settings.isEmpty()) {
            context.drawTextWithShadow(textRenderer, "Ten moduł nie posiada dodatkowych parametrów konfiguracyjnych.", contentX + 12, startY, QuiltTheme.TEXT_DIM);
            maxScroll = 0;
            return;
        }

        int settingW = contentWidth - 24;
        for (Setting<?> setting : settings) {
            if (!setting.isVisible()) continue;

            int rowH = 34;
            int rowY = startY;
            startY += rowH + 6;

            if (rowY + rowH < contentY || rowY > contentY + contentHeight) continue;

            context.fill(contentX + 12, rowY, contentX + 12 + settingW, rowY + rowH, QuiltTheme.CARD_BG);
            drawOutline(context, contentX + 12, rowY, settingW, rowY + rowH, QuiltTheme.CARD_BORDER);

            context.drawTextWithShadow(textRenderer, setting.getName(), contentX + 22, rowY + 12, QuiltTheme.TEXT_PRIMARY);

            // Render controls by setting type
            if (setting instanceof BooleanSetting boolSet) {
                int btnW = 44;
                int btnH = 20;
                int btnX = contentX + 12 + settingW - btnW - 10;
                int btnY = rowY + (rowH - btnH) / 2;

                boolean on = boolSet.getValue();
                context.fill(btnX, btnY, btnX + btnW, btnY + btnH, on ? QuiltTheme.TOGGLE_TRACK_ON : QuiltTheme.TOGGLE_TRACK_OFF);
                drawOutline(context, btnX, btnY, btnW, btnH, on ? QuiltTheme.ACCENT_GREEN : QuiltTheme.BUTTON_BORDER);
                String valStr = on ? "WŁ" : "WYŁ";
                int valW = textRenderer.getWidth(valStr);
                context.drawTextWithShadow(textRenderer, valStr, btnX + (btnW - valW) / 2, btnY + 6, on ? QuiltTheme.TEXT_PRIMARY : QuiltTheme.TEXT_MUTED);
            } else if (setting instanceof NumberSetting numSet) {
                int sliderW = 120;
                int sliderH = 14;
                int sliderX = contentX + 12 + settingW - sliderW - 55;
                int sliderY = rowY + (rowH - sliderH) / 2;

                double pct = (numSet.getValue() - numSet.getMin()) / (numSet.getMax() - numSet.getMin());
                pct = Math.max(0.0, Math.min(1.0, pct));
                int fillW = (int) (sliderW * pct);

                context.fill(sliderX, sliderY, sliderX + sliderW, sliderY + sliderH, QuiltTheme.SLIDER_TRACK);
                context.fill(sliderX, sliderY, sliderX + fillW, sliderY + sliderH, QuiltTheme.SLIDER_FILL);
                drawOutline(context, sliderX, sliderY, sliderW, sliderH, QuiltTheme.BUTTON_BORDER);

                int thumbX = sliderX + fillW - 2;
                context.fill(thumbX, sliderY - 2, thumbX + 4, sliderY + sliderH + 2, QuiltTheme.SLIDER_THUMB);

                String formatted = numSet.getFormattedValue();
                context.drawTextWithShadow(textRenderer, formatted, sliderX + sliderW + 8, rowY + 12, QuiltTheme.TEXT_PRIMARY);
            } else if (setting instanceof EnumSetting<?> enumSet) {
                int btnW = 90;
                int btnH = 20;
                int btnX = contentX + 12 + settingW - btnW - 10;
                int btnY = rowY + (rowH - btnH) / 2;

                boolean hov = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
                context.fill(btnX, btnY, btnX + btnW, btnY + btnH, hov ? QuiltTheme.BUTTON_BG_HOVER : QuiltTheme.BUTTON_BG);
                drawOutline(context, btnX, btnY, btnW, btnH, QuiltTheme.BUTTON_BORDER);

                String enumText = enumSet.getValue().toString();
                if (textRenderer.getWidth(enumText) > btnW - 8) {
                    enumText = textRenderer.trimToWidth(enumText, btnW - 12) + "...";
                }
                int textW = textRenderer.getWidth(enumText);
                context.drawTextWithShadow(textRenderer, enumText, btnX + (btnW - textW) / 2, btnY + 6, QuiltTheme.TEXT_PRIMARY);
            } else if (setting instanceof ColorSetting colorSet) {
                int boxW = 28;
                int boxH = 18;
                int boxX = contentX + 12 + settingW - boxW - 10;
                int boxY = rowY + (rowH - boxH) / 2;

                context.fill(boxX, boxY, boxX + boxW, boxY + boxH, colorSet.getValue());
                drawOutline(context, boxX, boxY, boxW, boxH, QuiltTheme.TEXT_PRIMARY);

                String hex = colorSet.getHex();
                int hexW = textRenderer.getWidth(hex);
                context.drawTextWithShadow(textRenderer, hex, boxX - hexW - 8, rowY + 12, QuiltTheme.TEXT_MUTED);
            } else if (setting instanceof KeybindSetting keySet) {
                int btnW = 60;
                int btnH = 20;
                int btnX = contentX + 12 + settingW - btnW - 10;
                int btnY = rowY + (rowH - btnH) / 2;

                boolean hov = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
                context.fill(btnX, btnY, btnX + btnW, btnY + btnH, hov ? QuiltTheme.BUTTON_BG_HOVER : QuiltTheme.BUTTON_BG);
                drawOutline(context, btnX, btnY, btnW, btnH, QuiltTheme.BUTTON_BORDER);

                String val = keySet.getKeyName();
                int valW = textRenderer.getWidth(val);
                context.drawTextWithShadow(textRenderer, val, btnX + (btnW - valW) / 2, btnY + 6, QuiltTheme.TEXT_PRIMARY);
            } else if (setting instanceof StringSetting strSet) {
                String val = strSet.getValue();
                if (textRenderer.getWidth(val) > 120) {
                    val = textRenderer.trimToWidth(val, 110) + "...";
                }
                int valW = textRenderer.getWidth(val);
                context.drawTextWithShadow(textRenderer, val, contentX + 12 + settingW - valW - 10, rowY + 12, QuiltTheme.TEXT_PRIMARY);
            }
        }

        maxScroll = Math.max(0, startY + scrollOffset - (contentY + contentHeight));
    }

    private void renderClientSettingsView(DrawContext context, int mouseX, int mouseY) {
        int startY = contentY + 16;
        context.drawTextWithShadow(textRenderer, "Ustawienia Klienta ZombekClient [Quilt]", contentX + 16, startY, QuiltTheme.ACCENT_CYAN);
        startY += 20;
        context.drawTextWithShadow(textRenderer, "Konfiguracja klienta i modułów zapisywana jest w formacie JSON v1:", contentX + 16, startY, QuiltTheme.TEXT_MUTED);
        startY += 16;
        context.drawTextWithShadow(textRenderer, "Katalog: config/zombekclient/modules.json", contentX + 16, startY, QuiltTheme.TEXT_DIM);
        startY += 26;

        context.drawTextWithShadow(textRenderer, "Wspierane Modloadery: Fabric, Forge, NeoForge, Quilt", contentX + 16, startY, QuiltTheme.ACCENT_GREEN);
    }

    private void renderBottombar(DrawContext context) {
        int bbX = windowX;
        int bbY = windowY + windowHeight - 28;
        int bbW = windowWidth;

        // Top separator
        context.fill(bbX, bbY, bbX + bbW, bbY + 1, QuiltTheme.CARD_BORDER);

        context.drawTextWithShadow(textRenderer, "ZombekClient — Uniwersalny Rdzeń Klienta Minecraft", bbX + 16, bbY + 9, QuiltTheme.TEXT_DIM);

        String tip = "Naciśnij [ESC] lub [RSHIFT], aby zamknąć";
        int tipW = textRenderer.getWidth(tip);
        context.drawTextWithShadow(textRenderer, tip, bbX + bbW - tipW - 16, bbY + 9, QuiltTheme.TEXT_DIM);
    }

    private void renderKeybindCaptureModal(DrawContext context) {
        context.fill(0, 0, this.width, this.height, 0xA0000000);

        int modalW = 280;
        int modalH = 120;
        int modalX = (this.width - modalW) / 2;
        int modalY = (this.height - modalH) / 2;

        context.fill(modalX, modalY, modalX + modalW, modalY + modalH, QuiltTheme.PANEL_BG);
        drawOutline(context, modalX, modalY, modalW, modalH, QuiltTheme.ACCENT_CYAN);

        String target = capturingModule != null ? capturingModule.getMetadata().getName() : capturingSetting.getName();
        context.drawCenteredTextWithShadow(textRenderer, "Przypisz klawisz dla:", modalX + modalW / 2, modalY + 20, QuiltTheme.TEXT_MUTED);
        context.drawCenteredTextWithShadow(textRenderer, target, modalX + modalW / 2, modalY + 36, QuiltTheme.ACCENT_CYAN);
        context.drawCenteredTextWithShadow(textRenderer, "Naciśnij dowolny klawisz...", modalX + modalW / 2, modalY + 60, QuiltTheme.TEXT_PRIMARY);
        context.drawCenteredTextWithShadow(textRenderer, "[ESC] = Anuluj / Wyczyść", modalX + modalW / 2, modalY + 85, QuiltTheme.TEXT_DIM);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (capturingModule != null || capturingSetting != null) {
            return true;
        }

        // 1. Edit HUD button click
        int hudBtnW = 85;
        int hudBtnH = 24;
        int hudBtnX = windowX + windowWidth - 180 - 16 - hudBtnW - 8;
        int hudBtnY = windowY + 10;
        if (mouseX >= hudBtnX && mouseX <= hudBtnX + hudBtnW && mouseY >= hudBtnY && mouseY <= hudBtnY + hudBtnH) {
            if (this.client != null) {
                this.client.setScreen(new ZombekQuiltHudEditorScreen(this));
            }
            return true;
        }

        // 2. Search box click
        int sbW = 180;
        int sbH = 24;
        int sbX = windowX + windowWidth - sbW - 16;
        int sbY = windowY + 10;
        if (mouseX >= sbX && mouseX <= sbX + sbW && mouseY >= sbY && mouseY <= sbY + sbH) {
            isSearching = true;
            return true;
        } else {
            isSearching = false;
        }

        // 2. Sidebar tab click
        if (mouseX >= windowX + 8 && mouseX <= windowX + sidebarWidth - 8) {
            int tabY = windowY + 54;
            int tabH = 32;
            int tabSpacing = 4;
            for (NavigationTab tab : NavigationTab.values()) {
                if (mouseY >= tabY && mouseY <= tabY + tabH) {
                    currentTab = tab;
                    selectedSettingsModule = null;
                    scrollOffset = 0;
                    return true;
                }
                tabY += tabH + tabSpacing;
            }
        }

        // 3. Settings View Click
        if (selectedSettingsModule != null) {
            int startY = contentY + 12 - scrollOffset;
            int backBtnW = 120;
            int backBtnH = 22;
            int backBtnX = contentX + 12;
            if (mouseX >= backBtnX && mouseX <= backBtnX + backBtnW && mouseY >= startY && mouseY <= startY + backBtnH) {
                selectedSettingsModule = null;
                scrollOffset = 0;
                return true;
            }

            startY += backBtnH + 14 + 16 + 20;
            int settingW = contentWidth - 24;
            for (Setting<?> setting : selectedSettingsModule.getSettings()) {
                if (!setting.isVisible()) continue;

                int rowH = 34;
                int rowY = startY;
                startY += rowH + 6;

                if (setting instanceof BooleanSetting boolSet) {
                    int btnW = 44;
                    int btnH = 20;
                    int btnX = contentX + 12 + settingW - btnW - 10;
                    int btnY = rowY + (rowH - btnH) / 2;
                    if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                        boolSet.toggle();
                        ZombekCore.getInstance().getConfigManager().scheduleSave();
                        return true;
                    }
                } else if (setting instanceof NumberSetting numSet) {
                    int sliderW = 120;
                    int sliderH = 14;
                    int sliderX = contentX + 12 + settingW - sliderW - 55;
                    int sliderY = rowY + (rowH - sliderH) / 2;
                    if (mouseX >= sliderX && mouseX <= sliderX + sliderW && mouseY >= sliderY && mouseY <= sliderY + sliderH) {
                        draggingSlider = numSet;
                        sliderDragMinX = sliderX;
                        sliderDragWidth = sliderW;
                        updateSliderFromMouse(mouseX);
                        return true;
                    }
                } else if (setting instanceof EnumSetting<?> enumSet) {
                    int btnW = 90;
                    int btnH = 20;
                    int btnX = contentX + 12 + settingW - btnW - 10;
                    int btnY = rowY + (rowH - btnH) / 2;
                    if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                        enumSet.cycleNext();
                        ZombekCore.getInstance().getConfigManager().scheduleSave();
                        return true;
                    }
                } else if (setting instanceof KeybindSetting keySet) {
                    int btnW = 60;
                    int btnH = 20;
                    int btnX = contentX + 12 + settingW - btnW - 10;
                    int btnY = rowY + (rowH - btnH) / 2;
                    if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                        capturingSetting = keySet;
                        return true;
                    }
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        // 4. Module Card Clicks
        List<AbstractModule> modules;
        if (!searchQuery.trim().isEmpty()) {
            modules = moduleManager.searchModules(searchQuery.trim());
        } else if (currentTab.getCategory() != null) {
            modules = moduleManager.getModulesByCategory(currentTab.getCategory());
        } else {
            modules = List.of();
        }

        int cardW = contentWidth - 24;
        int cardH = 54;
        int cardSpacing = 8;
        int startY = contentY + 12 - scrollOffset;

        for (AbstractModule module : modules) {
            int cardY = startY;
            startY += cardH + cardSpacing;

            if (cardY + cardH < contentY || cardY > contentY + contentHeight) continue;

            // Toggle switch click
            int toggleW = 34;
            int toggleH = 18;
            int toggleX = contentX + 12 + cardW - toggleW - 10;
            int toggleY = cardY + (cardH - toggleH) / 2;
            if (mouseX >= toggleX && mouseX <= toggleX + toggleW && mouseY >= toggleY && mouseY <= toggleY + toggleH) {
                module.toggle();
                ZombekCore.getInstance().getConfigManager().scheduleSave();
                return true;
            }

            // Gear click
            int gearBtnW = 24;
            int gearBtnH = 20;
            int gearBtnX = contentX + 12 + cardW - 44 - gearBtnW;
            int gearBtnY = cardY + (cardH - gearBtnH) / 2;
            if (mouseX >= gearBtnX && mouseX <= gearBtnX + gearBtnW && mouseY >= gearBtnY && mouseY <= gearBtnY + gearBtnH) {
                selectedSettingsModule = module;
                scrollOffset = 0;
                return true;
            }

            // Keybind pill click
            int bindBtnW = 54;
            int bindBtnH = 20;
            int bindBtnX = contentX + 12 + cardW - 100 - bindBtnW;
            int bindBtnY = cardY + (cardH - bindBtnH) / 2;
            if (mouseX >= bindBtnX && mouseX <= bindBtnX + bindBtnW && mouseY >= bindBtnY && mouseY <= bindBtnY + bindBtnH) {
                capturingModule = module;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
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
        if (draggingSlider != null) {
            draggingSlider = null;
            ZombekCore.getInstance().getConfigManager().scheduleSave();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void updateSliderFromMouse(double mouseX) {
        if (draggingSlider == null || sliderDragWidth <= 0) return;
        double pct = (mouseX - sliderDragMinX) / (double) sliderDragWidth;
        pct = Math.max(0.0, Math.min(1.0, pct));
        double val = draggingSlider.getMin() + pct * (draggingSlider.getMax() - draggingSlider.getMin());
        draggingSlider.setValue(val);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (maxScroll > 0) {
            scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - verticalAmount * 16));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Keybind capture modal
        if (capturingModule != null || capturingSetting != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                if (capturingModule != null) capturingModule.setKeybind(GLFW.GLFW_KEY_UNKNOWN);
                if (capturingSetting != null) capturingSetting.setValue(GLFW.GLFW_KEY_UNKNOWN);
            } else {
                if (capturingModule != null) capturingModule.setKeybind(keyCode);
                if (capturingSetting != null) capturingSetting.setValue(keyCode);
            }
            capturingModule = null;
            capturingSetting = null;
            ZombekCore.getInstance().getConfigManager().scheduleSave();
            return true;
        }

        // Live search typing
        if (isSearching) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!searchQuery.isEmpty()) {
                    searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                }
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
                isSearching = false;
                return true;
            }
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            this.close();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (isSearching && chr >= 32 && chr != 127) {
            searchQuery += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    private void drawOutline(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + 1, color);
        context.fill(x, y + h - 1, x + w, y + h, color);
        context.fill(x, y, x + 1, y + h, color);
        context.fill(x + w - 1, y, x + w, y + h, color);
    }

    private String getTabDisplayName(NavigationTab tab) {
        if (tab == NavigationTab.CLIENT_SETTINGS) return "Ustawienia";
        if (tab.getCategory() == null) return tab.name();
        return switch (tab.getCategory()) {
            case PVP -> "PvP / Combat";
            case HUD -> "HUD / Info";
            case VISUAL -> "Visual";
            case SURVIVAL -> "Survival";
            case OTHER -> "Other";
        };
    }
}
