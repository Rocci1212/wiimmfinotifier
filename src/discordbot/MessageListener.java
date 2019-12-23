package discordbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;

import handlers.DatabaseHandler;
import handlers.DiscordBotHandler;
import kernel.Config;
import kernel.Main;

public class MessageListener extends ListenerAdapter {
	public final static int DISCORD_MSG_CHARACTERS_LIMIT = 2000;
	
    public static void launch() {
        try {
            JDA jda = new JDABuilder(Config.discordBotToken)
                    .addEventListeners(new MessageListener())
                    .build();
            jda.awaitReady(); // Blocking guarantees that JDA will be completely loaded.
            jda.getPresence().setActivity(Activity.playing("DM me to follow games !"));
            DiscordBotHandler.setDiscordBot(jda);
            Main.printNewEvent("Finished Building JDA !", false);
        } catch (LoginException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            //Due to the fact that awaitReady is a blocking method, one which waits until JDA is fully loaded,
            // the waiting can be interrupted. This is the exception that would fire in that situation.
            //As a note: in this extremely simplified example this will never occur. In fact, this will never occur unless
            // you use awaitReady in a thread that has the possibility of being interrupted (async thread usage and interrupts)
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
