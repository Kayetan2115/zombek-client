package pl.zombek.client.neoforge.integration;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import pl.zombek.client.core.inventory.IInventoryDataProvider;
import pl.zombek.client.core.inventory.InventoryItemData;
import pl.zombek.client.core.inventory.InventorySlotSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * NeoForge platform data provider capturing player inventory items using Mojang mappings.
 */
public class NeoForgeInventoryDataProvider implements IInventoryDataProvider {

    @Override
    public boolean isPlayerAvailable() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.player != null && mc.level != null;
    }

    @Override
    public List<InventoryItemData> captureInventoryItems() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return Collections.emptyList();
        }

        Inventory inv = mc.player.getInventory();
        List<InventoryItemData> items = new ArrayList<>();

        // Hotbar: slots 0-8
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                items.add(toItemData(stack, i, InventorySlotSource.HOTBAR));
            }
        }

        // Main Inventory: slots 9-35
        for (int i = 9; i < 36; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                items.add(toItemData(stack, i, InventorySlotSource.MAIN_INVENTORY));
            }
        }

        // Armor: slots 36-39
        for (int i = 36; i < 40; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                items.add(toItemData(stack, i, InventorySlotSource.ARMOR));
            }
        }

        // Offhand: slot 40
        if (inv.getContainerSize() > 40) {
            ItemStack offhand = inv.getItem(40);
            if (!offhand.isEmpty()) {
                items.add(toItemData(offhand, 40, InventorySlotSource.OFFHAND));
            }
        }

        return items;
    }

    private InventoryItemData toItemData(ItemStack stack, int slot, InventorySlotSource source) {
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        String itemName = stack.getHoverName().getString();
        int count = stack.getCount();
        int maxStack = stack.getMaxStackSize();
        boolean isBlock = stack.getItem() instanceof BlockItem;
        boolean isFood = stack.getItem().isEdible();
        return new InventoryItemData(itemId, itemName, count, maxStack, slot, source, "", stack, isBlock, isFood);
    }

    @Override
    public String getCurrentWorldId() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return null;
        if (mc.hasSingleplayerServer() && mc.getSingleplayerServer() != null) {
            return mc.getSingleplayerServer().getWorldData().getLevelName();
        }
        if (mc.getCurrentServer() != null) {
            return mc.getCurrentServer().ip;
        }
        return null;
    }

    @Override
    public String getDimensionId() {
        Minecraft mc = Minecraft.getInstance();
        return (mc != null && mc.level != null)
                ? mc.level.dimension().location().toString()
                : "minecraft:overworld";
    }
}
