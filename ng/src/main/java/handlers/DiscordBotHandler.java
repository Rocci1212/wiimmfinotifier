package handlers;

import handlers.interfaces.BotInterfaceHandler;
import kernel.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import utils.MessagesSplitter;

public class DiscordBotHandler implements BotInterfaceHandler {
	public static DiscordBotHandler instance = new DiscordBotHandler();
	private JDA discordBot = null;
	private Thread discordBotThread;

	@Override
	public void sendTo(long userId, String message) {
		final JDA discordBot = getDiscordBot();
		if (discordBot == null) {
			return;
		}
		for (final String maxPossibleSizeMessage : MessagesSplitter.getMaximumPossibleSizeSplittedMessagesList(message, Config.discordMaxMessageLength)) {
			net.dv8tion.jda.api.entities.User user = discordBot.getUserById(userId);
			if (user != null) {
				user.openPrivateChannel().queue((channel) -> {
					channel.sendMessage(maxPossibleSizeMessage).queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
				});
			}
		}
	}

	@Override
	public void launch() {
		if (this.discordBotThread != null && this.discordBotThread.isAlive()) {
			this.discordBotThread.interrupt();
		}
		this.discordBotThread = new Thread() {
			@Override
			public void run() {
				discordbot.MessageListener.launch();
			}
		};
		this.discordBotThread.setName("discordBotThread");
		this.discordBotThread.setDaemon(true);
		this.discordBotThread.start();
	}

	@Override
	public String getBoldText(String input) {
		return String.format("***%s***", input);
	}
	
	public JDA getDiscordBot() {
		return this.discordBot;
	}

	public void setDiscordBot(JDA discordBot) {
		this.discordBot = discordBot;
	}
}