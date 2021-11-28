package utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import handlers.BotsHandler;
import handlers.DatabaseHandler;
import handlers.interfaces.BotInterfaceHandler;
import kernel.BotInterfaces;
import kernel.Config;
import kernel.Main;
import objects.Game;
import objects.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import telegrambot.TelegramBotKeyboards;
import wiimmfi.GamesListParser;

public class CommandsProcessor {
	private final static String[] commands = { "infos", "showplayedgames", "followgame", "unfollowgame" };

	public static String processCommandFromTelegram(User currentUser, String request, SendMessage telegramSendMessage) {
		return processCommand(currentUser, request, BotInterfaces.TELEGRAM, telegramSendMessage);
	}

	public static String processCommandFromDiscord(User currentUser, String request) {
		return processCommand(currentUser, request, BotInterfaces.DISCORD, null);
	}

	private static String processCommand(User currentUser, String request, BotInterfaces botInterface, SendMessage telegramSendMessage) {
		if (currentUser == null) {
			return "Wiimmfi Notifier does not know you are. This is not your fault, please contact the developer...";
		}
		final String[] args = request.split(" ");
		final String command = args[0].toLowerCase();

		ReplyKeyboardMarkup telegramReplyKeyboardMarkup = null;
		final BotInterfaceHandler botInterfaceHandler = BotsHandler.getBotInterfaceHandler(botInterface);
		final StringBuilder answer = new StringBuilder();
		if (command.equals("infos")) {
			answer.append("Wiimmfi Notifier v" + Main.version + "\n");
			answer.append("Made with ❤️ by Azlino\n");
			answer.append("\n");
			answer.append(botInterfaceHandler.getBoldText("Stats"));
			answer.append("\n");
			answer.append("Uptime : " + Main.getUptime() + "\n");
			answer.append("Total users count (Telegram + Discord) : ");
			answer.append(BotsHandler.getUsers().size() + "\n");
			answer.append("Number of games list check this session : ");
			answer.append(Stats.checkGamesListCount.get() + "\n");
			answer.append("Number of notifications delivered this session : ");
			answer.append(Stats.notificationsIssuedCount.get() + "\n");
			final Instant lastGamesListParsingSuccessInstant = GamesListParser.getLastSuccessInstant();
			answer.append("Last success parsing of games list : ");
			if (lastGamesListParsingSuccessInstant == null) {
				answer.append("Never !");
			} else {
				DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).localizedBy(Locale.US);
				answer.append(formatter.format(ZonedDateTime.ofInstant(lastGamesListParsingSuccessInstant, ZoneId.systemDefault())));
			}
			answer.append("\n\n");
			answer.append(botInterfaceHandler.getBoldText("Followed games"));
			answer.append("\n");
			List<String> followedGamesUid = new CopyOnWriteArrayList<>(currentUser.getFollowedGamesUid());
			if (followedGamesUid.isEmpty()) {
				answer.append("You don't follow any games");
			} else {
				for (int i = 0; i < followedGamesUid.size(); i++) {
					String gameUid = followedGamesUid.get(i);
					answer.append(gameUid);
					if (i + 1 < followedGamesUid.size()) {
						answer.append(", ");
					}
				}
			}
		} else if (command.equals("showplayedgames")) {
			List<Game> playedGames = new ArrayList<>();
			for (Game game : GamesListParser.getGames()) {
				if (game.getOnlineCount() > 0) {
					playedGames.add(game);
				}
			}
			if (playedGames.isEmpty()) {
				answer.append("No games are currently played on Wiimmfi !");
			} else {
				answer.append(botInterfaceHandler.getBoldText("Currently played games on Wiimmfi"));
				for (Game game : playedGames) {
					answer.append("\n- " + game.getType() + " " + game.getProductionName());
					answer.append(" (" + game.getOnlineCount() + " online)");
				}
			}
		} else if (command.equals("followgame")) {
			List<Game> notFollowedGames = currentUser.getNotFollowedGames();
			if (notFollowedGames.isEmpty()) {
				answer.append("Error : You are already following all games !");
			} else {
				try {
					String gameUid = args[1].toUpperCase();
					if (args[1].equals("all")) {
						for (Game notFollowedGame : notFollowedGames) {
							if (!currentUser.getFollowedGamesUid().contains(notFollowedGame.getUniqueId())) {
								currentUser.getFollowedGamesUid().add(notFollowedGame.getUniqueId());
								DatabaseHandler.addUserFollowedGame(currentUser.getUserId(), notFollowedGame.getUniqueId(), botInterface);
							}
						}
						answer.append("You are now following the activity of all Wiimmfi games !");
						Main.printNewEvent("User " + currentUser.getUserId() + " follow all games", true);
					} else {
						Game game = GamesListParser.getGameByUniqueId(gameUid);
						if (game == null) {
							throw new Exception();
						}
						if (!currentUser.getFollowedGamesUid().contains(game.getUniqueId())) {
							currentUser.getFollowedGamesUid().add(game.getUniqueId());
							DatabaseHandler.addUserFollowedGame(currentUser.getUserId(), game.getUniqueId(), botInterface);
							answer.append("You are now following the activity of the game : " + game.getProductionName());
							Main.printNewEvent("User " + currentUser.getUserId() + " follow " + game.getUniqueId(), true);
						} else {
							answer.append("Error : You follow already this game !");
						}
					}
				} catch (Exception e) {
					answer.append("Usage : followgame [gameId]\n");
					answer.append("Example for the game Bomberman Blitz : followgame KBBJ\n");
					answer.append("\nIdentifiers of every games can be found there : " + Config.wiimmfiFullGamesListPath + "\n");
					answer.append("\nTip : You can write \"followgame all\" to follow instantly the activity of all Wiimmfi games !");
				}
			}
		} else if (command.equals("unfollowgame")) {
			List<Game> followedGames = currentUser.getFollowedGames();
			if (followedGames.isEmpty()) {
				answer.append("Error : You are not following any games !");
			} else {
				try {
					String gameUid = args[1].toUpperCase();
					if (args[1].equals("all")) {
						currentUser.getFollowedGamesUid().clear();
						for (Game followedGame : followedGames) {
							DatabaseHandler.deleteUserFollowedGame(currentUser.getUserId(), followedGame.getUniqueId(), botInterface);
						}
						answer.append("You are not following the activity of any games anymore");
						Main.printNewEvent("User " + currentUser.getUserId() + " unfollow all games", true);
					} else {
						Game game = GamesListParser.getGameByUniqueId(gameUid);
						if (game == null) {
							throw new Exception();
						}
						if (currentUser.getFollowedGamesUid().contains(game.getUniqueId())) {
							currentUser.getFollowedGamesUid().remove(game.getUniqueId());
							DatabaseHandler.deleteUserFollowedGame(currentUser.getUserId(), game.getUniqueId(), botInterface);
							answer.append("You are not following anymore the activity of the game : " + game.getProductionName());
							Main.printNewEvent("User " + currentUser.getUserId() + " unfollow " + game.getUniqueId(), true);
						} else {
							answer.append("Error : You were not following this game !");
						}
					}
				} catch (Exception e) {
					answer.append("Usage : unfollowgame [gameId]\n");
					answer.append("Example for the game Bomberman Blitz : unfollowgame KBBJ\n");
					answer.append("\nIdentifiers of every games can be found there : " + Config.wiimmfiFullGamesListPath + "\n");
					answer.append("\nTip : You can write \"unfollowgame all\" to unfollow you from all Wiimmfi games at once !");
				}
			}
		} else {
			answer.append(botInterfaceHandler.getBoldText("Available commands"));
			for (String availableCommand : getCommands()) {
				answer.append("\n" + availableCommand);
			}
			telegramReplyKeyboardMarkup = TelegramBotKeyboards.getCommandsKeyboard();
		}
		if (answer.length() == 0) {
			return null;
		} else {
			final String answerText = answer.toString();
			if (telegramSendMessage != null) {
				if (telegramReplyKeyboardMarkup != null) {
					telegramSendMessage.setReplyMarkup(telegramReplyKeyboardMarkup);
				}
			}
			return answerText;
		}
	}

	public static String[] getCommands() {
		return commands;
	}
}
