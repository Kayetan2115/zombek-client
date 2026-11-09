package pl.zombek.client.core.modules.visual;

/**
 * Controller managing independent camera rotation for Freelook.
 * Keeps server-bound player rotation unchanged while providing decoupled camera angles.
 */
public class FreelookCameraController {
    private static final FreelookCameraController INSTANCE = new FreelookCameraController();

    private boolean active = false;
    private float originalYaw = 0.0f;
    private float originalPitch = 0.0f;

    private float cameraYaw = 0.0f;
    private float cameraPitch = 0.0f;

    // Snapback animation state
    private boolean inSnapback = false;
    private long snapbackStartTime = 0L;
    private long snapbackDuration = 150L;
    private float snapbackFromYaw = 0.0f;
    private float snapbackFromPitch = 0.0f;
    private float snapbackTargetYaw = 0.0f;
    private float snapbackTargetPitch = 0.0f;
    private FreelookInterpolation snapbackInterp = FreelookInterpolation.EASE_OUT;

    private FreelookCameraController() {}

    public static FreelookCameraController getInstance() {
        return INSTANCE;
    }

    /**
     * Starts freelook decoupling from current player orientation.
     */
    public synchronized void startFreelook(float playerYaw, float playerPitch) {
        this.active = true;
        this.inSnapback = false;
        this.originalYaw = playerYaw;
        this.originalPitch = playerPitch;
        this.cameraYaw = playerYaw;
        this.cameraPitch = playerPitch;
    }

    /**
     * Stops freelook and optionally triggers snapback transition.
     */
    public synchronized void stopFreelook(boolean useSnapback, long durationMs, FreelookInterpolation interp,
                                          float currentBodyYaw, float currentBodyPitch) {
        this.active = false;
        if (useSnapback && durationMs > 0) {
            this.inSnapback = true;
            this.snapbackStartTime = System.currentTimeMillis();
            this.snapbackDuration = durationMs;
            this.snapbackFromYaw = this.cameraYaw;
            this.snapbackFromPitch = this.cameraPitch;
            this.snapbackTargetYaw = currentBodyYaw;
            this.snapbackTargetPitch = currentBodyPitch;
            this.snapbackInterp = (interp != null) ? interp : FreelookInterpolation.EASE_OUT;
        } else {
            this.inSnapback = false;
            this.cameraYaw = currentBodyYaw;
            this.cameraPitch = currentBodyPitch;
        }
    }

    /**
     * Applies mouse movement delta to the decoupled camera angles.
     */
    public synchronized void handleMouseInput(float deltaYaw, float deltaPitch,
                                             float hSens, float vSens,
                                             boolean invertH, boolean invertV,
                                             float maxPitch, float maxYaw) {
        if (!this.active) {
            return;
        }
        if (this.inSnapback) {
            this.inSnapback = false;
        }

        float effectivePitchLimit = Math.max(10.0f, Math.min(90.0f, maxPitch));

        float dYaw = deltaYaw * hSens * (invertH ? -1.0f : 1.0f);
        float dPitch = deltaPitch * vSens * (invertV ? -1.0f : 1.0f);

        this.cameraYaw += dYaw;
        this.cameraPitch += dPitch;

        // Clamp pitch (-90 to +90 degrees)
        if (this.cameraPitch > effectivePitchLimit) {
            this.cameraPitch = effectivePitchLimit;
        } else if (this.cameraPitch < -effectivePitchLimit) {
            this.cameraPitch = -effectivePitchLimit;
        }

        // Optional relative yaw clamping
        if (maxYaw > 0.0f && maxYaw < 360.0f) {
            float diff = normalizeAngle(this.cameraYaw - this.originalYaw);
            if (diff > maxYaw) {
                this.cameraYaw = this.originalYaw + maxYaw;
            } else if (diff < -maxYaw) {
                this.cameraYaw = this.originalYaw - maxYaw;
            }
        }
    }

    /**
     * Progresses snapback animation towards player's current body angle.
     */
    public synchronized void updateSnapback(long currentTime, float currentBodyYaw, float currentBodyPitch) {
        if (!this.inSnapback) {
            return;
        }

        long elapsed = currentTime - this.snapbackStartTime;
        if (elapsed >= this.snapbackDuration || this.snapbackDuration <= 0) {
            this.inSnapback = false;
            this.cameraYaw = currentBodyYaw;
            this.cameraPitch = currentBodyPitch;
            return;
        }

        float t = Math.max(0.0f, Math.min(1.0f, (float) elapsed / (float) this.snapbackDuration));
        float p;
        switch (this.snapbackInterp) {
            case LINEAR:
                p = t;
                break;
            case SMOOTH_STEP:
                p = t * t * (3.0f - 2.0f * t);
                break;
            case EASE_OUT:
            default:
                float inv = 1.0f - t;
                p = 1.0f - inv * inv;
                break;
        }

        float diffYaw = normalizeAngle(currentBodyYaw - this.snapbackFromYaw);
        this.cameraYaw = this.snapbackFromYaw + diffYaw * p;
        this.cameraPitch = this.snapbackFromPitch + (currentBodyPitch - this.snapbackFromPitch) * p;
    }

    /**
     * Resets freelook state immediately (on world unload, death, focus lost).
     */
    public synchronized void reset() {
        this.active = false;
        this.inSnapback = false;
    }

    private static float normalizeAngle(float angle) {
        float a = angle % 360.0f;
        if (a >= 180.0f) {
            a -= 360.0f;
        } else if (a < -180.0f) {
            a += 360.0f;
        }
        return a;
    }

    public synchronized boolean isActive() {
        return this.active;
    }

    public synchronized boolean isInSnapback() {
        return this.inSnapback;
    }

    public synchronized boolean isControllingCamera() {
        return this.active || this.inSnapback;
    }

    public synchronized float getCameraYaw() {
        return this.cameraYaw;
    }

    public synchronized float getCameraPitch() {
        return this.cameraPitch;
    }

    public synchronized float getOriginalYaw() {
        return this.originalYaw;
    }

    public synchronized float getOriginalPitch() {
        return this.originalPitch;
    }

    public synchronized void setCameraAngles(float yaw, float pitch) {
        this.cameraYaw = yaw;
        this.cameraPitch = pitch;
    }
}
