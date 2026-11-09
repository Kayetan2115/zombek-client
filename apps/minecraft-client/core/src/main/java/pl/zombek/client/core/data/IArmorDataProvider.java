package pl.zombek.client.core.data;

import java.util.List;

/**
 * Provider contract supplying real-time snapshots of the player's equipped armor items.
 */
public interface IArmorDataProvider {
    /**
     * Returns the 4 armor slots (HELMET, CHESTPLATE, LEGGINGS, BOOTS).
     */
    List<ArmorItemData> getEquippedArmor();
}
