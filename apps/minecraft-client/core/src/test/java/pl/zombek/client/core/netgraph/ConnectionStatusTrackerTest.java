package pl.zombek.client.core.netgraph;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectionStatusTrackerTest {

    private ConnectionStatusTracker tracker;

    @BeforeEach
    public void setup() {
        tracker = new ConnectionStatusTracker();
        tracker.setThresholds(35, 75, 150, 15.0);
    }

    @Test
    public void testInitialState() {
        assertEquals(ConnectionStatus.UNKNOWN, tracker.getCurrentStatus());
        assertEquals(ConnectionQuality.UNAVAILABLE, tracker.getCurrentQuality());
    }

    @Test
    public void testSingleplayerHandling() {
        tracker.update(ConnectionStatus.CONNECTED, 25, 2.0, true);

        assertEquals(ConnectionStatus.SINGLEPLAYER, tracker.getCurrentStatus());
        assertEquals(ConnectionQuality.UNAVAILABLE, tracker.getCurrentQuality());
    }

    @Test
    public void testDisconnectedHandling() {
        tracker.update(ConnectionStatus.DISCONNECTED, -1, -1.0, false);

        assertEquals(ConnectionStatus.DISCONNECTED, tracker.getCurrentStatus());
        assertEquals(ConnectionQuality.UNAVAILABLE, tracker.getCurrentQuality());
    }

    @Test
    public void testQualityClassification() {
        // Excellent: Ping <= 35 and Jitter <= 7.5
        tracker.update(ConnectionStatus.CONNECTED, 25, 3.0, false);
        assertEquals(ConnectionQuality.EXCELLENT, tracker.getCurrentQuality());

        // Good: Ping <= 75 and Jitter <= 15
        tracker.update(ConnectionStatus.CONNECTED, 55, 8.0, false);
        assertEquals(ConnectionQuality.GOOD, tracker.getCurrentQuality());

        // Moderate: Ping <= 150
        tracker.update(ConnectionStatus.CONNECTED, 120, 12.0, false);
        assertEquals(ConnectionQuality.MODERATE, tracker.getCurrentQuality());

        // Poor: Ping > 150
        tracker.update(ConnectionStatus.CONNECTED, 200, 20.0, false);
        assertEquals(ConnectionQuality.POOR, tracker.getCurrentQuality());
    }

    @Test
    public void testHighJitterDegradesQuality() {
        // Low ping (30ms), but huge jitter (40ms) should degrade to Poor
        tracker.update(ConnectionStatus.CONNECTED, 30, 40.0, false);
        assertEquals(ConnectionQuality.POOR, tracker.getCurrentQuality());
    }
}
