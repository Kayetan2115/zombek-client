package pl.zombek.client.neoforge.integration;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import pl.zombek.client.core.netgraph.ConnectionStatus;
import pl.zombek.client.core.netgraph.INetworkDataProvider;

/**
 * NeoForge platform data provider extracting live network connectivity,
 * ping, and singleplayer status using official Mojang mappings.
 */
public class NeoForgeNetworkDataProvider implements INetworkDataProvider {

    @Override
    public boolean isConnected() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.level != null && mc.getConnection() != null;
    }

    @Override
    public boolean isSingleplayer() {
        Minecraft mc = Minecraft.getInstance();
        return mc == null || mc.isSingleplayer();
    }

    @Override
    public ConnectionStatus getConnectionStatus() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null) {
            return ConnectionStatus.DISCONNECTED;
        }
        if (mc.isSingleplayer()) {
            return ConnectionStatus.SINGLEPLAYER;
        }
        if (mc.getCurrentServer() != null) {
            return ConnectionStatus.CONNECTED;
        }
        if (mc.getConnection() != null) {
            return ConnectionStatus.CONNECTED;
        }
        return ConnectionStatus.UNKNOWN;
    }

    @Override
    public int getPingMs() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.isSingleplayer() || mc.player == null || mc.getConnection() == null) {
            return -1;
        }
        PlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
        return info != null ? info.getLatency() : -1;
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
