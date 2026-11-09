package pl.zombek.client.core.modules.hud.info;

import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.HudElementRegistry;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.hud.component.HudComponentModel;
import pl.zombek.client.core.hud.component.HudComponentRenderer;
import pl.zombek.client.core.hud.component.HudComponentStyle;
import pl.zombek.client.core.info.IInfoHudDataProvider;
import pl.zombek.client.core.info.InfoHudDataProviderRegistry;
import pl.zombek.client.core.info.PingHistoryTracker;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.settings.*;

import java.util.Locale;

/**
 * Universal Ping (Latency) HUD Module.
 * Displays network latency in ms with threshold-based color grading,
 * statistical tracking, and graceful Singleplayer handling (N/A).
 */
public class PingModule extends AbstractModule implements IHudElement {

    // General Group
    private final BooleanSetting showPing;
    private final BooleanSetting showAverage;
    private final BooleanSetting showMaximum;
    private final BooleanSetting showMinimum;
    private final BooleanSetting hideInSingleplayer;
    private final BooleanSetting compactLabel;

    // Thresholds Group
    private final NumberSetting goodThreshold;
    private final NumberSetting mediumThreshold;
    private final NumberSetting highThreshold;

    // Appearance Group
    private final EnumSetting<HudComponentStyle> componentStyle;
    private final ColorSetting goodColor;
    private final ColorSetting mediumColor;
    private final ColorSetting highColor;
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

    private final PingHistoryTracker tracker = new PingHistoryTracker(60);
    private final HudComponentModel model;

    public PingModule() {
        super("ping", "Ping (Latency)", "Wyświetla opóźnienie sieciowe (ping w ms) ze wskaźnikiem jakości połączenia.", ModuleCategory.HUD);

        this.model = new HudComponentModel("ping", 80, 20, HudAnchor.TOP_LEFT);

        // --- 1. General Settings ---
        SettingGroup generalGroup = new SettingGroup("general", "Ogólne");
        showPing = generalGroup.addSetting(new BooleanSetting(
                "show_ping", "Bieżący Ping", "Wyświetla aktualny ping w ms", true));
        showAverage = generalGroup.addSetting(new BooleanSetting(
                "show_average", "Średni Ping", "Wyświetla średni ping z bufora", false));
        showMaximum = generalGroup.addSetting(new BooleanSetting(
                "show_maximum", "Maksymalny Ping", "Wyświetla najwyższy odnotowany ping", false));
        showMinimum = generalGroup.addSetting(new BooleanSetting(
                "show_minimum", "Minimalny Ping", "Wyświetla najniższy odnotowany ping", false));
        hideInSingleplayer = generalGroup.addSetting(new BooleanSetting(
                "hide_singleplayer", "Ukryj w Singleplayer", "Ukrywa moduł gdy gracz gra lokalnie", false));
        compactLabel = generalGroup.addSetting(new BooleanSetting(
                "compact_label", "Format kompaktowy", "Wyświetla np. '24 ms' zamiast 'Ping: 24 ms'", true));
        registerGroup(generalGroup);

        // --- 2. Thresholds Settings ---
        SettingGroup thresholdsGroup = new SettingGroup("thresholds", "Progi jakości połączenia");
        goodThreshold = thresholdsGroup.addSetting(new NumberSetting(
                "good_threshold", "Próg Dobry (ms)", "Maksymalny ping dla koloru zielonego", 50.0, 10.0, 200.0, 5.0));
        mediumThreshold = thresholdsGroup.addSetting(new NumberSetting(
                "medium_threshold", "Próg Średni (ms)", "Maksymalny ping dla koloru żółtego", 100.0, 30.0, 300.0, 10.0));
        highThreshold = thresholdsGroup.addSetting(new NumberSetting(
                "high_threshold", "Próg Wysoki (ms)", "Maksymalny ping dla koloru pomarańczowego", 200.0, 50.0, 500.0, 10.0));
        registerGroup(thresholdsGroup);

        // --- 3. Appearance Settings ---
        SettingGroup appearanceGroup = new SettingGroup("appearance", "Wygląd");
        componentStyle = appearanceGroup.addSetting(new EnumSetting<>(
                "style", "Styl komponentu", "Styl tła i ramki",
                HudComponentStyle.CLASSIC));
        goodColor = appearanceGroup.addSetting(new ColorSetting(
                "good_color", "Kolor: Dobry", "Kolor dla niskiego pingu", 0xFF55FF55));
        mediumColor = appearanceGroup.addSetting(new ColorSetting(
                "medium_color", "Kolor: Średni", "Kolor dla umiarkowanego pingu", 0xFFFFFF55));
        highColor = appearanceGroup.addSetting(new ColorSetting(
                "high_color", "Kolor: Wysoki", "Kolor dla podwyższonego pingu", 0xFFFFAA00));
        criticalColor = appearanceGroup.addSetting(new ColorSetting(
                "critical_color", "Kolor: Krytyczny", "Kolor dla pingu krytycznego", 0xFFFF5555));
        textColor = appearanceGroup.addSetting(new ColorSetting(
                "text_color", "Kolor etykiet", "Kolor prefiksów tekstu", 0xFFA0AEC0));
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
                "pos_y", "Pozycja Y", "Przesunięcie w pionie", 80.0, 0.0, 2000.0, 1.0));
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
    }

    @Override
    public void render(IHudRenderer renderer) {
        if (renderer == null) return;
        IInfoHudDataProvider provider = InfoHudDataProviderRegistry.getInstance().getProvider();
        if (provider == null || !provider.isPlayerInGame()) return;

        boolean isSingleplayer = provider.isSingleplayer();
        if (isSingleplayer && hideInSingleplayer.getValue()) {
            return;
        }

        int rawPing = provider.getPingMs();
        tracker.recordSample(rawPing);
        pl.zombek.client.core.netgraph.NetworkMetricsCollector.getInstance().recordPing(rawPing);

        int valueCol = textColor.getValue();
        StringBuilder sb = new StringBuilder();

        if (rawPing < 0 || isSingleplayer) {
            sb.append(compactLabel.getValue() ? "Ping: N/A" : "N/A");
            valueCol = textColor.getValue();
        } else {
            PingHistoryTracker.PingStatus status = tracker.getStatus(
                    goodThreshold.getValue().intValue(),
                    mediumThreshold.getValue().intValue(),
                    highThreshold.getValue().intValue()
            );

            switch (status) {
                case GOOD -> valueCol = goodColor.getValue();
                case MEDIUM -> valueCol = mediumColor.getValue();
                case HIGH -> valueCol = highColor.getValue();
                case CRITICAL -> valueCol = criticalColor.getValue();
                default -> valueCol = textColor.getValue();
            }

            if (showPing.getValue()) {
                if (compactLabel.getValue()) {
                    sb.append(rawPing).append(" ms");
                } else {
                    sb.append("Ping: ").append(rawPing).append(" ms");
                }
            }
            if (showAverage.getValue() && tracker.getAvgPing() >= 0) {
                if (sb.length() > 0) sb.append(" | ");
                sb.append(String.format(Locale.US, "Avg: %.0f ms", tracker.getAvgPing()));
            }
            if (showMinimum.getValue() && tracker.getMinPing() >= 0) {
                if (sb.length() > 0) sb.append(" | ");
                sb.append("Min: ").append(tracker.getMinPing()).append(" ms");
            }
            if (showMaximum.getValue() && tracker.getMaxPing() >= 0) {
                if (sb.length() > 0) sb.append(" | ");
                sb.append("Max: ").append(tracker.getMaxPing()).append(" ms");
            }
        }

        String text = sb.toString();
        if (text.isEmpty()) return;

        float s = (float) (scale.getValue() * textSize.getValue());
        int padding = (int) Math.ceil(4 * scale.getValue());
        int textW = (int) Math.ceil(renderer.getTextWidth(text) * s);
        int fontH = (int) Math.ceil(renderer.getFontHeight() * s);

        int totalW = textW + padding * 2;
        int totalH = fontH + padding * 2;

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
        renderer.drawText(text, drawX + padding, drawY + padding, valueCol, textShadow.getValue(), s, false);
    }

    // Getters for settings testing
    public BooleanSetting getShowPing() { return showPing; }
    public BooleanSetting getShowAverage() { return showAverage; }
    public BooleanSetting getShowMaximum() { return showMaximum; }
    public BooleanSetting getShowMinimum() { return showMinimum; }
    public BooleanSetting getHideInSingleplayer() { return hideInSingleplayer; }
    public NumberSetting getGoodThreshold() { return goodThreshold; }
    public NumberSetting getMediumThreshold() { return mediumThreshold; }
    public NumberSetting getHighThreshold() { return highThreshold; }
    public EnumSetting<HudAnchor> getAnchor() { return anchor; }
    public NumberSetting getPositionX() { return positionX; }
    public NumberSetting getPositionY() { return positionY; }
    public PingHistoryTracker getTracker() { return tracker; }
}
