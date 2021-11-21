package handlers;

import handlers.interfaces.BotInterfaceHandler;
import kernel.Config;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import telegrambot.TelegramBot;
import utils.MessagesSplitter;

public class TelegramBotHandler implements BotInterfaceHandler {
	public static TelegramBotHandler instance = new TelegramBotHandler();
	private TelegramBot telegramBot = null;
	private Thread telegramBotThread;

	@Override
	public void launch() {
		if (this.telegramBotThread != null && this.telegramBotThread.isAlive()) {
			this.telegramBotThread.interrupt();
		}
		this.telegramBotThread = new Thread() {
			@Override
			public void run() {
				try {
					TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
					final TelegramBot telegramBot = new TelegramBot();
					telegramBotsApi.registerBot(telegramBot);
					setTelegramBot(telegramBot);
				} catch (final TelegramApiException e) {
					e.printStackTrace();
				}
			}
		};
		this.telegramBotThread.setName("telegramBotThread");
		this.telegramBotThread.setDaemon(true);
		this.telegramBotThread.start();
	}

	@Override
	public String getBoldText(String input) {
		return String.format("<b>%s</b>", input);
	}

	@Override
	public void sendTo(long chatId, String message) {
		final TelegramBot telegramBot = getTelegramBot();
		if (telegramBot == null) {
			return;
		}
		for (final String maxPossibleSizeMessage : MessagesSplitter.getMaximumPossibleSizeSplittedMessagesList(message, Config.telegramMaxMessageLength)) {
			// Create a message object
			final SendMessage sendMessage = new SendMessage();
			sendMessage.setChatId(String.valueOf(chatId));
			sendMessage.setText(maxPossibleSizeMessage);
			try {
				telegramBot.executeAsync(sendMessage).exceptionally((e) -> {
					System.err.println(e.getMessage());
					return null;
				});
			} catch (final TelegramApiException e) {
				System.err.println(e.getMessage());
			}
		}
	}

	public void setTelegramBot(TelegramBot telegramBot) {
		this.telegramBot = telegramBot;
	}

	public TelegramBot getTelegramBot() {
		return this.telegramBot;
	}
}
