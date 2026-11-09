package pl.zombek.client.neoforge.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import pl.zombek.client.core.data.IHitboxRenderer;

/**
 * World-space hitbox rendering implementation on NeoForge using LevelRenderer buffer sources.
 */
public class NeoForgeHitboxRenderer implements IHitboxRenderer {
    private final PoseStack poseStack;
    private final MultiBufferSource bufferSource;

    public NeoForgeHitboxRenderer(PoseStack poseStack, MultiBufferSource bufferSource) {
        this.poseStack = poseStack;
        this.bufferSource = bufferSource;
    }

    @Override
    public void drawLine(double x1, double y1, double z1, double x2, double y2, double z2, int colorArgb, float lineWidth) {
        if (bufferSource == null || poseStack == null) return;
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.lines());
        PoseStack.Pose pose = poseStack.last();
        Matrix4f posMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        float a = ((colorArgb >> 24) & 0xFF) / 255.0f;
        float r = ((colorArgb >> 16) & 0xFF) / 255.0f;
        float g = ((colorArgb >> 8) & 0xFF) / 255.0f;
        float b = (colorArgb & 0xFF) / 255.0f;

        float nx = (float) (x2 - x1);
        float ny = (float) (y2 - y1);
        float nz = (float) (z2 - z1);
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len > 0.0f) {
            nx /= len;
            ny /= len;
            nz /= len;
        }

        buffer.vertex(posMatrix, (float) x1, (float) y1, (float) z1).color(r, g, b, a).normal(normalMatrix, nx, ny, nz).endVertex();
        buffer.vertex(posMatrix, (float) x2, (float) y2, (float) z2).color(r, g, b, a).normal(normalMatrix, nx, ny, nz).endVertex();
    }

    @Override
    public void drawQuad(double x1, double y1, double z1,
                         double x2, double y2, double z2,
                         double x3, double y3, double z3,
                         double x4, double y4, double z4,
                         int colorArgb) {
        if (bufferSource == null || poseStack == null) return;
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.debugQuads());
        Matrix4f posMatrix = poseStack.last().pose();

        float a = ((colorArgb >> 24) & 0xFF) / 255.0f;
        float r = ((colorArgb >> 16) & 0xFF) / 255.0f;
        float g = ((colorArgb >> 8) & 0xFF) / 255.0f;
        float b = (colorArgb & 0xFF) / 255.0f;

        buffer.vertex(posMatrix, (float) x1, (float) y1, (float) z1).color(r, g, b, a).endVertex();
        buffer.vertex(posMatrix, (float) x2, (float) y2, (float) z2).color(r, g, b, a).endVertex();
        buffer.vertex(posMatrix, (float) x3, (float) y3, (float) z3).color(r, g, b, a).endVertex();
        buffer.vertex(posMatrix, (float) x4, (float) y4, (float) z4).color(r, g, b, a).endVertex();
    }
}
