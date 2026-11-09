package pl.zombek.client.integration;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import pl.zombek.client.core.data.CrosshairTargetType;
import pl.zombek.client.core.data.ICrosshairTargetProvider;

/**
 * Fabric provider inspecting client target, attack cooldown, and GUI state.
 */
public class FabricCrosshairTargetProvider implements ICrosshairTargetProvider {
    private final MinecraftClient client;

    public FabricCrosshairTargetProvider(MinecraftClient client) {
        this.client = client;
    }

    @Override
    public CrosshairTargetType getTargetType() {
        if (client == null || client.crosshairTarget == null) {
            return CrosshairTargetType.NONE;
        }
        HitResult.Type type = client.crosshairTarget.getType();
        if (type == HitResult.Type.ENTITY) {
            return CrosshairTargetType.ENTITY;
        } else if (type == HitResult.Type.BLOCK) {
            return CrosshairTargetType.BLOCK;
        }
        return CrosshairTargetType.NONE;
    }

    @Override
    public float getAttackCooldownProgress() {
        if (client != null && client.player != null) {
            return client.player.getAttackCooldownProgress(0.0f);
        }
        return 1.0f;
    }

    @Override
    public boolean isSpectator() {
        return client != null && client.player != null && client.player.isSpectator();
    }

    @Override
    public boolean isHudHidden() {
        return client != null && client.options != null && client.options.hudHidden;
    }

    @Override
    public boolean hasOpenScreen() {
        return client != null && client.currentScreen != null;
    }
}
