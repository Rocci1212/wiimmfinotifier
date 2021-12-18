package wiimmfi;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import handlers.BotsHandler;
import handlers.interfaces.BotInterfaceHandler;
import kernel.BotInterfaces;
import kernel.Config;
import kernel.Main;
import objects.Game;
import objects.User;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GamesListParser {
	private static final Map<String, Game> games = new ConcurrentHashMap<>();
	private static Instant lastSuccessInstant = null;

	public static Instant getLastSuccessInstant() {
		return lastSuccessInstant;
	}

	public static Game getGameByUniqueId(String uniqueId) {
		return games.get(uniqueId);
	}
	
	public static Map<String, Game> getGames() {
		return games;
	}
	
	public static void warnUsers() {
		for (BotInterfaces botInterface : BotInterfaces.values()) {
			for (Map.Entry<Long, User> entry : BotsHandler.getUsers(botInterface).entrySet()) {
				final long userId = entry.getKey();
				final User user = entry.getValue();
				StringBuilder notificationBuilder = new StringBuilder();
				for (Game game : getGames().values()) {
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
					final BotInterfaceHandler botInterfaceHandler = BotsHandler.getBotInterfaceHandler(botInterface);
					botInterfaceHandler.sendPrivateMessage(userId, notificationBuilder.substring(1), true);
				}
			}
		}
	}
	
	public static void gamesFinishedActivityWarning() {
		for (Game game : getGames().values()) {
			game.setWarnPlayingActivity((short) 0);
		}
	}

	public static void parseWiimmfiGamesList() throws IOException, InterruptedException {
		final Document doc;
		if (Config.useFlareSolverr) {
			doc = FlareSolverrGatewayManager.instance.accessWiimmfiUsingFlareSolverr();
		} else {
			// Accès direct
			final Connection connection = Jsoup.connect(Config.wiimmfiFullGamesListPath);
			connection.userAgent("Wiimmfi Notifier by Azlino v" + Main.version);
			doc = connection.get();
		}
		if (doc == null) {
			return;
		}
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
			    getGames().put(uniqueId, game);
		    }
		}
		lastSuccessInstant = Instant.now();
	}
}