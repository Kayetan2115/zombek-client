package pl.zombek.client.core.camera;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.zombek.client.core.modules.visual.ZoomModule;

/**
 * Controller managing smooth Zoom transitions, scroll-based magnification adjustment,
 * input mode resolution (Hold, Toggle, Toggle+Hold), and lifecycle restoration.
 */
public class ZoomController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZoomController.class);
    private static final ZoomController INSTANCE = new ZoomController();

    private boolean active = false;
    private boolean keyHeld = false;
    private long keyPressTime = 0L;

    private double userConfiguredZoom = 4.0;
    private double currentZoomFactor = 1.0;
    private double targetZoomFactor = 1.0;

    // Animation tracking
    private long animationStartTime = 0L;
    private double animationStartFactor = 1.0;

    private ZoomController() {}

    public static ZoomController getInstance() {
        return INSTANCE;
    }

    public synchronized boolean isActive() {
        return active;
    }

    public synchronized double getCurrentZoomFactor() {
        return currentZoomFactor;
    }

    public synchronized double getTargetZoomFactor() {
        return targetZoomFactor;
    }

    public synchronized void resetZoom() {
        resetZoomLevel();
    }

    public synchronized double getUserConfiguredZoom() {
        return userConfiguredZoom;
    }

    public synchronized void setUserConfiguredZoom(double zoom) {
        ZoomModule mod = ZoomModule.getInstance();
        double min = mod != null ? mod.getMinZoomSetting().getValue() : 1.2;
        double max = mod != null ? mod.getMaxZoomSetting().getValue() : 15.0;
        this.userConfiguredZoom = Math.max(min, Math.min(max, zoom));
        if (active) {
            startAnimation(this.userConfiguredZoom);
        }
    }

    /**
     * Handles keyboard press for Zoom activation according to the selected mode.
     */
    public synchronized void onKeyPressed() {
        ZoomModule mod = ZoomModule.getInstance();
        if (mod == null || !mod.isEnabled()) return;

        keyHeld = true;
        keyPressTime = System.currentTimeMillis();
        ZoomActivationMode mode = mod.getActivationModeSetting().getValue();

        switch (mode) {
            case HOLD -> setActive(true);
            case TOGGLE, TOGGLE_HOLD -> setActive(!active);
        }
    }

    /**
     * Handles keyboard release for Zoom deactivation.
     */
    public synchronized void onKeyReleased() {
        ZoomModule mod = ZoomModule.getInstance();
        keyHeld = false;
        if (mod == null || !mod.isEnabled()) {
            setActive(false);
            return;
        }

        ZoomActivationMode mode = mod.getActivationModeSetting().getValue();
        switch (mode) {
            case HOLD -> setActive(false);
            case TOGGLE -> {
                // Remains active until next toggle press
            }
            case TOGGLE_HOLD -> {
                long duration = System.currentTimeMillis() - keyPressTime;
                // If held for longer than 250ms, act as hold and deactivate on release
                if (duration > 250L) {
                    setActive(false);
                }
            }
        }
    }

    /**
     * Modifies current zoom level using mouse wheel input while zoom is engaged.
     */
    public synchronized boolean onMouseScroll(double verticalScroll) {
        ZoomModule mod = ZoomModule.getInstance();
        if (mod == null || !mod.isEnabled() || !active) return false;
        if (!mod.getScrollZoomSetting().getValue()) return false;
        if (Math.abs(verticalScroll) < 0.001) return false;

        double sensitivity = mod.getScrollSensitivitySetting().getValue();
        double step = mod.getScrollStepSetting().getValue();
        double min = mod.getMinZoomSetting().getValue();
        double max = mod.getMaxZoomSetting().getValue();

        double delta = verticalScroll * step * sensitivity;
        this.userConfiguredZoom = Math.max(min, Math.min(max, userConfiguredZoom + delta));
        startAnimation(this.userConfiguredZoom);
        return true;
    }

    /**
     * Resets magnification back to module default.
     */
    public synchronized void resetZoomLevel() {
        ZoomModule mod = ZoomModule.getInstance();
        double def = mod != null ? mod.getDefaultZoomSetting().getValue() : 4.0;
        this.userConfiguredZoom = def;
        if (active) {
            startAnimation(def);
        }
    }

    public synchronized void setActive(boolean active) {
        if (this.active == active) return;
        this.active = active;

        ZoomModule mod = ZoomModule.getInstance();
        if (active) {
            if (mod != null && mod.getDefaultZoomSetting() != null) {
                double def = mod.getDefaultZoomSetting().getValue();
                if (Math.abs(userConfiguredZoom - 4.0) < 0.0001 && Math.abs(def - 4.0) > 0.0001) {
                    this.userConfiguredZoom = def;
                }
            }
            double target = (mod != null) ? userConfiguredZoom : 4.0;
            startAnimation(target);
        } else {
            startAnimation(1.0);
        }
    }

    private void startAnimation(double target) {
        ZoomModule mod = ZoomModule.getInstance();
        this.targetZoomFactor = target;
        this.animationStartFactor = this.currentZoomFactor;
        this.animationStartTime = System.currentTimeMillis();

        if (mod != null && mod.getInstantZoomSetting().getValue()) {
            this.currentZoomFactor = target;
        }
    }

    /**
     * Updates interpolation towards target zoom factor. Called each client render tick.
     */
    public synchronized void updateAnimation() {
        ZoomModule mod = ZoomModule.getInstance();
        if (mod != null && mod.getInstantZoomSetting().getValue()) {
            this.currentZoomFactor = targetZoomFactor;
            return;
        }

        double duration = mod != null ? mod.getTransitionDurationSetting().getValue() : 150.0;
        if (duration <= 0.0) {
            this.currentZoomFactor = targetZoomFactor;
            return;
        }

        long elapsed = System.currentTimeMillis() - animationStartTime;
        double progress = Math.min(1.0, elapsed / duration);
        ZoomEasing easing = mod != null ? mod.getEasingSetting().getValue() : ZoomEasing.EASE_OUT;
        double eased = easing.ease(progress);

        this.currentZoomFactor = animationStartFactor + (targetZoomFactor - animationStartFactor) * eased;

        if (progress >= 1.0) {
            this.currentZoomFactor = targetZoomFactor;
        }
    }

    /**
     * Called when window focus is lost.
     */
    public synchronized void onFocusLost() {
        ZoomModule mod = ZoomModule.getInstance();
        if (mod != null && mod.getResetOnFocusLostSetting().getValue()) {
            if (active && mod.getActivationModeSetting().getValue() != ZoomActivationMode.TOGGLE) {
                setActive(false);
            }
        }
        keyHeld = false;
    }

    /**
     * Called upon world change, server disconnect, or dimension transfer.
     */
    public synchronized void onWorldChanged() {
        ZoomModule mod = ZoomModule.getInstance();
        if (mod != null && mod.getResetOnWorldChangeSetting().getValue()) {
            this.active = false;
            this.keyHeld = false;
            this.currentZoomFactor = 1.0;
            this.targetZoomFactor = 1.0;
            if (mod.getDefaultZoomSetting() != null) {
                this.userConfiguredZoom = mod.getDefaultZoomSetting().getValue();
            }
        }
    }

    /**
     * Called when a GUI or screen is opened.
     */
    public synchronized void onGuiOpened() {
        ZoomModule mod = ZoomModule.getInstance();
        if (mod != null && mod.getDisableInGuiSetting().getValue()) {
            if (active && mod.getActivationModeSetting().getValue() != ZoomActivationMode.TOGGLE) {
                setActive(false);
            }
        }
        keyHeld = false;
    }

    public synchronized void resetForTesting() {
        ZoomModule mod = ZoomModule.getInstance();
        this.active = false;
        this.keyHeld = false;
        this.userConfiguredZoom = (mod != null && mod.getDefaultZoomSetting() != null) ? mod.getDefaultZoomSetting().getValue() : 4.0;
        this.currentZoomFactor = 1.0;
        this.targetZoomFactor = 1.0;
        this.animationStartTime = 0L;
    }
}
