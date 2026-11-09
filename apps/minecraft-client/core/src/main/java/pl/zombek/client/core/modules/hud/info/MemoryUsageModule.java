package pl.zombek.client.core.modules.hud.info;

import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.HudElementRegistry;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.hud.component.HudComponentModel;
import pl.zombek.client.core.hud.component.HudComponentRenderer;
import pl.zombek.client.core.hud.component.HudComponentStyle;
import pl.zombek.client.core.info.MemoryUnit;
import pl.zombek.client.core.info.MemoryUsageTracker;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;

import java.util.Locale;

/**
 * Universal Memory Usage HUD Module.
 * Displays JVM RAM utilization (used, allocated, maximum, percentage),
 * an animated progress bar, and customizable warning/critical thresholds.
 * Explicitly never calls System.gc().
 */
public class MemoryUsageModule extends AbstractModule implements IHudElement {

    // General Group
    private final BooleanSetting showUsedMemory;
    private final BooleanSetting showAllocatedMemory;
    private final BooleanSetting showMaximumMemory;
    private final BooleanSetting showUsagePercentage;
    private final EnumSetting<MemoryUnit> unitFormat;

    // Memory Bar Group
    private final BooleanSetting enableMemoryBar;
    private final NumberSetting barWidth;
    private final NumberSetting barHeight;

    // Thresholds Group
    private final NumberSetting warningThreshold;
    private final NumberSetting criticalThreshold;

    // Appearance Group
    private final EnumSetting<HudComponentStyle> componentStyle;
    private final ColorSetting normalColor;
    private final ColorSetting warningColor;
    private final ColorSetting criticalColor;
    private final ColorSetting textColor;
    private final ColorSetting backgroundColor;
    private final NumberSetting backgroundOpacity;
    private final BooleanSetting showBorder;
    private final ColorSetting borderColor;
    private final NumberSetting borderThickness;
    private final NumberSetting scale;
    private final NumberSetting textSize;
    private final BooleanSetting textShadow;

    // Position Group
    private final EnumSetting<HudAnchor> anchor;
    private final NumberSetting positionX;
    private final NumberSetting positionY;

    private final MemoryUsageTracker tracker = new MemoryUsageTracker();
    private final HudComponentModel model;

    public MemoryUsageModule() {
        super("memory_usage", "Memory Usage (RAM)", "Wyświetla wykorzystanie pamięci RAM przez proces JVM Minecrafta oraz pasek obciążenia.", ModuleCategory.HUD);

        this.model = new HudComponentModel("memory_usage", 110, 26, HudAnchor.TOP_LEFT);

        // --- 1. General Settings ---
        SettingGroup generalGroup = new SettingGroup("general", "Ogólne");
        showUsedMemory = generalGroup.addSetting(new BooleanSetting(
                "show_used", "Zajęta pamięć", "Wyświetla aktualnie zajętą pamięć RAM", true));
        showAllocatedMemory = generalGroup.addSetting(new BooleanSetting(
                "show_allocated", "Przydzielona pamięć", "Wyświetla pamięć zarezerwowaną przez JVM", false));
        showMaximumMemory = generalGroup.addSetting(new BooleanSetting(
                "show_max", "Maksymalna pamięć", "Wyświetla limit pamięci (-Xmx)", true));
        showUsagePercentage = generalGroup.addSetting(new BooleanSetting(
                "show_percentage", "Procent użycia", "Wyświetla stopień zajęcia w %", true));
        unitFormat = generalGroup.addSetting(new EnumSetting<>(
                "unit", "Jednostka pamięci", "Format jednostki pamięci",
                MemoryUnit.MB));
        registerGroup(generalGroup);

        // --- 2. Memory Bar Settings ---
        SettingGroup barGroup = new SettingGroup("bar", "Pasek Pamięci");
        enableMemoryBar = barGroup.addSetting(new BooleanSetting(
                "enable_bar", "Pasek zużycia", "Rysuje graficzny pasek zużycia RAM", true));
        barWidth = barGroup.addSetting(new NumberSetting(
                "bar_width", "Szerokość paska", "Szerokość paska w pikselach", 100.0, 40.0, 250.0, 5.0));
        barHeight = barGroup.addSetting(new NumberSetting(
                "bar_height", "Wysokość paska", "Wysokość paska w pikselach", 5.0, 2.0, 16.0, 1.0));
        registerGroup(barGroup);

        // --- 3. Thresholds Settings ---
        SettingGroup thresholdsGroup = new SettingGroup("thresholds", "Progi Ostrzegawcze");
        warningThreshold = thresholdsGroup.addSetting(new NumberSetting(
                "warning_threshold", "Próg ostrzegawczy (%)", "Procent pamięci dla koloru żółtego", 75.0, 40.0, 90.0, 5.0));
        criticalThreshold = thresholdsGroup.addSetting(new NumberSetting(
                "critical_threshold", "Próg krytyczny (%)", "Procent pamięci dla koloru czerwonego", 90.0, 60.0, 98.0, 2.0));
        registerGroup(thresholdsGroup);

        // --- 4. Appearance Settings ---
        SettingGroup appearanceGroup = new SettingGroup("appearance", "Wygląd");
        componentStyle = appearanceGroup.addSetting(new EnumSetting<>(
                "style", "Styl komponentu", "Styl tła i ramki",
                HudComponentStyle.CLASSIC));
        normalColor = appearanceGroup.addSetting(new ColorSetting(
                "normal_color", "Kolor: Normalny", "Kolor przy standardowym zużyciu", 0xFF10B981));
        warningColor = appearanceGroup.addSetting(new ColorSetting(
                "warning_color", "Kolor: Ostrzeżenie", "Kolor przy przekroczeniu progu ostrzegawczego", 0xFFF59E0B));
        criticalColor = appearanceGroup.addSetting(new ColorSetting(
                "critical_color", "Kolor: Krytyczny", "Kolor przy wysokim obciążeniu", 0xFFEF4444));
        textColor = appearanceGroup.addSetting(new ColorSetting(
                "text_color", "Kolor tekstu", "Kolor napisów informacyjnych", 0xFFFFFFFF));
        backgroundColor = appearanceGroup.addSetting(new ColorSetting(
                "bg_color", "Kolor tła", "Własny kolor tła modułu", 0xFF111827));
        backgroundOpacity = appearanceGroup.addSetting(new NumberSetting(
                "opacity", "Przezroczystość tła", "Przezroczystość tła w procentach", 65.0, 0.0, 100.0, 5.0));
        showBorder = appearanceGroup.addSetting(new BooleanSetting(
                "show_border", "Pokaż ramkę", "Rysuje obramowanie", true));
        borderColor = appearanceGroup.addSetting(new ColorSetting(
                "border_color", "Kolor ramki", "Kolor obramowania", 0x40FFFFFF));
        borderThickness = appearanceGroup.addSetting(new NumberSetting(
                "border_thickness", "Grubość ramki", "Grubość obramowania w pikselach", 1.0, 1.0, 4.0, 1.0));
        scale = appearanceGroup.addSetting(new NumberSetting(
                "scale", "Skala modułu", "Ogólne skalowanie elementu", 1.0, 0.5, 2.5, 0.1));
        textSize = appearanceGroup.addSetting(new NumberSetting(
                "text_size", "Rozmiar tekstu", "Mnożnik czcionki", 1.0, 0.5, 2.0, 0.1));
        textShadow = appearanceGroup.addSetting(new BooleanSetting(
                "text_shadow", "Cień tekstu", "Rysuje cień pod napisami", true));
        registerGroup(appearanceGroup);

        // --- 5. Position Group ---
        SettingGroup positionGroup = new SettingGroup("position", "Pozycja");
        anchor = positionGroup.addSetting(new EnumSetting<>(
                "anchor", "Kotwica", "Narożnik ekranu",
                HudAnchor.TOP_LEFT));
        positionX = positionGroup.addSetting(new NumberSetting(
                "pos_x", "Pozycja X", "Przesunięcie w poziomie", 4.0, 0.0, 2000.0, 1.0));
        positionY = positionGroup.addSetting(new NumberSetting(
                "pos_y", "Pozycja Y", "Przesunięcie w pionie", 104.0, 0.0, 2000.0, 1.0));
        registerGroup(positionGroup);
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
        if (renderer == null) return;
        tracker.update();

        MemoryUnit unit = unitFormat.getValue();
        double usedPercent = tracker.getUsagePercent();

        MemoryUsageTracker.MemoryStatus status = tracker.getStatus(
                warningThreshold.getValue(),
                criticalThreshold.getValue()
        );

        int statusCol = switch (status) {
            case CRITICAL -> criticalColor.getValue();
            case WARNING -> warningColor.getValue();
            case NORMAL -> normalColor.getValue();
        };

        // Format Text
        StringBuilder sb = new StringBuilder("RAM: ");
        if (showUsedMemory.getValue()) {
            sb.append(unit.format(tracker.getUsedBytes()));
        }
        if (showAllocatedMemory.getValue()) {
            sb.append(" (Alloc: ").append(unit.format(tracker.getAllocatedBytes())).append(")");
        }
        if (showMaximumMemory.getValue()) {
            sb.append(" / ").append(unit.format(tracker.getMaxBytes()));
        }
        if (showUsagePercentage.getValue()) {
            sb.append(String.format(Locale.US, " (%.0f%%)", usedPercent));
        }

        String text = sb.toString();

        float s = (float) (scale.getValue() * textSize.getValue());
        int padding = (int) Math.ceil(4 * scale.getValue());
        int textW = (int) Math.ceil(renderer.getTextWidth(text) * s);
        int fontH = (int) Math.ceil(renderer.getFontHeight() * s);

        boolean drawBar = enableMemoryBar.getValue();
        int bW = drawBar ? (int) Math.ceil(barWidth.getValue() * scale.getValue()) : 0;
        int bH = drawBar ? (int) Math.ceil(barHeight.getValue() * scale.getValue()) : 0;

        int totalW = Math.max(textW, bW) + padding * 2;
        int totalH = fontH + (drawBar ? (bH + padding) : 0) + padding * 2;

        model.setWidth(totalW);
        model.setHeight(totalH);
        model.setScale(1.0);
        model.setAnchor(anchor.getValue());
        model.setPosX(positionX.getValue().intValue());
        model.setPosY(positionY.getValue().intValue());

        int screenW = renderer.getScreenWidth();
        int screenH = renderer.getScreenHeight();
        model.clampToScreen(screenW, screenH);

        int drawX = model.getScreenX(screenW);
        int drawY = model.getScreenY(screenH);

        // Render Background
        HudComponentRenderer.renderBackground(
                renderer, drawX, drawY, totalW, totalH,
                componentStyle.getValue(),
                backgroundColor.getValue(),
                backgroundOpacity.getValue(),
                borderColor.getValue(),
                showBorder.getValue(),
                borderThickness.getValue().intValue()
        );

        // Render Text
        int currentY = drawY + padding;
        renderer.drawText(text, drawX + padding, currentY, statusCol, textShadow.getValue(), s, false);

        // Render Memory Bar
        if (drawBar && bW > 0 && bH > 0) {
            int barY = currentY + fontH + padding;
            int barX = drawX + padding;

            // Bar background
            renderer.fill(barX, barY, bW, bH, 0x50000000);
            renderer.drawBorder(barX, barY, bW, bH, 0x30FFFFFF, 1);

            // Allocated marker
            long maxB = tracker.getMaxBytes();
            if (maxB > 0) {
                int allocWidth = (int) Math.min(bW, ((double) tracker.getAllocatedBytes() / (double) maxB) * bW);
                if (allocWidth > 0) {
                    renderer.fill(barX, barY, allocWidth, bH, 0x30FFFFFF);
                }
            }

            // Used progress fill
            int fillWidth = (int) Math.min(bW, (usedPercent / 100.0) * bW);
            if (fillWidth > 0) {
                renderer.fill(barX, barY, fillWidth, bH, statusCol);
            }
        }
    }

    // Getters for settings testing
    public BooleanSetting getShowUsedMemory() { return showUsedMemory; }
    public BooleanSetting getShowAllocatedMemory() { return showAllocatedMemory; }
    public BooleanSetting getShowMaximumMemory() { return showMaximumMemory; }
    public BooleanSetting getShowUsagePercentage() { return showUsagePercentage; }
    public EnumSetting<MemoryUnit> getUnitFormat() { return unitFormat; }
    public BooleanSetting getEnableMemoryBar() { return enableMemoryBar; }
    public NumberSetting getWarningThreshold() { return warningThreshold; }
    public NumberSetting getCriticalThreshold() { return criticalThreshold; }
    public EnumSetting<HudAnchor> getAnchor() { return anchor; }
    public NumberSetting getPositionX() { return positionX; }
    public NumberSetting getPositionY() { return positionY; }
    public MemoryUsageTracker getTracker() { return tracker; }
}
