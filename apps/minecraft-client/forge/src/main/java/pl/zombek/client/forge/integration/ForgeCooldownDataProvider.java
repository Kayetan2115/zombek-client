package pl.zombek.client.forge.integration;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import pl.zombek.client.core.data.CooldownItemData;
import pl.zombek.client.core.data.ICooldownDataProvider;

import java.util.*;

/**
 * Forge provider inspecting player inventory and Minecraft's ItemCooldowns for active cooldowns.
 */
public class ForgeCooldownDataProvider implements ICooldownDataProvider {

    @Override
    public List<CooldownItemData> getActiveCooldowns() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return Collections.emptyList();
        }

        ItemCooldowns mgr = mc.player.getCooldowns();
        if (mgr == null) return Collections.emptyList();

        Inventory inv = mc.player.getInventory();
        Set<Item> checkedItems = new HashSet<>();
        List<CooldownItemData> active = new ArrayList<>();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack == null || stack.isEmpty()) continue;

            Item item = stack.getItem();
            if (checkedItems.add(item) && mgr.isOnCooldown(item)) {
                float progress = mgr.getCooldownPercent(item, 0.0f);
                float estimatedSeconds = progress * 15.0f;
                active.add(new CooldownItemData(
                        item.toString(),
                        stack.getHoverName().getString(),
                        progress,
                        estimatedSeconds,
                        stack
                ));
            }
        }
        return active;
    }
}
