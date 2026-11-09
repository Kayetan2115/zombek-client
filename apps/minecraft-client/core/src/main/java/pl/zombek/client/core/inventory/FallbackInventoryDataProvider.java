package pl.zombek.client.core.inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Null Object fallback for inventory queries when in main menu or during tests.
 */
public class FallbackInventoryDataProvider implements IInventoryDataProvider {

    private boolean playerAvailable = false;
    private final List<InventoryItemData> items = new ArrayList<>();
    private String worldId = "test_world";
    private String dimensionId = "minecraft:overworld";

    @Override
    public boolean isPlayerAvailable() {
        return playerAvailable;
    }

    public void setPlayerAvailable(boolean playerAvailable) {
        this.playerAvailable = playerAvailable;
    }

    @Override
    public List<InventoryItemData> captureInventoryItems() {
        return new ArrayList<>(items);
    }

    public void setItems(List<InventoryItemData> newItems) {
        this.items.clear();
        if (newItems != null) {
            this.items.addAll(newItems);
        }
    }

    public void addItem(InventoryItemData item) {
        if (item != null) {
            this.items.add(item);
        }
    }

    public void clearItems() {
        this.items.clear();
    }

    @Override
    public String getCurrentWorldId() {
        return worldId;
    }

    public void setWorldId(String worldId) {
        this.worldId = worldId;
    }

    @Override
    public String getDimensionId() {
        return dimensionId;
    }

    public void setDimensionId(String dimensionId) {
        this.dimensionId = dimensionId;
    }
}
