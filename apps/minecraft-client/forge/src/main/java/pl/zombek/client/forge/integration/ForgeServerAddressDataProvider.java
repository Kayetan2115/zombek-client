package pl.zombek.client.forge.integration;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import pl.zombek.client.core.serveraddress.IServerAddressDataProvider;
import pl.zombek.client.core.serveraddress.ServerConnectionData;
import pl.zombek.client.core.serveraddress.ServerConnectionType;

/**
 * Forge platform data provider extracting connection and server address details using Mojang mappings.
 */
public class ForgeServerAddressDataProvider implements IServerAddressDataProvider {

    @Override
    public ServerConnectionData getConnectionData() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null) {
            return new ServerConnectionData(
                    ServerConnectionType.DISCONNECTED,
                    null,
                    25565,
                    null,
                    null,
                    "minecraft:overworld",
                    false
            );
        }

        String dimension = mc.level.dimension().location().toString();

        if (mc.hasSingleplayerServer()) {
            String worldName = (mc.getSingleplayerServer() != null)
                    ? mc.getSingleplayerServer().getWorldData().getLevelName()
                    : "Singleplayer World";
            return new ServerConnectionData(
                    ServerConnectionType.SINGLEPLAYER,
                    null,
                    0,
                    null,
                    worldName,
                    dimension,
                    true
            );
        }

        ServerData entry = mc.getCurrentServer();
        if (entry != null) {
            String rawAddress = entry.ip;
            String host = rawAddress;
            int port = 25565;

            if (rawAddress != null && rawAddress.contains(":")) {
                String[] parts = rawAddress.split(":");
                host = parts[0];
                try {
                    port = Integer.parseInt(parts[1]);
                } catch (Exception ignored) {}
            }

            return new ServerConnectionData(
                    ServerConnectionType.MULTIPLAYER,
                    host,
                    port,
                    entry.name,
                    null,
                    dimension,
                    true
            );
        }

        return new ServerConnectionData(
                ServerConnectionType.DISCONNECTED,
                null,
                25565,
                null,
                null,
                dimension,
                false
        );
    }
}
