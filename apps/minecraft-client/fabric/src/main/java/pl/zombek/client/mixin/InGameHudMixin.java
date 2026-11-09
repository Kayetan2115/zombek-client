package pl.zombek.client.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.zombek.client.core.modules.hud.titles.TitlesModule;
import pl.zombek.client.core.modules.visual.CrosshairModule;
import pl.zombek.client.core.titles.CustomTitleRenderer;
import pl.zombek.client.core.titles.ZombekTitleService;
import pl.zombek.client.render.FabricHudRenderer;

/**
 * Mixin safely cancelling vanilla Minecraft HUD overlays when ZombekClient modules are active
 * and routing Titles/Action Bar to ZombekClient CustomTitleRenderer.
 */
@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Shadow
    private int titleRemainTicks;

    @Shadow
    private int overlayRemaining;

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void onRenderCrosshair(DrawContext context, CallbackInfo ci) {
        if (CrosshairModule.shouldHideVanillaCrosshair()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void onRenderScoreboardSidebar(DrawContext context, net.minecraft.scoreboard.ScoreboardObjective objective, CallbackInfo ci) {
        if (pl.zombek.client.core.modules.hud.scoreboard.ScoreboardModule.shouldOverrideVanilla()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    private void onRenderStatusEffectOverlay(DrawContext context, CallbackInfo ci) {
        if (pl.zombek.client.core.modules.hud.potions.PotionEffectsModule.shouldHideVanilla()) {
            ci.cancel();
        }
    }

    @Inject(method = "setTitle", at = @At("HEAD"))
    private void onSetTitle(Text title, CallbackInfo ci) {
        ZombekTitleService.getInstance().onTitle(title != null ? title.getString() : null);
    }

    @Inject(method = "setSubtitle", at = @At("HEAD"))
    private void onSetSubtitle(Text subtitle, CallbackInfo ci) {
        ZombekTitleService.getInstance().onSubtitle(subtitle != null ? subtitle.getString() : null);
    }

    @Inject(method = "setOverlayMessage", at = @At("HEAD"))
    private void onSetOverlayMessage(Text message, boolean tinted, CallbackInfo ci) {
        ZombekTitleService.getInstance().onActionBar(message != null ? message.getString() : null);
    }

    @Inject(method = "setTitleTicks", at = @At("HEAD"))
    private void onSetTitleTicks(int fadeInTicks, int stayTicks, int fadeOutTicks, CallbackInfo ci) {
        ZombekTitleService.getInstance().onTitleTimes(fadeInTicks, stayTicks, fadeOutTicks);
    }

    @Inject(method = "clearTitle", at = @At("HEAD"))
    private void onClearTitle(CallbackInfo ci) {
        ZombekTitleService.getInstance().onClearTitles();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderHead(DrawContext context, float tickDelta, CallbackInfo ci) {
        if (TitlesModule.shouldOverrideVanilla()) {
            this.titleRemainTicks = 0;
        }
        if (TitlesModule.shouldOverrideActionBar()) {
            this.overlayRemaining = 0;
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderTail(DrawContext context, float tickDelta, CallbackInfo ci) {
        net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
        if (TitlesModule.shouldOverrideVanilla()) {
            CustomTitleRenderer.getInstance().renderTitles(
                    FabricHudRenderer.getOrCreate(context, client),
                    context.getScaledWindowWidth(),
                    context.getScaledWindowHeight()
            );
        }
        if (TitlesModule.shouldOverrideActionBar()) {
            CustomTitleRenderer.getInstance().renderActionBar(
                    FabricHudRenderer.getOrCreate(context, client),
                    context.getScaledWindowWidth(),
                    context.getScaledWindowHeight()
            );
        }

        pl.zombek.client.core.modules.visual.ZoomModule zoomModule = pl.zombek.client.core.modules.visual.ZoomModule.getInstance();
        if (zoomModule != null && zoomModule.isEnabled() && zoomModule.getShowZoomIndicatorSetting().getValue()) {
            pl.zombek.client.core.camera.ZoomController zoomCtrl = pl.zombek.client.core.camera.ZoomController.getInstance();
            if (zoomCtrl.isActive() || zoomCtrl.getCurrentZoomFactor() > 1.01) {
                int screenW = context.getScaledWindowWidth();
                int screenH = context.getScaledWindowHeight();
                String text = zoomModule.getShowZoomLevelSetting().getValue()
                        ? String.format("ZOOM: %.1fx", zoomCtrl.getCurrentZoomFactor())
                        : "ZOOM";
                int textW = client.textRenderer.getWidth(text);
                int x = (screenW - textW) / 2;
                int y = screenH - 50;
                int opacity = (int) (zoomModule.getBackgroundOpacitySetting().getValue() * 255.0);
                int bg = (opacity << 24) | 0x000000;
                context.fill(x - 5, y - 3, x + textW + 5, y + 11, bg);
                context.drawTextWithShadow(client.textRenderer, text, x, y, zoomModule.getIndicatorColorSetting().getValue());
            }
        }
    }
}
