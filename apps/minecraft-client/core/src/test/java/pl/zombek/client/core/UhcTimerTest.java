package pl.zombek.client.core;

import org.junit.jupiter.api.Test;
import pl.zombek.client.core.uhc.UhcTimer;
import pl.zombek.client.core.uhc.UhcTimerState;
import pl.zombek.client.core.uhc.UhcTimerType;

import static org.junit.jupiter.api.Assertions.*;

public class UhcTimerTest {

    @Test
    public void testCountUpTimerLifecycle() throws InterruptedException {
        UhcTimer timer = new UhcTimer(UhcTimerType.GAME);
        assertEquals(UhcTimerState.STOPPED, timer.getState());
        assertEquals(0L, timer.getElapsedSeconds());
        assertFalse(timer.isCountdown());

        timer.start();
        assertEquals(UhcTimerState.RUNNING, timer.getState());

        Thread.sleep(50);
        timer.pause();
        assertEquals(UhcTimerState.PAUSED, timer.getState());

        timer.resume();
        assertEquals(UhcTimerState.RUNNING, timer.getState());

        timer.stop();
        assertEquals(UhcTimerState.STOPPED, timer.getState());
        assertEquals(0L, timer.getElapsedSeconds());
    }

    @Test
    public void testCountdownTimer() {
        UhcTimer timer = new UhcTimer("Test PvP", true, 600); // 10 minutes
        assertTrue(timer.isCountdown());
        assertEquals(600L, timer.getRemainingSeconds());
        assertFalse(timer.isExpired());

        timer.start();
        assertTrue(timer.getRemainingSeconds() <= 600L);
        assertFalse(timer.isExpired());

        // Zero seconds countdown
        UhcTimer instantTimer = new UhcTimer("Instant", true, 0);
        instantTimer.start();
        assertTrue(instantTimer.isExpired());
    }

    @Test
    public void testFormatTime() {
        UhcTimer timer = new UhcTimer("Timer", false, 0);
        assertEquals("00:00", timer.formatTime(0));
        assertEquals("01:05", timer.formatTime(65));
        assertEquals("01:00:00", timer.formatTime(3600));
        assertEquals("02:15:30", timer.formatTime(8130));
    }
}
