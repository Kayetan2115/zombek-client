package pl.zombek.client.core.tracking;

/**
 * Thread-safe combat combo tracking engine.
 * Records hit registration, miss events, target changes, and handles timeout expirations.
 * Pure Java 17 logic without direct Minecraft dependencies.
 */
public class ComboTracker {

    private static final ComboTracker INSTANCE = new ComboTracker();

    private int currentCombo = 0;
    private int bestCombo = 0;
    private int targetEntityId = -1;
    private long lastHitTimeNanos = 0;
    private long comboStartTimeNanos = 0;
    private boolean comboActive = false;

    // Minimum interval between registering hits (to prevent double-counting within the same tick)
    private static final long MIN_HIT_INTERVAL_NANOS = 50_000_000L; // 50ms

    private ComboTracker() {}

    public static ComboTracker getInstance() {
        return INSTANCE;
    }

    /**
     * Records a confirmed or highly-confident hit on a target entity.
     *
     * @param entityId            Target entity unique ID
     * @param resetOnTargetChange Whether changing target resets the combo
     */
    public synchronized void recordHit(int entityId, boolean resetOnTargetChange) {
        long now = System.nanoTime();

        // Prevent duplicate hit registration within 50ms
        if (now - lastHitTimeNanos < MIN_HIT_INTERVAL_NANOS) {
            return;
        }

        if (resetOnTargetChange && targetEntityId != -1 && targetEntityId != entityId) {
            // Target changed -> reset combo to 1 on the new target
            currentCombo = 1;
            comboStartTimeNanos = now;
        } else {
            currentCombo++;
            if (!comboActive) {
                comboStartTimeNanos = now;
            }
        }

        if (currentCombo > bestCombo) {
            bestCombo = currentCombo;
        }

        targetEntityId = entityId;
        lastHitTimeNanos = now;
        comboActive = true;
    }

    /**
     * Records a miss click (swing without connecting).
     */
    public synchronized void recordMiss(boolean resetOnMiss) {
        if (resetOnMiss && comboActive) {
            currentCombo = 0;
            targetEntityId = -1;
            comboActive = false;
        }
    }

    /**
     * Called on client tick to check for combo timeout.
     *
     * @param timeoutMs Timeout in milliseconds (e.g. 2000ms)
     */
    public synchronized void onTick(long timeoutMs) {
        if (!comboActive) return;

        long now = System.nanoTime();
        long elapsedNanos = now - lastHitTimeNanos;
        long timeoutNanos = timeoutMs * 1_000_000L;

        if (elapsedNanos > timeoutNanos) {
            currentCombo = 0;
            targetEntityId = -1;
            comboActive = false;
        }
    }

    /**
     * Resets combo upon player death or world change.
     */
    public synchronized void resetCombo() {
        currentCombo = 0;
        targetEntityId = -1;
        comboActive = false;
        lastHitTimeNanos = 0;
        comboStartTimeNanos = 0;
    }

    /**
     * Resets both current and best combo records.
     */
    public synchronized void resetAll() {
        resetCombo();
        bestCombo = 0;
    }

    public synchronized int getCurrentCombo() {
        return currentCombo;
    }

    public synchronized int getBestCombo() {
        return bestCombo;
    }

    public synchronized int getTargetEntityId() {
        return targetEntityId;
    }

    public synchronized long getLastHitTimeNanos() {
        return lastHitTimeNanos;
    }

    public synchronized boolean isComboActive() {
        return comboActive;
    }

    public synchronized long getComboDurationMs() {
        if (!comboActive) return 0;
        return (System.nanoTime() - comboStartTimeNanos) / 1_000_000L;
    }
}
