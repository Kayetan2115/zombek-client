package pl.zombek.client.core.chat;

/**
 * Central singleton orchestrating chat event reception, lifecycle transitions,
 * and integration between modloaders and core chat managers.
 */
public class ZombekChatService {
    private static ZombekChatService instance;

    public ZombekChatService() {}

    public static synchronized ZombekChatService getInstance() {
        if (instance == null) {
            instance = new ZombekChatService();
        }
        return instance;
    }

    /**
     * Called by loader adapters when an incoming chat message is received from server/game.
     * Returns the processed ChatMessageEntry (or null if rejected).
     */
    public ChatMessageEntry onIncomingMessage(String rawText, String senderName, String senderUuid,
                                              boolean system, Object nativeComponent, String clientPlayerName) {
        return ChatMessageManager.getInstance().processIncoming(rawText, senderName, senderUuid, system, nativeComponent, clientPlayerName);
    }

    public void onClientTick() {
        // Periodic housekeeping if needed
    }

    public void onWorldLeave() {
        ChatHistoryManager.getInstance().onDisconnect();
        ChatInputManager.getInstance().clearAllDrafts();
        ChatNotificationController.getInstance().resetUnread();
    }

    public static synchronized void resetForTesting() {
        instance = null;
        ChatMessageManager.resetForTesting();
        ChatFilterManager.resetForTesting();
        ChatHistoryManager.resetForTesting();
        ChatTabManager.resetForTesting();
        ChatNotificationController.resetForTesting();
        ChatInputManager.resetForTesting();
        ChatLayoutManager.resetForTesting();
        CustomChatRenderer.resetForTesting();
    }
}
