package pl.zombek.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.zombek.client.core.modules.ModuleManager;
import pl.zombek.client.core.modules.visual.tabeditor.CustomTabRenderer;
import pl.zombek.client.core.modules.visual.tabeditor.TabEditorModule;
import pl.zombek.client.render.FabricHudRenderer;
import pl.zombek.client.integration.FabricPlayerInfoDataProvider;

/**
 * Mixin intercepting player list headers, footers, and rendering for TabEditorModule on Fabric.
 */
@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {

    @Inject(method = "setHeader", at = @At("HEAD"))
    private void zombekclient$onSetHeader(@Nullable Text header, CallbackInfo ci) {
        FabricPlayerInfoDataProvider.serverHeader = (header != null) ? header.getString() : "";
    }

    @Inject(method = "setFooter", at = @At("HEAD"))
    private void zombekclient$onSetFooter(@Nullable Text footer, CallbackInfo ci) {
        FabricPlayerInfoDataProvider.serverFooter = (footer != null) ? footer.getString() : "";
    }

    @Inject(method = "clear", at = @At("HEAD"))
    private void zombekclient$onClear(CallbackInfo ci) {
        FabricPlayerInfoDataProvider.serverHeader = "";
        FabricPlayerInfoDataProvider.serverFooter = "";
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void zombekclient$onRender(DrawContext context, int scaledWindowWidth, Scoreboard scoreboard, @Nullable ScoreboardObjective objective, CallbackInfo ci) {
        if (TabEditorModule.shouldOverrideVanilla()) {
            ci.cancel();
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null) {
                FabricHudRenderer renderer = FabricHudRenderer.getOrCreate(context, client);
                TabEditorModule mod = (TabEditorModule) ModuleManager.getInstance().getModuleById(TabEditorModule.ID);
                if (mod != null) {
                    CustomTabRenderer.renderTabList(
                            renderer,
                            mod,
                            null,
                            FabricPlayerInfoDataProvider.serverHeader,
                            FabricPlayerInfoDataProvider.serverFooter,
                            false
                    );
                }
            }
        }
    }
}
