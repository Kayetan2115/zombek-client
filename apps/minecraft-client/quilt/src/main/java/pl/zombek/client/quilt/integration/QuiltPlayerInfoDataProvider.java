package pl.zombek.client.quilt.integration;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.scoreboard.Team;
import pl.zombek.client.core.playerinfo.DataAvailability;
import pl.zombek.client.core.playerinfo.IPlayerInfoDataProvider;
import pl.zombek.client.core.playerinfo.PlayerArmorData;
import pl.zombek.client.core.playerinfo.PlayerInfoData;

import java.util.*;

/**
 * Quilt platform data provider supplying connected player information,
 * scoreboard team metadata, ping, health, armor, and entity nametag data.
 */
public class QuiltPlayerInfoDataProvider implements IPlayerInfoDataProvider {

    public static String serverHeader = "";
    public static String serverFooter = "";

    @Override
    public List<PlayerInfoData> getConnectedPlayers() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) {
            return Collections.emptyList();
        }

        List<PlayerInfoData> result = new ArrayList<>();

        if (client.getNetworkHandler() != null) {
            Collection<PlayerListEntry> entries = client.getNetworkHandler().getPlayerList();
            for (PlayerListEntry entry : entries) {
                if (entry == null || entry.getProfile() == null) continue;
                UUID uuid = entry.getProfile().getId();
                String username = entry.getProfile().getName();
                String displayName = entry.getDisplayName() != null ? entry.getDisplayName().getString() : username;
                int latency = entry.getLatency();
                DataAvailability latencyAvail = (latency >= 0) ? DataAvailability.AVAILABLE : DataAvailability.UNKNOWN;

                String gameMode = (entry.getGameMode() != null) ? entry.getGameMode().getName().toUpperCase(Locale.ROOT) : "SURVIVAL";

                // Check if entity is currently loaded in client world
                PlayerEntity playerEntity = client.world.getPlayerByUuid(uuid);
                result.add(buildPlayerData(uuid, username, displayName, latency, latencyAvail, gameMode, playerEntity, client));
            }
        }

        // If in singleplayer and player list is empty, include client player
        if (result.isEmpty() && client.player != null) {
            result.add(buildPlayerData(
                    client.player.getUuid(),
                    client.player.getName().getString(),
                    client.player.getDisplayName().getString(),
                    0,
                    DataAvailability.AVAILABLE,
                    "SURVIVAL",
                    client.player,
                    client
            ));
        }

        return result;
    }

    @Override
    public PlayerInfoData getPlayerInfo(UUID uuid, String username) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) {
            return null;
        }

        if (client.getNetworkHandler() != null) {
            PlayerListEntry entry = null;
            if (uuid != null) {
                entry = client.getNetworkHandler().getPlayerListEntry(uuid);
            }
            if (entry == null && username != null && !username.isBlank()) {
                for (PlayerListEntry e : client.getNetworkHandler().getPlayerList()) {
                    if (e != null && e.getProfile() != null && username.equalsIgnoreCase(e.getProfile().getName())) {
                        entry = e;
                        break;
                    }
                }
            }

            if (entry != null && entry.getProfile() != null) {
                UUID u = entry.getProfile().getId();
                String name = entry.getProfile().getName();
                String displayName = entry.getDisplayName() != null ? entry.getDisplayName().getString() : name;
                int latency = entry.getLatency();
                DataAvailability latencyAvail = (latency >= 0) ? DataAvailability.AVAILABLE : DataAvailability.UNKNOWN;
                String gameMode = (entry.getGameMode() != null) ? entry.getGameMode().getName().toUpperCase(Locale.ROOT) : "SURVIVAL";

                PlayerEntity playerEntity = client.world.getPlayerByUuid(u);
                return buildPlayerData(u, name, displayName, latency, latencyAvail, gameMode, playerEntity, client);
            }
        }

        if (client.player != null) {
            if ((uuid != null && uuid.equals(client.player.getUuid())) ||
                    (username != null && username.equalsIgnoreCase(client.player.getName().getString()))) {
                return buildPlayerData(
                        client.player.getUuid(),
                        client.player.getName().getString(),
                        client.player.getDisplayName().getString(),
                        0,
                        DataAvailability.AVAILABLE,
                        "SURVIVAL",
                        client.player,
                        client
                );
            }
        }

        return null;
    }

    @Override
    public PlayerInfoData getEntityInfo(Object entityHandle) {
        if (!(entityHandle instanceof Entity entity)) {
            return null;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        UUID uuid = entity.getUuid();
        String name = entity.getName().getString();
        String displayName = entity.getDisplayName() != null ? entity.getDisplayName().getString() : name;

        if (entity instanceof PlayerEntity player) {
            return buildPlayerData(uuid, name, displayName, 0, DataAvailability.UNKNOWN, "SURVIVAL", player, client);
        }

        // Living entity / Mob
        float health = 20.0f;
        float maxHealth = 20.0f;
        float absorption = 0.0f;
        DataAvailability healthAvail = DataAvailability.UNAVAILABLE;
        List<PlayerArmorData> armor = Collections.emptyList();
        DataAvailability armorAvail = DataAvailability.UNAVAILABLE;
        String heldItem = "";
        DataAvailability heldAvail = DataAvailability.UNAVAILABLE;

        if (entity instanceof LivingEntity living) {
            health = living.getHealth();
            maxHealth = living.getMaxHealth();
            absorption = living.getAbsorptionAmount();
            healthAvail = DataAvailability.AVAILABLE;

            armor = extractArmor(living);
            armorAvail = DataAvailability.AVAILABLE;

            ItemStack mainHand = living.getMainHandStack();
            if (mainHand != null && !mainHand.isEmpty()) {
                heldItem = mainHand.getName().getString();
                heldAvail = DataAvailability.AVAILABLE;
            }
        }

        double distance = (client != null && client.player != null) ? client.player.distanceTo(entity) : 0.0;

        return new PlayerInfoData(
                uuid, name, displayName, "", "", "", 0xFFFFFFFF,
                -1, DataAvailability.UNAVAILABLE,
                "SURVIVAL", DataAvailability.UNAVAILABLE,
                health, maxHealth, absorption, healthAvail,
                armor, armorAvail,
                heldItem, heldAvail,
                distance, DataAvailability.AVAILABLE,
                false, null, null
        );
    }

    @Override
    public boolean isMultiplayer() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && !client.isInSingleplayer();
    }

    @Override
    public String getServerHeader() {
        return serverHeader;
    }

    @Override
    public String getServerFooter() {
        return serverFooter;
    }

    @Override
    public boolean isTabListKeyPressed() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client != null && client.options != null && client.options.playerListKey != null && client.options.playerListKey.isPressed();
    }

    private PlayerInfoData buildPlayerData(
            UUID uuid,
            String username,
            String displayName,
            int latency,
            DataAvailability latencyAvail,
            String gameMode,
            PlayerEntity playerEntity,
            MinecraftClient client) {

        String teamName = "";
        int teamColor = 0xFFFFFFFF;
        float health = 20.0f;
        float maxHealth = 20.0f;
        float absorption = 0.0f;
        DataAvailability healthAvail = DataAvailability.UNKNOWN;
        List<PlayerArmorData> armor = Collections.emptyList();
        DataAvailability armorAvail = DataAvailability.UNKNOWN;
        String heldItem = "";
        DataAvailability heldAvail = DataAvailability.UNKNOWN;
        double distance = 0.0;
        DataAvailability distanceAvail = DataAvailability.UNKNOWN;

        if (playerEntity != null) {
            Team team = playerEntity.getScoreboardTeam();
            if (team != null) {
                teamName = team.getName();
                if (team.getColor().getColorValue() != null) {
                    teamColor = 0xFF000000 | team.getColor().getColorValue();
                }
            }

            health = playerEntity.getHealth();
            maxHealth = playerEntity.getMaxHealth();
            absorption = playerEntity.getAbsorptionAmount();
            healthAvail = DataAvailability.AVAILABLE;

            armor = extractArmor(playerEntity);
            armorAvail = DataAvailability.AVAILABLE;

            ItemStack mainHand = playerEntity.getMainHandStack();
            if (mainHand != null && !mainHand.isEmpty()) {
                heldItem = mainHand.getName().getString();
                heldAvail = DataAvailability.AVAILABLE;
            }

            if (client != null && client.player != null) {
                distance = client.player.distanceTo(playerEntity);
                distanceAvail = DataAvailability.AVAILABLE;
            }
        }

        return new PlayerInfoData(
                uuid, username, displayName, "", "", teamName, teamColor,
                latency, latencyAvail,
                gameMode, DataAvailability.AVAILABLE,
                health, maxHealth, absorption, healthAvail,
                armor, armorAvail,
                heldItem, heldAvail,
                distance, distanceAvail,
                false, null, null
        );
    }

    private List<PlayerArmorData> extractArmor(LivingEntity living) {
        List<PlayerArmorData> list = new ArrayList<>();
        addArmorPiece(list, living.getEquippedStack(EquipmentSlot.HEAD), PlayerArmorData.ArmorSlot.HELMET);
        addArmorPiece(list, living.getEquippedStack(EquipmentSlot.CHEST), PlayerArmorData.ArmorSlot.CHESTPLATE);
        addArmorPiece(list, living.getEquippedStack(EquipmentSlot.LEGS), PlayerArmorData.ArmorSlot.LEGGINGS);
        addArmorPiece(list, living.getEquippedStack(EquipmentSlot.FEET), PlayerArmorData.ArmorSlot.BOOTS);
        return list;
    }

    private void addArmorPiece(List<PlayerArmorData> list, ItemStack stack, PlayerArmorData.ArmorSlot slot) {
        if (stack == null || stack.isEmpty()) return;
        String regId = Registries.ITEM.getId(stack.getItem()).toString();
        String name = stack.getName().getString();
        int max = stack.getMaxDamage();
        int cur = stack.getDamage();
        int durability = Math.max(0, max - cur);
        list.add(new PlayerArmorData(slot, regId, name, durability, max, max > 0, stack));
    }
}
