package pl.zombek.client.core.hud.potions;

import java.util.List;

/**
 * Platform provider interface for querying player's active status/potion effects.
 */
public interface IPotionEffectsDataProvider {

    /**
     * Checks if the player is in an active game session.
     */
    boolean isPlayerAvailable();

    /**
     * Retrieves all active status effects on the local player.
     * Guaranteed to return a non-null list.
     */
    List<StatusEffectData> getActiveEffects();
}
