package pl.zombek.client.core.inventory;

import java.util.List;

/**
 * Platform provider interface supplying live player inventory contents across mod loaders.
 */
public interface IInventoryDataProvider {

    /**
     * Checks if the player is currently in a loaded world and has an inventory accessible.
     */
    boolean isPlayerAvailable();

    /**
     * Captures current items from all player inventory slots.
     */
    List<InventoryItemData> captureInventoryItems();

    /**
     * Gets current world identifier (world name or server address) for snapshot scoping.
     */
    String getCurrentWorldId();

    /**
     * Gets current dimension identifier (e.g. "minecraft:overworld").
     */
    String getDimensionId();
}
