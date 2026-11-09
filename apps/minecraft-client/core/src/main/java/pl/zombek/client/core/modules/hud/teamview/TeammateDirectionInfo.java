package pl.zombek.client.core.modules.hud.teamview;

import java.util.UUID;

/**
 * Directional metadata for a teammate, ready for integration with future Direction HUD.
 */
public record TeammateDirectionInfo(
        UUID uuid,
        String name,
        int teamColorArgb,
        double distanceBlocks,
        double relativeAngleDegrees
) {}
