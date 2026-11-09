package pl.zombek.client.core.worldvisual.lighting;

import java.util.Collections;
import java.util.List;

/**
 * Central manager orchestrating dynamic ambient light calculations,
 * candidate gathering, and distance culling.
 */
public class DynamicLightManager {
    private static final DynamicLightManager INSTANCE = new DynamicLightManager();

    private final LightSourceCache cache = new LightSourceCache();
    private final DynamicLightRenderer renderer = new DynamicLightRenderer();
    private IDynamicLightProvider platformProvider;

    private boolean enabled = true;
    private int updateIntervalTicks = 2;
    private int currentTick = 0;

    private double lastCameraX = 0;
    private double lastCameraY = 0;
    private double lastCameraZ = 0;

    private DynamicLightManager() {}

    public static DynamicLightManager getInstance() {
        return INSTANCE;
    }

    public void setPlatformProvider(IDynamicLightProvider provider) {
        this.platformProvider = provider;
    }

    public IDynamicLightProvider getPlatformProvider() {
        return platformProvider;
    }

    public LightSourceCache getCache() {
        return cache;
    }

    public DynamicLightRenderer getRenderer() {
        return renderer;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            cache.clear();
        }
    }

    public void setUpdateIntervalTicks(int updateIntervalTicks) {
        this.updateIntervalTicks = Math.max(1, Math.min(20, updateIntervalTicks));
    }

    /**
     * Ticks the manager, collecting candidate sources when update interval expires.
     */
    public void onClientTick(double cameraX, double cameraY, double cameraZ,
                             boolean includeMainHand, boolean includeOffhand,
                             boolean includeDroppedItems, boolean includeEntities) {
        if (!enabled) {
            return;
        }

        this.lastCameraX = cameraX;
        this.lastCameraY = cameraY;
        this.lastCameraZ = cameraZ;

        currentTick++;
        if (currentTick % updateIntervalTicks != 0) {
            return;
        }

        if (platformProvider != null) {
            List<DynamicLightSource> candidates = platformProvider.collectCandidateSources(
                    cameraX, cameraY, cameraZ,
                    includeMainHand, includeOffhand,
                    includeDroppedItems, includeEntities
            );
            cache.updateSources(candidates, cameraX, cameraY, cameraZ);
        }
    }

    /**
     * Obtains the dynamic light level at the specified world position.
     * @return dynamic light contribution between 0 and 15
     */
    public int getDynamicLightLevel(double x, double y, double z) {
        if (!enabled || cache.size() == 0) {
            return 0;
        }
        return renderer.calculateDynamicLight(x, y, z, cache.getActiveSources());
    }

    /**
     * Returns the active cached sources list.
     */
    public List<DynamicLightSource> getActiveSources() {
        if (!enabled) return Collections.emptyList();
        return cache.getActiveSources();
    }

    public void clear() {
        cache.clear();
    }

    public void resetForTesting() {
        cache.clear();
        enabled = true;
        updateIntervalTicks = 2;
        currentTick = 0;
        platformProvider = null;
    }
}
