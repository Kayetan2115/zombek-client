package pl.zombek.client.core.titles;

import pl.zombek.client.core.hud.IHudRenderer;

/**
 * Custom renderer for Minecraft Titles, Subtitles, and Action Bar.
 * Supports smooth alpha transitions, scale/slide animations, custom typography,
 * backgrounds, and screen clamping.
 */
public class CustomTitleRenderer {
    private static CustomTitleRenderer instance;

    // Title settings
    private float titleScale = 3.0f;
    private int titleColorArgb = 0xFFFFFFFF;
    private TitleAlignment titleAlignment = TitleAlignment.CENTER;
    private int titlePositionY = -30; // relative to screen center
    private boolean titleShadow = true;
    private boolean titleBackground = false;
    private int titleBackgroundColorArgb = 0x66000000;

    // Subtitle settings
    private float subtitleScale = 1.5f;
    private int subtitleColorArgb = 0xFFDDDDDD;
    private int subtitleSpacing = 16;
    private boolean subtitleShadow = true;

    // Action Bar settings
    private boolean enableActionBar = true;
    private float actionBarScale = 1.0f;
    private int actionBarColorArgb = 0xFFFFFFFF;
    private int actionBarPositionY = 68; // from bottom
    private boolean actionBarBackground = true;
    private int actionBarBackgroundColorArgb = 0x77000000;
    private boolean actionBarShadow = true;

    // Animations
    private TitleAnimationStyle animationStyle = TitleAnimationStyle.FADE;
    private TitlePreset preset = TitlePreset.MODERN;

    public CustomTitleRenderer() {}

    public static synchronized CustomTitleRenderer getInstance() {
        if (instance == null) {
            instance = new CustomTitleRenderer();
        }
        return instance;
    }

    /**
     * Renders Title and Subtitle.
     */
    public void renderTitles(IHudRenderer renderer, int screenWidth, int screenHeight) {
        if (renderer == null) return;

        ActiveTitleState state = TitleQueueManager.getInstance().getState();
        if (!state.isTitleActive()) return;

        float alpha = state.getTitleAlpha();
        if (alpha <= 0.01f) return;

        int a = (int) (alpha * 255.0f);
        if (a <= 0) return;

        int centerX = screenWidth / 2;
        int centerY = (screenHeight / 2) + titlePositionY;

        // Animation calculation
        int animOffsetY = 0;
        float effectiveScale = titleScale;

        if (animationStyle == TitleAnimationStyle.SLIDE) {
            animOffsetY = (int) ((1.0f - alpha) * -20.0f);
        } else if (animationStyle == TitleAnimationStyle.SCALE) {
            effectiveScale = titleScale * (0.8f + 0.2f * alpha);
        }

        int curY = centerY + animOffsetY;

        // 1. Render Title
        if (state.getTitle() != null && !state.getTitle().isEmpty()) {
            String titleText = state.getTitle();
            int strW = (int) (renderer.getTextWidth(titleText) * (effectiveScale / 1.0f));
            int drawX = centerX - (strW / 2);
            if (titleAlignment == TitleAlignment.LEFT) drawX = 40;
            else if (titleAlignment == TitleAlignment.RIGHT) drawX = screenWidth - strW - 40;

            int effectiveColor = (a << 24) | (titleColorArgb & 0x00FFFFFF);

            if (titleBackground) {
                int bgAlpha = (int) (((titleBackgroundColorArgb >> 24) & 0xFF) * alpha);
                int effBg = (bgAlpha << 24) | (titleBackgroundColorArgb & 0x00FFFFFF);
                renderer.fill(drawX - 8, curY - 4, strW + 16, (int) (10 * effectiveScale) + 8, effBg);
            }

            renderer.drawText(titleText, drawX, curY, effectiveColor, titleShadow, effectiveScale, true);
            curY += (int) (10 * effectiveScale) + subtitleSpacing;
        }

        // 2. Render Subtitle
        if (state.getSubtitle() != null && !state.getSubtitle().isEmpty()) {
            String subText = state.getSubtitle();
            int subW = (int) (renderer.getTextWidth(subText) * (subtitleScale / 1.0f));
            int subX = centerX - (subW / 2);
            if (titleAlignment == TitleAlignment.LEFT) subX = 40;
            else if (titleAlignment == TitleAlignment.RIGHT) subX = screenWidth - subW - 40;

            int effectiveSubColor = (a << 24) | (subtitleColorArgb & 0x00FFFFFF);
            renderer.drawText(subText, subX, curY, effectiveSubColor, subtitleShadow, subtitleScale, false);
        }
    }

    /**
     * Renders Action Bar message independently of Titles.
     */
    public void renderActionBar(IHudRenderer renderer, int screenWidth, int screenHeight) {
        if (renderer == null || !enableActionBar) return;

        ActiveTitleState state = TitleQueueManager.getInstance().getState();
        if (!state.isActionBarActive()) return;

        float alpha = state.getActionBarAlpha();
        if (alpha <= 0.01f) return;

        int a = (int) (alpha * 255.0f);
        if (a <= 0) return;

        String abText = state.getActionBar();
        if (abText == null || abText.isEmpty()) return;

        int strW = renderer.getTextWidth(abText);
        int centerX = screenWidth / 2;
        int drawX = centerX - (strW / 2);
        int drawY = screenHeight - actionBarPositionY;

        int effectiveColor = (a << 24) | (actionBarColorArgb & 0x00FFFFFF);

        if (actionBarBackground) {
            int bgAlpha = (int) (((actionBarBackgroundColorArgb >> 24) & 0xFF) * alpha);
            int effBg = (bgAlpha << 24) | (actionBarBackgroundColorArgb & 0x00FFFFFF);
            renderer.fill(drawX - 6, drawY - 3, strW + 12, 14, effBg);
            renderer.drawBorder(drawX - 6, drawY - 3, strW + 12, 14, 0x33FFFFFF, 1);
        }

        renderer.drawText(abText, drawX, drawY, effectiveColor, actionBarShadow, actionBarScale, false);
    }

    public void applyPreset(TitlePreset preset) {
        if (preset == null) return;
        this.preset = preset;
        switch (preset) {
            case VANILLA:
                titleScale = 4.0f;
                titleColorArgb = 0xFFFFFFFF;
                titleShadow = true;
                titleBackground = false;
                subtitleScale = 2.0f;
                subtitleSpacing = 14;
                actionBarScale = 1.0f;
                actionBarBackground = false;
                animationStyle = TitleAnimationStyle.FADE;
                break;
            case CLASSIC:
                titleScale = 3.5f;
                titleColorArgb = 0xFFFFAA00;
                titleShadow = true;
                titleBackground = false;
                subtitleScale = 1.7f;
                subtitleSpacing = 16;
                actionBarScale = 1.0f;
                actionBarBackground = false;
                animationStyle = TitleAnimationStyle.FADE;
                break;
            case MODERN:
                titleScale = 3.0f;
                titleColorArgb = 0xFFFFFFFF;
                titleShadow = true;
                titleBackground = true;
                titleBackgroundColorArgb = 0x77111118;
                subtitleScale = 1.4f;
                subtitleSpacing = 18;
                actionBarScale = 1.0f;
                actionBarBackground = true;
                actionBarBackgroundColorArgb = 0x88111118;
                animationStyle = TitleAnimationStyle.SLIDE;
                break;
            case MINIMAL:
                titleScale = 2.5f;
                titleColorArgb = 0xFFFFFFFF;
                titleShadow = false;
                titleBackground = false;
                subtitleScale = 1.2f;
                subtitleSpacing = 12;
                actionBarScale = 1.0f;
                actionBarBackground = false;
                animationStyle = TitleAnimationStyle.STATIC;
                break;
            case CUSTOM:
                break;
        }
    }

    public float getTitleScale() { return titleScale; }
    public void setTitleScale(float titleScale) { this.titleScale = titleScale; }
    public int getTitleColorArgb() { return titleColorArgb; }
    public void setTitleColorArgb(int titleColorArgb) { this.titleColorArgb = titleColorArgb; }
    public TitleAlignment getTitleAlignment() { return titleAlignment; }
    public void setTitleAlignment(TitleAlignment titleAlignment) { this.titleAlignment = titleAlignment; }
    public int getTitlePositionY() { return titlePositionY; }
    public void setTitlePositionY(int titlePositionY) { this.titlePositionY = titlePositionY; }
    public boolean isTitleShadow() { return titleShadow; }
    public void setTitleShadow(boolean titleShadow) { this.titleShadow = titleShadow; }
    public boolean isTitleBackground() { return titleBackground; }
    public void setTitleBackground(boolean titleBackground) { this.titleBackground = titleBackground; }
    public int getTitleBackgroundColorArgb() { return titleBackgroundColorArgb; }
    public void setTitleBackgroundColorArgb(int titleBackgroundColorArgb) { this.titleBackgroundColorArgb = titleBackgroundColorArgb; }
    public float getSubtitleScale() { return subtitleScale; }
    public void setSubtitleScale(float subtitleScale) { this.subtitleScale = subtitleScale; }
    public int getSubtitleColorArgb() { return subtitleColorArgb; }
    public void setSubtitleColorArgb(int subtitleColorArgb) { this.subtitleColorArgb = subtitleColorArgb; }
    public int getSubtitleSpacing() { return subtitleSpacing; }
    public void setSubtitleSpacing(int subtitleSpacing) { this.subtitleSpacing = subtitleSpacing; }
    public boolean isSubtitleShadow() { return subtitleShadow; }
    public void setSubtitleShadow(boolean subtitleShadow) { this.subtitleShadow = subtitleShadow; }
    public boolean isEnableActionBar() { return enableActionBar; }
    public void setEnableActionBar(boolean enableActionBar) { this.enableActionBar = enableActionBar; }
    public float getActionBarScale() { return actionBarScale; }
    public void setActionBarScale(float actionBarScale) { this.actionBarScale = actionBarScale; }
    public int getActionBarColorArgb() { return actionBarColorArgb; }
    public void setActionBarColorArgb(int actionBarColorArgb) { this.actionBarColorArgb = actionBarColorArgb; }
    public int getActionBarPositionY() { return actionBarPositionY; }
    public void setActionBarPositionY(int actionBarPositionY) { this.actionBarPositionY = actionBarPositionY; }
    public boolean isActionBarBackground() { return actionBarBackground; }
    public void setActionBarBackground(boolean actionBarBackground) { this.actionBarBackground = actionBarBackground; }
    public int getActionBarBackgroundColorArgb() { return actionBarBackgroundColorArgb; }
    public void setActionBarBackgroundColorArgb(int actionBarBackgroundColorArgb) { this.actionBarBackgroundColorArgb = actionBarBackgroundColorArgb; }
    public boolean isActionBarShadow() { return actionBarShadow; }
    public void setActionBarShadow(boolean actionBarShadow) { this.actionBarShadow = actionBarShadow; }
    public TitleAnimationStyle getAnimationStyle() { return animationStyle; }
    public void setAnimationStyle(TitleAnimationStyle animationStyle) { this.animationStyle = animationStyle; }
    public TitlePreset getPreset() { return preset; }
    public void setPreset(TitlePreset preset) { applyPreset(preset); }

    public static synchronized void resetForTesting() {
        instance = null;
    }
}
