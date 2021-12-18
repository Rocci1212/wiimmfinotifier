package objects;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import wiimmfi.GamesListParser;

public class User {
	private final Set<String> followedGamesUid;

	public User() {
		this.followedGamesUid = ConcurrentHashMap.newKeySet();
	}

	public boolean isFollowingGame(String gameUniqueId) {
		return followedGamesUid.contains(gameUniqueId);
	}
	
	public Map<String, Game> getFollowableGames() {
		Map<String, Game> notFollowedGames = new HashMap<>();
		for (Map.Entry<String, Game> entry : GamesListParser.getGames().entrySet()) {
			if (!isFollowingGame(entry.getKey())) {
				notFollowedGames.put(entry.getKey(), entry.getValue());
			}
		}
		return notFollowedGames;
	}
	
	public Set<String> getFollowedGamesUid() {
		return followedGamesUid;
	}
}