package pl.zombek.client.forge.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.zombek.client.core.ClientConstants;
import pl.zombek.client.core.modules.AbstractModule;
import pl.zombek.client.core.modules.ModuleManager;
import pl.zombek.client.core.settings.KeybindSetting;
import pl.zombek.client.forge.gui.ZombekForgeScreen;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keybind manager for Minecraft Forge.
 * Registers KeyMapping for the Right Shift menu, detects edge presses,
 * handles module toggles, and provides conflict detection.
 */
public class ForgeKeybindManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ForgeKeybindManager.class);
    private static final ForgeKeybindManager INSTANCE = new ForgeKeybindManager();

    public static final int DEFAULT_MENU_KEY = GLFW.GLFW_KEY_RIGHT_SHIFT;

    public static final KeyMapping MENU_KEY_MAPPING = new KeyMapping(
            "key." + ClientConstants.MOD_ID + ".open_menu",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            DEFAULT_MENU_KEY,
            "category." + ClientConstants.MOD_ID + ".general"
    );

    public static final KeyMapping HUD_EDITOR_KEY_MAPPING = new KeyMapping(
            "key." + ClientConstants.MOD_ID + ".hud_editor",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            "category." + ClientConstants.MOD_ID + ".general"
    );

    public static final KeyMapping HUD_PROFILE_SWITCH_KEY_MAPPING = new KeyMapping(
            "key." + ClientConstants.MOD_ID + ".hud_profile_switch",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            "category." + ClientConstants.MOD_ID + ".general"
    );

    public static final KeyMapping FREELOOK_KEY_MAPPING = new KeyMapping(
            "key." + ClientConstants.MOD_ID + ".freelook",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "category." + ClientConstants.MOD_ID + ".general"
    );

    public static final KeyMapping TOGGLE_SPRINT_KEY_MAPPING = new KeyMapping(
            "key." + ClientConstants.MOD_ID + ".toggle_sprint",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            "category." + ClientConstants.MOD_ID + ".general"
    );

    public static final KeyMapping TOGGLE_SNEAK_KEY_MAPPING = new KeyMapping(
            "key." + ClientConstants.MOD_ID + ".toggle_sneak",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "category." + ClientConstants.MOD_ID + ".general"
    );

    public static final KeyMapping SNAPLOOK_KEY_MAPPING = new KeyMapping(
            "key." + ClientConstants.MOD_ID + ".snaplook",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "category." + ClientConstants.MOD_ID + ".general"
    );

    public static final KeyMapping ZOOM_KEY_MAPPING = new KeyMapping(
            "key." + ClientConstants.MOD_ID + ".zoom",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            "category." + ClientConstants.MOD_ID + ".general"
    );

    public static final KeyMapping FOV_CHANGER_KEY_MAPPING = new KeyMapping(
            "key." + ClientConstants.MOD_ID + ".fov_changer",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            "category." + ClientConstants.MOD_ID + ".general"
    );

    public static final KeyMapping MOTION_BLUR_KEY_MAPPING = new KeyMapping(
            "key." + ClientConstants.MOD_ID + ".motion_blur",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            "category." + ClientConstants.MOD_ID + ".general"
    );

    public static final KeyMapping HURT_CAM_KEY_MAPPING = new KeyMapping(
            "key." + ClientConstants.MOD_ID + ".hurt_cam",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            "category." + ClientConstants.MOD_ID + ".general"
    );

    private final ModuleManager moduleManager;
    private final Set<Integer> pressedKeys = ConcurrentHashMap.newKeySet();

    private ForgeKeybindManager() {
        this.moduleManager = ModuleManager.getInstance();
    }

    public static ForgeKeybindManager getInstance() {
        return INSTANCE;
    }

    public void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(MENU_KEY_MAPPING);
        event.register(HUD_EDITOR_KEY_MAPPING);
        event.register(HUD_PROFILE_SWITCH_KEY_MAPPING);
        event.register(FREELOOK_KEY_MAPPING);
        event.register(TOGGLE_SPRINT_KEY_MAPPING);
        event.register(TOGGLE_SNEAK_KEY_MAPPING);
        event.register(SNAPLOOK_KEY_MAPPING);
        event.register(ZOOM_KEY_MAPPING);
        event.register(FOV_CHANGER_KEY_MAPPING);
        event.register(MOTION_BLUR_KEY_MAPPING);
        event.register(HURT_CAM_KEY_MAPPING);

        pl.zombek.client.core.modules.pvp.snaplook.SnaplookController.getInstance().setAdapter(new pl.zombek.client.core.modules.pvp.snaplook.ISnaplookPerspectiveAdapter() {
            @Override
            public pl.zombek.client.core.modules.pvp.snaplook.SnaplookPerspective getPerspective() {
                Minecraft mc = Minecraft.getInstance();
                if (mc == null || mc.options == null) return pl.zombek.client.core.modules.pvp.snaplook.SnaplookPerspective.FIRST_PERSON;
                return switch (mc.options.getCameraType()) {
                    case FIRST_PERSON -> pl.zombek.client.core.modules.pvp.snaplook.SnaplookPerspective.FIRST_PERSON;
                    case THIRD_PERSON_BACK -> pl.zombek.client.core.modules.pvp.snaplook.SnaplookPerspective.THIRD_PERSON_BACK;
                    case THIRD_PERSON_FRONT -> pl.zombek.client.core.modules.pvp.snaplook.SnaplookPerspective.THIRD_PERSON_FRONT;
                };
            }

            @Override
            public void setPerspective(pl.zombek.client.core.modules.pvp.snaplook.SnaplookPerspective perspective) {
                Minecraft mc = Minecraft.getInstance();
                if (mc == null || mc.options == null) return;
                switch (perspective) {
                    case FIRST_PERSON -> mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
                    case THIRD_PERSON_BACK -> mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK);
                    case THIRD_PERSON_FRONT -> mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT);
                }
            }
        });

        LOGGER.info("[{}] Registered Forge KeyMappings for Menu, Freelook, Snaplook, ToggleSprint, ToggleSneak.", ClientConstants.MOD_ID);
    }

    public void handleInputTick(Minecraft client) {
        if (client == null) return;

        // 1. Process Menu Key
        while (MENU_KEY_MAPPING.consumeClick()) {
            if (client.screen == null) {
                client.setScreen(new ZombekForgeScreen());
                return;
            }
        }

        // Process HUD Editor Key
        while (HUD_EDITOR_KEY_MAPPING.consumeClick()) {
            if (client.screen == null) {
                client.setScreen(new pl.zombek.client.forge.gui.ZombekForgeHudEditorScreen());
                return;
            }
        }

        // Process HUD Profile Switch Key
        while (HUD_PROFILE_SWITCH_KEY_MAPPING.consumeClick()) {
            pl.zombek.client.core.hud.editor.HudEditorManager.getInstance().switchProfileNext();
        }

        // Process Freelook Hotkey
        if (client.player != null && client.screen == null) {
            boolean flDown = FREELOOK_KEY_MAPPING.isDown();
            pl.zombek.client.core.modules.visual.FreelookModule freelook = pl.zombek.client.core.modules.visual.FreelookModule.getInstance();
            if (freelook != null && freelook.isEnabled()) {
                freelook.handleKeyInput(flDown, client.player.getYRot(), client.player.getXRot());
            }

            // Process Toggle Sprint Hotkey
            boolean tsDown = TOGGLE_SPRINT_KEY_MAPPING.isDown();
            pl.zombek.client.core.modules.pvp.ToggleSprintModule sprint = pl.zombek.client.core.modules.pvp.ToggleSprintModule.getInstance();
            if (sprint != null && sprint.isEnabled()) {
                sprint.handleKeyInput(tsDown);
            }

            // Process Toggle Sneak Hotkey
            boolean snDown = TOGGLE_SNEAK_KEY_MAPPING.isDown();
            pl.zombek.client.core.modules.pvp.ToggleSneakModule sneak = pl.zombek.client.core.modules.pvp.ToggleSneakModule.getInstance();
            if (sneak != null && sneak.isEnabled()) {
                sneak.handleKeyInput(snDown);
            }

            // Process Snaplook Hotkey
            boolean slDown = SNAPLOOK_KEY_MAPPING.isDown();
            pl.zombek.client.core.modules.pvp.snaplook.SnaplookController snaplook = pl.zombek.client.core.modules.pvp.snaplook.SnaplookController.getInstance();
            if (slDown) {
                snaplook.onKeyPressed();
            } else {
                snaplook.onKeyReleased();
            }

            // Process Zoom Hotkey
            boolean zmDown = ZOOM_KEY_MAPPING.isDown();
            pl.zombek.client.core.camera.ZoomController zoom = pl.zombek.client.core.camera.ZoomController.getInstance();
            if (zmDown) {
                zoom.onKeyPressed();
            } else {
                zoom.onKeyReleased();
            }

            // Process FOV Changer Hotkey
            while (FOV_CHANGER_KEY_MAPPING.consumeClick()) {
                pl.zombek.client.core.modules.visual.FovChangerModule mod = pl.zombek.client.core.modules.visual.FovChangerModule.getInstance();
                if (mod != null) mod.toggle();
            }

            // Process Motion Blur Hotkey
            while (MOTION_BLUR_KEY_MAPPING.consumeClick()) {
                pl.zombek.client.core.modules.visual.MotionBlurModule mod = pl.zombek.client.core.modules.visual.MotionBlurModule.getInstance();
                if (mod != null) mod.toggle();
            }

            // Process Hurt Cam Hotkey
            while (HURT_CAM_KEY_MAPPING.consumeClick()) {
                pl.zombek.client.core.modules.visual.HurtCamModule mod = pl.zombek.client.core.modules.visual.HurtCamModule.getInstance();
                if (mod != null) mod.toggle();
            }
        } else {
            pl.zombek.client.core.camera.ZoomController.getInstance().onGuiOpened();
        }

        // 2. Process module hotkeys in-game
        if (client.screen == null && client.level != null) {
            long windowHandle = client.getWindow().getWindow();
            for (AbstractModule module : moduleManager.getModules()) {
                int key = module.getKeybind();
                if (key == KeybindSetting.KEY_NONE || key == GLFW.GLFW_KEY_UNKNOWN) {
                    continue;
                }

                boolean isCurrentlyDown = isInputDown(windowHandle, key);
                boolean wasDown = pressedKeys.contains(key);

                if (isCurrentlyDown && !wasDown) {
                    pressedKeys.add(key);
                    module.toggle();
                    LOGGER.debug("Module '{}' toggled via Forge key {} (state: {})",
                            module.getId(), key, module.isEnabled());
                } else if (!isCurrentlyDown && wasDown) {
                    pressedKeys.remove(key);
                }
            }
        } else {
            pressedKeys.clear();
        }
    }

    private boolean isInputDown(long windowHandle, int keyCode) {
        try {
            if (keyCode >= 1000 && keyCode <= 1007) {
                return GLFW.glfwGetMouseButton(windowHandle, keyCode - 1000) == GLFW.GLFW_PRESS;
            } else {
                return GLFW.glfwGetKey(windowHandle, keyCode) == GLFW.GLFW_PRESS;
            }
        } catch (Throwable ignored) {
            return false;
        }
    }

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

    public boolean hasConflict(int keyCode) {
        return getConflictingModules(keyCode).size() > 1;
    }
}
