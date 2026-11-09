package pl.zombek.client.quilt.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import pl.zombek.client.core.hud.IHudRenderer;

/**
 * Quilt implementation of IHudRenderer using Yarn DrawContext and TextRenderer.
 * Supports instance caching to prevent allocation churn on the render thread.
 */
public class QuiltHudRenderer implements IHudRenderer {

    private static final ThreadLocal<QuiltHudRenderer> CACHED_RENDERER =
            ThreadLocal.withInitial(() -> new QuiltHudRenderer(null, null));

    public static QuiltHudRenderer getOrCreate(DrawContext context, MinecraftClient client) {
        QuiltHudRenderer renderer = CACHED_RENDERER.get();
        renderer.setContext(context, client);
        return renderer;
    }

    private DrawContext context;
    private MinecraftClient client;

    public QuiltHudRenderer(DrawContext context, MinecraftClient client) {
        this.context = context;
        this.client = client;
    }

    public void setContext(DrawContext context, MinecraftClient client) {
        this.context = context;
        this.client = client;
    }

    @Override
    public void fill(int x, int y, int width, int height, int color) {
        if (context == null) return;
        context.fill(x, y, x + width, y + height, color);
    }

    @Override
    public void drawBorder(int x, int y, int width, int height, int color, int thickness) {
        if (context == null) return;
        int t = Math.max(1, thickness);
        // Top, Bottom, Left, Right
        context.fill(x, y, x + width, y + t, color);
        context.fill(x, y + height - t, x + width, y + height, color);
        context.fill(x, y + t, x + t, y + height - t, color);
        context.fill(x + width - t, y + t, x + width, y + height - t, color);
    }

    @Override
    public void drawText(String text, int x, int y, int color, boolean shadow, float scale, boolean bold) {
        if (context == null || text == null || client == null || client.textRenderer == null) return;

        if (Math.abs(scale - 1.0f) > 0.01f) {
            MatrixStack matrices = context.getMatrices();
            matrices.push();
            matrices.translate(x, y, 0);
            matrices.scale(scale, scale, 1.0f);
            context.drawText(client.textRenderer, text, 0, 0, color, shadow);
            matrices.pop();
        } else {
            context.drawText(client.textRenderer, text, x, y, color, shadow);
        }
    }

    @Override
    public int getTextWidth(String text) {
        if (text == null || client == null || client.textRenderer == null) return 0;
        return client.textRenderer.getWidth(text);
    }

    @Override
    public int getFontHeight() {
        if (client == null || client.textRenderer == null) return 9;
        return client.textRenderer.fontHeight;
    }

    @Override
    public int getScreenWidth() {
        if (client == null || client.getWindow() == null) return 854;
        return client.getWindow().getScaledWidth();
    }

    @Override
    public int getScreenHeight() {
        if (client == null || client.getWindow() == null) return 480;
        return client.getWindow().getScaledHeight();
    }

    @Override
    public void renderItem(Object itemHandle, int x, int y, float scale) {
        if (context == null) return;
        if (itemHandle instanceof net.minecraft.item.ItemStack stack && !stack.isEmpty()) {
            if (Math.abs(scale - 1.0f) > 0.01f) {
                MatrixStack matrices = context.getMatrices();
                matrices.push();
                matrices.translate(x, y, 0);
                matrices.scale(scale, scale, 1.0f);
                context.drawItem(stack, 0, 0);
                matrices.pop();
            } else {
                context.drawItem(stack, x, y);
            }
        }
    }
}
