package objects;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import wiimmfi.GamesListParser;

public class User {
	private final List<String> followedGamesUid = new CopyOnWriteArrayList<>();

	public User() {
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
}