package pl.zombek.client.core.tracking;

/**
 * Thread-safe tracker measuring fired projectiles and observed hits.
 * Only records genuine client projectile interactions; if server confirmation is unavailable,
 * notes that hits cannot be confirmed.
 */
public class ProjectileTracker {
    private static final ProjectileTracker INSTANCE = new ProjectileTracker();

    private int projectilesFired = 0;
    private int observedHits = 0;
    private ProjectileType lastProjectileType = ProjectileType.OTHER;
    private double lastDistance = 0.0;

    private ProjectileTracker() {}

    public static ProjectileTracker getInstance() {
        return INSTANCE;
    }

    public synchronized void recordProjectileFired(ProjectileType type) {
        this.projectilesFired++;
        this.lastProjectileType = (type != null) ? type : ProjectileType.OTHER;
    }

    public synchronized void recordProjectileHit(double distance) {
        this.observedHits++;
        if (distance > 0.0) {
            this.lastDistance = distance;
        }
    }

    public synchronized int getProjectilesFired() {
        return this.projectilesFired;
    }

    public synchronized int getObservedHits() {
        return this.observedHits;
    }

    public synchronized double getHitRate() {
        if (this.projectilesFired == 0) {
            return 0.0;
        }
        return ((double) this.observedHits / (double) this.projectilesFired) * 100.0;
    }

    public synchronized ProjectileType getLastProjectileType() {
        return this.lastProjectileType;
    }

    public synchronized double getLastDistance() {
        return this.lastDistance;
    }

    public synchronized void reset() {
        this.projectilesFired = 0;
        this.observedHits = 0;
        this.lastDistance = 0.0;
        this.lastProjectileType = ProjectileType.OTHER;
    }
}
