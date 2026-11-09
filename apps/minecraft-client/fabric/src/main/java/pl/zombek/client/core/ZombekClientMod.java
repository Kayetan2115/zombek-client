package pl.zombek.client.core;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.zombek.client.core.bootstrap.ZombekCore;
import pl.zombek.client.core.hud.HudElementRegistry;
import pl.zombek.client.integration.FabricInputHandler;
import pl.zombek.client.keybind.KeybindManager;
import pl.zombek.client.render.FabricHudRenderer;

/**
 * Main client entry point for Fabric mod loader.
 * Delegates lifecycle and core management to shared ZombekCore.
 */
public class ZombekClientMod implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientConstants.CLIENT_NAME);

    private static ZombekClientMod instance;
    private ZombekCore core;
    private KeybindManager keybindManager;
    private static long lastFrameNano = 0;

    public static ZombekClientMod getInstance() {
        return instance;
    }

    public ZombekCore getCore() {
        return core;
    }

    public KeybindManager getKeybindManager() {
        return keybindManager;
    }

    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("[{}] Bootstrapping Fabric client mod...", ClientConstants.MOD_ID);

        // 1. Initialize shared ZombekCore with FabricPlatformAdapter
        this.core = ZombekCore.init(new FabricPlatformAdapter());

        // 2. Register data providers for HUD modules
        pl.zombek.client.core.data.ArmorDataProviderRegistry.setProvider(new pl.zombek.client.integration.FabricArmorDataProvider());
        pl.zombek.client.core.data.CooldownDataProviderRegistry.setProvider(new pl.zombek.client.integration.FabricCooldownDataProvider());
        pl.zombek.client.core.data.CrosshairTargetRegistry.setProvider(new pl.zombek.client.integration.FabricCrosshairTargetProvider(MinecraftClient.getInstance()));
        pl.zombek.client.core.uhc.UhcDataProviderRegistry.setProvider(new pl.zombek.client.integration.FabricUhcDataProvider());
        pl.zombek.client.core.bedwars.BedwarsDataProviderRegistry.setProvider(new pl.zombek.client.integration.FabricBedwarsDataProvider());
        pl.zombek.client.core.hypixel.HypixelDataProviderRegistry.setProvider(new pl.zombek.client.integration.FabricHypixelDataProvider());
        pl.zombek.client.core.info.InfoHudDataProviderRegistry.getInstance().register(new pl.zombek.client.integration.FabricInfoHudDataProvider());
        pl.zombek.client.core.hud.scoreboard.ScoreboardDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.integration.FabricScoreboardDataProvider());
        pl.zombek.client.core.hud.potions.PotionEffectsDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.integration.FabricPotionEffectsDataProvider());
        pl.zombek.client.core.hud.saturation.SaturationDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.integration.FabricSaturationDataProvider());
        pl.zombek.client.core.hud.bossbar.BossBarDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.integration.FabricBossBarDataProvider());
        pl.zombek.client.core.time.TimeDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.integration.FabricTimeDataProvider());
        pl.zombek.client.core.inventory.InventoryDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.integration.FabricInventoryDataProvider());
        pl.zombek.client.core.serveraddress.ServerAddressDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.integration.FabricServerAddressDataProvider());
        pl.zombek.client.core.playerinfo.PlayerInfoDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.integration.FabricPlayerInfoDataProvider());
        pl.zombek.client.core.netgraph.NetworkDataProviderRegistry.getInstance().registerProvider(new pl.zombek.client.integration.FabricNetworkDataProvider());
        pl.zombek.client.core.worldvisual.lighting.DynamicLightManager.getInstance().setPlatformProvider(new pl.zombek.client.integration.FabricDynamicLightProvider());

        // 3. Register attack listener for Combo Counter & Hit Color
        net.fabricmc.fabric.api.event.player.AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && player == client.player && entity != null) {
                pl.zombek.client.core.tracking.ComboTracker.getInstance().recordHit(entity.getId(), true);
                pl.zombek.client.core.tracking.HitColorTracker.getInstance().recordHit(
                        entity.getId(),
                        pl.zombek.client.integration.FabricEntityClassifier.classify(entity),
                        true,
                        true
                );

                // Reach calculation to AABB
                net.minecraft.util.math.Vec3d eyePos = player.getEyePos();
                net.minecraft.util.math.Box box = entity.getBoundingBox();
                double dist = pl.zombek.client.core.tracking.ReachTracker.calculateDistanceToAABB(
                        eyePos.x, eyePos.y, eyePos.z,
                        box.minX, box.minY, box.minZ,
                        box.maxX, box.maxY, box.maxZ
                );
                pl.zombek.client.core.tracking.ReachTracker.getInstance().recordObservedHit(
                        dist, entity.getId(), entity.getName().getString()
                );

                // PvP Info Session tracking
                pl.zombek.client.core.tracking.PvPSession session = pl.zombek.client.core.tracking.PvPSessionManager.getInstance().getCurrentSession();
                session.recordAttackAttempt();
                session.recordObservedHit();
                session.recordCombo(pl.zombek.client.core.tracking.ComboTracker.getInstance().getCurrentCombo());
                if (entity instanceof net.minecraft.entity.LivingEntity living) {
                    session.updateTargetHealth(living.getHealth());
                }
            }
            return net.minecraft.util.ActionResult.PASS;
        });

        // 4. Initialize Fabric KeybindManager
        this.keybindManager = KeybindManager.getInstance();
        this.keybindManager.initialize();

        // 3. Hook client tick to update active modules via core & input handler
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client != null && client.world != null) {
                this.core.onClientTick();
                FabricInputHandler.getInstance().onClientTick(client);

                try {
                    if (client.player != null) {
                        pl.zombek.client.core.worldvisual.ZombekWorldVisualController.getInstance().onClientTick(
                                client.player.getX(), client.player.getEyeY(), client.player.getZ()
                        );
                    }
                } catch (Throwable ignored) {}

                // Update UHC Scoreboard lines
                try {
                    net.minecraft.scoreboard.Scoreboard sb = client.world.getScoreboard();
                    if (sb != null) {
                        net.minecraft.scoreboard.ScoreboardObjective obj = sb.getObjectiveForSlot(net.minecraft.scoreboard.ScoreboardDisplaySlot.SIDEBAR);
                        if (obj != null) {
                            java.util.List<String> lines = new java.util.ArrayList<>();
                            lines.add(obj.getDisplayName().getString());
                            for (net.minecraft.scoreboard.ScoreboardEntry entry : sb.getScoreboardEntries(obj)) {
                                lines.add(entry.name() != null ? entry.name().getString() : entry.owner());
                            }
                            pl.zombek.client.core.uhc.UhcSessionTracker.getInstance().getScoreboardParser().parseLines(lines);
                            pl.zombek.client.core.bedwars.BedwarsManager.getInstance().onScoreboardUpdate(lines);
                        }
                    }
                } catch (Throwable ignored) {}
            } else {
                FabricInputHandler.getInstance().onClientTick(client);
            }
        });

        // Register incoming chat message listeners for UHC and Bedwars
        try {
            net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
                if (message != null) {
                    String str = message.getString();
                    pl.zombek.client.core.uhc.UhcSessionTracker.getInstance().getChatParser().onChatMessage(str);
                    pl.zombek.client.core.bedwars.BedwarsManager.getInstance().onChatMessage(str);
                    pl.zombek.client.core.hypixel.HypixelManager.getInstance().processIncomingChat(str);
                }
            });
            net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
                if (message != null) {
                    String str = message.getString();
                    pl.zombek.client.core.uhc.UhcSessionTracker.getInstance().getChatParser().onChatMessage(str);
                    pl.zombek.client.core.bedwars.BedwarsManager.getInstance().onChatMessage(str);
                    pl.zombek.client.core.hypixel.HypixelManager.getInstance().processIncomingChat(str);
                }
            });
        } catch (Throwable ignored) {}

        // 4. Hook in-game HUD rendering
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.world != null && (client.options == null || !client.options.hudHidden)) {
                FabricHudRenderer renderer = FabricHudRenderer.getOrCreate(drawContext, client);
                HudElementRegistry.getInstance().renderAll(renderer);

                // Benchmark tracking
                long nowNano = System.nanoTime();
                if (lastFrameNano > 0) {
                    pl.zombek.client.core.benchmark.PerformanceBenchmarkManager.getInstance()
                            .recordFrame(nowNano - lastFrameNano, client.getCurrentFps());
                }
                lastFrameNano = nowNano;
            }
        });

        // 5. Hook world rendering for HitboxModule, TNT Countdown, and Team View
        net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            pl.zombek.client.integration.FabricWorldOverlayRenderer.renderOverlays(context);

            pl.zombek.client.core.modules.visual.HitboxModule hitboxModule = pl.zombek.client.core.modules.visual.HitboxModule.getInstance();
            if (hitboxModule == null || !hitboxModule.isEnabled()) {
                return;
            }

            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.world == null || context.camera() == null) {
                return;
            }

            net.minecraft.util.math.Vec3d camPos = context.camera().getPos();
            pl.zombek.client.integration.FabricHitboxRenderer hitboxRenderer =
                    new pl.zombek.client.integration.FabricHitboxRenderer(context.matrixStack(), context.consumers());

            int renderedCount = 0;
            int maxEntities = hitboxModule.getMaxRenderedEntitiesSetting().getValue().intValue();

            for (net.minecraft.entity.Entity entity : client.world.getEntities()) {
                if (renderedCount >= maxEntities) break;
                if (entity == client.player && client.options.getPerspective().isFirstPerson()) {
                    continue; // Do not render own hitbox in first person
                }

                net.minecraft.util.math.Box box = entity.getBoundingBox();
                double dist = camPos.distanceTo(entity.getPos());
                boolean isTargeted = client.targetedEntity == entity;
                net.minecraft.util.math.Vec3d look = entity.getRotationVec(context.tickDelta());

                pl.zombek.client.core.data.HitboxEntityData data = new pl.zombek.client.core.data.HitboxEntityData(
                        entity.getId(),
                        entity.getName().getString(),
                        pl.zombek.client.integration.FabricEntityClassifier.classify(entity),
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

                    pl.zombek.client.core.modules.visual.HitboxGeometryCalculator.renderEntityHitbox(
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
        });

        LOGGER.info("[{}] Fabric Client Mod initialized with shared ZombekCore.", ClientConstants.MOD_ID);
    }
}
