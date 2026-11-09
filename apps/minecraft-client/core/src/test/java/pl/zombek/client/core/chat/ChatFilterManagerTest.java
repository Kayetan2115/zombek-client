package pl.zombek.client.core.chat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ChatFilterManagerTest {

    private ChatFilterManager filterManager;

    @BeforeEach
    public void setUp() {
        filterManager = ChatFilterManager.getInstance();
        filterManager.initDefaults();
    }

    @Test
    public void testCategoryClassification() {
        assertEquals(ChatCategory.PRIVATE_MESSAGES, filterManager.classifyMessage("from Notch: hello", false));
        assertEquals(ChatCategory.PARTY_MESSAGES, filterManager.classifyMessage("Party > [MVP+] Notch: lets go", false));
        assertEquals(ChatCategory.GUILD_MESSAGES, filterManager.classifyMessage("Guild > Alex: welcome all", false));
        assertEquals(ChatCategory.JOIN_MESSAGES, filterManager.classifyMessage("Steve joined the game", true));
        assertEquals(ChatCategory.LEAVE_MESSAGES, filterManager.classifyMessage("Steve left the game", true));
        assertEquals(ChatCategory.DEATH_MESSAGES, filterManager.classifyMessage("Steve hit the ground too hard", true));
        assertEquals(ChatCategory.SYSTEM_MESSAGES, filterManager.classifyMessage("[ANNOUNCEMENT] Server restart", true));
        assertEquals(ChatCategory.PLAYER_MESSAGES, filterManager.classifyMessage("<Notch> Nice build!", false));
    }

    @Test
    public void testExactFilterRule() {
        UniversalChatFilterRule rule = new UniversalChatFilterRule(
                "Exact Spam", ChatFilterMatchType.EXACT, "spam", null, ChatFilterAction.HIDE_LOCALLY, 10
        );
        filterManager.addRule(rule);

        ChatMessageEntry entry1 = new ChatMessageEntry("spam", "Steve", null, ChatCategory.PLAYER_MESSAGES, false, null);
        filterManager.evaluateMessage(entry1);
        assertTrue(entry1.isHidden(), "Exact match should hide 'spam'");

        ChatMessageEntry entry2 = new ChatMessageEntry("not spam", "Steve", null, ChatCategory.PLAYER_MESSAGES, false, null);
        filterManager.evaluateMessage(entry2);
        assertFalse(entry2.isHidden(), "Different text should not be hidden");
    }

    @Test
    public void testContainsFilterRule() {
        UniversalChatFilterRule rule = new UniversalChatFilterRule(
                "Keyword Block", ChatFilterMatchType.CONTAINS, "free ranks", null, ChatFilterAction.HIDE_LOCALLY, 10
        );
        filterManager.addRule(rule);

        ChatMessageEntry entry = new ChatMessageEntry("Visit scam.com for free ranks!", "Scammer", null, ChatCategory.PLAYER_MESSAGES, false, null);
        filterManager.evaluateMessage(entry);
        assertTrue(entry.isHidden(), "Contains match should hide message");
    }

    @Test
    public void testRegexFilterRule() {
        UniversalChatFilterRule rule = new UniversalChatFilterRule(
                "Discord Link", ChatFilterMatchType.REGEX, "https?://discord\\.gg/[a-zA-Z0-9]+", null, ChatFilterAction.HIDE_LOCALLY, 10
        );
        filterManager.addRule(rule);

        ChatMessageEntry entry = new ChatMessageEntry("Join my server: https://discord.gg/abc1234", "Advertiser", null, ChatCategory.PLAYER_MESSAGES, false, null);
        filterManager.evaluateMessage(entry);
        assertTrue(entry.isHidden(), "Regex match should trigger hide action");
    }

    @Test
    public void testReDosProtectionDoesNotHang() {
        // Evil ReDoS pattern: (a+)+$
        UniversalChatFilterRule rule = new UniversalChatFilterRule(
                "ReDoS Test", ChatFilterMatchType.REGEX, "(a+)+$", null, ChatFilterAction.HIDE_LOCALLY, 10
        );
        filterManager.addRule(rule);

        String evilInput = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaX";
        ChatMessageEntry entry = new ChatMessageEntry(evilInput, "Attacker", null, ChatCategory.PLAYER_MESSAGES, false, null);

        long start = System.currentTimeMillis();
        filterManager.evaluateMessage(entry);
        long duration = System.currentTimeMillis() - start;

        assertTrue(duration < 500, "ReDoS evaluation should terminate safely within timeout, took: " + duration + "ms");
    }

    @Test
    public void testFilterActionHighlightAndColor() {
        UniversalChatFilterRule rule = new UniversalChatFilterRule(
                "Highlight Admin", ChatFilterMatchType.CONTAINS, "[ADMIN]", null, ChatFilterAction.HIGHLIGHT, 20
        );
        rule.setHighlightColorArgb(0xFFFF0000);
        filterManager.addRule(rule);

        ChatMessageEntry entry = new ChatMessageEntry("[ADMIN] Hypixel: Maintenance scheduled", "Admin", null, ChatCategory.SYSTEM_MESSAGES, true, null);
        filterManager.evaluateMessage(entry);

        assertFalse(entry.isHidden());
        assertTrue(entry.isHighlighted());
        assertEquals(0xFFFF0000, entry.getHighlightColorArgb());
    }

    @Test
    public void testFilterMetrics() {
        UniversalChatFilterRule rule = new UniversalChatFilterRule(
                "Spam", ChatFilterMatchType.CONTAINS, "advert", null, ChatFilterAction.HIDE_LOCALLY, 10
        );
        filterManager.addRule(rule);

        ChatMessageEntry entry = new ChatMessageEntry("This is an advert for a clan", "User", null, ChatCategory.PLAYER_MESSAGES, false, null);
        filterManager.evaluateMessage(entry);

        assertEquals(1, filterManager.getHiddenMessagesCount());
    }
}
