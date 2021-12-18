package telegrambot;

import handlers.BotsHandler;
import handlers.DatabaseHandler;
import kernel.BotInterfaces;
import kernel.Config;
import kernel.Main;
import objects.User;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import utils.CommandsProcessor;
import utils.MessagesSplitter;

public class TelegramBot extends TelegramLongPollingBot {
	
	@Override
	public String getBotToken() {
		return Config.telegramBotToken;
	}

	@Override
	public String getBotUsername() {
		return "Wiimmfi Notifier";
	}

	@Override
	public void onUpdateReceived(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
			// Set variables
			final String message_text = update.getMessage().getText();
			final long chat_id = update.getMessage().getChatId();
			User currentUser = BotsHandler.getUser(chat_id, BotInterfaces.TELEGRAM);
			if (currentUser == null) {
				currentUser = new User();
				BotsHandler.getUsers(BotInterfaces.TELEGRAM).put(chat_id, currentUser);
				DatabaseHandler.addUser(chat_id, BotInterfaces.TELEGRAM);
				Main.printNewEvent("User creation : " + chat_id, true, BotInterfaces.TELEGRAM);
			}
			final SendMessage answerMessage = new SendMessage();
			final String answerText = CommandsProcessor.processCommandFromTelegram(chat_id, currentUser, message_text, answerMessage);
			if (answerText != null) {
				for (final String maxPossibleSizeMessage : MessagesSplitter.getMaximumPossibleSizeSplittedMessagesList(answerText, Config.discordMaxMessageLength)) {
					final SendMessage maxPossibleAnswerMessage = new SendMessage();
					maxPossibleAnswerMessage.setText(maxPossibleSizeMessage);
					maxPossibleAnswerMessage.enableHtml(true);
					maxPossibleAnswerMessage.setReplyMarkup(answerMessage.getReplyMarkup());
					maxPossibleAnswerMessage.setChatId(String.valueOf(chat_id));
					try {
						// Sending our message object to the user
						execute(maxPossibleAnswerMessage);
					} catch (final TelegramApiException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
