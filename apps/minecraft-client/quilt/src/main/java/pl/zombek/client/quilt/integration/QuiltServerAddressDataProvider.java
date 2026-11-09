package pl.zombek.client.quilt.integration;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import pl.zombek.client.core.serveraddress.IServerAddressDataProvider;
import pl.zombek.client.core.serveraddress.ServerConnectionData;
import pl.zombek.client.core.serveraddress.ServerConnectionType;

/**
 * Quilt platform data provider extracting connection and server address details using Yarn/Quilt mappings.
 */
public class QuiltServerAddressDataProvider implements IServerAddressDataProvider {

    @Override
    public ServerConnectionData getConnectionData() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) {
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

        String dimension = client.world.getRegistryKey().getValue().toString();

        if (client.isInSingleplayer()) {
            String worldName = (client.getServer() != null)
                    ? client.getServer().getSaveProperties().getLevelName()
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

        ServerInfo entry = client.getCurrentServerEntry();
        if (entry != null) {
            String rawAddress = entry.address;
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
