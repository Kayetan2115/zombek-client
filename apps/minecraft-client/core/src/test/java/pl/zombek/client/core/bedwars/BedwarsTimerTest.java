package pl.zombek.client.core.bedwars;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class BedwarsTimerTest {
    private BedwarsTimerManager manager;

    @BeforeEach
    void setUp() {
        manager = new BedwarsTimerManager();
    }

    @Test
    void testMatchTimerTicksUpward() {
        BedwarsTimer match = manager.getMatchTimer();
        assertEquals("00:00", match.getFormattedTime());

        manager.tickSecond();
        manager.tickSecond();

        assertEquals(2, match.getRemainingSeconds());
        assertEquals("00:02", match.getFormattedTime());
    }

    @Test
    void testUpgradeTimerTicksDownward() {
        BedwarsTimer diamond = new BedwarsTimer("dia", "Diamond II", 125); // 02:05
        assertEquals("02:05", diamond.getFormattedTime());

        diamond.tick();
        assertEquals(124, diamond.getRemainingSeconds());
        assertEquals("02:04", diamond.getFormattedTime());
    }

    @Test
    void testTimerFinishesAtZero() {
        BedwarsTimer timer = new BedwarsTimer("test", "Test", 1);
        assertEquals(TimerState.RUNNING, timer.getState());

        timer.tick();
        assertEquals(0, timer.getRemainingSeconds());
        assertEquals(TimerState.FINISHED, timer.getState());
    }

    @Test
    void testUpdateFromScoreboardTimers() {
        BedwarsTimer beds = new BedwarsTimer("b", "Beds Destroy", 300);
        BedwarsTimer dia = new BedwarsTimer("d", "Diamond II", 120);

        manager.updateFromScoreboardTimers(java.util.Arrays.asList(beds, dia));

        assertEquals(300, manager.getBedDestructionTimer().getRemainingSeconds());
        assertEquals(TimerState.RUNNING, manager.getBedDestructionTimer().getState());
        assertEquals(1, manager.getUpgradeTimers().size());
        assertEquals("Diamond II", manager.getUpgradeTimers().get(0).getName());
    }
}
