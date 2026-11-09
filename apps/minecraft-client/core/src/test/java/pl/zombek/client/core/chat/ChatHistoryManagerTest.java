package pl.zombek.client.core.chat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ChatHistoryManagerTest {

    private ChatHistoryManager historyManager;

    @BeforeEach
    public void setUp() {
        historyManager = ChatHistoryManager.getInstance();
        historyManager.clearHistory();
        historyManager.setPrivacyMode(false);
        historyManager.setStreamerMode(false);
        historyManager.setMaxMessages(1000);
    }

    @Test
    public void testRingBufferLimit() {
        historyManager.setMaxMessages(50); // Minimum enforced in setMaxMessages is 50
        for (int i = 1; i <= 60; i++) {
            ChatMessageEntry entry = new ChatMessageEntry("Message " + i, "Sender" + i, null, ChatCategory.PLAYER_MESSAGES, false, null);
            historyManager.addMessage(entry);
        }

        List<ChatMessageEntry> all = historyManager.getMessages();
        assertEquals(50, all.size(), "History should not exceed capacity 50");
        assertEquals("Message 11", all.get(0).getCleanText());
        assertEquals("Message 60", all.get(49).getCleanText());
    }

    @Test
    public void testSearchByContent() {
        historyManager.addMessage(new ChatMessageEntry("Hello world!", "Alice", null, ChatCategory.PLAYER_MESSAGES, false, null));
        historyManager.addMessage(new ChatMessageEntry("Goodbye everyone!", "Bob", null, ChatCategory.PLAYER_MESSAGES, false, null));
        historyManager.addMessage(new ChatMessageEntry("World of Warcraft", "Charlie", null, ChatCategory.PLAYER_MESSAGES, false, null));

        List<ChatMessageEntry> results = historyManager.search("world", null, null, 0, 0);
        assertEquals(2, results.size());
    }

    @Test
    public void testSearchByCategory() {
        ChatMessageEntry msg1 = new ChatMessageEntry("System alert", null, null, ChatCategory.SYSTEM_MESSAGES, true, null);
        historyManager.addMessage(msg1);

        ChatMessageEntry msg2 = new ChatMessageEntry("Player chat", "Dave", null, ChatCategory.PLAYER_MESSAGES, false, null);
        historyManager.addMessage(msg2);

        List<ChatMessageEntry> systemResults = historyManager.search(null, null, ChatCategory.SYSTEM_MESSAGES, 0, 0);
        assertEquals(1, systemResults.size());
        assertEquals("System alert", systemResults.get(0).getCleanText());
    }

    @Test
    public void testCredentialSanitization() {
        ChatMessageEntry loginMsg = new ChatMessageEntry("/login SuperSecretPassword123!", "Player", null, ChatCategory.COMMAND_FEEDBACK, false, null);
        historyManager.addMessage(loginMsg);

        List<ChatMessageEntry> all = historyManager.getMessages();
        assertEquals(0, all.size(), "Authentication commands containing passwords must never be stored in history");
    }

    @Test
    public void testPrivacyAndStreamerModeExport() {
        ChatMessageEntry msg = new ChatMessageEntry("Hello secret server", "SecretFriend", null, ChatCategory.PLAYER_MESSAGES, false, null);
        historyManager.addMessage(msg);

        historyManager.setPrivacyMode(true);
        historyManager.setStreamerMode(true);

        String exported = historyManager.exportHistoryToText(TimestampFormat.HH_MM);
        assertNotNull(exported);
        assertFalse(exported.contains("SecretFriend"), "Sender name should be redacted in streamer mode");
        assertTrue(exported.contains("[SENDER REDACTED]"));
    }

    @Test
    public void testClearOnDisconnect() {
        historyManager.addMessage(new ChatMessageEntry("Test", "User", null, ChatCategory.PLAYER_MESSAGES, false, null));
        assertEquals(1, historyManager.getMessages().size());

        historyManager.setClearOnDisconnect(true);
        historyManager.onDisconnect();
        assertEquals(0, historyManager.getMessages().size(), "History should be wiped on disconnect for security");
    }
}
