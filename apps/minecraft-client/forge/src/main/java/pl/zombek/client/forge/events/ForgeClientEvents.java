package pl.zombek.client.forge.events;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pl.zombek.client.core.bootstrap.ZombekCore;
import pl.zombek.client.core.hud.HudElementRegistry;
import pl.zombek.client.forge.input.ForgeInputHandler;
import pl.zombek.client.forge.input.ForgeKeybindManager;
import pl.zombek.client.forge.render.ForgeHudRenderer;

/**
 * Event bus listeners connecting Forge events to ZombekClient Core.
 */
public class ForgeClientEvents {

    /**
     * Mod Event Bus handler for registering key mappings.
     */
    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        ForgeKeybindManager.getInstance().onRegisterKeyMappings(event);
    }

    /**
     * Mod Event Bus handler for registering HUD overlays.
     */
    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("zombekclient_hud", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.options.hideGui) {
                return;
            }
            ForgeHudRenderer renderer = new ForgeHudRenderer(guiGraphics, mc);
            HudElementRegistry.getInstance().renderAll(renderer);

            // Render custom tab list overlay if Tab is held and TabEditorModule overrides vanilla
            if (pl.zombek.client.core.modules.visual.tabeditor.TabEditorModule.shouldOverrideVanilla()) {
                if (mc.options.keyPlayerList.isDown()) {
                    pl.zombek.client.core.modules.visual.tabeditor.TabEditorModule mod =
                            (pl.zombek.client.core.modules.visual.tabeditor.TabEditorModule) pl.zombek.client.core.modules.ModuleManager.getInstance().getModuleById(pl.zombek.client.core.modules.visual.tabeditor.TabEditorModule.ID);
                    if (mod != null) {
                        pl.zombek.client.core.modules.visual.tabeditor.CustomTabRenderer.renderTabList(
                                renderer,
                                mod,
                                null,
                                pl.zombek.client.forge.integration.ForgePlayerInfoDataProvider.serverHeader,
                                pl.zombek.client.forge.integration.ForgePlayerInfoDataProvider.serverFooter,
                                false
                        );
                    }
                }
            }

            // Render Custom Chat if ChatModule overrides vanilla
            if (pl.zombek.client.core.modules.visual.chat.ChatModule.shouldOverrideVanilla()) {
                boolean chatOpen = mc.screen instanceof net.minecraft.client.gui.screens.ChatScreen;
                int mouseX = (int) (mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / Math.max(1, mc.getWindow().getWidth()));
                int mouseY = (int) (mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / Math.max(1, mc.getWindow().getHeight()));
                pl.zombek.client.core.chat.CustomChatRenderer.getInstance().render(
                        renderer, mouseX, mouseY, chatOpen, screenWidth, screenHeight
                );
            }

            // Render Titles & Action Bar if TitlesModule overrides vanilla
            if (pl.zombek.client.core.modules.hud.titles.TitlesModule.shouldOverrideVanilla()) {
                pl.zombek.client.core.titles.CustomTitleRenderer.getInstance().renderTitles(renderer, screenWidth, screenHeight);
            }
            if (pl.zombek.client.core.modules.hud.titles.TitlesModule.shouldOverrideActionBar()) {
                pl.zombek.client.core.titles.CustomTitleRenderer.getInstance().renderActionBar(renderer, screenWidth, screenHeight);
            }

            // Render Zoom indicator if active
            pl.zombek.client.core.modules.visual.ZoomModule zoomModule = pl.zombek.client.core.modules.visual.ZoomModule.getInstance();
            if (zoomModule != null && zoomModule.isEnabled() && zoomModule.getShowZoomIndicatorSetting().getValue()) {
                pl.zombek.client.core.camera.ZoomController zoomCtrl = pl.zombek.client.core.camera.ZoomController.getInstance();
                if (zoomCtrl.isActive() || zoomCtrl.getCurrentZoomFactor() > 1.01) {
                    String text = zoomModule.getShowZoomLevelSetting().getValue()
                            ? String.format("ZOOM: %.1fx", zoomCtrl.getCurrentZoomFactor())
                            : "ZOOM";
                    int textW = mc.font.width(text);
                    int x = (screenWidth - textW) / 2;
                    int y = screenHeight - 50;
                    int opacity = (int) (zoomModule.getBackgroundOpacitySetting().getValue() * 255.0);
                    int bg = (opacity << 24) | 0x000000;
                    guiGraphics.fill(x - 5, y - 3, x + textW + 5, y + 11, bg);
                    guiGraphics.drawString(mc.font, text, x, y, zoomModule.getIndicatorColorSetting().getValue(), true);
                }
            }
        });
    }

    /**
     * Forge Event Bus handler for player attack events.
     */
    @SubscribeEvent
    public static void onAttackEntity(net.minecraftforge.event.entity.player.AttackEntityEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && event.getEntity() == mc.player && event.getTarget() != null) {
            pl.zombek.client.core.tracking.ComboTracker.getInstance().recordHit(event.getTarget().getId(), true);
            pl.zombek.client.core.tracking.HitColorTracker.getInstance().recordHit(
                    event.getTarget().getId(),
                    pl.zombek.client.forge.integration.ForgeEntityClassifier.classify(event.getTarget()),
                    true,
                    true
            );

            if (mc.player != null) {
                net.minecraft.world.phys.Vec3 eyePos = mc.player.getEyePosition();
                net.minecraft.world.phys.AABB box = event.getTarget().getBoundingBox();
                double dist = pl.zombek.client.core.tracking.ReachTracker.calculateDistanceToAABB(
                        eyePos.x, eyePos.y, eyePos.z,
                        box.minX, box.minY, box.minZ,
                        box.maxX, box.maxY, box.maxZ
                );
                pl.zombek.client.core.tracking.ReachTracker.getInstance().recordObservedHit(
                        dist, event.getTarget().getId(), event.getTarget().getName().getString()
                );
                pl.zombek.client.core.tracking.PvPSession session = pl.zombek.client.core.tracking.PvPSessionManager.getInstance().getCurrentSession();
                session.recordAttackAttempt();
                session.recordObservedHit();
                session.recordCombo(pl.zombek.client.core.tracking.ComboTracker.getInstance().getCurrentCombo());
                if (event.getTarget() instanceof net.minecraft.world.entity.LivingEntity living) {
                    session.updateTargetHealth(living.getHealth());
                }
            }
        }
    }

    /**
     * Forge Event Bus handler for decoupled camera rotation (Freelook).
     */
    @SubscribeEvent
    public static void onComputeCameraAngles(net.minecraftforge.client.event.ViewportEvent.ComputeCameraAngles event) {
        pl.zombek.client.core.camera.ZombekCameraController.getInstance().onRenderTick(
                event.getYaw(), event.getPitch(), (float) event.getPartialTick()
        );

        pl.zombek.client.core.modules.visual.FreelookCameraController controller = pl.zombek.client.core.modules.visual.FreelookCameraController.getInstance();
        if (controller.isControllingCamera()) {
            event.setYaw(controller.getCameraYaw());
            event.setPitch(controller.getCameraPitch());
        }

        pl.zombek.client.core.camera.HurtCamController hurtCam = pl.zombek.client.core.camera.HurtCamController.getInstance();
        if (hurtCam.shouldCancelHurtCam()) {
            event.setRoll(0.0f);
        } else {
            float multiplier = hurtCam.getHurtTiltMultiplier();
            if (Math.abs(multiplier - 1.0f) > 0.001f) {
                event.setRoll(event.getRoll() * multiplier);
            }
        }
    }

    /**
     * Forge Event Bus handler for 1.7 Visuals first-person hand & item transformations.
     */
    @SubscribeEvent
    public static void onRenderHand(net.minecraftforge.client.event.RenderHandEvent event) {
        pl.zombek.client.core.modules.visual.oneseven.OneSevenVisualsModule module =
                pl.zombek.client.core.modules.visual.oneseven.OneSevenVisualsModule.getInstance();
        if (module == null || !module.isEnabled()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return;
        }

        net.minecraft.world.item.ItemStack stack = event.getItemStack();
        if (stack == null || stack.isEmpty()) {
            return;
        }

        String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        boolean isMainHand = (event.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND);
        boolean isUsingItem = mc.player.isUsingItem() && mc.player.getUsedItemHand() == event.getHand();
        int itemUseTicks = mc.player.getTicksUsingItem();
        boolean isRightMouseDown = mc.options.keyUse.isDown();

        pl.zombek.client.core.modules.visual.oneseven.TransformOffset transform =
                pl.zombek.client.core.modules.visual.oneseven.FirstPersonAnimationRenderer.getInstance().calculateCombinedTransform(
                        itemId,
                        isMainHand,
                        event.getSwingProgress(),
                        event.getEquipProgress(),
                        isUsingItem,
                        itemUseTicks,
                        isRightMouseDown,
                        false,
                        event.getPartialTick(),
                        pl.zombek.client.core.modules.visual.oneseven.AnimationProfileManager.getInstance().getCurrentSettings()
                );

        if (transform != null && !transform.isIdentity()) {
            com.mojang.blaze3d.vertex.PoseStack poseStack = event.getPoseStack();
            poseStack.translate(transform.getPosX(), transform.getPosY(), transform.getPosZ());
            if (transform.getRotX() != 0.0f) {
                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(transform.getRotX()));
            }
            if (transform.getRotY() != 0.0f) {
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(transform.getRotY()));
            }
            if (transform.getRotZ() != 0.0f) {
                poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(transform.getRotZ()));
            }
            if (transform.getScaleX() != 1.0f || transform.getScaleY() != 1.0f || transform.getScaleZ() != 1.0f) {
                poseStack.scale(transform.getScaleX(), transform.getScaleY(), transform.getScaleZ());
            }
        }
    }

    /**
     * Forge Event Bus handler for cancelling vanilla crosshair when CrosshairModule is active.
     */
    @SubscribeEvent
    public static void onRenderGuiOverlayPre(net.minecraftforge.client.event.RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay() != null) {
            String overlayId = event.getOverlay().id().toString();
            if (net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.CROSSHAIR.id().equals(event.getOverlay().id())) {
                if (pl.zombek.client.core.modules.visual.CrosshairModule.shouldHideVanillaCrosshair()) {
                    event.setCanceled(true);
                }
            } else if (net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.SCOREBOARD.id().equals(event.getOverlay().id())) {
                if (pl.zombek.client.core.modules.hud.scoreboard.ScoreboardModule.shouldOverrideVanilla()) {
                    event.setCanceled(true);
                }
            } else if (net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.BOSS_EVENT_PROGRESS.id().equals(event.getOverlay().id())) {
                if (pl.zombek.client.core.modules.hud.bossbar.BossBarModule.shouldOverrideVanilla()) {
                    event.setCanceled(true);
                }
            } else if (net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.POTION_ICONS.id().equals(event.getOverlay().id())) {
                if (pl.zombek.client.core.modules.hud.potions.PotionEffectsModule.shouldHideVanilla()) {
                    event.setCanceled(true);
                }
            } else if (net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.PLAYER_LIST.id().equals(event.getOverlay().id())) {
                if (pl.zombek.client.core.modules.visual.tabeditor.TabEditorModule.shouldOverrideVanilla()) {
                    event.setCanceled(true);
                }
            } else if (net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.CHAT_PANEL.id().equals(event.getOverlay().id())) {
                if (pl.zombek.client.core.modules.visual.chat.ChatModule.shouldOverrideVanilla()) {
                    event.setCanceled(true);
                }
            } else if (net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.TITLE_TEXT.id().equals(event.getOverlay().id())) {
                if (pl.zombek.client.core.modules.hud.titles.TitlesModule.shouldOverrideVanilla()) {
                    event.setCanceled(true);
                }
            } else if (net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.RECORD_OVERLAY.id().equals(event.getOverlay().id())) {
                if (pl.zombek.client.core.modules.hud.titles.TitlesModule.shouldOverrideActionBar()) {
                    event.setCanceled(true);
                }
            }
        }
    }

    /**
     * Forge Event Bus handler for custom NameTags rendering and Fair Play visibility rules.
     */
    @SubscribeEvent
    public static void onRenderNameTag(net.minecraftforge.client.event.RenderNameTagEvent event) {
        if (!pl.zombek.client.core.modules.visual.nametags.NameTagsModule.shouldOverrideVanilla()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return;
        }

        pl.zombek.client.core.modules.visual.nametags.NameTagsModule module =
                (pl.zombek.client.core.modules.visual.nametags.NameTagsModule) pl.zombek.client.core.modules.ModuleManager.getInstance().getModuleById(pl.zombek.client.core.modules.visual.nametags.NameTagsModule.ID);
        if (module == null) {
            return;
        }

        net.minecraft.world.entity.Entity entity = event.getEntity();
        if (entity == null) {
            return;
        }

        pl.zombek.client.core.modules.visual.nametags.NameTagEntityCategory category =
                pl.zombek.client.forge.integration.ForgeEntityClassifier.classifyToNameTagCategory(entity);
        if (category == null) {
            return;
        }

        pl.zombek.client.core.playerinfo.PlayerInfoData data =
                pl.zombek.client.core.playerinfo.ZombekPlayerInfoService.getInstance().getEntityInfo(entity);
        if (data == null) {
            return;
        }

        boolean isSneaking = entity.isShiftKeyDown();
        boolean isInvisible = entity.isInvisible();
        boolean isSpectator = entity.isSpectator();
        boolean isGuiOpen = mc.screen != null;
        double distance = mc.player.distanceTo(entity);

        boolean canRender = pl.zombek.client.core.modules.visual.nametags.NameTagVisibilityRuleEvaluator.shouldRender(
                data,
                category,
                isSneaking,
                isInvisible,
                isSpectator,
                isGuiOpen,
                distance,
                module.getTargetPlayersSetting().getValue(),
                module.getTargetLivingSetting().getValue(),
                module.getTargetOtherSetting().getValue(),
                module.getRespectVanillaVisibilitySetting().getValue(),
                module.getMaxDisplayDistanceSetting().getValue().doubleValue(),
                module.getMinDisplayDistanceSetting().getValue().doubleValue(),
                module.getHideInGuiSetting().getValue(),
                module.getHideInSpectatorSetting().getValue(),
                module.getHideInvisibleSetting().getValue(),
                module.getHideSneakingSetting().getValue()
        );

        if (!canRender) {
            event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);
            return;
        }

        pl.zombek.client.core.modules.visual.nametags.CustomNameTagRenderer.NameTagFormattedContent formatted =
                pl.zombek.client.core.modules.visual.nametags.CustomNameTagRenderer.formatNameTag(data, module);

        if (formatted != null && !formatted.mainLine.isBlank()) {
            event.setContent(net.minecraft.network.chat.Component.literal(formatted.mainLine));
        }
    }

    /**
     * Forge Event Bus handler for world-space hitbox rendering.
     */
    @SubscribeEvent
    public static void onRenderLevelStage(net.minecraftforge.client.event.RenderLevelStageEvent event) {
        if (event.getStage() != net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }

        pl.zombek.client.forge.render.ForgeWorldOverlayRenderer.renderOverlays(event);

        pl.zombek.client.core.modules.visual.HitboxModule hitboxModule = pl.zombek.client.core.modules.visual.HitboxModule.getInstance();
        if (hitboxModule == null || !hitboxModule.isEnabled()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || event.getCamera() == null) {
            return;
        }

        net.minecraft.world.phys.Vec3 camPos = event.getCamera().getPosition();
        pl.zombek.client.forge.render.ForgeHitboxRenderer hitboxRenderer =
                new pl.zombek.client.forge.render.ForgeHitboxRenderer(event.getPoseStack(), mc.renderBuffers().bufferSource());

        int renderedCount = 0;
        int maxEntities = hitboxModule.getMaxRenderedEntitiesSetting().getValue().intValue();

        for (net.minecraft.world.entity.Entity entity : mc.level.entitiesForRendering()) {
            if (renderedCount >= maxEntities) break;
            if (entity == mc.player && mc.options.getCameraType().isFirstPerson()) {
                continue;
            }

            net.minecraft.world.phys.AABB box = entity.getBoundingBox();
            double dist = camPos.distanceTo(entity.position());
            boolean isTargeted = mc.crosshairPickEntity == entity;
            net.minecraft.world.phys.Vec3 look = entity.getViewVector(event.getPartialTick());

            pl.zombek.client.core.data.HitboxEntityData data = new pl.zombek.client.core.data.HitboxEntityData(
                    entity.getId(),
                    entity.getName().getString(),
                    pl.zombek.client.forge.integration.ForgeEntityClassifier.classify(entity),
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
    }

    /**
     * Forge Event Bus handler for movement input updates (Toggle Sneak).
     */
    @SubscribeEvent
    public static void onMovementInputUpdate(net.minecraftforge.client.event.MovementInputUpdateEvent event) {
        pl.zombek.client.core.modules.pvp.ToggleSneakModule sneakMod = pl.zombek.client.core.modules.pvp.ToggleSneakModule.getInstance();
        if (sneakMod != null && sneakMod.isEnabled()) {
            boolean sneakActive = pl.zombek.client.core.movement.MovementToggleController.getInstance().isSneakActive(
                    true,
                    sneakMod.getActivationModeSetting().getValue(),
                    sneakMod.getPreserveVanillaSneakSetting().getValue(),
                    sneakMod.getDisableWhileFlyingSetting().getValue(),
                    sneakMod.getDisableWhileRidingSetting().getValue()
            );
            if (sneakActive && event.getInput() != null) {
                event.getInput().shiftKeyDown = true;
            }
        }
    }

    /**
     * Forge Event Bus handler for incoming chat messages (UHC Event Parsing, Hypixel, and ZombekChatService).
     */
    @SubscribeEvent
    public static void onClientChatReceived(net.minecraftforge.client.event.ClientChatReceivedEvent event) {
        if (event.getMessage() != null) {
            String str = event.getMessage().getString();
            pl.zombek.client.core.uhc.UhcSessionTracker.getInstance().getChatParser().onChatMessage(str);
            pl.zombek.client.core.bedwars.BedwarsManager.getInstance().onChatMessage(str);
            String processed = pl.zombek.client.core.hypixel.HypixelManager.getInstance().processIncomingChat(str);
            if (processed == null) {
                event.setCanceled(true);
            }

            Minecraft mc = Minecraft.getInstance();
            String clientName = (mc != null && mc.player != null) ? mc.player.getName().getString() : null;
            String senderUuid = event.getSender() != null ? event.getSender().toString() : null;
            pl.zombek.client.core.chat.ChatMessageEntry entry = pl.zombek.client.core.chat.ZombekChatService.getInstance().onIncomingMessage(
                    str,
                    senderUuid,
                    null,
                    !event.isSystem(),
                    event.getMessage(),
                    clientName
            );
            if (entry != null && entry.isHidden()) {
                event.setCanceled(true);
            }
        }
    }

    /**
     * Forge Event Bus handler for client ticks.
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            ForgeInputHandler.getInstance().onClientTick(mc);

            if (mc.level != null) {
                ZombekCore core = ZombekCore.getInstance();
                if (core != null) {
                    core.onClientTick();
                }

                if (mc.player != null) {
                    try {
                        pl.zombek.client.core.worldvisual.ZombekWorldVisualController.getInstance().onClientTick(
                                mc.player.getX(), mc.player.getEyeY(), mc.player.getZ()
                        );
                    } catch (Throwable ignored) {}
                }

                ForgeKeybindManager.getInstance().handleInputTick(mc);

                // Synchronize Title and Action Bar
                if (mc.gui != null) {
                    pl.zombek.client.forge.integration.ForgeTitleSynchronizer.sync(mc.gui);
                }

                // Update UHC Scoreboard lines
                try {
                    net.minecraft.world.scores.Scoreboard sb = mc.level.getScoreboard();
                    if (sb != null) {
                        net.minecraft.world.scores.Objective obj = sb.getDisplayObjective(net.minecraft.world.scores.DisplaySlot.SIDEBAR);
                        if (obj != null) {
                            java.util.List<String> lines = new java.util.ArrayList<>();
                            lines.add(obj.getDisplayName().getString());
                            for (net.minecraft.world.scores.PlayerScoreEntry entry : sb.listPlayerScores(obj)) {
                                lines.add(entry.ownerName().getString());
                            }
                            pl.zombek.client.core.uhc.UhcSessionTracker.getInstance().getScoreboardParser().parseLines(lines);
                            pl.zombek.client.core.bedwars.BedwarsManager.getInstance().onScoreboardUpdate(lines);
                        }
                    }
                } catch (Throwable ignored) {}
            }
        }
    }

    /**
     * Handles FOV calculation across FOV Changer, Dynamic Multipliers, and Zoom.
     */
    @SubscribeEvent
    public static void onComputeFov(net.minecraftforge.client.event.ViewportEvent.ComputeFov event) {
        Minecraft mc = Minecraft.getInstance();
        boolean isSprinting = mc.player != null && mc.player.isSprinting();
        boolean isMoving = mc.player != null && mc.player.input != null &&
                (mc.player.input.forwardImpulse != 0 || mc.player.input.leftImpulse != 0);
        boolean isUsingItem = mc.player != null && mc.player.isUsingItem();
        double vanillaBaseFov = (mc.options != null && mc.options.fov() != null) ? mc.options.fov().get() : 70.0;

        double computed = pl.zombek.client.core.camera.CameraFovController.getInstance().computeFov(
                event.getFOV(), vanillaBaseFov, (float) event.getPartialTick(), isSprinting, isMoving, isUsingItem
        );
        event.setFOV(computed);
    }

    /**
     * Handles mouse wheel scrolling for Zoom without switching inventory hotbar slot.
     */
    @SubscribeEvent
    public static void onMouseScroll(net.minecraftforge.client.event.InputEvent.MouseScrollingEvent event) {
        pl.zombek.client.core.camera.ZoomController zoom = pl.zombek.client.core.camera.ZoomController.getInstance();
        Minecraft mc = Minecraft.getInstance();
        if (zoom.isActive() && mc != null && mc.screen == null) {
            zoom.onMouseScroll(event.getDeltaY());
            event.setCanceled(true);
        }
    }

    /**
     * Handles Fog Customizer distance calculations for Forge.
     */
    @SubscribeEvent
    public static void onRenderFog(net.minecraftforge.client.event.ViewportEvent.RenderFog event) {
        pl.zombek.client.core.rendereffect.fog.FogVisualController controller =
                pl.zombek.client.core.rendereffect.fog.FogVisualController.getInstance();
        if (controller.isActive()) {
            Minecraft mc = Minecraft.getInstance();
            String dim = (mc != null && mc.level != null) ? mc.level.dimension().location().toString() : "minecraft:overworld";
            pl.zombek.client.core.rendereffect.fog.FogEnvironmentType env = pl.zombek.client.core.rendereffect.fog.FogEnvironmentType.AIR;
            if (event.getCamera() != null) {
                net.minecraft.world.level.material.FogType type = event.getCamera().getFluidInCamera();
                if (type == net.minecraft.world.level.material.FogType.WATER) env = pl.zombek.client.core.rendereffect.fog.FogEnvironmentType.WATER;
                else if (type == net.minecraft.world.level.material.FogType.LAVA) env = pl.zombek.client.core.rendereffect.fog.FogEnvironmentType.LAVA;
                else if (type == net.minecraft.world.level.material.FogType.POWDER_SNOW) env = pl.zombek.client.core.rendereffect.fog.FogEnvironmentType.POWDER_SNOW;
            }

            pl.zombek.client.core.rendereffect.fog.FogParameters params = controller.computeFogParameters(
                    event.getNearPlaneDistance(), event.getFarPlaneDistance(),
                    1.0f, 1.0f, 1.0f, 1.0f,
                    dim, env
            );

            event.setNearPlaneDistance(params.startDistance());
            event.setFarPlaneDistance(params.endDistance());
            event.setCanceled(true);
        }
    }

    /**
     * Handles Fog Customizer color calculations for Forge.
     */
    @SubscribeEvent
    public static void onComputeFogColor(net.minecraftforge.client.event.ViewportEvent.ComputeFogColor event) {
        pl.zombek.client.core.rendereffect.fog.FogVisualController controller =
                pl.zombek.client.core.rendereffect.fog.FogVisualController.getInstance();
        if (controller.isActive()) {
            Minecraft mc = Minecraft.getInstance();
            String dim = (mc != null && mc.level != null) ? mc.level.dimension().location().toString() : "minecraft:overworld";
            pl.zombek.client.core.rendereffect.fog.FogEnvironmentType env = pl.zombek.client.core.rendereffect.fog.FogEnvironmentType.AIR;
            if (event.getCamera() != null) {
                net.minecraft.world.level.material.FogType type = event.getCamera().getFluidInCamera();
                if (type == net.minecraft.world.level.material.FogType.WATER) env = pl.zombek.client.core.rendereffect.fog.FogEnvironmentType.WATER;
                else if (type == net.minecraft.world.level.material.FogType.LAVA) env = pl.zombek.client.core.rendereffect.fog.FogEnvironmentType.LAVA;
                else if (type == net.minecraft.world.level.material.FogType.POWDER_SNOW) env = pl.zombek.client.core.rendereffect.fog.FogEnvironmentType.POWDER_SNOW;
            }

            pl.zombek.client.core.rendereffect.fog.FogParameters params = controller.computeFogParameters(
                    16.0f, 128.0f,
                    event.getRed(), event.getGreen(), event.getBlue(), 1.0f,
                    dim, env
            );

            event.setRed(params.red());
            event.setGreen(params.green());
            event.setBlue(params.blue());
        }
    }
}
