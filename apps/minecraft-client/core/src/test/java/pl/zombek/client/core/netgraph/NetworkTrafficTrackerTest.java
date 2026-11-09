package pl.zombek.client.core.netgraph;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NetworkTrafficTrackerTest {

    private NetworkTrafficTracker tracker;

    @BeforeEach
    public void setup() {
        tracker = new NetworkTrafficTracker();
    }

    @Test
    public void testInitialState() {
        assertEquals(0, tracker.getIncomingBytesPerSecond());
        assertEquals(0, tracker.getOutgoingBytesPerSecond());
        assertEquals(0, tracker.getTotalBytesReceived());
        assertEquals(0, tracker.getTotalBytesSent());
        assertEquals(0, tracker.getPeakIncomingBps());
        assertEquals(0, tracker.getPeakOutgoingBps());
    }

    @Test
    public void testThroughputUpdate() {
        // Record 10,240 bytes inbound and 2,048 bytes outbound
        tracker.recordIncomingBytes(10240);
        tracker.recordOutgoingBytes(2048);

        // Update with 1000ms elapsed
        tracker.update(1000);

        assertEquals(10240, tracker.getIncomingBytesPerSecond());
        assertEquals(2048, tracker.getOutgoingBytesPerSecond());
        assertEquals(10240, tracker.getTotalBytesReceived());
        assertEquals(2048, tracker.getTotalBytesSent());
        assertEquals(10240, tracker.getPeakIncomingBps());
        assertEquals(2048, tracker.getPeakOutgoingBps());

        // Subsequent update with 0 bytes recorded
        tracker.update(1000);
        assertEquals(0, tracker.getIncomingBytesPerSecond());
        assertEquals(0, tracker.getOutgoingBytesPerSecond());
        assertEquals(10240, tracker.getTotalBytesReceived());
        assertEquals(2048, tracker.getTotalBytesSent());
        assertEquals(10240, tracker.getPeakIncomingBps()); // Peak remains unchanged
    }

    @Test
    public void testFormatBytesPerSecond() {
        assertEquals("500 B/s", NetworkTrafficTracker.formatBytesPerSecond(500, TrafficUnit.AUTO));
        assertEquals("10.0 KB/s", NetworkTrafficTracker.formatBytesPerSecond(10240, TrafficUnit.AUTO));
        assertEquals("2.00 MB/s", NetworkTrafficTracker.formatBytesPerSecond(2097152, TrafficUnit.AUTO));
        assertEquals("N/A", NetworkTrafficTracker.formatBytesPerSecond(-1, TrafficUnit.AUTO));

        // Fixed unit formatting
        assertEquals("10240 B/s", NetworkTrafficTracker.formatBytesPerSecond(10240, TrafficUnit.B_S));
        assertEquals("10.0 KB/s", NetworkTrafficTracker.formatBytesPerSecond(10240, TrafficUnit.KB_S));
        assertEquals("0.01 MB/s", NetworkTrafficTracker.formatBytesPerSecond(10240, TrafficUnit.MB_S));
    }

    @Test
    public void testFormatTotalBytes() {
        assertEquals("512 B", NetworkTrafficTracker.formatTotalBytes(512));
        assertEquals("1.5 KB", NetworkTrafficTracker.formatTotalBytes(1536));
        assertEquals("5.00 MB", NetworkTrafficTracker.formatTotalBytes(5242880));
        assertEquals("1.00 GB", NetworkTrafficTracker.formatTotalBytes(1073741824L));
        assertEquals("N/A", NetworkTrafficTracker.formatTotalBytes(-1));
    }

    @Test
    public void testReset() {
        tracker.recordIncomingBytes(5000);
        tracker.update(1000);
        tracker.reset();

        assertEquals(0, tracker.getIncomingBytesPerSecond());
        assertEquals(0, tracker.getOutgoingBytesPerSecond());
        assertEquals(0, tracker.getTotalBytesReceived());
        assertEquals(0, tracker.getTotalBytesSent());
        assertEquals(0, tracker.getPeakIncomingBps());
    }
}
