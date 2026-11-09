package pl.zombek.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.zombek.client.core.chat.ChatMessageEntry;
import pl.zombek.client.core.chat.CustomChatRenderer;
import pl.zombek.client.core.chat.ZombekChatService;
import pl.zombek.client.core.modules.visual.chat.ChatModule;
import pl.zombek.client.render.FabricHudRenderer;

/**
 * Mixin intercepting incoming chat messages and rendering CustomChatRenderer when ChatModule is active.
 */
@Mixin(ChatHud.class)
public class ChatHudMixin {

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At("HEAD"), cancellable = true)
    private void onAddMessage(Text message, MessageSignatureData signatureData, MessageIndicator indicator, CallbackInfo ci) {
        if (message != null) {
            String clientName = MinecraftClient.getInstance().player != null ? MinecraftClient.getInstance().player.getName().getString() : null;
            ChatMessageEntry entry = ZombekChatService.getInstance().onIncomingMessage(
                    message.getString(),
                    null,
                    null,
                    indicator != null,
                    message,
                    clientName
            );
            if (entry != null && entry.isHidden()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(DrawContext context, int currentTick, int mouseX, int mouseY, CallbackInfo ci) {
        if (ChatModule.shouldOverrideVanilla()) {
            ci.cancel();
            MinecraftClient client = MinecraftClient.getInstance();
            boolean chatOpen = client.currentScreen instanceof net.minecraft.client.gui.screen.ChatScreen;
            CustomChatRenderer.getInstance().render(
                    FabricHudRenderer.getOrCreate(context, client),
                    mouseX, mouseY, chatOpen,
                    context.getScaledWindowWidth(), context.getScaledWindowHeight()
            );
        }
    }
}
