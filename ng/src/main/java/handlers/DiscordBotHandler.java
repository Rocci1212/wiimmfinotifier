package handlers;

import java.util.concurrent.CopyOnWriteArrayList;

import net.dv8tion.jda.api.JDA;
import objects.User;

public class DiscordBotHandler {
	private static CopyOnWriteArrayList<User> users = new CopyOnWriteArrayList<>();
	private static JDA discordBot = null;
	private static Thread discordBotThread;

	private static class SendTo extends Thread {
		private long userId;
		private String message;

		public SendTo(String str, long userId, String message) {
			super(str);
			this.userId = userId;
			this.message = message;
		}

		@Override
		public void run() {
			try {
				if (DiscordBotHandler.getDiscordBot() != null) {
					net.dv8tion.jda.api.entities.User user = DiscordBotHandler.getDiscordBot().getUserById(userId);
					if (user != null) {
				        user.openPrivateChannel().queue((channel) -> {
				            channel.sendMessage(message).queue();
				        });
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			DiscordBotHandler.stopThread(this);
		}
	}
	
	public static void sendTo(long userId, String message) {
		final Thread thread = new SendTo("sendTo", userId, message);
		thread.setDaemon(true);
		thread.start();
	}
	
	public static CopyOnWriteArrayList<User> getUsers() {
		return users;
	}

	public static void setUsers(CopyOnWriteArrayList<User> users) {
		DiscordBotHandler.users = users;
	}
	
	public static User getUser(long userId) {
		for (User user : new CopyOnWriteArrayList<>(getUsers())) {
			if (user.getUserId() == userId) {
				return user;
			}
		}
		return null;
	}
	
	public static void launch() {
		if (DiscordBotHandler.discordBotThread != null && DiscordBotHandler.discordBotThread.isAlive()) {
			DiscordBotHandler.discordBotThread.interrupt();
		}
		DiscordBotHandler.discordBotThread = new Thread() {
			@Override
			public void run() {
				discordbot.MessageListener.launch();
				DiscordBotHandler.stopThread(this);
			}
		};
		DiscordBotHandler.discordBotThread.setName("discordBotThread");
		DiscordBotHandler.discordBotThread.setDaemon(true);
		DiscordBotHandler.discordBotThread.start();
	}

	private static synchronized void stopThread(Thread thread) {
		if (thread != null) {
			thread = null;
		}
	}
	
	public static JDA getDiscordBot() {
		return discordBot;
	}

	public static void setDiscordBot(JDA discordBot) {
		DiscordBotHandler.discordBot = discordBot;
	}
}