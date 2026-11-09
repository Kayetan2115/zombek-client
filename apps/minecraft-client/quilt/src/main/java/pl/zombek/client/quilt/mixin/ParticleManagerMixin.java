package pl.zombek.client.quilt.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.zombek.client.core.rendereffect.particle.ParticleRenderController;

/**
 * Quilt Mixin intercepting particle emissions to apply performance caps,
 * distance culling, category throttling, and custom rules.
 */
@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

    @Inject(
            method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void zombekclient$onAddParticle(
            ParticleEffect parameters,
            double x, double y, double z,
            double velocityX, double velocityY, double velocityZ,
            CallbackInfoReturnable<Particle> cir
    ) {
        ParticleRenderController controller = ParticleRenderController.getInstance();
        if (controller.isActive()) {
            MinecraftClient client = MinecraftClient.getInstance();
            double px = (client != null && client.player != null) ? client.player.getX() : 0.0;
            double py = (client != null && client.player != null) ? client.player.getY() : 0.0;
            double pz = (client != null && client.player != null) ? client.player.getZ() : 0.0;

            String id = "";
            if (parameters != null && parameters.getType() != null) {
                id = Registries.PARTICLE_TYPE.getId(parameters.getType()).toString();
            }

            if (!controller.shouldSpawnParticle(id, x, y, z, px, py, pz)) {
                cir.setReturnValue(null);
            }
        }
    }
}
