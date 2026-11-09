package pl.zombek.client.core.tracking;

/**
 * Immutable snapshot of an attack reach measurement.
 */
public record ReachRecord(
        long timestamp,
        double distance,
        ReachEventType eventType,
        int targetEntityId,
        String targetName
) {
    public boolean isObservedHit() {
        return eventType == ReachEventType.OBSERVED_HIT;
    }
}
