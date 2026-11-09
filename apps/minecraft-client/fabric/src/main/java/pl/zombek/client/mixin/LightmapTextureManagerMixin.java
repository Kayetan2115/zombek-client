package pl.zombek.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import pl.zombek.client.core.worldvisual.fullbright.FullBrightController;

/**
 * Mixin injecting into LightmapTextureManager to integrate Full Bright brightness enhancement
 * directly into the client-side lightmap calculation without corrupting vanilla gamma options.
 */
@Mixin(LightmapTextureManager.class)
public abstract class LightmapTextureManagerMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Redirect(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;"
            )
    )
    private Object zombekclient$onGetGamma(SimpleOption<?> option) {
        Object val = option.getValue();
        if (this.client != null && this.client.options != null && option == this.client.options.getGamma() && val instanceof Double d) {
            return FullBrightController.getInstance().computeEffectiveGamma(d);
        }
        return val;
    }
}
