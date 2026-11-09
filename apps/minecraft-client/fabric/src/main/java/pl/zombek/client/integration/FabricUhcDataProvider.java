package pl.zombek.client.integration;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import pl.zombek.client.core.uhc.IUhcDataProvider;
import pl.zombek.client.core.uhc.UhcTrackedItem;

import java.util.Locale;

/**
 * Fabric provider supplying live player health, hunger, absorption, and inventory counts for UHC Overlay.
 */
public class FabricUhcDataProvider implements IUhcDataProvider {

    @Override
    public boolean isPlayerInGame() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.player != null && client.world != null;
    }

    @Override
    public float getPlayerHealth() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.player != null ? client.player.getHealth() : 20.0f;
    }

    @Override
    public float getPlayerMaxHealth() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.player != null ? client.player.getMaxHealth() : 20.0f;
    }

    @Override
    public float getPlayerAbsorption() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.player != null ? client.player.getAbsorptionAmount() : 0.0f;
    }

    @Override
    public int getPlayerFoodLevel() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.player != null && client.player.getHungerManager() != null
                ? client.player.getHungerManager().getFoodLevel() : 20;
    }

    @Override
    public float getPlayerSaturation() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.player != null && client.player.getHungerManager() != null
                ? client.player.getHungerManager().getSaturationLevel() : 5.0f;
    }

    @Override
    public int getItemCount(UhcTrackedItem item) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return 0;
        }

        PlayerInventory inv = client.player.getInventory();
        int count = 0;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (stack == null || stack.isEmpty()) continue;

            if (matchesItem(stack, item)) {
                count += stack.getCount();
            }
        }

        return count;
    }

    private boolean matchesItem(ItemStack stack, UhcTrackedItem item) {
        switch (item) {
            case GOLDEN_APPLE:
                return stack.isOf(Items.GOLDEN_APPLE) || stack.isOf(Items.ENCHANTED_GOLDEN_APPLE);
            case GOLDEN_HEAD:
                if (stack.isOf(Items.PLAYER_HEAD)) {
                    String name = stack.getName().getString().toLowerCase(Locale.ROOT);
                    return name.contains("head") || name.contains("ghead") || name.contains("golden");
                }
                return false;
            case ARROW:
                return stack.isOf(Items.ARROW) || stack.isOf(Items.SPECTRAL_ARROW) || stack.isOf(Items.TIPPED_ARROW);
            case ENDER_PEARL:
                return stack.isOf(Items.ENDER_PEARL);
            case HEALING:
                return stack.isOf(Items.POTION) || stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION);
            case BLOCKS:
                return stack.getItem() instanceof BlockItem;
            case FLINT_AND_STEEL:
                return stack.isOf(Items.FLINT_AND_STEEL);
            case WATER_BUCKET:
                return stack.isOf(Items.WATER_BUCKET);
            case LAVA_BUCKET:
                return stack.isOf(Items.LAVA_BUCKET);
            default:
                return false;
        }
    }
}
