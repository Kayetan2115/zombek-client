package pl.zombek.client.core.netgraph;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks real-time incoming and outgoing packet transmission rates (PPS - Packets Per Second).
 * Thread-safe: Increments atomic counters on network transmission events without counting render passes.
 */
public class PacketRateTracker {

    private final AtomicInteger incomingPacketAccumulator = new AtomicInteger(0);
    private final AtomicInteger outgoingPacketAccumulator = new AtomicInteger(0);

    private volatile int incomingPps = 0;
    private volatile int outgoingPps = 0;
    private volatile int maxIncomingPps = 0;
    private volatile int maxOutgoingPps = 0;

    private long totalIncomingPackets = 0;
    private long totalOutgoingPackets = 0;
    private long updateCount = 0;
    private double sumIncomingPps = 0.0;
    private double sumOutgoingPps = 0.0;

    /**
     * Called when an inbound packet is received by the client network connection.
     */
    public void recordIncomingPacket() {
        incomingPacketAccumulator.incrementAndGet();
    }

    /**
     * Called when an outbound packet is dispatched to the network connection.
     */
    public void recordOutgoingPacket() {
        outgoingPacketAccumulator.incrementAndGet();
    }

    /**
     * Recalculates PPS rates based on elapsed milliseconds since previous update cycle.
     */
    public synchronized void update(long elapsedMillis) {
        if (elapsedMillis <= 0) return;

        int rxCount = incomingPacketAccumulator.getAndSet(0);
        int txCount = outgoingPacketAccumulator.getAndSet(0);

        incomingPps = (int) Math.round(rxCount * 1000.0 / elapsedMillis);
        outgoingPps = (int) Math.round(txCount * 1000.0 / elapsedMillis);

        totalIncomingPackets += rxCount;
        totalOutgoingPackets += txCount;

        if (incomingPps > maxIncomingPps) {
            maxIncomingPps = incomingPps;
        }
        if (outgoingPps > maxOutgoingPps) {
            maxOutgoingPps = outgoingPps;
        }

        updateCount++;
        sumIncomingPps += incomingPps;
        sumOutgoingPps += outgoingPps;
    }

    public synchronized void reset() {
        incomingPacketAccumulator.set(0);
        outgoingPacketAccumulator.set(0);
        incomingPps = 0;
        outgoingPps = 0;
        maxIncomingPps = 0;
        maxOutgoingPps = 0;
        totalIncomingPackets = 0;
        totalOutgoingPackets = 0;
        updateCount = 0;
        sumIncomingPps = 0.0;
        sumOutgoingPps = 0.0;
    }

    public int getIncomingPps() {
        return incomingPps;
    }

    public int getOutgoingPps() {
        return outgoingPps;
    }

    public int getMaxIncomingPps() {
        return maxIncomingPps;
    }

    public int getMaxOutgoingPps() {
        return maxOutgoingPps;
    }

    public double getAvgIncomingPps() {
        return updateCount > 0 ? sumIncomingPps / updateCount : 0.0;
    }

    public double getAvgOutgoingPps() {
        return updateCount > 0 ? sumOutgoingPps / updateCount : 0.0;
    }

    public long getTotalIncomingPackets() {
        return totalIncomingPackets;
    }

    public long getTotalOutgoingPackets() {
        return totalOutgoingPackets;
    }
}
