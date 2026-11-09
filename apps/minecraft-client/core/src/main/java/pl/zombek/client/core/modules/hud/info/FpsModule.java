package pl.zombek.client.core.modules.hud.info;

import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.HudElementRegistry;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.hud.component.HudComponentModel;
import pl.zombek.client.core.hud.component.HudComponentRenderer;
import pl.zombek.client.core.hud.component.HudComponentStyle;
import pl.zombek.client.core.info.FpsHistoryTracker;
import pl.zombek.client.core.info.IInfoHudDataProvider;
import pl.zombek.client.core.info.InfoHudDataProviderRegistry;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;

import java.util.Locale;

/**
 * Universal FPS Counter HUD Module.
 * Displays real-time game framerate, sliding average, minimum, maximum,
 * and an optional zero-allocation historical FPS graph.
 */
public class FpsModule extends AbstractModule implements IHudElement {

    // General Group
    private final BooleanSetting showCurrentFps;
    private final BooleanSetting showAverageFps;
    private final BooleanSetting showMinimumFps;
    private final BooleanSetting showMaximumFps;
    private final BooleanSetting compactLabel;

    // Graph Group
    private final BooleanSetting enableGraph;
    private final NumberSetting graphWidth;
    private final NumberSetting graphHeight;
    private final BooleanSetting showAverageLine;
    private final BooleanSetting showMinimumLine;

    // Appearance Group
    private final EnumSetting<HudComponentStyle> componentStyle;
    private final ColorSetting textColor;
    private final ColorSetting valueColor;
    private final ColorSetting graphColor;
    private final ColorSetting avgLineColor;
    private final ColorSetting minLineColor;
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

    private final FpsHistoryTracker tracker = new FpsHistoryTracker(100);
    private final HudComponentModel model;
    private final int[] graphSampleBuffer = new int[120];

    // Rendering optimization text cache
    private String cachedFpsText = "";
    private int lastCachedFps = -1;
    private int lastCachedAvg = -1;
    private int lastCachedMin = -1;
    private int lastCachedMax = -1;

    public FpsModule() {
        super("fps", "FPS Counter", "Wyświetla aktualną liczbę klatek na sekundę (FPS), średnią, min/max oraz wykres stabilności.", ModuleCategory.HUD);

        this.model = new HudComponentModel("fps", 90, 20, HudAnchor.TOP_LEFT);

        // --- 1. General Settings ---
        SettingGroup generalGroup = new SettingGroup("general", "Ogólne");
        showCurrentFps = generalGroup.addSetting(new BooleanSetting(
                "show_current", "Bieżący FPS", "Wyświetla aktualny licznik FPS", true));
        showAverageFps = generalGroup.addSetting(new BooleanSetting(
                "show_average", "Średni FPS", "Wyświetla średnią z bufora pomiarów", false));
        showMinimumFps = generalGroup.addSetting(new BooleanSetting(
                "show_minimum", "Minimalny FPS (1% Low)", "Wyświetla najniższą klatkę w buforze", false));
        showMaximumFps = generalGroup.addSetting(new BooleanSetting(
                "show_maximum", "Maksymalny FPS", "Wyświetla najwyższy odnotowany FPS", false));
        compactLabel = generalGroup.addSetting(new BooleanSetting(
                "compact_label", "Format kompaktowy", "Wyświetla np. '144 FPS' zamiast 'FPS: 144'", true));
        registerGroup(generalGroup);

        // --- 2. Graph Settings ---
        SettingGroup graphGroup = new SettingGroup("graph", "Wykres FPS");
        enableGraph = graphGroup.addSetting(new BooleanSetting(
                "enable_graph", "Włącz wykres", "Rysuje mini-wykres historii FPS", false));
        graphWidth = graphGroup.addSetting(new NumberSetting(
                "graph_width", "Szerokość wykresu", "Szerokość wykresu w pikselach", 80.0, 40.0, 160.0, 5.0));
        graphHeight = graphGroup.addSetting(new NumberSetting(
                "graph_height", "Wysokość wykresu", "Wysokość wykresu w pikselach", 24.0, 12.0, 60.0, 2.0));
        showAverageLine = graphGroup.addSetting(new BooleanSetting(
                "show_avg_line", "Linia średniej", "Rysuje poziomą linię średniego FPS", true));
        showMinimumLine = graphGroup.addSetting(new BooleanSetting(
                "show_min_line", "Linia minimum", "Rysuje poziomą linię minimalnego FPS", true));
        registerGroup(graphGroup);

        // --- 3. Appearance Settings ---
        SettingGroup appearanceGroup = new SettingGroup("appearance", "Wygląd");
        componentStyle = appearanceGroup.addSetting(new EnumSetting<>(
                "style", "Styl komponentu", "Styl tła i ramki",
                HudComponentStyle.CLASSIC));
        textColor = appearanceGroup.addSetting(new ColorSetting(
                "text_color", "Kolor etykiet", "Kolor prefiksów tekstu", 0xFFA0AEC0));
        valueColor = appearanceGroup.addSetting(new ColorSetting(
                "value_color", "Kolor wartości", "Kolor cyfr FPS", 0xFF55FF55));
        graphColor = appearanceGroup.addSetting(new ColorSetting(
                "graph_color", "Kolor wykresu", "Kolor słupków / linii wykresu", 0xFF3B82F6));
        avgLineColor = appearanceGroup.addSetting(new ColorSetting(
                "avg_line_color", "Kolor linii średniej", "Kolor wskaźnika średniego FPS", 0xFFFBBF24));
        minLineColor = appearanceGroup.addSetting(new ColorSetting(
                "min_line_color", "Kolor linii minimum", "Kolor wskaźnika minimalnego FPS", 0xFFEF4444));
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

        // --- 4. Position Group ---
        SettingGroup positionGroup = new SettingGroup("position", "Pozycja");
        anchor = positionGroup.addSetting(new EnumSetting<>(
                "anchor", "Kotwica", "Narożnik ekranu",
                HudAnchor.TOP_LEFT));
        positionX = positionGroup.addSetting(new NumberSetting(
                "pos_x", "Pozycja X", "Przesunięcie w poziomie", 4.0, 0.0, 2000.0, 1.0));
        positionY = positionGroup.addSetting(new NumberSetting(
                "pos_y", "Pozycja Y", "Przesunięcie w pionie", 56.0, 0.0, 2000.0, 1.0));
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

    public void resetStatistics() {
        tracker.reset();
        lastCachedFps = -1;
        cachedFpsText = "";
    }

    @Override
    public void render(IHudRenderer renderer) {
        if (renderer == null) return;
        IInfoHudDataProvider provider = InfoHudDataProviderRegistry.getInstance().getProvider();
        if (provider == null || !provider.isPlayerInGame()) return;

        int currentFps = provider.getCurrentFps();
        tracker.recordSample(currentFps);

        int avgFpsInt = (int) Math.round(tracker.getAvgFps());
        int minFps = tracker.getMinFps();
        int maxFps = tracker.getMaxFps();

        if (currentFps != lastCachedFps || avgFpsInt != lastCachedAvg || minFps != lastCachedMin || maxFps != lastCachedMax) {
            lastCachedFps = currentFps;
            lastCachedAvg = avgFpsInt;
            lastCachedMin = minFps;
            lastCachedMax = maxFps;

            StringBuilder sb = new StringBuilder(32);
            if (showCurrentFps.getValue()) {
                if (compactLabel.getValue()) {
                    sb.append(currentFps).append(" FPS");
                } else {
                    sb.append("FPS: ").append(currentFps);
                }
            }
            if (showAverageFps.getValue()) {
                if (sb.length() > 0) sb.append(" | ");
                sb.append("Avg: ").append(avgFpsInt);
            }
            if (showMinimumFps.getValue()) {
                if (sb.length() > 0) sb.append(" | ");
                sb.append("Min: ").append(minFps);
            }
            if (showMaximumFps.getValue()) {
                if (sb.length() > 0) sb.append(" | ");
                sb.append("Max: ").append(maxFps);
            }
            cachedFpsText = sb.toString();
        }

        String text = cachedFpsText;
        if (text.isEmpty() && !enableGraph.getValue()) return;

        float s = (float) (scale.getValue() * textSize.getValue());
        int padding = (int) Math.ceil(4 * scale.getValue());
        int textW = text.isEmpty() ? 0 : (int) Math.ceil(renderer.getTextWidth(text) * s);
        int fontH = text.isEmpty() ? 0 : (int) Math.ceil(renderer.getFontHeight() * s);

        boolean drawGraph = enableGraph.getValue();
        int gW = drawGraph ? (int) Math.ceil(graphWidth.getValue() * scale.getValue()) : 0;
        int gH = drawGraph ? (int) Math.ceil(graphHeight.getValue() * scale.getValue()) : 0;

        int totalW = Math.max(textW, gW) + padding * 2;
        int totalH = fontH + (drawGraph ? (gH + padding) : 0) + padding * 2;

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
        if (!text.isEmpty()) {
            renderer.drawText(text, drawX + padding, currentY, valueColor.getValue(), textShadow.getValue(), s, false);
            currentY += fontH + padding;
        }

        // Render Graph
        if (drawGraph && gW > 0 && gH > 0) {
            renderFpsGraph(renderer, drawX + padding, currentY, gW, gH);
        }
    }

    private void renderFpsGraph(IHudRenderer renderer, int gx, int gy, int gw, int gh) {
        // Graph Box outline
        renderer.fill(gx, gy, gw, gh, 0x50000000);
        renderer.drawBorder(gx, gy, gw, gh, 0x30FFFFFF, 1);

        int count = tracker.getChronologicalSamples(graphSampleBuffer);
        if (count < 2) return;

        int maxInPlot = Math.max(120, tracker.getMaxFps());
        int col = graphColor.getValue();

        for (int i = 0; i < count; i++) {
            int fpsVal = graphSampleBuffer[i];
            int colX = gx + (int) ((double) i / (double) count * (double) gw);
            int barHeight = Math.min(gh - 2, (int) (((double) fpsVal / (double) maxInPlot) * (gh - 2)));
            if (barHeight > 0) {
                renderer.fill(colX, gy + gh - barHeight - 1, Math.max(1, gw / count), barHeight, col);
            }
        }

        // Average Line
        if (showAverageLine.getValue()) {
            double avg = tracker.getAvgFps();
            int avgY = gy + gh - 1 - (int) ((avg / (double) maxInPlot) * (gh - 2));
            if (avgY >= gy && avgY <= gy + gh) {
                renderer.fill(gx, avgY, gw, 1, avgLineColor.getValue());
            }
        }

        // Minimum Line
        if (showMinimumLine.getValue()) {
            int min = tracker.getMinFps();
            int minY = gy + gh - 1 - (int) (((double) min / (double) maxInPlot) * (gh - 2));
            if (minY >= gy && minY <= gy + gh) {
                renderer.fill(gx, minY, gw, 1, minLineColor.getValue());
            }
        }
    }

    // Getters for settings testing
    public BooleanSetting getShowCurrentFps() { return showCurrentFps; }
    public BooleanSetting getShowAverageFps() { return showAverageFps; }
    public BooleanSetting getShowMinimumFps() { return showMinimumFps; }
    public BooleanSetting getShowMaximumFps() { return showMaximumFps; }
    public BooleanSetting getEnableGraph() { return enableGraph; }
    public EnumSetting<HudAnchor> getAnchor() { return anchor; }
    public NumberSetting getPositionX() { return positionX; }
    public NumberSetting getPositionY() { return positionY; }
    public FpsHistoryTracker getTracker() { return tracker; }
}
