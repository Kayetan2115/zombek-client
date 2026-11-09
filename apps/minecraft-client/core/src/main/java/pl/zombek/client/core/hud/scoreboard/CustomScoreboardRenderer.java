package pl.zombek.client.core.hud.scoreboard;

import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.hud.component.HudComponentModel;

import java.util.ArrayList;
import java.util.List;

/**
 * High-performance, customizable renderer for Minecraft Scoreboard sidebar.
 * Encapsulates title formatting, team prefix/suffix stitching, scores alignment, and styles.
 */
public final class CustomScoreboardRenderer {

    private CustomScoreboardRenderer() {}

    /**
     * Renders the given scoreboard objective according to the module settings.
     *
     * @return Calculated dimensions [width, height]
     */
    public static int[] render(
            IHudRenderer renderer,
            HudComponentModel model,
            ScoreboardObjectiveData objective,
            ScoreboardStyle style,
            boolean showTitle,
            String customTitleText,
            int titleColor,
            float titleScale,
            TextAlignment titleAlignment,
            boolean titleShadow,
            boolean titleBackground,
            int titlePadding,
            boolean showLines,
            boolean showScores,
            boolean showTeamPrefixes,
            boolean showTeamSuffixes,
            int lineSpacing,
            TextAlignment lineAlignment,
            float lineScale,
            boolean lineShadow,
            boolean lineBackground,
            boolean alternatingBg,
            int maxLines,
            boolean truncateLines,
            int maxLineLength,
            boolean dynamicWidth,
            int fixedWidth,
            boolean hideEmptyLines,
            boolean hideEmptyScores,
            int borderThickness,
            boolean showBorder,
            int textColor,
            int scoreColor,
            int bgColor,
            int altBgColor,
            int borderColor,
            boolean previewMode
    ) {
        if (renderer == null || objective == null) {
            return new int[]{0, 0};
        }

        int fontH = renderer.getFontHeight();
        String rawTitle = (customTitleText != null && !customTitleText.trim().isEmpty())
                ? customTitleText
                : objective.getTitle();

        if (previewMode && (rawTitle == null || rawTitle.isEmpty())) {
            rawTitle = "§6§lSCOREBOARD";
        }

        // Process lines
        List<ScoreboardLineData> rawLines = objective.getLines();
        List<ScoreboardLineData> linesToRender = new ArrayList<>();

        if (showLines && rawLines != null) {
            for (ScoreboardLineData line : rawLines) {
                if (line == null) continue;
                if (hideEmptyLines && line.isEmpty()) continue;
                linesToRender.add(line);
                if (linesToRender.size() >= maxLines) break;
            }
        }

        // Calculate maximum content width
        int titleTextWidth = (int) Math.ceil(renderer.getTextWidth(rawTitle) * titleScale);
        int maxLineWidth = titleTextWidth;

        List<String> formattedLineTexts = new ArrayList<>();
        List<String> scoreTexts = new ArrayList<>();
        int maxScoreWidth = 0;

        for (ScoreboardLineData line : linesToRender) {
            StringBuilder sb = new StringBuilder();
            if (showTeamPrefixes && !line.getPrefix().isEmpty()) {
                sb.append(line.getPrefix());
            }
            sb.append(line.getPlayerName());
            if (showTeamSuffixes && !line.getSuffix().isEmpty()) {
                sb.append(line.getSuffix());
            }

            String fullText = sb.toString();
            if (fullText.isEmpty() && !line.getFormattedText().isEmpty()) {
                fullText = line.getFormattedText();
            }

            if (truncateLines && fullText.length() > maxLineLength) {
                fullText = fullText.substring(0, Math.max(0, maxLineLength - 3)) + "...";
            }

            formattedLineTexts.add(fullText);
            int lw = (int) Math.ceil(renderer.getTextWidth(fullText) * lineScale);
            if (lw > maxLineWidth) {
                maxLineWidth = lw;
            }

            if (showScores && (!hideEmptyScores || line.getScore() != 0)) {
                String scoreStr = String.valueOf(line.getScore());
                scoreTexts.add(scoreStr);
                int sw = (int) Math.ceil(renderer.getTextWidth(scoreStr) * lineScale);
                if (sw > maxScoreWidth) {
                    maxScoreWidth = sw;
                }
            } else {
                scoreTexts.add("");
            }
        }

        int scoreGap = maxScoreWidth > 0 ? 8 : 0;
        int totalContentWidth = maxLineWidth + maxScoreWidth + scoreGap;
        int paddingX = 4;
        int finalWidth = dynamicWidth ? Math.max(80, totalContentWidth + paddingX * 2) : Math.max(80, fixedWidth);

        // Calculate total height
        int headerHeight = showTitle ? (int) Math.ceil(fontH * titleScale) + titlePadding * 2 : 0;
        int singleLineH = (int) Math.ceil(fontH * lineScale);
        int linesHeight = linesToRender.size() * (singleLineH + lineSpacing);
        int finalHeight = headerHeight + linesHeight + 4;

        int screenW = renderer.getScreenWidth();
        int screenH = renderer.getScreenHeight();
        model.setWidth(finalWidth);
        model.setHeight(finalHeight);
        int startX = model.getScreenX(screenW);
        int startY = model.getScreenY(screenH);

        // Background & Border styling
        renderScoreboardBackdrop(renderer, startX, startY, finalWidth, finalHeight, style, bgColor, borderColor, showBorder, borderThickness);

        int currentY = startY + 2;

        // Render Title
        if (showTitle) {
            if (titleBackground) {
                int titleBg = (style == ScoreboardStyle.GLASS) ? 0x40101525 : 0x40000000;
                renderer.fill(startX, currentY, finalWidth, headerHeight, titleBg);
            }

            int titleX;
            switch (titleAlignment) {
                case LEFT:
                    titleX = startX + paddingX;
                    break;
                case RIGHT:
                    titleX = startX + finalWidth - paddingX - titleTextWidth;
                    break;
                case CENTER:
                default:
                    titleX = startX + (finalWidth - titleTextWidth) / 2;
                    break;
            }

            int titleDrawY = currentY + titlePadding;
            renderer.drawText(rawTitle, titleX, titleDrawY, titleColor, titleShadow, titleScale, true);
            currentY += headerHeight + 2;
        }

        // Render Lines
        for (int i = 0; i < linesToRender.size(); i++) {
            String lineText = formattedLineTexts.get(i);
            String scoreStr = scoreTexts.get(i);

            int lineY = currentY + i * (singleLineH + lineSpacing);

            // Alternating line background
            if (alternatingBg && (i % 2 == 1)) {
                renderer.fill(startX, lineY, finalWidth, singleLineH + lineSpacing, altBgColor);
            } else if (lineBackground) {
                // Subtle standard line highlight if configured
            }

            // Align text
            int textX;
            int textW = (int) Math.ceil(renderer.getTextWidth(lineText) * lineScale);
            switch (lineAlignment) {
                case CENTER:
                    textX = startX + (finalWidth - textW - maxScoreWidth - scoreGap) / 2;
                    break;
                case RIGHT:
                    textX = startX + finalWidth - paddingX - maxScoreWidth - scoreGap - textW;
                    break;
                case LEFT:
                default:
                    textX = startX + paddingX;
                    break;
            }

            renderer.drawText(lineText, textX, lineY, textColor, lineShadow, lineScale, false);

            // Render score
            if (!scoreStr.isEmpty()) {
                int scoreW = (int) Math.ceil(renderer.getTextWidth(scoreStr) * lineScale);
                int scoreX = startX + finalWidth - paddingX - scoreW;
                renderer.drawText(scoreStr, scoreX, lineY, scoreColor, lineShadow, lineScale, false);
            }
        }

        // Preview indicator
        if (previewMode) {
            String badge = "§8[§cPREVIEW§8]";
            int badgeW = renderer.getTextWidth(badge);
            renderer.drawText(badge, startX + finalWidth - badgeW - 2, startY + finalHeight - fontH, 0xFFAAAAAA, true, 0.75f, false);
        }

        return new int[]{finalWidth, finalHeight};
    }

    private static void renderScoreboardBackdrop(
            IHudRenderer renderer, int x, int y, int width, int height,
            ScoreboardStyle style, int customBg, int customBorder, boolean showBorder, int borderThickness
    ) {
        switch (style) {
            case VANILLA:
                renderer.fill(x, y, width, height, 0x60000000);
                break;
            case MINIMAL:
                // No outer background in minimal mode
                break;
            case MODERN:
                int modernBg = 0xAA0F172A; // Deep modern slate
                int modernBorder = 0x5538BDF8; // Subtle sky accent
                renderer.fill(x, y, width, height, modernBg);
                renderer.drawBorder(x, y, width, height, modernBorder, 1);
                break;
            case GLASS:
                int glassBg = 0x35101726; // High-transparency glass
                int glassBorder = 0x25FFFFFF; // Frost outline
                renderer.fill(x, y, width, height, glassBg);
                renderer.drawBorder(x, y, width, height, glassBorder, 1);
                break;
            case CUSTOM:
            default:
                if ((customBg & 0xFF000000) != 0) {
                    renderer.fill(x, y, width, height, customBg);
                }
                if (showBorder && borderThickness > 0) {
                    renderer.drawBorder(x, y, width, height, customBorder, borderThickness);
                }
                break;
        }
    }
}
