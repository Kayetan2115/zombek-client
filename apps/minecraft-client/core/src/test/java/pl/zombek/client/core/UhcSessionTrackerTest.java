package pl.zombek.client.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.uhc.UhcMode;
import pl.zombek.client.core.uhc.UhcSessionTracker;
import pl.zombek.client.core.uhc.UhcTimerState;
import pl.zombek.client.core.uhc.UhcTimerType;

import static org.junit.jupiter.api.Assertions.*;

public class UhcSessionTrackerTest {

    private UhcSessionTracker tracker;

    @BeforeEach
    public void setUp() {
        tracker = UhcSessionTracker.getInstance();
        tracker.resetSession();
    }

    @Test
    public void testSessionLifecycle() {
        assertFalse(tracker.isSessionActive());
        assertEquals(0L, tracker.getSessionDurationSeconds());

        tracker.setMode(UhcMode.TEAMS);
        assertEquals(UhcMode.TEAMS, tracker.getMode());

        tracker.startSession();
        assertTrue(tracker.isSessionActive());
        assertEquals(UhcTimerState.RUNNING, tracker.getTimer(UhcTimerType.GAME).getState());

        tracker.resetSession();
        assertFalse(tracker.isSessionActive());
        assertEquals(UhcTimerState.STOPPED, tracker.getTimer(UhcTimerType.GAME).getState());
    }

    @Test
    public void testEventTriggers() {
        tracker.onMatchStart();
        assertTrue(tracker.isSessionActive());
        assertEquals(UhcTimerState.RUNNING, tracker.getTimer(UhcTimerType.GAME).getState());
        assertEquals(UhcTimerState.RUNNING, tracker.getTimer(UhcTimerType.PVP).getState());

        tracker.onPvpEnabled();
        assertEquals(UhcTimerState.STOPPED, tracker.getTimer(UhcTimerType.PVP).getState());

        tracker.onDeathmatch();
        assertEquals(UhcTimerState.RUNNING, tracker.getTimer(UhcTimerType.DEATHMATCH).getState());

        tracker.onGameOver();
        assertEquals(UhcTimerState.PAUSED, tracker.getTimer(UhcTimerType.GAME).getState());
    }
}
