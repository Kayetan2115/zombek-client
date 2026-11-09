package pl.zombek.client.core.titles;

/**
 * Encapsulates the active lifecycle state, alpha curves, and timing for Title, Subtitle, and Action Bar.
 */
public class ActiveTitleState {
    // Content
    private String title = null;
    private String subtitle = null;
    private String actionBar = null;

    // Title & Subtitle Timing (ticks, 20 ticks = 1s)
    private int titleFadeInTicks = 10;
    private int titleStayTicks = 70;
    private int titleFadeOutTicks = 20;
    private int titleAgeTicks = 0;

    // Action Bar Timing (separate timeline)
    private int abFadeInTicks = 5;
    private int abStayTicks = 60;
    private int abFadeOutTicks = 15;
    private int abAgeTicks = 0;

    public ActiveTitleState() {}

    public void setTitle(String newTitle) {
        this.title = newTitle;
        this.titleAgeTicks = 0;
        // If title is null or cleared, reset
        if (newTitle == null || newTitle.isEmpty()) {
            this.title = null;
        }
    }

    public void setSubtitle(String newSubtitle) {
        this.subtitle = newSubtitle;
        // Subtitle shares the active title's timeline, or starts its own if title already present
    }

    public void setActionBar(String newActionBar) {
        this.actionBar = newActionBar;
        this.abAgeTicks = 0;
    }

    public void setTitleTimes(int fadeIn, int stay, int fadeOut) {
        if (fadeIn >= 0) this.titleFadeInTicks = fadeIn;
        if (stay >= 0) this.titleStayTicks = stay;
        if (fadeOut >= 0) this.titleFadeOutTicks = fadeOut;
    }

    public void clearTitles() {
        this.title = null;
        this.subtitle = null;
        this.titleAgeTicks = 0;
    }

    public void clearActionBar() {
        this.actionBar = null;
        this.abAgeTicks = 0;
    }

    public void updateTick() {
        if (title != null || subtitle != null) {
            titleAgeTicks++;
            int totalTitleTicks = titleFadeInTicks + titleStayTicks + titleFadeOutTicks;
            if (titleAgeTicks >= totalTitleTicks) {
                clearTitles();
            }
        }

        if (actionBar != null) {
            abAgeTicks++;
            int totalAbTicks = abFadeInTicks + abStayTicks + abFadeOutTicks;
            if (abAgeTicks >= totalAbTicks) {
                clearActionBar();
            }
        }
    }

    /**
     * Normalized alpha (0.0 to 1.0) for Title & Subtitle.
     */
    public float getTitleAlpha() {
        if (title == null && subtitle == null) return 0.0f;
        int total = titleFadeInTicks + titleStayTicks + titleFadeOutTicks;
        if (titleAgeTicks >= total) return 0.0f;

        if (titleFadeInTicks > 0 && titleAgeTicks < titleFadeInTicks) {
            return Math.min(1.0f, (float) titleAgeTicks / (float) titleFadeInTicks);
        }
        if (titleAgeTicks < titleFadeInTicks + titleStayTicks) {
            return 1.0f;
        }
        if (titleFadeOutTicks > 0) {
            int remaining = total - titleAgeTicks;
            return Math.max(0.0f, Math.min(1.0f, (float) remaining / (float) titleFadeOutTicks));
        }
        return 0.0f;
    }

    /**
     * Normalized alpha (0.0 to 1.0) for Action Bar.
     */
    public float getActionBarAlpha() {
        if (actionBar == null) return 0.0f;
        int total = abFadeInTicks + abStayTicks + abFadeOutTicks;
        if (abAgeTicks >= total) return 0.0f;

        if (abFadeInTicks > 0 && abAgeTicks < abFadeInTicks) {
            return Math.min(1.0f, (float) abAgeTicks / (float) abFadeInTicks);
        }
        if (abAgeTicks < abFadeInTicks + abStayTicks) {
            return 1.0f;
        }
        if (abFadeOutTicks > 0) {
            int remaining = total - abAgeTicks;
            return Math.max(0.0f, Math.min(1.0f, (float) remaining / (float) abFadeOutTicks));
        }
        return 0.0f;
    }

    public boolean isTitleActive() {
        if (title == null && subtitle == null) return false;
        int total = titleFadeInTicks + titleStayTicks + titleFadeOutTicks;
        return titleAgeTicks < total;
    }

    public boolean isActionBarActive() {
        if (actionBar == null) return false;
        int total = abFadeInTicks + abStayTicks + abFadeOutTicks;
        return abAgeTicks < total;
    }

    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getActionBar() { return actionBar; }
    public int getTitleFadeInTicks() { return titleFadeInTicks; }
    public int getTitleStayTicks() { return titleStayTicks; }
    public int getTitleFadeOutTicks() { return titleFadeOutTicks; }
    public int getTitleAgeTicks() { return titleAgeTicks; }
    public int getAbFadeInTicks() { return abFadeInTicks; }
    public int getAbStayTicks() { return abStayTicks; }
    public int getAbFadeOutTicks() { return abFadeOutTicks; }
    public int getAbAgeTicks() { return abAgeTicks; }
}
