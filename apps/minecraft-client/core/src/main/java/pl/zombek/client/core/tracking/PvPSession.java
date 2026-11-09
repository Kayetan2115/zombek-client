package pl.zombek.client.core.tracking;

/**
 * Encapsulates real-time statistics for an individual PvP session.
 */
public class PvPSession {
    private long startTime;
    private long endTime;
    private boolean active;

    // Melee statistics
    private int meleeAttacks = 0;
    private int observedHits = 0;
    private int highestCombo = 0;

    // Health statistics
    private float damageReceived = 0.0f;
    private int healthChangeEvents = 0;
    private float playerHealth = 20.0f;
    private float playerAbsorption = 0.0f;
    private float targetHealth = -1.0f; // -1 indicates N/A (no reliable server target health)

    public PvPSession() {
        this.startTime = System.currentTimeMillis();
        this.endTime = 0L;
        this.active = true;
    }

    public synchronized void recordAttackAttempt() {
        if (!this.active) {
            this.active = true;
            this.startTime = System.currentTimeMillis();
        }
        this.meleeAttacks++;
    }

    public synchronized void recordObservedHit() {
        if (!this.active) {
            this.active = true;
            this.startTime = System.currentTimeMillis();
        }
        this.observedHits++;
    }

    public synchronized void recordCombo(int currentCombo) {
        if (currentCombo > this.highestCombo) {
            this.highestCombo = currentCombo;
        }
    }

    public synchronized void recordDamageReceived(float amount) {
        if (amount > 0.0f) {
            this.damageReceived += amount;
            this.healthChangeEvents++;
        }
    }

    public synchronized void updatePlayerHealth(float health, float absorption) {
        this.playerHealth = health;
        this.playerAbsorption = absorption;
    }

    public synchronized void updateTargetHealth(float health) {
        this.targetHealth = health;
    }

    public synchronized void clearTargetHealth() {
        this.targetHealth = -1.0f;
    }

    public synchronized double getEstimatedHitRate() {
        if (this.meleeAttacks == 0) {
            return 0.0;
        }
        return ((double) this.observedHits / (double) this.meleeAttacks) * 100.0;
    }

    public synchronized long getDurationMs() {
        if (!this.active) {
            return (this.endTime > this.startTime) ? (this.endTime - this.startTime) : 0L;
        }
        return System.currentTimeMillis() - this.startTime;
    }

    public synchronized void end() {
        this.active = false;
        this.endTime = System.currentTimeMillis();
    }

    public synchronized void reset() {
        this.startTime = System.currentTimeMillis();
        this.endTime = 0L;
        this.active = true;
        this.meleeAttacks = 0;
        this.observedHits = 0;
        this.highestCombo = 0;
        this.damageReceived = 0.0f;
        this.healthChangeEvents = 0;
        this.targetHealth = -1.0f;
    }

    // Getters
    public synchronized boolean isActive() { return active; }
    public synchronized int getMeleeAttacks() { return meleeAttacks; }
    public synchronized int getObservedHits() { return observedHits; }
    public synchronized int getHighestCombo() { return highestCombo; }
    public synchronized float getDamageReceived() { return damageReceived; }
    public synchronized int getHealthChangeEvents() { return healthChangeEvents; }
    public synchronized float getPlayerHealth() { return playerHealth; }
    public synchronized float getPlayerAbsorption() { return playerAbsorption; }
    public synchronized float getTargetHealth() { return targetHealth; }
    public synchronized boolean hasValidTargetHealth() { return targetHealth >= 0.0f; }
}
