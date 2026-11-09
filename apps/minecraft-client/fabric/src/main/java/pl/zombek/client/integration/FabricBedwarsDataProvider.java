package pl.zombek.client.integration;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Team;
import pl.zombek.client.core.bedwars.BedwarsTeamColor;
import pl.zombek.client.core.bedwars.IBedwarsDataProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fabric provider supplying live player name, UUID, and tablist entries for Bedwars Helper.
 */
public class FabricBedwarsDataProvider implements IBedwarsDataProvider {

    @Override
    public boolean isPlayerInGame() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.player != null && client.world != null;
    }

    @Override
    public String getPlayerName() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.player != null ? client.player.getName().getString() : "Player";
    }

    @Override
    public String getPlayerUuid() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.player != null ? client.player.getUuidAsString() : "";
    }

    @Override
    public List<String> getTabListPlayerNames() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getNetworkHandler() == null) {
            return Collections.emptyList();
        }

        List<String> names = new ArrayList<>();
        for (PlayerListEntry entry : client.getNetworkHandler().getPlayerList()) {
            if (entry != null && entry.getProfile() != null && entry.getProfile().getName() != null) {
                names.add(entry.getProfile().getName());
            }
        }
        return names;
    }

    @Override
    public BedwarsTeamColor getPlayerTeamColor() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return null;
        }

        Team team = client.player.getScoreboardTeam();
        if (team != null) {
            return BedwarsTeamColor.fromName(team.getName());
        }
        return null;
    }
}
