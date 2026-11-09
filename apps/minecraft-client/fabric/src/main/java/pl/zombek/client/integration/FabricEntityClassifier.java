package pl.zombek.client.integration;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import pl.zombek.client.core.data.TargetEntityType;

/**
 * Classifies Minecraft Yarn entities into TargetEntityType.
 */
public final class FabricEntityClassifier {
    private FabricEntityClassifier() {}

    public static TargetEntityType classify(Entity entity) {
        if (entity instanceof PlayerEntity) {
            return TargetEntityType.PLAYER;
        }
        if (entity instanceof Monster) {
            return TargetEntityType.HOSTILE;
        }
        if (entity instanceof PassiveEntity) {
            return TargetEntityType.PASSIVE;
        }
        if (entity instanceof ProjectileEntity) {
            return TargetEntityType.PROJECTILE;
        }
        if (entity instanceof BoatEntity || entity instanceof AbstractMinecartEntity) {
            return TargetEntityType.VEHICLE;
        }
        return TargetEntityType.OTHER;
    }

    public static pl.zombek.client.core.modules.visual.nametags.NameTagEntityCategory classifyToNameTagCategory(Entity entity) {
        if (entity instanceof PlayerEntity) {
            return pl.zombek.client.core.modules.visual.nametags.NameTagEntityCategory.PLAYERS;
        }
        if (entity instanceof net.minecraft.entity.LivingEntity) {
            return pl.zombek.client.core.modules.visual.nametags.NameTagEntityCategory.LIVING_ENTITIES;
        }
        return pl.zombek.client.core.modules.visual.nametags.NameTagEntityCategory.OTHER_ENTITIES;
    }
}
