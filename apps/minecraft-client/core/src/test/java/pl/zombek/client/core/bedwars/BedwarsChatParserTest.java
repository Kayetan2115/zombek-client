package pl.zombek.client.core.bedwars;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BedwarsChatParserTest {
    private BedwarsManager manager;
    private BedwarsChatParser chatParser;

    @BeforeEach
    void setUp() {
        manager = new BedwarsManager();
        manager.reset();
        chatParser = new BedwarsChatParser(manager);
    }

    @Test
    void testHypixelBedDestructionMessage() {
        chatParser.onChatMessage("BED DESTRUCTION > Red Bed was destroyed by Steve!");

        BedwarsTeam red = manager.getScoreboardParser().getParsedTeams().get(BedwarsTeamColor.RED);
        assertEquals(BedStatus.BED_DESTROYED, red.getBedStatus());
        assertTrue(red.getLastKnownEvent().contains("Steve"));

        assertEquals(1, manager.getEventTracker().getVisibleEvents().size());
        assertEquals("RED BED DESTROYED", manager.getEventTracker().getVisibleEvents().get(0).getTitle());
    }

    @Test
    void testPolishBedDestructionMessage() {
        chatParser.onChatMessage("Łóżko drużyny Niebiescy zostało zniszczone przez Alex!");

        BedwarsTeam blue = manager.getScoreboardParser().getParsedTeams().get(BedwarsTeamColor.BLUE);
        assertEquals(BedStatus.BED_DESTROYED, blue.getBedStatus());

        assertEquals(1, manager.getEventTracker().getVisibleEvents().size());
        assertEquals("BLUE BED DESTROYED", manager.getEventTracker().getVisibleEvents().get(0).getTitle());
    }

    @Test
    void testHypixelTeamEliminatedMessage() {
        chatParser.onChatMessage("TEAM ELIMINATED > Green Team has been eliminated!");

        BedwarsTeam green = manager.getScoreboardParser().getParsedTeams().get(BedwarsTeamColor.GREEN);
        assertEquals(BedStatus.TEAM_ELIMINATED, green.getBedStatus());
        assertEquals(0, green.getAlivePlayers());
    }

    @Test
    void testPolishTeamEliminatedMessage() {
        chatParser.onChatMessage("Drużyna Żółci została wyeliminowana!");

        BedwarsTeam yellow = manager.getScoreboardParser().getParsedTeams().get(BedwarsTeamColor.YELLOW);
        assertEquals(BedStatus.TEAM_ELIMINATED, yellow.getBedStatus());
        assertEquals(0, yellow.getAlivePlayers());
    }

    @Test
    void testFinalKillMessage() {
        chatParser.onChatMessage("Enemy1 was knocked into the void by Killer1. FINAL KILL!");

        assertEquals(1, manager.getEventTracker().getVisibleEvents().size());
        assertEquals("FINAL KILL: Enemy1", manager.getEventTracker().getVisibleEvents().get(0).getTitle());
    }

    @Test
    void testRegularKillMessage() {
        chatParser.onChatMessage("Enemy2 was killed by Killer2.");

        assertEquals(1, manager.getEventTracker().getVisibleEvents().size());
        assertEquals("Enemy2 zabity", manager.getEventTracker().getVisibleEvents().get(0).getTitle());
    }

    @Test
    void testMatchVictoryMessage() {
        chatParser.onChatMessage("VICTORY!");

        assertEquals(BedwarsGameState.GAME_ENDING, manager.getGameDetector().getCurrentState());
        assertEquals(1, manager.getSessionTracker().getConfirmedWins());
    }

    @Test
    void testArbitraryChatDoesNotTriggerEvents() {
        chatParser.onChatMessage("Hey, do you want to trade some emeralds?");
        assertEquals(0, manager.getEventTracker().getVisibleEvents().size());
        assertEquals(0, manager.getSessionTracker().getObservedKills());
    }
}
