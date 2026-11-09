package pl.zombek.client.modules;

/**
 * Contract for client-side mod modules (e.g. gameplay enhancements, utilities).
 * Implementation planned for subsequent modular architecture stages.
 */
public interface IClientModule {
    String getName();
    String getDescription();
    boolean isEnabled();
    void setEnabled(boolean enabled);
    void onEnable();
    void onDisable();
}
