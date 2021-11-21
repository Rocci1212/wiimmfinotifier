package handlers.interfaces;

public interface BotInterfaceHandler {
    void sendNotification(long chatId, String message);

    void launch();

    String getBoldText(final String input);
}
