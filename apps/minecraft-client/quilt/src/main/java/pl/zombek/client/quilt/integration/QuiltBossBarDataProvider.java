package pl.zombek.client.quilt.integration;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.entity.boss.BossBar;
import pl.zombek.client.core.hud.bossbar.*;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Quilt platform data provider supplying active Boss Bars.
 */
public class QuiltBossBarDataProvider implements IBossBarDataProvider {

    @Override
    public boolean hasActiveBossBar() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.inGameHud == null || client.inGameHud.getBossBarHud() == null) {
            return false;
        }
        Map<UUID, ClientBossBar> map = getBossBarMap(client);
        return map != null && !map.isEmpty();
    }

    @Override
    public List<BossBarData> getActiveBossBars() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.inGameHud == null || client.inGameHud.getBossBarHud() == null) {
            return Collections.emptyList();
        }

        Map<UUID, ClientBossBar> map = getBossBarMap(client);
        if (map == null || map.isEmpty()) {
            return Collections.emptyList();
        }

        List<BossBarData> result = new ArrayList<>(map.size());
        for (Map.Entry<UUID, ClientBossBar> entry : map.entrySet()) {
            ClientBossBar bar = entry.getValue();
            if (bar == null) continue;

            String id = entry.getKey().toString();
            String name = bar.getName().getString();
            float progress = bar.getPercent();
            BossBarColor color = mapColor(bar.getColor());
            BossBarOverlay overlay = mapOverlay(bar.getStyle());
            boolean darkenSky = bar.shouldDarkenSky();
            boolean playMusic = bar.hasDragonMusic();
            boolean createWorldFog = bar.shouldThickenFog();

            result.add(new BossBarData(id, name, progress, color, overlay, darkenSky, playMusic, createWorldFog));
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<UUID, ClientBossBar> getBossBarMap(MinecraftClient client) {
        try {
            Object bossBarHud = client.inGameHud.getBossBarHud();
            for (Field f : bossBarHud.getClass().getDeclaredFields()) {
                if (Map.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    return (Map<UUID, ClientBossBar>) f.get(bossBarHud);
                }
            }
        } catch (Throwable ignored) {}
        return Collections.emptyMap();
    }

    private BossBarColor mapColor(BossBar.Color color) {
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

    private BossBarOverlay mapOverlay(BossBar.Style style) {
        if (style == null) return BossBarOverlay.PROGRESS;
        switch (style) {
            case NOTCHED_6: return BossBarOverlay.NOTCHED_6;
            case NOTCHED_10: return BossBarOverlay.NOTCHED_10;
            case NOTCHED_12: return BossBarOverlay.NOTCHED_12;
            case NOTCHED_20: return BossBarOverlay.NOTCHED_20;
            case PROGRESS:
            default: return BossBarOverlay.PROGRESS;
        }
    }
}
