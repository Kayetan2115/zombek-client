package pl.zombek.client.quilt.integration;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import pl.zombek.client.core.inventory.IInventoryDataProvider;
import pl.zombek.client.core.inventory.InventoryItemData;
import pl.zombek.client.core.inventory.InventorySlotSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Quilt platform data provider capturing player inventory items using Yarn/Quilt mappings.
 */
public class QuiltInventoryDataProvider implements IInventoryDataProvider {

    @Override
    public boolean isPlayerAvailable() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.player != null && client.world != null;
    }

    @Override
    public List<InventoryItemData> captureInventoryItems() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return Collections.emptyList();
        }

        PlayerInventory inv = client.player.getInventory();
        List<InventoryItemData> items = new ArrayList<>();

        // Hotbar: slots 0-8
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty()) {
                items.add(toItemData(stack, i, InventorySlotSource.HOTBAR));
            }
        }

        // Main Inventory: slots 9-35
        for (int i = 9; i < 36; i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty()) {
                items.add(toItemData(stack, i, InventorySlotSource.MAIN_INVENTORY));
            }
        }

        // Armor: slots 36-39
        for (int i = 36; i < 40; i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty()) {
                items.add(toItemData(stack, i, InventorySlotSource.ARMOR));
            }
        }

        // Offhand: slot 40
        if (inv.size() > 40) {
            ItemStack offhand = inv.getStack(40);
            if (!offhand.isEmpty()) {
                items.add(toItemData(offhand, 40, InventorySlotSource.OFFHAND));
            }
        }

        return items;
    }

    private InventoryItemData toItemData(ItemStack stack, int slot, InventorySlotSource source) {
        String itemId = Registries.ITEM.getId(stack.getItem()).toString();
        String itemName = stack.getName().getString();
        int count = stack.getCount();
        int maxStack = stack.getMaxCount();
        boolean isBlock = stack.getItem() instanceof BlockItem;
        boolean isFood = stack.isFood();
        return new InventoryItemData(itemId, itemName, count, maxStack, slot, source, "", stack, isBlock, isFood);
    }

    @Override
    public String getCurrentWorldId() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return null;
        if (client.isInSingleplayer() && client.getServer() != null) {
            return client.getServer().getSaveProperties().getLevelName();
        }
        if (client.getCurrentServerEntry() != null) {
            return client.getCurrentServerEntry().address;
        }
        return null;
    }

    @Override
    public String getDimensionId() {
        MinecraftClient client = MinecraftClient.getInstance();
        return (client != null && client.world != null)
                ? client.world.getRegistryKey().getValue().toString()
                : "minecraft:overworld";
    }
}
