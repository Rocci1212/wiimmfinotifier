package handlers.interfaces;

public interface BotInterfaceHandler {
    void sendTo(long chatId, String message);

    void launch();

    String getBoldText(final String input);
}
