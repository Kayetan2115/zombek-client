package pl.zombek.client.quilt.input;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.zombek.client.core.bootstrap.ZombekCore;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleManager;
import pl.zombek.client.quilt.gui.ZombekQuiltScreen;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Manages keybinds and input edge-detection for ZombekClient on Quilt Loader.
 * Uses native Minecraft KeyBinding without requiring QSL or Fabric API.
 */
public class QuiltKeybindManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuiltKeybindManager.class);
    private static final QuiltKeybindManager INSTANCE = new QuiltKeybindManager();

    public static final String KEY_CATEGORY_ZOMBEK = "key.categories.zombekclient";
    public static final String KEY_OPEN_MENU_NAME = "key.zombekclient.open_menu";
    public static final String KEY_HUD_EDITOR_NAME = "key.zombekclient.hud_editor";
    public static final String KEY_HUD_PROFILE_SWITCH_NAME = "key.zombekclient.hud_profile_switch";
    public static final String KEY_FREELOOK_NAME = "key.zombekclient.freelook";
    public static final String KEY_SNAPLOOK_NAME = "key.zombekclient.snaplook";
    public static final String KEY_TOGGLE_SPRINT_NAME = "key.zombekclient.toggle_sprint";
    public static final String KEY_TOGGLE_SNEAK_NAME = "key.zombekclient.toggle_sneak";
    public static final String KEY_ZOOM_NAME = "key.zombekclient.zoom";
    public static final String KEY_FOV_CHANGER_NAME = "key.zombekclient.fov_changer";
    public static final String KEY_MOTION_BLUR_NAME = "key.zombekclient.motion_blur";
    public static final String KEY_HURT_CAM_NAME = "key.zombekclient.hurt_cam";
    public static final int DEFAULT_OPEN_KEY = GLFW.GLFW_KEY_RIGHT_SHIFT;
    public static final int DEFAULT_FREELOOK_KEY = GLFW.GLFW_KEY_V;
    public static final int DEFAULT_SNAPLOOK_KEY = GLFW.GLFW_KEY_V;
    public static final int DEFAULT_TOGGLE_SPRINT_KEY = GLFW.GLFW_KEY_LEFT_ALT;
    public static final int DEFAULT_TOGGLE_SNEAK_KEY = GLFW.GLFW_KEY_C;
    public static final int DEFAULT_ZOOM_KEY = GLFW.GLFW_KEY_Z;

    private KeyBinding openMenuKeyBinding;
    private KeyBinding hudEditorKeyBinding;
    private KeyBinding hudProfileSwitchKeyBinding;
    private KeyBinding freelookKeyBinding;
    private KeyBinding snaplookKeyBinding;
    private KeyBinding toggleSprintKeyBinding;
    private KeyBinding toggleSneakKeyBinding;
    private KeyBinding zoomKeyBinding;
    private KeyBinding fovChangerKeyBinding;
    private KeyBinding motionBlurKeyBinding;
    private KeyBinding hurtCamKeyBinding;
    private boolean menuKeyWasDown = false;
    private final Set<Integer> pressedKeys = new HashSet<>();
    private boolean registered = false;

    private QuiltKeybindManager() {
        this.openMenuKeyBinding = new KeyBinding(
                KEY_OPEN_MENU_NAME,
                InputUtil.Type.KEYSYM,
                DEFAULT_OPEN_KEY,
                KEY_CATEGORY_ZOMBEK
        );
        this.hudEditorKeyBinding = new KeyBinding(
                KEY_HUD_EDITOR_NAME,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                KEY_CATEGORY_ZOMBEK
        );
        this.hudProfileSwitchKeyBinding = new KeyBinding(
                KEY_HUD_PROFILE_SWITCH_NAME,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                KEY_CATEGORY_ZOMBEK
        );
        this.freelookKeyBinding = new KeyBinding(
                KEY_FREELOOK_NAME,
                InputUtil.Type.KEYSYM,
                DEFAULT_FREELOOK_KEY,
                KEY_CATEGORY_ZOMBEK
        );
        this.snaplookKeyBinding = new KeyBinding(
                KEY_SNAPLOOK_NAME,
                InputUtil.Type.KEYSYM,
                DEFAULT_SNAPLOOK_KEY,
                KEY_CATEGORY_ZOMBEK
        );
        this.toggleSprintKeyBinding = new KeyBinding(
                KEY_TOGGLE_SPRINT_NAME,
                InputUtil.Type.KEYSYM,
                DEFAULT_TOGGLE_SPRINT_KEY,
                KEY_CATEGORY_ZOMBEK
        );
        this.toggleSneakKeyBinding = new KeyBinding(
                KEY_TOGGLE_SNEAK_NAME,
                InputUtil.Type.KEYSYM,
                DEFAULT_TOGGLE_SNEAK_KEY,
                KEY_CATEGORY_ZOMBEK
        );
        this.zoomKeyBinding = new KeyBinding(
                KEY_ZOOM_NAME,
                InputUtil.Type.KEYSYM,
                DEFAULT_ZOOM_KEY,
                KEY_CATEGORY_ZOMBEK
        );
        this.fovChangerKeyBinding = new KeyBinding(
                KEY_FOV_CHANGER_NAME,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                KEY_CATEGORY_ZOMBEK
        );
        this.motionBlurKeyBinding = new KeyBinding(
                KEY_MOTION_BLUR_NAME,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                KEY_CATEGORY_ZOMBEK
        );
        this.hurtCamKeyBinding = new KeyBinding(
                KEY_HURT_CAM_NAME,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                KEY_CATEGORY_ZOMBEK
        );
    }

    public static QuiltKeybindManager getInstance() {
        return INSTANCE;
    }

    public KeyBinding getOpenMenuKeyBinding() {
        return openMenuKeyBinding;
    }

    /**
     * Native registration of the KeyBinding into MinecraftClient's GameOptions.allKeys.
     */
    public synchronized void registerKeyMappings(MinecraftClient client) {
        if (registered || client == null || client.options == null) {
            return;
        }

        try {
            if (openMenuKeyBinding != null) {
                Field allKeysField = null;
                for (Field f : GameOptions.class.getDeclaredFields()) {
                    if (f.getType().isArray() && f.getType().getComponentType() == KeyBinding.class) {
                        allKeysField = f;
                        break;
                    }
                }

                if (allKeysField != null) {
                    allKeysField.setAccessible(true);
                    KeyBinding[] current = (KeyBinding[]) allKeysField.get(client.options);
                    if (!ArrayUtils.contains(current, openMenuKeyBinding)) {
                        current = ArrayUtils.add(current, openMenuKeyBinding);
                    }
                    if (hudEditorKeyBinding != null && !ArrayUtils.contains(current, hudEditorKeyBinding)) {
                        current = ArrayUtils.add(current, hudEditorKeyBinding);
                    }
                    if (hudProfileSwitchKeyBinding != null && !ArrayUtils.contains(current, hudProfileSwitchKeyBinding)) {
                        current = ArrayUtils.add(current, hudProfileSwitchKeyBinding);
                    }
                    if (freelookKeyBinding != null && !ArrayUtils.contains(current, freelookKeyBinding)) {
                        current = ArrayUtils.add(current, freelookKeyBinding);
                    }
                    if (snaplookKeyBinding != null && !ArrayUtils.contains(current, snaplookKeyBinding)) {
                        current = ArrayUtils.add(current, snaplookKeyBinding);
                    }
                    if (toggleSprintKeyBinding != null && !ArrayUtils.contains(current, toggleSprintKeyBinding)) {
                        current = ArrayUtils.add(current, toggleSprintKeyBinding);
                    }
                    if (toggleSneakKeyBinding != null && !ArrayUtils.contains(current, toggleSneakKeyBinding)) {
                        current = ArrayUtils.add(current, toggleSneakKeyBinding);
                    }
                    if (zoomKeyBinding != null && !ArrayUtils.contains(current, zoomKeyBinding)) {
                        current = ArrayUtils.add(current, zoomKeyBinding);
                    }
                    if (fovChangerKeyBinding != null && !ArrayUtils.contains(current, fovChangerKeyBinding)) {
                        current = ArrayUtils.add(current, fovChangerKeyBinding);
                    }
                    if (motionBlurKeyBinding != null && !ArrayUtils.contains(current, motionBlurKeyBinding)) {
                        current = ArrayUtils.add(current, motionBlurKeyBinding);
                    }
                    if (hurtCamKeyBinding != null && !ArrayUtils.contains(current, hurtCamKeyBinding)) {
                        current = ArrayUtils.add(current, hurtCamKeyBinding);
                    }
                    allKeysField.set(client.options, current);

                    pl.zombek.client.core.modules.pvp.snaplook.SnaplookController.getInstance().setAdapter(new pl.zombek.client.core.modules.pvp.snaplook.ISnaplookPerspectiveAdapter() {
                        @Override
                        public pl.zombek.client.core.modules.pvp.snaplook.SnaplookPerspective getPerspective() {
                            if (client.options == null) return pl.zombek.client.core.modules.pvp.snaplook.SnaplookPerspective.FIRST_PERSON;
                            return switch (client.options.getPerspective()) {
                                case FIRST_PERSON -> pl.zombek.client.core.modules.pvp.snaplook.SnaplookPerspective.FIRST_PERSON;
                                case THIRD_PERSON_BACK -> pl.zombek.client.core.modules.pvp.snaplook.SnaplookPerspective.THIRD_PERSON_BACK;
                                case THIRD_PERSON_FRONT -> pl.zombek.client.core.modules.pvp.snaplook.SnaplookPerspective.THIRD_PERSON_FRONT;
                            };
                        }

                        @Override
                        public void setPerspective(pl.zombek.client.core.modules.pvp.snaplook.SnaplookPerspective perspective) {
                            if (client.options == null) return;
                            switch (perspective) {
                                case FIRST_PERSON -> client.options.setPerspective(net.minecraft.client.option.Perspective.FIRST_PERSON);
                                case THIRD_PERSON_BACK -> client.options.setPerspective(net.minecraft.client.option.Perspective.THIRD_PERSON_BACK);
                                case THIRD_PERSON_FRONT -> client.options.setPerspective(net.minecraft.client.option.Perspective.THIRD_PERSON_FRONT);
                            }
                        }
                    });

                    LOGGER.info("[ZombekClient-Quilt] Zarejestrowano natywne KeyBindingi (Menu, Freelook, Snaplook, ToggleSprint, ToggleSneak) w GameOptions.allKeys.");
                }
            }
            registered = true;
        } catch (Throwable t) {
            LOGGER.warn("[ZombekClient-Quilt] Rejestracja w allKeys pominięta ({}), aktywna detekcja bezpośrednia GLFW.", t.getMessage());
            registered = true;
        }
    }

    /**
     * Called on each client tick. Performs edge-detection and triggers menu / module actions.
     */
    public void onClientTick(MinecraftClient client) {
        if (client == null) return;

        // Auto-register on first tick if not done yet
        if (!registered && client.options != null) {
            registerKeyMappings(client);
        }

        // Tick input handler for keystrokes and click detection
        QuiltInputHandler.getInstance().onClientTick(client);

        // 1. Edge-detection for opening / closing the menu
        boolean isDown = false;
        if (openMenuKeyBinding != null && openMenuKeyBinding.isPressed()) {
            isDown = true;
        } else if (client.getWindow() != null) {
            long handle = client.getWindow().getHandle();
            if (handle != 0 && GLFW.glfwGetKey(handle, DEFAULT_OPEN_KEY) == GLFW.GLFW_PRESS) {
                isDown = true;
            }
        }

        if (isDown && !menuKeyWasDown) {
            // Rising edge
            if (client.currentScreen == null) {
                client.setScreen(new ZombekQuiltScreen());
            } else if (client.currentScreen instanceof ZombekQuiltScreen) {
                client.setScreen(null);
            }
        }
        menuKeyWasDown = isDown;

        // Process HUD Editor Hotkey
        if (hudEditorKeyBinding != null && hudEditorKeyBinding.wasPressed()) {
            if (client.currentScreen == null) {
                client.setScreen(new pl.zombek.client.quilt.gui.ZombekQuiltHudEditorScreen());
            }
        }

        // Process HUD Profile Switch Hotkey
        if (hudProfileSwitchKeyBinding != null && hudProfileSwitchKeyBinding.wasPressed()) {
            pl.zombek.client.core.hud.editor.HudEditorManager.getInstance().switchProfileNext();
        }

        // Process Freelook Hotkey
        if (client.player != null && client.currentScreen == null) {
            boolean flDown = false;
            if (freelookKeyBinding != null && freelookKeyBinding.isPressed()) {
                flDown = true;
            } else if (client.getWindow() != null) {
                long handle = client.getWindow().getHandle();
                if (handle != 0 && GLFW.glfwGetKey(handle, DEFAULT_FREELOOK_KEY) == GLFW.GLFW_PRESS) {
                    flDown = true;
                }
            }
            pl.zombek.client.core.modules.visual.FreelookModule freelook = pl.zombek.client.core.modules.visual.FreelookModule.getInstance();
            if (freelook != null && freelook.isEnabled()) {
                freelook.handleKeyInput(flDown, client.player.getYaw(), client.player.getPitch());
            }

            // Process Toggle Sprint Hotkey
            boolean tsDown = false;
            if (toggleSprintKeyBinding != null && toggleSprintKeyBinding.isPressed()) {
                tsDown = true;
            } else if (client.getWindow() != null) {
                long handle = client.getWindow().getHandle();
                if (handle != 0 && GLFW.glfwGetKey(handle, DEFAULT_TOGGLE_SPRINT_KEY) == GLFW.GLFW_PRESS) {
                    tsDown = true;
                }
            }
            pl.zombek.client.core.modules.pvp.ToggleSprintModule sprint = pl.zombek.client.core.modules.pvp.ToggleSprintModule.getInstance();
            if (sprint != null && sprint.isEnabled()) {
                sprint.handleKeyInput(tsDown);
            }

            // Process Toggle Sneak Hotkey
            boolean snDown = false;
            if (toggleSneakKeyBinding != null && toggleSneakKeyBinding.isPressed()) {
                snDown = true;
            } else if (client.getWindow() != null) {
                long handle = client.getWindow().getHandle();
                if (handle != 0 && GLFW.glfwGetKey(handle, DEFAULT_TOGGLE_SNEAK_KEY) == GLFW.GLFW_PRESS) {
                    snDown = true;
                }
            }
            pl.zombek.client.core.modules.pvp.ToggleSneakModule sneak = pl.zombek.client.core.modules.pvp.ToggleSneakModule.getInstance();
            if (sneak != null && sneak.isEnabled()) {
                sneak.handleKeyInput(snDown);
            }

            // Process Snaplook Hotkey
            boolean slDown = false;
            if (snaplookKeyBinding != null && snaplookKeyBinding.isPressed()) {
                slDown = true;
            } else if (client.getWindow() != null) {
                long handle = client.getWindow().getHandle();
                if (handle != 0 && GLFW.glfwGetKey(handle, DEFAULT_SNAPLOOK_KEY) == GLFW.GLFW_PRESS) {
                    slDown = true;
                }
            }
            pl.zombek.client.core.modules.pvp.snaplook.SnaplookController snaplook = pl.zombek.client.core.modules.pvp.snaplook.SnaplookController.getInstance();
            if (slDown) {
                snaplook.onKeyPressed();
            } else {
                snaplook.onKeyReleased();
            }

            // Process Zoom Hotkey
            boolean zmDown = false;
            if (zoomKeyBinding != null && zoomKeyBinding.isPressed()) {
                zmDown = true;
            } else if (client.getWindow() != null) {
                long handle = client.getWindow().getHandle();
                if (handle != 0 && GLFW.glfwGetKey(handle, DEFAULT_ZOOM_KEY) == GLFW.GLFW_PRESS) {
                    zmDown = true;
                }
            }
            pl.zombek.client.core.camera.ZoomController zoom = pl.zombek.client.core.camera.ZoomController.getInstance();
            if (zmDown) {
                zoom.onKeyPressed();
            } else {
                zoom.onKeyReleased();
            }

            // Process FOV Changer Hotkey
            if (fovChangerKeyBinding != null && fovChangerKeyBinding.wasPressed()) {
                pl.zombek.client.core.modules.visual.FovChangerModule mod = pl.zombek.client.core.modules.visual.FovChangerModule.getInstance();
                if (mod != null) mod.toggle();
            }

            // Process Motion Blur Hotkey
            if (motionBlurKeyBinding != null && motionBlurKeyBinding.wasPressed()) {
                pl.zombek.client.core.modules.visual.MotionBlurModule mod = pl.zombek.client.core.modules.visual.MotionBlurModule.getInstance();
                if (mod != null) mod.toggle();
            }

            // Process Hurt Cam Hotkey
            if (hurtCamKeyBinding != null && hurtCamKeyBinding.wasPressed()) {
                pl.zombek.client.core.modules.visual.HurtCamModule mod = pl.zombek.client.core.modules.visual.HurtCamModule.getInstance();
                if (mod != null) mod.toggle();
            }
        }

        // 2. Dispatch client tick to active modules
        if (client.world != null) {
            ZombekCore core = ZombekCore.getInstance();
            if (core != null) {
                core.onClientTick();
            }

            if (client.player != null) {
                try {
                    pl.zombek.client.core.worldvisual.ZombekWorldVisualController.getInstance().onClientTick(
                            client.player.getX(), client.player.getEyeY(), client.player.getZ()
                    );
                } catch (Throwable ignored) {}
            }

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
        }

        // 3. Module keybinds check when in game (not inside any GUI)
        if (client.currentScreen == null && client.world != null && client.getWindow() != null) {
            checkModuleKeybinds(client);
        }
    }

    private void checkModuleKeybinds(MinecraftClient client) {
        ModuleManager mm = ZombekCore.getInstance().getModuleManager();
        if (mm == null || client.getWindow() == null) return;

        long window = client.getWindow().getHandle();
        if (window == 0) return;

        List<AbstractModule> modules = mm.getModules();
        for (AbstractModule mod : modules) {
            int key = mod.getKeybind();
            if (key == GLFW.GLFW_KEY_UNKNOWN) continue;

            boolean isDown = GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
            if (isDown && !pressedKeys.contains(key)) {
                // Rising edge for module toggle
                mod.toggle();
                pressedKeys.add(key);
            } else if (!isDown && pressedKeys.contains(key)) {
                pressedKeys.remove(key);
            }
        }
    }

    /**
     * Checks if a given GLFW key code has multiple module bindings assigned.
     */
    public boolean hasConflict(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN) return false;
        return getConflictingModules(keyCode).size() > 1;
    }

    /**
     * Returns the list of modules bound to the same key code.
     */
    public List<AbstractModule> getConflictingModules(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN) return Collections.emptyList();
        ModuleManager mm = ZombekCore.getInstance().getModuleManager();
        if (mm == null) return Collections.emptyList();

        List<AbstractModule> conflicts = new ArrayList<>();
        for (AbstractModule module : mm.getModules()) {
            if (module.getKeybind() == keyCode) {
                conflicts.add(module);
            }
        }
        return conflicts;
    }

    public void setMenuKeyWasDown(boolean wasDown) {
        this.menuKeyWasDown = wasDown;
    }

    public boolean isMenuKeyWasDown() {
        return menuKeyWasDown;
    }
}
