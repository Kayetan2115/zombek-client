package pl.zombek.client.core.chat;

import pl.zombek.client.core.hud.IHudRenderer;

import java.util.List;

/**
 * Renders the custom ZombekClient chat interface including background,
 * tabs bar, unread badges, timestamped messages, and input field.
 */
public class CustomChatRenderer {
    private static CustomChatRenderer instance;

    public CustomChatRenderer() {}

    public static synchronized CustomChatRenderer getInstance() {
        if (instance == null) {
            instance = new CustomChatRenderer();
        }
        return instance;
    }

    /**
     * Renders the complete chat HUD or chat screen overlay.
     */
    public void render(IHudRenderer renderer, int mouseX, int mouseY, boolean chatOpen, int screenWidth, int screenHeight) {
        if (renderer == null) return;

        ChatLayoutManager layout = ChatLayoutManager.getInstance();
        layout.clampToScreen(screenWidth, screenHeight);

        int x = layout.getPosX();
        int y = screenHeight - layout.getPosY() - layout.getChatHeight();
        int w = layout.getChatWidth();
        int h = layout.getChatHeight();

        // 1. Draw Chat Background
        if (layout.isShowBackground() && (chatOpen || layout.getUnfocusedOpacity() > 0.05f)) {
            int bg = layout.getBackgroundColorArgb();
            int alpha = (int) (((bg >> 24) & 0xFF) * (chatOpen ? layout.getFocusedOpacity() : layout.getUnfocusedOpacity()));
            int effectiveBg = (alpha << 24) | (bg & 0x00FFFFFF);

            renderer.fill(x, y, w, h, effectiveBg);

            if (layout.isBorder()) {
                int borderColor = layout.getBorderColorArgb();
                renderer.drawBorder(x, y, w, h, borderColor, 1);
            }
        }

        // 2. Draw Tabs Bar (when chat is open or focused)
        int tabY = y - 16;
        if (chatOpen && tabY >= 0) {
            ChatTabManager tabMgr = ChatTabManager.getInstance();
            int curX = x + 2;
            for (ChatTab tab : tabMgr.getTabs()) {
                boolean active = tab.getId().equalsIgnoreCase(tabMgr.getActiveTabId());
                int tabBg = active ? 0xCC333344 : 0x88111122;
                String tabLabel = (tab.getIcon().isEmpty() ? "" : tab.getIcon() + " ") + tab.getDisplayName();
                if (tab.getUnreadCount() > 0) {
                    tabLabel += " §c(" + tab.getUnreadCount() + ")";
                }

                int labelW = renderer.getTextWidth(tabLabel) + 8;
                renderer.fill(curX, tabY, labelW, 14, tabBg);
                if (active) {
                    renderer.drawBorder(curX, tabY, labelW, 14, 0xFF55FF55, 1);
                }
                renderer.drawText(tabLabel, curX + 4, tabY + 3, active ? 0xFFFFFFFF : 0xFFAAAAAA, false, 1.0f, false);

                curX += labelW + 3;
                if (curX > x + w - 10) break;
            }
        }

        // 3. Draw Messages
        ChatTab activeTab = ChatTabManager.getInstance().getActiveTab();
        ChatHistoryManager history = ChatHistoryManager.getInstance();
        List<ChatMessageEntry> messages = history.getMessagesForTab(activeTab, false);

        int lineH = layout.getLineSpacing();
        int maxLines = layout.getMaxVisibleLines();
        int startLine = Math.max(0, messages.size() - maxLines - layout.getScrollOffset());
        int endLine = Math.min(messages.size(), startLine + maxLines);

        int msgY = y + h - layout.getMessagePadding() - lineH;

        for (int i = endLine - 1; i >= startLine && msgY >= y + layout.getMessagePadding(); i--) {
            ChatMessageEntry msg = messages.get(i);

            StringBuilder line = new StringBuilder();
            String ts = ChatMessageManager.getInstance().formatTimestamp(msg.getTimestamp());
            if (!ts.isEmpty()) {
                line.append("§7[").append(ts).append("]§r ");
            }

            if (msg.getSenderName() != null && !msg.getSenderName().isEmpty()) {
                line.append("§f<").append(msg.getSenderName()).append(">§r ");
            }

            line.append(msg.getCleanText());

            if (msg.getRepeatCount() > 1) {
                line.append(" §e×").append(msg.getRepeatCount());
            }

            int textColor = msg.isHighlighted() ? msg.getHighlightColorArgb() : 0xFFFFFFFF;
            if (msg.isImportant()) {
                renderer.fill(x + 2, msgY - 1, w - 4, lineH, 0x44FF5555);
            }

            renderer.drawText(line.toString(), x + layout.getMessagePadding(), msgY, textColor, layout.isShadow(), 1.0f, false);
            msgY -= lineH;
        }

        // 4. Hidden messages indicator
        int hiddenCount = ChatFilterManager.getInstance().getHiddenMessagesCount();
        if (hiddenCount > 0 && chatOpen) {
            String hiddenText = "§8[Ukryte wiadomości: " + hiddenCount + "]";
            renderer.drawText(hiddenText, x + w - renderer.getTextWidth(hiddenText) - 6, y + 4, 0xFFAAAAAA, true, 1.0f, false);
        }

        // 5. Draw Custom Input Box (when chat is open)
        if (chatOpen) {
            ChatInputManager inputMgr = ChatInputManager.getInstance();
            int inputH = inputMgr.getInputHeight();
            int inputY = screenHeight - inputH - 2;

            renderer.fill(x, inputY, w, inputH, inputMgr.getBackgroundColorArgb());
            renderer.drawBorder(x, inputY, w, inputH, inputMgr.getBorderColorArgb(), 1);

            String draft = inputMgr.getDraftForTab(activeTab != null ? activeTab.getId() : "all");
            if (draft.isEmpty()) {
                renderer.drawText(inputMgr.getPlaceholder(), x + 4, inputY + 3, 0xFF777788, false, 1.0f, false);
            } else {
                renderer.drawText(draft + "_", x + 4, inputY + 3, inputMgr.getTextColorArgb(), false, 1.0f, false);
            }
        }
    }

    public static synchronized void resetForTesting() {
        instance = null;
    }
}
