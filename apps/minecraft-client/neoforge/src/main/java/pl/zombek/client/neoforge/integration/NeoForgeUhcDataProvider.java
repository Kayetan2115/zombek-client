package pl.zombek.client.neoforge.integration;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import pl.zombek.client.core.uhc.IUhcDataProvider;
import pl.zombek.client.core.uhc.UhcTrackedItem;

import java.util.Locale;

/**
 * NeoForge provider supplying live player health, hunger, absorption, and inventory counts for UHC Overlay.
 */
public class NeoForgeUhcDataProvider implements IUhcDataProvider {

    @Override
    public boolean isPlayerInGame() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.player != null && mc.level != null;
    }

    @Override
    public float getPlayerHealth() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.player != null ? mc.player.getHealth() : 20.0f;
    }

    @Override
    public float getPlayerMaxHealth() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.player != null ? mc.player.getMaxHealth() : 20.0f;
    }

    @Override
    public float getPlayerAbsorption() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.player != null ? mc.player.getAbsorptionAmount() : 0.0f;
    }

    @Override
    public int getPlayerFoodLevel() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.player != null && mc.player.getFoodData() != null
                ? mc.player.getFoodData().getFoodLevel() : 20;
    }

    @Override
    public float getPlayerSaturation() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.player != null && mc.player.getFoodData() != null
                ? mc.player.getFoodData().getSaturationLevel() : 5.0f;
    }

    @Override
    public int getItemCount(UhcTrackedItem item) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return 0;
        }

        Inventory inv = mc.player.getInventory();
        int count = 0;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
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
                return stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE);
            case GOLDEN_HEAD:
                if (stack.is(Items.PLAYER_HEAD)) {
                    String name = stack.getHoverName().getString().toLowerCase(Locale.ROOT);
                    return name.contains("head") || name.contains("ghead") || name.contains("golden");
                }
                return false;
            case ARROW:
                return stack.is(Items.ARROW) || stack.is(Items.SPECTRAL_ARROW) || stack.is(Items.TIPPED_ARROW);
            case ENDER_PEARL:
                return stack.is(Items.ENDER_PEARL);
            case HEALING:
                return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION);
            case BLOCKS:
                return stack.getItem() instanceof BlockItem;
            case FLINT_AND_STEEL:
                return stack.is(Items.FLINT_AND_STEEL);
            case WATER_BUCKET:
                return stack.is(Items.WATER_BUCKET);
            case LAVA_BUCKET:
                return stack.is(Items.LAVA_BUCKET);
            default:
                return false;
        }
    }
}
