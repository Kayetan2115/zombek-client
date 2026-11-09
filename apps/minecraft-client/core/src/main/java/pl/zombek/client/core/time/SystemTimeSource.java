package pl.zombek.client.core.time;

import java.time.ZoneId;

/**
 * Standard live system time source utilizing the underlying JVM and OS clocks.
 */
public class SystemTimeSource implements ITimeSource {

    @Override
    public long getWallTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public long getMonotonicNanos() {
        return System.nanoTime();
    }

    @Override
    public ZoneId getZoneId() {
        return ZoneId.systemDefault();
    }
}
