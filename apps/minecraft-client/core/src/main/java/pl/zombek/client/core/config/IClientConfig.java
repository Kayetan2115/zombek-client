package pl.zombek.client.core.config;

/**
 * Common configuration contract across all loaders.
 */
public interface IClientConfig {
    void load();
    void save();
    String getConfigName();
}
