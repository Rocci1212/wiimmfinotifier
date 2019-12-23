package handlers;

import java.util.concurrent.CopyOnWriteArrayList;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import objects.User;
import telegrambot.TelegramBot;

public class TelegramBotHandler {
	private static CopyOnWriteArrayList<User> users = new CopyOnWriteArrayList<>();
	private static TelegramBot telegramBot = null;
	private static Thread telegramBotThread;
	
	private static class SendTo extends Thread {
		private long chatId;
		private String message;

		public SendTo(String str, long chatId, String message) {
			super(str);
			this.chatId = chatId;
			this.message = message;
		}

		@Override
		public void run() {
			if (TelegramBotHandler.getTelegramBot() != null) {
				// Create a message object
				final SendMessage sendMessage = new SendMessage();
				sendMessage.setChatId(chatId).setText(message);
				try {
					TelegramBotHandler.getTelegramBot().execute(sendMessage);
				} catch (final TelegramApiException e) {
					e.printStackTrace();
				}
			}
			TelegramBotHandler.stopThread(this);
		}
	}

	public static void launch() {
		if (TelegramBotHandler.telegramBotThread != null && TelegramBotHandler.telegramBotThread.isAlive()) {
			TelegramBotHandler.telegramBotThread.interrupt();
		}
		TelegramBotHandler.telegramBotThread = new Thread() {
			@Override
			public void run() {
				// Initialize Api Context
				ApiContextInitializer.init();

				// Instantiate Telegram Bots API
				final TelegramBotsApi botsApi = new TelegramBotsApi();

				// Register our bot
				try {
					final TelegramBot telegramBot = new TelegramBot();
					botsApi.registerBot(telegramBot);
					TelegramBotHandler.setTelegramBot(telegramBot);
				} catch (final TelegramApiException e) {
					e.printStackTrace();
				}
				TelegramBotHandler.stopThread(this);
			}
		};
		TelegramBotHandler.telegramBotThread.setName("telegramBotThread");
		TelegramBotHandler.telegramBotThread.setDaemon(true);
		TelegramBotHandler.telegramBotThread.start();
	}

	public static void sendTo(long chatId, String message) {
		final Thread thread = new SendTo("sendTo", chatId, message);
		thread.setDaemon(true);
		thread.start();
	}

	public static void setTelegramBot(TelegramBot telegramBot) {
		TelegramBotHandler.telegramBot = telegramBot;
	}

	private synchronized static void stopThread(Thread thread) {
		if (thread != null) {
			thread = null;
		}
	}

	public static TelegramBot getTelegramBot() {
		return TelegramBotHandler.telegramBot;
	}

	public static CopyOnWriteArrayList<User> getUsers() {
		return users;
	}

	public static void setUsers(CopyOnWriteArrayList<User> users) {
		TelegramBotHandler.users = users;
	}
	
	public static User getUser(long userId) {
		for (User user : new CopyOnWriteArrayList<>(getUsers())) {
			if (user.getUserId() == userId) {
				return user;
			}
		}
		return null;
	}
}
