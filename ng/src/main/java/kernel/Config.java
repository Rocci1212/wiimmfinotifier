package kernel;

import java.io.BufferedReader;
import java.io.FileReader;

public class Config {
	// Main
	public static boolean debug;
	public static String discordBotToken = "";
	public static String telegramBotToken = "";
	// Database
	public static String dbNameTelegram = "", dbNameDiscord = "", dbHost = "127.0.0.1", dbUser = "root", dbPass = "";
	// Parsing
	public static int gamesListParsingSecondsInterval = 60;
	public static String wiimmfiBaseUrl = null;
	public static String wiimmfiFullGamesListPath;
	public static String wiimmfiNDSTypeImagePath;
	public static String wiimmfiDSIWARETypeImagePath;
	public static String wiimmfiWIITypeImagePath;
	public static String wiimmfiWIIWARETypeImagePath;
	// Bypass CloudFlare
	public static boolean useFlareSolverr = false;
	public static String flareSolverrUrl = "";
	public static String flareSolverrSession = "";
	public static int flareSolverrRequestMaxTimeout = 60;
	// API specifications
	public static int discordMaxMessageLength = 2000;
	public static int telegramMaxMessageLength = 4096;

	public static boolean load() {

		try (BufferedReader config = new BufferedReader(new FileReader("config.properties"))) {
			String line = "";
			while ((line = config.readLine()) != null) {
				final String[] data = line.split("=", 2);
				if (data.length <= 1) {
					continue;
				}
				final String param = data[0].trim();
				String value = data[1].trim();
				if (value.equals("")) {
					continue;
				}
				if (param.equalsIgnoreCase("DEBUG") && value.equalsIgnoreCase("true")) {
					Config.debug = true;
				} else if (param.equalsIgnoreCase("USE_FLARE_SOLVERR") && value.equalsIgnoreCase("true")) {
					Config.useFlareSolverr = true;
				} else if (param.equalsIgnoreCase("FLARE_SOLVERR_URL")) {
					Config.flareSolverrUrl = value;
				} else if (param.equalsIgnoreCase("FLARE_SOLVERR_SESSION")) {
					Config.flareSolverrSession = value;
				} else if (param.equalsIgnoreCase("FLARE_SOLVERR_REQUEST_MAX_TIMEOUT")) {
					Config.flareSolverrRequestMaxTimeout = Integer.parseInt(value);
				} else if (param.equalsIgnoreCase("GAMES_LIST_PARSING_SECONDS_INTERVAL")) {
					Config.gamesListParsingSecondsInterval = Integer.parseInt(value);
				} else if (param.equalsIgnoreCase("MAX_MESSAGE_LENGTH_DISCORD")) {
					Config.discordMaxMessageLength = Integer.parseInt(value);
				} else if (param.equalsIgnoreCase("MAX_MESSAGE_LENGTH_TELEGRAM")) {
					Config.telegramMaxMessageLength = Integer.parseInt(value);
				} else if (param.equalsIgnoreCase("BOT_TOKEN_DISCORD")) {
					Config.discordBotToken = value;
				} else if (param.equalsIgnoreCase("BOT_TOKEN_TELEGRAM")) {
					Config.telegramBotToken = value;
				} else if (param.equalsIgnoreCase("DB_NAME_DISCORD")) {
					Config.dbNameDiscord = value;
				} else if (param.equalsIgnoreCase("DB_NAME_TELEGRAM")) {
					Config.dbNameTelegram = value;
				} else if (param.equalsIgnoreCase("DB_HOST")) {
					Config.dbHost = value;
				} else if (param.equalsIgnoreCase("DB_USER")) {
					Config.dbUser = value;
				} else if (param.equalsIgnoreCase("DB_PASS")) {
					Config.dbPass = value == null ? "" : value;
				} else if (param.equalsIgnoreCase("WIIMMFI_BASE_URL")) {
					Config.wiimmfiBaseUrl = value;
				} else if (param.equalsIgnoreCase("WIIMMFI_FULLGAMESLIST_PATH")) {
					if (Config.wiimmfiBaseUrl != null) { 
						value = Config.wiimmfiBaseUrl + value;
					}
					Config.wiimmfiFullGamesListPath = value;
				} else if (param.equalsIgnoreCase("WIIMMFI_NDS_TYPE_IMAGE_PATH")) {
					if (Config.wiimmfiBaseUrl != null) { 
						value = Config.wiimmfiBaseUrl + value;
					}
					Config.wiimmfiNDSTypeImagePath = value;
				} else if (param.equalsIgnoreCase("WIIMMFI_DSIWARE_TYPE_IMAGE_PATH")) {
					if (Config.wiimmfiBaseUrl != null) { 
						value = Config.wiimmfiBaseUrl + value;
					}
					Config.wiimmfiDSIWARETypeImagePath = value;
				} else if (param.equalsIgnoreCase("WIIMMFI_WII_TYPE_IMAGE_PATH")) {
					if (Config.wiimmfiBaseUrl != null) { 
						value = Config.wiimmfiBaseUrl + value;
					}
					Config.wiimmfiWIITypeImagePath = value;
				} else if (param.equalsIgnoreCase("WIIMMFI_WIIWARE_TYPE_IMAGE_PATH")) {
					if (Config.wiimmfiBaseUrl != null) { 
						value = Config.wiimmfiBaseUrl + value;
					}
					Config.wiimmfiWIIWARETypeImagePath = value;
			}
			if (Config.dbNameDiscord == null || Config.dbNameTelegram == null || Config.dbHost == null || Config.dbPass == null || Config.dbUser == null) {
				throw new Exception();
			}
		}
		} catch (final Exception e) {
			System.err.println(e.getMessage());
			System.err.println("config.properties file not found or unreadable");
			return false;
		}
		return true;
	}
}
