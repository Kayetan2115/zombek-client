package pl.zombek.client.core.netgraph;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PacketRateTrackerTest {

    private PacketRateTracker tracker;

    @BeforeEach
    public void setup() {
        tracker = new PacketRateTracker();
    }

    @Test
    public void testInitialState() {
        assertEquals(0, tracker.getIncomingPps());
        assertEquals(0, tracker.getOutgoingPps());
        assertEquals(0, tracker.getMaxIncomingPps());
        assertEquals(0, tracker.getMaxOutgoingPps());
        assertEquals(0.0, tracker.getAvgIncomingPps());
        assertEquals(0.0, tracker.getAvgOutgoingPps());
        assertEquals(0, tracker.getTotalIncomingPackets());
        assertEquals(0, tracker.getTotalOutgoingPackets());
    }

    @Test
    public void testPacketRateAccumulation() {
        // Record 20 incoming packets and 10 outgoing packets
        for (int i = 0; i < 20; i++) tracker.recordIncomingPacket();
        for (int i = 0; i < 10; i++) tracker.recordOutgoingPacket();

        tracker.update(1000);

        assertEquals(20, tracker.getIncomingPps());
        assertEquals(10, tracker.getOutgoingPps());
        assertEquals(20, tracker.getMaxIncomingPps());
        assertEquals(10, tracker.getMaxOutgoingPps());
        assertEquals(20.0, tracker.getAvgIncomingPps());
        assertEquals(10.0, tracker.getAvgOutgoingPps());
        assertEquals(20, tracker.getTotalIncomingPackets());
        assertEquals(10, tracker.getTotalOutgoingPackets());

        // Second update with 40 incoming and 5 outgoing
        for (int i = 0; i < 40; i++) tracker.recordIncomingPacket();
        for (int i = 0; i < 5; i++) tracker.recordOutgoingPacket();

        tracker.update(1000);

        assertEquals(40, tracker.getIncomingPps());
        assertEquals(5, tracker.getOutgoingPps());
        assertEquals(40, tracker.getMaxIncomingPps());
        assertEquals(10, tracker.getMaxOutgoingPps()); // Previous peak was 10
        assertEquals(30.0, tracker.getAvgIncomingPps(), 0.001); // (20 + 40) / 2 = 30.0
        assertEquals(7.5, tracker.getAvgOutgoingPps(), 0.001);  // (10 + 5) / 2 = 7.5
        assertEquals(60, tracker.getTotalIncomingPackets());
        assertEquals(15, tracker.getTotalOutgoingPackets());
    }

    @Test
    public void testReset() {
        tracker.recordIncomingPacket();
        tracker.update(1000);
        tracker.reset();

        assertEquals(0, tracker.getIncomingPps());
        assertEquals(0, tracker.getOutgoingPps());
        assertEquals(0, tracker.getMaxIncomingPps());
        assertEquals(0, tracker.getTotalIncomingPackets());
    }
}
