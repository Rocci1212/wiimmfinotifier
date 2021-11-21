package discordbot;

import javax.security.auth.login.LoginException;

import handlers.BotsHandler;
import handlers.DatabaseHandler;
import handlers.DiscordBotHandler;
import kernel.BotInterfaces;
import kernel.Config;
import kernel.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import utils.CommandsProcessor;
import utils.MessagesSplitter;

public class MessageListener extends ListenerAdapter {
    public static void launch() {
        try {
            final JDABuilder jdaBuilder = JDABuilder.createDefault(Config.discordBotToken);
            jdaBuilder.setMemberCachePolicy(MemberCachePolicy.ALL);
            jdaBuilder.setChunkingFilter(ChunkingFilter.ALL);
            jdaBuilder.disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.EMOTE, CacheFlag.VOICE_STATE);
            jdaBuilder.enableIntents(GatewayIntent.GUILD_MEMBERS);
            jdaBuilder.disableIntents(GatewayIntent.GUILD_MESSAGE_TYPING);
            jdaBuilder.addEventListeners(new MessageListener());
            final JDA jda = jdaBuilder.build().awaitReady();
            jda.getPresence().setActivity(Activity.playing("DM me to follow games !"));
            DiscordBotHandler.instance.setDiscordBot(jda);
            Main.printNewEvent("Finished Building JDA !", false);
        } catch (final LoginException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized objects.User getCurrentUser(long userId, BotInterfaces botInterface) {
    	objects.User currentUser = BotsHandler.getUser(userId, botInterface);
		if (currentUser == null) {
			currentUser = new objects.User(userId, botInterface);
			BotsHandler.getUsers().add(currentUser);
			DatabaseHandler.addUser(userId, botInterface);
			Main.printNewEvent("User creation : " + userId, true);
		}
		return currentUser;
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        User author = event.getAuthor();
        Message message = event.getMessage();
        MessageChannel channel = event.getChannel();
        String msg = message.getContentDisplay();
        boolean bot = author.isBot();
        if (!bot) {
            if (event.isFromType(ChannelType.PRIVATE)) {
            	objects.User currentUser = getCurrentUser(author.getIdLong(), BotInterfaces.DISCORD);
            	String answer = CommandsProcessor.processCommandFromDiscord(currentUser, msg);
            	if (answer != null) {
            	    for (final String maxPossibleSizeMessage : MessagesSplitter.getMaximumPossibleSizeSplittedMessagesList(answer, Config.discordMaxMessageLength)) {
                        channel.sendMessage(maxPossibleSizeMessage).queue();
                    }
            	}
            } else if (event.isFromType(ChannelType.TEXT)) {
            	if (msg.equalsIgnoreCase("!showplayedgames") || msg.equalsIgnoreCase("!wp")) {
            		// Global command
            		String answer = CommandsProcessor.processCommandFromDiscord(null, "showplayedgames");
            		if (answer != null) {
            			channel.sendMessage(answer).queue();
            		}
            	}
            }
        }
    }
}
