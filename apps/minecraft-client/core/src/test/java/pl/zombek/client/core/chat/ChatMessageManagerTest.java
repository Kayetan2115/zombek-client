package pl.zombek.client.core.chat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ChatMessageManagerTest {

    private ChatMessageManager messageManager;

    @BeforeEach
    public void setUp() {
        messageManager = ChatMessageManager.getInstance();
        messageManager.setEnableRepeatedMessageGrouping(true);
        messageManager.setShowTimestamps(true);
        messageManager.setTimestampFormat(TimestampFormat.HH_MM);
        ChatHistoryManager.getInstance().clearHistory();
    }

    @Test
    public void testRepeatedMessageGrouping() {
        ChatMessageEntry entry1 = messageManager.processIncoming("Hello there!", "Steve", null, false, null, "Me");
        assertNotNull(entry1);
        assertEquals(1, entry1.getRepeatCount());
        assertEquals("Hello there!", entry1.getCleanText());

        ChatMessageEntry entry2 = messageManager.processIncoming("Hello there!", "Steve", null, false, null, "Me");
        assertNotNull(entry2);
        assertSame(entry1, entry2, "Repeated message should return the existing grouped entry");
        assertEquals(2, entry2.getRepeatCount(), "Repeated identical message should increment repeat count");

        ChatMessageEntry entry3 = messageManager.processIncoming("Hello there!", "Steve", null, false, null, "Me");
        assertEquals(3, entry3.getRepeatCount());

        // Different message creates a new entry
        ChatMessageEntry entry4 = messageManager.processIncoming("Something else", "Steve", null, false, null, "Me");
        assertNotSame(entry1, entry4);
        assertEquals(1, entry4.getRepeatCount());
    }

    @Test
    public void testTimestampFormatting() {
        messageManager.setTimestampFormat(TimestampFormat.HH_MM);
        long now = 1700000000000L;
        String formatted = messageManager.formatTimestamp(now);

        assertNotNull(formatted);
        assertFalse(formatted.isEmpty());
        assertTrue(formatted.matches("\\d{2}:\\d{2}"), "Should format as HH:mm");
    }

    @Test
    public void testDisableTimestamps() {
        messageManager.setShowTimestamps(false);
        String formatted = messageManager.formatTimestamp(System.currentTimeMillis());
        assertEquals("", formatted, "Should return empty string when timestamps disabled");
    }

    @Test
    public void testHistoryStorageThroughMessageManager() {
        messageManager.processIncoming("Line 1", "Steve", null, false, null, "Me");
        messageManager.processIncoming("Line 2", "Steve", null, false, null, "Me");
        messageManager.processIncoming("Line 3", "Steve", null, false, null, "Me");

        List<ChatMessageEntry> activeMessages = ChatHistoryManager.getInstance().getMessages();
        assertEquals(3, activeMessages.size());
    }
}
