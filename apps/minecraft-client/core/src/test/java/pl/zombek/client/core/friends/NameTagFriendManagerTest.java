package pl.zombek.client.core.friends;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NameTagFriendManagerTest {

    @TempDir
    Path tempDir;

    private NameTagFriendManager manager;

    @BeforeEach
    void setUp() {
        manager = NameTagFriendManager.getInstance();
        manager.setConfigDirectory(tempDir);
        manager.clearAll();
    }

    @AfterEach
    void tearDown() {
        manager.clearAll();
    }

    @Test
    void testAddAndQueryFriendByUuid() {
        UUID uuid = UUID.randomUUID();
        FriendEntry entry = new FriendEntry(uuid, "Steve", "MyBuddy", 0xFF55FF55, "★", "[Friend] ", "Best friend");
        manager.addFriend(entry);

        assertTrue(manager.isFriend(uuid, "Steve"));
        assertTrue(manager.isFriend(uuid, "steve"));
        FriendEntry retrieved = manager.getFriend(uuid, null);
        assertNotNull(retrieved);
        assertEquals("MyBuddy", retrieved.getAlias());
        assertEquals("★", retrieved.getFriendMarkerIcon());
        assertEquals(0xFF55FF55, retrieved.getColorArgb());
    }

    @Test
    void testAddAndQueryFriendByUsernameOnly() {
        FriendEntry entry = new FriendEntry(null, "AlexPro", "ProAlex", 0xFF55FFFF, "♦", "", "");
        manager.addFriend(entry);

        assertTrue(manager.isFriend(null, "alexpro"));
        assertTrue(manager.isFriend(null, "AlexPro"));
        assertFalse(manager.isFriend(null, "Unknown"));

        FriendEntry retrieved = manager.getFriend(null, "alexpro");
        assertNotNull(retrieved);
        assertEquals("ProAlex", retrieved.getAlias());
    }

    @Test
    void testRemoveFriend() {
        UUID uuid = UUID.randomUUID();
        FriendEntry entry = new FriendEntry(uuid, "Gamer123", "Gamer", 0xFFFF5555, "★", "", "");
        manager.addFriend(entry);
        assertTrue(manager.isFriend(uuid, "Gamer123"));

        manager.removeFriend(uuid, "Gamer123");
        assertFalse(manager.isFriend(uuid, "Gamer123"));
        assertNull(manager.getFriend(uuid, "Gamer123"));
    }

    @Test
    void testPersistence() {
        UUID uuid = UUID.randomUUID();
        FriendEntry entry = new FriendEntry(uuid, "PersistedUser", "AliasUser", 0xFFFFAA00, "★", "[VIP] ", "Note");
        manager.addFriend(entry);

        // Re-read from disk by creating fresh load
        manager.loadFriends();
        assertTrue(manager.isFriend(uuid, "PersistedUser"));
        FriendEntry reloaded = manager.getFriend(uuid, "PersistedUser");
        assertNotNull(reloaded);
        assertEquals("AliasUser", reloaded.getAlias());
        assertEquals(0xFFFFAA00, reloaded.getColorArgb());
    }

    @Test
    void testClearAll() {
        manager.addFriend(new FriendEntry(UUID.randomUUID(), "U1", "A1", 0xFFFFFFFF, "★", "", ""));
        manager.addFriend(new FriendEntry(UUID.randomUUID(), "U2", "A2", 0xFFFFFFFF, "★", "", ""));
        assertEquals(2, manager.getAllFriends().size());

        manager.clearAll();
        assertEquals(0, manager.getAllFriends().size());
    }
}
