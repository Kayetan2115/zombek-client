package pl.zombek.client.quilt.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import pl.zombek.client.core.modules.hud.teamview.TeamDetectionMode;
import pl.zombek.client.core.modules.hud.teamview.TeamTracker;
import pl.zombek.client.core.modules.hud.teamview.TeamViewModule;
import pl.zombek.client.core.modules.hud.teamview.TeammateData;
import pl.zombek.client.core.modules.pvp.tnt.TntCountdownCalculator;
import pl.zombek.client.core.modules.pvp.tnt.TntCountdownModule;
import pl.zombek.client.core.modules.pvp.tnt.TntTracker;

import java.util.UUID;

/**
 * World billboard rendering for TNT Countdown and Team View on Quilt Loader.
 */
public class QuiltWorldOverlayRenderer {

    public static void renderOverlays(MatrixStack matrices, Camera camera, MinecraftClient client) {
        if (client == null || client.world == null || camera == null || matrices == null) {
            return;
        }

        VertexConsumerProvider consumers = client.getBufferBuilders().getEntityVertexConsumers();

        renderTnt(matrices, camera, client, consumers);
        renderTeamView(matrices, camera, client, consumers);
    }

    private static void renderTnt(MatrixStack matrices, Camera camera, MinecraftClient client, VertexConsumerProvider consumers) {
        TntCountdownModule module = TntCountdownModule.getInstance();
        if (module == null || !module.isEnabled() || !module.getShowCountdownSetting().getValue()) {
            return;
        }

        if (module.getHideInGuiSetting().getValue() && client.currentScreen != null) {
            return;
        }

        Vec3d camPos = camera.getPos();
        double maxDist = module.getMaxDistanceSetting().getValue().doubleValue();
        float vertOffset = module.getVerticalOffsetSetting().getValue().floatValue();
        float scaleSetting = module.getScaleSetting().getValue().floatValue();
        int offsetTicks = module.getFuseOffsetTicksSetting().getValue().intValue();
        TntTracker tracker = TntTracker.getInstance();

        for (Entity entity : client.world.getEntities()) {
            if (entity instanceof TntEntity tnt) {
                double dist = camPos.distanceTo(tnt.getPos());
                if (dist > maxDist) continue;

                int fuse = tnt.getFuse();
                tracker.updateTnt(tnt.getId(), tnt.getX(), tnt.getY(), tnt.getZ(), fuse, dist, true);

                String text = TntCountdownCalculator.formatLabel(
                        fuse,
                        offsetTicks,
                        module.getShowPrefixSetting().getValue(),
                        module.getShowDecimalPlacesSetting().getValue(),
                        module.getDecimalPrecisionSetting().getValue().intValue(),
                        module.getDisplayFormatSetting().getValue()
                );

                int color = TntCountdownCalculator.resolveColor(
                        fuse,
                        module.getDynamicColorSetting().getValue(),
                        module.getStaticColorSetting().getValue()
                );

                renderBillboardText(
                        matrices,
                        camera,
                        consumers,
                        client,
                        text,
                        tnt.getX(),
                        tnt.getY() + tnt.getHeight() + vertOffset,
                        tnt.getZ(),
                        color,
                        scaleSetting,
                        module.getTextShadowSetting().getValue()
                );
            }
        }
    }

    private static void renderTeamView(MatrixStack matrices, Camera camera, MinecraftClient client, VertexConsumerProvider consumers) {
        TeamViewModule module = TeamViewModule.getInstance();
        if (module == null || !module.isEnabled()) {
            return;
        }

        if (module.getHideInGuiSetting().getValue() && client.currentScreen != null) {
            return;
        }

        if (module.getHideWhenDeadSetting().getValue() && client.player != null && !client.player.isAlive()) {
            return;
        }

        if (module.getHideWhenSpectatorSetting().getValue() && client.player != null && client.player.isSpectator()) {
            return;
        }

        Vec3d camPos = camera.getPos();
        double maxDist = module.getMaxDistanceSetting().getValue().doubleValue();
        TeamDetectionMode mode = module.getDetectionModeSetting().getValue();
        TeamTracker tracker = TeamTracker.getInstance();

        String myTeam = (client.player != null && client.player.getScoreboardTeam() != null)
                ? client.player.getScoreboardTeam().getName() : null;

        for (AbstractClientPlayerEntity player : client.world.getPlayers()) {
            if (player == client.player) continue;

            String theirTeam = player.getScoreboardTeam() != null ? player.getScoreboardTeam().getName() : null;
            UUID uuid = player.getUuid();
            String name = player.getName().getString();

            if (!tracker.isTeammate(uuid, name, theirTeam, myTeam, mode)) {
                continue;
            }

            double dist = camPos.distanceTo(player.getPos());
            if (dist > maxDist) continue;

            int teamColor = module.getShowTeamColorSetting().getValue()
                    ? (player.getScoreboardTeam() != null && player.getScoreboardTeam().getColor().getColorValue() != null
                    ? (0xFF000000 | player.getScoreboardTeam().getColor().getColorValue())
                    : module.getDefaultTeamColorSetting().getValue())
                    : module.getMarkerColorSetting().getValue();

            tracker.updateTeammate(new TeammateData(
                    uuid, name, theirTeam, teamColor, player.getX(), player.getY(), player.getZ(), dist, true
            ));

            StringBuilder sb = new StringBuilder();
            if (module.getShowMarkersSetting().getValue()) {
                sb.append(module.getMarkerStyleSetting().getValue().getSymbol()).append(" ");
            }
            if (module.getShowNamesSetting().getValue()) {
                sb.append(name).append(" ");
            }
            if (module.getShowDistanceSetting().getValue()) {
                sb.append(String.format("[%.0fm]", dist));
            }

            String text = sb.toString().trim();
            if (!text.isEmpty()) {
                renderBillboardText(
                        matrices,
                        camera,
                        consumers,
                        client,
                        text,
                        player.getX(),
                        player.getY() + player.getHeight() + 0.6,
                        player.getZ(),
                        teamColor,
                        module.getMarkerSizeSetting().getValue().floatValue(),
                        true
                );
            }
        }
    }

    private static void renderBillboardText(MatrixStack matrices, Camera camera, VertexConsumerProvider consumers,
                                           MinecraftClient client, String text, double x, double y, double z,
                                           int color, float scale, boolean shadow) {
        Vec3d camPos = camera.getPos();
        TextRenderer textRenderer = client.textRenderer;

        matrices.push();
        matrices.translate(x - camPos.x, y - camPos.y, z - camPos.z);
        matrices.multiply(camera.getRotation());
        matrices.scale(-0.025f * scale, -0.025f * scale, 0.025f * scale);

        Matrix4f model = matrices.peek().getPositionMatrix();
        float textWidth = textRenderer.getWidth(text);
        float textX = -textWidth / 2.0f;

        textRenderer.draw(
                text,
                textX,
                0,
                color,
                shadow,
                model,
                consumers,
                TextRenderer.TextLayerType.SEE_THROUGH,
                0x55000000,
                0xF000F0
        );

        matrices.pop();
    }
}
