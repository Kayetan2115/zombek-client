package pl.zombek.client.neoforge.integration;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.scores.PlayerTeam;
import pl.zombek.client.core.bedwars.BedwarsTeamColor;
import pl.zombek.client.core.bedwars.IBedwarsDataProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * NeoForge provider supplying live player name, UUID, and tablist entries for Bedwars Helper.
 */
public class NeoForgeBedwarsDataProvider implements IBedwarsDataProvider {

    @Override
    public boolean isPlayerInGame() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.player != null && mc.level != null;
    }

    @Override
    public String getPlayerName() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.player != null ? mc.player.getName().getString() : "Player";
    }

    @Override
    public String getPlayerUuid() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.player != null ? mc.player.getStringUUID() : "";
    }

    @Override
    public List<String> getTabListPlayerNames() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getConnection() == null) {
            return Collections.emptyList();
        }

        List<String> names = new ArrayList<>();
        for (PlayerInfo info : mc.getConnection().getOnlinePlayers()) {
            if (info != null && info.getProfile() != null && info.getProfile().getName() != null) {
                names.add(info.getProfile().getName());
            }
        }
        return names;
    }

    @Override
    public BedwarsTeamColor getPlayerTeamColor() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return null;
        }

        PlayerTeam team = mc.player.getTeam();
        if (team != null) {
            return BedwarsTeamColor.fromName(team.getName());
        }
        return null;
    }
}
