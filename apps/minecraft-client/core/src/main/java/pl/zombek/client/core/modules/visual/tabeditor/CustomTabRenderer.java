package pl.zombek.client.core.modules.visual.tabeditor;

import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.playerinfo.DataAvailability;
import pl.zombek.client.core.playerinfo.PlayerInfoData;
import pl.zombek.client.core.playerinfo.ZombekPlayerInfoService;

import java.util.List;

/**
 * Universal renderer for the customized Tab Player List.
 */
public final class CustomTabRenderer {

    private static long openStartTime = 0L;
    private static long closeStartTime = 0L;
    private static boolean wasOpen = false;

    private CustomTabRenderer() {}

    public static void renderTabList(
            IHudRenderer renderer,
            TabEditorModule module,
            List<PlayerInfoData> players,
            String serverHeader,
            String serverFooter,
            boolean isPreview) {
        if (renderer == null || module == null) return;

        // 1. Animation Alpha
        long now = System.currentTimeMillis();
        float alpha = 1.0f;
        int animDuration = module.getAnimationDurationSetting().getValue().intValue();

        if (module.getFadeInSetting().getValue() && animDuration > 0 && !isPreview) {
            if (!wasOpen) {
                openStartTime = now;
                wasOpen = true;
            }
            float progress = (float) (now - openStartTime) / animDuration;
            alpha = Math.min(1.0f, Math.max(0.1f, progress));
        }

        // 2. Prepare Data
        List<PlayerInfoData> list = (players != null && !players.isEmpty())
                ? players
                : ZombekPlayerInfoService.getInstance().getPlayers();

        // Sort players
        List<PlayerInfoData> sorted = TabSortingManager.sortPlayers(
                list,
                module.getSortingModeSetting().getValue(),
                module.getFriendsFirstSetting().getValue(),
                module.getTeamGroupingSetting().getValue()
        );

        // Header and Footer lines
        HeaderFooterMode headerMode = module.getHeaderModeSetting().getValue();
        if (module.getHideServerHeaderSetting().getValue() && headerMode == HeaderFooterMode.ORIGINAL) {
            headerMode = HeaderFooterMode.HIDDEN;
        }
        List<String> headerLines = module.getShowHeaderSetting().getValue()
                ? TabHeaderFooterManager.resolveLines(headerMode, serverHeader, module.getCustomHeaderSetting().getValue(), true)
                : List.of();

        HeaderFooterMode footerMode = module.getFooterModeSetting().getValue();
        if (module.getHideServerFooterSetting().getValue() && footerMode == HeaderFooterMode.ORIGINAL) {
            footerMode = HeaderFooterMode.HIDDEN;
        }
        List<String> footerLines = module.getShowFooterSetting().getValue()
                ? TabHeaderFooterManager.resolveLines(footerMode, serverFooter, module.getCustomFooterSetting().getValue(), false)
                : List.of();

        // 3. Compute Grid Geometry
        int screenWidth = renderer.getScreenWidth();
        int screenHeight = renderer.getScreenHeight();

        int maxCols = module.getMaxColumnsSetting().getValue().intValue();
        int maxRows = module.getMaxRowsSetting().getValue().intValue();
        int colWidth = module.getColumnWidthSetting().getValue().intValue();
        int rowHeight = module.getRowHeightSetting().getValue().intValue();
        int hSpacing = module.getHSpacingSetting().getValue().intValue();
        int vSpacing = module.getVSpacingSetting().getValue().intValue();
        boolean adaptiveWidth = module.getAdaptiveWidthSetting().getValue();

        TabLayoutManager.GridLayoutResult grid = TabLayoutManager.computeGrid(
                sorted.size(), maxCols, maxRows, colWidth, rowHeight, hSpacing, vSpacing, screenWidth, adaptiveWidth
        );

        int paddingH = 10;
        int paddingV = 8;
        int fontHeight = renderer.getFontHeight();

        int headerHeight = headerLines.size() * (fontHeight + 2);
        int footerHeight = footerLines.size() * (fontHeight + 2);

        int contentWidth = Math.max(grid.totalGridWidth, 200);
        for (String h : headerLines) {
            contentWidth = Math.max(contentWidth, renderer.getTextWidth(h));
        }
        for (String f : footerLines) {
            contentWidth = Math.max(contentWidth, renderer.getTextWidth(f));
        }

        int panelWidth = contentWidth + paddingH * 2;
        int panelHeight = paddingV * 2 + (headerHeight > 0 ? headerHeight + 6 : 0) + grid.totalGridHeight + (footerHeight > 0 ? footerHeight + 6 : 0);

        int panelX = (screenWidth - panelWidth) / 2;
        int panelY = Math.max(10, (screenHeight - panelHeight) / 4);

        // Apply background and opacity
        int bgAlpha = (int) (module.getBackgroundOpacitySetting().getValue() * 2.55f * alpha);
        int bgColor = (bgAlpha << 24) | (module.getBackgroundColorSetting().getValue() & 0x00FFFFFF);

        renderer.fill(panelX, panelY, panelWidth, panelHeight, bgColor);

        if (module.getShowBorderSetting().getValue()) {
            int borderAlpha = (int) (255 * alpha);
            int borderColor = (borderAlpha << 24) | (module.getBorderColorSetting().getValue() & 0x00FFFFFF);
            renderer.drawBorder(panelX, panelY, panelWidth, panelHeight, borderColor, module.getBorderThicknessSetting().getValue().intValue());
        }

        int curY = panelY + paddingV;

        // 4. Render Header
        if (!headerLines.isEmpty()) {
            int headerColor = module.getHeaderColorSetting().getValue();
            for (String line : headerLines) {
                int lineW = renderer.getTextWidth(line);
                int lineX = panelX + (panelWidth - lineW) / 2;
                renderer.drawText(line, lineX, curY, headerColor, module.getShadowSetting().getValue(), 1.0f, true);
                curY += fontHeight + 2;
            }
            curY += 4;
        }

        // 5. Render Player Entries Grid
        int gridStartX = panelX + (panelWidth - grid.totalGridWidth) / 2;
        int gridStartY = curY;

        boolean showAvatar = module.getShowAvatarSetting().getValue();
        boolean showPrefix = module.getShowPrefixSetting().getValue();
        boolean showPing = module.getShowPingSetting().getValue();
        TabPingMode pingMode = module.getPingModeSetting().getValue();
        boolean friendHighlight = module.getEnableFriendHighlightSetting().getValue();

        for (int i = 0; i < sorted.size(); i++) {
            PlayerInfoData p = sorted.get(i);
            int colIndex = i / grid.rows;
            int rowIndex = i % grid.rows;

            if (colIndex >= grid.columns) break;

            int cellX = grid.getCellX(colIndex, gridStartX, hSpacing);
            int cellY = grid.getCellY(rowIndex, gridStartY, vSpacing);

            // Row background for card/modern layout
            if (module.getLayoutPresetSetting().getValue() == TabLayout.CARD || module.getLayoutPresetSetting().getValue() == TabLayout.MODERN) {
                int rowBg = (Math.min(40, bgAlpha / 2) << 24) | 0x00FFFFFF;
                renderer.fill(cellX, cellY, grid.effectiveColumnWidth, grid.effectiveRowHeight, rowBg);
            }

            int contentX = cellX + 2;

            // Avatar placeholder
            if (showAvatar) {
                int avatarSize = 8;
                int avatarY = cellY + (grid.effectiveRowHeight - avatarSize) / 2;
                renderer.fill(contentX, avatarY, avatarSize, avatarSize, 0xFF888888);
                contentX += avatarSize + 3;
            }

            // Prefix
            if (showPrefix && !p.getServerPrefix().isBlank()) {
                renderer.drawText(p.getServerPrefix(), contentX, cellY + 2, 0xFFAAAAAA, module.getShadowSetting().getValue(), 1.0f, false);
                contentX += renderer.getTextWidth(p.getServerPrefix()) + 2;
            }

            // Player Name & Friend Tag
            String displayName = TabFriendHighlightManager.resolveDisplayName(p, friendHighlight, module.getFriendIconSetting().getValue());
            int nameColor = TabFriendHighlightManager.resolveEntryColor(
                    p, friendHighlight, module.getNameColorSetting().getValue(), module.getFriendColorSetting().getValue()
            );

            renderer.drawText(displayName, contentX, cellY + 2, nameColor, module.getShadowSetting().getValue(), 1.0f, false);

            // Ping
            if (showPing) {
                String pingStr = formatPing(p, pingMode);
                int pingColor = resolvePingColor(p, module);
                int pingW = renderer.getTextWidth(pingStr);
                int pingX = cellX + grid.effectiveColumnWidth - pingW - 3;
                renderer.drawText(pingStr, pingX, cellY + 2, pingColor, module.getShadowSetting().getValue(), 1.0f, false);
            }
        }

        curY = gridStartY + grid.totalGridHeight + 6;

        // 6. Render Footer
        if (!footerLines.isEmpty()) {
            int footerColor = module.getFooterColorSetting().getValue();
            for (String line : footerLines) {
                int lineW = renderer.getTextWidth(line);
                int lineX = panelX + (panelWidth - lineW) / 2;
                renderer.drawText(line, lineX, curY, footerColor, module.getShadowSetting().getValue(), 1.0f, false);
                curY += fontHeight + 2;
            }
        }
    }

    private static String formatPing(PlayerInfoData p, TabPingMode mode) {
        if (p.getLatencyAvailability() != DataAvailability.AVAILABLE || p.getLatency() < 0) {
            return "?";
        }
        int ms = p.getLatency();
        switch (mode) {
            case BARS:
                if (ms < 50) return "📶5";
                if (ms < 100) return "📶4";
                if (ms < 150) return "📶3";
                if (ms < 300) return "📶2";
                return "📶1";
            case MINIMAL_ICON:
                return "●";
            case COLORED_TEXT:
            case NUMERIC:
            default:
                return ms + "ms";
        }
    }

    private static int resolvePingColor(PlayerInfoData p, TabEditorModule module) {
        if (p.getLatencyAvailability() != DataAvailability.AVAILABLE || p.getLatency() < 0) {
            return module.getUnknownColorSetting().getValue();
        }
        int ms = p.getLatency();
        if (ms <= 60) return module.getGoodColorSetting().getValue();
        if (ms <= 150) return module.getMediumColorSetting().getValue();
        return module.getHighColorSetting().getValue();
    }
}
