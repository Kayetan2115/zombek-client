package pl.zombek.client.core.data;

import java.util.List;

/**
 * Provider contract supplying active item cooldowns from Minecraft's ItemCooldownManager.
 */
public interface ICooldownDataProvider {
    /**
     * Returns the list of currently cooling down items.
     */
    List<CooldownItemData> getActiveCooldowns();
}
