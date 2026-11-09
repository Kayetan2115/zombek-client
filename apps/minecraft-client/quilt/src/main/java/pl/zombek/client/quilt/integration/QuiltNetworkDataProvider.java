package pl.zombek.client.quilt.integration;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import pl.zombek.client.core.netgraph.ConnectionStatus;
import pl.zombek.client.core.netgraph.INetworkDataProvider;

/**
 * Quilt platform data provider extracting live network connectivity,
 * player list ping, and singleplayer connection status using Yarn mappings.
 */
public class QuiltNetworkDataProvider implements INetworkDataProvider {

    @Override
    public boolean isConnected() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.world != null && client.getNetworkHandler() != null;
    }

    @Override
    public boolean isSingleplayer() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client == null || client.isInSingleplayer();
    }

    @Override
    public ConnectionStatus getConnectionStatus() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) {
            return ConnectionStatus.DISCONNECTED;
        }
        if (client.isInSingleplayer()) {
            return ConnectionStatus.SINGLEPLAYER;
        }
        if (client.getCurrentServerEntry() != null) {
            return ConnectionStatus.CONNECTED;
        }
        if (client.getNetworkHandler() != null) {
            return ConnectionStatus.CONNECTED;
        }
        return ConnectionStatus.UNKNOWN;
    }

    @Override
    public int getPingMs() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.isInSingleplayer() || client.player == null || client.getNetworkHandler() == null) {
            return -1;
        }
        PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
        return entry != null ? entry.getLatency() : -1;
    }

    @Override
    public long getIncomingBytes() {
        return -1;
    }

    @Override
    public long getOutgoingBytes() {
        return -1;
    }

    @Override
    public int getIncomingPackets() {
        return 0;
    }

    @Override
    public int getOutgoingPackets() {
        return 0;
    }

    @Override
    public boolean isTrafficTrackingSupported() {
        return !isSingleplayer();
    }
}
