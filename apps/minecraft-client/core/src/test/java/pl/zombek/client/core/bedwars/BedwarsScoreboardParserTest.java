package pl.zombek.client.core.bedwars;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BedwarsScoreboardParserTest {
    private BedwarsScoreboardParser parser;

    @BeforeEach
    void setUp() {
        parser = new BedwarsScoreboardParser();
    }

    @Test
    void testParseHypixelScoreboardFormat() {
        List<String> lines = Arrays.asList(
                "§e§lBED WARS",
                "§711/03/24 §8m123A",
                "",
                "Diamond II in §a01:30",
                "Emerald II in §a03:45",
                "",
                "§cR §fRed: §a✓ §7(you)",
                "§9B §fBlue: §a✓",
                "§aG §fGreen: §c✕",
                "§eY §fYellow: §c[E]",
                "",
                "Kills: §a4",
                "Final Kills: §a2",
                "Beds Broken: §a1",
                "",
                "Map: §aLighthouse",
                "Mode: §aDoubles",
                "",
                "§ewww.hypixel.net"
        );
        parser.parseLines(lines);

        assertEquals("Lighthouse", parser.getParsedMap());
        assertEquals(BedwarsMode.DOUBLES, parser.getParsedMode());
        assertEquals(BedwarsTeamColor.RED, parser.getPlayerTeamColor());

        assertEquals(4, parser.getScoreboardKills());
        assertEquals(2, parser.getScoreboardFinalKills());
        assertEquals(1, parser.getScoreboardBedsBroken());

        assertEquals(2, parser.getParsedTimers().size());
        assertEquals("Diamond II", parser.getParsedTimers().get(0).getName());
        assertEquals(90, parser.getParsedTimers().get(0).getRemainingSeconds());

        BedwarsTeam red = parser.getParsedTeams().get(BedwarsTeamColor.RED);
        assertEquals(BedStatus.BED_ALIVE, red.getBedStatus());

        BedwarsTeam green = parser.getParsedTeams().get(BedwarsTeamColor.GREEN);
        assertEquals(BedStatus.BED_DESTROYED, green.getBedStatus());

        BedwarsTeam yellow = parser.getParsedTeams().get(BedwarsTeamColor.YELLOW);
        assertEquals(BedStatus.TEAM_ELIMINATED, yellow.getBedStatus());
    }

    @Test
    void testParseCachingIdenticalLines() {
        List<String> lines = Arrays.asList("BED WARS", "Map: Speedway");
        parser.parseLines(lines);
        assertEquals("Speedway", parser.getParsedMap());

        // Second call with same lines should be a no-op / cache hit
        parser.parseLines(lines);
        assertEquals("Speedway", parser.getParsedMap());
    }

    @Test
    void testCustomPatternsParsing() {
        parser.setProfile(ParserProfile.CUSTOM);
        parser.setCustomPatterns("(?i)Team\\s+(\\w+)\\s+state:\\s+(\\w+)");

        List<String> lines = Arrays.asList(
                "CUSTOM BEDWARS",
                "Team Red state: alive",
                "Team Blue state: destroyed"
        );
        parser.parseLines(lines);

        assertEquals(BedStatus.BED_ALIVE, parser.getParsedTeams().get(BedwarsTeamColor.RED).getBedStatus());
        assertEquals(BedStatus.BED_DESTROYED, parser.getParsedTeams().get(BedwarsTeamColor.BLUE).getBedStatus());
    }

    @Test
    void testCorruptedLinesDoNotCrash() {
        assertDoesNotThrow(() -> {
            parser.parseLines(null);
            parser.parseLines(Collections.emptyList());
            parser.parseLines(Arrays.asList("§§§§", "", "???:::---", null));
        });
    }
}
