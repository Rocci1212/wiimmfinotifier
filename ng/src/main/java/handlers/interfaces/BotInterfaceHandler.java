package handlers.interfaces;

public interface BotInterfaceHandler {
    void sendPrivateMessage(long chatId, String message, boolean isNotification);

    void launch();

    String getBoldText(final String input);
}
