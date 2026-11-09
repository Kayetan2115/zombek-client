package pl.zombek.client.forge.integration;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import pl.zombek.client.core.hud.potions.IPotionEffectsDataProvider;
import pl.zombek.client.core.hud.potions.StatusEffectCategory;
import pl.zombek.client.core.hud.potions.StatusEffectData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Forge platform provider supplying active player status and potion effects.
 */
public class ForgePotionEffectsDataProvider implements IPotionEffectsDataProvider {

    @Override
    public boolean isPlayerAvailable() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.player != null;
    }

    @Override
    public List<StatusEffectData> getActiveEffects() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return Collections.emptyList();
        }

        Collection<MobEffectInstance> effects = mc.player.getActiveEffects();
        if (effects == null || effects.isEmpty()) {
            return Collections.emptyList();
        }

        List<StatusEffectData> list = new ArrayList<>(effects.size());
        for (MobEffectInstance inst : effects) {
            if (inst == null) continue;
            MobEffect effect = inst.getEffect();
            if (effect == null) continue;

            String id = BuiltInRegistries.MOB_EFFECT.getKey(effect) != null
                    ? BuiltInRegistries.MOB_EFFECT.getKey(effect).toString()
                    : "minecraft:effect";

            String name = effect.getDisplayName().getString();
            int amplifier = inst.getAmplifier();
            int durationTicks = inst.getDuration();
            boolean ambient = inst.isAmbient();
            boolean infinite = inst.isInfiniteDuration();

            StatusEffectCategory coreCategory = mapCategory(effect.getCategory());
            int color = effect.getColor();

            list.add(new StatusEffectData(id, name, amplifier, durationTicks, ambient, infinite, coreCategory, color));
        }

        return list;
    }

    private StatusEffectCategory mapCategory(MobEffectCategory category) {
        if (category == null) return StatusEffectCategory.BENEFICIAL;
        switch (category) {
            case HARMFUL: return StatusEffectCategory.HARMFUL;
            case NEUTRAL: return StatusEffectCategory.NEUTRAL;
            case BENEFICIAL:
            default: return StatusEffectCategory.BENEFICIAL;
        }
    }
}
