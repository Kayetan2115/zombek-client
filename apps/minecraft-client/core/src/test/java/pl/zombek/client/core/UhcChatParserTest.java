package pl.zombek.client.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.uhc.UhcChatParser;
import pl.zombek.client.core.uhc.UhcSessionTracker;
import pl.zombek.client.core.uhc.UhcTimerState;
import pl.zombek.client.core.uhc.UhcTimerType;

import static org.junit.jupiter.api.Assertions.*;

public class UhcChatParserTest {

    private UhcSessionTracker tracker;
    private UhcChatParser chatParser;

    @BeforeEach
    public void setUp() {
        tracker = UhcSessionTracker.getInstance();
        tracker.resetSession();
        chatParser = tracker.getChatParser();
    }

    @Test
    public void testMatchStartDetection() {
        assertFalse(tracker.isSessionActive());
        chatParser.onChatMessage("§e[System] The game has begun! Good luck!");
        assertTrue(tracker.isSessionActive());
        assertEquals(UhcTimerState.RUNNING, tracker.getTimer(UhcTimerType.GAME).getState());
    }

    @Test
    public void testPvpActivationDetection() {
        tracker.startSession();
        tracker.getTimer(UhcTimerType.PVP).start();
        assertEquals(UhcTimerState.RUNNING, tracker.getTimer(UhcTimerType.PVP).getState());

        chatParser.onChatMessage("§c§lPvP is now enabled! Watch your back!");
        assertEquals(UhcTimerState.STOPPED, tracker.getTimer(UhcTimerType.PVP).getState());
    }

    @Test
    public void testDeathmatchAndGameOverDetection() {
        tracker.startSession();

        chatParser.onChatMessage("§6Deathmatch will begin in 60 seconds!");
        assertEquals(UhcTimerState.RUNNING, tracker.getTimer(UhcTimerType.DEATHMATCH).getState());

        chatParser.onChatMessage("§aPlayer1 has won the game! Victory!");
        assertEquals(UhcTimerState.PAUSED, tracker.getTimer(UhcTimerType.GAME).getState());
    }
}
