package pl.zombek.client.core.modules.pvp.tnt;

/**
 * Snapshot of client-side Primed TNT entity state for countdown rendering.
 */
public record TntEntityData(
        int entityId,
        double x,
        double y,
        double z,
        int fuseTicks,
        double distanceBlocks,
        boolean visible
) {}
