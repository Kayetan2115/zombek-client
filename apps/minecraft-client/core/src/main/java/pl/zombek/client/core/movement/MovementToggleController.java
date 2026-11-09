package pl.zombek.client.core.movement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central controller coordinating Toggle Sprint, Toggle Sneak, physical user input,
 * and vanilla Minecraft movement constraints.
 * Thread-safe singleton.
 */
public class MovementToggleController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MovementToggleController.class);
    private static final MovementToggleController INSTANCE = new MovementToggleController();

    // Toggle Sprint states
    private boolean sprintToggled = false;
    private boolean sprintKeyHeld = false;
    private boolean sprintKeyWasDown = false;
    private boolean physicalSprintDown = false;

    // Toggle Sneak states
    private boolean sneakToggled = false;
    private boolean sneakKeyHeld = false;
    private boolean sneakKeyWasDown = false;
    private boolean physicalSneakDown = false;

    // Game conditions
    private boolean movingForward = false;
    private boolean canSprint = true;
    private boolean canSneak = true;
    private boolean usingItem = false;
    private boolean flying = false;
    private boolean riding = false;

    private MovementToggleController() {}

    public static MovementToggleController getInstance() {
        return INSTANCE;
    }

    // ==========================================
    // KEY INPUT HANDLERS
    // ==========================================

    public synchronized void onSprintKeyInput(boolean isDown, SprintActivationMode mode) {
        if (mode == SprintActivationMode.TOGGLE) {
            if (isDown && !sprintKeyWasDown) {
                this.sprintToggled = !this.sprintToggled;
                LOGGER.debug("Toggle Sprint toggled: {}", this.sprintToggled);
            }
        } else if (mode == SprintActivationMode.HOLD) {
            this.sprintKeyHeld = isDown;
        }
        this.sprintKeyWasDown = isDown;
    }

    public synchronized void onSneakKeyInput(boolean isDown, SneakActivationMode mode) {
        if (mode == SneakActivationMode.TOGGLE) {
            if (isDown && !sneakKeyWasDown) {
                this.sneakToggled = !this.sneakToggled;
                LOGGER.debug("Toggle Sneak toggled: {}", this.sneakToggled);
            }
        } else if (mode == SneakActivationMode.HOLD) {
            this.sneakKeyHeld = isDown;
        } else if (mode == SneakActivationMode.TOGGLE_AND_HOLD) {
            if (isDown && !sneakKeyWasDown) {
                this.sneakToggled = !this.sneakToggled;
                LOGGER.debug("Toggle Sneak toggled (TOGGLE_AND_HOLD): {}", this.sneakToggled);
            }
            this.sneakKeyHeld = isDown;
        }
        this.sneakKeyWasDown = isDown;
    }

    public synchronized void setPhysicalSprintDown(boolean down) {
        this.physicalSprintDown = down;
    }

    public synchronized void setPhysicalSneakDown(boolean down) {
        this.physicalSneakDown = down;
    }

    // ==========================================
    // GAME CONDITIONS UPDATE
    // ==========================================

    public synchronized void updateGameConditions(boolean movingForward, boolean canSprint, boolean canSneak,
                                                   boolean usingItem, boolean flying, boolean riding) {
        this.movingForward = movingForward;
        this.canSprint = canSprint;
        this.canSneak = canSneak;
        this.usingItem = usingItem;
        this.flying = flying;
        this.riding = riding;
    }

    // ==========================================
    // EVALUATION
    // ==========================================

    public synchronized boolean isSprintActive(boolean sprintModuleEnabled,
                                              SprintActivationMode mode,
                                              boolean alwaysSprint,
                                              boolean preserveVanilla,
                                              boolean disableWhileSneaking,
                                              boolean disableWhileUsingItems) {
        if (sprintModuleEnabled) {
            if (disableWhileSneaking && isSneakEffective()) {
                return false;
            }
            if (disableWhileUsingItems && this.usingItem) {
                return false;
            }
            if (!this.canSprint) {
                return false;
            }

            if (alwaysSprint && this.movingForward) {
                return true;
            }
            if (mode == SprintActivationMode.TOGGLE && this.sprintToggled && this.movingForward) {
                return true;
            }
            if (mode == SprintActivationMode.HOLD && this.sprintKeyHeld && this.movingForward) {
                return true;
            }
        }

        // Vanilla physical fallback
        if (preserveVanilla && this.physicalSprintDown && this.canSprint && this.movingForward) {
            return true;
        }

        return false;
    }

    public synchronized boolean isSneakActive(boolean sneakModuleEnabled,
                                             SneakActivationMode mode,
                                             boolean preserveVanilla,
                                             boolean disableWhileFlying,
                                             boolean disableWhileRiding) {
        if (sneakModuleEnabled) {
            if (disableWhileFlying && this.flying) {
                return false;
            }
            if (disableWhileRiding && this.riding) {
                return false;
            }
            if (!this.canSneak) {
                return false;
            }

            if (mode == SneakActivationMode.TOGGLE && this.sneakToggled) {
                return true;
            }
            if (mode == SneakActivationMode.HOLD && this.sneakKeyHeld) {
                return true;
            }
            if (mode == SneakActivationMode.TOGGLE_AND_HOLD) {
                if (this.sneakToggled || this.sneakKeyHeld || (preserveVanilla && this.physicalSneakDown)) {
                    return true;
                }
            }
        }

        // Vanilla physical fallback
        if (preserveVanilla && this.physicalSneakDown && (!disableWhileFlying || !this.flying) && (!disableWhileRiding || !this.riding)) {
            return true;
        }

        return false;
    }

    private synchronized boolean isSneakEffective() {
        return this.sneakToggled || this.sneakKeyHeld || this.physicalSneakDown;
    }

    // ==========================================
    // HUD STATUS TEXT GENERATION
    // ==========================================

    public synchronized String getSprintStatusText(MovementIndicatorStyle style,
                                                   boolean sprintModuleEnabled,
                                                   SprintActivationMode mode,
                                                   boolean alwaysSprint) {
        if (!sprintModuleEnabled) {
            return formatIndicator("Sprint", "Disabled", style, false);
        }

        if (alwaysSprint) {
            return formatIndicator("Sprint", "Always", style, this.movingForward && this.canSprint);
        }

        if (mode == SprintActivationMode.TOGGLE) {
            if (this.sprintToggled) {
                return formatIndicator("Sprint", "Toggled", style, this.movingForward && this.canSprint);
            }
        } else if (mode == SprintActivationMode.HOLD) {
            if (this.sprintKeyHeld) {
                return formatIndicator("Sprint", "Key Held", style, this.movingForward && this.canSprint);
            }
        }

        if (this.physicalSprintDown) {
            return formatIndicator("Sprint", "Vanilla", style, this.movingForward && this.canSprint);
        }

        return formatIndicator("Sprint", "Off", style, false);
    }

    public synchronized String getSneakStatusText(MovementIndicatorStyle style,
                                                  boolean sneakModuleEnabled,
                                                  SneakActivationMode mode) {
        if (!sneakModuleEnabled) {
            return formatIndicator("Sneak", "Disabled", style, false);
        }

        if (this.physicalSneakDown) {
            return formatIndicator("Sneak", "Key Held", style, true);
        }

        if (mode == SneakActivationMode.TOGGLE || mode == SneakActivationMode.TOGGLE_AND_HOLD) {
            if (this.sneakToggled) {
                return formatIndicator("Sneak", "Toggled", style, true);
            }
        } else if (mode == SneakActivationMode.HOLD) {
            if (this.sneakKeyHeld) {
                return formatIndicator("Sneak", "Key Held", style, true);
            }
        }

        return formatIndicator("Sneak", "Off", style, false);
    }

    private static String formatIndicator(String action, String status, MovementIndicatorStyle style, boolean active) {
        return switch (style) {
            case BRACKETED -> "[" + action + " (" + status + ")]";
            case DETAILED -> action + ": " + status;
            case COMPACT -> active ? action : action + " (Off)";
            case TEXT_ONLY -> action.toUpperCase();
        };
    }

    // ==========================================
    // RESETS & SAFETY GUARDS
    // ==========================================

    public synchronized void resetSprint() {
        this.sprintToggled = false;
        this.sprintKeyHeld = false;
        this.sprintKeyWasDown = false;
        this.physicalSprintDown = false;
    }

    public synchronized void resetSneak() {
        this.sneakToggled = false;
        this.sneakKeyHeld = false;
        this.sneakKeyWasDown = false;
        this.physicalSneakDown = false;
    }

    public synchronized void resetAll() {
        resetSprint();
        resetSneak();
        this.movingForward = false;
        this.usingItem = false;
        this.flying = false;
        this.riding = false;
    }

    public synchronized void onWorldChange(boolean resetSprint, boolean resetSneak) {
        if (resetSprint) resetSprint();
        if (resetSneak) resetSneak();
    }

    public synchronized void onWorldChange() {
        onWorldChange(true, true);
    }

    public synchronized void onPlayerDeath(boolean resetSprint, boolean resetSneak) {
        if (resetSprint) resetSprint();
        if (resetSneak) resetSneak();
    }

    public synchronized void onPlayerDeath() {
        onPlayerDeath(true, true);
    }

    public synchronized void onPlayerRespawn(boolean resetSprint, boolean resetSneak) {
        if (resetSprint) resetSprint();
        if (resetSneak) resetSneak();
    }

    public synchronized void onPlayerRespawn() {
        onPlayerRespawn(true, true);
    }

    public synchronized void onDeathOrRespawn() {
        onPlayerDeath(true, true);
    }

    public synchronized void onFocusLost(boolean resetSprint, boolean resetSneak) {
        if (resetSprint) {
            this.sprintKeyHeld = false;
            this.physicalSprintDown = false;
        }
        if (resetSneak) {
            this.sneakKeyHeld = false;
            this.physicalSneakDown = false;
        }
    }

    public synchronized void onFocusLost() {
        onFocusLost(true, true);
    }

    public synchronized void onMenuOpen(boolean disableSprintInMenus, boolean disableSneakInMenus) {
        if (disableSprintInMenus) {
            this.sprintKeyHeld = false;
            this.physicalSprintDown = false;
        }
        if (disableSneakInMenus) {
            this.sneakKeyHeld = false;
            this.physicalSneakDown = false;
        }
    }

    // Getters and Setters for state
    public synchronized boolean isSprintToggled() {
        return sprintToggled;
    }

    public synchronized void setSprintToggled(boolean sprintToggled) {
        this.sprintToggled = sprintToggled;
    }

    public synchronized boolean isSprintKeyHeld() {
        return sprintKeyHeld;
    }

    public synchronized boolean isPhysicalSprintDown() {
        return physicalSprintDown;
    }

    public synchronized boolean isSneakToggled() {
        return sneakToggled;
    }

    public synchronized void setSneakToggled(boolean sneakToggled) {
        this.sneakToggled = sneakToggled;
    }

    public synchronized boolean isSneakKeyHeld() {
        return sneakKeyHeld;
    }

    public synchronized boolean isPhysicalSneakDown() {
        return physicalSneakDown;
    }

    public synchronized boolean isMovingForward() {
        return movingForward;
    }

    public synchronized boolean isCanSprint() {
        return canSprint;
    }

    public synchronized boolean isCanSneak() {
        return canSneak;
    }
}
