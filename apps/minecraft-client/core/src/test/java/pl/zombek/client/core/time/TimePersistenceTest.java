package pl.zombek.client.core.time;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TimePersistenceTest {

    @TempDir
    Path tempDir;

    private MockTimeSource mockTimeSource;
    private ZombekTimeService timeService;
    private TimePersistenceManager persistenceManager;

    @BeforeEach
    public void setUp() {
        mockTimeSource = new MockTimeSource();
        ZombekTimeService.resetForTesting(mockTimeSource);
        timeService = ZombekTimeService.getInstance();
        persistenceManager = new TimePersistenceManager(tempDir);
    }

    @AfterEach
    public void tearDown() {
        ZombekTimeService.resetForTesting(null);
    }

    @Test
    public void testSaveAndLoadCycle() {
        // Set historical playtime
        timeService.setHistoricalClientPlaytimeSeconds(7200);

        // Record a session
        timeService.onWorldJoin("SurvivalWorld", null, true);
        mockTimeSource.advanceSeconds(300);
        timeService.onWorldLeave();

        // Adjust a stopwatch profile
        StopwatchProfile sp = timeService.getStopwatchProfile("Building");
        sp.start();
        mockTimeSource.advanceSeconds(50);
        sp.pause();

        // Save
        long expectedTotal = timeService.getTotalClientPlaytimeSeconds();
        persistenceManager.save(timeService, true);
        assertTrue(Files.exists(persistenceManager.getDataFile()));

        // Create a new fresh time service instance and load
        ZombekTimeService.resetForTesting(new MockTimeSource());
        ZombekTimeService freshService = ZombekTimeService.getInstance();
        persistenceManager.load(freshService);

        assertEquals(expectedTotal, freshService.getTotalClientPlaytimeSeconds());
        List<PlaytimeSessionRecord> history = freshService.getSessionHistory();
        assertEquals(1, history.size());
        assertEquals("SurvivalWorld", history.get(0).getWorldName());
        assertEquals(300L, history.get(0).getDurationSeconds());

        StopwatchProfile loadedSp = freshService.getStopwatchProfile("Building");
        assertNotNull(loadedSp);
        assertEquals(50_000_000_000L, loadedSp.getElapsedNanos());
        assertEquals(StopwatchState.PAUSED, loadedSp.getState());
    }

    @Test
    public void testCorruptFileRecovery() throws IOException {
        Path targetFile = persistenceManager.getDataFile();
        Files.writeString(targetFile, "{ this is not valid JSON content !!!", StandardCharsets.UTF_8);

        // Loading corrupt file should not throw exception
        assertDoesNotThrow(() -> persistenceManager.load(timeService));

        // Corrupt file should be backed up as .bak
        Path bakFile = tempDir.resolve("time_data.json.bak");
        assertTrue(Files.exists(bakFile));
    }
}
