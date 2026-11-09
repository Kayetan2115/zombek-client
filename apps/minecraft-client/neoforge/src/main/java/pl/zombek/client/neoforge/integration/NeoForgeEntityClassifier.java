package pl.zombek.client.neoforge.integration;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import pl.zombek.client.core.data.TargetEntityType;

/**
 * Classifies Minecraft official mapped entities into TargetEntityType on NeoForge.
 */
public final class NeoForgeEntityClassifier {
    private NeoForgeEntityClassifier() {}

    public static TargetEntityType classify(Entity entity) {
        if (entity instanceof Player) {
            return TargetEntityType.PLAYER;
        }
        if (entity instanceof Monster) {
            return TargetEntityType.HOSTILE;
        }
        if (entity instanceof Animal) {
            return TargetEntityType.PASSIVE;
        }
        if (entity instanceof Projectile) {
            return TargetEntityType.PROJECTILE;
        }
        if (entity instanceof Boat || entity instanceof AbstractMinecart) {
            return TargetEntityType.VEHICLE;
        }
        return TargetEntityType.OTHER;
    }

    public static pl.zombek.client.core.modules.visual.nametags.NameTagEntityCategory classifyToNameTagCategory(Entity entity) {
        if (entity instanceof Player) {
            return pl.zombek.client.core.modules.visual.nametags.NameTagEntityCategory.PLAYERS;
        }
        if (entity instanceof net.minecraft.world.entity.LivingEntity) {
            return pl.zombek.client.core.modules.visual.nametags.NameTagEntityCategory.LIVING_ENTITIES;
        }
        return pl.zombek.client.core.modules.visual.nametags.NameTagEntityCategory.OTHER_ENTITIES;
    }
}
