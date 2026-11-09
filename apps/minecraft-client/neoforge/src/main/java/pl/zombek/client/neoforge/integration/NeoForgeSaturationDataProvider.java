package pl.zombek.client.neoforge.integration;

import net.minecraft.client.Minecraft;
import net.minecraft.world.food.FoodData;
import pl.zombek.client.core.hud.saturation.ISaturationDataProvider;

/**
 * NeoForge platform provider supplying real hunger, saturation, and exhaustion metrics.
 */
public class NeoForgeSaturationDataProvider implements ISaturationDataProvider {

    @Override
    public boolean isPlayerAvailable() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.player != null;
    }

    @Override
    public int getFoodLevel() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null) {
            FoodData food = mc.player.getFoodData();
            if (food != null) {
                return food.getFoodLevel();
            }
        }
        return 20;
    }

    @Override
    public boolean isSaturationAvailable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return false;
        if (mc.isSingleplayer()) return true;
        FoodData food = mc.player.getFoodData();
        return food != null && food.getSaturationLevel() > 0.0f;
    }

    @Override
    public float getSaturationLevel() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null) {
            FoodData food = mc.player.getFoodData();
            if (food != null) {
                return food.getSaturationLevel();
            }
        }
        return 0.0f;
    }

    @Override
    public boolean isExhaustionAvailable() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.isSingleplayer();
    }

    @Override
    public float getExhaustionLevel() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null) {
            FoodData food = mc.player.getFoodData();
            if (food != null) {
                return food.getExhaustionLevel();
            }
        }
        return 0.0f;
    }
}
