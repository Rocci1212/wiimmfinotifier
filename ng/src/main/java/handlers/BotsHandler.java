package handlers;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import handlers.interfaces.BotInterfaceHandler;
import kernel.BotInterfaces;
import objects.User;

public class BotsHandler {

    private static final List<User> users = new CopyOnWriteArrayList<>();

    public static List<User> getUsers() {
        return users;
    }

    public static User getUser(long userId, BotInterfaces botInterface) {
        for (User user : getUsers()) {
            if (botInterface == user.getBotInterface() && user.getUserId() == userId) {
                return user;
            }
        }
        return null;
    }

    public static void launchBotInterfaces() {
        TelegramBotHandler.instance.launch();
        DiscordBotHandler.instance.launch();
    }

    public static BotInterfaceHandler getBotInterfaceHandler(BotInterfaces botInterface) {
        switch (botInterface) {
            case DISCORD:
                return DiscordBotHandler.instance;
            case TELEGRAM:
            default:
                return TelegramBotHandler.instance;
        }
    }
}
