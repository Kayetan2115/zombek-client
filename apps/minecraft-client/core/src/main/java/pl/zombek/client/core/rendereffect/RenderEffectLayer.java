package pl.zombek.client.core.rendereffect;

/**
 * Rendering layers affected by client-side visual render effects.
 */
public enum RenderEffectLayer {
    WORLD_FOG("World Fog", 10),
    POST_PROCESSING("Color Post Processing", 20),
    ITEM_RENDERING("Item Glint Rendering", 30),
    PARTICLE_RENDERING("Particle Rendering", 40);

    private final String layerName;
    private final int priority;

    RenderEffectLayer(String layerName, int priority) {
        this.layerName = layerName;
        this.priority = priority;
    }

    public String getLayerName() {
        return layerName;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public String toString() {
        return layerName;
    }
}
