package handlers;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import handlers.interfaces.BotInterfaceHandler;
import kernel.BotInterfaces;
import objects.User;

public class BotsHandler {

    private static final Map<BotInterfaces, Map<Long, User>> users = new EnumMap<>(BotInterfaces.class);

    static {
        for (BotInterfaces botInterface : BotInterfaces.values()) {
            users.put(botInterface, new ConcurrentHashMap<>());
        }
    }

    public static Map<Long, User> getUsers(BotInterfaces botInterface) {
        return users.get(botInterface);
    }

    public static User getUser(long userId, BotInterfaces botInterface) {
        return getUsers(botInterface).get(userId);
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
