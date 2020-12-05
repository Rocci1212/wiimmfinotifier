package discordbot;

import javax.security.auth.login.LoginException;

import handlers.DatabaseHandler;
import handlers.DiscordBotHandler;
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

public class MessageListener extends ListenerAdapter {
	public final static int DISCORD_MSG_CHARACTERS_LIMIT = 2000;

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
            DiscordBotHandler.setDiscordBot(jda);
            Main.printNewEvent("Finished Building JDA !", false);
        } catch (final LoginException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized objects.User getCurrentUser(long userId) {
    	objects.User currentUser = DiscordBotHandler.getUser(userId);
		if (currentUser == null) {
			currentUser = new objects.User(userId);
			DiscordBotHandler.getUsers().add(currentUser);
			DatabaseHandler.addUser(userId);
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
            	objects.User currentUser = getCurrentUser(author.getIdLong());
            	String answer = CommandsProcessor.processCommand(currentUser, msg);
            	if (answer != null) {
            		for (int i = 0; i < answer.length(); i = i + MessageListener.DISCORD_MSG_CHARACTERS_LIMIT) {
            			boolean answerLastPart = i + MessageListener.DISCORD_MSG_CHARACTERS_LIMIT > answer.length();
            			channel.sendMessage(answer.substring(i, answerLastPart ? answer.length() : i + MessageListener.DISCORD_MSG_CHARACTERS_LIMIT)).queue();
            		}
            	}
            } else if (event.isFromType(ChannelType.TEXT)) {
            	if (msg.equalsIgnoreCase("!showplayedgames") || msg.equalsIgnoreCase("!wp")) {
            		// Global command
            		String answer = CommandsProcessor.processCommand(null, "showplayedgames");
            		if (answer != null) {
            			channel.sendMessage(answer).queue();
            		}
            	}
            }
        }
    }
}
