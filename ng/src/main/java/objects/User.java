package objects;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import kernel.BotInterfaces;
import wiimmfi.GamesListParser;

public class User {
	private final long userId;
	private final List<String> followedGamesUid = new CopyOnWriteArrayList<>();
	private final BotInterfaces botInterface;

	public User(long userId, BotInterfaces botInterface) {
		this.userId = userId;
		this.botInterface = botInterface;
	}

	public long getUserId() {
		return userId;
	}

	public boolean isGameFollowed(String gameUniqueId) {
		for (String uId : getFollowedGamesUid()) {
			if (uId.equals(gameUniqueId)) {
				return true;
			}
		}
		return false;
	}
	
	public List<Game> getNotFollowedGames() {
		List<Game> notFollowedGames = new ArrayList<>();
		for (Game game : GamesListParser.getGames().values()) {
			if (!isGameFollowed(game.getUniqueId())) {
				notFollowedGames.add(game);
			}
		}
		return notFollowedGames;
	}
	
	public List<Game> getFollowedGames() {
		List<Game> followedGames = new ArrayList<>();
		for (Game game : GamesListParser.getGames().values()) {
			if (isGameFollowed(game.getUniqueId())) {
				followedGames.add(game);
			}
		}
		return followedGames;
	}
	
	public List<String> getFollowedGamesUid() {
		return followedGamesUid;
	}

	public BotInterfaces getBotInterface() {
		return botInterface;
	}
}