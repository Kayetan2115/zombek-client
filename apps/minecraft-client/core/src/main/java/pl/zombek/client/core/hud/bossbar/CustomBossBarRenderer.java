package pl.zombek.client.core.hud.bossbar;

import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.hud.component.HudComponentModel;

import java.util.List;

/**
 * Custom renderer for Minecraft Boss Bars (Ender Dragon, Wither, Raid, modded bosses).
 */
public final class CustomBossBarRenderer {

    private CustomBossBarRenderer() {}

    public static int[] render(
            IHudRenderer renderer,
            HudComponentModel model,
            List<BossBarData> bossBars,
            BossBarStyle style,
            int barWidth,
            int barHeight,
            int borderThickness,
            int cornerRadius,
            boolean showBossName,
            boolean showProgressBar,
            boolean showPercentage,
            int maxBossBars,
            int spacing,
            StackDirection stackDirection,
            boolean useBossColor,
            int customBarColor,
            int textColor,
            int bgColor,
            int borderColor,
            boolean previewMode
    ) {
        if (renderer == null || bossBars == null || bossBars.isEmpty()) {
            return new int[]{0, 0};
        }

        int count = Math.min(bossBars.size(), maxBossBars);
        int fontH = renderer.getFontHeight();

        int itemH = barHeight + (showBossName ? fontH + 2 : 0) + 2;
        int totalH = count * itemH + Math.max(0, count - 1) * spacing;
        int totalW = barWidth + 8;

        int screenW = renderer.getScreenWidth();
        int screenH = renderer.getScreenHeight();

        model.setWidth(totalW);
        model.setHeight(totalH);

        int startX = model.getScreenX(screenW);
        int startY = model.getScreenY(screenH);

        for (int i = 0; i < count; i++) {
            int index = (stackDirection == StackDirection.DOWN) ? i : (count - 1 - i);
            BossBarData boss = bossBars.get(index);

            int currentY = startY + i * (itemH + spacing);

            // Determine bar color
            int barColor = useBossColor ? boss.getColor().getArgb() : customBarColor;

            // Draw Boss Name
            if (showBossName) {
                String nameText = boss.getName();
                if (showPercentage) {
                    nameText += " §7(" + boss.getPercentage() + "%)";
                }
                int nameW = renderer.getTextWidth(nameText);
                int nameX = startX + (totalW - nameW) / 2;
                renderer.drawText(nameText, nameX, currentY, textColor, true, 1.0f, true);
                currentY += fontH + 2;
            }

            // Draw Progress Bar
            if (showProgressBar) {
                int barX = startX + (totalW - barWidth) / 2;

                // Background track
                int trackBg = (style == BossBarStyle.MODERN) ? 0xAA0F172A : bgColor;
                renderer.fill(barX, currentY, barWidth, barHeight, trackBg);

                // Progress fill
                int fillW = (int) Math.round(barWidth * boss.getProgress());
                if (fillW > 0) {
                    renderer.fill(barX, currentY, fillW, barHeight, barColor);
                }

                // Notched segments if applicable
                int notches = boss.getOverlay().getNotches();
                if (style == BossBarStyle.SEGMENTED || notches > 0) {
                    int numSegments = notches > 0 ? notches : 6;
                    float segStep = (float) barWidth / numSegments;
                    for (int s = 1; s < numSegments; s++) {
                        int notchX = barX + (int) Math.round(s * segStep);
                        renderer.fill(notchX, currentY, 1, barHeight, 0x80000000);
                    }
                }

                // Border
                if (borderThickness > 0) {
                    int borderC = (style == BossBarStyle.MODERN) ? 0x6638BDF8 : borderColor;
                    renderer.drawBorder(barX, currentY, barWidth, barHeight, borderC, borderThickness);
                }
            }
        }

        if (previewMode) {
            String badge = "§8[§cPREVIEW§8]";
            renderer.drawText(badge, startX + totalW - renderer.getTextWidth(badge) - 2, startY + totalH + 2, 0xFFAAAAAA, true, 0.7f, false);
        }

        return new int[]{totalW, totalH};
    }
}
