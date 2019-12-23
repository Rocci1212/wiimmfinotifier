package telegrambot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import handlers.DatabaseHandler;
import handlers.TelegramBotHandler;
import kernel.Config;
import kernel.Main;
import objects.User;

public class TelegramBot extends TelegramLongPollingBot {
	
	@Override
	public String getBotToken() {
		return Config.telegramBotToken;
	}

	@Override
	public String getBotUsername() {
		return null;
	}

	@Override
	public void onUpdateReceived(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
			// Set variables
			final String message_text = update.getMessage().getText();
			final long chat_id = update.getMessage().getChatId();
			User currentUser = TelegramBotHandler.getUser(chat_id);
			if (currentUser == null) {
				currentUser = new User(chat_id);
				TelegramBotHandler.getUsers().add(currentUser);
				DatabaseHandler.addUser(chat_id);
				Main.printNewEvent("User creation : " + chat_id, true);
			}
			SendMessage answerMessage = TelegramBotCommands.processCommand(currentUser, message_text);
			if (answerMessage != null) {
				answerMessage.enableHtml(true);
				answerMessage.setChatId(chat_id);
				try {
					// Sending our message object to the user
					execute(answerMessage);
				} catch (final TelegramApiException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
