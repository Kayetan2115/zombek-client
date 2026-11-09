package pl.zombek.client.core.camera;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.zombek.client.core.modules.visual.FreelookCameraController;
import pl.zombek.client.core.modules.pvp.snaplook.SnaplookController;

/**
 * Unified camera manager coordinating all camera transformation layers:
 * Zoom, FOV Changer, Motion Blur, Hurt Cam, alongside Freelook and Snaplook.
 * Enforces strict priority hierarchy, state isolation, and lifecycle restoration.
 */
public class ZombekCameraController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombekCameraController.class);
    private static final ZombekCameraController INSTANCE = new ZombekCameraController();

    private final ZoomController zoomController = ZoomController.getInstance();
    private final CameraFovController fovController = CameraFovController.getInstance();
    private final MotionBlurController motionBlurController = MotionBlurController.getInstance();
    private final HurtCamController hurtCamController = HurtCamController.getInstance();

    private ZombekCameraController() {}

    public static ZombekCameraController getInstance() {
        return INSTANCE;
    }

    public ZoomController getZoomController() {
        return zoomController;
    }

    public CameraFovController getFovController() {
        return fovController;
    }

    public MotionBlurController getMotionBlurController() {
        return motionBlurController;
    }

    public HurtCamController getHurtCamController() {
        return hurtCamController;
    }

    /**
     * Resolves the current composite camera state.
     */
    public synchronized CameraState getCameraState(double vanillaFov, float vanillaYaw, float vanillaPitch, int perspective) {
        double effectiveFov = fovController.computeFov(vanillaFov, 1.0f, false, false, false);
        boolean zoomActive = zoomController.isActive();
        double zoomFactor = zoomController.getCurrentZoomFactor();

        FreelookCameraController freelook = FreelookCameraController.getInstance();
        boolean flActive = freelook.isControllingCamera();
        float flYaw = freelook.getCameraYaw();
        float flPitch = freelook.getCameraPitch();

        SnaplookController snaplook = SnaplookController.getInstance();
        boolean slActive = snaplook.isActive();

        boolean hurtCamActive = !hurtCamController.shouldCancelHurtCam();
        float hurtCamTilt = hurtCamController.getHurtTiltMultiplier();

        boolean mbActive = motionBlurController.getCurrentBlurAlpha() > 0.005f;
        float mbAlpha = motionBlurController.getCurrentBlurAlpha();

        return new CameraState(
                vanillaFov, vanillaYaw, vanillaPitch, perspective,
                effectiveFov, zoomActive, zoomFactor,
                flActive, flYaw, flPitch,
                slActive, hurtCamActive, hurtCamTilt,
                mbActive, mbAlpha
        );
    }

    /**
     * Called on each client game tick.
     */
    public synchronized void onClientTick() {
        zoomController.updateAnimation();
    }

    /**
     * Called on each render frame before world or post-process rendering.
     */
    public synchronized void onRenderTick(float cameraYaw, float cameraPitch, float tickDelta) {
        motionBlurController.updateCameraRotation(cameraYaw, cameraPitch);
    }

    /**
     * Called when the Minecraft window loses focus.
     */
    public synchronized void onFocusLost() {
        zoomController.onFocusLost();
    }

    /**
     * Called when changing worlds, dimensions, or disconnecting.
     */
    public synchronized void onWorldChanged() {
        zoomController.onWorldChanged();
        motionBlurController.onWorldChanged();
        hurtCamController.resetForTesting();
    }

    /**
     * Called when a GUI or menu screen is opened.
     */
    public synchronized void onGuiOpened(boolean isHudEditor) {
        zoomController.onGuiOpened();
        if (isHudEditor) {
            motionBlurController.resetForTesting();
        }
    }

    /**
     * Called upon player death or respawn.
     */
    public synchronized void onPlayerRespawn() {
        zoomController.setActive(false);
        motionBlurController.onWorldChanged();
        hurtCamController.resetForTesting();
    }

    public synchronized void resetAll() {
        resetForTesting();
    }

    public synchronized void resetForTesting() {
        zoomController.resetForTesting();
        fovController.resetForTesting();
        motionBlurController.resetForTesting();
        hurtCamController.resetForTesting();
    }
}
