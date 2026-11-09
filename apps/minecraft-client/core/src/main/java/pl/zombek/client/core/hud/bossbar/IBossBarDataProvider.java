package pl.zombek.client.core.hud.bossbar;

import java.util.List;

/**
 * Platform provider interface for querying active Boss Bars.
 */
public interface IBossBarDataProvider {

    /**
     * Checks if there is at least one active Boss Bar in the current world.
     */
    boolean hasActiveBossBar();

    /**
     * Retrieves all active Boss Bars ordered by game appearance.
     * Guaranteed non-null.
     */
    List<BossBarData> getActiveBossBars();
}
