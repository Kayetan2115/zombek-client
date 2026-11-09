package pl.zombek.client.quilt.platform;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import pl.zombek.client.core.data.CooldownItemData;
import pl.zombek.client.core.data.ICooldownDataProvider;

import java.util.*;

/**
 * Quilt provider inspecting player inventory and Minecraft's ItemCooldownManager for active cooldowns.
 */
public class QuiltCooldownDataProvider implements ICooldownDataProvider {

    @Override
    public List<CooldownItemData> getActiveCooldowns() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return Collections.emptyList();
        }

        ItemCooldownManager mgr = client.player.getItemCooldownManager();
        if (mgr == null) return Collections.emptyList();

        PlayerInventory inv = client.player.getInventory();
        Set<Item> checkedItems = new HashSet<>();
        List<CooldownItemData> active = new ArrayList<>();

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (stack == null || stack.isEmpty()) continue;

            Item item = stack.getItem();
            if (checkedItems.add(item) && mgr.isCoolingDown(item)) {
                float progress = mgr.getCooldownProgress(item, 0.0f);
                float estimatedSeconds = progress * 15.0f;
                active.add(new CooldownItemData(
                        item.toString(),
                        stack.getName().getString(),
                        progress,
                        estimatedSeconds,
                        stack
                ));
            }
        }
        return active;
    }
}
