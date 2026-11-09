package pl.zombek.client.core.bedwars;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BedwarsGameDetectorTest {
    private BedwarsGameDetector detector;

    @BeforeEach
    void setUp() {
        detector = new BedwarsGameDetector();
    }

    @Test
    void testInitialStateIsUnknown() {
        assertEquals(BedwarsGameState.UNKNOWN, detector.getCurrentState());
        assertEquals(DetectionConfidence.UNKNOWN, detector.getConfidence());
        assertEquals("N/A", detector.getDetectedMap());
        assertEquals(BedwarsMode.OTHER, detector.getDetectedMode());
    }

    @Test
    void testWaitingLobbyDetection() {
        List<String> lines = Arrays.asList(
                "BED WARS",
                "11/03/24 m123A",
                "Waiting for players... (4/16)",
                "Map: Speedway",
                "Mode: 4v4v4v4"
        );
        detector.evaluateScoreboard("BED WARS", lines);

        assertEquals(BedwarsGameState.WAITING, detector.getCurrentState());
        assertEquals(DetectionConfidence.HIGH, detector.getConfidence());
        assertEquals("Speedway", detector.getDetectedMap());
        assertEquals(BedwarsMode.FOUR_V_FOUR_V_FOUR_V_FOUR, detector.getDetectedMode());
    }

    @Test
    void testStartingCountdownDetection() {
        List<String> lines = Arrays.asList(
                "BED WARS",
                "11/03/24 m123A",
                "Starting in 5s",
                "Map: Lighthouse",
                "Mode: Doubles"
        );
        detector.evaluateScoreboard("BED WARS", lines);

        assertEquals(BedwarsGameState.STARTING, detector.getCurrentState());
        assertEquals(DetectionConfidence.HIGH, detector.getConfidence());
        assertEquals("Lighthouse", detector.getDetectedMap());
        assertEquals(BedwarsMode.DOUBLES, detector.getDetectedMode());
    }

    @Test
    void testActiveMatchDetection() {
        List<String> lines = Arrays.asList(
                "BED WARS",
                "11/03/24 m123A",
                "Diamond II in 01:45",
                "R Red: ✓",
                "B Blue: ✓",
                "G Green: 2",
                "Y Yellow: [E]",
                "Map: Airshow",
                "Mode: 4v4v4v4"
        );
        detector.evaluateScoreboard("BED WARS", lines);

        assertEquals(BedwarsGameState.BEDWARS_ACTIVE, detector.getCurrentState());
        assertEquals(DetectionConfidence.HIGH, detector.getConfidence());
        assertEquals("Airshow", detector.getDetectedMap());
        assertEquals(BedwarsMode.FOUR_V_FOUR_V_FOUR_V_FOUR, detector.getDetectedMode());
    }

    @Test
    void testSuddenDeathDetection() {
        List<String> lines = Arrays.asList(
                "BED WARS",
                "Sudden Death: 05:00",
                "R Red: 1",
                "B Blue: 1"
        );
        detector.evaluateScoreboard("BED WARS", lines);

        assertEquals(BedwarsGameState.SUDDEN_DEATH, detector.getCurrentState());
        assertEquals(DetectionConfidence.HIGH, detector.getConfidence());
    }

    @Test
    void testGameEndedDetection() {
        List<String> lines = Arrays.asList(
                "BED WARS",
                "Game Over!",
                "Winner: Red"
        );
        detector.evaluateScoreboard("BED WARS", lines);

        assertEquals(BedwarsGameState.GAME_ENDED, detector.getCurrentState());
        assertEquals(DetectionConfidence.HIGH, detector.getConfidence());
    }

    @Test
    void testNonBedwarsScoreboardDetection() {
        List<String> lines = Arrays.asList(
                "SKYWARS",
                "Players: 12",
                "Kills: 2"
        );
        detector.evaluateScoreboard("SKYWARS", lines);

        assertEquals(BedwarsGameState.NOT_IN_BEDWARS, detector.getCurrentState());
        assertEquals(DetectionConfidence.HIGH, detector.getConfidence());
    }

    @Test
    void testGenericBedwarsWithoutBedwarsInTitle() {
        List<String> lines = Arrays.asList(
                "SERVER NAME",
                "R: ✓",
                "B: ✕"
        );
        detector.evaluateScoreboard("SERVER NAME", lines);

        assertEquals(BedwarsGameState.BEDWARS_ACTIVE, detector.getCurrentState());
        assertEquals(DetectionConfidence.MEDIUM, detector.getConfidence());
    }

    @Test
    void testChatSignalLowConfidence() {
        detector.evaluateChatMessage("Join Bedwars lobby 1!");
        assertEquals(DetectionConfidence.LOW, detector.getConfidence());
        // State remains UNKNOWN without scoreboard confirmation
        assertEquals(BedwarsGameState.UNKNOWN, detector.getCurrentState());
    }

    @Test
    void testShouldDisplayOverlay() {
        detector.setCurrentState(BedwarsGameState.BEDWARS_ACTIVE);
        detector.setConfidence(DetectionConfidence.HIGH);

        // High confidence match -> true
        assertTrue(detector.shouldDisplayOverlay(true, true, false, true));

        // When require high confidence is true, but confidence is MEDIUM -> false
        detector.setConfidence(DetectionConfidence.MEDIUM);
        assertFalse(detector.shouldDisplayOverlay(true, true, false, true));

        // When require high confidence is false and confidence is MEDIUM -> true
        assertTrue(detector.shouldDisplayOverlay(true, true, false, false));

        // Manual mode overrides everything
        detector.setCurrentState(BedwarsGameState.NOT_IN_BEDWARS);
        detector.setConfidence(DetectionConfidence.UNKNOWN);
        assertTrue(detector.shouldDisplayOverlay(false, false, true, true));
    }
}
