package handlers;

import java.sql.*;
import java.util.EnumMap;
import java.util.Map;

import com.zaxxer.hikari.HikariDataSource;
import kernel.BotInterfaces;
import kernel.Config;
import kernel.Main;
import objects.User;

public class DatabaseHandler {
	private static final Map<BotInterfaces, HikariDataSource> DATA_SOURCES = new EnumMap<>(BotInterfaces.class);

	public static void initBotInterfaceDataSource(BotInterfaces botInterface) {
		// Datasource config
		final String DBName = botInterface == BotInterfaces.DISCORD ? Config.DBNameDiscord : Config.DBNameTelegram;
		final HikariDataSource ds = new HikariDataSource();
		ds.setDriverClassName(Config.DBDriverClassName);
		ds.setJdbcUrl(Config.DBBaseJDBCUrl + Config.DBHost + ":" + Config.DBPort + "/" + DBName);
		ds.addDataSourceProperty("user", Config.DBUser);
		ds.addDataSourceProperty("password", Config.DBPass);
		ds.addDataSourceProperty("cachePrepStmts", true);
		ds.addDataSourceProperty("prepStmtCacheSize", 250);
		ds.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
		ds.addDataSourceProperty("useServerPrepStmts", true);
		ds.addDataSourceProperty("useLocalSessionState", true);
		ds.addDataSourceProperty("rewriteBatchedStatements", true);
		ds.addDataSourceProperty("cacheResultSetMetadata", true);
		ds.addDataSourceProperty("cacheServerConfiguration", true);
		ds.addDataSourceProperty("elideSetAutoCommits", true);
		ds.addDataSourceProperty("maintainTimeStats", false);

		// Pool datasource config
		ds.setPoolName(DBName);
		ds.setMaximumPoolSize(10);
		ds.setAutoCommit(true);
		ds.setConnectionTimeout(3000); // 3s
		final int secondsTtlToMs = Config.DBConnectionSecondsTtl * 1000;
		ds.setMaxLifetime(secondsTtlToMs);
		DATA_SOURCES.put(botInterface, ds);
	}

	public synchronized static void loadUsers(BotInterfaces botInterface) {
		int i = 0;
		final String query = "SELECT * FROM `users`;";
		try (Connection con = DatabaseHandler.getConnection(botInterface);
			 ResultSet RS = DatabaseHandler.executeQuery(query, con)) {
			while (RS.next()) {
				final long userId = RS.getLong("userId");
				User user = new User();
				BotsHandler.getUsers(botInterface).put(userId, user);
				i++;
			}
			Main.printNewEvent(i + " user(s) loaded from database", false, botInterface);
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized static void loadUsersFollowedGames(BotInterfaces botInterface) {
		int i = 0;
		final String query = "SELECT * FROM `users_followedgames`;";
		try (Connection con = DatabaseHandler.getConnection(botInterface);
			 ResultSet RS = DatabaseHandler.executeQuery(query, con)) {
			while (RS.next()) {
				User user = BotsHandler.getUser(RS.getLong("userId"), botInterface);
				if (user != null) {
					user.getFollowedGamesUid().add(RS.getString("gameUid"));
					i++;
				}
			}
			Main.printNewEvent(i + " user(s) followed games loaded from database", false, botInterface);
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized static void addUser(long userId, BotInterfaces botInterface) {
		final String query = "INSERT INTO `users` VALUES (?);";
		try (Connection con = DatabaseHandler.getConnection(botInterface);
			 PreparedStatement p = DatabaseHandler.newTransact(query, con)) {
			p.setLong(1, userId);
			p.executeUpdate();
		} catch (final SQLException e) {
			System.err.println("SQL Exception " + e.getMessage());
		}
	}
	
	public synchronized static void addUserFollowedGame(long userId, String gameId, BotInterfaces botInterface) {
		final String query = "INSERT INTO `users_followedgames` VALUES (?,?);";
		try (Connection con = DatabaseHandler.getConnection(botInterface);
			 PreparedStatement p = DatabaseHandler.newTransact(query, con)) {
			p.setLong(1, userId);
			p.setString(2, gameId);
			p.executeUpdate();
		} catch (final SQLException e) {
			System.err.println("SQL Exception " + e.getMessage());
		}
	}
	
	public synchronized static void deleteUserFollowedGame(long userId, String gameId, BotInterfaces botInterface) {
		final String query = "DELETE FROM `users_followedgames` WHERE userId = ? AND gameUid = ?";
		try (Connection con = DatabaseHandler.getConnection(botInterface);
			 PreparedStatement p = DatabaseHandler.newTransact(query, con)) {
			p.setLong(1, userId);
			p.setString(2, gameId);
			p.executeUpdate();
		} catch (final SQLException e) {
			System.err.println("SQL Exception " + e.getMessage());
		}
	}

	private static PreparedStatement newTransact(String baseQuery, Connection con) throws SQLException
	{
		return con.prepareStatement(baseQuery);
	}

	private static PreparedStatement newTransactWithKeys(String baseQuery, Connection con) throws SQLException
	{
		return con.prepareStatement(baseQuery, Statement.RETURN_GENERATED_KEYS);
	}

	private static ResultSet executeQuery(String query, Connection con) throws SQLException
	{
		Statement stat = con.createStatement();
		stat.setQueryTimeout(60);
		return stat.executeQuery(query);
	}
	
	private static Connection getConnection(BotInterfaces botInterface) throws SQLException {
		return DATA_SOURCES.get(botInterface).getConnection();
	}
}