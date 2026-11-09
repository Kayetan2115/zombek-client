package pl.zombek.client.core.tracking;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Universal tracker of keyboard key states and mouse buttons for the Keystrokes module.
 * Provides clean reset hooks when the game loses focus, enters menus, or exits worlds.
 */
public class KeyTracker {
    private static final KeyTracker INSTANCE = new KeyTracker();

    // Standard GLFW Key Codes
    public static final int KEY_W = 87;
    public static final int KEY_A = 65;
    public static final int KEY_S = 83;
    public static final int KEY_D = 68;
    public static final int KEY_SPACE = 32;
    public static final int KEY_LEFT_SHIFT = 340;
    public static final int KEY_LEFT_CONTROL = 341;

    // Standard GLFW Mouse Buttons
    public static final int MOUSE_BUTTON_LEFT = 0;
    public static final int MOUSE_BUTTON_RIGHT = 1;

    private final Set<Integer> pressedKeys = ConcurrentHashMap.newKeySet();
    private volatile boolean leftMouseDown = false;
    private volatile boolean rightMouseDown = false;

    private KeyTracker() {}

    public static KeyTracker getInstance() {
        return INSTANCE;
    }

    public void setKeyPressed(int keyCode, boolean isDown) {
        if (isDown) {
            pressedKeys.add(keyCode);
        } else {
            pressedKeys.remove(keyCode);
        }
    }

    public boolean isKeyPressed(int keyCode) {
        return pressedKeys.contains(keyCode);
    }

    public void setMouseButtonPressed(int button, boolean isDown) {
        if (button == MOUSE_BUTTON_LEFT) {
            this.leftMouseDown = isDown;
        } else if (button == MOUSE_BUTTON_RIGHT) {
            this.rightMouseDown = isDown;
        }
    }

    public boolean isMouseButtonPressed(int button) {
        if (button == MOUSE_BUTTON_LEFT) {
            return leftMouseDown;
        } else if (button == MOUSE_BUTTON_RIGHT) {
            return rightMouseDown;
        }
        return false;
    }

    public boolean isLeftMouseDown() {
        return leftMouseDown;
    }

    public boolean isRightMouseDown() {
        return rightMouseDown;
    }

    /**
     * Resets all pressed keys and mouse buttons.
     * Invoked when Minecraft loses focus, a GUI screen opens, or upon leaving world.
     */
    public void resetAll() {
        pressedKeys.clear();
        leftMouseDown = false;
        rightMouseDown = false;
    }

    /**
     * Hook called on window focus loss.
     */
    public void onFocusLost() {
        resetAll();
    }
}
