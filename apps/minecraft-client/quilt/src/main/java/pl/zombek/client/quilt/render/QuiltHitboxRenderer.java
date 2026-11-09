package pl.zombek.client.quilt.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import pl.zombek.client.core.data.IHitboxRenderer;

/**
 * World-space hitbox rendering implementation on Quilt Loader.
 */
public class QuiltHitboxRenderer implements IHitboxRenderer {
    private final MatrixStack matrices;
    private final VertexConsumerProvider consumers;

    public QuiltHitboxRenderer(MatrixStack matrices, VertexConsumerProvider consumers) {
        this.matrices = matrices;
        this.consumers = consumers;
    }

    @Override
    public void drawLine(double x1, double y1, double z1, double x2, double y2, double z2, int colorArgb, float lineWidth) {
        if (consumers == null || matrices == null) return;
        VertexConsumer buffer = consumers.getBuffer(RenderLayer.getLines());
        MatrixStack.Entry entry = matrices.peek();
        Matrix4f posMatrix = entry.getPositionMatrix();
        Matrix3f normalMatrix = entry.getNormalMatrix();

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

        buffer.vertex(posMatrix, (float) x1, (float) y1, (float) z1).color(r, g, b, a).normal(normalMatrix, nx, ny, nz).next();
        buffer.vertex(posMatrix, (float) x2, (float) y2, (float) z2).color(r, g, b, a).normal(normalMatrix, nx, ny, nz).next();
    }

    @Override
    public void drawQuad(double x1, double y1, double z1,
                         double x2, double y2, double z2,
                         double x3, double y3, double z3,
                         double x4, double y4, double z4,
                         int colorArgb) {
        if (consumers == null || matrices == null) return;
        VertexConsumer buffer = consumers.getBuffer(RenderLayer.getDebugQuads());
        Matrix4f posMatrix = matrices.peek().getPositionMatrix();

        float a = ((colorArgb >> 24) & 0xFF) / 255.0f;
        float r = ((colorArgb >> 16) & 0xFF) / 255.0f;
        float g = ((colorArgb >> 8) & 0xFF) / 255.0f;
        float b = (colorArgb & 0xFF) / 255.0f;

        buffer.vertex(posMatrix, (float) x1, (float) y1, (float) z1).color(r, g, b, a).next();
        buffer.vertex(posMatrix, (float) x2, (float) y2, (float) z2).color(r, g, b, a).next();
        buffer.vertex(posMatrix, (float) x3, (float) y3, (float) z3).color(r, g, b, a).next();
        buffer.vertex(posMatrix, (float) x4, (float) y4, (float) z4).color(r, g, b, a).next();
    }
}
