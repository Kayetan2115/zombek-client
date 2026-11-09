package pl.zombek.client.quilt.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.zombek.client.core.data.HitboxEntityData;
import pl.zombek.client.core.modules.visual.HitboxGeometryCalculator;
import pl.zombek.client.core.modules.visual.HitboxModule;
import pl.zombek.client.quilt.integration.QuiltEntityClassifier;
import pl.zombek.client.quilt.render.QuiltHitboxRenderer;

/**
 * Mixin injecting into WorldRenderer on Quilt Loader to render entity hitboxes in world space.
 */
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderWorld(MatrixStack matrices, float tickDelta, long limitTime,
                               boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
                               LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix,
                               CallbackInfo ci) {
        if (client != null && client.world != null && camera != null && matrices != null) {
            pl.zombek.client.quilt.render.QuiltWorldOverlayRenderer.renderOverlays(matrices, camera, client);
        }

        HitboxModule hitboxModule = HitboxModule.getInstance();
        if (hitboxModule == null || !hitboxModule.isEnabled() || client == null || client.world == null || camera == null) {
            return;
        }

        Vec3d camPos = camera.getPos();
        QuiltHitboxRenderer hitboxRenderer = new QuiltHitboxRenderer(matrices, client.getBufferBuilders().getEntityVertexConsumers());

        int renderedCount = 0;
        int maxEntities = hitboxModule.getMaxRenderedEntitiesSetting().getValue().intValue();

        for (net.minecraft.entity.Entity entity : client.world.getEntities()) {
            if (renderedCount >= maxEntities) break;
            if (entity == client.player && client.options.getPerspective().isFirstPerson()) {
                continue;
            }

            Box box = entity.getBoundingBox();
            double dist = camPos.distanceTo(entity.getPos());
            boolean isTargeted = client.targetedEntity == entity;
            Vec3d look = entity.getRotationVec(tickDelta);

            HitboxEntityData data = new HitboxEntityData(
                    entity.getId(),
                    entity.getName().getString(),
                    QuiltEntityClassifier.classify(entity),
                    box.minX, box.minY, box.minZ,
                    box.maxX, box.maxY, box.maxZ,
                    entity.getEyeY(),
                    look.x, look.y, look.z,
                    dist,
                    isTargeted,
                    entity.isInvisible(),
                    !entity.isAlive(),
                    entity.isSpectator()
            );

            if (hitboxModule.shouldRenderEntity(data)) {
                int outlineColor = hitboxModule.resolveOutlineColor(data);
                int fillColor = hitboxModule.resolveFillColor(data);
                float lineWidth = hitboxModule.getLineWidthSetting().getValue().floatValue();

                HitboxGeometryCalculator.renderEntityHitbox(
                        hitboxRenderer,
                        data,
                        camPos.x, camPos.y, camPos.z,
                        hitboxModule.getRenderStyleSetting().getValue(),
                        outlineColor,
                        fillColor,
                        lineWidth,
                        hitboxModule.getShowEyePositionSetting().getValue(),
                        hitboxModule.getShowLookDirectionSetting().getValue(),
                        hitboxModule.getShowNameSetting().getValue(),
                        hitboxModule.getShowDistanceSetting().getValue(),
                        hitboxModule.getShowEntityIdSetting().getValue()
                );
                renderedCount++;
            }
        }
    }
}
