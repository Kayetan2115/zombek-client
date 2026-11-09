package pl.zombek.client.core.uhc;

/**
 * Platform provider interface supplying live player status and inventory counts for UHC Overlay.
 */
public interface IUhcDataProvider {
    boolean isPlayerInGame();

    float getPlayerHealth();

    float getPlayerMaxHealth();

    float getPlayerAbsorption();

    int getPlayerFoodLevel();

    float getPlayerSaturation();

    int getItemCount(UhcTrackedItem item);

    default Object getItemStack(UhcTrackedItem item) {
        return null;
    }
}
