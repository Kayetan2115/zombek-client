package pl.zombek.client.core.chat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.friends.FriendEntry;
import pl.zombek.client.core.friends.NameTagFriendManager;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ChatNotificationControllerTest {

    private ChatNotificationController notificationController;

    @BeforeEach
    public void setUp() {
        notificationController = ChatNotificationController.getInstance();
        notificationController.clearKeywords();
        notificationController.resetUnread();
        notificationController.setEnableMentions(true);
        notificationController.setEnableKeywordAlerts(true);
        notificationController.setPlaySound(true);
        NameTagFriendManager.getInstance().clear();
    }

    @Test
    public void testOwnPlayerMention() {
        ChatMessageEntry entry = new ChatMessageEntry("Hey ZombekPlayer, come over here!", "Alice", null, ChatCategory.PLAYER_MESSAGES, false, null);
        boolean notified = notificationController.evaluate(entry, "ZombekPlayer");

        assertTrue(notified, "Player name mention should trigger notification");
        assertTrue(entry.isHighlighted(), "Entry should be highlighted");
        assertTrue(entry.isImportant(), "Entry should be marked important");
    }

    @Test
    public void testFriendAliasMention() {
        UUID friendUuid = UUID.fromString("00000000-0000-0000-0000-000000000002");
        FriendEntry friend = new FriendEntry(friendUuid, "RealFriendName", "Bestie", 0xFF00FF00, "★", "", "");
        NameTagFriendManager.getInstance().addFriend(friend);

        ChatMessageEntry entry = new ChatMessageEntry("Did you see Bestie around?", "Alice", null, ChatCategory.PLAYER_MESSAGES, false, null);
        boolean notified = notificationController.evaluate(entry, "ZombekPlayer");

        assertTrue(notified, "Friend alias mention should trigger notification");
        assertTrue(entry.isHighlighted());
    }

    @Test
    public void testCustomKeywordMention() {
        notificationController.addKeyword("dragon");
        notificationController.addKeyword("teleport");

        ChatMessageEntry entry = new ChatMessageEntry("The dragon has spawned in end!", "Server", null, ChatCategory.SYSTEM_MESSAGES, true, null);
        boolean notified = notificationController.evaluate(entry, "ZombekPlayer");

        assertTrue(notified, "Custom keyword should trigger notification");
        assertTrue(entry.isHighlighted(), "Entry matching custom keyword should be highlighted");
    }

    @Test
    public void testAudioNotificationCooldown() {
        notificationController.setPlaySound(true);
        notificationController.setSoundCooldownSeconds(10);

        boolean sound1 = notificationController.shouldPlaySound();
        assertTrue(sound1, "First notification should play sound");

        // Immediately subsequent call within 10 seconds cooldown
        boolean sound2 = notificationController.shouldPlaySound();
        assertFalse(sound2, "Audio notification should be on cooldown");
    }

    @Test
    public void testUnreadCounter() {
        ChatMessageEntry entry = new ChatMessageEntry("Hello ZombekPlayer", "Alice", null, ChatCategory.PLAYER_MESSAGES, false, null);
        notificationController.evaluate(entry, "ZombekPlayer");

        assertEquals(1, notificationController.getTotalUnreadNotifications());
        notificationController.resetUnread();
        assertEquals(0, notificationController.getTotalUnreadNotifications());
    }
}
