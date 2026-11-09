package pl.zombek.client.core.modules.hud.teamview;

import java.util.UUID;

/**
 * Snapshot of client-side teammate entity information.
 */
public record TeammateData(
        UUID uuid,
        String name,
        String teamName,
        int teamColorArgb,
        double x,
        double y,
        double z,
        double distanceBlocks,
        boolean isVisible
) {}
