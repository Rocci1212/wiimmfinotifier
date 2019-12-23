package handlers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import kernel.Config;
import kernel.Main;
import objects.User;

public class DatabaseHandler {
	private static Connection connection;

	public synchronized static void loadUsers() {
		int i = 0;
		try {
			final String query = "SELECT * FROM `users`;";
			final ResultSet resultset = DatabaseHandler.executeQuery(query);
			while (resultset.next()) {
				User user = new User(resultset.getLong("userId"));
				DiscordBotHandler.getUsers().add(user);
				i++;
			}
			DatabaseHandler.closeResultSet(resultset);
			Main.printNewEvent(i + " user(s) loaded from database", false);
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized static void loadUsersFollowedGames() {
		int i = 0;
		try {
			final String query = "SELECT * FROM `users_followedgames`;";
			final ResultSet resultset = DatabaseHandler.executeQuery(query);
			while (resultset.next()) {
				User user = DiscordBotHandler.getUser(resultset.getLong("userId"));
				if (user != null) {
					user.getFollowedGamesUid().add(resultset.getString("gameUid"));
					i++;
				}
			}
			DatabaseHandler.closeResultSet(resultset);
			Main.printNewEvent(i + " user(s) followed games loaded from database", false);
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized static void addUser(long userId) {
		final String query = "INSERT INTO `users` VALUES (?);";
		try {
			final PreparedStatement p = DatabaseHandler.newTransact(query);
			p.setLong(1, userId);
			p.execute();
			DatabaseHandler.closePreparedStatement(p);
		} catch (final SQLException e) {
			System.err.println("SQL Exception " + e.getMessage());
		}
	}
	
	public synchronized static void addUserFollowedGame(long userId, String gameId) {
		final String query = "INSERT INTO `users_followedgames` VALUES (?,?);";
		try {
			final PreparedStatement p = DatabaseHandler.newTransact(query);
			p.setLong(1, userId);
			p.setString(2, gameId);
			p.execute();
			DatabaseHandler.closePreparedStatement(p);
		} catch (final SQLException e) {
			System.err.println("SQL Exception " + e.getMessage());
		}
	}
	
	public synchronized static void deleteUserFollowedGame(long userId, String gameId) {
		final String query = "DELETE FROM `users_followedgames` WHERE userId = ? AND gameUid = ?";
		try {
			final PreparedStatement p = DatabaseHandler.newTransact(query);
			p.setLong(1, userId);
			p.setString(2, gameId);
			p.execute();
			DatabaseHandler.closePreparedStatement(p);
		} catch (final SQLException e) {
			System.err.println("SQL Exception " + e.getMessage());
		}
	}
	
	public synchronized static PreparedStatement newTransact(String basequery) throws SQLException {
		checkConnection();
		return DatabaseHandler.connection.prepareStatement(basequery);
	}

	public synchronized static void closeCons() {
		try {
			DatabaseHandler.connection.close();
		} catch (final Exception e) {
			System.err.println("SQL Exception " + e.getMessage());
		}
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
	
	public synchronized static ResultSet executeQuery(String query) {
		checkConnection();
		try {
			Statement stat = DatabaseHandler.connection.createStatement();
			final ResultSet resultSet = stat.executeQuery(query);
			stat.setQueryTimeout(20);
			return resultSet;
		} catch (SQLException e) {
			System.err.println("SQLException " + e.getMessage());
		}
		return null;
	}
	
	public synchronized static void checkConnection() {
		while (true) {
			try {
				if (!DatabaseHandler.connection.isValid(1000)) throw new SQLException();
			} catch (SQLException e1) {
				Main.printNewEvent("Database connection has been lost, auto reconnecting in 1s", false);
				try {
					Thread.sleep(1000);
					if (!setUpConnexion()) { // Reconnect
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
	
	public synchronized static final boolean setUpConnexion() {
		try {
			DatabaseHandler.connection = DriverManager.getConnection("jdbc:mysql://" + Config.dbHost + "/" + Config.dbName, Config.dbUser, Config.dbPass);
			DatabaseHandler.connection.setAutoCommit(true);
			if (!DatabaseHandler.connection.isValid(1000)) {
				Main.printNewEvent("Connection to the database : failed (invalid)", false);
				return false;
			}
			Main.printNewEvent("Connection to the database : ok", false);
			return true;
		} catch (final SQLException e) {
			Main.printNewEvent("Connection to the database : failed (" + e.getMessage() + ")", false);
			return false;
		}
	}
	
	public static Connection getConnection() {
		return connection;
	}
}