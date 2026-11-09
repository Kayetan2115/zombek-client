package pl.zombek.client.core.modules.visual.nametags;

import pl.zombek.client.core.playerinfo.PlayerInfoData;

/**
 * Strict evaluator for NameTag visibility rules according to Minecraft vanilla standards
 * and ZombekClient Fair Play policies.
 * Never acts as ESP, never permits see-through-walls, never unhides invisibles or sneaking players
 * beyond standard gameplay allowances.
 */
public final class NameTagVisibilityRuleEvaluator {

    private NameTagVisibilityRuleEvaluator() {}

    public static boolean shouldRender(
            PlayerInfoData player,
            NameTagEntityCategory category,
            boolean isSneaking,
            boolean isInvisible,
            boolean isSpectator,
            boolean isGuiOpen,
            double distance,
            boolean targetPlayers,
            boolean targetLiving,
            boolean targetOther,
            boolean respectVanillaVisibility,
            double maxDistance,
            double minDistance,
            boolean hideInGui,
            boolean hideInSpectator,
            boolean hideInvisible,
            boolean hideSneaking) {

        // 1. Entity category filter
        if (category == NameTagEntityCategory.PLAYERS && !targetPlayers) return false;
        if (category == NameTagEntityCategory.LIVING_ENTITIES && !targetLiving) return false;
        if (category == NameTagEntityCategory.OTHER_ENTITIES && !targetOther) return false;

        // 2. GUI screen open check
        if (isGuiOpen && hideInGui) return false;

        // 3. Spectator mode
        if (isSpectator && hideInSpectator) return false;

        // 4. Invisibility potion / effect
        if (isInvisible && (hideInvisible || respectVanillaVisibility)) return false;

        // 5. Sneaking / crouching player names
        if (isSneaking && (hideSneaking || respectVanillaVisibility)) return false;

        // 6. Distance boundary check
        if (distance > maxDistance || (minDistance > 0 && distance < minDistance)) return false;

        // 7. Data presence check
        if (player == null) return false;

        return true;
    }
}
