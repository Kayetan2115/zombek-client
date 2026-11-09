package pl.zombek.client.quilt.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.zombek.client.core.camera.ZoomController;

/**
 * Quilt Mixin intercepting mouse wheel scrolling when Zoom is actively engaged
 * to adjust zoom factor without switching hotbar inventory slots.
 */
@Mixin(Mouse.class)
public abstract class MouseMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void zombekclient$onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        ZoomController zoom = ZoomController.getInstance();
        if (zoom.isActive() && client.currentScreen == null) {
            zoom.onMouseScroll(vertical);
            ci.cancel();
        }
    }
}
