package pl.zombek.client.quilt.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.zombek.client.quilt.input.QuiltKeybindManager;
import pl.zombek.client.quilt.platform.QuiltPlatformAdapter;

/**
 * Minimal client mixin hooking MinecraftClient lifecycle and tick events on Quilt Loader.
 * Purely forwards control to QuiltPlatformAdapter and QuiltKeybindManager without any business logic.
 */
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(RunArgs args, CallbackInfo ci) {
        QuiltPlatformAdapter.onClientReady((MinecraftClient) (Object) this);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        QuiltKeybindManager.getInstance().onClientTick((MinecraftClient) (Object) this);
    }
}
