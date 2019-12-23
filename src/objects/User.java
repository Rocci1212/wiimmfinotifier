package objects;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import wiimmfi.GamesListParser;

public class User {
	private long userId;
	private CopyOnWriteArrayList<String> followedGamesUid = new CopyOnWriteArrayList<>();

	public User(long userId) {
		setUserId(userId);
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}
	
	public boolean isGameFollowed(String gameUniqueId) {
		for (String uId : new CopyOnWriteArrayList<>(getFollowedGamesUid())) {
			if (uId.equals(gameUniqueId)) {
				return true;
			}
		}
		return false;
	}
	
	public ArrayList<Game> getNotFollowedGames() {
		ArrayList<Game> notFollowedGames = new ArrayList<>();
		for (Game game : new CopyOnWriteArrayList<>(GamesListParser.getGames())) {
			if (!isGameFollowed(game.getUniqueId())) {
				notFollowedGames.add(game);
			}
		}
		return notFollowedGames;
	}
	
	public ArrayList<Game> getFollowedGames() {
		ArrayList<Game> followedGames = new ArrayList<>();
		for (Game game : new CopyOnWriteArrayList<>(GamesListParser.getGames())) {
			if (isGameFollowed(game.getUniqueId())) {
				followedGames.add(game);
			}
		}
		return followedGames;
	}
	
	public CopyOnWriteArrayList<String> getFollowedGamesUid() {
		return followedGamesUid;
	}

	public void setFollowedGamesUid(CopyOnWriteArrayList<String> followedGamesUid) {
		this.followedGamesUid = followedGamesUid;
	}
}