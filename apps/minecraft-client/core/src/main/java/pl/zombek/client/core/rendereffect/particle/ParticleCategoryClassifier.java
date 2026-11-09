package pl.zombek.client.core.rendereffect.particle;

/**
 * Fast string-based classifier mapping Minecraft particle IDs to ParticleCategory.
 */
public final class ParticleCategoryClassifier {

    private ParticleCategoryClassifier() {
    }

    public static ParticleCategory classify(String particleId) {
        if (particleId == null || particleId.isEmpty()) {
            return ParticleCategory.OTHER;
        }

        String id = particleId.toLowerCase();
        if (id.startsWith("minecraft:")) {
            id = id.substring("minecraft:".length());
        }

        // Combat
        if (id.contains("crit") || id.contains("damage") || id.contains("sweep") ||
            id.contains("enchanted_hit") || id.contains("sonic_boom")) {
            return ParticleCategory.COMBAT;
        }

        // Environment
        if (id.contains("rain") || id.contains("splash") || id.contains("smoke") ||
            id.contains("bubble") || id.contains("cloud") || id.contains("campfire") ||
            id.contains("spore") || id.contains("ash") || id.contains("white_smoke") ||
            id.contains("drip") || id.contains("landing") || id.contains("mycelium") ||
            id.contains("cherry_leaves") || id.contains("snowflake")) {
            return ParticleCategory.ENVIRONMENT;
        }

        // Magic
        if (id.contains("witch") || id.contains("spell") || id.contains("potion") ||
            id.contains("instant_effect") || id.contains("dragon_breath") ||
            id.contains("portal") || id.contains("enchant") || id.contains("glow") ||
            id.contains("sculk_charge") || id.contains("soul") || id.contains("totem") ||
            id.contains("flash") || id.contains("reverse_portal") || id.contains("end_rod")) {
            return ParticleCategory.MAGIC;
        }

        // Blocks
        if (id.contains("block") || id.contains("dust") || id.contains("falling_dust") ||
            id.contains("wax") || id.contains("scrape")) {
            return ParticleCategory.BLOCKS;
        }

        // Items
        if (id.contains("item") || id.contains("slime") || id.contains("snowball")) {
            return ParticleCategory.ITEMS;
        }

        // Other / Social / Ambient
        return ParticleCategory.OTHER;
    }
}
