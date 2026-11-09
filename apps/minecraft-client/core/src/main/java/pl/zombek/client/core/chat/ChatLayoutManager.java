package pl.zombek.client.core.chat;

/**
 * Manages geometric layout, dimensions, presets, line spacing, and screen clamping for Custom Chat.
 */
public class ChatLayoutManager {
    private static ChatLayoutManager instance;

    // Presets & Styling
    private ChatPreset preset = ChatPreset.MODERN;
    private boolean showBackground = true;
    private int backgroundColorArgb = 0x88000000;
    private float backgroundOpacity = 0.55f;
    private boolean roundedCorners = true;
    private int cornerRadius = 6;
    private boolean border = true;
    private int borderColorArgb = 0x33FFFFFF;
    private boolean shadow = true;

    // Dimensions
    private int chatWidth = 320;
    private int chatHeight = 160;
    private int maxChatHeight = 240;
    private float chatScale = 1.0f;
    private int lineSpacing = 10;
    private int messagePadding = 4;
    private int maxVisibleLines = 10;
    private int scrollOffset = 0;

    // Positioning
    private int posX = 2;
    private int posY = 20; // from bottom

    // Opacity
    private float defaultOpacity = 0.8f;
    private float focusedOpacity = 1.0f;
    private float unfocusedOpacity = 0.6f;

    // Animations
    private boolean fadeIn = true;
    private boolean slideIn = false;

    public ChatLayoutManager() {
        applyPreset(ChatPreset.MODERN);
    }

    public static synchronized ChatLayoutManager getInstance() {
        if (instance == null) {
            instance = new ChatLayoutManager();
        }
        return instance;
    }

    public void applyPreset(ChatPreset preset) {
        if (preset == null) return;
        this.preset = preset;
        switch (preset) {
            case VANILLA:
                showBackground = true;
                backgroundColorArgb = 0x80000000;
                backgroundOpacity = 0.5f;
                roundedCorners = false;
                border = false;
                shadow = false;
                lineSpacing = 9;
                break;
            case CLASSIC:
                showBackground = true;
                backgroundColorArgb = 0x66000000;
                backgroundOpacity = 0.4f;
                roundedCorners = false;
                border = false;
                shadow = true;
                lineSpacing = 10;
                break;
            case MINIMAL:
                showBackground = false;
                backgroundColorArgb = 0x00000000;
                backgroundOpacity = 0.0f;
                roundedCorners = false;
                border = false;
                shadow = true;
                lineSpacing = 9;
                break;
            case MODERN:
                showBackground = true;
                backgroundColorArgb = 0xAA121216;
                backgroundOpacity = 0.65f;
                roundedCorners = true;
                cornerRadius = 6;
                border = true;
                borderColorArgb = 0x44555566;
                shadow = true;
                lineSpacing = 11;
                break;
            case GLASS:
                showBackground = true;
                backgroundColorArgb = 0x55222233;
                backgroundOpacity = 0.35f;
                roundedCorners = true;
                cornerRadius = 8;
                border = true;
                borderColorArgb = 0x66FFFFFF;
                shadow = true;
                lineSpacing = 10;
                break;
            case COMPACT:
                showBackground = true;
                backgroundColorArgb = 0x990A0A0E;
                backgroundOpacity = 0.6f;
                roundedCorners = false;
                border = true;
                borderColorArgb = 0x33FFFFFF;
                shadow = false;
                lineSpacing = 8;
                maxVisibleLines = 15;
                break;
            case CUSTOM:
                // Keep user-configured settings
                break;
        }
    }

    /**
     * Clamps chat rectangle so it never extends beyond the screen.
     */
    public void clampToScreen(int screenWidth, int screenHeight) {
        if (screenWidth <= 0 || screenHeight <= 0) return;

        chatWidth = Math.min(chatWidth, screenWidth - 10);
        chatHeight = Math.min(chatHeight, screenHeight - 40);

        if (posX < 0) posX = 0;
        if (posX + chatWidth > screenWidth) posX = Math.max(0, screenWidth - chatWidth);

        if (posY < 0) posY = 0;
        if (posY + chatHeight > screenHeight) posY = Math.max(0, screenHeight - chatHeight);
    }

    public void scroll(int lines) {
        this.scrollOffset += lines;
        if (this.scrollOffset < 0) {
            this.scrollOffset = 0;
        }
    }

    public void resetScroll() {
        this.scrollOffset = 0;
    }

    public ChatPreset getPreset() { return preset; }
    public void setPreset(ChatPreset preset) { applyPreset(preset); }
    public boolean isShowBackground() { return showBackground; }
    public void setShowBackground(boolean showBackground) { this.showBackground = showBackground; }
    public int getBackgroundColorArgb() { return backgroundColorArgb; }
    public void setBackgroundColorArgb(int backgroundColorArgb) { this.backgroundColorArgb = backgroundColorArgb; }
    public float getBackgroundOpacity() { return backgroundOpacity; }
    public void setBackgroundOpacity(float backgroundOpacity) { this.backgroundOpacity = backgroundOpacity; }
    public boolean isRoundedCorners() { return roundedCorners; }
    public void setRoundedCorners(boolean roundedCorners) { this.roundedCorners = roundedCorners; }
    public int getCornerRadius() { return cornerRadius; }
    public void setCornerRadius(int cornerRadius) { this.cornerRadius = cornerRadius; }
    public boolean isBorder() { return border; }
    public void setBorder(boolean border) { this.border = border; }
    public int getBorderColorArgb() { return borderColorArgb; }
    public void setBorderColorArgb(int borderColorArgb) { this.borderColorArgb = borderColorArgb; }
    public boolean isShadow() { return shadow; }
    public void setShadow(boolean shadow) { this.shadow = shadow; }
    public int getChatWidth() { return chatWidth; }
    public void setChatWidth(int chatWidth) { this.chatWidth = chatWidth; }
    public int getChatHeight() { return chatHeight; }
    public void setChatHeight(int chatHeight) { this.chatHeight = chatHeight; }
    public int getMaxChatHeight() { return maxChatHeight; }
    public void setMaxChatHeight(int maxChatHeight) { this.maxChatHeight = maxChatHeight; }
    public float getChatScale() { return chatScale; }
    public void setChatScale(float chatScale) { this.chatScale = chatScale; }
    public int getLineSpacing() { return lineSpacing; }
    public void setLineSpacing(int lineSpacing) { this.lineSpacing = lineSpacing; }
    public int getMessagePadding() { return messagePadding; }
    public void setMessagePadding(int messagePadding) { this.messagePadding = messagePadding; }
    public int getMaxVisibleLines() { return maxVisibleLines; }
    public void setMaxVisibleLines(int maxVisibleLines) { this.maxVisibleLines = maxVisibleLines; }
    public int getScrollOffset() { return scrollOffset; }
    public int getPosX() { return posX; }
    public void setPosX(int posX) { this.posX = posX; }
    public int getPosY() { return posY; }
    public void setPosY(int posY) { this.posY = posY; }
    public float getDefaultOpacity() { return defaultOpacity; }
    public void setDefaultOpacity(float defaultOpacity) { this.defaultOpacity = defaultOpacity; }
    public float getFocusedOpacity() { return focusedOpacity; }
    public void setFocusedOpacity(float focusedOpacity) { this.focusedOpacity = focusedOpacity; }
    public float getUnfocusedOpacity() { return unfocusedOpacity; }
    public void setUnfocusedOpacity(float unfocusedOpacity) { this.unfocusedOpacity = unfocusedOpacity; }
    public boolean isFadeIn() { return fadeIn; }
    public void setFadeIn(boolean fadeIn) { this.fadeIn = fadeIn; }
    public boolean isSlideIn() { return slideIn; }
    public void setSlideIn(boolean slideIn) { this.slideIn = slideIn; }

    public static synchronized void resetForTesting() {
        instance = null;
    }
}
