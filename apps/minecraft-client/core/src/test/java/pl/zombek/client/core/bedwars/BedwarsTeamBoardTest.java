package pl.zombek.client.core.bedwars;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BedwarsTeamBoardTest {
    private BedwarsScoreboardParser parser;

    @BeforeEach
    void setUp() {
        parser = new BedwarsScoreboardParser();
    }

    @Test
    void testStandardFourTeamParsing() {
        List<String> lines = Arrays.asList(
                "BED WARS",
                "R Red: ✓",
                "B Blue: ✓",
                "G Green: ✕",
                "Y Yellow: [E]"
        );
        parser.parseLines(lines);

        BedwarsTeam red = parser.getParsedTeams().get(BedwarsTeamColor.RED);
        BedwarsTeam blue = parser.getParsedTeams().get(BedwarsTeamColor.BLUE);
        BedwarsTeam green = parser.getParsedTeams().get(BedwarsTeamColor.GREEN);
        BedwarsTeam yellow = parser.getParsedTeams().get(BedwarsTeamColor.YELLOW);

        assertEquals(BedStatus.BED_ALIVE, red.getBedStatus());
        assertEquals(TeamState.ACTIVE, red.getTeamState());

        assertEquals(BedStatus.BED_ALIVE, blue.getBedStatus());
        assertEquals(TeamState.ACTIVE, blue.getTeamState());

        assertEquals(BedStatus.BED_DESTROYED, green.getBedStatus());
        assertEquals(TeamState.BEDLESS, green.getTeamState());

        assertEquals(BedStatus.TEAM_ELIMINATED, yellow.getBedStatus());
        assertEquals(TeamState.ELIMINATED, yellow.getTeamState());
        assertEquals(0, yellow.getAlivePlayers());
    }

    @Test
    void testNumericPlayerCountWhenBedDestroyed() {
        List<String> lines = Arrays.asList(
                "BED WARS",
                "R Red: 3",
                "B Blue: 1",
                "G Green: 0"
        );
        parser.parseLines(lines);

        BedwarsTeam red = parser.getParsedTeams().get(BedwarsTeamColor.RED);
        BedwarsTeam blue = parser.getParsedTeams().get(BedwarsTeamColor.BLUE);
        BedwarsTeam green = parser.getParsedTeams().get(BedwarsTeamColor.GREEN);

        assertEquals(BedStatus.BED_DESTROYED, red.getBedStatus());
        assertEquals(3, red.getAlivePlayers());
        assertEquals(TeamState.BEDLESS, red.getTeamState());

        assertEquals(BedStatus.BED_DESTROYED, blue.getBedStatus());
        assertEquals(1, blue.getAlivePlayers());

        assertEquals(BedStatus.TEAM_ELIMINATED, green.getBedStatus());
        assertEquals(0, green.getAlivePlayers());
        assertEquals(TeamState.ELIMINATED, green.getTeamState());
    }

    @Test
    void testSelfTeamDetection() {
        List<String> lines = Arrays.asList(
                "BED WARS",
                "R Red: ✓ (you)",
                "B Blue: ✓"
        );
        parser.parseLines(lines);

        assertEquals(BedwarsTeamColor.RED, parser.getPlayerTeamColor());
    }

    @Test
    void testEightTeamParsingForSoloAndDoubles() {
        List<String> lines = Arrays.asList(
                "BED WARS",
                "R: ✓", "B: ✓", "G: ✓", "Y: ✓",
                "A: ✓", "W: ✓", "P: ✓", "S: ✓"
        );
        parser.parseLines(lines);

        for (BedwarsTeamColor color : BedwarsTeamColor.values()) {
            BedwarsTeam team = parser.getParsedTeams().get(color);
            assertNotNull(team);
            assertEquals(BedStatus.BED_ALIVE, team.getBedStatus());
        }
    }

    @Test
    void testBedDestroyedDoesNotImmediatelyEliminate() {
        BedwarsTeam team = new BedwarsTeam(BedwarsTeamColor.RED, "Red", BedStatus.BED_ALIVE, 4);
        assertTrue(team.isBedAlive());
        assertFalse(team.isEliminated());

        team.setBedStatus(BedStatus.BED_DESTROYED);
        assertFalse(team.isBedAlive());
        assertFalse(team.isEliminated()); // Still 4 players alive!
        assertEquals(TeamState.BEDLESS, team.getTeamState());

        team.setAlivePlayers(0);
        assertTrue(team.isEliminated());
        assertEquals(TeamState.ELIMINATED, team.getTeamState());
    }

    @Test
    void testResetRestoresInitialUnknownState() {
        parser.parseLines(Arrays.asList("BED WARS", "R Red: ✓"));
        assertEquals(BedStatus.BED_ALIVE, parser.getParsedTeams().get(BedwarsTeamColor.RED).getBedStatus());

        parser.reset();
        assertEquals(BedStatus.UNKNOWN, parser.getParsedTeams().get(BedwarsTeamColor.RED).getBedStatus());
        assertEquals("N/A", parser.getParsedMap());
    }
}
