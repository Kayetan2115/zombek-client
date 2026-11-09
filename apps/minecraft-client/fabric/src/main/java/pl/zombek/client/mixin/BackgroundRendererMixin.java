package pl.zombek.client.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.zombek.client.core.rendereffect.fog.FogEnvironmentType;
import pl.zombek.client.core.rendereffect.fog.FogParameters;
import pl.zombek.client.core.rendereffect.fog.FogVisualController;

/**
 * Mixin hooking BackgroundRenderer to inject custom fog distances, colors,
 * and per-environment profiles.
 */
@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin {

    @Inject(
            method = "applyFog",
            at = @At("TAIL")
    )
    private static void zombekclient$onApplyFog(
            Camera camera,
            BackgroundRenderer.FogType fogType,
            float viewDistance,
            boolean thickFog,
            float tickDelta,
            CallbackInfo ci
    ) {
        FogVisualController controller = FogVisualController.getInstance();
        if (controller.isActive()) {
            float start = RenderSystem.getShaderFogStart();
            float end = RenderSystem.getShaderFogEnd();
            float[] color = RenderSystem.getShaderFogColor();

            String dim = "minecraft:overworld";
            if (camera != null && camera.getFocusedEntity() != null && camera.getFocusedEntity().getWorld() != null) {
                dim = camera.getFocusedEntity().getWorld().getRegistryKey().getValue().toString();
            }

            FogEnvironmentType env = FogEnvironmentType.AIR;
            if (camera != null) {
                CameraSubmersionType sub = camera.getSubmersionType();
                if (sub == CameraSubmersionType.WATER) {
                    env = FogEnvironmentType.WATER;
                } else if (sub == CameraSubmersionType.LAVA) {
                    env = FogEnvironmentType.LAVA;
                } else if (sub == CameraSubmersionType.POWDER_SNOW) {
                    env = FogEnvironmentType.POWDER_SNOW;
                }
            }

            FogParameters params = controller.computeFogParameters(
                    start, end,
                    color != null && color.length > 0 ? color[0] : 1.0f,
                    color != null && color.length > 1 ? color[1] : 1.0f,
                    color != null && color.length > 2 ? color[2] : 1.0f,
                    color != null && color.length > 3 ? color[3] : 1.0f,
                    dim, env
            );

            RenderSystem.setShaderFogStart(params.startDistance());
            RenderSystem.setShaderFogEnd(params.endDistance());
            RenderSystem.setShaderFogColor(params.red(), params.green(), params.blue(), params.alpha());
        }
    }
}
