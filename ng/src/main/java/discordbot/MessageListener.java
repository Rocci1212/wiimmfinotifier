package discordbot;

import java.util.Objects;
import javax.security.auth.login.LoginException;

import handlers.BotsHandler;
import handlers.DatabaseHandler;
import handlers.DiscordBotHandler;
import handlers.interfaces.BotInterfaceHandler;
import kernel.BotInterfaces;
import kernel.Config;
import kernel.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
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
            CommandListUpdateAction commands = jda.updateCommands();
            commands.addCommands(
                    new CommandData("infos", "Show infos and statistics")
            );
            commands.addCommands(
                    new CommandData("showplayedgames", "Show the list of currently played games")
            );
            commands.addCommands(
                    new CommandData("followgame", "Get notified when someone starts playing an unactive game")
                            .addOptions(new OptionData(OptionType.STRING, "gameid", "A valid gameId. Type this command with an invalid argument to get more help.")
                                    .setRequired(true))
            );
            commands.addCommands(
                    new CommandData("unfollowgame", "Stop receiving notifications about a followed game")
                            .addOptions(new OptionData(OptionType.STRING, "gameid", "A valid gameId. Type this command with an invalid argument to get more help.")
                                    .setRequired(true))
            );
            commands.addCommands(
                    new CommandData("help", "Show available commands list")
            );
            commands.queue();
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
    public void onSlashCommand(SlashCommandEvent event) {
        User author = event.getUser();
        final String command = event.getName();
        StringBuilder sb = new StringBuilder().append(command);
        switch (command) {
            case "followgame":
            case "unfollowgame":
                sb.append(" ").append(Objects.requireNonNull(event.getOption("gameid")).getAsString());
                break;
        }
        objects.User currentUser = getCurrentUser(author.getIdLong(), BotInterfaces.DISCORD);
        String answer = CommandsProcessor.processCommandFromDiscord(currentUser, sb.toString());
        if (answer != null) {
            final boolean tooLong = answer.length() > Config.discordMaxMessageLength;
            if (tooLong) {
                event.reply("*Command processed successfully*").setEphemeral(true).queue();
                // On ne passe pas par event reply à cause de la limite de 2000 caractères...
                final BotInterfaceHandler botInterfaceHandler = BotsHandler.getBotInterfaceHandler(BotInterfaces.DISCORD);
                botInterfaceHandler.sendPrivateMessage(author.getIdLong(), answer, false);
            } else {
                event.reply(answer).setEphemeral(true).queue();
            }
        }
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
