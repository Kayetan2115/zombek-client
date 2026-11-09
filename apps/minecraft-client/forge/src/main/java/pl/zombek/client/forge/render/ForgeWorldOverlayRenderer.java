package pl.zombek.client.forge.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
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
 * World billboard rendering for TNT Countdown and Team View on Minecraft Forge.
 */
public class ForgeWorldOverlayRenderer {

    public static void renderOverlays(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client == null || client.level == null || event.getCamera() == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Camera camera = event.getCamera();
        MultiBufferSource bufferSource = client.renderBuffers().bufferSource();

        renderTnt(poseStack, camera, client, bufferSource);
        renderTeamView(poseStack, camera, client, bufferSource);
    }

    private static void renderTnt(PoseStack poseStack, Camera camera, Minecraft client, MultiBufferSource bufferSource) {
        TntCountdownModule module = TntCountdownModule.getInstance();
        if (module == null || !module.isEnabled() || !module.getShowCountdownSetting().getValue()) {
            return;
        }

        if (module.getHideInGuiSetting().getValue() && client.screen != null) {
            return;
        }

        Vec3 camPos = camera.getPosition();
        double maxDist = module.getMaxDistanceSetting().getValue().doubleValue();
        float vertOffset = module.getVerticalOffsetSetting().getValue().floatValue();
        float scaleSetting = module.getScaleSetting().getValue().floatValue();
        int offsetTicks = module.getFuseOffsetTicksSetting().getValue().intValue();
        TntTracker tracker = TntTracker.getInstance();

        for (Entity entity : client.level.entitiesForRendering()) {
            if (entity instanceof PrimedTnt tnt) {
                double dist = camPos.distanceTo(tnt.position());
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
                        poseStack,
                        camera,
                        bufferSource,
                        client,
                        text,
                        tnt.getX(),
                        tnt.getY() + tnt.getBbHeight() + vertOffset,
                        tnt.getZ(),
                        color,
                        scaleSetting,
                        module.getTextShadowSetting().getValue()
                );
            }
        }
    }

    private static void renderTeamView(PoseStack poseStack, Camera camera, Minecraft client, MultiBufferSource bufferSource) {
        TeamViewModule module = TeamViewModule.getInstance();
        if (module == null || !module.isEnabled()) {
            return;
        }

        if (module.getHideInGuiSetting().getValue() && client.screen != null) {
            return;
        }

        if (module.getHideWhenDeadSetting().getValue() && client.player != null && !client.player.isAlive()) {
            return;
        }

        if (module.getHideWhenSpectatorSetting().getValue() && client.player != null && client.player.isSpectator()) {
            return;
        }

        Vec3 camPos = camera.getPosition();
        double maxDist = module.getMaxDistanceSetting().getValue().doubleValue();
        TeamDetectionMode mode = module.getDetectionModeSetting().getValue();
        TeamTracker tracker = TeamTracker.getInstance();

        String myTeam = (client.player != null && client.player.getTeam() != null)
                ? client.player.getTeam().getName() : null;

        for (Player player : client.level.players()) {
            if (player == client.player) continue;

            String theirTeam = player.getTeam() != null ? player.getTeam().getName() : null;
            UUID uuid = player.getUUID();
            String name = player.getName().getString();

            if (!tracker.isTeammate(uuid, name, theirTeam, myTeam, mode)) {
                continue;
            }

            double dist = camPos.distanceTo(player.position());
            if (dist > maxDist) continue;

            int teamColor = module.getShowTeamColorSetting().getValue()
                    ? (player.getTeam() != null && player.getTeam().getColor().getColor() != null
                    ? (0xFF000000 | player.getTeam().getColor().getColor())
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
                        poseStack,
                        camera,
                        bufferSource,
                        client,
                        text,
                        player.getX(),
                        player.getY() + player.getBbHeight() + 0.6,
                        player.getZ(),
                        teamColor,
                        module.getMarkerSizeSetting().getValue().floatValue(),
                        true
                );
            }
        }
    }

    private static void renderBillboardText(PoseStack poseStack, Camera camera, MultiBufferSource bufferSource,
                                           Minecraft client, String text, double x, double y, double z,
                                           int color, float scale, boolean shadow) {
        Vec3 camPos = camera.getPosition();
        Font font = client.font;

        poseStack.pushPose();
        poseStack.translate(x - camPos.x, y - camPos.y, z - camPos.z);
        poseStack.mulPose(camera.rotation());
        poseStack.scale(-0.025f * scale, -0.025f * scale, 0.025f * scale);

        Matrix4f model = poseStack.last().pose();
        float textWidth = font.width(text);
        float textX = -textWidth / 2.0f;

        font.drawInBatch(
                text,
                textX,
                0,
                color,
                shadow,
                model,
                bufferSource,
                Font.DisplayMode.SEE_THROUGH,
                0x55000000,
                0xF000F0
        );

        poseStack.popPose();
    }
}
