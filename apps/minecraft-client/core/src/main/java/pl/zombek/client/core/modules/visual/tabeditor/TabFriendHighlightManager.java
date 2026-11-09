package pl.zombek.client.core.modules.visual.tabeditor;

import pl.zombek.client.core.friends.FriendEntry;
import pl.zombek.client.core.playerinfo.PlayerInfoData;

/**
 * Manages friend highlights and visual badges in the Tab Player List.
 */
public final class TabFriendHighlightManager {

    private TabFriendHighlightManager() {}

    public static int resolveEntryColor(PlayerInfoData player, boolean enableHighlight, int defaultColor, int friendDefaultColor) {
        if (player == null) return defaultColor;

        if (enableHighlight && player.isFriend()) {
            FriendEntry entry = player.getFriendEntry();
            if (entry != null && entry.getCustomColorArgb() != 0) {
                return entry.getCustomColorArgb();
            }
            return friendDefaultColor;
        }

        if (player.getTeamColorArgb() != 0 && player.getTeamColorArgb() != 0xFFFFFFFF) {
            return player.getTeamColorArgb();
        }

        return defaultColor;
    }

    public static String resolveDisplayName(PlayerInfoData player, boolean enableHighlight, String friendIcon) {
        if (player == null) return "Player";

        if (enableHighlight && player.isFriend()) {
            FriendEntry entry = player.getFriendEntry();
            String icon = (entry != null && !entry.getFriendMarkerIcon().isBlank())
                    ? entry.getFriendMarkerIcon()
                    : friendIcon;

            String alias = (entry != null && !entry.getAlias().isBlank())
                    ? entry.getAlias()
                    : player.getDisplayName();

            return icon + " " + alias;
        }

        return player.getDisplayName();
    }
}
