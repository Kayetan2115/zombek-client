package pl.zombek.client.core.time;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

public class ZombekTimeServiceTest {

    private MockTimeSource mockTimeSource;
    private ZombekTimeService timeService;
    private FallbackTimeDataProvider mockDataProvider;

    @BeforeEach
    public void setUp() {
        // Epoch millis: 2026-11-03 14:35:27 UTC
        // 1793716527000L
        mockTimeSource = new MockTimeSource(1793716527000L, 10_000_000_000L, ZoneId.of("UTC"));
        ZombekTimeService.resetForTesting(mockTimeSource);
        timeService = ZombekTimeService.getInstance();

        mockDataProvider = new FallbackTimeDataProvider();
        TimeDataProviderRegistry.getInstance().registerProvider(mockDataProvider);
    }

    @AfterEach
    public void tearDown() {
        ZombekTimeService.resetForTesting(null);
    }

    @Test
    public void testWallClockQueries() {
        LocalTime lt = timeService.getLocalTime();
        assertNotNull(lt);
        assertEquals(ZoneId.of("UTC"), timeService.getTimeSource().getZoneId());

        // Change time zone to Europe/Warsaw (+1)
        mockTimeSource.setZoneId(ZoneId.of("Europe/Warsaw"));
        LocalTime warsawTime = timeService.getLocalTime();
        assertNotEquals(lt.getHour(), warsawTime.getHour());
    }

    @Test
    public void testClientSessionMonotonicTiming() {
        assertEquals(0L, timeService.getClientSessionSeconds());

        // Advance 125 seconds
        mockTimeSource.advanceSeconds(125);
        assertEquals(125L, timeService.getClientSessionSeconds());
        assertEquals(125L, timeService.getTotalClientPlaytimeSeconds());

        // With historical playtime
        timeService.setHistoricalClientPlaytimeSeconds(3600);
        assertEquals(3725L, timeService.getTotalClientPlaytimeSeconds());
    }

    @Test
    public void testWorldSessionLifecycle() {
        assertFalse(timeService.isWorldSessionActive());
        assertEquals(0L, timeService.getWorldSessionSeconds());

        timeService.onWorldJoin("TestWorld", "mc.testserver.net", true);
        assertTrue(timeService.isWorldSessionActive());

        mockTimeSource.advanceSeconds(45);
        assertEquals(45L, timeService.getWorldSessionSeconds());

        timeService.onWorldLeave();
        assertFalse(timeService.isWorldSessionActive());
        assertEquals(0L, timeService.getWorldSessionSeconds());

        // Recorded to history
        assertEquals(1, timeService.getSessionHistory().size());
        PlaytimeSessionRecord record = timeService.getSessionHistory().get(0);
        assertEquals(45L, record.getDurationSeconds());
        assertTrue(record.isSingleplayer());
        assertEquals("TestWorld", record.getWorldName());
    }

    @Test
    public void testMinecraftWorldTimeConversion() {
        // In Minecraft: 0 ticks = 06:00
        assertEquals(6, ZombekTimeService.calculateMinecraftHours(0));
        assertEquals(0, ZombekTimeService.calculateMinecraftMinutes(0));

        // 6000 ticks = 12:00 (Noon)
        assertEquals(12, ZombekTimeService.calculateMinecraftHours(6000));
        assertEquals(0, ZombekTimeService.calculateMinecraftMinutes(6000));

        // 12000 ticks = 18:00 (Dusk)
        assertEquals(18, ZombekTimeService.calculateMinecraftHours(12000));
        assertEquals(0, ZombekTimeService.calculateMinecraftMinutes(12000));

        // 18000 ticks = 00:00 (Midnight)
        assertEquals(0, ZombekTimeService.calculateMinecraftHours(18000));
        assertEquals(0, ZombekTimeService.calculateMinecraftMinutes(18000));

        // 23500 ticks = 05:30 (Dawn)
        assertEquals(5, ZombekTimeService.calculateMinecraftHours(23500));
        assertEquals(30, ZombekTimeService.calculateMinecraftMinutes(23500));

        // String formatting 24h & 12h
        assertEquals("12:00", ZombekTimeService.formatMinecraftTime(6000, true, false, true));
        assertEquals("12:00 PM", ZombekTimeService.formatMinecraftTime(6000, false, true, true));
        assertEquals("06:00", ZombekTimeService.formatMinecraftTime(0, true, false, true));
        assertEquals("6:00 AM", ZombekTimeService.formatMinecraftTime(0, false, true, false));
    }

    @Test
    public void testDayNightPhases() {
        assertEquals(TimeOfDayPhase.DAWN, TimeOfDayPhase.fromTicks(0));
        assertEquals(TimeOfDayPhase.NOON, TimeOfDayPhase.fromTicks(6000));
        assertEquals(TimeOfDayPhase.DUSK, TimeOfDayPhase.fromTicks(13000));
        assertEquals(TimeOfDayPhase.MIDNIGHT, TimeOfDayPhase.fromTicks(18000));
        assertEquals(TimeOfDayPhase.NIGHT, TimeOfDayPhase.fromTicks(20000));

        assertTrue(TimeOfDayPhase.isDay(6000));
        assertFalse(TimeOfDayPhase.isDay(16000));
        assertTrue(TimeOfDayPhase.isSunrise(23500));
        assertTrue(TimeOfDayPhase.isSunset(12500));
    }

    @Test
    public void testWorldDayCalculationAndRollbackDetection() {
        // Day 1: 0 ticks
        assertEquals(1L, ZombekTimeService.calculateWorldDay(0, 0));
        // Day 2: 24000 ticks
        assertEquals(2L, ZombekTimeService.calculateWorldDay(24000, 0));
        // Day 127: 126 * 24000 = 3024000 ticks
        assertEquals(127L, ZombekTimeService.calculateWorldDay(3024000, 0));

        // Manual offset
        assertEquals(130L, ZombekTimeService.calculateWorldDay(3024000, 3));

        // Invalid negative gameTime
        assertEquals(-1L, ZombekTimeService.calculateWorldDay(-1, 0));

        // Rollback detection
        assertFalse(timeService.isTimeResetDetected());
        timeService.updateWorldTime(6000, 100000);
        assertFalse(timeService.isTimeResetDetected());

        // Server rolls time backwards to 50000
        timeService.updateWorldTime(6000, 50000);
        assertTrue(timeService.isTimeResetDetected());

        timeService.resetTimeResetFlag();
        assertFalse(timeService.isTimeResetDetected());
    }

    @Test
    public void testWorldAgeFormatting() {
        // 24000 ticks = 1d 0h
        assertEquals("1d 0h", ZombekTimeService.formatWorldAge(24000));
        // 30000 ticks = 1d 6h
        assertEquals("1d 6h", ZombekTimeService.formatWorldAge(30000));
        assertEquals("N/A", ZombekTimeService.formatWorldAge(-1));
    }
}
