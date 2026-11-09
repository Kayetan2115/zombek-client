package pl.zombek.client.forge.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import pl.zombek.client.core.hud.IHudRenderer;

/**
 * Forge implementation of IHudRenderer using GuiGraphics and Minecraft font renderer.
 */
public class ForgeHudRenderer implements IHudRenderer {

    private final GuiGraphics guiGraphics;
    private final Minecraft minecraft;

    public ForgeHudRenderer(GuiGraphics guiGraphics, Minecraft minecraft) {
        this.guiGraphics = guiGraphics;
        this.minecraft = minecraft;
    }

    @Override
    public void fill(int x, int y, int width, int height, int color) {
        guiGraphics.fill(x, y, x + width, y + height, color);
    }

    @Override
    public void drawBorder(int x, int y, int width, int height, int color, int thickness) {
        int t = Math.max(1, thickness);
        // Top, Bottom, Left, Right
        guiGraphics.fill(x, y, x + width, y + t, color);
        guiGraphics.fill(x, y + height - t, x + width, y + height, color);
        guiGraphics.fill(x, y + t, x + t, y + height - t, color);
        guiGraphics.fill(x + width - t, y + t, x + width, y + height - t, color);
    }

    @Override
    public void drawText(String text, int x, int y, int color, boolean shadow, float scale, boolean bold) {
        if (text == null || minecraft.font == null) return;

        if (Math.abs(scale - 1.0f) > 0.01f) {
            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            poseStack.translate(x, y, 0);
            poseStack.scale(scale, scale, 1.0f);
            guiGraphics.drawString(minecraft.font, text, 0, 0, color, shadow);
            poseStack.popPose();
        } else {
            guiGraphics.drawString(minecraft.font, text, x, y, color, shadow);
        }
    }

    @Override
    public int getTextWidth(String text) {
        if (text == null || minecraft.font == null) return 0;
        return minecraft.font.width(text);
    }

    @Override
    public int getFontHeight() {
        if (minecraft.font == null) return 9;
        return minecraft.font.lineHeight;
    }

    @Override
    public int getScreenWidth() {
        if (minecraft.getWindow() == null) return 854;
        return minecraft.getWindow().getGuiScaledWidth();
    }

    @Override
    public int getScreenHeight() {
        if (minecraft.getWindow() == null) return 480;
        return minecraft.getWindow().getGuiScaledHeight();
    }

    @Override
    public void renderItem(Object itemHandle, int x, int y, float scale) {
        if (itemHandle instanceof net.minecraft.world.item.ItemStack stack && !stack.isEmpty()) {
            if (Math.abs(scale - 1.0f) > 0.01f) {
                PoseStack poseStack = guiGraphics.pose();
                poseStack.pushPose();
                poseStack.translate(x, y, 0);
                poseStack.scale(scale, scale, 1.0f);
                guiGraphics.renderItem(stack, 0, 0);
                poseStack.popPose();
            } else {
                guiGraphics.renderItem(stack, x, y);
            }
        }
    }
}
