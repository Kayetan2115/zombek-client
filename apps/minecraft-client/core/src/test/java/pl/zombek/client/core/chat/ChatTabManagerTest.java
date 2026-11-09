package pl.zombek.client.core.chat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ChatTabManagerTest {

    private ChatTabManager tabManager;

    @BeforeEach
    public void setUp() {
        tabManager = ChatTabManager.getInstance();
        tabManager.initDefaultTabs();
    }

    @Test
    public void testDefaultTabsPresent() {
        List<ChatTab> tabs = tabManager.getTabs();
        assertTrue(tabs.size() >= 5, "Should have default tabs (All, General, Private, Party, Guild, System)");
        assertEquals("all", tabs.get(0).getId());
        assertEquals("all", tabManager.getActiveTabId());
    }

    @Test
    public void testTabSwitching() {
        tabManager.switchTab("private");
        assertEquals("private", tabManager.getActiveTabId());

        ChatTab activeTab = tabManager.getActiveTab();
        assertNotNull(activeTab);
        assertEquals("private", activeTab.getId());
    }

    @Test
    public void testMessageRoutingToTabs() {
        ChatMessageEntry whisperMsg = new ChatMessageEntry("whisper text", "Notch", null, ChatCategory.PRIVATE_MESSAGES, false, null);

        ChatTab privateTab = null;
        ChatTab allTab = null;
        ChatTab partyTab = null;

        for (ChatTab tab : tabManager.getTabs()) {
            if ("private".equalsIgnoreCase(tab.getId())) privateTab = tab;
            if ("all".equalsIgnoreCase(tab.getId())) allTab = tab;
            if ("party".equalsIgnoreCase(tab.getId())) partyTab = tab;
        }

        assertNotNull(privateTab);
        assertNotNull(allTab);
        assertNotNull(partyTab);

        assertTrue(privateTab.matches(whisperMsg), "Private tab should match private messages");
        assertTrue(allTab.matches(whisperMsg), "All tab should match all messages");
        assertFalse(partyTab.matches(whisperMsg), "Party tab should not match whisper messages");
    }

    @Test
    public void testUnreadBadges() {
        tabManager.switchTab("all");

        ChatMessageEntry partyMsg = new ChatMessageEntry("Party > Hi", "Leader", null, ChatCategory.PARTY_MESSAGES, false, null);

        ChatTab partyTab = null;
        for (ChatTab tab : tabManager.getTabs()) {
            if ("party".equalsIgnoreCase(tab.getId())) {
                partyTab = tab;
                break;
            }
        }
        assertNotNull(partyTab);
        int initialUnread = partyTab.getUnreadCount();

        tabManager.dispatchToTabs(partyMsg);

        assertEquals(initialUnread + 1, partyTab.getUnreadCount(), "Unread count should increment on background tab");

        // Switching to party tab resets unread count
        tabManager.switchTab("party");
        assertEquals(0, partyTab.getUnreadCount(), "Unread count should clear when tab becomes active");
    }

    @Test
    public void testCustomTabCreation() {
        ChatTab custom = new ChatTab("trades", "Trades", 0xFFFFAA00, "$", EnumSet.of(ChatCategory.OTHER_MESSAGES), 10, false, true);
        tabManager.addCustomTab(custom);

        ChatMessageEntry customMsg = new ChatMessageEntry("WTS Dragon Sword", "Trader", null, ChatCategory.OTHER_MESSAGES, false, null);
        assertTrue(custom.matches(customMsg));
    }
}
