package pl.zombek.client.core.modules.hud.netgraph;

import pl.zombek.client.core.hud.HudAnchor;
import pl.zombek.client.core.hud.HudElementRegistry;
import pl.zombek.client.core.hud.IHudElement;
import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.hud.component.HudComponentModel;
import pl.zombek.client.core.hud.component.HudPriorityResolver;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleCategory;
import pl.zombek.client.core.netgraph.*;
import pl.zombek.client.core.settings.*;

/**
 * Universal graphical Net Graph HUD module.
 * Visualizes ping latency, jitter, bandwidth throughput, packet transmission rates,
 * and connection quality diagnostics.
 */
public class NetGraphModule extends AbstractModule implements IHudElement {

    public static final String ID = "net_graph";

    // --- 1. General Settings ---
    private final EnumSetting<NetGraphMode> mode;
    private final BooleanSetting showConnectionStatus;
    private final BooleanSetting showConnectionQuality;
    private final BooleanSetting showCurrentValues;
    private final BooleanSetting showLegend;
    private final BooleanSetting autoScale;

    // --- 2. Latency Settings ---
    private final BooleanSetting enableLatency;
    private final BooleanSetting showPing;
    private final BooleanSetting showAveragePing;
    private final BooleanSetting showJitter;
    private final BooleanSetting showMinMaxPing;
    private final NumberSetting latencyHistoryDuration;
    private final BooleanSetting smoothing;
    private final NumberSetting decimalPrecision;

    // --- 3. Traffic Settings ---
    private final BooleanSetting enableTraffic;
    private final BooleanSetting showIncomingTraffic;
    private final BooleanSetting showOutgoingTraffic;
    private final BooleanSetting showTotalReceived;
    private final BooleanSetting showTotalSent;
    private final EnumSetting<TrafficUnit> trafficUnit;
    private final NumberSetting trafficUpdateInterval;

    // --- 4. Packets Settings ---
    private final BooleanSetting enablePackets;
    private final BooleanSetting showIncomingPps;
    private final BooleanSetting showOutgoingPps;
    private final BooleanSetting showMaxPps;
    private final NumberSetting packetsHistoryDuration;

    // --- 5. Graph Settings ---
    private final EnumSetting<NetGraphType> graphType;
    private final NumberSetting graphWidth;
    private final NumberSetting graphHeight;
    private final NumberSetting graphScale;
    private final BooleanSetting showGrid;
    private final BooleanSetting showAxisLabels;
    private final NumberSetting lineThickness;
    private final NumberSetting fillOpacity;

    // --- 6. Connection Settings ---
    private final BooleanSetting showConnectionType;
    private final BooleanSetting showStatusBadge;
    private final BooleanSetting showQualityBadge;
    private final BooleanSetting showSessionDuration;

    // --- 7. Statistics Settings ---
    private final BooleanSetting showSessionStatistics;
    private final BooleanSetting autoResetOnReconnect;

    // --- 8. Thresholds Settings ---
    private final NumberSetting excellentPingThreshold;
    private final NumberSetting goodPingThreshold;
    private final NumberSetting moderatePingThreshold;
    private final NumberSetting poorPingThreshold;
    private final NumberSetting jitterWarningThreshold;
    private final NumberSetting trafficWarningThreshold;

    // --- 9. Appearance Settings ---
    private final ColorSetting backgroundColor;
    private final NumberSetting backgroundOpacity;
    private final BooleanSetting showBorder;
    private final ColorSetting borderColor;
    private final NumberSetting borderThickness;
    private final NumberSetting textSize;
    private final BooleanSetting textShadow;
    private final NumberSetting padding;
    private final NumberSetting spacing;

    // --- 10. Position Settings ---
    private final EnumSetting<HudAnchor> anchor;
    private final NumberSetting positionX;
    private final NumberSetting positionY;

    // --- 11. Colors Settings ---
    private final ColorSetting pingColor;
    private final ColorSetting incomingColor;
    private final ColorSetting outgoingColor;
    private final ColorSetting incomingPpsColor;
    private final ColorSetting outgoingPpsColor;
    private final ColorSetting gridColor;
    private final ColorSetting textColor;

    private final HudComponentModel model;

    public NetGraphModule() {
        super(ID, "Net Graph", "Graficzny system monitorowania jakości, opóźnienia i aktywności połączenia sieciowego.", ModuleCategory.HUD);

        this.model = new HudComponentModel(ID, 200, 90, HudAnchor.TOP_LEFT);
        this.model.setRenderPriority(HudPriorityResolver.PRIORITY_NET_GRAPH);

        // 1. General
        SettingGroup generalGroup = new SettingGroup("general", "Ogólne");
        mode = generalGroup.addSetting(new EnumSetting<>(
                "mode", "Tryb wykresu", "Wybór prezentowanych metryk", NetGraphMode.COMBINED));
        showConnectionStatus = generalGroup.addSetting(new BooleanSetting(
                "show_status", "Status połączenia", "Wyświetla stan połączenia w nagłówku", true));
        showConnectionQuality = generalGroup.addSetting(new BooleanSetting(
                "show_quality", "Jakość połączenia", "Wyświetla odznakę jakości (Excellent, Good, etc.)", true));
        showCurrentValues = generalGroup.addSetting(new BooleanSetting(
                "show_current_values", "Bieżące wartości", "Wyświetla aktualne liczby na wykresie", true));
        showLegend = generalGroup.addSetting(new BooleanSetting(
                "show_legend", "Legenda / Pasek metryk", "Rysuje legendę i live badge pod wykresem", true));
        autoScale = generalGroup.addSetting(new BooleanSetting(
                "auto_scale", "Automatyczna skala", "Dopasowuje skalę osi pionowej do wartości szczytowych", true));
        registerGroup(generalGroup);

        // 2. Latency
        SettingGroup latencyGroup = new SettingGroup("latency", "Opóźnienie (Ping / Latency)");
        enableLatency = latencyGroup.addSetting(new BooleanSetting(
                "enable_latency", "Włącz śledzenie opóźnienia", "Rejestruje ping i jitter", true));
        showPing = latencyGroup.addSetting(new BooleanSetting(
                "show_ping", "Bieżący Ping", "Pokazuje aktualny ping w ms", true));
        showAveragePing = latencyGroup.addSetting(new BooleanSetting(
                "show_avg_ping", "Średni Ping", "Rysuje poziomą linię średniego pingu", true));
        showJitter = latencyGroup.addSetting(new BooleanSetting(
                "show_jitter", "Pokaż Jitter", "Wyświetla wartość jittera (zgodnie z RFC 3550)", true));
        showMinMaxPing = latencyGroup.addSetting(new BooleanSetting(
                "show_min_max", "Min/Maks Ping", "Pokazuje wartości skrajne w oknie historii", false));
        latencyHistoryDuration = latencyGroup.addSetting(new NumberSetting(
                "history_duration", "Czas historii (s)", "Szerokość okna czasowego próbek w sekundach", 30.0, 5.0, 60.0, 5.0));
        smoothing = latencyGroup.addSetting(new BooleanSetting(
                "smoothing", "Wygładzanie wizualne", "Płynne tłumienie skoków skali osi Y", true));
        decimalPrecision = latencyGroup.addSetting(new NumberSetting(
                "decimal_precision", "Miejsca po przecinku", "Precyzja wyświetlania jittera", 1.0, 0.0, 3.0, 1.0));
        registerGroup(latencyGroup);

        // 3. Traffic
        SettingGroup trafficGroup = new SettingGroup("traffic", "Przepustowość (Traffic)");
        enableTraffic = trafficGroup.addSetting(new BooleanSetting(
                "enable_traffic", "Włącz śledzenie transferu", "Rejestruje ruch przychodzący i wychodzący", true));
        showIncomingTraffic = trafficGroup.addSetting(new BooleanSetting(
                "show_incoming", "Ruch przychodzący (RX)", "Wyświetla pobierane dane (B/s, KB/s)", true));
        showOutgoingTraffic = trafficGroup.addSetting(new BooleanSetting(
                "show_outgoing", "Ruch wychodzący (TX)", "Wyświetla wysyłane dane (B/s, KB/s)", true));
        showTotalReceived = trafficGroup.addSetting(new BooleanSetting(
                "show_total_rcv", "Łącznie odebrane", "Wyświetla całkowity transfer odebrany w sesji", false));
        showTotalSent = trafficGroup.addSetting(new BooleanSetting(
                "show_total_sent", "Łącznie wysłane", "Wyświetla całkowity transfer wysłany w sesji", false));
        trafficUnit = trafficGroup.addSetting(new EnumSetting<>(
                "traffic_unit", "Jednostka transferu", "Jednostka dla przepustowości", TrafficUnit.AUTO));
        trafficUpdateInterval = trafficGroup.addSetting(new NumberSetting(
                "traffic_interval", "Interwał aktualizacji (s)", "Częstotliwość agregacji danych transferu", 1.0, 0.5, 5.0, 0.5));
        registerGroup(trafficGroup);

        // 4. Packets
        SettingGroup packetsGroup = new SettingGroup("packets", "Częstotliwość pakietów (Packets)");
        enablePackets = packetsGroup.addSetting(new BooleanSetting(
                "enable_packets", "Włącz śledzenie pakietów", "Rejestruje częstotliwość transmisji PPS", true));
        showIncomingPps = packetsGroup.addSetting(new BooleanSetting(
                "show_incoming_pps", "Pakiety wejściowe (RX PPS)", "Częstotliwość odbieranych pakietów", true));
        showOutgoingPps = packetsGroup.addSetting(new BooleanSetting(
                "show_outgoing_pps", "Pakiety wyjściowe (TX PPS)", "Częstotliwość nadawanych pakietów", true));
        showMaxPps = packetsGroup.addSetting(new BooleanSetting(
                "show_max_pps", "Maksymalne PPS", "Pokazuje najwyższe odnotowane PPS", false));
        packetsHistoryDuration = packetsGroup.addSetting(new NumberSetting(
                "packet_duration", "Czas historii PPS (s)", "Okres buforowania pakietów w sekundach", 30.0, 5.0, 60.0, 5.0));
        registerGroup(packetsGroup);

        // 5. Graph
        SettingGroup graphGroup = new SettingGroup("graph", "Wykres");
        graphType = graphGroup.addSetting(new EnumSetting<>(
                "graph_type", "Typ wykresu", "Styl rysowania serii (Line, Bar, Area)", NetGraphType.LINE));
        graphWidth = graphGroup.addSetting(new NumberSetting(
                "graph_width", "Szerokość wykresu", "Szerokość panelu w pikselach", 200.0, 100.0, 400.0, 10.0));
        graphHeight = graphGroup.addSetting(new NumberSetting(
                "graph_height", "Wysokość wykresu", "Wysokość panelu w pikselach", 90.0, 50.0, 200.0, 5.0));
        graphScale = graphGroup.addSetting(new NumberSetting(
                "graph_scale", "Skala elementu", "Mnożnik skalowania HUD", 1.0, 0.5, 2.5, 0.1));
        showGrid = graphGroup.addSetting(new BooleanSetting(
                "show_grid", "Siatka referencyjna", "Rysuje subtelne linie poziome i ramkę wykresu", true));
        showAxisLabels = graphGroup.addSetting(new BooleanSetting(
                "show_axis_labels", "Etykiety osi", "Wyświetla wartości progowe na osi pionowej", true));
        lineThickness = graphGroup.addSetting(new NumberSetting(
                "line_thickness", "Grubość linii", "Grubość rysowanych krzywych", 1.0, 1.0, 3.0, 1.0));
        fillOpacity = graphGroup.addSetting(new NumberSetting(
                "fill_opacity", "Przezroczystość wypełnienia (%)", "Stopień krycia dla wykresu warstwowego (Area)", 35.0, 0.0, 100.0, 5.0));
        registerGroup(graphGroup);

        // 6. Connection
        SettingGroup connGroup = new SettingGroup("connection", "Status połączenia");
        showConnectionType = connGroup.addSetting(new BooleanSetting(
                "show_conn_type", "Typ połączenia", "Pokazuje np. Singleplayer / Multiplayer", true));
        showStatusBadge = connGroup.addSetting(new BooleanSetting(
                "show_status_badge", "Plakietka statusu", "Pokazuje [Connected], [Connecting], itp.", true));
        showQualityBadge = connGroup.addSetting(new BooleanSetting(
                "show_quality_badge", "Plakietka jakości", "Pokazuje [Excellent], [Good], [Poor]", true));
        showSessionDuration = connGroup.addSetting(new BooleanSetting(
                "show_session_duration", "Czas trwania sesji", "Pokazuje czas od nawiązania połączenia", true));
        registerGroup(connGroup);

        // 7. Statistics
        SettingGroup statsGroup = new SettingGroup("statistics", "Statystyki sesji");
        showSessionStatistics = statsGroup.addSetting(new BooleanSetting(
                "show_session_stats", "Włącz statystyki sesji", "Gromadzi dane szczytowe i średnie", true));
        autoResetOnReconnect = statsGroup.addSetting(new BooleanSetting(
                "auto_reset_reconnect", "Resetuj przy reconnect", "Automatycznie czyści statystyki przy ponownym łączeniu", true));
        registerGroup(statsGroup);

        // 8. Thresholds
        SettingGroup thresholdsGroup = new SettingGroup("thresholds", "Progi diagnostyczne");
        excellentPingThreshold = thresholdsGroup.addSetting(new NumberSetting(
                "excellent_ping", "Próg Znakomity (ms)", "Maksymalny ping dla jakości Excellent", 35.0, 10.0, 100.0, 5.0));
        goodPingThreshold = thresholdsGroup.addSetting(new NumberSetting(
                "good_ping", "Próg Dobry (ms)", "Maksymalny ping dla jakości Good", 75.0, 20.0, 150.0, 5.0));
        moderatePingThreshold = thresholdsGroup.addSetting(new NumberSetting(
                "moderate_ping", "Próg Umiarkowany (ms)", "Maksymalny ping dla jakości Moderate", 150.0, 50.0, 300.0, 10.0));
        poorPingThreshold = thresholdsGroup.addSetting(new NumberSetting(
                "poor_ping", "Próg Słaby (ms)", "Próg powyżej którego łącze klasyfikowane jest jako Poor", 250.0, 100.0, 500.0, 25.0));
        jitterWarningThreshold = thresholdsGroup.addSetting(new NumberSetting(
                "jitter_warning", "Ostrzeżenie Jitter (ms)", "Maksymalny dopuszczalny jitter stabilnego łącza", 15.0, 2.0, 50.0, 1.0));
        trafficWarningThreshold = thresholdsGroup.addSetting(new NumberSetting(
                "traffic_warning", "Ostrzeżenie Transfer (KB/s)", "Próg wysokiego obciążenia łącza", 500.0, 50.0, 2000.0, 50.0));
        registerGroup(thresholdsGroup);

        // 9. Appearance
        SettingGroup appearanceGroup = new SettingGroup("appearance", "Wygląd panelu");
        backgroundColor = appearanceGroup.addSetting(new ColorSetting(
                "bg_color", "Kolor tła", "Bazowy kolor panelu", 0xFF111827));
        backgroundOpacity = appearanceGroup.addSetting(new NumberSetting(
                "bg_opacity", "Przezroczystość tła (%)", "Krycie tła wykresu", 75.0, 0.0, 100.0, 5.0));
        showBorder = appearanceGroup.addSetting(new BooleanSetting(
                "show_border", "Pokaż obramowanie", "Rysuje zewnętrzną ramkę panelu", true));
        borderColor = appearanceGroup.addSetting(new ColorSetting(
                "border_color", "Kolor ramki", "Kolor krawędzi", 0x40FFFFFF));
        borderThickness = appearanceGroup.addSetting(new NumberSetting(
                "border_thickness", "Grubość ramki", "Szerokość linii ramki w pikselach", 1.0, 1.0, 4.0, 1.0));
        textSize = appearanceGroup.addSetting(new NumberSetting(
                "text_size", "Rozmiar tekstu", "Mnożnik czcionki etykiet", 1.0, 0.5, 2.0, 0.1));
        textShadow = appearanceGroup.addSetting(new BooleanSetting(
                "text_shadow", "Cień tekstu", "Rysuje cień pod napisami", true));
        padding = appearanceGroup.addSetting(new NumberSetting(
                "padding", "Margines wewnętrzny", "Odstęp wykresu od krawędzi panelu", 4.0, 1.0, 12.0, 1.0));
        spacing = appearanceGroup.addSetting(new NumberSetting(
                "spacing", "Odstępy elementów", "Odstęp między nagłówkiem a wykresem", 2.0, 0.0, 8.0, 1.0));
        registerGroup(appearanceGroup);

        // 10. Position
        SettingGroup positionGroup = new SettingGroup("position", "Pozycja");
        anchor = positionGroup.addSetting(new EnumSetting<>(
                "anchor", "Kotwica", "Narożnik ekranu dla pozycjonowania", HudAnchor.TOP_LEFT));
        positionX = positionGroup.addSetting(new NumberSetting(
                "pos_x", "Pozycja X", "Przesunięcie w poziomie", 4.0, 0.0, 2000.0, 1.0));
        positionY = positionGroup.addSetting(new NumberSetting(
                "pos_y", "Pozycja Y", "Przesunięcie w pionie", 105.0, 0.0, 2000.0, 1.0));
        registerGroup(positionGroup);

        // 11. Colors
        SettingGroup colorsGroup = new SettingGroup("colors", "Kolory serii");
        pingColor = colorsGroup.addSetting(new ColorSetting(
                "ping_color", "Kolor: Ping", "Kolor linii/słupków opóźnienia", 0xFF55FF55));
        incomingColor = colorsGroup.addSetting(new ColorSetting(
                "incoming_color", "Kolor: RX Transfer", "Kolor linii pobierania danych", 0xFF55FFFF));
        outgoingColor = colorsGroup.addSetting(new ColorSetting(
                "outgoing_color", "Kolor: TX Transfer", "Kolor linii wysyłania danych", 0xFFFFAA00));
        incomingPpsColor = colorsGroup.addSetting(new ColorSetting(
                "incoming_pps_color", "Kolor: RX PPS", "Kolor linii pakietów wejściowych", 0xFF55AAFF));
        outgoingPpsColor = colorsGroup.addSetting(new ColorSetting(
                "outgoing_pps_color", "Kolor: TX PPS", "Kolor linii pakietów wyjściowych", 0xFFFF55AA));
        gridColor = colorsGroup.addSetting(new ColorSetting(
                "grid_color", "Kolor siatki", "Kolor pomocniczych linii siatki", 0x25FFFFFF));
        textColor = colorsGroup.addSetting(new ColorSetting(
                "text_color", "Kolor tekstu", "Kolor etykiet i napisów", 0xFFE2E8F0));
        registerGroup(colorsGroup);
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
        if (renderer == null || !isVisible()) return;

        model.setWidth((int) (graphWidth.getValue() * graphScale.getValue()));
        model.setHeight((int) (graphHeight.getValue() * graphScale.getValue()));
        model.setPosX(positionX.getValue().intValue());
        model.setPosY(positionY.getValue().intValue());
        model.setAnchor(anchor.getValue());

        int posX = model.getScreenX(renderer.getScreenWidth());
        int posY = model.getScreenY(renderer.getScreenHeight());

        NetworkGraphRenderer.getInstance().render(renderer, this, posX, posY, false);
    }

    public void renderPreview(IHudRenderer renderer, int x, int y) {
        if (renderer == null) return;
        NetworkGraphRenderer.getInstance().render(renderer, this, x, y, true);
    }

    public void resetStatistics() {
        NetworkMetricsCollector.getInstance().resetSession();
    }

    public void resetToDefaults() {
        mode.setValue(NetGraphMode.COMBINED);
        showConnectionStatus.setValue(true);
        showConnectionQuality.setValue(true);
        showCurrentValues.setValue(true);
        showLegend.setValue(true);
        autoScale.setValue(true);

        enableLatency.setValue(true);
        showPing.setValue(true);
        showAveragePing.setValue(true);
        showJitter.setValue(true);
        showMinMaxPing.setValue(false);
        latencyHistoryDuration.setValue(30.0);
        smoothing.setValue(true);
        decimalPrecision.setValue(1.0);

        enableTraffic.setValue(true);
        showIncomingTraffic.setValue(true);
        showOutgoingTraffic.setValue(true);
        showTotalReceived.setValue(false);
        showTotalSent.setValue(false);
        trafficUnit.setValue(TrafficUnit.AUTO);
        trafficUpdateInterval.setValue(1.0);

        enablePackets.setValue(true);
        showIncomingPps.setValue(true);
        showOutgoingPps.setValue(true);
        showMaxPps.setValue(false);
        packetsHistoryDuration.setValue(30.0);

        graphType.setValue(NetGraphType.LINE);
        graphWidth.setValue(200.0);
        graphHeight.setValue(90.0);
        graphScale.setValue(1.0);
        showGrid.setValue(true);
        showAxisLabels.setValue(true);
        lineThickness.setValue(1.0);
        fillOpacity.setValue(35.0);

        showConnectionType.setValue(true);
        showStatusBadge.setValue(true);
        showQualityBadge.setValue(true);
        showSessionDuration.setValue(true);

        showSessionStatistics.setValue(true);
        autoResetOnReconnect.setValue(true);

        excellentPingThreshold.setValue(35.0);
        goodPingThreshold.setValue(75.0);
        moderatePingThreshold.setValue(150.0);
        poorPingThreshold.setValue(250.0);
        jitterWarningThreshold.setValue(15.0);
        trafficWarningThreshold.setValue(500.0);

        backgroundColor.setValue(0xFF111827);
        backgroundOpacity.setValue(75.0);
        showBorder.setValue(true);
        borderColor.setValue(0x40FFFFFF);
        borderThickness.setValue(1.0);
        textSize.setValue(1.0);
        textShadow.setValue(true);
        padding.setValue(4.0);
        spacing.setValue(2.0);

        anchor.setValue(HudAnchor.TOP_LEFT);
        positionX.setValue(4.0);
        positionY.setValue(105.0);

        pingColor.setValue(0xFF55FF55);
        incomingColor.setValue(0xFF55FFFF);
        outgoingColor.setValue(0xFFFFAA00);
        incomingPpsColor.setValue(0xFF55AAFF);
        outgoingPpsColor.setValue(0xFFFF55AA);
        gridColor.setValue(0x25FFFFFF);
        textColor.setValue(0xFFE2E8F0);
    }

    // Getters for settings
    public EnumSetting<NetGraphMode> getMode() { return mode; }
    public BooleanSetting getShowConnectionStatus() { return showConnectionStatus; }
    public BooleanSetting getShowConnectionQuality() { return showConnectionQuality; }
    public BooleanSetting getShowCurrentValues() { return showCurrentValues; }
    public BooleanSetting getShowLegend() { return showLegend; }
    public BooleanSetting getAutoScale() { return autoScale; }

    public BooleanSetting getEnableLatency() { return enableLatency; }
    public BooleanSetting getShowPing() { return showPing; }
    public BooleanSetting getShowAveragePing() { return showAveragePing; }
    public BooleanSetting getShowJitter() { return showJitter; }
    public BooleanSetting getShowMinMaxPing() { return showMinMaxPing; }
    public NumberSetting getLatencyHistoryDuration() { return latencyHistoryDuration; }
    public BooleanSetting getSmoothing() { return smoothing; }
    public NumberSetting getDecimalPrecision() { return decimalPrecision; }

    public BooleanSetting getEnableTraffic() { return enableTraffic; }
    public BooleanSetting getShowIncomingTraffic() { return showIncomingTraffic; }
    public BooleanSetting getShowOutgoingTraffic() { return showOutgoingTraffic; }
    public BooleanSetting getShowTotalReceived() { return showTotalReceived; }
    public BooleanSetting getShowTotalSent() { return showTotalSent; }
    public EnumSetting<TrafficUnit> getTrafficUnit() { return trafficUnit; }
    public NumberSetting getTrafficUpdateInterval() { return trafficUpdateInterval; }

    public BooleanSetting getEnablePackets() { return enablePackets; }
    public BooleanSetting getShowIncomingPps() { return showIncomingPps; }
    public BooleanSetting getShowOutgoingPps() { return showOutgoingPps; }
    public BooleanSetting getShowMaxPps() { return showMaxPps; }
    public NumberSetting getPacketsHistoryDuration() { return packetsHistoryDuration; }

    public EnumSetting<NetGraphType> getGraphType() { return graphType; }
    public NumberSetting getGraphWidth() { return graphWidth; }
    public NumberSetting getGraphHeight() { return graphHeight; }
    public NumberSetting getGraphScale() { return graphScale; }
    public BooleanSetting getShowGrid() { return showGrid; }
    public BooleanSetting getShowAxisLabels() { return showAxisLabels; }
    public NumberSetting getLineThickness() { return lineThickness; }
    public NumberSetting getFillOpacity() { return fillOpacity; }

    public BooleanSetting getShowConnectionType() { return showConnectionType; }
    public BooleanSetting getShowStatusBadge() { return showStatusBadge; }
    public BooleanSetting getShowQualityBadge() { return showQualityBadge; }
    public BooleanSetting getShowSessionDuration() { return showSessionDuration; }

    public BooleanSetting getShowSessionStatistics() { return showSessionStatistics; }
    public BooleanSetting getAutoResetOnReconnect() { return autoResetOnReconnect; }

    public NumberSetting getExcellentPingThreshold() { return excellentPingThreshold; }
    public NumberSetting getGoodPingThreshold() { return goodPingThreshold; }
    public NumberSetting getModeratePingThreshold() { return moderatePingThreshold; }
    public NumberSetting getPoorPingThreshold() { return poorPingThreshold; }
    public NumberSetting getJitterWarningThreshold() { return jitterWarningThreshold; }
    public NumberSetting getTrafficWarningThreshold() { return trafficWarningThreshold; }

    public ColorSetting getBackgroundColor() { return backgroundColor; }
    public NumberSetting getBackgroundOpacity() { return backgroundOpacity; }
    public BooleanSetting getShowBorder() { return showBorder; }
    public ColorSetting getBorderColor() { return borderColor; }
    public NumberSetting getBorderThickness() { return borderThickness; }
    public NumberSetting getTextSize() { return textSize; }
    public BooleanSetting getTextShadow() { return textShadow; }
    public NumberSetting getPadding() { return padding; }
    public NumberSetting getSpacing() { return spacing; }

    public EnumSetting<HudAnchor> getAnchor() { return anchor; }
    public NumberSetting getPositionX() { return positionX; }
    public NumberSetting getPositionY() { return positionY; }

    public ColorSetting getPingColor() { return pingColor; }
    public ColorSetting getIncomingColor() { return incomingColor; }
    public ColorSetting getOutgoingColor() { return outgoingColor; }
    public ColorSetting getIncomingPpsColor() { return incomingPpsColor; }
    public ColorSetting getOutgoingPpsColor() { return outgoingPpsColor; }
    public ColorSetting getGridColor() { return gridColor; }
    public ColorSetting getTextColor() { return textColor; }
}
