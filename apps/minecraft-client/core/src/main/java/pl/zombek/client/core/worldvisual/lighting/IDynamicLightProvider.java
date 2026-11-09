package pl.zombek.client.core.worldvisual.lighting;

import java.util.List;

/**
 * Platform provider interface feeding dynamic light candidate sources into the manager.
 */
public interface IDynamicLightProvider {

    /**
     * Collects all candidate dynamic light sources in range of the player/camera.
     */
    List<DynamicLightSource> collectCandidateSources(
            double playerX, double playerY, double playerZ,
            boolean includeMainHand, boolean includeOffhand,
            boolean includeDroppedItems, boolean includeEntities
    );
}
