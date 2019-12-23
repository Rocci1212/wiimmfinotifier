package kernel;

import java.io.BufferedReader;
import java.io.FileReader;

public class Config {
	// Main
	public static boolean debug;
	public static String telegramBotToken = "";
	// Database
	public static String dbName = "wiimmfinotifier", dbHost = "127.0.0.1", dbUser = "root", dbPass = "";
	// Parsing
	public static String wiimmfiBaseUrl = null;
	public static String wiimmfiFullGamesListPath;
	public static String wiimmfiNDSTypeImagePath;
	public static String wiimmfiDSIWARETypeImagePath;
	public static String wiimmfiWIITypeImagePath;
	public static String wiimmfiWIIWARETypeImagePath;

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
				} else if (param.equalsIgnoreCase("TELEGRAMBOT_TOKEN")) {
					Config.telegramBotToken = value;
				} else if (param.equalsIgnoreCase("DB_NAME")) {
					Config.dbName = value;
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
			if (Config.dbName == null || Config.dbHost == null || Config.dbPass == null || Config.dbUser == null) {
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
