package pl.zombek.client.forge.integration;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import pl.zombek.client.core.data.ArmorItemData;
import pl.zombek.client.core.data.ArmorSlot;
import pl.zombek.client.core.data.IArmorDataProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Forge provider supplying real-time snapshots of the player's equipped armor.
 */
public class ForgeArmorDataProvider implements IArmorDataProvider {

    @Override
    public List<ArmorItemData> getEquippedArmor() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return Arrays.asList(
                    ArmorItemData.empty(ArmorSlot.HELMET),
                    ArmorItemData.empty(ArmorSlot.CHESTPLATE),
                    ArmorItemData.empty(ArmorSlot.LEGGINGS),
                    ArmorItemData.empty(ArmorSlot.BOOTS)
            );
        }

        List<ArmorItemData> list = new ArrayList<>(4);
        for (ArmorSlot slot : ArmorSlot.values()) {
            ItemStack stack = mc.player.getInventory().armor.get(slot.getInventoryIndex());
            if (stack == null || stack.isEmpty()) {
                list.add(ArmorItemData.empty(slot));
            } else {
                String name = stack.getHoverName().getString();
                String id = stack.getItem().toString();
                boolean damageable = stack.isDamageableItem();
                int maxDamage = stack.getMaxDamage();
                int damage = stack.getDamageValue();

                List<String> enchantNames = new ArrayList<>();
                try {
                    Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);
                    for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                        String encName = entry.getKey().getFullname(entry.getValue()).getString();
                        enchantNames.add(encName);
                    }
                } catch (Throwable ignored) {}

                list.add(new ArmorItemData(
                        slot,
                        id,
                        name,
                        damage,
                        maxDamage,
                        damageable,
                        false,
                        enchantNames,
                        stack
                ));
            }
        }
        return list;
    }
}
