package pl.zombek.client.core.time;

/**
 * Null Object fallback for time and world queries when no world is active or during unit tests.
 */
public class FallbackTimeDataProvider implements ITimeDataProvider {

    private boolean worldLoaded = false;
    private long worldTimeOfDay = -1L;
    private long worldGameTime = -1L;
    private boolean timeAvailable = false;
    private boolean singleplayer = true;
    private String worldName = null;
    private String serverAddress = null;
    private long singleplayerPlaytimeTicks = -1L;
    private String dimensionId = "minecraft:overworld";

    @Override
    public boolean isWorldLoaded() {
        return worldLoaded;
    }

    public void setWorldLoaded(boolean worldLoaded) {
        this.worldLoaded = worldLoaded;
    }

    @Override
    public long getWorldTimeOfDay() {
        return worldTimeOfDay;
    }

    public void setWorldTimeOfDay(long worldTimeOfDay) {
        this.worldTimeOfDay = worldTimeOfDay;
        this.timeAvailable = worldTimeOfDay >= 0;
    }

    @Override
    public long getWorldGameTime() {
        return worldGameTime;
    }

    public void setWorldGameTime(long worldGameTime) {
        this.worldGameTime = worldGameTime;
    }

    @Override
    public boolean isTimeAvailable() {
        return timeAvailable;
    }

    public void setTimeAvailable(boolean timeAvailable) {
        this.timeAvailable = timeAvailable;
    }

    @Override
    public boolean isSingleplayer() {
        return singleplayer;
    }

    public void setSingleplayer(boolean singleplayer) {
        this.singleplayer = singleplayer;
    }

    @Override
    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    @Override
    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    @Override
    public long getSingleplayerPlaytimeTicks() {
        return singleplayerPlaytimeTicks;
    }

    public void setSingleplayerPlaytimeTicks(long singleplayerPlaytimeTicks) {
        this.singleplayerPlaytimeTicks = singleplayerPlaytimeTicks;
    }

    @Override
    public String getDimensionId() {
        return dimensionId;
    }

    public void setDimensionId(String dimensionId) {
        this.dimensionId = dimensionId;
    }
}
