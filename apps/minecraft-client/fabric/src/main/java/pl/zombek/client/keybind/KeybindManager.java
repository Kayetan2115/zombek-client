package pl.zombek.client.keybind;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.zombek.client.core.ClientConstants;
import pl.zombek.client.gui.ZombekMenuScreen;
import pl.zombek.client.modules.AbstractModule;
import pl.zombek.client.modules.ModuleManager;
import pl.zombek.client.settings.KeybindSetting;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central manager for keyboard and mouse keybindings.
 * Handles client-wide hotkeys (Right Shift for menu), per-module key triggers,
 * and conflict detection.
 */
public class KeybindManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeybindManager.class);
    private static final KeybindManager INSTANCE = new KeybindManager();

    public static final int DEFAULT_MENU_KEY = GLFW.GLFW_KEY_RIGHT_SHIFT;

    private final ModuleManager moduleManager;
    private KeyBinding menuKeyBinding;
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
    private final Set<Integer> pressedKeys = ConcurrentHashMap.newKeySet();

    private KeybindManager(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    private KeybindManager() {
        this(ModuleManager.getInstance());
    }

    public static KeybindManager getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes keybind hooks and registers the Fabric Menu KeyBinding.
     */
    public void initialize() {
        menuKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key." + ClientConstants.MOD_ID + ".open_menu",
                InputUtil.Type.KEYSYM,
                DEFAULT_MENU_KEY,
                "category." + ClientConstants.MOD_ID + ".general"
        ));

        hudEditorKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key." + ClientConstants.MOD_ID + ".hud_editor",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "category." + ClientConstants.MOD_ID + ".general"
        ));

        hudProfileSwitchKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key." + ClientConstants.MOD_ID + ".hud_profile_switch",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "category." + ClientConstants.MOD_ID + ".general"
        ));

        freelookKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key." + ClientConstants.MOD_ID + ".freelook",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "category." + ClientConstants.MOD_ID + ".general"
        ));

        snaplookKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key." + ClientConstants.MOD_ID + ".snaplook",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "category." + ClientConstants.MOD_ID + ".general"
        ));

        pl.zombek.client.core.modules.pvp.snaplook.SnaplookController.getInstance().setAdapter(new pl.zombek.client.core.modules.pvp.snaplook.ISnaplookPerspectiveAdapter() {
            @Override
            public pl.zombek.client.core.modules.pvp.snaplook.SnaplookPerspective getPerspective() {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client == null || client.options == null) return pl.zombek.client.core.modules.pvp.snaplook.SnaplookPerspective.FIRST_PERSON;
                return switch (client.options.getPerspective()) {
                    case FIRST_PERSON -> pl.zombek.client.core.modules.pvp.snaplook.SnaplookPerspective.FIRST_PERSON;
                    case THIRD_PERSON_BACK -> pl.zombek.client.core.modules.pvp.snaplook.SnaplookPerspective.THIRD_PERSON_BACK;
                    case THIRD_PERSON_FRONT -> pl.zombek.client.core.modules.pvp.snaplook.SnaplookPerspective.THIRD_PERSON_FRONT;
                };
            }

            @Override
            public void setPerspective(pl.zombek.client.core.modules.pvp.snaplook.SnaplookPerspective perspective) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client == null || client.options == null) return;
                switch (perspective) {
                    case FIRST_PERSON -> client.options.setPerspective(net.minecraft.client.option.Perspective.FIRST_PERSON);
                    case THIRD_PERSON_BACK -> client.options.setPerspective(net.minecraft.client.option.Perspective.THIRD_PERSON_BACK);
                    case THIRD_PERSON_FRONT -> client.options.setPerspective(net.minecraft.client.option.Perspective.THIRD_PERSON_FRONT);
                }
            }
        });

        toggleSprintKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key." + ClientConstants.MOD_ID + ".toggle_sprint",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_ALT,
                "category." + ClientConstants.MOD_ID + ".general"
        ));

        toggleSneakKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key." + ClientConstants.MOD_ID + ".toggle_sneak",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                "category." + ClientConstants.MOD_ID + ".general"
        ));

        zoomKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key." + ClientConstants.MOD_ID + ".zoom",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                "category." + ClientConstants.MOD_ID + ".general"
        ));

        fovChangerKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key." + ClientConstants.MOD_ID + ".fov_changer",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "category." + ClientConstants.MOD_ID + ".general"
        ));

        motionBlurKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key." + ClientConstants.MOD_ID + ".motion_blur",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "category." + ClientConstants.MOD_ID + ".general"
        ));

        hurtCamKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key." + ClientConstants.MOD_ID + ".hurt_cam",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "category." + ClientConstants.MOD_ID + ".general"
        ));

        // Hook client tick to handle menu opening and module toggles
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);

        LOGGER.info("KeybindManager initialized with default menu key [RIGHT SHIFT].");
    }

    /**
     * Tick-based key evaluation for in-game triggers.
     */
    public void onClientTick(MinecraftClient client) {
        if (client == null) return;

        // Process Right Shift / Menu Key
        if (menuKeyBinding != null && menuKeyBinding.wasPressed()) {
            if (client.currentScreen == null) {
                client.setScreen(new ZombekMenuScreen());
                return;
            }
        }

        // Process HUD Editor Hotkey
        if (hudEditorKeyBinding != null && hudEditorKeyBinding.wasPressed()) {
            if (client.currentScreen == null) {
                client.setScreen(new pl.zombek.client.gui.ZombekFabricHudEditorScreen());
                return;
            }
        }

        // Process HUD Profile Quick Switch Hotkey
        if (hudProfileSwitchKeyBinding != null && hudProfileSwitchKeyBinding.wasPressed()) {
            pl.zombek.client.core.hud.editor.HudEditorManager.getInstance().switchProfileNext();
        }

        // Process Freelook Hotkey
        if (freelookKeyBinding != null && client.player != null && client.currentScreen == null) {
            boolean isDown = freelookKeyBinding.isPressed();
            pl.zombek.client.core.modules.visual.FreelookModule freelook = pl.zombek.client.core.modules.visual.FreelookModule.getInstance();
            if (freelook != null && freelook.isEnabled()) {
                freelook.handleKeyInput(isDown, client.player.getYaw(), client.player.getPitch());
            }
        }

        // Process Snaplook Hotkey
        if (snaplookKeyBinding != null && client.player != null && client.currentScreen == null) {
            boolean isDown = snaplookKeyBinding.isPressed();
            pl.zombek.client.core.modules.pvp.snaplook.SnaplookController snaplook = pl.zombek.client.core.modules.pvp.snaplook.SnaplookController.getInstance();
            if (isDown) {
                snaplook.onKeyPressed();
            } else {
                snaplook.onKeyReleased();
            }
        }

        // Process Toggle Sprint Hotkey
        if (toggleSprintKeyBinding != null && client.player != null && client.currentScreen == null) {
            boolean isDown = toggleSprintKeyBinding.isPressed();
            pl.zombek.client.core.modules.pvp.ToggleSprintModule sprint = pl.zombek.client.core.modules.pvp.ToggleSprintModule.getInstance();
            if (sprint != null && sprint.isEnabled()) {
                sprint.handleKeyInput(isDown);
            }
        }

        // Process Toggle Sneak Hotkey
        if (toggleSneakKeyBinding != null && client.player != null && client.currentScreen == null) {
            boolean isDown = toggleSneakKeyBinding.isPressed();
            pl.zombek.client.core.modules.pvp.ToggleSneakModule sneak = pl.zombek.client.core.modules.pvp.ToggleSneakModule.getInstance();
            if (sneak != null && sneak.isEnabled()) {
                sneak.handleKeyInput(isDown);
            }
        }

        // Process Zoom Hotkey
        if (zoomKeyBinding != null && client.player != null) {
            if (client.currentScreen == null) {
                boolean isDown = zoomKeyBinding.isPressed();
                pl.zombek.client.core.camera.ZoomController zoomController = pl.zombek.client.core.camera.ZoomController.getInstance();
                if (isDown) {
                    zoomController.onKeyPressed();
                } else {
                    zoomController.onKeyReleased();
                }
            } else {
                pl.zombek.client.core.camera.ZoomController.getInstance().onGuiOpened();
            }
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

        // Only process module hotkeys if the player is in-game without an active screen (chat, inventory, etc.)
        if (client.currentScreen == null && client.world != null) {
            long windowHandle = client.getWindow().getHandle();
            for (AbstractModule module : moduleManager.getModules()) {
                int key = module.getKeybind();
                if (key == KeybindSetting.KEY_NONE || key == GLFW.GLFW_KEY_UNKNOWN) {
                    continue;
                }

                boolean isCurrentlyDown = isInputDown(windowHandle, key);
                boolean wasDown = pressedKeys.contains(key);

                if (isCurrentlyDown && !wasDown) {
                    // Key press edge triggered
                    pressedKeys.add(key);
                    module.toggle();
                    LOGGER.debug("Module '{}' toggled by key {} (new state: {})",
                            module.getId(), key, module.isEnabled());
                } else if (!isCurrentlyDown && wasDown) {
                    // Key released
                    pressedKeys.remove(key);
                }
            }
        } else {
            // Screen is open, reset pressed states
            pressedKeys.clear();
        }
    }

    private boolean isInputDown(long windowHandle, int keyCode) {
        try {
            if (keyCode >= 1000 && keyCode <= 1007) {
                // Mouse button
                return GLFW.glfwGetMouseButton(windowHandle, keyCode - 1000) == GLFW.GLFW_PRESS;
            } else {
                return GLFW.glfwGetKey(windowHandle, keyCode) == GLFW.GLFW_PRESS;
            }
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * Finds any modules conflicting on the specified key code.
     */
    public List<AbstractModule> getConflictingModules(int keyCode) {
        if (keyCode == KeybindSetting.KEY_NONE || keyCode == GLFW.GLFW_KEY_UNKNOWN) {
            return Collections.emptyList();
        }
        List<AbstractModule> conflicts = new ArrayList<>();
        for (AbstractModule module : moduleManager.getModules()) {
            if (module.getKeybind() == keyCode) {
                conflicts.add(module);
            }
        }
        return conflicts;
    }

    /**
     * Returns true if more than one module shares the given key code.
     */
    public boolean hasConflict(int keyCode) {
        return getConflictingModules(keyCode).size() > 1;
    }

    public KeyBinding getSnaplookKeyBinding() {
        return snaplookKeyBinding;
    }
}
