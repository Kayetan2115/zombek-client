package pl.zombek.client.quilt.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.zombek.client.core.modules.visual.oneseven.AnimationProfileManager;
import pl.zombek.client.core.modules.visual.oneseven.FirstPersonAnimationRenderer;
import pl.zombek.client.core.modules.visual.oneseven.OneSevenVisualsModule;
import pl.zombek.client.core.modules.visual.oneseven.TransformOffset;

/**
 * Quilt Mixin intercepting first-person item rendering to apply classic 1.7/1.8 item transformations,
 * sword blocking poses, blockhit animations, swing arcs, and item switching.
 */
@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

    @Inject(
            method = "renderFirstPersonItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", shift = At.Shift.AFTER)
    )
    private void zombekclient$applyOneSevenTransforms(
            AbstractClientPlayerEntity player,
            float tickDelta,
            float pitch,
            Hand hand,
            float swingProgress,
            ItemStack item,
            float equipProgress,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo ci
    ) {
        OneSevenVisualsModule module = OneSevenVisualsModule.getInstance();
        if (module == null || !module.isEnabled() || item == null || item.isEmpty()) {
            return;
        }

        String itemId = Registries.ITEM.getId(item.getItem()).toString();
        boolean isMainHand = (hand == Hand.MAIN_HAND);
        boolean isUsingItem = player.isUsingItem() && player.getActiveHand() == hand;
        int itemUseTicks = player.getItemUseTime();
        boolean isRightMouseDown = MinecraftClient.getInstance().options.useKey.isPressed();

        TransformOffset transform = FirstPersonAnimationRenderer.getInstance().calculateCombinedTransform(
                itemId,
                isMainHand,
                swingProgress,
                equipProgress,
                isUsingItem,
                itemUseTicks,
                isRightMouseDown,
                false,
                tickDelta,
                AnimationProfileManager.getInstance().getCurrentSettings()
        );

        if (transform != null && !transform.isIdentity()) {
            matrices.translate(transform.getPosX(), transform.getPosY(), transform.getPosZ());
            if (transform.getRotX() != 0.0f) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(transform.getRotX()));
            }
            if (transform.getRotY() != 0.0f) {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(transform.getRotY()));
            }
            if (transform.getRotZ() != 0.0f) {
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(transform.getRotZ()));
            }
            if (transform.getScaleX() != 1.0f || transform.getScaleY() != 1.0f || transform.getScaleZ() != 1.0f) {
                matrices.scale(transform.getScaleX(), transform.getScaleY(), transform.getScaleZ());
            }
        }
    }
}
