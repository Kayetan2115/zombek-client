package pl.zombek.client.core.netgraph;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NetworkHistoryBufferTest {

    private NetworkHistoryBuffer buffer;

    @BeforeEach
    public void setup() {
        buffer = new NetworkHistoryBuffer(5);
    }

    @Test
    public void testEmptyBuffer() {
        assertEquals(0, buffer.getSampleCount());
        assertTrue(buffer.getSamples().isEmpty());
        assertNull(buffer.getLatestSample());
        assertEquals(-1, buffer.getMaxPing(10000));
        assertEquals(-1, buffer.getMinPing(10000));
        assertEquals(-1.0, buffer.getAvgPing(10000));
    }

    @Test
    public void testAddSampleAndCapacityEviction() {
        long now = System.currentTimeMillis();
        for (int i = 1; i <= 5; i++) {
            buffer.addSample(new NetworkMetricSample(
                    now + i * 1000, 20 + i * 5, 2.0, 1000 * i, 200 * i, 10 + i, 5 + i,
                    ConnectionStatus.CONNECTED, ConnectionQuality.GOOD
            ));
        }

        assertEquals(5, buffer.getSampleCount());
        assertEquals(45, buffer.getLatestSample().getPingMs()); // 20 + 5*5 = 45

        // Adding 6th sample should evict oldest (ping 25)
        buffer.addSample(new NetworkMetricSample(
                now + 6000, 100, 5.0, 6000, 1200, 25, 12,
                ConnectionStatus.CONNECTED, ConnectionQuality.MODERATE
        ));

        assertEquals(5, buffer.getSampleCount());
        List<NetworkMetricSample> samples = buffer.getSamples();
        assertEquals(5, samples.size());
        assertEquals(30, samples.get(0).getPingMs()); // First is now 30
        assertEquals(100, samples.get(4).getPingMs()); // Last is 100
    }

    @Test
    public void testAggregatesCalculation() {
        long now = System.currentTimeMillis();
        buffer.addSample(new NetworkMetricSample(now - 3000, 30, 2.0, 5000, 1000, 15, 8, ConnectionStatus.CONNECTED, ConnectionQuality.GOOD));
        buffer.addSample(new NetworkMetricSample(now - 1000, 70, 4.0, 15000, 2000, 25, 12, ConnectionStatus.CONNECTED, ConnectionQuality.GOOD));

        assertEquals(70, buffer.getMaxPing(5000));
        assertEquals(30, buffer.getMinPing(5000));
        assertEquals(50.0, buffer.getAvgPing(5000), 0.001);
        assertEquals(17000, buffer.getMaxTraffic(5000)); // 15000 + 2000 = 17000
        assertEquals(37, buffer.getMaxPps(5000)); // 25 + 12 = 37
    }

    @Test
    public void testClear() {
        buffer.addSample(new NetworkMetricSample(System.currentTimeMillis(), 30, 1.0, 100, 50, 10, 5, ConnectionStatus.CONNECTED, ConnectionQuality.GOOD));
        buffer.clear();
        assertEquals(0, buffer.getSampleCount());
        assertNull(buffer.getLatestSample());
    }
}
