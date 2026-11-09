package pl.zombek.client.core.time;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StopwatchProfileTest {

    private MockTimeSource mockTimeSource;
    private StopwatchProfile profile;

    @BeforeEach
    public void setUp() {
        mockTimeSource = new MockTimeSource();
        profile = new StopwatchProfile("Speedrun", StopwatchProfileType.SPEEDRUN, mockTimeSource);
    }

    @Test
    public void testInitialState() {
        assertEquals("Speedrun", profile.getName());
        assertEquals(StopwatchProfileType.SPEEDRUN, profile.getType());
        assertEquals(StopwatchState.STOPPED, profile.getState());
        assertEquals(0L, profile.getElapsedNanos());
        assertTrue(profile.getLaps().isEmpty());
    }

    @Test
    public void testStartPauseResumeReset() {
        profile.start();
        assertTrue(profile.isRunning());

        mockTimeSource.advanceSeconds(5);
        assertEquals(5_000_000_000L, profile.getElapsedNanos());

        profile.pause();
        assertTrue(profile.isPaused());

        // While paused, time should not advance
        mockTimeSource.advanceSeconds(10);
        assertEquals(5_000_000_000L, profile.getElapsedNanos());

        profile.resume();
        assertTrue(profile.isRunning());

        mockTimeSource.advanceSeconds(3);
        assertEquals(8_000_000_000L, profile.getElapsedNanos());

        profile.reset();
        assertTrue(profile.isStopped());
        assertEquals(0L, profile.getElapsedNanos());
    }

    @Test
    public void testToggle() {
        profile.toggle();
        assertTrue(profile.isRunning());

        profile.toggle();
        assertTrue(profile.isPaused());

        profile.toggle();
        assertTrue(profile.isRunning());
    }

    @Test
    public void testLapRecordingAndDelta() {
        profile.start();

        // Lap 1 at 35 seconds
        mockTimeSource.advanceSeconds(35);
        StopwatchLap lap1 = profile.recordLap();
        assertNotNull(lap1);
        assertEquals(1, lap1.getLapNumber());
        assertEquals(35_000_000_000L, lap1.getLapTimeNanos());
        assertEquals(35_000_000_000L, lap1.getTotalTimeNanos());
        assertEquals(0L, lap1.getDeltaNanos());
        assertEquals("±0.00", lap1.getFormattedDelta());

        // Lap 2 at 77 seconds (lap 2 duration = 42 seconds, +7s vs lap 1)
        mockTimeSource.advanceSeconds(42);
        StopwatchLap lap2 = profile.recordLap();
        assertNotNull(lap2);
        assertEquals(2, lap2.getLapNumber());
        assertEquals(42_000_000_000L, lap2.getLapTimeNanos());
        assertEquals(77_000_000_000L, lap2.getTotalTimeNanos());
        assertEquals(7_000_000_000L, lap2.getDeltaNanos());
        assertEquals("+7.00", lap2.getFormattedDelta());

        // Lap 3 at 115 seconds (lap 3 duration = 38 seconds, -4s vs lap 2)
        mockTimeSource.advanceSeconds(38);
        StopwatchLap lap3 = profile.recordLap();
        assertNotNull(lap3);
        assertEquals(3, lap3.getLapNumber());
        assertEquals(38_000_000_000L, lap3.getLapTimeNanos());
        assertEquals("-4.00", lap3.getFormattedDelta());

        List<StopwatchLap> laps = profile.getLaps();
        assertEquals(3, laps.size());

        // Best lap (lap 1 with 35s)
        StopwatchLap best = profile.getBestLap();
        assertNotNull(best);
        assertEquals(1, best.getLapNumber());

        // Average lap: (35 + 42 + 38) / 3 = 115 / 3 = 38.333s
        long avg = profile.getAverageLapNanos();
        assertEquals(115_000_000_000L / 3, avg);

        profile.clearLaps();
        assertTrue(profile.getLaps().isEmpty());
    }

    @Test
    public void testFormatNanos() {
        // 35 seconds and 210 milliseconds (21 hundredths)
        long nanos = (35L * 1_000_000_000L) + 210_000_000L;
        assertEquals("00:35.21", StopwatchLap.formatNanos(nanos, true, false));
        assertEquals("00:35", StopwatchLap.formatNanos(nanos, false, false));
        assertEquals("00:00:35.21", StopwatchLap.formatNanos(nanos, true, true));

        // 1 hour, 2 minutes, 3 seconds
        long hourNanos = (3600L + 120L + 3L) * 1_000_000_000L;
        assertEquals("01:02:03", StopwatchLap.formatNanos(hourNanos, false, false));
    }
}
