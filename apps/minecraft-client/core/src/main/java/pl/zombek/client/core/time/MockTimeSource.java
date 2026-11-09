package pl.zombek.client.core.time;

import java.time.ZoneId;

/**
 * Controllable mock time source for deterministic unit and integration tests.
 */
public class MockTimeSource implements ITimeSource {

    private long wallTimeMillis;
    private long monotonicNanos;
    private ZoneId zoneId;

    public MockTimeSource() {
        this(1700000000000L, 0L, ZoneId.of("UTC"));
    }

    public MockTimeSource(long initialWallMillis, long initialMonotonicNanos, ZoneId zoneId) {
        this.wallTimeMillis = initialWallMillis;
        this.monotonicNanos = initialMonotonicNanos;
        this.zoneId = zoneId != null ? zoneId : ZoneId.of("UTC");
    }

    @Override
    public long getWallTimeMillis() {
        return wallTimeMillis;
    }

    @Override
    public long getMonotonicNanos() {
        return monotonicNanos;
    }

    @Override
    public ZoneId getZoneId() {
        return zoneId;
    }

    public void setWallTimeMillis(long wallTimeMillis) {
        this.wallTimeMillis = wallTimeMillis;
    }

    public void setMonotonicNanos(long monotonicNanos) {
        this.monotonicNanos = monotonicNanos;
    }

    public void setZoneId(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    public void advanceMillis(long deltaMillis) {
        this.wallTimeMillis += deltaMillis;
        this.monotonicNanos += deltaMillis * 1_000_000L;
    }

    public void advanceSeconds(long deltaSeconds) {
        advanceMillis(deltaSeconds * 1000L);
    }

    public void advanceNanos(long deltaNanos) {
        this.monotonicNanos += deltaNanos;
        this.wallTimeMillis += deltaNanos / 1_000_000L;
    }
}
