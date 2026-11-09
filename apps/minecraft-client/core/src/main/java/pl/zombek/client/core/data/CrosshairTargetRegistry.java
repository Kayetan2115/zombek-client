package pl.zombek.client.core.data;

import java.util.Objects;

/**
 * Thread-safe registry for the active ICrosshairTargetProvider.
 */
public final class CrosshairTargetRegistry {
    private static final ICrosshairTargetProvider DEFAULT_PROVIDER = new ICrosshairTargetProvider() {
        @Override
        public CrosshairTargetType getTargetType() {
            return CrosshairTargetType.NONE;
        }

        @Override
        public float getAttackCooldownProgress() {
            return 1.0f;
        }

        @Override
        public boolean isSpectator() {
            return false;
        }

        @Override
        public boolean isHudHidden() {
            return false;
        }

        @Override
        public boolean hasOpenScreen() {
            return false;
        }
    };

    private static volatile ICrosshairTargetProvider currentProvider = DEFAULT_PROVIDER;

    private CrosshairTargetRegistry() {
    }

    public static void setProvider(ICrosshairTargetProvider provider) {
        currentProvider = Objects.requireNonNullElse(provider, DEFAULT_PROVIDER);
    }

    public static ICrosshairTargetProvider getProvider() {
        return currentProvider;
    }

    public static void reset() {
        currentProvider = DEFAULT_PROVIDER;
    }
}
