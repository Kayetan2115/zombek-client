package pl.zombek.client.core.time;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Abstraction layer for querying wall-clock and monotonic system time.
 * Enables deterministic testing with controlled mock clocks without real-time delays.
 */
public interface ITimeSource {

    /**
     * Current wall-clock time in epoch milliseconds (System.currentTimeMillis).
     */
    long getWallTimeMillis();

    /**
     * High-precision monotonic elapsed time in nanoseconds (System.nanoTime).
     */
    long getMonotonicNanos();

    /**
     * Active system or configured time zone.
     */
    ZoneId getZoneId();

    /**
     * Current local time.
     */
    default LocalTime getLocalTime() {
        return LocalTime.ofInstant(Instant.ofEpochMilli(getWallTimeMillis()), getZoneId());
    }

    /**
     * Current local date.
     */
    default LocalDate getLocalDate() {
        return LocalDate.ofInstant(Instant.ofEpochMilli(getWallTimeMillis()), getZoneId());
    }

    /**
     * Current zoned date-time.
     */
    default ZonedDateTime getZonedDateTime() {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(getWallTimeMillis()), getZoneId());
    }
}
