package pl.zombek.client.core.playerinfo;

import pl.zombek.client.core.friends.FriendEntry;
import pl.zombek.client.core.friends.NameTagFriendManager;

import java.util.*;

/**
 * Fallback and preview provider supplying realistic player and entity entries.
 */
public class FallbackPlayerInfoDataProvider implements IPlayerInfoDataProvider {

    private boolean tabKeyPressed = false;

    public void setTabKeyPressed(boolean pressed) {
        this.tabKeyPressed = pressed;
    }

    @Override
    public List<PlayerInfoData> getConnectedPlayers() {
        List<PlayerInfoData> list = new ArrayList<>();

        UUID uuid1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
        FriendEntry friend1 = NameTagFriendManager.getInstance().getFriend(uuid1, "ZombekMaster");
        list.add(new PlayerInfoData(
                uuid1, "ZombekMaster", "ZombekMaster", "[DEV] ", " ★", "RedTeam", 0xFFFF5555,
                24, DataAvailability.AVAILABLE, "SURVIVAL", DataAvailability.AVAILABLE,
                20.0f, 20.0f, 4.0f, DataAvailability.AVAILABLE,
                Collections.emptyList(), DataAvailability.AVAILABLE, "Diamond Sword", DataAvailability.AVAILABLE,
                12.5, DataAvailability.AVAILABLE, friend1 != null, friend1, null
        ));

        UUID uuid2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
        list.add(new PlayerInfoData(
                uuid2, "Alex", "Alex", "", "", "BlueTeam", 0xFF5555FF,
                48, DataAvailability.AVAILABLE, "SURVIVAL", DataAvailability.AVAILABLE,
                16.0f, 20.0f, 0.0f, DataAvailability.AVAILABLE,
                Collections.emptyList(), DataAvailability.AVAILABLE, "Bow", DataAvailability.AVAILABLE,
                24.0, DataAvailability.AVAILABLE, false, null, null
        ));

        UUID uuid3 = UUID.fromString("00000000-0000-0000-0000-000000000003");
        list.add(new PlayerInfoData(
                uuid3, "Steve", "Steve", "", "", "BlueTeam", 0xFF5555FF,
                32, DataAvailability.AVAILABLE, "CREATIVE", DataAvailability.AVAILABLE,
                20.0f, 20.0f, 0.0f, DataAvailability.AVAILABLE,
                Collections.emptyList(), DataAvailability.AVAILABLE, "Golden Apple", DataAvailability.AVAILABLE,
                8.2, DataAvailability.AVAILABLE, false, null, null
        ));

        UUID uuid4 = UUID.fromString("00000000-0000-0000-0000-000000000004");
        list.add(new PlayerInfoData(
                uuid4, "Guest_42", "Guest_42", "", "", "", 0xFFFFFFFF,
                -1, DataAvailability.UNAVAILABLE, "SURVIVAL", DataAvailability.AVAILABLE,
                10.0f, 20.0f, 0.0f, DataAvailability.AVAILABLE,
                Collections.emptyList(), DataAvailability.AVAILABLE, "Shield", DataAvailability.AVAILABLE,
                35.0, DataAvailability.AVAILABLE, false, null, null
        ));

        return list;
    }

    @Override
    public PlayerInfoData getPlayerInfo(UUID uuid, String username) {
        for (PlayerInfoData p : getConnectedPlayers()) {
            if (uuid != null && uuid.equals(p.getUuid())) return p;
            if (username != null && username.equalsIgnoreCase(p.getUsername())) return p;
        }
        return new PlayerInfoData(
                uuid != null ? uuid : UUID.randomUUID(),
                username != null ? username : "Player",
                username != null ? username : "Player",
                "", "", "", 0xFFFFFFFF,
                0, DataAvailability.UNKNOWN, "SURVIVAL", DataAvailability.UNKNOWN,
                20.0f, 20.0f, 0.0f, DataAvailability.UNKNOWN,
                Collections.emptyList(), DataAvailability.UNKNOWN, "", DataAvailability.UNKNOWN,
                0.0, DataAvailability.UNKNOWN, false, null, null
        );
    }

    @Override
    public PlayerInfoData getEntityInfo(Object entityHandle) {
        return new PlayerInfoData(
                UUID.randomUUID(), "Zombie", "Zombie", "", "", "", 0xFF55FF55,
                0, DataAvailability.UNAVAILABLE, "SURVIVAL", DataAvailability.UNAVAILABLE,
                20.0f, 20.0f, 0.0f, DataAvailability.AVAILABLE,
                Collections.emptyList(), DataAvailability.AVAILABLE, "Iron Shovel", DataAvailability.AVAILABLE,
                15.4, DataAvailability.AVAILABLE, false, null, null
        );
    }

    @Override
    public boolean isMultiplayer() {
        return false;
    }

    @Override
    public String getServerHeader() {
        return "§6§lZombekClient§r §7— §fOfficial Server";
    }

    @Override
    public String getServerFooter() {
        return "§eplay.zombekclient.pl §7| §bPing: §a24ms";
    }

    @Override
    public boolean isTabListKeyPressed() {
        return tabKeyPressed;
    }
}
