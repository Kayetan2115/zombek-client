package pl.zombek.client.core.data;

/**
 * Provider supplying crosshair targeting data from Minecraft game state.
 */
public interface ICrosshairTargetProvider {
    CrosshairTargetType getTargetType();
    float getAttackCooldownProgress();
    boolean isSpectator();
    boolean isHudHidden();
    boolean hasOpenScreen();
}
