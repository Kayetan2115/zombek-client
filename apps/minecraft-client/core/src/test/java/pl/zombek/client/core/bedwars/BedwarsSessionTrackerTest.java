package pl.zombek.client.core.bedwars;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BedwarsSessionTrackerTest {
    private BedwarsSessionTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new BedwarsSessionTracker();
    }

    @Test
    void testInitialCountersAreZero() {
        assertEquals(0, tracker.getObservedKills());
        assertEquals(0, tracker.getConfirmedFinalKills());
        assertEquals(0, tracker.getObservedDeaths());
        assertEquals(0, tracker.getConfirmedBedsBroken());
        assertEquals(0, tracker.getConfirmedWins());
        assertEquals(0, tracker.getConfirmedLosses());
    }

    @Test
    void testRecordObservedKill() {
        tracker.recordObservedKill("Player1");
        tracker.recordObservedKill("Player2");

        assertEquals(2, tracker.getObservedKills());
        assertEquals(0, tracker.getConfirmedFinalKills());
    }

    @Test
    void testRecordConfirmedFinalKillIncrementsBoth() {
        tracker.recordConfirmedFinalKill("Player3");

        assertEquals(1, tracker.getConfirmedFinalKills());
        assertEquals(1, tracker.getObservedKills());
    }

    @Test
    void testDeduplicationPreventsDoubleCounting() {
        // Two reports for same victim within dedup window
        tracker.recordObservedKill("Player1");
        tracker.recordObservedKill("Player1");

        assertEquals(1, tracker.getObservedKills());
    }

    @Test
    void testRecordBedBroken() {
        tracker.recordConfirmedBedBroken(BedwarsTeamColor.RED);
        tracker.recordConfirmedBedBroken(BedwarsTeamColor.BLUE);
        // Duplicate report for blue bed
        tracker.recordConfirmedBedBroken(BedwarsTeamColor.BLUE);

        assertEquals(2, tracker.getConfirmedBedsBroken());
    }

    @Test
    void testVictoryAndDefeatArchive() {
        tracker.recordObservedKill("Enemy1");
        tracker.recordConfirmedFinalKill("Enemy2");
        tracker.recordConfirmedBedBroken(BedwarsTeamColor.RED);
        tracker.recordVictory();

        assertEquals(1, tracker.getConfirmedWins());
        assertEquals(1, tracker.getMatchHistory().size());
        assertTrue(tracker.getMatchHistory().get(0).isVictory());
        assertEquals(2, tracker.getMatchHistory().get(0).getKills());

        // New match counters reset automatically if autoReset is enabled
        assertEquals(0, tracker.getObservedKills());
        assertEquals(0, tracker.getConfirmedFinalKills());
    }

    @Test
    void testHistoryLimitRetention() {
        tracker.setMaxStoredSessions(3);
        tracker.recordVictory();
        tracker.recordDefeat();
        tracker.recordVictory();
        tracker.recordDefeat();

        assertEquals(3, tracker.getMatchHistory().size());
    }
}
