package pl.zombek.client.mixin;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.zombek.client.core.worldvisual.time.WorldTimeVisualController;
import pl.zombek.client.core.worldvisual.weather.WeatherVisualController;

/**
 * Mixin injecting into World to route visual sky angle, visual time of day,
 * and rain/thunder gradients through ZombekClient world visual controllers for ClientWorld instances.
 * Server tick, day length, mob spawning, and sleep logic remain completely unaffected.
 */
@Mixin(World.class)
public abstract class ClientWorldMixin {

    @Inject(method = "getSkyAngleRadians", at = @At("RETURN"), cancellable = true)
    private void zombekclient$onGetSkyAngleRadians(float tickDelta, CallbackInfoReturnable<Float> cir) {
        if (!((Object) this instanceof ClientWorld world)) {
            return;
        }
        String dimensionId = world.getRegistryKey() != null ? world.getRegistryKey().getValue().toString() : "minecraft:overworld";
        float vanillaRadians = cir.getReturnValue();
        float vanillaAngle = vanillaRadians / 6.2831855F;
        float visualAngle = WorldTimeVisualController.getInstance().getVisualSkyAngle(vanillaAngle, dimensionId);
        cir.setReturnValue(visualAngle * 6.2831855F);
    }

    @Inject(method = "getTimeOfDay", at = @At("RETURN"), cancellable = true)
    private void zombekclient$onGetTimeOfDay(CallbackInfoReturnable<Long> cir) {
        if (!((Object) this instanceof ClientWorld world)) {
            return;
        }
        String dimensionId = world.getRegistryKey() != null ? world.getRegistryKey().getValue().toString() : "minecraft:overworld";
        long visualTime = WorldTimeVisualController.getInstance().getVisualTimeOfDay(cir.getReturnValue(), dimensionId);
        cir.setReturnValue(visualTime);
    }

    @Inject(method = "getRainGradient", at = @At("RETURN"), cancellable = true)
    private void zombekclient$onGetRainGradient(float delta, CallbackInfoReturnable<Float> cir) {
        if (!((Object) this instanceof ClientWorld world)) {
            return;
        }
        String dimensionId = world.getRegistryKey() != null ? world.getRegistryKey().getValue().toString() : "minecraft:overworld";
        float visualRain = WeatherVisualController.getInstance().getVisualRainGradient(cir.getReturnValue(), dimensionId, true);
        cir.setReturnValue(visualRain);
    }

    @Inject(method = "getThunderGradient", at = @At("RETURN"), cancellable = true)
    private void zombekclient$onGetThunderGradient(float delta, CallbackInfoReturnable<Float> cir) {
        if (!((Object) this instanceof ClientWorld world)) {
            return;
        }
        String dimensionId = world.getRegistryKey() != null ? world.getRegistryKey().getValue().toString() : "minecraft:overworld";
        float visualThunder = WeatherVisualController.getInstance().getVisualThunderGradient(cir.getReturnValue(), dimensionId);
        cir.setReturnValue(visualThunder);
    }
}
