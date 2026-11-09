package pl.zombek.client.core.modules.visual.nametags;

import pl.zombek.client.core.hud.IHudRenderer;
import pl.zombek.client.core.playerinfo.PlayerArmorData;
import pl.zombek.client.core.playerinfo.PlayerInfoData;

import java.util.ArrayList;
import java.util.List;

/**
 * Renderer for custom 3D entity NameTags and 2D Right Shift GUI preview.
 */
public final class CustomNameTagRenderer {

    public static final class NameTagFormattedContent {
        public final String headerLine;
        public final String mainLine;
        public final String subLine;
        public final int mainTextColor;
        public final int healthColor;
        public final boolean isFriend;

        public NameTagFormattedContent(String headerLine, String mainLine, String subLine, int mainTextColor, int healthColor, boolean isFriend) {
            this.headerLine = headerLine;
            this.mainLine = mainLine;
            this.subLine = subLine;
            this.mainTextColor = mainTextColor;
            this.healthColor = healthColor;
            this.isFriend = isFriend;
        }
    }

    private CustomNameTagRenderer() {}

    public static NameTagFormattedContent formatNameTag(
            PlayerInfoData player,
            NameTagsModule module) {
        if (player == null || module == null) {
            return new NameTagFormattedContent("", "Player", "", 0xFFFFFFFF, 0xFF55FF55, false);
        }

        // 1. Name & Prefix/Suffix/Alias
        StringBuilder nameBuilder = new StringBuilder();

        boolean isFriend = player.isFriend();
        if (isFriend && module.getShowFriendMarkerSetting().getValue()) {
            String icon = (player.getFriendEntry() != null)
                    ? player.getFriendEntry().getFriendMarkerIcon()
                    : module.getFriendMarkerIconSetting().getValue();
            nameBuilder.append(icon).append(" ");
        }

        if (module.getShowCustomPrefixSetting().getValue()) {
            String customPrefix = module.getCustomPrefixSetting().getValue();
            if (!customPrefix.isBlank()) {
                nameBuilder.append(customPrefix);
            }
        }
        if (!player.getServerPrefix().isBlank()) {
            nameBuilder.append(player.getServerPrefix());
        }

        String effectiveName = player.getEffectiveName(module.getEnableAliasSetting().getValue());
        nameBuilder.append(effectiveName);

        if (!player.getServerSuffix().isBlank()) {
            nameBuilder.append(player.getServerSuffix());
        }
        if (module.getShowCustomSuffixSetting().getValue()) {
            String customSuffix = module.getCustomSuffixSetting().getValue();
            if (!customSuffix.isBlank()) {
                nameBuilder.append(customSuffix);
            }
        }

        // 2. Health
        int healthColor = module.getHealthColorSetting().getValue();
        if (module.getShowHealthSetting().getValue() && player.getHealthAvailability().isAvailable()) {
            float hp = player.getHealth();
            float maxHp = player.getMaxHealth();
            if (hp <= 6.0f) {
                healthColor = module.getLowHealthColorSetting().getValue();
            }

            HealthDisplayStyle style = module.getHealthStyleSetting().getValue();
            String hpStr = formatHealth(hp, maxHp, player.getAbsorption(), style, module);
            nameBuilder.append(" ").append(hpStr);
        }

        // 3. Distance
        if (module.getShowDistanceSetting().getValue() && player.getDistanceAvailability().isAvailable()) {
            String distStr = formatDistance(player.getDistance(), module.getDistanceFormatSetting().getValue());
            nameBuilder.append(" [").append(distStr).append("]");
        }

        // 4. Ping
        if (module.getShowPingSetting().getValue() && player.getLatencyAvailability().isAvailable() && player.getLatency() >= 0) {
            nameBuilder.append(" ").append(player.getLatency()).append("ms");
        }

        // 5. Team / SubLine
        String subLine = "";
        if (module.getShowTeamSetting().getValue() && !player.getTeamName().isBlank()) {
            subLine = "<" + player.getTeamName() + ">";
        }

        // 6. Armor / HeaderLine
        String headerLine = "";
        if (module.getShowArmorSetting().getValue() && player.getArmorAvailability().isAvailable() && !player.getArmor().isEmpty()) {
            StringBuilder armorSb = new StringBuilder();
            for (PlayerArmorData a : player.getArmor()) {
                armorSb.append("[").append(a.getSlot().getLabel()).append("] ");
            }
            headerLine = armorSb.toString().trim();
        }

        int mainTextColor = player.getTeamColorArgb();
        if (isFriend && player.getFriendEntry() != null && player.getFriendEntry().getCustomColorArgb() != 0) {
            mainTextColor = player.getFriendEntry().getCustomColorArgb();
        }

        return new NameTagFormattedContent(headerLine, nameBuilder.toString(), subLine, mainTextColor, healthColor, isFriend);
    }

    public static String formatHealth(float hp, float maxHp, float absorption, HealthDisplayStyle style, NameTagsModule module) {
        int precision = module.getDecimalPrecisionSetting().getValue().intValue();
        String hpFormat = "%." + precision + "f";

        switch (style) {
            case HEARTS:
                int fullHearts = Math.max(0, (int) Math.ceil(hp / 2.0f));
                return "❤".repeat(Math.min(10, fullHearts));
            case BAR:
                int bars = Math.max(0, (int) Math.round((hp / maxHp) * 10));
                return "[" + "|".repeat(Math.min(10, bars)) + ".".repeat(Math.max(0, 10 - bars)) + "]";
            case COMPACT:
                return String.format(java.util.Locale.ROOT, hpFormat, hp);
            case CUSTOM:
                return String.format(java.util.Locale.ROOT, hpFormat + " / " + hpFormat, hp, maxHp);
            case NUMERIC:
            default:
                if (absorption > 0 && module.getShowAbsorptionSetting().getValue()) {
                    return String.format(java.util.Locale.ROOT, hpFormat + "+%.1f HP", hp, absorption);
                }
                return String.format(java.util.Locale.ROOT, hpFormat + " HP", hp);
        }
    }

    public static String formatDistance(double distance, DistanceFormat format) {
        switch (format) {
            case DECIMAL:
                return String.format(java.util.Locale.ROOT, "%.1fm", distance);
            case ROUNDED:
                return ((int) Math.round(distance / 5.0) * 5) + "m";
            case BLOCKS:
            default:
                return (int) Math.round(distance) + "m";
        }
    }

    /**
     * Renders a 2D NameTag preview box for the Right Shift settings GUI.
     */
    public static void renderPreview(IHudRenderer renderer, NameTagsModule module, int posX, int posY) {
        if (renderer == null || module == null) return;

        PlayerInfoData demoPlayer = new PlayerInfoData(
                java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "ZombekPlayer", "ZombekPlayer", "[VIP] ", "", "Champions", 0xFF55FFFF,
                24, pl.zombek.client.core.playerinfo.DataAvailability.AVAILABLE,
                "SURVIVAL", pl.zombek.client.core.playerinfo.DataAvailability.AVAILABLE,
                18.0f, 20.0f, 4.0f, pl.zombek.client.core.playerinfo.DataAvailability.AVAILABLE,
                List.of(new PlayerArmorData(PlayerArmorData.ArmorSlot.HELMET, "minecraft:diamond_helmet", "Diamond Helmet", 300, 363, true, null)),
                pl.zombek.client.core.playerinfo.DataAvailability.AVAILABLE,
                "Diamond Sword", pl.zombek.client.core.playerinfo.DataAvailability.AVAILABLE,
                14.2, pl.zombek.client.core.playerinfo.DataAvailability.AVAILABLE,
                true, new pl.zombek.client.core.friends.FriendEntry(null, "ZombekPlayer", "FriendZombek", 0xFFFFAA00, "★", "", ""),
                null
        );

        NameTagFormattedContent content = formatNameTag(demoPlayer, module);

        List<String> lines = new ArrayList<>();
        if (!content.headerLine.isBlank()) lines.add(content.headerLine);
        lines.add(content.mainLine + " [PREVIEW]");
        if (!content.subLine.isBlank()) lines.add(content.subLine);

        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, renderer.getTextWidth(line));
        }

        int paddingH = 6;
        int paddingV = 4;
        int lineHeight = renderer.getFontHeight() + 2;
        int totalWidth = maxWidth + paddingH * 2;
        int totalHeight = lines.size() * lineHeight + paddingV * 2;

        int bgAlpha = (int) (module.getBackgroundOpacitySetting().getValue() * 2.55);
        int bgColor = (bgAlpha << 24) | (module.getBackgroundColorSetting().getValue() & 0x00FFFFFF);

        renderer.fill(posX, posY, totalWidth, totalHeight, bgColor);

        if (module.getShowBorderSetting().getValue()) {
            renderer.drawBorder(posX, posY, totalWidth, totalHeight, module.getBorderColorSetting().getValue(), module.getBorderThicknessSetting().getValue().intValue());
        }

        int curY = posY + paddingV;
        for (String line : lines) {
            renderer.drawText(line, posX + paddingH, curY, content.mainTextColor, module.getShadowSetting().getValue(), 1.0f, false);
            curY += lineHeight;
        }
    }
}
