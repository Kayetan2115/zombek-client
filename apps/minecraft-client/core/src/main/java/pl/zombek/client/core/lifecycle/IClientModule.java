package pl.zombek.client.core.lifecycle;

/**
 * Universal contract for client modules across all mod loaders.
 */
public interface IClientModule {
    String getName();
    String getDescription();
    boolean isEnabled();
    void setEnabled(boolean enabled);
    void onEnable();
    void onDisable();
}
