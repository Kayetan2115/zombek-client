package pl.zombek.client.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.zombek.client.core.modules.visual.shinypots.ShinyPotsController;
import pl.zombek.client.core.rendereffect.glint.GlintLocation;
import pl.zombek.client.core.rendereffect.glint.GlintRenderController;

/**
 * Mixin into ItemStack to conditionally display enchantment glint for potions
 * and custom items without altering actual NBT, lore, or server attributes.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow
    public abstract Item getItem();

    @Inject(method = "hasGlint", at = @At("HEAD"), cancellable = true)
    private void zombekclient$onHasGlint(CallbackInfoReturnable<Boolean> cir) {
        Item item = this.getItem();
        if (item != null) {
            String regId = Registries.ITEM.getId(item).toString();
            if (ShinyPotsController.getInstance().shouldHaveGlint(regId)) {
                cir.setReturnValue(true);
                return;
            }
            if (GlintRenderController.getInstance().isActive()) {
                int glint = GlintRenderController.getInstance().resolveGlintColor(
                        regId, false, GlintLocation.INVENTORY, System.currentTimeMillis()
                );
                if (glint != 0) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}
