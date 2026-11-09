package pl.zombek.client.mixin;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.zombek.client.core.uhc.UhcItemScalingController;

/**
 * Mixin scaling dropped item entities in the world for UHC Overlay without changing
 * physical hitboxes, gravity, or server interactions.
 */
@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin {

    @Inject(method = "render(Lnet/minecraft/entity/ItemEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", shift = At.Shift.AFTER))
    private void zombekclient$scaleDroppedItem(ItemEntity entity, float yaw, float tickDelta,
                                               MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                               int light, CallbackInfo ci) {
        if (entity != null) {
            ItemStack stack = entity.getStack();
            if (stack != null && !stack.isEmpty()) {
                String regId = Registries.ITEM.getId(stack.getItem()).toString();
                String displayName = stack.hasCustomName() ? stack.getName().getString() : null;
                float scale = UhcItemScalingController.getInstance().resolveScale(regId, displayName);
                if (scale != 1.0f) {
                    matrices.scale(scale, scale, scale);
                }
            }
        }
    }
}
