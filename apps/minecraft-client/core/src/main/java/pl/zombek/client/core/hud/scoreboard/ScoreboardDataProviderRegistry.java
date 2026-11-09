package pl.zombek.client.core.hud.scoreboard;

import java.util.Objects;

/**
 * Registry holding the active platform provider for Scoreboard data.
 */
public final class ScoreboardDataProviderRegistry {

    private static final ScoreboardDataProviderRegistry INSTANCE = new ScoreboardDataProviderRegistry();

    private IScoreboardDataProvider provider = new FallbackScoreboardDataProvider();

    private ScoreboardDataProviderRegistry() {}

    public static ScoreboardDataProviderRegistry getInstance() {
        return INSTANCE;
    }

    public IScoreboardDataProvider getProvider() {
        return provider;
    }

    public void registerProvider(IScoreboardDataProvider provider) {
        this.provider = Objects.requireNonNull(provider, "Scoreboard data provider cannot be null");
    }

    public void reset() {
        this.provider = new FallbackScoreboardDataProvider();
    }

    public void resetToDefault() {
        reset();
    }
}
