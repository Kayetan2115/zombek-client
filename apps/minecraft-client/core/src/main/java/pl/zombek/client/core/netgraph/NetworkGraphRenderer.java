package pl.zombek.client.core.netgraph;

import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.modules.hud.netgraph.NetGraphModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * High-performance HUD renderer for the graphical Net Graph system.
 * Renders latency curves, traffic throughput, packet rates, subtle grid reference lines,
 * connection quality badges, and legends.
 */
public class NetworkGraphRenderer {

    private static final NetworkGraphRenderer INSTANCE = new NetworkGraphRenderer();

    // Damping factor for smooth auto-scale transitions
    private float smoothedMaxPing = 100.0f;
    private float smoothedMaxTraffic = 10240.0f; // 10 KB/s
    private float smoothedMaxPps = 30.0f;

    public static NetworkGraphRenderer getInstance() {
        return INSTANCE;
    }

    public void render(
            IHudRenderer renderer,
            NetGraphModule module,
            int posX,
            int posY,
            boolean isPreview
    ) {
        if (renderer == null || module == null) return;

        int width = module.getGraphWidth().getValue().intValue();
        int height = module.getGraphHeight().getValue().intValue();
        float scale = module.getGraphScale().getValue().floatValue();
        int padding = module.getPadding().getValue().intValue();

        int scaledWidth = (int) (width * scale);
        int scaledHeight = (int) (height * scale);

        // 1. Draw Background & Border
        int rawBg = module.getBackgroundColor().getValue();
        int opacityPct = module.getBackgroundOpacity().getValue().intValue();
        int alpha = Math.min(255, Math.max(0, (int) (opacityPct * 2.55f)));
        int bgColor = (alpha << 24) | (rawBg & 0x00FFFFFF);

        renderer.fill(posX, posY, scaledWidth, scaledHeight, bgColor);

        if (module.getShowBorder().getValue()) {
            int borderColor = module.getBorderColor().getValue();
            int borderThick = module.getBorderThickness().getValue().intValue();
            renderer.drawBorder(posX, posY, scaledWidth, scaledHeight, borderColor, borderThick);
        }

        // 2. Fetch Samples
        NetworkMetricsCollector collector = NetworkMetricsCollector.getInstance();
        long durationMs = module.getLatencyHistoryDuration().getValue().longValue() * 1000L;
        if (durationMs <= 0) durationMs = 30000L;

        List<NetworkMetricSample> samples;
        ConnectionStatus status;
        ConnectionQuality quality;
        boolean isSingleplayer;

        if (isPreview) {
            samples = generatePreviewSamples(durationMs);
            status = ConnectionStatus.CONNECTED;
            quality = ConnectionQuality.EXCELLENT;
            isSingleplayer = false;
        } else {
            samples = collector.getHistoryBuffer().getSamplesWithin(durationMs);
            status = collector.getStatusTracker().getCurrentStatus();
            quality = collector.getStatusTracker().getCurrentQuality();
            isSingleplayer = status == ConnectionStatus.SINGLEPLAYER;

            INetworkDataProvider provider = NetworkDataProviderRegistry.getInstance().getProvider();
            if (provider != null) {
                if (provider.isSingleplayer()) {
                    isSingleplayer = true;
                    status = ConnectionStatus.SINGLEPLAYER;
                    quality = ConnectionQuality.UNAVAILABLE;
                } else if (status == ConnectionStatus.UNKNOWN && provider.getConnectionStatus() != null) {
                    status = provider.getConnectionStatus();
                }
            }
        }

        int headerHeight = 16;
        int footerHeight = module.getShowLegend().getValue() ? 12 : 0;
        int plotX = posX + padding;
        int plotY = posY + headerHeight;
        int plotW = scaledWidth - (padding * 2);
        int plotH = scaledHeight - headerHeight - footerHeight - padding;

        // 3. Render Header (Title, Connection Status & Quality Badges)
        renderHeader(renderer, module, posX + padding, posY + 3, scaledWidth - (padding * 2), status, quality, isSingleplayer);

        // 4. Handle Singleplayer Mode Display
        if (isSingleplayer && !isPreview) {
            renderSingleplayerView(renderer, module, plotX, plotY, plotW, plotH);
            return;
        }

        // 5. Render Grid and Axis
        if (module.getShowGrid().getValue() && plotW > 20 && plotH > 20) {
            renderGrid(renderer, module, plotX, plotY, plotW, plotH);
        }

        if (samples.isEmpty()) {
            renderer.drawText("Awaiting network samples...", plotX + 10, plotY + (plotH / 2) - 4, 0xFFAAAAAA, true, 0.85f, false);
            return;
        }

        // 6. Render Data Series according to selected NetGraphMode
        NetGraphMode mode = module.getMode().getValue();
        NetGraphType graphType = module.getGraphType().getValue();

        switch (mode) {
            case LATENCY -> renderLatencySeries(renderer, module, samples, plotX, plotY, plotW, plotH, graphType);
            case TRAFFIC -> renderTrafficSeries(renderer, module, samples, plotX, plotY, plotW, plotH, graphType);
            case PACKETS -> renderPacketsSeries(renderer, module, samples, plotX, plotY, plotW, plotH, graphType);
            case COMBINED -> renderCombinedSeries(renderer, module, samples, plotX, plotY, plotW, plotH);
            case CUSTOM -> renderCustomSeries(renderer, module, samples, plotX, plotY, plotW, plotH, graphType);
        }

        // 7. Render Footer Legend / Live Values
        if (module.getShowLegend().getValue()) {
            renderFooterLegend(renderer, module, samples, posX + padding, posY + scaledHeight - footerHeight, scaledWidth - (padding * 2));
        }
    }

    private void renderHeader(
            IHudRenderer renderer,
            NetGraphModule module,
            int x,
            int y,
            int width,
            ConnectionStatus status,
            ConnectionQuality quality,
            boolean isSingleplayer
    ) {
        String title = "Net Graph";
        if (isSingleplayer) {
            title = "Net Graph [Local]";
        }
        renderer.drawText(title, x, y, module.getTextColor().getValue(), true, 0.9f, true);

        int rightX = x + width;

        if (module.getShowConnectionQuality().getValue() && quality != ConnectionQuality.UNAVAILABLE) {
            String qText = "[" + quality.getLabel() + "]";
            int qWidth = (int) (renderer.getTextWidth(qText) * 0.8f);
            rightX -= (qWidth + 4);
            renderer.drawText(qText, rightX, y, quality.getDefaultColor(), true, 0.8f, false);
        }

        if (module.getShowConnectionStatus().getValue()) {
            String sText = status.getLabel();
            int sWidth = (int) (renderer.getTextWidth(sText) * 0.8f);
            rightX -= (sWidth + 4);
            renderer.drawText(sText, rightX, y, status.getDefaultColor(), true, 0.8f, true);
        }
    }

    private void renderSingleplayerView(
            IHudRenderer renderer,
            NetGraphModule module,
            int plotX,
            int plotY,
            int plotW,
            int plotH
    ) {
        // Subtle outline of plot area
        renderer.fill(plotX, plotY, plotW, plotH, 0x15FFFFFF);
        int midY = plotY + (plotH / 2);

        renderer.drawText("Singleplayer — Integrated Server", plotX + 10, midY - 10, 0xFF55FFFF, true, 0.85f, true);
        renderer.drawText("Internet Latency: N/A (Local loopback)", plotX + 10, midY + 2, 0xFFAAAAAA, true, 0.75f, false);
    }

    private void renderGrid(
            IHudRenderer renderer,
            NetGraphModule module,
            int x,
            int y,
            int w,
            int h
    ) {
        int gridCol = module.getGridColor().getValue();

        // 3 horizontal guideline levels (25%, 50%, 75%)
        for (int i = 1; i <= 3; i++) {
            int lineY = y + (h * i / 4);
            renderer.fill(x, lineY, w, 1, gridCol);
        }

        // Border around plot box
        renderer.drawBorder(x, y, w, h, gridCol, 1);
    }

    private void renderLatencySeries(
            IHudRenderer renderer,
            NetGraphModule module,
            List<NetworkMetricSample> samples,
            int x,
            int y,
            int w,
            int h,
            NetGraphType type
    ) {
        float maxPing = 50.0f;
        if (module.getAutoScale().getValue()) {
            for (NetworkMetricSample s : samples) {
                if (s.getPingMs() > maxPing) maxPing = s.getPingMs();
            }
            maxPing *= 1.25f; // headroom
            smoothedMaxPing += (maxPing - smoothedMaxPing) * 0.2f;
        } else {
            smoothedMaxPing = 200.0f;
        }

        int lineColor = module.getPingColor().getValue();
        int areaColor = (0x35 << 24) | (lineColor & 0x00FFFFFF);
        int lineThick = module.getLineThickness().getValue().intValue();

        plotNumericSeries(renderer, samples, x, y, w, h, smoothedMaxPing, lineColor, areaColor, lineThick, type,
                NetworkMetricSample::getPingMs);

        // Average line marker
        double avg = calculateAverage(samples, NetworkMetricSample::getPingMs);
        if (avg > 0) {
            int avgY = y + h - (int) ((avg / Math.max(1.0f, smoothedMaxPing)) * h);
            renderer.fill(x, avgY, w, 1, 0x80FFFF55);
            renderer.drawText(String.format(Locale.ROOT, "avg: %.0fms", avg), x + 2, avgY - 7, 0xCCFFFF55, true, 0.7f, false);
        }
    }

    private void renderTrafficSeries(
            IHudRenderer renderer,
            NetGraphModule module,
            List<NetworkMetricSample> samples,
            int x,
            int y,
            int w,
            int h,
            NetGraphType type
    ) {
        float maxBps = 1024.0f;
        if (module.getAutoScale().getValue()) {
            for (NetworkMetricSample s : samples) {
                if (s.getIncomingBps() > maxBps) maxBps = s.getIncomingBps();
                if (s.getOutgoingBps() > maxBps) maxBps = s.getOutgoingBps();
            }
            maxBps *= 1.25f;
            smoothedMaxTraffic += (maxBps - smoothedMaxTraffic) * 0.2f;
        } else {
            smoothedMaxTraffic = 102400.0f; // 100 KB/s
        }

        int inColor = module.getIncomingColor().getValue();
        int inArea = (0x30 << 24) | (inColor & 0x00FFFFFF);
        int outColor = module.getOutgoingColor().getValue();
        int outArea = (0x30 << 24) | (outColor & 0x00FFFFFF);
        int thick = module.getLineThickness().getValue().intValue();

        plotNumericSeries(renderer, samples, x, y, w, h, smoothedMaxTraffic, inColor, inArea, thick, type,
                s -> (int) s.getIncomingBps());
        plotNumericSeries(renderer, samples, x, y, w, h, smoothedMaxTraffic, outColor, outArea, thick, NetGraphType.LINE,
                s -> (int) s.getOutgoingBps());
    }

    private void renderPacketsSeries(
            IHudRenderer renderer,
            NetGraphModule module,
            List<NetworkMetricSample> samples,
            int x,
            int y,
            int w,
            int h,
            NetGraphType type
    ) {
        float maxPps = 20.0f;
        if (module.getAutoScale().getValue()) {
            for (NetworkMetricSample s : samples) {
                if (s.getIncomingPps() > maxPps) maxPps = s.getIncomingPps();
                if (s.getOutgoingPps() > maxPps) maxPps = s.getOutgoingPps();
            }
            maxPps *= 1.25f;
            smoothedMaxPps += (maxPps - smoothedMaxPps) * 0.2f;
        } else {
            smoothedMaxPps = 60.0f;
        }

        int inColor = module.getIncomingPpsColor().getValue();
        int inArea = (0x30 << 24) | (inColor & 0x00FFFFFF);
        int outColor = module.getOutgoingPpsColor().getValue();
        int thick = module.getLineThickness().getValue().intValue();

        plotNumericSeries(renderer, samples, x, y, w, h, smoothedMaxPps, inColor, inArea, thick, type,
                NetworkMetricSample::getIncomingPps);
        plotNumericSeries(renderer, samples, x, y, w, h, smoothedMaxPps, outColor, 0, thick, NetGraphType.LINE,
                NetworkMetricSample::getOutgoingPps);
    }

    private void renderCombinedSeries(
            IHudRenderer renderer,
            NetGraphModule module,
            List<NetworkMetricSample> samples,
            int x,
            int y,
            int w,
            int h
    ) {
        // Multi-layer overview
        renderLatencySeries(renderer, module, samples, x, y, w, h, NetGraphType.AREA);
        renderTrafficSeries(renderer, module, samples, x, y, w, h, NetGraphType.LINE);
    }

    private void renderCustomSeries(
            IHudRenderer renderer,
            NetGraphModule module,
            List<NetworkMetricSample> samples,
            int x,
            int y,
            int w,
            int h,
            NetGraphType type
    ) {
        if (module.getEnableLatency().getValue()) {
            renderLatencySeries(renderer, module, samples, x, y, w, h, type);
        }
        if (module.getEnableTraffic().getValue()) {
            renderTrafficSeries(renderer, module, samples, x, y, w, h, NetGraphType.LINE);
        }
        if (module.getEnablePackets().getValue()) {
            renderPacketsSeries(renderer, module, samples, x, y, w, h, NetGraphType.LINE);
        }
    }

    private void renderFooterLegend(
            IHudRenderer renderer,
            NetGraphModule module,
            List<NetworkMetricSample> samples,
            int x,
            int y,
            int width
    ) {
        NetworkMetricSample latest = samples.isEmpty() ? null : samples.get(samples.size() - 1);
        if (latest == null) return;

        StringBuilder sb = new StringBuilder();
        if (latest.isPingAvailable() && module.getShowPing().getValue()) {
            sb.append("Ping: ").append(latest.getPingMs()).append("ms  ");
        }
        if (latest.isJitterAvailable() && module.getShowJitter().getValue()) {
            sb.append("Jitter: ").append(String.format(Locale.ROOT, "%.1fms", latest.getJitterMs())).append("  ");
        }
        if (latest.isTrafficAvailable() && module.getShowIncomingTraffic().getValue()) {
            sb.append("RX: ").append(NetworkTrafficTracker.formatBytesPerSecond(latest.getIncomingBps(), module.getTrafficUnit().getValue())).append("  ");
        }
        if (latest.isTrafficAvailable() && module.getShowOutgoingTraffic().getValue()) {
            sb.append("TX: ").append(NetworkTrafficTracker.formatBytesPerSecond(latest.getOutgoingBps(), module.getTrafficUnit().getValue()));
        }

        renderer.drawText(sb.toString().trim(), x, y + 1, module.getTextColor().getValue(), true, 0.72f, false);
    }

    private void plotNumericSeries(
            IHudRenderer renderer,
            List<NetworkMetricSample> samples,
            int x,
            int y,
            int w,
            int h,
            float maxVal,
            int lineColor,
            int areaColor,
            int lineThick,
            NetGraphType type,
            java.util.function.ToIntFunction<NetworkMetricSample> valueExtractor
    ) {
        int n = samples.size();
        if (n < 1 || maxVal <= 0.0f) return;

        int[] ptX = new int[n];
        int[] ptY = new int[n];

        for (int i = 0; i < n; i++) {
            ptX[i] = x + (int) ((float) i / Math.max(1, n - 1) * w);
            int val = valueExtractor.applyAsInt(samples.get(i));
            if (val < 0) val = 0;
            float ratio = Math.min(1.0f, val / maxVal);
            ptY[i] = y + h - (int) (ratio * h);
        }

        if (type == NetGraphType.BAR) {
            int barW = Math.max(1, (w / Math.max(1, n)) - 1);
            for (int i = 0; i < n; i++) {
                int barH = y + h - ptY[i];
                renderer.fill(ptX[i], ptY[i], barW, barH, lineColor);
            }
        } else if (type == NetGraphType.AREA) {
            // Fill vertical columns under line curve
            for (int i = 0; i < n - 1; i++) {
                int x1 = ptX[i];
                int x2 = ptX[i + 1];
                int colW = Math.max(1, x2 - x1);
                int topY = Math.min(ptY[i], ptY[i + 1]);
                int fillH = y + h - topY;
                renderer.fill(x1, topY, colW, fillH, areaColor);
                drawLineSegment(renderer, x1, ptY[i], x2, ptY[i + 1], lineColor, lineThick);
            }
        } else { // LINE
            for (int i = 0; i < n - 1; i++) {
                drawLineSegment(renderer, ptX[i], ptY[i], ptX[i + 1], ptY[i + 1], lineColor, lineThick);
            }
        }
    }

    private void drawLineSegment(IHudRenderer renderer, int x1, int y1, int x2, int y2, int color, int thickness) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        int curX = x1;
        int curY = y1;

        while (true) {
            renderer.fill(curX, curY, thickness, thickness, color);
            if (curX == x2 && curY == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                curX += sx;
            }
            if (e2 < dx) {
                err += dx;
                curY += sy;
            }
        }
    }

    private double calculateAverage(List<NetworkMetricSample> samples, java.util.function.ToIntFunction<NetworkMetricSample> extractor) {
        long sum = 0;
        int count = 0;
        for (NetworkMetricSample s : samples) {
            int v = extractor.applyAsInt(s);
            if (v >= 0) {
                sum += v;
                count++;
            }
        }
        return count > 0 ? (double) sum / count : -1.0;
    }

    private List<NetworkMetricSample> generatePreviewSamples(long durationMs) {
        List<NetworkMetricSample> list = new ArrayList<>();
        long now = System.currentTimeMillis();
        int count = Math.min(60, (int) (durationMs / 1000L));
        for (int i = 0; i < count; i++) {
            long ts = now - (count - i) * 1000L;
            int basePing = 28 + (int) (Math.sin(i * 0.4) * 8);
            double jitter = 2.0 + Math.abs(Math.sin(i * 0.7) * 3.0);
            long rx = 12000L + (long) (Math.cos(i * 0.3) * 4000L);
            long tx = 2500L + (long) (Math.sin(i * 0.5) * 800L);
            int rxPps = 20 + (int) (Math.sin(i * 0.5) * 4);
            int txPps = 10 + (int) (Math.cos(i * 0.5) * 2);

            list.add(new NetworkMetricSample(
                    ts, basePing, jitter, rx, tx, rxPps, txPps,
                    ConnectionStatus.CONNECTED, ConnectionQuality.EXCELLENT
            ));
        }
        return list;
    }
}
