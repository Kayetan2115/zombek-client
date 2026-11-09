package pl.zombek.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.zombek.client.core.camera.CameraFovController;
import pl.zombek.client.core.camera.HurtCamController;
import pl.zombek.client.core.camera.ZombekCameraController;

/**
 * Mixin injecting into Minecraft GameRenderer to integrate FOV Changer,
 * smooth Zoom interpolation, Hurt Cam tilt modulation, and Motion Blur rotation tracking.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow
    @Final
    MinecraftClient client;

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void zombekclient$onGetFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
        boolean isSprinting = client.player != null && client.player.isSprinting();
        boolean isMoving = client.player != null && client.player.input != null &&
                (client.player.input.movementForward != 0 || client.player.input.movementSideways != 0);
        boolean isUsingItem = client.player != null && client.player.isUsingItem();
        double currentFov = cir.getReturnValue();
        double vanillaBaseFov = (client.options != null && client.options.getFov() != null) ? client.options.getFov().getValue() : 70.0;

        double computed = CameraFovController.getInstance().computeFov(currentFov, vanillaBaseFov, tickDelta, isSprinting, isMoving, isUsingItem);
        cir.setReturnValue(computed);
    }

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void zombekclient$onTiltViewWhenHurt(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        HurtCamController hurtCam = HurtCamController.getInstance();
        if (hurtCam.shouldCancelHurtCam()) {
            ci.cancel();
            return;
        }

        float tiltMultiplier = hurtCam.getHurtTiltMultiplier();
        if (Math.abs(tiltMultiplier - 1.0f) > 0.001f) {
            ci.cancel();
            if (tiltMultiplier > 0.001f && client.getCameraEntity() instanceof LivingEntity living) {
                float f = (float) living.hurtTime - tickDelta;
                if (living.isDead()) {
                    float g = (float) living.deathTime + tickDelta;
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(40.0f - 8000.0f / (g + 200.0f)));
                }
                if (f < 0.0f) return;
                f /= (float) living.maxHurtTime;
                f = MathHelper.sin(f * f * f * f * (float) Math.PI);
                float h = living.getDamageTiltYaw();
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-h));
                float tiltAngle = -f * 14.0f * tiltMultiplier;
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(tiltAngle));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(h));
            }
        }
    }

    @Inject(method = "renderWorld", at = @At("HEAD"))
    private void zombekclient$onRenderWorldHead(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
        if (client.cameraEntity != null) {
            ZombekCameraController.getInstance().onRenderTick(
                    client.cameraEntity.getYaw(tickDelta),
                    client.cameraEntity.getPitch(tickDelta),
                    tickDelta
            );
        }
    }
}
