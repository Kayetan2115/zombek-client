package pl.zombek.client.core.tracking;

/**
 * Thread-safe manager coordinating PvP sessions across combat tracking systems.
 */
public class PvPSessionManager {
    private static final PvPSessionManager INSTANCE = new PvPSessionManager();

    private PvPSession currentSession = new PvPSession();
    private boolean automaticSessions = true;

    private PvPSessionManager() {}

    public static PvPSessionManager getInstance() {
        return INSTANCE;
    }

    public synchronized PvPSession getCurrentSession() {
        return this.currentSession;
    }

    public synchronized void startNewSession() {
        this.currentSession = new PvPSession();
    }

    public synchronized void resetSession() {
        this.currentSession.reset();
        ComboTracker.getInstance().resetCombo();
        ReachTracker.getInstance().resetStatistics();
        ProjectileTracker.getInstance().reset();
    }

    public synchronized void onWorldChange(boolean resetOnWorldChange) {
        if (resetOnWorldChange) {
            resetSession();
        }
    }

    public synchronized void onPlayerDeath(boolean resetOnDeath) {
        if (resetOnDeath) {
            resetSession();
        }
    }

    public synchronized boolean isAutomaticSessions() {
        return this.automaticSessions;
    }

    public synchronized void setAutomaticSessions(boolean auto) {
        this.automaticSessions = auto;
    }
}
