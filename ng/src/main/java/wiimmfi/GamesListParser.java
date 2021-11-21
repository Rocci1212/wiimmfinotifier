package wiimmfi;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import handlers.BotsHandler;
import handlers.interfaces.BotInterfaceHandler;
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
	private static final List<Game> games = new CopyOnWriteArrayList<>();
	private static Instant lastSuccessInstant = null;

	public static Instant getLastSuccessInstant() {
		return lastSuccessInstant;
	}

	public static Game getGameByUniqueId(String uniqueId) {
		for (Game game : getGames()) {
			if (game.getUniqueId().equals(uniqueId)) {
				return game;
			}

		}
		return null;
	}
	
	public static List<Game> getGames() {
		return games;
	}
	
	public static void warnUsers() {
		for (User user : BotsHandler.getUsers()) {
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
				final BotInterfaceHandler botInterfaceHandler = BotsHandler.getBotInterfaceHandler(user.getBotInterface());
				botInterfaceHandler.sendTo(user.getUserId(), notificationBuilder.substring(1));
			}
		}
	}
	
	public static void gamesFinishedActivityWarning() {
		for (Game game : getGames()) {
			game.setWarnPlayingActivity((short) 0);
		}
	}

	public static Document accessWiimmfiUsingFlareSolverr() throws IOException, InterruptedException {
		var values = new HashMap<String, String>() {{
			put("cmd", "request.get");
			put("url", Config.wiimmfiFullGamesListPath);
			put("session", Config.flareSolverrSession);
		}};
		var objectMapper = new ObjectMapper();
		final String requestBody = objectMapper
				.writeValueAsString(values);

		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.header("Content-Type", "application/json")
				.uri(URI.create(Config.flareSolverrUrl))
				.POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.build();

		HttpResponse<String> response = client.send(request,
				HttpResponse.BodyHandlers.ofString());

		JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
		if (!jsonObject.get("status").getAsString().equalsIgnoreCase("ok")) {
			return null;
		}
		final Document doc = Jsoup.parse(jsonObject.get("solution").getAsJsonObject().get("response").getAsString());
		doc.setBaseUri(Config.wiimmfiBaseUrl);
		return doc;
	}

	public static void parseWiimmfiGamesList() throws IOException, InterruptedException {
		final Document doc;
		if (Config.useFlareSolverr) {
			doc = accessWiimmfiUsingFlareSolverr();
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
			    getGames().add(game);
		    }
		}
		lastSuccessInstant = Instant.now();
	}
}