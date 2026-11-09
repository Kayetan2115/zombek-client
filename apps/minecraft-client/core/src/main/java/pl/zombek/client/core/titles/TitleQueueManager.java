package pl.zombek.client.core.titles;

/**
 * Singleton managing active screen titles, subtitles, action bar messages,
 * timing transitions, and user override configurations.
 */
public class TitleQueueManager {
    private static TitleQueueManager instance;

    private final ActiveTitleState state = new ActiveTitleState();

    // User Timing Overrides
    private boolean preserveVanillaTiming = true;
    private int overrideFadeInTicks = 10;
    private int overrideStayTicks = 70;
    private int overrideFadeOutTicks = 20;
    private int overrideActionBarStayTicks = 60;

    public TitleQueueManager() {}

    public static synchronized TitleQueueManager getInstance() {
        if (instance == null) {
            instance = new TitleQueueManager();
        }
        return instance;
    }

    public synchronized void onTitle(String titleText) {
        if (!preserveVanillaTiming) {
            state.setTitleTimes(overrideFadeInTicks, overrideStayTicks, overrideFadeOutTicks);
        }
        state.setTitle(titleText);
    }

    public synchronized void onSubtitle(String subtitleText) {
        state.setSubtitle(subtitleText);
    }

    public synchronized void onActionBar(String actionBarText) {
        if (!preserveVanillaTiming) {
            // Apply custom action bar stay duration
            state.setActionBar(actionBarText);
        } else {
            state.setActionBar(actionBarText);
        }
    }

    public synchronized void onTitleTimes(int fadeIn, int stay, int fadeOut) {
        if (preserveVanillaTiming) {
            state.setTitleTimes(fadeIn, stay, fadeOut);
        }
    }

    public synchronized void onClearTitles() {
        state.clearTitles();
    }

    public synchronized void onClientTick() {
        state.updateTick();
    }

    public ActiveTitleState getState() {
        return state;
    }

    public boolean isPreserveVanillaTiming() { return preserveVanillaTiming; }
    public void setPreserveVanillaTiming(boolean preserveVanillaTiming) {
        this.preserveVanillaTiming = preserveVanillaTiming;
    }
    public int getOverrideFadeInTicks() { return overrideFadeInTicks; }
    public void setOverrideFadeInTicks(int overrideFadeInTicks) {
        this.overrideFadeInTicks = Math.max(0, overrideFadeInTicks);
    }
    public int getOverrideStayTicks() { return overrideStayTicks; }
    public void setOverrideStayTicks(int overrideStayTicks) {
        this.overrideStayTicks = Math.max(1, overrideStayTicks);
    }
    public int getOverrideFadeOutTicks() { return overrideFadeOutTicks; }
    public void setOverrideFadeOutTicks(int overrideFadeOutTicks) {
        this.overrideFadeOutTicks = Math.max(0, overrideFadeOutTicks);
    }
    public int getOverrideActionBarStayTicks() { return overrideActionBarStayTicks; }
    public void setOverrideActionBarStayTicks(int overrideActionBarStayTicks) {
        this.overrideActionBarStayTicks = Math.max(1, overrideActionBarStayTicks);
    }

    public static synchronized void resetForTesting() {
        instance = null;
    }
}
