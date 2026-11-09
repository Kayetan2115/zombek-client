package pl.zombek.client.core.modules.pvp.snaplook;

/**
 * Controller coordinating Snaplook camera transitions, conflict resolution with F5,
 * and lifecycle resets.
 */
public class SnaplookController {
    private static final SnaplookController INSTANCE = new SnaplookController();

    private ISnaplookPerspectiveAdapter adapter;
    private boolean active = false;
    private SnaplookPerspective previousPerspective = SnaplookPerspective.FIRST_PERSON;
    private long activationStartTime = 0L;

    private SnaplookController() {}

    public static SnaplookController getInstance() {
        return INSTANCE;
    }

    public synchronized void setAdapter(ISnaplookPerspectiveAdapter adapter) {
        this.adapter = adapter;
    }

    public synchronized boolean isActive() {
        return active;
    }

    public synchronized SnaplookPerspective getPreviousPerspective() {
        return previousPerspective;
    }

    /**
     * Called when the Snaplook key is pressed.
     */
    public synchronized void onKeyPressed() {
        SnaplookModule module = SnaplookModule.getInstance();
        if (module == null || !module.isEnabled()) return;
        if (adapter == null) return;

        SnaplookActivationMode mode = module.getActivationModeSetting().getValue();
        SnaplookPerspective target = module.getTargetPerspectiveSetting().getValue();

        switch (mode) {
            case HOLD -> {
                if (!active) {
                    this.previousPerspective = adapter.getPerspective();
                    this.active = true;
                    this.activationStartTime = System.currentTimeMillis();
                    adapter.setPerspective(target);
                }
            }
            case TOGGLE -> {
                if (active) {
                    deactivate();
                } else {
                    this.previousPerspective = adapter.getPerspective();
                    this.active = true;
                    this.activationStartTime = System.currentTimeMillis();
                    adapter.setPerspective(target);
                }
            }
            case TAP -> {
                if (!active) {
                    this.previousPerspective = adapter.getPerspective();
                    this.active = true;
                    this.activationStartTime = System.currentTimeMillis();
                    adapter.setPerspective(target);
                } else {
                    deactivate();
                }
            }
        }
    }

    /**
     * Called when the Snaplook key is released.
     */
    public synchronized void onKeyReleased() {
        SnaplookModule module = SnaplookModule.getInstance();
        if (module == null || !module.isEnabled()) return;

        SnaplookActivationMode mode = module.getActivationModeSetting().getValue();
        if (mode == SnaplookActivationMode.HOLD && active) {
            deactivate();
        }
    }

    /**
     * Handles manual perspective change (e.g. user pressed standard F5 key).
     */
    public synchronized void onManualPerspectiveChanged(SnaplookPerspective newPerspective) {
        SnaplookModule module = SnaplookModule.getInstance();
        if (module != null && module.getRespectManualChangesSetting().getValue() && active) {
            // User intentionally pressed F5 during Snaplook - respect choice and resolve conflict
            this.previousPerspective = newPerspective;
            this.active = false;
        }
    }

    /**
     * Deactivates Snaplook and restores previous camera perspective.
     */
    public synchronized void deactivate() {
        if (!active) return;
        this.active = false;
        if (adapter != null) {
            SnaplookModule module = SnaplookModule.getInstance();
            if (module == null || module.getRestorePreviousPerspectiveSetting().getValue()) {
                adapter.setPerspective(previousPerspective);
            }
        }
    }

    /**
     * Resets Snaplook state without throwing exceptions.
     */
    public synchronized void reset() {
        if (active && adapter != null) {
            adapter.setPerspective(previousPerspective);
        }
        this.active = false;
    }

    public synchronized void onWorldChange() {
        SnaplookModule module = SnaplookModule.getInstance();
        if (module == null || module.getResetOnWorldChangeSetting().getValue()) {
            reset();
        }
    }

    public synchronized void onPlayerDeath() {
        SnaplookModule module = SnaplookModule.getInstance();
        if (module == null || module.getResetOnDeathSetting().getValue()) {
            reset();
        }
    }

    public synchronized void onRespawn() {
        SnaplookModule module = SnaplookModule.getInstance();
        if (module == null || module.getResetOnRespawnSetting().getValue()) {
            reset();
        }
    }

    public synchronized void onFocusLost() {
        SnaplookModule module = SnaplookModule.getInstance();
        if (module == null || module.getResetOnFocusLostSetting().getValue()) {
            reset();
        }
    }

    public synchronized void onGuiOpen() {
        SnaplookModule module = SnaplookModule.getInstance();
        if (module != null && module.getDisableInGuiSetting().getValue()) {
            reset();
        }
    }

    public long getActivationStartTime() {
        return activationStartTime;
    }
}
