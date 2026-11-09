package pl.zombek.client.quilt.integration;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.Box;
import pl.zombek.client.core.worldvisual.lighting.DynamicLightSource;
import pl.zombek.client.core.worldvisual.lighting.DynamicLightSourceRegistry;
import pl.zombek.client.core.worldvisual.lighting.DynamicLightSourceType;
import pl.zombek.client.core.worldvisual.lighting.IDynamicLightProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Quilt platform implementation of IDynamicLightProvider.
 * Gathers dynamic light candidates from player held items, nearby dropped items,
 * and illuminated/burning entities in the client world using Yarn mappings.
 */
public class QuiltDynamicLightProvider implements IDynamicLightProvider {

    private static final double SCAN_RANGE = 24.0;

    @Override
    public List<DynamicLightSource> collectCandidateSources(
            double playerX, double playerY, double playerZ,
            boolean includeMainHand, boolean includeOffhand,
            boolean includeDroppedItems, boolean includeEntities
    ) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null || client.player == null) {
            return Collections.emptyList();
        }

        List<DynamicLightSource> sources = new ArrayList<>();

        // 1. Main hand item
        if (includeMainHand) {
            ItemStack mainStack = client.player.getMainHandStack();
            if (mainStack != null && !mainStack.isEmpty()) {
                String itemId = Registries.ITEM.getId(mainStack.getItem()).toString();
                int light = DynamicLightSourceRegistry.getInstance().getLightLevelForItem(itemId);
                if (light > 0) {
                    sources.add(new DynamicLightSource(
                            "player_main_hand",
                            DynamicLightSourceType.MAIN_HAND,
                            client.player.getX(),
                            client.player.getEyeY(),
                            client.player.getZ(),
                            light,
                            0xFFFFA0
                    ));
                }
            }
        }

        // 2. Offhand item
        if (includeOffhand) {
            ItemStack offStack = client.player.getOffHandStack();
            if (offStack != null && !offStack.isEmpty()) {
                String itemId = Registries.ITEM.getId(offStack.getItem()).toString();
                int light = DynamicLightSourceRegistry.getInstance().getLightLevelForItem(itemId);
                if (light > 0) {
                    sources.add(new DynamicLightSource(
                            "player_offhand",
                            DynamicLightSourceType.OFFHAND,
                            client.player.getX(),
                            client.player.getEyeY(),
                            client.player.getZ(),
                            light,
                            0xFFFFA0
                    ));
                }
            }
        }

        // 3. Nearby dropped items and living entities
        if (includeDroppedItems || includeEntities) {
            Box box = new Box(
                    playerX - SCAN_RANGE, playerY - SCAN_RANGE, playerZ - SCAN_RANGE,
                    playerX + SCAN_RANGE, playerY + SCAN_RANGE, playerZ + SCAN_RANGE
            );

            for (Entity entity : client.world.getOtherEntities(client.player, box)) {
                if (entity == null || !entity.isAlive()) {
                    continue;
                }

                if (includeDroppedItems && entity instanceof ItemEntity itemEntity) {
                    ItemStack stack = itemEntity.getStack();
                    if (stack != null && !stack.isEmpty()) {
                        String itemId = Registries.ITEM.getId(stack.getItem()).toString();
                        int light = DynamicLightSourceRegistry.getInstance().getLightLevelForItem(itemId);
                        if (light > 0) {
                            sources.add(new DynamicLightSource(
                                    "item_" + entity.getId(),
                                    DynamicLightSourceType.DROPPED_ITEM,
                                    entity.getX(),
                                    entity.getY() + 0.2,
                                    entity.getZ(),
                                    light,
                                    0xFFFFA0
                            ));
                        }
                    }
                } else if (includeEntities && entity instanceof LivingEntity living) {
                    String entityType = Registries.ENTITY_TYPE.getId(living.getType()).getPath();
                    int light = DynamicLightSourceRegistry.getInstance().getLightLevelForEntity(entityType);

                    if (living.isGlowing()) {
                        light = Math.max(light, 9);
                    }
                    if (living.isOnFire()) {
                        light = Math.max(light, 12);
                    }

                    if (light > 0) {
                        sources.add(new DynamicLightSource(
                                "entity_" + entity.getId(),
                                DynamicLightSourceType.LIVING_ENTITY,
                                living.getX(),
                                living.getY() + living.getHeight() * 0.5,
                                living.getZ(),
                                light,
                                0xFFFFA0
                        ));
                    }
                }
            }
        }

        return sources;
    }
}
