package pl.zombek.client.core.modules.visual.tabeditor;

import pl.zombek.client.core.playerinfo.PlayerInfoData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Visual sorter for Tab Player List entries.
 * Strictly visual reordering: never deletes, creates, or alters underlying player entries.
 */
public final class TabSortingManager {

    private TabSortingManager() {}

    public static List<PlayerInfoData> sortPlayers(
            List<PlayerInfoData> originalList,
            TabSortingMode mode,
            boolean friendsFirst,
            boolean teamGrouping) {
        if (originalList == null || originalList.isEmpty()) {
            return new ArrayList<>();
        }

        List<PlayerInfoData> sorted = new ArrayList<>(originalList);

        Comparator<PlayerInfoData> comparator = getComparator(mode, teamGrouping);

        if (friendsFirst) {
            Comparator<PlayerInfoData> friendComparator = Comparator.comparing(
                    PlayerInfoData::isFriend,
                    Comparator.reverseOrder()
            );
            comparator = friendComparator.thenComparing(comparator);
        }

        sorted.sort(comparator);
        return sorted;
    }

    private static Comparator<PlayerInfoData> getComparator(TabSortingMode mode, boolean teamGrouping) {
        if (teamGrouping) {
            Comparator<PlayerInfoData> teamComp = Comparator.comparing(PlayerInfoData::getTeamName, String.CASE_INSENSITIVE_ORDER);
            return teamComp.thenComparing(PlayerInfoData::getUsername, String.CASE_INSENSITIVE_ORDER);
        }

        switch (mode) {
            case ALPHABETICAL:
                return Comparator.comparing(PlayerInfoData::getUsername, String.CASE_INSENSITIVE_ORDER);
            case TEAM:
                return Comparator.comparing(PlayerInfoData::getTeamName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(PlayerInfoData::getUsername, String.CASE_INSENSITIVE_ORDER);
            case VANILLA_ORDER:
            case FRIENDS_FIRST:
            case CUSTOM_ORDER:
            default:
                // Preserves incoming order (stable comparator)
                return (p1, p2) -> 0;
        }
    }
}
