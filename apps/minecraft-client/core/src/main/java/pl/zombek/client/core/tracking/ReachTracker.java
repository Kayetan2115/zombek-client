package pl.zombek.client.core.tracking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Thread-safe manager tracking attack reach distances to targets using exact AABB geometry.
 */
public class ReachTracker {
    private static final ReachTracker INSTANCE = new ReachTracker();

    private final List<ReachRecord> history = new ArrayList<>();
    private int maxHistoryEntries = 50;
    private double currentTargetDistance = 0.0;
    private int lastTargetId = -1;

    private ReachTracker() {}

    public static ReachTracker getInstance() {
        return INSTANCE;
    }

    /**
     * Calculates the exact distance from player eye position to the nearest point on target AABB.
     */
    public static double calculateDistanceToAABB(double eyeX, double eyeY, double eyeZ,
                                                 double minX, double minY, double minZ,
                                                 double maxX, double maxY, double maxZ) {
        double clampedX = Math.max(minX, Math.min(eyeX, maxX));
        double clampedY = Math.max(minY, Math.min(eyeY, maxY));
        double clampedZ = Math.max(minZ, Math.min(eyeZ, maxZ));

        double dx = eyeX - clampedX;
        double dy = eyeY - clampedY;
        double dz = eyeZ - clampedZ;

        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Records an attack attempt.
     */
    public synchronized void recordAttackAttempt(double distance, int targetId, String targetName) {
        addRecord(new ReachRecord(System.currentTimeMillis(), distance, ReachEventType.ATTACK_ATTEMPT, targetId, targetName));
    }

    /**
     * Records a verified/observed hit against target.
     */
    public synchronized void recordObservedHit(double distance, int targetId, String targetName) {
        addRecord(new ReachRecord(System.currentTimeMillis(), distance, ReachEventType.OBSERVED_HIT, targetId, targetName));
    }

    /**
     * Records an ambiguous or unknown result.
     */
    public synchronized void recordUnknownResult(double distance, int targetId, String targetName) {
        addRecord(new ReachRecord(System.currentTimeMillis(), distance, ReachEventType.UNKNOWN_RESULT, targetId, targetName));
    }

    private void addRecord(ReachRecord record) {
        this.history.add(record);
        this.lastTargetId = record.targetEntityId();
        while (this.history.size() > this.maxHistoryEntries) {
            this.history.remove(0);
        }
    }

    public synchronized double getLastReach() {
        if (this.history.isEmpty()) {
            return 0.0;
        }
        return this.history.get(this.history.size() - 1).distance();
    }

    public synchronized double getLastObservedHitDistance() {
        for (int i = this.history.size() - 1; i >= 0; i--) {
            ReachRecord r = this.history.get(i);
            if (r.eventType() == ReachEventType.OBSERVED_HIT) {
                return r.distance();
            }
        }
        return 0.0;
    }

    public synchronized double getAverageReach() {
        if (this.history.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (ReachRecord r : this.history) {
            sum += r.distance();
        }
        return sum / this.history.size();
    }

    public synchronized double getMaximumReach() {
        if (this.history.isEmpty()) {
            return 0.0;
        }
        double max = 0.0;
        for (ReachRecord r : this.history) {
            if (r.distance() > max) {
                max = r.distance();
            }
        }
        return max;
    }

    public synchronized double getMinimumReach() {
        if (this.history.isEmpty()) {
            return 0.0;
        }
        double min = Double.MAX_VALUE;
        for (ReachRecord r : this.history) {
            if (r.distance() < min) {
                min = r.distance();
            }
        }
        return min == Double.MAX_VALUE ? 0.0 : min;
    }

    public synchronized double getCurrentTargetDistance() {
        return this.currentTargetDistance;
    }

    public synchronized void setCurrentTargetDistance(double distance) {
        this.currentTargetDistance = distance;
    }

    public synchronized int getLastTargetId() {
        return this.lastTargetId;
    }

    public synchronized List<ReachRecord> getHistory() {
        return Collections.unmodifiableList(new ArrayList<>(this.history));
    }

    public synchronized int getMaxHistoryEntries() {
        return this.maxHistoryEntries;
    }

    public synchronized void setMaxHistoryEntries(int max) {
        this.maxHistoryEntries = Math.max(1, max);
        while (this.history.size() > this.maxHistoryEntries) {
            this.history.remove(0);
        }
    }

    public synchronized void clearHistory() {
        this.history.clear();
        this.currentTargetDistance = 0.0;
    }

    public synchronized void resetStatistics() {
        clearHistory();
        this.lastTargetId = -1;
    }
}
