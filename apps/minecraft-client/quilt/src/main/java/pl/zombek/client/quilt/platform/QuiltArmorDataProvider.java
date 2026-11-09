package pl.zombek.client.quilt.platform;

import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import pl.zombek.client.core.data.ArmorItemData;
import pl.zombek.client.core.data.ArmorSlot;
import pl.zombek.client.core.data.IArmorDataProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Quilt provider supplying real-time snapshots of the player's equipped armor.
 */
public class QuiltArmorDataProvider implements IArmorDataProvider {

    @Override
    public List<ArmorItemData> getEquippedArmor() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return Arrays.asList(
                    ArmorItemData.empty(ArmorSlot.HELMET),
                    ArmorItemData.empty(ArmorSlot.CHESTPLATE),
                    ArmorItemData.empty(ArmorSlot.LEGGINGS),
                    ArmorItemData.empty(ArmorSlot.BOOTS)
            );
        }

        List<ArmorItemData> list = new ArrayList<>(4);
        for (ArmorSlot slot : ArmorSlot.values()) {
            ItemStack stack = client.player.getInventory().getArmorStack(slot.getInventoryIndex());
            if (stack == null || stack.isEmpty()) {
                list.add(ArmorItemData.empty(slot));
            } else {
                String name = stack.getName().getString();
                String id = stack.getItem().toString();
                boolean damageable = stack.isDamageable();
                int maxDamage = stack.getMaxDamage();
                int damage = stack.getDamage();

                List<String> enchantNames = new ArrayList<>();
                try {
                    Map<Enchantment, Integer> enchants = EnchantmentHelper.get(stack);
                    for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                        String encName = entry.getKey().getName(entry.getValue()).getString();
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
