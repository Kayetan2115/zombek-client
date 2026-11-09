package pl.zombek.client.core.modules.pvp;

import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.HudElementRegistry;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;
import pl.zombek.client.core.tracking.ClickTracker;
import pl.zombek.client.core.tracking.KeyTracker;

/**
 * Keystrokes HUD Module — Inspired by competitive PvP clients (Lunar / Badlion).
 * Displays real-time state of WASD movement keys, Spacebar, LMB, RMB, Shift, and Ctrl.
 * Highly customizable layout, colors, anchors, and mouse button styles.
 */
public class KeystrokesModule extends AbstractModule implements IHudElement {

    public enum LayoutStyle {
        STANDARD("Standardowy (W, ASD, Mysz, Spacja)"),
        COMPACT("Kompaktowy"),
        WASD_ONLY("Tylko WASD");

        private final String displayName;
        LayoutStyle(String displayName) { this.displayName = displayName; }
        @Override public String toString() { return displayName; }
    }

    public enum MouseStyle {
        CPS_AND_NAME("Nazwa i CPS (LMB 10)"),
        NAME_ONLY("Tylko Nazwa (LMB)"),
        CPS_ONLY("Tylko CPS (10 CPS)");

        private final String displayName;
        MouseStyle(String displayName) { this.displayName = displayName; }
        @Override public String toString() { return displayName; }
    }

    // General Group
    private final BooleanSetting showW;
    private final BooleanSetting showA;
    private final BooleanSetting showS;
    private final BooleanSetting showD;
    private final BooleanSetting showLmb;
    private final BooleanSetting showRmb;
    private final BooleanSetting showSpace;
    private final BooleanSetting showShift;
    private final BooleanSetting showCtrl;

    // Layout Group
    private final EnumSetting<LayoutStyle> layoutStyle;
    private final NumberSetting horizontalGap;
    private final NumberSetting verticalGap;
    private final NumberSetting keyWidth;
    private final NumberSetting keyHeight;
    private final NumberSetting scale;
    private final NumberSetting padding;
    private final NumberSetting positionX;
    private final NumberSetting positionY;
    private final EnumSetting<HudAnchor> anchor;

    // Colors Group
    private final ColorSetting backgroundColor;
    private final ColorSetting pressedBackgroundColor;
    private final ColorSetting textColor;
    private final ColorSetting pressedTextColor;
    private final ColorSetting borderColor;
    private final NumberSetting opacity;

    // Appearance Group
    private final BooleanSetting showBackground;
    private final BooleanSetting showBorder;
    private final BooleanSetting showShadow;
    private final NumberSetting borderThickness;
    private final NumberSetting cornerRadius;
    private final NumberSetting textSize;
    private final BooleanSetting boldText;

    // Advanced Group
    private final EnumSetting<MouseStyle> mouseButtonStyle;
    private final BooleanSetting customLabels;
    private final NumberSetting keySpacing;
    private final NumberSetting animationDuration;

    public KeystrokesModule() {
        super("keystrokes", "Keystrokes", "Wyświetla naciśnięte klawisze ruchu i przyciski myszy na ekranie gry.", ModuleCategory.PVP);

        // --- 1. General ---
        SettingGroup generalGroup = new SettingGroup("general", "Ogólne");
        showW = generalGroup.addSetting(new BooleanSetting("show_w", "Pokaż W", "Wyświetla klawisz ruchu w przód", true));
        showA = generalGroup.addSetting(new BooleanSetting("show_a", "Pokaż A", "Wyświetla klawisz ruchu w lewo", true));
        showS = generalGroup.addSetting(new BooleanSetting("show_s", "Pokaż S", "Wyświetla klawisz ruchu w tył", true));
        showD = generalGroup.addSetting(new BooleanSetting("show_d", "Pokaż D", "Wyświetla klawisz ruchu w prawo", true));
        showLmb = generalGroup.addSetting(new BooleanSetting("show_lmb", "Pokaż LMB", "Wyświetla lewy przycisk myszy", true));
        showRmb = generalGroup.addSetting(new BooleanSetting("show_rmb", "Pokaż RMB", "Wyświetla prawy przycisk myszy", true));
        showSpace = generalGroup.addSetting(new BooleanSetting("show_space", "Pokaż SPACE", "Wyświetla klawisz spacji", true));
        showShift = generalGroup.addSetting(new BooleanSetting("show_shift", "Pokaż Shift", "Wyświetla klawisz skradania (Shift)", false));
        showCtrl = generalGroup.addSetting(new BooleanSetting("show_ctrl", "Pokaż Ctrl", "Wyświetla klawisz sprintu (Ctrl)", false));
        registerGroup(generalGroup);

        // --- 2. Layout ---
        SettingGroup layoutGroup = new SettingGroup("layout", "Układ i Pozycja");
        layoutStyle = layoutGroup.addSetting(new EnumSetting<>("layout_style", "Styl układu", "Układ modułów klawiszy", LayoutStyle.STANDARD));
        horizontalGap = layoutGroup.addSetting(new NumberSetting("horizontal_gap", "Odstęp poziomy", "Odstęp między kolumnami", 2.0, 0.0, 20.0, 1.0));
        verticalGap = layoutGroup.addSetting(new NumberSetting("vertical_gap", "Odstęp pionowy", "Odstęp między wierszami", 2.0, 0.0, 20.0, 1.0));
        keyWidth = layoutGroup.addSetting(new NumberSetting("key_width", "Szerokość klawisza", "Bazowa szerokość pojedynczego klawisza", 24.0, 14.0, 50.0, 1.0));
        keyHeight = layoutGroup.addSetting(new NumberSetting("key_height", "Wysokość klawisza", "Bazowa wysokość pojedynczego klawisza", 24.0, 14.0, 50.0, 1.0));
        scale = layoutGroup.addSetting(new NumberSetting("scale", "Skala", "Skala całego modułu", 1.0, 0.5, 2.5, 0.1));
        padding = layoutGroup.addSetting(new NumberSetting("padding", "Margines wewnętrzny", "Wewnętrzny padding klawiszy", 2.0, 0.0, 10.0, 1.0));
        positionX = layoutGroup.addSetting(new NumberSetting("position_x", "Pozycja X", "Przesunięcie w poziomie od punktu kotwiczenia", 10.0, 0.0, 2000.0, 1.0));
        positionY = layoutGroup.addSetting(new NumberSetting("position_y", "Pozycja Y", "Przesunięcie w pionie od punktu kotwiczenia", 10.0, 0.0, 2000.0, 1.0));
        anchor = layoutGroup.addSetting(new EnumSetting<>("anchor", "Punkt kotwiczenia", "Róg ekranu dla pozycjonowania", HudAnchor.TOP_LEFT));
        registerGroup(layoutGroup);

        // --- 3. Colors ---
        SettingGroup colorsGroup = new SettingGroup("colors", "Kolory");
        backgroundColor = colorsGroup.addSetting(new ColorSetting("bg_color", "Kolor tła", "Tło nieaktywnego przycisku", 0x99161E2E, true));
        pressedBackgroundColor = colorsGroup.addSetting(new ColorSetting("pressed_bg_color", "Kolor wciśniętego tła", "Tło aktywnego przycisku", 0xCC10B981, true));
        textColor = colorsGroup.addSetting(new ColorSetting("text_color", "Kolor tekstu", "Kolor tekstu nieaktywnego przycisku", 0xFFFFFFFF, false));
        pressedTextColor = colorsGroup.addSetting(new ColorSetting("pressed_text_color", "Kolor wciśniętego tekstu", "Kolor tekstu aktywnego przycisku", 0xFFFFFFFF, false));
        borderColor = colorsGroup.addSetting(new ColorSetting("border_color", "Kolor obramowania", "Kolor konturu przycisku", 0x40FFFFFF, true));
        opacity = colorsGroup.addSetting(new NumberSetting("opacity", "Przezroczystość", "Globalny współczynnik krycia w %", 80.0, 0.0, 100.0, 5.0));
        registerGroup(colorsGroup);

        // --- 4. Appearance ---
        SettingGroup appearanceGroup = new SettingGroup("appearance", "Wygląd");
        showBackground = appearanceGroup.addSetting(new BooleanSetting("show_bg", "Pokaż tło", "Czy renderować tło przycisków", true));
        showBorder = appearanceGroup.addSetting(new BooleanSetting("show_border", "Pokaż obramowanie", "Czy renderować obramowanie przycisków", true));
        showShadow = appearanceGroup.addSetting(new BooleanSetting("show_shadow", "Cień tekstu", "Renderowanie cienia pod literami", true));
        borderThickness = appearanceGroup.addSetting(new NumberSetting("border_thickness", "Grubość ramki", "Szerokość linii ramki w px", 1.0, 1.0, 4.0, 1.0));
        cornerRadius = appearanceGroup.addSetting(new NumberSetting("corner_radius", "Zaokrąglenie", "Promień zaokrąglenia rogów", 0.0, 0.0, 6.0, 1.0));
        textSize = appearanceGroup.addSetting(new NumberSetting("text_size", "Rozmiar tekstu", "Skala czcionki", 1.0, 0.8, 1.5, 0.1));
        boldText = appearanceGroup.addSetting(new BooleanSetting("bold_text", "Pogrubiony tekst", "Pogrubienie napisów", false));
        registerGroup(appearanceGroup);

        // --- 5. Advanced ---
        SettingGroup advancedGroup = new SettingGroup("advanced", "Zaawansowane");
        mouseButtonStyle = advancedGroup.addSetting(new EnumSetting<>("mouse_style", "Styl przycisków myszy", "Format wyświetlania LMB/RMB", MouseStyle.CPS_AND_NAME));
        customLabels = advancedGroup.addSetting(new BooleanSetting("custom_labels", "Niestandardowe etykiety", "Krótsze oznaczenia przycisków", false));
        keySpacing = advancedGroup.addSetting(new NumberSetting("key_spacing", "Odstępy klawiszy", "Globalny mnożnik odstępów", 2.0, 0.0, 10.0, 1.0));
        animationDuration = advancedGroup.addSetting(new NumberSetting("anim_duration", "Czas animacji", "Czas trwania przejścia kolorów w ms", 50.0, 0.0, 300.0, 10.0));
        registerGroup(advancedGroup);
    }

    @Override
    public void onEnable() {
        HudElementRegistry.getInstance().register(this);
    }

    @Override
    public void onDisable() {
        HudElementRegistry.getInstance().unregister(this);
    }

    @Override
    public String getElementId() {
        return getId();
    }

    @Override
    public boolean isVisible() {
        return isEnabled();
    }

    @Override
    public void render(IHudRenderer renderer) {
        if (!isEnabled() || renderer == null) return;

        int kw = (int) (keyWidth.getValue() * scale.getValue());
        int kh = (int) (keyHeight.getValue() * scale.getValue());
        int hGap = (int) (horizontalGap.getValue() * scale.getValue());
        int vGap = (int) (verticalGap.getValue() * scale.getValue());

        // Base 3-column total width (A, S, D)
        int totalWidth = 3 * kw + 2 * hGap;

        // Calculate total height
        int rows = 0;
        if (showW.getValue()) rows++;
        if (showA.getValue() || showS.getValue() || showD.getValue()) rows++;
        if (layoutStyle.getValue() != LayoutStyle.WASD_ONLY) {
            if (showLmb.getValue() || showRmb.getValue()) rows++;
            if (showSpace.getValue()) rows++;
            if (showShift.getValue() || showCtrl.getValue()) rows++;
        }

        int spaceBarHeight = Math.max(8, kh / 2);
        int totalHeight = (rows > 0 ? (rows - 1) * vGap : 0) + (rows * kh);
        if (showSpace.getValue() && layoutStyle.getValue() != LayoutStyle.WASD_ONLY) {
            totalHeight -= (kh - spaceBarHeight);
        }

        // Bounded Anchor placement
        int startX = anchor.getValue().calculateX(positionX.getValue().intValue(), totalWidth, renderer.getScreenWidth());
        int startY = anchor.getValue().calculateY(positionY.getValue().intValue(), totalHeight, renderer.getScreenHeight());

        KeyTracker tracker = KeyTracker.getInstance();
        int currentY = startY;

        // Row 1: [W] centered
        if (showW.getValue()) {
            int wX = startX + kw + hGap;
            boolean isDown = tracker.isKeyPressed(KeyTracker.KEY_W);
            renderKeyBox(renderer, wX, currentY, kw, kh, customLabels.getValue() ? "^" : "W", isDown);
            currentY += kh + vGap;
        }

        // Row 2: [A] [S] [D]
        if (showA.getValue() || showS.getValue() || showD.getValue()) {
            if (showA.getValue()) {
                boolean isDown = tracker.isKeyPressed(KeyTracker.KEY_A);
                renderKeyBox(renderer, startX, currentY, kw, kh, customLabels.getValue() ? "<" : "A", isDown);
            }
            if (showS.getValue()) {
                boolean isDown = tracker.isKeyPressed(KeyTracker.KEY_S);
                renderKeyBox(renderer, startX + kw + hGap, currentY, kw, kh, customLabels.getValue() ? "v" : "S", isDown);
            }
            if (showD.getValue()) {
                boolean isDown = tracker.isKeyPressed(KeyTracker.KEY_D);
                renderKeyBox(renderer, startX + 2 * (kw + hGap), currentY, kw, kh, customLabels.getValue() ? ">" : "D", isDown);
            }
            currentY += kh + vGap;
        }

        // Only render mouse and space if not WASD_ONLY
        if (layoutStyle.getValue() != LayoutStyle.WASD_ONLY) {
            // Row 3: [LMB] [RMB]
            if (showLmb.getValue() || showRmb.getValue()) {
                int mouseWidth = (totalWidth - hGap) / 2;
                ClickTracker clickTracker = ClickTracker.getInstance();

                if (showLmb.getValue()) {
                    boolean isDown = tracker.isLeftMouseDown();
                    int cps = clickTracker.getCpsLmb();
                    String label = formatMouseLabel("LMB", cps);
                    renderKeyBox(renderer, startX, currentY, mouseWidth, kh, label, isDown);
                }

                if (showRmb.getValue()) {
                    boolean isDown = tracker.isRightMouseDown();
                    int cps = clickTracker.getCpsRmb();
                    String label = formatMouseLabel("RMB", cps);
                    int rmbX = startX + mouseWidth + hGap;
                    renderKeyBox(renderer, rmbX, currentY, totalWidth - mouseWidth - hGap, kh, label, isDown);
                }
                currentY += kh + vGap;
            }

            // Row 4: [SPACE]
            if (showSpace.getValue()) {
                boolean isDown = tracker.isKeyPressed(KeyTracker.KEY_SPACE);
                renderKeyBox(renderer, startX, currentY, totalWidth, spaceBarHeight, customLabels.getValue() ? "---" : "SPACE", isDown);
                currentY += spaceBarHeight + vGap;
            }

            // Row 5: [SHIFT] [CTRL]
            if (showShift.getValue() || showCtrl.getValue()) {
                if (showShift.getValue() && showCtrl.getValue()) {
                    int halfWidth = (totalWidth - hGap) / 2;
                    boolean isShiftDown = tracker.isKeyPressed(KeyTracker.KEY_LEFT_SHIFT);
                    boolean isCtrlDown = tracker.isKeyPressed(KeyTracker.KEY_LEFT_CONTROL);
                    renderKeyBox(renderer, startX, currentY, halfWidth, kh, "SHIFT", isShiftDown);
                    renderKeyBox(renderer, startX + halfWidth + hGap, currentY, totalWidth - halfWidth - hGap, kh, "CTRL", isCtrlDown);
                } else if (showShift.getValue()) {
                    boolean isDown = tracker.isKeyPressed(KeyTracker.KEY_LEFT_SHIFT);
                    renderKeyBox(renderer, startX, currentY, totalWidth, kh, "SHIFT", isDown);
                } else {
                    boolean isDown = tracker.isKeyPressed(KeyTracker.KEY_LEFT_CONTROL);
                    renderKeyBox(renderer, startX, currentY, totalWidth, kh, "CTRL", isDown);
                }
            }
        }
    }

    private String formatMouseLabel(String name, int cps) {
        switch (mouseButtonStyle.getValue()) {
            case NAME_ONLY:
                return name;
            case CPS_ONLY:
                return cps + " CPS";
            case CPS_AND_NAME:
            default:
                return name + " " + cps;
        }
    }

    private void renderKeyBox(IHudRenderer renderer, int x, int y, int w, int h, String label, boolean isPressed) {
        double globalOpacity = opacity.getValue() / 100.0;

        // Background
        if (showBackground.getValue()) {
            int baseBg = isPressed ? pressedBackgroundColor.getValue() : backgroundColor.getValue();
            int alpha = (int) (((baseBg >>> 24) & 0xFF) * globalOpacity);
            int color = (alpha << 24) | (baseBg & 0x00FFFFFF);
            renderer.fill(x, y, w, h, color);
        }

        // Border
        if (showBorder.getValue()) {
            int baseBorder = borderColor.getValue();
            int alpha = (int) (((baseBorder >>> 24) & 0xFF) * globalOpacity);
            int color = (alpha << 24) | (baseBorder & 0x00FFFFFF);
            renderer.drawBorder(x, y, w, h, color, borderThickness.getValue().intValue());
        }

        // Text
        int baseText = isPressed ? pressedTextColor.getValue() : textColor.getValue();
        float txtScale = (float) (textSize.getValue() * scale.getValue());
        int strWidth = (int) (renderer.getTextWidth(label) * txtScale);
        int fontH = (int) (renderer.getFontHeight() * txtScale);

        int textX = x + Math.max(0, (w - strWidth) / 2);
        int textY = y + Math.max(0, (h - fontH) / 2);

        renderer.drawText(label, textX, textY, baseText, showShadow.getValue(), txtScale, boldText.getValue());
    }

    // Getters for settings testing
    public BooleanSetting getShowW() { return showW; }
    public BooleanSetting getShowA() { return showA; }
    public BooleanSetting getShowS() { return showS; }
    public BooleanSetting getShowD() { return showD; }
    public BooleanSetting getShowLmb() { return showLmb; }
    public BooleanSetting getShowRmb() { return showRmb; }
    public BooleanSetting getShowSpace() { return showSpace; }
    public NumberSetting getPositionX() { return positionX; }
    public NumberSetting getPositionY() { return positionY; }
    public EnumSetting<HudAnchor> getAnchor() { return anchor; }
    public NumberSetting getScale() { return scale; }
}
