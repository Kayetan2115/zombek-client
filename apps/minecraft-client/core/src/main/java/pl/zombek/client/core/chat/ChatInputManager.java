package pl.zombek.client.core.chat;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages chat input state, preserving draft text across tabs and GUI closes.
 */
public class ChatInputManager {
    private static ChatInputManager instance;

    // Per-tab or global draft map
    private final Map<String, String> drafts = new HashMap<>();
    private String globalDraft = "";

    // Input visual settings
    private int backgroundColorArgb = 0xAA000000;
    private int borderColorArgb = 0x55FFFFFF;
    private int textColorArgb = 0xFFFFFFFF;
    private int cursorColorArgb = 0xFFFFFFFF;
    private String placeholder = "Napisz wiadomość... (lub / pomoc)";
    private int inputHeight = 14;

    public ChatInputManager() {}

    public static synchronized ChatInputManager getInstance() {
        if (instance == null) {
            instance = new ChatInputManager();
        }
        return instance;
    }

    public synchronized void setDraftForTab(String tabId, String text) {
        if (tabId == null || tabId.isEmpty()) {
            this.globalDraft = text != null ? text : "";
        } else {
            this.drafts.put(tabId.toLowerCase(), text != null ? text : "");
        }
    }

    public synchronized String getDraftForTab(String tabId) {
        if (tabId != null && drafts.containsKey(tabId.toLowerCase())) {
            return drafts.get(tabId.toLowerCase());
        }
        return globalDraft;
    }

    public synchronized void clearDraft(String tabId) {
        if (tabId != null) {
            drafts.remove(tabId.toLowerCase());
        }
        globalDraft = "";
    }

    public synchronized void clearAllDrafts() {
        drafts.clear();
        globalDraft = "";
    }

    public int getBackgroundColorArgb() { return backgroundColorArgb; }
    public void setBackgroundColorArgb(int backgroundColorArgb) { this.backgroundColorArgb = backgroundColorArgb; }
    public int getBorderColorArgb() { return borderColorArgb; }
    public void setBorderColorArgb(int borderColorArgb) { this.borderColorArgb = borderColorArgb; }
    public int getTextColorArgb() { return textColorArgb; }
    public void setTextColorArgb(int textColorArgb) { this.textColorArgb = textColorArgb; }
    public int getCursorColorArgb() { return cursorColorArgb; }
    public void setCursorColorArgb(int cursorColorArgb) { this.cursorColorArgb = cursorColorArgb; }
    public String getPlaceholder() { return placeholder; }
    public void setPlaceholder(String placeholder) { this.placeholder = placeholder; }
    public int getInputHeight() { return inputHeight; }
    public void setInputHeight(int inputHeight) { this.inputHeight = inputHeight; }

    public static synchronized void resetForTesting() {
        instance = null;
    }
}
