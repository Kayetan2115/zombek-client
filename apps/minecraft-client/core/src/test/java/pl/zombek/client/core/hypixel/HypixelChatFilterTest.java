package pl.zombek.client.core.hypixel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HypixelChatFilterTest {
    private HypixelChatFilter filter;

    @BeforeEach
    void setUp() {
        filter = new HypixelChatFilter();
        filter.resetFilteredCount();
        filter.clearCustomRules();
        filter.setEnabled(true);
    }

    @Test
    void testFilterPrivateMessages() {
        filter.setFilterPrivateMessages(true);
        assertTrue(filter.shouldFilter("From Steve: Hey are you there?"));
        assertTrue(filter.shouldFilter("To Alex: Yes I am here"));
        assertEquals(2, filter.getFilteredCount());

        filter.setFilterPrivateMessages(false);
        assertFalse(filter.shouldFilter("From Steve: Hello"));
    }

    @Test
    void testFilterPartyAndGuildChat() {
        filter.setFilterPartyChat(true);
        filter.setFilterGuildChat(true);

        assertTrue(filter.shouldFilter("Party > Steve: Let's warp"));
        assertTrue(filter.shouldFilter("Guild > Alex: Good job"));
        assertFalse(filter.shouldFilter("Team > Player: Defend the bed!"));
    }

    @Test
    void testFilterLobbySpamAndMysteryBox() {
        filter.setFilterLobbyStatusMessages(true);
        filter.setFilterMysteryBoxAnnouncements(true);
        filter.setFilterSoulWellAnnouncements(true);

        assertTrue(filter.shouldFilter("Steve has joined the lobby!"));
        assertTrue(filter.shouldFilter("Player1 found a 5-Star Mystery Box!"));
        assertTrue(filter.shouldFilter("Player2 found an Epic Hat in the Soul Well!"));

        assertFalse(filter.shouldFilter("Steve: Want to play Bedwars?"));
    }

    @Test
    void testCustomRulesContainsAndExact() {
        filter.addCustomRule(new ChatFilterRule(
                "FilterGG",
                ChatFilterRule.MatchType.CONTAINS,
                "discord.gg",
                ChatFilterCategory.CUSTOM,
                true
        ));
        filter.addCustomRule(new ChatFilterRule(
                "ExactSpam",
                ChatFilterRule.MatchType.EXACT,
                "spam",
                ChatFilterCategory.CUSTOM,
                true
        ));

        assertTrue(filter.shouldFilter("Join my server at discord.gg/fake!"));
        assertTrue(filter.shouldFilter("spam"));
        assertFalse(filter.shouldFilter("not a spam message"));
    }

    @Test
    void testCustomRuleRegexWithReDoSGuard() {
        // Safe regex
        ChatFilterRule safeRule = new ChatFilterRule(
                "NumberSpam",
                ChatFilterRule.MatchType.REGEX,
                "\\d{4,}",
                ChatFilterCategory.CUSTOM,
                true
        );
        filter.addCustomRule(safeRule);
        assertFalse(safeRule.isInvalidRegex());
        assertTrue(filter.shouldFilter("Call me at 12345678!"));

        // Catastrophic backtracking regex: e.g. (a+)+
        ChatFilterRule maliciousRule = new ChatFilterRule(
                "EvilRegex",
                ChatFilterRule.MatchType.REGEX,
                "(a+)+",
                ChatFilterCategory.CUSTOM,
                true
        );
        assertTrue(maliciousRule.isInvalidRegex());
        assertFalse(filter.shouldFilter("aaaaaaaaaaaaaaaaaaaaaaaa!"));
    }

    @Test
    void testDisableAllFilters() {
        filter.setFilterPrivateMessages(true);
        filter.setFilterLobbyStatusMessages(true);

        filter.disableAllFilters();
        assertFalse(filter.isFilterPrivateMessages());
        assertFalse(filter.isFilterLobbyStatusMessages());
        assertFalse(filter.shouldFilter("From Steve: Test"));
    }
}
