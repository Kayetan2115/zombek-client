package pl.zombek.client.forge.integration;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.PlayerTeam;
import pl.zombek.client.core.playerinfo.DataAvailability;
import pl.zombek.client.core.playerinfo.IPlayerInfoDataProvider;
import pl.zombek.client.core.playerinfo.PlayerArmorData;
import pl.zombek.client.core.playerinfo.PlayerInfoData;

import java.util.*;

/**
 * Forge platform data provider supplying connected player information,
 * scoreboard team metadata, ping, health, armor, and entity nametag data.
 */
public class ForgePlayerInfoDataProvider implements IPlayerInfoDataProvider {

    public static String serverHeader = "";
    public static String serverFooter = "";

    @Override
    public List<PlayerInfoData> getConnectedPlayers() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null) {
            return Collections.emptyList();
        }

        List<PlayerInfoData> result = new ArrayList<>();

        if (mc.getConnection() != null) {
            Collection<PlayerInfo> entries = mc.getConnection().getOnlinePlayers();
            for (PlayerInfo entry : entries) {
                if (entry == null || entry.getProfile() == null) continue;
                UUID uuid = entry.getProfile().getId();
                String username = entry.getProfile().getName();
                Component displayComp = entry.getTabListDisplayName();
                String displayName = displayComp != null ? displayComp.getString() : username;
                int latency = entry.getLatency();
                DataAvailability latencyAvail = (latency >= 0) ? DataAvailability.AVAILABLE : DataAvailability.UNKNOWN;

                String gameMode = (entry.getGameMode() != null) ? entry.getGameMode().getName().toUpperCase(Locale.ROOT) : "SURVIVAL";

                Player playerEntity = mc.level.getPlayerByUUID(uuid);
                result.add(buildPlayerData(uuid, username, displayName, latency, latencyAvail, gameMode, playerEntity, mc));
            }
        }

        if (result.isEmpty() && mc.player != null) {
            result.add(buildPlayerData(
                    mc.player.getUUID(),
                    mc.player.getName().getString(),
                    mc.player.getDisplayName().getString(),
                    0,
                    DataAvailability.AVAILABLE,
                    "SURVIVAL",
                    mc.player,
                    mc
            ));
        }

        return result;
    }

    @Override
    public PlayerInfoData getPlayerInfo(UUID uuid, String username) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null) {
            return null;
        }

        if (mc.getConnection() != null) {
            PlayerInfo entry = null;
            if (uuid != null) {
                entry = mc.getConnection().getPlayerInfo(uuid);
            }
            if (entry == null && username != null && !username.isBlank()) {
                for (PlayerInfo e : mc.getConnection().getOnlinePlayers()) {
                    if (e != null && e.getProfile() != null && username.equalsIgnoreCase(e.getProfile().getName())) {
                        entry = e;
                        break;
                    }
                }
            }

            if (entry != null && entry.getProfile() != null) {
                UUID u = entry.getProfile().getId();
                String name = entry.getProfile().getName();
                Component displayComp = entry.getTabListDisplayName();
                String displayName = displayComp != null ? displayComp.getString() : name;
                int latency = entry.getLatency();
                DataAvailability latencyAvail = (latency >= 0) ? DataAvailability.AVAILABLE : DataAvailability.UNKNOWN;
                String gameMode = (entry.getGameMode() != null) ? entry.getGameMode().getName().toUpperCase(Locale.ROOT) : "SURVIVAL";

                Player playerEntity = mc.level.getPlayerByUUID(u);
                return buildPlayerData(u, name, displayName, latency, latencyAvail, gameMode, playerEntity, mc);
            }
        }

        if (mc.player != null) {
            if ((uuid != null && uuid.equals(mc.player.getUUID())) ||
                    (username != null && username.equalsIgnoreCase(mc.player.getName().getString()))) {
                return buildPlayerData(
                        mc.player.getUUID(),
                        mc.player.getName().getString(),
                        mc.player.getDisplayName().getString(),
                        0,
                        DataAvailability.AVAILABLE,
                        "SURVIVAL",
                        mc.player,
                        mc
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

        Minecraft mc = Minecraft.getInstance();
        UUID uuid = entity.getUUID();
        String name = entity.getName().getString();
        String displayName = entity.getDisplayName() != null ? entity.getDisplayName().getString() : name;

        if (entity instanceof Player player) {
            return buildPlayerData(uuid, name, displayName, 0, DataAvailability.UNKNOWN, "SURVIVAL", player, mc);
        }

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

            ItemStack mainHand = living.getMainHandItem();
            if (mainHand != null && !mainHand.isEmpty()) {
                heldItem = mainHand.getHoverName().getString();
                heldAvail = DataAvailability.AVAILABLE;
            }
        }

        double distance = (mc != null && mc.player != null) ? mc.player.distanceTo(entity) : 0.0;

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
        Minecraft mc = Minecraft.getInstance();
        return mc != null && !mc.isSingleplayer();
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
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.options != null && mc.options.keyPlayerList != null && mc.options.keyPlayerList.isDown();
    }

    private PlayerInfoData buildPlayerData(
            UUID uuid,
            String username,
            String displayName,
            int latency,
            DataAvailability latencyAvail,
            String gameMode,
            Player playerEntity,
            Minecraft mc) {

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
            PlayerTeam team = playerEntity.getTeam();
            if (team != null) {
                teamName = team.getName();
                if (team.getColor().getColor() != null) {
                    teamColor = 0xFF000000 | team.getColor().getColor();
                }
            }

            health = playerEntity.getHealth();
            maxHealth = playerEntity.getMaxHealth();
            absorption = playerEntity.getAbsorptionAmount();
            healthAvail = DataAvailability.AVAILABLE;

            armor = extractArmor(playerEntity);
            armorAvail = DataAvailability.AVAILABLE;

            ItemStack mainHand = playerEntity.getMainHandItem();
            if (mainHand != null && !mainHand.isEmpty()) {
                heldItem = mainHand.getHoverName().getString();
                heldAvail = DataAvailability.AVAILABLE;
            }

            if (mc != null && mc.player != null) {
                distance = mc.player.distanceTo(playerEntity);
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
        addArmorPiece(list, living.getItemBySlot(EquipmentSlot.HEAD), PlayerArmorData.ArmorSlot.HELMET);
        addArmorPiece(list, living.getItemBySlot(EquipmentSlot.CHEST), PlayerArmorData.ArmorSlot.CHESTPLATE);
        addArmorPiece(list, living.getItemBySlot(EquipmentSlot.LEGS), PlayerArmorData.ArmorSlot.LEGGINGS);
        addArmorPiece(list, living.getItemBySlot(EquipmentSlot.FEET), PlayerArmorData.ArmorSlot.BOOTS);
        return list;
    }

    private void addArmorPiece(List<PlayerArmorData> list, ItemStack stack, PlayerArmorData.ArmorSlot slot) {
        if (stack == null || stack.isEmpty()) return;
        String regId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        String name = stack.getHoverName().getString();
        int max = stack.getMaxDamage();
        int cur = stack.getDamageValue();
        int durability = Math.max(0, max - cur);
        list.add(new PlayerArmorData(slot, regId, name, durability, max, max > 0, stack));
    }
}
