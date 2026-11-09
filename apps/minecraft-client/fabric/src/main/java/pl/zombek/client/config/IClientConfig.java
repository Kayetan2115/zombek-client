package pl.zombek.client.config;

/**
 * Contract for client-side configuration managers.
 * Implementation planned for subsequent configuration synchronization stages.
 */
public interface IClientConfig {
    void load();
    void save();
    String getConfigName();
}
