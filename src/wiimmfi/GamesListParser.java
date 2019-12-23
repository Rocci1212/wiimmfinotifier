package wiimmfi;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import handlers.DiscordBotHandler;
import kernel.Config;
import kernel.Main;
import objects.Game;
import objects.User;

public class GamesListParser {
	private static CopyOnWriteArrayList<Game> games = new CopyOnWriteArrayList<>();
	
	public static Game getGameByUniqueId(String uniqueId) {
		for (Game game : new CopyOnWriteArrayList<>(getGames())) {
			if (game.getUniqueId().equals(uniqueId)) {
				return game;
			}
		}
		return null;
	}
	
	public static CopyOnWriteArrayList<Game> getGames() {
		return games;
	}
	
	public static void warnUsers() {
		for (User user : new CopyOnWriteArrayList<>(DiscordBotHandler.getUsers())) {
			StringBuilder notificationBuilder = new StringBuilder();
			for (Game game : getGames()) {
				if (user.isGameFollowed(game.getUniqueId())) {
					switch (game.getWarnPlayingActivity()) {
					case 1:
						notificationBuilder.append("\n- " + game.getType() + " " + game.getProductionName() + " is now played");
						notificationBuilder.append(" (" + game.getOnlineCount() + " online)");
						break;
					case 2:
						notificationBuilder.append("\n- " + game.getType() + " " + game.getProductionName() + " is not played anymore");
						break;
					default:
						break;
					}
				}
			}
			if (notificationBuilder.length() > 0) {
				DiscordBotHandler.sendTo(user.getUserId(), notificationBuilder.substring(1));
			}
		}
	}
	
	public static void gamesFinishedActivityWarning() {
		for (Game game : getGames()) {
			game.setWarnPlayingActivity((short) 0);
		}
	}
	
	public static void parseWiimmfiGamesList() throws IOException {
		final Connection connection = Jsoup.connect(Config.wiimmfiFullGamesListPath);
		connection.userAgent("Wiimmfi Notifier by Azlino v" + Main.version);
		Document doc = connection.get();
		Element table = doc.getElementById("game");
		Elements rows = table.select("tr");
		for (int i = 2; i < rows.size() - 1; i++) {
		    Elements row = rows.get(i).getElementsByTag("td");
		    String uniqueId = row.get(0).text();
		    String rawOnline = row.get(4).text();
		    int onlineCount = 0;
		    if (!rawOnline.equals("—")) {
		    	onlineCount = Integer.parseInt(rawOnline);
		    }
		    Game existingGame = GamesListParser.getGameByUniqueId(uniqueId);
		    if (existingGame != null) {
		    	int previousOnlineCount = existingGame.getOnlineCount();
		    	if (previousOnlineCount == 0 && onlineCount > 0) {
		    		existingGame.setWarnPlayingActivity((short) 1);
		    	} else if (previousOnlineCount > 0 && onlineCount == 0) {
		    		// Not played anymore
		    		existingGame.setWarnPlayingActivity((short) 2);
		    	}
		    	existingGame.setOnlineCount(onlineCount);
		    } else {
			    String productionName = row.get(1).text();
			    String typeLink = row.get(1).getElementsByTag("img").first().absUrl("src");
			    String type;
			    if (typeLink.equals(Config.wiimmfiNDSTypeImagePath)) {
			    	type = "NDS";
			    } else if (typeLink.equals(Config.wiimmfiDSIWARETypeImagePath)) {
			    	type = "DSiWare";
			    } else if (typeLink.equals(Config.wiimmfiWIITypeImagePath)) {
			    	type = "Wii";
			    } else if (typeLink.equals(Config.wiimmfiWIIWARETypeImagePath)) {
			    	type = "WiiWare";
			    } else {
			    	type = "Unknown";
			    }
			    Game game = new Game(uniqueId, productionName, type, onlineCount);
			    getGames().add(game);
		    }
		}
	}
}