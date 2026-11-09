package pl.zombek.client.neoforge.integration;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import pl.zombek.client.core.worldvisual.lighting.DynamicLightSource;
import pl.zombek.client.core.worldvisual.lighting.DynamicLightSourceRegistry;
import pl.zombek.client.core.worldvisual.lighting.DynamicLightSourceType;
import pl.zombek.client.core.worldvisual.lighting.IDynamicLightProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * NeoForge platform implementation of IDynamicLightProvider.
 * Collects dynamic light sources from held items, dropped items, and living entities
 * using official Mojang mappings.
 */
public class NeoForgeDynamicLightProvider implements IDynamicLightProvider {

    private static final double SCAN_RANGE = 24.0;

    @Override
    public List<DynamicLightSource> collectCandidateSources(
            double playerX, double playerY, double playerZ,
            boolean includeMainHand, boolean includeOffhand,
            boolean includeDroppedItems, boolean includeEntities
    ) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || mc.player == null) {
            return Collections.emptyList();
        }

        List<DynamicLightSource> sources = new ArrayList<>();

        // 1. Main hand
        if (includeMainHand) {
            ItemStack mainStack = mc.player.getMainHandItem();
            if (mainStack != null && !mainStack.isEmpty()) {
                String itemId = BuiltInRegistries.ITEM.getKey(mainStack.getItem()).toString();
                int light = DynamicLightSourceRegistry.getInstance().getLightLevelForItem(itemId);
                if (light > 0) {
                    sources.add(new DynamicLightSource(
                            "player_main_hand",
                            DynamicLightSourceType.MAIN_HAND,
                            mc.player.getX(),
                            mc.player.getEyeY(),
                            mc.player.getZ(),
                            light,
                            0xFFFFA0
                    ));
                }
            }
        }

        // 2. Offhand
        if (includeOffhand) {
            ItemStack offStack = mc.player.getOffhandItem();
            if (offStack != null && !offStack.isEmpty()) {
                String itemId = BuiltInRegistries.ITEM.getKey(offStack.getItem()).toString();
                int light = DynamicLightSourceRegistry.getInstance().getLightLevelForItem(itemId);
                if (light > 0) {
                    sources.add(new DynamicLightSource(
                            "player_offhand",
                            DynamicLightSourceType.OFFHAND,
                            mc.player.getX(),
                            mc.player.getEyeY(),
                            mc.player.getZ(),
                            light,
                            0xFFFFA0
                    ));
                }
            }
        }

        // 3. Nearby items and entities
        if (includeDroppedItems || includeEntities) {
            AABB box = new AABB(
                    playerX - SCAN_RANGE, playerY - SCAN_RANGE, playerZ - SCAN_RANGE,
                    playerX + SCAN_RANGE, playerY + SCAN_RANGE, playerZ + SCAN_RANGE
            );

            for (Entity entity : mc.level.getEntitiesOfClass(Entity.class, box, e -> e != mc.player)) {
                if (entity == null || !entity.isAlive()) {
                    continue;
                }

                if (includeDroppedItems && entity instanceof ItemEntity itemEntity) {
                    ItemStack stack = itemEntity.getItem();
                    if (stack != null && !stack.isEmpty()) {
                        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
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
                    String entityType = BuiltInRegistries.ENTITY_TYPE.getKey(living.getType()).getPath();
                    int light = DynamicLightSourceRegistry.getInstance().getLightLevelForEntity(entityType);

                    if (living.hasGlowingTag()) {
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
                                living.getY() + living.getBbHeight() * 0.5,
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
