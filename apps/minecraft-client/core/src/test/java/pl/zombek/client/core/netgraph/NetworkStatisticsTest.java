package pl.zombek.client.core.netgraph;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NetworkStatisticsTest {

    private NetworkStatistics stats;

    @BeforeEach
    public void setup() {
        stats = new NetworkStatistics();
        stats.startSession();
    }

    @Test
    public void testInitialState() {
        assertEquals(-1, stats.getMinPing());
        assertEquals(-1, stats.getMaxPing());
        assertEquals(-1.0, stats.getAvgPing());
        assertEquals(-1.0, stats.getMaxJitter());
        assertEquals(-1.0, stats.getAvgJitter());
        assertEquals(0, stats.getPeakIncomingBps());
        assertEquals(0, stats.getPeakOutgoingBps());
        assertEquals(0, stats.getTotalBytesReceived());
        assertEquals(0, stats.getTotalBytesSent());
        assertEquals(0, stats.getMaxIncomingPps());
        assertEquals(0, stats.getMaxOutgoingPps());
    }

    @Test
    public void testUpdateAndAverages() {
        long now = System.currentTimeMillis();
        stats.update(new NetworkMetricSample(now, 30, 2.0, 10240, 2048, 20, 10, ConnectionStatus.CONNECTED, ConnectionQuality.GOOD));
        stats.update(new NetworkMetricSample(now + 1000, 50, 4.0, 20480, 4096, 30, 15, ConnectionStatus.CONNECTED, ConnectionQuality.GOOD));

        assertEquals(30, stats.getMinPing());
        assertEquals(50, stats.getMaxPing());
        assertEquals(40.0, stats.getAvgPing(), 0.001);
        assertEquals(4.0, stats.getMaxJitter(), 0.001);
        assertEquals(3.0, stats.getAvgJitter(), 0.001);

        assertEquals(20480, stats.getPeakIncomingBps());
        assertEquals(4096, stats.getPeakOutgoingBps());
        assertEquals(30720, stats.getTotalBytesReceived());
        assertEquals(6144, stats.getTotalBytesSent());

        assertEquals(30, stats.getMaxIncomingPps());
        assertEquals(15, stats.getMaxOutgoingPps());
        assertEquals(25.0, stats.getAvgIncomingPps(), 0.001);
        assertEquals(12.5, stats.getAvgOutgoingPps(), 0.001);
    }

    @Test
    public void testExportToJsonDoesNotContainPrivateData() {
        long now = System.currentTimeMillis();
        stats.update(new NetworkMetricSample(now, 32, 2.5, 15000, 3000, 22, 11, ConnectionStatus.CONNECTED, ConnectionQuality.GOOD));

        String json = stats.exportToJson();
        assertNotNull(json);
        assertTrue(json.contains("\"minPingMs\": 32"));
        assertTrue(json.contains("\"totalReceivedBytes\": 15000"));

        // Privacy assertion: zero IP address, zero server name, zero passwords
        assertFalse(json.contains("ip"));
        assertFalse(json.contains("host"));
        assertFalse(json.contains("address"));
        assertFalse(json.contains("password"));
        assertFalse(json.contains("chat"));
    }

    @Test
    public void testExportToText() {
        long now = System.currentTimeMillis();
        stats.update(new NetworkMetricSample(now, 28, 1.8, 12000, 2400, 20, 10, ConnectionStatus.CONNECTED, ConnectionQuality.EXCELLENT));

        String text = stats.exportToText();
        assertNotNull(text);
        assertTrue(text.contains("ZombekClient Net Graph Diagnostics"));
        assertTrue(text.contains("Min Ping: 28 ms"));
        assertTrue(text.contains("Max Ping: 28 ms"));
        assertTrue(text.contains("Avg Ping: 28.0 ms"));
    }

    @Test
    public void testReset() {
        stats.update(new NetworkMetricSample(System.currentTimeMillis(), 40, 2.0, 1000, 200, 15, 5, ConnectionStatus.CONNECTED, ConnectionQuality.GOOD));
        stats.reset();

        assertEquals(-1, stats.getMinPing());
        assertEquals(-1, stats.getMaxPing());
        assertEquals(0, stats.getTotalBytesReceived());
    }
}
