package pl.zombek.client.neoforge.integration;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import pl.zombek.client.core.data.CrosshairTargetType;
import pl.zombek.client.core.data.ICrosshairTargetProvider;

/**
 * NeoForge provider inspecting client crosshair target, attack cooldown, and GUI state.
 */
public class NeoForgeCrosshairTargetProvider implements ICrosshairTargetProvider {
    private final Minecraft mc;

    public NeoForgeCrosshairTargetProvider(Minecraft mc) {
        this.mc = mc;
    }

    @Override
    public CrosshairTargetType getTargetType() {
        if (mc == null || mc.hitResult == null) {
            return CrosshairTargetType.NONE;
        }
        HitResult.Type type = mc.hitResult.getType();
        if (type == HitResult.Type.ENTITY) {
            return CrosshairTargetType.ENTITY;
        } else if (type == HitResult.Type.BLOCK) {
            return CrosshairTargetType.BLOCK;
        }
        return CrosshairTargetType.NONE;
    }

    @Override
    public float getAttackCooldownProgress() {
        if (mc != null && mc.player != null) {
            return mc.player.getAttackStrengthScale(0.0f);
        }
        return 1.0f;
    }

    @Override
    public boolean isSpectator() {
        return mc != null && mc.player != null && mc.player.isSpectator();
    }

    @Override
    public boolean isHudHidden() {
        return mc != null && mc.options != null && mc.options.hideGui;
    }

    @Override
    public boolean hasOpenScreen() {
        return mc != null && mc.screen != null;
    }
}
