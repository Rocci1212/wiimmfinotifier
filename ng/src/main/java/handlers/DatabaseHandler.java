package handlers;

import java.sql.*;
import java.util.EnumMap;
import java.util.Map;

import kernel.BotInterfaces;
import kernel.Config;
import kernel.Main;
import objects.User;

public class DatabaseHandler {
	private static Map<BotInterfaces, Connection> connections = new EnumMap<>(BotInterfaces.class);

	public synchronized static void loadUsers(BotInterfaces botInterface) {
		int i = 0;
		try {
			final String query = "SELECT * FROM `users`;";
			final ResultSet resultset = DatabaseHandler.executeQuery(query, botInterface);
			while (resultset.next()) {
				User user = new User(resultset.getLong("userId"), botInterface);
				BotsHandler.getUsers().add(user);
				i++;
			}
			DatabaseHandler.closeResultSet(resultset);
			Main.printNewEvent(i + " user(s) loaded from database", false, botInterface);
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized static void loadUsersFollowedGames(BotInterfaces botInterface) {
		int i = 0;
		try {
			final String query = "SELECT * FROM `users_followedgames`;";
			final ResultSet resultset = DatabaseHandler.executeQuery(query, botInterface);
			while (resultset.next()) {
				User user = BotsHandler.getUser(resultset.getLong("userId"), botInterface);
				if (user != null) {
					user.getFollowedGamesUid().add(resultset.getString("gameUid"));
					i++;
				}
			}
			DatabaseHandler.closeResultSet(resultset);
			Main.printNewEvent(i + " user(s) followed games loaded from database", false, botInterface);
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized static void addUser(long userId, BotInterfaces botInterface) {
		final String query = "INSERT INTO `users` VALUES (?);";
		try {
			final PreparedStatement p = DatabaseHandler.newTransact(query, botInterface);
			p.setLong(1, userId);
			p.execute();
			DatabaseHandler.closePreparedStatement(p);
		} catch (final SQLException e) {
			System.err.println("SQL Exception " + e.getMessage());
		}
	}
	
	public synchronized static void addUserFollowedGame(long userId, String gameId, BotInterfaces botInterface) {
		final String query = "INSERT INTO `users_followedgames` VALUES (?,?);";
		try {
			final PreparedStatement p = DatabaseHandler.newTransact(query, botInterface);
			p.setLong(1, userId);
			p.setString(2, gameId);
			p.execute();
			DatabaseHandler.closePreparedStatement(p);
		} catch (final SQLException e) {
			System.err.println("SQL Exception " + e.getMessage());
		}
	}
	
	public synchronized static void deleteUserFollowedGame(long userId, String gameId, BotInterfaces botInterface) {
		final String query = "DELETE FROM `users_followedgames` WHERE userId = ? AND gameUid = ?";
		try {
			final PreparedStatement p = DatabaseHandler.newTransact(query, botInterface);
			p.setLong(1, userId);
			p.setString(2, gameId);
			p.execute();
			DatabaseHandler.closePreparedStatement(p);
		} catch (final SQLException e) {
			System.err.println("SQL Exception " + e.getMessage());
		}
	}
	
	public synchronized static PreparedStatement newTransact(String basequery, BotInterfaces botInterface) throws SQLException {
		checkConnection(botInterface);
		return getConnection(botInterface).prepareStatement(basequery);
	}

	public synchronized static void closePreparedStatement(PreparedStatement p) {
		try {
			p.clearParameters();
			p.close();
		} catch (final SQLException e) {
			System.err.println("SQL Exception " + e.getMessage());
		}
	}

	public synchronized static void closeResultSet(ResultSet resultset) {
		try {
			resultset.getStatement().close();
			resultset.close();
		} catch (final SQLException e) {
			System.err.println("SQL Exception " + e.getMessage());
		}
	}
	
	public synchronized static ResultSet executeQuery(String query, BotInterfaces botInterface) {
		checkConnection(botInterface);
		try {
			Statement stat = getConnection(botInterface).createStatement();
			final ResultSet resultSet = stat.executeQuery(query);
			stat.setQueryTimeout(20);
			return resultSet;
		} catch (SQLException e) {
			System.err.println("SQLException " + e.getMessage());
		}
		return null;
	}
	
	public synchronized static void checkConnection(BotInterfaces botInterface) {
		while (true) {
			try {
				if (!getConnection(botInterface).isValid(1000)) throw new SQLException();
			} catch (SQLException e1) {
				Main.printNewEvent("Database connection has been lost, auto reconnecting in 1s", false, botInterface);
				try {
					Thread.sleep(1000);
					if (!setUpConnexion(botInterface)) { // Reconnect
						// Failed
						continue;
					}
				} catch (InterruptedException e2) {
					e2.printStackTrace();
				}
			}
			break;
		}
	}
	
	public synchronized static boolean setUpConnexion(final BotInterfaces botInterface) {
		try {
			final String dbName = botInterface == BotInterfaces.DISCORD ? Config.dbNameDiscord : Config.dbNameTelegram;
			final Connection connection = DriverManager.getConnection("jdbc:mysql://" + Config.dbHost + "/" + dbName, Config.dbUser, Config.dbPass);
			connection.setAutoCommit(true);
			if (!connection.isValid(1000)) {
				Main.printNewEvent("Connection to the database : failed (invalid)", false, botInterface);
				return false;
			}
			connections.put(botInterface, connection);
			Main.printNewEvent("Connection to the database : ok", false, botInterface);
			return true;
		} catch (final SQLException e) {
			Main.printNewEvent("Connection to the database : failed (" + e.getMessage() + ")", false, botInterface);
			return false;
		}
	}
	
	public static Connection getConnection(BotInterfaces botInterface) {
		return connections.get(botInterface);
	}
}