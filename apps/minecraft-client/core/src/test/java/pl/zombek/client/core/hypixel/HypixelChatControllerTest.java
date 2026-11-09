package pl.zombek.client.core.hypixel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HypixelChatControllerTest {
    private HypixelChatController controller;

    @BeforeEach
    void setUp() {
        controller = new HypixelChatController();
        controller.setEnableShortChannelNames(true);
        controller.setPrefixStyle(ChannelPrefixStyle.BRACKETED);
        controller.setPreserveOriginalFormatting(true);
        controller.setHideGuildMotd(true);
        controller.setHideGuildOnTab(true);
        controller.setShowTimestamps(false);
    }

    @Test
    void testShortChannelNamesBracketed() {
        String input = "Party > Steve: Let's play Bedwars!";
        String formatted = controller.formatChatMessage(input);
        assertEquals("[P] Steve: Let's play Bedwars!", formatted);

        String guildInput = "Guild > Alex: Good morning everyone!";
        assertEquals("[G] Alex: Good morning everyone!", controller.formatChatMessage(guildInput));

        String officerInput = "Officer > Mod1: Meeting at 5pm";
        assertEquals("[O] Mod1: Meeting at 5pm", controller.formatChatMessage(officerInput));
    }

    @Test
    void testShortChannelNamesPreservesColors() {
        String input = "§9Party > §fSteve: Hello!";
        String formatted = controller.formatChatMessage(input);
        assertEquals("§9[P] §fSteve: Hello!", formatted);
    }

    @Test
    void testPrefixStyles() {
        controller.setPrefixStyle(ChannelPrefixStyle.COLON);
        assertEquals("P: Steve: Hi", controller.formatChatMessage("Party > Steve: Hi"));

        controller.setPrefixStyle(ChannelPrefixStyle.MINIMAL);
        assertEquals("P > Steve: Hi", controller.formatChatMessage("Party > Steve: Hi"));

        controller.setPrefixStyle(ChannelPrefixStyle.PLAIN);
        assertEquals("P Steve: Hi", controller.formatChatMessage("Party > Steve: Hi"));
    }

    @Test
    void testGuildMotdDetection() {
        assertTrue(controller.isGuildMotd("Guild Message of the Day: Welcome to our guild!"));
        assertTrue(controller.isGuildMotd("Guild MOTD: Check Discord!"));
        assertTrue(controller.isGuildMotd("-----------------------------------------------------"));

        assertFalse(controller.isGuildMotd("Steve: Hello guild mates!"));
    }

    @Test
    void testHideGuildOnTablist() {
        assertEquals("Steve", controller.cleanTablistName("Steve [PRO]"));
        assertEquals("§a[MVP+] §fAlex", controller.cleanTablistName("§a[MVP+] §fAlex [WAR]"));
        assertEquals("Player1", controller.cleanTablistName("Player1"));
    }

    @Test
    void testTimestampsFormatting() {
        controller.setShowTimestamps(true);
        controller.setTimestampFormat("HH:mm");
        String formatted = controller.formatChatMessage("Hello world!");
        assertTrue(formatted.matches("^§7\\[\\d{2}:\\d{2}\\] §rHello world!$"));
    }
}
