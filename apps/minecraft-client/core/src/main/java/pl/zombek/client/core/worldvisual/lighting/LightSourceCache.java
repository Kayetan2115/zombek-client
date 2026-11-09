package pl.zombek.client.core.worldvisual.lighting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * High-performance spatial cache of active dynamic light sources.
 * Prevents allocating memory during render frames and sorts sources by proximity.
 */
public class LightSourceCache {
    private final List<DynamicLightSource> activeSources = new CopyOnWriteArrayList<>();
    private int maxActiveSources = 12;
    private double maxDistanceSquared = 24.0 * 24.0;

    public LightSourceCache() {}

    public void configure(int maxActiveSources, double maxDistance) {
        this.maxActiveSources = Math.max(1, Math.min(64, maxActiveSources));
        this.maxDistanceSquared = maxDistance * maxDistance;
    }

    /**
     * Updates active sources with proximity sorting and capacity clamping.
     */
    public void updateSources(List<DynamicLightSource> candidateSources, double cameraX, double cameraY, double cameraZ) {
        if (candidateSources == null || candidateSources.isEmpty()) {
            activeSources.clear();
            return;
        }

        List<DynamicLightSource> inRange = new ArrayList<>(candidateSources.size());
        for (DynamicLightSource s : candidateSources) {
            if (s != null && s.isActive() && s.getEffectiveLightLevel() > 0) {
                if (s.distanceSquared(cameraX, cameraY, cameraZ) <= maxDistanceSquared) {
                    inRange.add(s);
                }
            }
        }

        // Sort by distance ascending so closest sources take priority
        inRange.sort(Comparator.comparingDouble(s -> s.distanceSquared(cameraX, cameraY, cameraZ)));

        activeSources.clear();
        int count = Math.min(inRange.size(), maxActiveSources);
        for (int i = 0; i < count; i++) {
            activeSources.add(inRange.get(i));
        }
    }

    public List<DynamicLightSource> getActiveSources() {
        return activeSources;
    }

    public void clear() {
        activeSources.clear();
    }

    public int size() {
        return activeSources.size();
    }
}
