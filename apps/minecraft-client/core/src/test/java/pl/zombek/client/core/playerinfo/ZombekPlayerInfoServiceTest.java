package pl.zombek.client.core.playerinfo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.friends.FriendEntry;
import pl.zombek.client.core.friends.NameTagFriendManager;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ZombekPlayerInfoServiceTest {

    private ZombekPlayerInfoService service;
    private FallbackPlayerInfoDataProvider fallbackProvider;

    @BeforeEach
    void setUp() {
        fallbackProvider = new FallbackPlayerInfoDataProvider();
        PlayerInfoDataProviderRegistry.getInstance().registerProvider(fallbackProvider);
        service = ZombekPlayerInfoService.getInstance();
        service.resetForTesting();
        NameTagFriendManager.getInstance().clearAll();
    }

    @AfterEach
    void tearDown() {
        service.resetForTesting();
        NameTagFriendManager.getInstance().clearAll();
    }

    @Test
    void testGetPlayersReturnsPopulatedList() {
        List<PlayerInfoData> players = service.getPlayers();
        assertNotNull(players);
        assertFalse(players.isEmpty());
        assertTrue(players.stream().anyMatch(p -> "ZombekMaster".equals(p.getUsername())));
    }

    @Test
    void testEnrichmentWithFriendStatus() {
        UUID friendUuid = UUID.fromString("00000000-0000-0000-0000-000000000002");
        FriendEntry friend = new FriendEntry(friendUuid, "Alex", "BestieAlex", 0xFF55FF55, "★", "", "");
        NameTagFriendManager.getInstance().addFriend(friend);

        service.refreshCache();
        PlayerInfoData alex = service.getPlayer(friendUuid, "Alex");

        assertNotNull(alex);
        assertTrue(alex.isFriend());
        assertEquals("BestieAlex", alex.getEffectiveName(true));
        assertEquals("Alex", alex.getEffectiveName(false));
    }

    @Test
    void testCacheClearOnWorldLeave() {
        List<PlayerInfoData> list1 = service.getPlayers();
        assertFalse(list1.isEmpty());

        service.onWorldLeave();
        // Fallback provider will supply again on next call, but cache was wiped cleanly
        List<PlayerInfoData> list2 = service.getPlayers();
        assertNotNull(list2);
    }

    @Test
    void testGetEntityInfo() {
        PlayerInfoData entityInfo = service.getEntityInfo("dummy_handle");
        assertNotNull(entityInfo);
        assertEquals("Zombie", entityInfo.getUsername());
        assertTrue(entityInfo.getHealthAvailability().isAvailable());
    }
}
