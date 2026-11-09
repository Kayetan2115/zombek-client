package pl.zombek.client.core.modules.pvp;

import pl.zombek.client.core.data.CooldownDataProviderRegistry;
import pl.zombek.client.core.data.CooldownItemData;
import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.HudElementRegistry;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Cooldowns HUD Module.
 * Visualizes active item cooldowns (Ender Pearl, Chorus Fruit, Shield, etc.)
 * with item icons, names, animated progress bars, remaining times, and configurable layouts.
 */
public class CooldownsModule extends AbstractModule implements IHudElement {

    public enum DisplayStyle {
        DETAILED("Szczegółowy (Ikona, Nazwa, Pasek, Czas)"),
        COMPACT("Kompaktowy (Ikona i Pasek)"),
        BAR_ONLY("Tylko Pasek i Czas");

        private final String displayName;
        DisplayStyle(String displayName) { this.displayName = displayName; }
        @Override public String toString() { return displayName; }
    }

    public enum Orientation {
        VERTICAL("Pionowy"),
        HORIZONTAL("Poziomy");

        private final String displayName;
        Orientation(String displayName) { this.displayName = displayName; }
        @Override public String toString() { return displayName; }
    }

    // General Group
    private final BooleanSetting showItemIcon;
    private final BooleanSetting showItemName;
    private final BooleanSetting showRemainingTime;
    private final BooleanSetting showProgressBar;
    private final BooleanSetting showPercentage;

    // Display Group
    private final EnumSetting<DisplayStyle> displayStyle;
    private final NumberSetting iconSize;
    private final NumberSetting textSize;
    private final NumberSetting scale;
    private final NumberSetting spacing;

    // Behavior Group
    private final BooleanSetting hideCompleted;
    private final BooleanSetting sortByRemaining;
    private final BooleanSetting showOnlyActive;

    // Colors Group
    private final ColorSetting textColor;
    private final ColorSetting progressColor;
    private final ColorSetting progressBgColor;
    private final ColorSetting backgroundColor;
    private final ColorSetting borderColor;
    private final NumberSetting opacity;

    // Layout Group
    private final EnumSetting<Orientation> orientation;
    private final NumberSetting positionX;
    private final NumberSetting positionY;
    private final EnumSetting<HudAnchor> anchor;
    private final NumberSetting padding;
    private final BooleanSetting showBackground;
    private final BooleanSetting showBorder;
    private final NumberSetting borderThickness;
    private final BooleanSetting showShadow;

    public CooldownsModule() {
        super("cooldowns", "Cooldowns", "Wyświetla czasy odnowienia przedmiotów (perły, tarcza, owoce itp.) na ekranie gry.", ModuleCategory.PVP);

        // --- 1. General ---
        SettingGroup generalGroup = new SettingGroup("general", "Ogólne");
        showItemIcon = generalGroup.addSetting(new BooleanSetting("show_icon", "Pokaż ikonę", "Renderowanie ikony przedmiotu", true));
        showItemName = generalGroup.addSetting(new BooleanSetting("show_name", "Pokaż nazwę", "Wyświetlanie nazwy przedmiotu", true));
        showRemainingTime = generalGroup.addSetting(new BooleanSetting("show_time", "Pokaż czas", "Wyświetlanie pozostałych sekund", true));
        showProgressBar = generalGroup.addSetting(new BooleanSetting("show_bar", "Pokaż pasek", "Wyświetlanie paska postępu", true));
        showPercentage = generalGroup.addSetting(new BooleanSetting("show_percentage", "Pokaż procent", "Wyświetlanie wartości procentowej", false));
        registerGroup(generalGroup);

        // --- 2. Display ---
        SettingGroup displayGroup = new SettingGroup("display", "Prezentacja");
        displayStyle = displayGroup.addSetting(new EnumSetting<>("display_style", "Styl", "Format wizualny wpisów", DisplayStyle.DETAILED));
        iconSize = displayGroup.addSetting(new NumberSetting("icon_size", "Rozmiar ikony", "Wymiary ikony w px", 16.0, 12.0, 32.0, 1.0));
        textSize = displayGroup.addSetting(new NumberSetting("text_size", "Skala czcionki", "Rozmiar czcionki", 1.0, 0.8, 1.5, 0.1));
        scale = displayGroup.addSetting(new NumberSetting("scale", "Skala", "Globalne powiększenie modułu", 1.0, 0.5, 2.5, 0.1));
        spacing = displayGroup.addSetting(new NumberSetting("spacing", "Odstęp", "Odstęp między elementami listy", 4.0, 0.0, 20.0, 1.0));
        registerGroup(displayGroup);

        // --- 3. Behavior ---
        SettingGroup behaviorGroup = new SettingGroup("behavior", "Zachowanie");
        hideCompleted = behaviorGroup.addSetting(new BooleanSetting("hide_completed", "Ukryj zakończone", "Ukrywa przedmioty, których cooldown minął", true));
        sortByRemaining = behaviorGroup.addSetting(new BooleanSetting("sort_remaining", "Sortuj wg czasu", "Sortowanie wpisów od najkrótszego czasu", true));
        showOnlyActive = behaviorGroup.addSetting(new BooleanSetting("show_only_active", "Tylko aktywne", "Wyświetla wyłącznie aktywne czasy odnowienia", true));
        registerGroup(behaviorGroup);

        // --- 4. Colors ---
        SettingGroup colorsGroup = new SettingGroup("colors", "Kolory");
        textColor = colorsGroup.addSetting(new ColorSetting("text_color", "Kolor tekstu", "Kolor etykiet i czasu", 0xFFFFFFFF, false));
        progressColor = colorsGroup.addSetting(new ColorSetting("progress_color", "Kolor paska", "Kolor wypełnienia paska postępu", 0xFFEF4444, false));
        progressBgColor = colorsGroup.addSetting(new ColorSetting("progress_bg", "Tło paska", "Kolor tła pod paskiem postępu", 0x66000000, true));
        backgroundColor = colorsGroup.addSetting(new ColorSetting("bg_color", "Kolor tła", "Tło modułu", 0x80000000, true));
        borderColor = colorsGroup.addSetting(new ColorSetting("border_color", "Kolor obramowania", "Obramowanie modułu", 0x40FFFFFF, true));
        opacity = colorsGroup.addSetting(new NumberSetting("opacity", "Przezroczystość", "Krycie tła w %", 80.0, 0.0, 100.0, 5.0));
        registerGroup(colorsGroup);

        // --- 5. Layout ---
        SettingGroup layoutGroup = new SettingGroup("layout", "Układ i Pozycja");
        orientation = layoutGroup.addSetting(new EnumSetting<>("orientation", "Orientacja", "Pionowy lub poziomy układ", Orientation.VERTICAL));
        positionX = layoutGroup.addSetting(new NumberSetting("position_x", "Pozycja X", "Przesunięcie w poziomie od punktu kotwiczenia", 10.0, 0.0, 2000.0, 1.0));
        positionY = layoutGroup.addSetting(new NumberSetting("position_y", "Pozycja Y", "Przesunięcie w pionie od punktu kotwiczenia", 260.0, 0.0, 2000.0, 1.0));
        anchor = layoutGroup.addSetting(new EnumSetting<>("anchor", "Punkt kotwiczenia", "Róg ekranu dla pozycjonowania", HudAnchor.TOP_LEFT));
        padding = layoutGroup.addSetting(new NumberSetting("padding", "Margines wewnętrzny", "Wewnętrzny padding ramki", 4.0, 0.0, 20.0, 1.0));
        showBackground = layoutGroup.addSetting(new BooleanSetting("show_bg", "Pokaż tło", "Renderowanie tła prostokąta", true));
        showBorder = layoutGroup.addSetting(new BooleanSetting("show_border", "Pokaż obramowanie", "Renderowanie konturu ramki", true));
        borderThickness = layoutGroup.addSetting(new NumberSetting("border_thickness", "Grubość ramki", "Szerokość linii ramki w px", 1.0, 1.0, 4.0, 1.0));
        showShadow = layoutGroup.addSetting(new BooleanSetting("show_shadow", "Cień tekstu", "Renderowanie cienia pod literami", true));
        registerGroup(layoutGroup);
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
        return isEnabled() && !hasError();
    }

    @Override
    public void render(IHudRenderer renderer) {
        if (!isVisible() || renderer == null) return;

        List<CooldownItemData> activeList = getFilteredAndSortedCooldowns();
        if (activeList.isEmpty()) return;

        int pad = padding.getValue().intValue();
        int space = spacing.getValue().intValue();
        int iconPx = iconSize.getValue().intValue();
        float s = scale.getValue().floatValue();
        float textScale = textSize.getValue().floatValue() * s;
        boolean isHorizontal = orientation.getValue() == Orientation.HORIZONTAL;
        DisplayStyle style = displayStyle.getValue();

        int barWidth = (int) (50 * s);
        int barHeight = (int) (4 * s);

        // Calculate entry slot width and height
        int entryWidth = 0;
        if (style != DisplayStyle.BAR_ONLY && showItemIcon.getValue()) {
            entryWidth += iconPx + 4;
        }

        int maxTextWidth = 0;
        for (CooldownItemData cd : activeList) {
            String label = buildCooldownLabel(cd);
            int tw = (int) (renderer.getTextWidth(label) * textScale);
            if (tw > maxTextWidth) maxTextWidth = tw;
        }

        if (style == DisplayStyle.DETAILED) {
            entryWidth += maxTextWidth + (showProgressBar.getValue() ? (barWidth + 6) : 0);
        } else if (style == DisplayStyle.COMPACT) {
            entryWidth += (showProgressBar.getValue() ? barWidth : maxTextWidth);
        } else { // BAR_ONLY
            entryWidth += (showProgressBar.getValue() ? barWidth : 0) + 4 + maxTextWidth;
        }

        int entryHeight = Math.max(iconPx, renderer.getFontHeight() + 2);

        int totalWidth;
        int totalHeight;

        if (isHorizontal) {
            totalWidth = pad * 2 + (activeList.size() * entryWidth) + (Math.max(0, activeList.size() - 1) * space);
            totalHeight = pad * 2 + entryHeight;
        } else {
            totalWidth = pad * 2 + entryWidth;
            totalHeight = pad * 2 + (activeList.size() * entryHeight) + (Math.max(0, activeList.size() - 1) * space);
        }

        int baseX = anchor.getValue().calculateX(positionX.getValue().intValue(), totalWidth, renderer.getScreenWidth());
        int baseY = anchor.getValue().calculateY(positionY.getValue().intValue(), totalHeight, renderer.getScreenHeight());

        // Background & Border
        if (showBackground.getValue()) {
            int bg = applyOpacity(backgroundColor.getValue(), opacity.getValue().floatValue() / 100.0f);
            renderer.fill(baseX, baseY, totalWidth, totalHeight, bg);
        }
        if (showBorder.getValue()) {
            renderer.drawBorder(baseX, baseY, totalWidth, totalHeight, borderColor.getValue(), borderThickness.getValue().intValue());
        }

        // Render each cooldown entry
        int curX = baseX + pad;
        int curY = baseY + pad;

        for (CooldownItemData cd : activeList) {
            int entryX = curX;

            // 1. Draw Item Icon
            if (style != DisplayStyle.BAR_ONLY && showItemIcon.getValue()) {
                renderer.renderItem(cd.getItemHandle(), entryX, curY, s);
                entryX += iconPx + 4;
            }

            // 2. Draw Progress Bar
            if (showProgressBar.getValue()) {
                int barY = curY + (entryHeight - barHeight) / 2;
                // Background bar
                renderer.fill(entryX, barY, barWidth, barHeight, progressBgColor.getValue());
                // Filled progress
                int fillW = Math.max(1, (int) (barWidth * cd.getProgress()));
                renderer.fill(entryX, barY, fillW, barHeight, progressColor.getValue());
                entryX += barWidth + 4;
            }

            // 3. Draw Text / Time Label
            String label = buildCooldownLabel(cd);
            if (!label.isEmpty()) {
                int textY = curY + (entryHeight - renderer.getFontHeight()) / 2;
                renderer.drawText(label, entryX, textY, textColor.getValue(), showShadow.getValue(), textScale, false);
            }

            if (isHorizontal) {
                curX += entryWidth + space;
            } else {
                curY += entryHeight + space;
            }
        }
    }

    private List<CooldownItemData> getFilteredAndSortedCooldowns() {
        List<CooldownItemData> list = CooldownDataProviderRegistry.getProvider().getActiveCooldowns();
        if (list == null || list.isEmpty()) return Collections.emptyList();

        List<CooldownItemData> filtered = new ArrayList<>();
        for (CooldownItemData cd : list) {
            if (cd == null) continue;
            if (showOnlyActive.getValue() && cd.getProgress() <= 0.0f) continue;
            if (hideCompleted.getValue() && cd.getProgress() <= 0.0f) continue;
            filtered.add(cd);
        }

        if (sortByRemaining.getValue()) {
            filtered.sort(Comparator.comparingDouble(CooldownItemData::getRemainingSeconds));
        }

        return filtered;
    }

    private String buildCooldownLabel(CooldownItemData cd) {
        StringBuilder sb = new StringBuilder();

        if (showItemName.getValue() && !cd.getDisplayName().isEmpty() && displayStyle.getValue() == DisplayStyle.DETAILED) {
            sb.append(cd.getDisplayName()).append(" ");
        }

        if (showRemainingTime.getValue()) {
            sb.append(String.format("%.1fs", cd.getRemainingSeconds()));
        }

        if (showPercentage.getValue()) {
            if (sb.length() > 0) sb.append(" (");
            sb.append(Math.round(cd.getProgressPercentage())).append("%");
            if (showRemainingTime.getValue()) sb.append(")");
        }

        return sb.toString().trim();
    }

    private int applyOpacity(int color, float alphaFactor) {
        int alpha = (color >> 24) & 0xFF;
        int newAlpha = Math.round(alpha * alphaFactor);
        return (newAlpha << 24) | (color & 0x00FFFFFF);
    }

    // Getters for settings
    public BooleanSetting getShowItemIcon() { return showItemIcon; }
    public BooleanSetting getShowItemName() { return showItemName; }
    public BooleanSetting getShowRemainingTime() { return showRemainingTime; }
    public BooleanSetting getShowProgressBar() { return showProgressBar; }
    public EnumSetting<DisplayStyle> getDisplayStyle() { return displayStyle; }
    public EnumSetting<Orientation> getOrientation() { return orientation; }
}
