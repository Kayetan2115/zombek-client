package pl.zombek.client.core.modules.visual.tabeditor;

import org.junit.jupiter.api.Test;
import pl.zombek.client.core.playerinfo.DataAvailability;
import pl.zombek.client.core.playerinfo.PlayerInfoData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TabSortingManagerTest {

    private PlayerInfoData createPlayer(String username, String team, boolean isFriend) {
        return new PlayerInfoData(
                UUID.randomUUID(), username, username, "", "", team, 0xFFFFFFFF,
                30, DataAvailability.AVAILABLE, "SURVIVAL", DataAvailability.AVAILABLE,
                20.0f, 20.0f, 0.0f, DataAvailability.AVAILABLE,
                Collections.emptyList(), DataAvailability.AVAILABLE, "", DataAvailability.AVAILABLE,
                10.0, DataAvailability.AVAILABLE, isFriend, null, null
        );
    }

    @Test
    void testAlphabeticalSorting() {
        List<PlayerInfoData> list = List.of(
                createPlayer("Charlie", "Blue", false),
                createPlayer("Alice", "Red", false),
                createPlayer("Bob", "Green", false)
        );

        List<PlayerInfoData> sorted = TabSortingManager.sortPlayers(list, TabSortingMode.ALPHABETICAL, false, false);
        assertEquals(3, sorted.size());
        assertEquals("Alice", sorted.get(0).getUsername());
        assertEquals("Bob", sorted.get(1).getUsername());
        assertEquals("Charlie", sorted.get(2).getUsername());
    }

    @Test
    void testTeamSorting() {
        List<PlayerInfoData> list = List.of(
                createPlayer("PlayerZ", "YellowTeam", false),
                createPlayer("PlayerA", "BlueTeam", false),
                createPlayer("PlayerM", "RedTeam", false)
        );

        List<PlayerInfoData> sorted = TabSortingManager.sortPlayers(list, TabSortingMode.TEAM, false, false);
        assertEquals(3, sorted.size());
        assertEquals("BlueTeam", sorted.get(0).getTeamName());
        assertEquals("RedTeam", sorted.get(1).getTeamName());
        assertEquals("YellowTeam", sorted.get(2).getTeamName());
    }

    @Test
    void testFriendsFirstSorting() {
        List<PlayerInfoData> list = List.of(
                createPlayer("User1", "TeamA", false),
                createPlayer("FriendUser", "TeamB", true),
                createPlayer("User2", "TeamA", false)
        );

        List<PlayerInfoData> sorted = TabSortingManager.sortPlayers(list, TabSortingMode.ALPHABETICAL, true, false);
        assertEquals(3, sorted.size());
        assertTrue(sorted.get(0).isFriend());
        assertEquals("FriendUser", sorted.get(0).getUsername());
    }

    @Test
    void testNoPlayerLostOrDuplicated() {
        List<PlayerInfoData> list = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            list.add(createPlayer("Player" + (50 - i), "Team" + (i % 4), i % 5 == 0));
        }

        List<PlayerInfoData> sorted = TabSortingManager.sortPlayers(list, TabSortingMode.FRIENDS_FIRST, true, true);
        assertEquals(50, sorted.size());

        for (PlayerInfoData original : list) {
            assertTrue(sorted.contains(original));
        }
    }
}
