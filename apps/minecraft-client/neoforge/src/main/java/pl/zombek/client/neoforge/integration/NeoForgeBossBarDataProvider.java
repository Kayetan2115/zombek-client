package pl.zombek.client.neoforge.integration;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.world.BossEvent;
import pl.zombek.client.core.hud.bossbar.*;

import java.lang.reflect.Field;
import java.util.*;

/**
 * NeoForge platform provider supplying active Boss Bars.
 */
public class NeoForgeBossBarDataProvider implements IBossBarDataProvider {

    @Override
    public boolean hasActiveBossBar() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.gui == null || mc.gui.getBossOverlay() == null) {
            return false;
        }
        Map<UUID, LerpingBossEvent> map = getBossEventMap(mc);
        return map != null && !map.isEmpty();
    }

    @Override
    public List<BossBarData> getActiveBossBars() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.gui == null || mc.gui.getBossOverlay() == null) {
            return Collections.emptyList();
        }

        Map<UUID, LerpingBossEvent> map = getBossEventMap(mc);
        if (map == null || map.isEmpty()) {
            return Collections.emptyList();
        }

        List<BossBarData> result = new ArrayList<>(map.size());
        for (Map.Entry<UUID, LerpingBossEvent> entry : map.entrySet()) {
            LerpingBossEvent event = entry.getValue();
            if (event == null) continue;

            String id = entry.getKey().toString();
            String name = event.getName().getString();
            float progress = event.getProgress();
            BossBarColor color = mapColor(event.getColor());
            BossBarOverlay overlay = mapOverlay(event.getOverlay());
            boolean darkenSky = event.shouldDarkenScreen();
            boolean playMusic = event.shouldPlayBossMusic();
            boolean createWorldFog = event.shouldCreateWorldFog();

            result.add(new BossBarData(id, name, progress, color, overlay, darkenSky, playMusic, createWorldFog));
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<UUID, LerpingBossEvent> getBossEventMap(Minecraft mc) {
        try {
            BossHealthOverlay overlay = mc.gui.getBossOverlay();
            for (Field f : overlay.getClass().getDeclaredFields()) {
                if (Map.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    return (Map<UUID, LerpingBossEvent>) f.get(overlay);
                }
            }
        } catch (Throwable ignored) {}
        return Collections.emptyMap();
    }

    private BossBarColor mapColor(BossEvent.BossBarColor color) {
        if (color == null) return BossBarColor.PURPLE;
        switch (color) {
            case PINK: return BossBarColor.PINK;
            case BLUE: return BossBarColor.BLUE;
            case RED: return BossBarColor.RED;
            case GREEN: return BossBarColor.GREEN;
            case YELLOW: return BossBarColor.YELLOW;
            case PURPLE: return BossBarColor.PURPLE;
            case WHITE: return BossBarColor.WHITE;
            default: return BossBarColor.PURPLE;
        }
    }

    private BossBarOverlay mapOverlay(BossEvent.BossBarOverlay overlay) {
        if (overlay == null) return BossBarOverlay.PROGRESS;
        switch (overlay) {
            case NOTCHED_6: return BossBarOverlay.NOTCHED_6;
            case NOTCHED_10: return BossBarOverlay.NOTCHED_10;
            case NOTCHED_12: return BossBarOverlay.NOTCHED_12;
            case NOTCHED_20: return BossBarOverlay.NOTCHED_20;
            case PROGRESS:
            default: return BossBarOverlay.PROGRESS;
        }
    }
}
