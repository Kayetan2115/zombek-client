package pl.zombek.client.core.info;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Thread-safe registry holding the active IInfoHudDataProvider.
 */
public class InfoHudDataProviderRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(InfoHudDataProviderRegistry.class);
    private static final InfoHudDataProviderRegistry INSTANCE = new InfoHudDataProviderRegistry();

    private final AtomicReference<IInfoHudDataProvider> activeProvider = new AtomicReference<>(new FallbackInfoDataProvider());

    private InfoHudDataProviderRegistry() {}

    public static InfoHudDataProviderRegistry getInstance() {
        return INSTANCE;
    }

    public void register(IInfoHudDataProvider provider) {
        Objects.requireNonNull(provider, "Provider cannot be null");
        activeProvider.set(provider);
        LOGGER.info("[InfoHUD] Registered info data provider: {}", provider.getClass().getSimpleName());
    }

    public IInfoHudDataProvider getProvider() {
        return activeProvider.get();
    }

    public void resetToDefault() {
        activeProvider.set(new FallbackInfoDataProvider());
    }

    /**
     * Default headless fallback provider.
     */
    public static class FallbackInfoDataProvider implements IInfoHudDataProvider {
        private double x = 0.0;
        private double y = 64.0;
        private double z = 0.0;
        private float yaw = 0.0f;
        private float pitch = 0.0f;
        private String dimension = "minecraft:overworld";
        private int fps = 60;
        private int ping = -1;
        private boolean singleplayer = true;
        private boolean inGame = true;

        public void setPosition(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public void setRotation(float yaw, float pitch) {
            this.yaw = yaw;
            this.pitch = pitch;
        }

        public void setDimension(String dimension) {
            this.dimension = dimension;
        }

        public void setFps(int fps) {
            this.fps = fps;
        }

        public void setPing(int ping) {
            this.ping = ping;
        }

        public void setSingleplayer(boolean singleplayer) {
            this.singleplayer = singleplayer;
        }

        public void setInGame(boolean inGame) {
            this.inGame = inGame;
        }

        @Override public boolean isPlayerInGame() { return inGame; }
        @Override public double getPlayerX() { return x; }
        @Override public double getPlayerY() { return y; }
        @Override public double getPlayerZ() { return z; }
        @Override public float getPlayerYaw() { return yaw; }
        @Override public float getPlayerPitch() { return pitch; }
        @Override public String getDimensionId() { return dimension; }
        @Override public int getCurrentFps() { return fps; }
        @Override public int getPingMs() { return ping; }
        @Override public boolean isSingleplayer() { return singleplayer; }
    }
}
