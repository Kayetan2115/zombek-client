package pl.zombek.client.settings;

import org.lwjgl.glfw.GLFW;

/**
 * Keybind setting representing a GLFW keyboard key or mouse button binding.
 * 0 or GLFW_KEY_UNKNOWN (-1) represents unbound (NONE).
 */
public class KeybindSetting extends Setting<Integer> {
    public static final int KEY_NONE = 0;

    public KeybindSetting(String id, String name, String description, int defaultKeyCode) {
        super(id, name, description, defaultKeyCode);
    }

    public KeybindSetting(String id, String name, String description) {
        this(id, name, description, KEY_NONE);
    }

    public boolean isBound() {
        return getValue() != KEY_NONE && getValue() != GLFW.GLFW_KEY_UNKNOWN;
    }

    public String getKeyName() {
        return getKeyName(getValue());
    }

    public static String getKeyName(int keyCode) {
        if (keyCode == KEY_NONE || keyCode == GLFW.GLFW_KEY_UNKNOWN) {
            return "NONE";
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) return "RSHIFT";
        if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT) return "LSHIFT";
        if (keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL) return "RCTRL";
        if (keyCode == GLFW.GLFW_KEY_LEFT_CONTROL) return "LCTRL";
        if (keyCode == GLFW.GLFW_KEY_RIGHT_ALT) return "RALT";
        if (keyCode == GLFW.GLFW_KEY_LEFT_ALT) return "LALT";
        if (keyCode == GLFW.GLFW_KEY_SPACE) return "SPACE";
        if (keyCode == GLFW.GLFW_KEY_ENTER) return "ENTER";
        if (keyCode == GLFW.GLFW_KEY_TAB) return "TAB";
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) return "ESC";
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) return "BACK";

        // Function keys
        if (keyCode >= GLFW.GLFW_KEY_F1 && keyCode <= GLFW.GLFW_KEY_F25) {
            return "F" + (keyCode - GLFW.GLFW_KEY_F1 + 1);
        }

        // Try getting GLFW key name
        try {
            String name = GLFW.glfwGetKeyName(keyCode, 0);
            if (name != null && !name.trim().isEmpty()) {
                return name.toUpperCase();
            }
        } catch (Throwable ignored) {
        }

        // Mouse buttons (typically 0-7 or mapped with an offset)
        if (keyCode >= 1000 && keyCode <= 1007) {
            return "M" + (keyCode - 1000 + 1);
        }

        return "KEY_" + keyCode;
    }

    @Override
    public String serialize() {
        return String.valueOf(getValue());
    }

    @Override
    public boolean deserialize(String raw) {
        if (raw == null) return false;
        try {
            int code = Integer.parseInt(raw.trim());
            return setValue(code);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
