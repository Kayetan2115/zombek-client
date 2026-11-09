package pl.zombek.client.core.bedwars;

/**
 * Local representation of a player in a Bedwars match.
 * Strictly avoids ESP or private coordinates; relies only on visible tablist and scoreboard info.
 */
public class BedwarsPlayer {
    private final String name;
    private final String uuid;
    private BedwarsTeamColor teamColor;
    private boolean isAlive;
    private boolean inTabList;
    private boolean isSelf;

    public BedwarsPlayer(String name, String uuid, BedwarsTeamColor teamColor, boolean isAlive, boolean inTabList, boolean isSelf) {
        this.name = name;
        this.uuid = uuid != null ? uuid : "";
        this.teamColor = teamColor;
        this.isAlive = isAlive;
        this.inTabList = inTabList;
        this.isSelf = isSelf;
    }

    public BedwarsPlayer(String name, BedwarsTeamColor teamColor, boolean isSelf) {
        this(name, null, teamColor, true, true, isSelf);
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    public BedwarsTeamColor getTeamColor() {
        return teamColor;
    }

    public void setTeamColor(BedwarsTeamColor teamColor) {
        this.teamColor = teamColor;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public boolean isInTabList() {
        return inTabList;
    }

    public void setInTabList(boolean inTabList) {
        this.inTabList = inTabList;
    }

    public boolean isSelf() {
        return isSelf;
    }

    public void setSelf(boolean self) {
        isSelf = self;
    }
}
