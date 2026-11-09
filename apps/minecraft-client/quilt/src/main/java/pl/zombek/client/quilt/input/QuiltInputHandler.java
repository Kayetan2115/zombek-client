package pl.zombek.client.quilt.input;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import pl.zombek.client.core.tracking.ClickTracker;
import pl.zombek.client.core.tracking.KeyTracker;

/**
 * Handles keyboard key state polling and mouse click edge detection for Quilt.
 * Safely resets states when focus is lost or a screen/menu is opened.
 */
public class QuiltInputHandler {
    private static final QuiltInputHandler INSTANCE = new QuiltInputHandler();

    private boolean wasLmbDown = false;
    private boolean wasRmbDown = false;
    private boolean wasInWorld = false;

    private QuiltInputHandler() {}

    public static QuiltInputHandler getInstance() {
        return INSTANCE;
    }

    public void onClientTick(MinecraftClient client) {
        if (client == null) return;

        if (client.world != null) {
            wasInWorld = true;
        }

        // Only track active gameplay input if no GUI screen is open, player is in a world, and window is focused
        if (client.currentScreen == null && client.world != null && client.isWindowFocused() && client.getWindow() != null) {
            long window = client.getWindow().getHandle();
            KeyTracker keyTracker = KeyTracker.getInstance();

            // 1. Movement keys
            boolean wDown = (client.options != null && client.options.forwardKey.isPressed())
                    || (window != 0 && GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS);
            boolean aDown = (client.options != null && client.options.leftKey.isPressed())
                    || (window != 0 && GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS);
            boolean sDown = (client.options != null && client.options.backKey.isPressed())
                    || (window != 0 && GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS);
            boolean dDown = (client.options != null && client.options.rightKey.isPressed())
                    || (window != 0 && GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS);
            boolean spaceDown = (client.options != null && client.options.jumpKey.isPressed())
                    || (window != 0 && GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS);
            boolean shiftDown = (client.options != null && client.options.sneakKey.isPressed())
                    || (window != 0 && GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS);
            boolean ctrlDown = (client.options != null && client.options.sprintKey.isPressed())
                    || (window != 0 && GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS);

            keyTracker.setKeyPressed(KeyTracker.KEY_W, wDown);
            keyTracker.setKeyPressed(KeyTracker.KEY_A, aDown);
            keyTracker.setKeyPressed(KeyTracker.KEY_S, sDown);
            keyTracker.setKeyPressed(KeyTracker.KEY_D, dDown);
            keyTracker.setKeyPressed(KeyTracker.KEY_SPACE, spaceDown);
            keyTracker.setKeyPressed(KeyTracker.KEY_LEFT_SHIFT, shiftDown);
            keyTracker.setKeyPressed(KeyTracker.KEY_LEFT_CONTROL, ctrlDown);

            // Movement Toggle conditions & application
            if (client.player != null) {
                boolean movingForward = client.player.input != null && client.player.input.movementForward > 0;
                boolean canSprint = client.player.getHungerManager().getFoodLevel() > 6
                        && !client.player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.BLINDNESS)
                        && !client.player.horizontalCollision;
                boolean canSneak = !client.player.isSwimming() && !client.player.isFallFlying();
                boolean usingItem = client.player.isUsingItem();
                boolean flying = client.player.getAbilities().flying;
                boolean riding = client.player.hasVehicle();

                pl.zombek.client.core.movement.MovementToggleController.getInstance().updateGameConditions(
                        movingForward, canSprint, canSneak, usingItem, flying, riding
                );
                pl.zombek.client.core.movement.MovementToggleController.getInstance().setPhysicalSprintDown(ctrlDown);
                pl.zombek.client.core.movement.MovementToggleController.getInstance().setPhysicalSneakDown(shiftDown);

                // Apply Sprint
                pl.zombek.client.core.modules.pvp.ToggleSprintModule sprintMod = pl.zombek.client.core.modules.pvp.ToggleSprintModule.getInstance();
                if (sprintMod != null && sprintMod.isEnabled()) {
                    boolean sprintActive = pl.zombek.client.core.movement.MovementToggleController.getInstance().isSprintActive(
                            true,
                            sprintMod.getActivationModeSetting().getValue(),
                            sprintMod.getAlwaysSprintSetting().getValue(),
                            sprintMod.getPreserveVanillaSprintSetting().getValue(),
                            sprintMod.getDisableWhileSneakingSetting().getValue(),
                            sprintMod.getDisableWhileUsingItemsSetting().getValue()
                    );
                    if (sprintActive) {
                        client.player.setSprinting(true);
                    }
                }

                // Apply Sneak
                pl.zombek.client.core.modules.pvp.ToggleSneakModule sneakMod = pl.zombek.client.core.modules.pvp.ToggleSneakModule.getInstance();
                if (sneakMod != null && sneakMod.isEnabled()) {
                    boolean sneakActive = pl.zombek.client.core.movement.MovementToggleController.getInstance().isSneakActive(
                            true,
                            sneakMod.getActivationModeSetting().getValue(),
                            sneakMod.getPreserveVanillaSneakSetting().getValue(),
                            sneakMod.getDisableWhileFlyingSetting().getValue(),
                            sneakMod.getDisableWhileRidingSetting().getValue()
                    );
                    if (sneakActive && client.player.input != null) {
                        client.player.input.sneaking = true;
                    }
                }
            }

            // 2. Mouse buttons & click tracking
            boolean lmbDown = (client.options != null && client.options.attackKey.isPressed())
                    || (window != 0 && GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS);
            boolean rmbDown = (client.options != null && client.options.useKey.isPressed())
                    || (window != 0 && GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_2) == GLFW.GLFW_PRESS);

            if (lmbDown && !wasLmbDown) {
                ClickTracker.getInstance().recordLeftClick();

                if (client.targetedEntity != null) {
                    pl.zombek.client.core.tracking.ComboTracker.getInstance().recordHit(client.targetedEntity.getId(), true);
                    pl.zombek.client.core.tracking.HitColorTracker.getInstance().recordHit(
                            client.targetedEntity.getId(),
                            pl.zombek.client.quilt.integration.QuiltEntityClassifier.classify(client.targetedEntity),
                            true,
                            true
                    );

                    if (client.player != null) {
                        net.minecraft.util.math.Vec3d eyePos = client.player.getEyePos();
                        net.minecraft.util.math.Box box = client.targetedEntity.getBoundingBox();
                        double dist = pl.zombek.client.core.tracking.ReachTracker.calculateDistanceToAABB(
                                eyePos.x, eyePos.y, eyePos.z,
                                box.minX, box.minY, box.minZ,
                                box.maxX, box.maxY, box.maxZ
                        );
                        pl.zombek.client.core.tracking.ReachTracker.getInstance().recordObservedHit(
                                dist, client.targetedEntity.getId(), client.targetedEntity.getName().getString()
                        );
                        pl.zombek.client.core.tracking.PvPSession session = pl.zombek.client.core.tracking.PvPSessionManager.getInstance().getCurrentSession();
                        session.recordAttackAttempt();
                        session.recordObservedHit();
                        session.recordCombo(pl.zombek.client.core.tracking.ComboTracker.getInstance().getCurrentCombo());
                    }
                } else if (client.crosshairTarget == null || client.crosshairTarget.getType() == net.minecraft.util.hit.HitResult.Type.MISS) {
                    pl.zombek.client.core.tracking.ComboTracker.getInstance().recordMiss(false);
                    pl.zombek.client.core.tracking.PvPSessionManager.getInstance().getCurrentSession().recordAttackAttempt();
                    pl.zombek.client.core.tracking.ReachTracker.getInstance().recordAttackAttempt(0.0, -1, "Miss");
                }
            }
            if (rmbDown && !wasRmbDown) {
                ClickTracker.getInstance().recordRightClick();
                trackProjectileUse(client);
            }

            keyTracker.setMouseButtonPressed(KeyTracker.MOUSE_BUTTON_LEFT, lmbDown);
            keyTracker.setMouseButtonPressed(KeyTracker.MOUSE_BUTTON_RIGHT, rmbDown);

            wasLmbDown = lmbDown;
            wasRmbDown = rmbDown;

            // Check player health
            if (client.player != null && !client.player.isAlive()) {
                pl.zombek.client.core.movement.MovementToggleController.getInstance().onPlayerDeath();
                pl.zombek.client.core.uhc.UhcSessionTracker.getInstance().onGameOver();
                pl.zombek.client.core.tracking.ComboTracker.getInstance().resetCombo();
                pl.zombek.client.core.tracking.PvPSessionManager.getInstance().onPlayerDeath(true);
            }

            // Freelook camera control
            pl.zombek.client.core.modules.visual.FreelookCameraController flController = pl.zombek.client.core.modules.visual.FreelookCameraController.getInstance();
            pl.zombek.client.core.modules.visual.FreelookModule flModule = pl.zombek.client.core.modules.visual.FreelookModule.getInstance();
            if (flController.isActive() && flModule != null && flModule.isEnabled() && client.player != null) {
                float deltaYaw = client.player.getYaw() - flController.getOriginalYaw();
                float deltaPitch = client.player.getPitch() - flController.getOriginalPitch();
                if (deltaYaw != 0.0f || deltaPitch != 0.0f) {
                    flController.handleMouseInput(
                            deltaYaw, deltaPitch,
                            flModule.getHSensitivitySetting().getValue().floatValue(),
                            flModule.getVSensitivitySetting().getValue().floatValue(),
                            flModule.getInvertHSetting().getValue(),
                            flModule.getInvertVSetting().getValue(),
                            flModule.getMaxPitchSetting().getValue().floatValue(),
                            flModule.getMaxYawSetting().getValue().floatValue()
                    );
                    client.player.setYaw(flController.getOriginalYaw());
                    client.player.setPitch(flController.getOriginalPitch());
                    client.player.prevYaw = flController.getOriginalYaw();
                    client.player.prevPitch = flController.getOriginalPitch();
                    client.player.headYaw = flController.getOriginalYaw();
                    client.player.prevHeadYaw = flController.getOriginalYaw();
                    client.player.bodyYaw = flController.getOriginalYaw();
                    client.player.prevBodyYaw = flController.getOriginalYaw();
                }
            } else if (flController.isInSnapback() && client.player != null) {
                flController.updateSnapback(System.currentTimeMillis(), client.player.getYaw(), client.player.getPitch());
            }

            // Health & Target update for PvP Info & Reach
            if (client.player != null) {
                pl.zombek.client.core.tracking.PvPSession session = pl.zombek.client.core.tracking.PvPSessionManager.getInstance().getCurrentSession();
                session.updatePlayerHealth(client.player.getHealth(), client.player.getAbsorptionAmount());
                if (client.player.hurtTime == 9) {
                    session.recordDamageReceived(1.0f);
                }

                if (client.targetedEntity instanceof net.minecraft.entity.LivingEntity targetLiving) {
                    session.updateTargetHealth(targetLiving.getHealth());
                    net.minecraft.util.math.Vec3d eyePos = client.player.getEyePos();
                    net.minecraft.util.math.Box box = targetLiving.getBoundingBox();
                    double targetDist = pl.zombek.client.core.tracking.ReachTracker.calculateDistanceToAABB(
                            eyePos.x, eyePos.y, eyePos.z,
                            box.minX, box.minY, box.minZ,
                            box.maxX, box.maxY, box.maxZ
                    );
                    pl.zombek.client.core.tracking.ReachTracker.getInstance().setCurrentTargetDistance(targetDist);
                } else {
                    session.clearTargetHealth();
                    pl.zombek.client.core.tracking.ReachTracker.getInstance().setCurrentTargetDistance(0.0);
                }
            }

            // Damage detection for Hit Color
            if (client.world != null && pl.zombek.client.core.modules.visual.HitColorModule.getInstance() != null
                    && pl.zombek.client.core.modules.visual.HitColorModule.getInstance().isEnabled()) {
                for (net.minecraft.entity.Entity entity : client.world.getEntities()) {
                    if (entity instanceof net.minecraft.entity.LivingEntity living && living.hurtTime == 9) {
                        boolean isTargeted = client.targetedEntity == living;
                        pl.zombek.client.core.tracking.HitColorTracker.getInstance().recordHit(
                                living.getId(),
                                pl.zombek.client.quilt.integration.QuiltEntityClassifier.classify(living),
                                false,
                                isTargeted
                        );
                    }
                }
            }
        } else {
            // Window lost focus, in menu, or disconnected -> cleanly reset
            wasLmbDown = false;
            wasRmbDown = false;
            KeyTracker.getInstance().resetAll();

            pl.zombek.client.core.movement.MovementToggleController.getInstance().onFocusLost();
            pl.zombek.client.core.modules.visual.FreelookCameraController.getInstance().reset();
            pl.zombek.client.core.modules.pvp.snaplook.SnaplookController.getInstance().onFocusLost();

            if (client.world == null && wasInWorld) {
                wasInWorld = false;
                pl.zombek.client.core.movement.MovementToggleController.getInstance().onWorldChange();
                pl.zombek.client.core.modules.pvp.snaplook.SnaplookController.getInstance().onWorldChange();
                pl.zombek.client.core.uhc.UhcSessionTracker.getInstance().resetSession();
                pl.zombek.client.core.tracking.ComboTracker.getInstance().resetCombo();
                pl.zombek.client.core.tracking.HitColorTracker.getInstance().clear();
                pl.zombek.client.core.tracking.PvPSessionManager.getInstance().onWorldChange(true);
                pl.zombek.client.core.tracking.ReachTracker.getInstance().resetStatistics();
            }
        }
    }

    private void trackProjectileUse(MinecraftClient client) {
        if (client == null || client.player == null) return;
        net.minecraft.item.ItemStack main = client.player.getMainHandStack();
        net.minecraft.item.ItemStack off = client.player.getOffHandStack();
        net.minecraft.item.Item item = !main.isEmpty() ? main.getItem() : off.getItem();

        if (item instanceof net.minecraft.item.BowItem || item instanceof net.minecraft.item.CrossbowItem) {
            pl.zombek.client.core.tracking.ProjectileTracker.getInstance().recordProjectileFired(pl.zombek.client.core.tracking.ProjectileType.ARROW);
        } else if (item instanceof net.minecraft.item.SnowballItem) {
            pl.zombek.client.core.tracking.ProjectileTracker.getInstance().recordProjectileFired(pl.zombek.client.core.tracking.ProjectileType.SNOWBALL);
        } else if (item instanceof net.minecraft.item.EggItem) {
            pl.zombek.client.core.tracking.ProjectileTracker.getInstance().recordProjectileFired(pl.zombek.client.core.tracking.ProjectileType.EGG);
        } else if (item instanceof net.minecraft.item.TridentItem) {
            pl.zombek.client.core.tracking.ProjectileTracker.getInstance().recordProjectileFired(pl.zombek.client.core.tracking.ProjectileType.TRIDENT);
        } else if (item instanceof net.minecraft.item.SplashPotionItem || item instanceof net.minecraft.item.LingeringPotionItem) {
            pl.zombek.client.core.tracking.ProjectileTracker.getInstance().recordProjectileFired(pl.zombek.client.core.tracking.ProjectileType.POTION);
        } else if (item instanceof net.minecraft.item.FishingRodItem) {
            pl.zombek.client.core.tracking.ProjectileTracker.getInstance().recordProjectileFired(pl.zombek.client.core.tracking.ProjectileType.FISHING_BOBBER);
        }
    }
}
