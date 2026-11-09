package pl.zombek.client.quilt.integration;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import pl.zombek.client.core.hud.potions.IPotionEffectsDataProvider;
import pl.zombek.client.core.hud.potions.StatusEffectData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Quilt platform data provider supplying active status and potion effects.
 */
public class QuiltPotionEffectsDataProvider implements IPotionEffectsDataProvider {

    @Override
    public boolean isPlayerAvailable() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.player != null;
    }

    @Override
    public List<StatusEffectData> getActiveEffects() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return Collections.emptyList();
        }

        Collection<StatusEffectInstance> effects = client.player.getStatusEffects();
        if (effects == null || effects.isEmpty()) {
            return Collections.emptyList();
        }

        List<StatusEffectData> list = new ArrayList<>(effects.size());
        for (StatusEffectInstance inst : effects) {
            if (inst == null) continue;
            StatusEffect type = inst.getEffectType();
            if (type == null) continue;

            String id = Registries.STATUS_EFFECT.getId(type) != null
                    ? Registries.STATUS_EFFECT.getId(type).toString()
                    : "minecraft:effect";

            String name = type.getName().getString();
            int amplifier = inst.getAmplifier();
            int durationTicks = inst.getDuration();
            boolean ambient = inst.isAmbient();
            boolean infinite = inst.isInfinite();

            pl.zombek.client.core.hud.potions.StatusEffectCategory coreCategory = mapCategory(type.getCategory());
            int color = type.getColor();

            list.add(new StatusEffectData(id, name, amplifier, durationTicks, ambient, infinite, coreCategory, color));
        }

        return list;
    }

    private pl.zombek.client.core.hud.potions.StatusEffectCategory mapCategory(StatusEffectCategory category) {
        if (category == null) return pl.zombek.client.core.hud.potions.StatusEffectCategory.BENEFICIAL;
        switch (category) {
            case HARMFUL:
                return pl.zombek.client.core.hud.potions.StatusEffectCategory.HARMFUL;
            case NEUTRAL:
                return pl.zombek.client.core.hud.potions.StatusEffectCategory.NEUTRAL;
            case BENEFICIAL:
            default:
                return pl.zombek.client.core.hud.potions.StatusEffectCategory.BENEFICIAL;
        }
    }
}
