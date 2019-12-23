package telegrambot;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

public class TelegramBotKeyboards {
	
	public static ReplyKeyboardMarkup getCommandsKeyboard() {
		final ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
		replyKeyboardMarkup.setResizeKeyboard(true);
		replyKeyboardMarkup.setOneTimeKeyboard(true);
		final List<KeyboardRow> keyboard = new ArrayList<>();
		for (final String command : TelegramBotCommands.getCommands()) {
			final KeyboardRow keyboardRow = new KeyboardRow();
			keyboardRow.add(command);
			keyboard.add(keyboardRow);
		}
		replyKeyboardMarkup.setKeyboard(keyboard);
		return replyKeyboardMarkup;
	}
}
