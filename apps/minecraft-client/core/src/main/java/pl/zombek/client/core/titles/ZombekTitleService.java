package pl.zombek.client.core.titles;

/**
 * Central singleton orchestrating title, subtitle, and action bar lifecycle events,
 * timing synchronization, and loader integration.
 */
public class ZombekTitleService {
    private static ZombekTitleService instance;

    public ZombekTitleService() {}

    public static synchronized ZombekTitleService getInstance() {
        if (instance == null) {
            instance = new ZombekTitleService();
        }
        return instance;
    }

    public void onTitle(String titleText) {
        TitleQueueManager.getInstance().onTitle(titleText);
    }

    public void onSubtitle(String subtitleText) {
        TitleQueueManager.getInstance().onSubtitle(subtitleText);
    }

    public void onActionBar(String actionBarText) {
        TitleQueueManager.getInstance().onActionBar(actionBarText);
    }

    public void onTitleTimes(int fadeIn, int stay, int fadeOut) {
        TitleQueueManager.getInstance().onTitleTimes(fadeIn, stay, fadeOut);
    }

    public void onClearTitles() {
        TitleQueueManager.getInstance().onClearTitles();
    }

    public void onClientTick() {
        TitleQueueManager.getInstance().onClientTick();
    }

    public void onWorldLeave() {
        TitleQueueManager.getInstance().onClearTitles();
    }

    public static synchronized void resetForTesting() {
        instance = null;
        TitleQueueManager.resetForTesting();
        CustomTitleRenderer.resetForTesting();
    }
}
