package pl.zombek.client.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.uhc.UhcScoreboardParser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UhcScoreboardParserTest {

    private UhcScoreboardParser parser;

    @BeforeEach
    public void setUp() {
        parser = new UhcScoreboardParser();
    }

    @Test
    public void testDefaultNAValues() {
        assertEquals("N/A", parser.getMatchName());
        assertEquals("N/A", parser.getMatchStatus());
        assertEquals("N/A", parser.getMatchTime());
        assertEquals("N/A", parser.getAlivePlayers());
        assertEquals("N/A", parser.getTeamCount());
        assertEquals("N/A", parser.getBorderStatus());
    }

    @Test
    public void testParseEnglishScoreboard() {
        List<String> lines = Arrays.asList(
                "§6§lHYPIXEL UHC§r",
                "§703/11/26§r",
                "Time: 24:15",
                "Players: 42/100",
                "Teams: 14",
                "Border: 500m",
                "Phase: Deathmatch"
        );

        parser.parseLines(lines);

        assertEquals("HYPIXEL UHC", parser.getMatchName());
        assertEquals("24:15", parser.getMatchTime());
        assertEquals("42/100", parser.getAlivePlayers());
        assertEquals("14", parser.getTeamCount());
        assertEquals("500m", parser.getBorderStatus());
        assertEquals("Deathmatch", parser.getMatchStatus());
    }

    @Test
    public void testParsePolishScoreboard() {
        List<String> lines = Arrays.asList(
                "§c§lUHC POLSKA",
                "Żywi: 18",
                "Drużyny: 6",
                "Granica: 250",
                "Czas: 45:10"
        );

        parser.parseLines(lines);

        assertEquals("UHC POLSKA", parser.getMatchName());
        assertEquals("18", parser.getAlivePlayers());
        assertEquals("6", parser.getTeamCount());
        assertEquals("250", parser.getBorderStatus());
        assertEquals("45:10", parser.getMatchTime());
    }

    @Test
    public void testResetCleansState() {
        parser.parseLines(Collections.singletonList("Players: 10"));
        assertEquals("10", parser.getAlivePlayers());

        parser.reset();
        assertEquals("N/A", parser.getAlivePlayers());
    }
}
