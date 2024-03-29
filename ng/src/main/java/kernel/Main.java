package kernel;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import exceptions.FlareSolverrException;
import handlers.BotsHandler;
import handlers.DatabaseHandler;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.io.IoBuilder;
import utils.Stats;
import wiimmfi.FlareSolverrGatewayManager;
import wiimmfi.GamesListParser;

public class Main {
	private static Thread checkGamesListTask;
	private static long startTime;
	private volatile static boolean running;
	public static String version = "2.0.8";
	
	public static void main(String[] args) {
		setRunning(true);
		header();
		if (Config.load()) {
			Main.printNewEvent("Configuration file loaded successfully", false);
		} else {
			Main.printNewEvent("Configuration file loading failed", false);
			System.exit(1);
		}
		configureLog4j2();
		for (final BotInterfaces botInterface : BotInterfaces.values()) {
			Main.printNewEvent("Load " + botInterface.toString() + " bot interface database", false);
			DatabaseHandler.initBotInterfaceDataSource(botInterface);
			DatabaseHandler.loadUsers(botInterface);
			DatabaseHandler.loadUsersFollowedGames(botInterface);
		}
		BotsHandler.launchBotInterfaces();
		Main.launchCheckGamesListTask();
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
		    @Override
		    public void run()
		    {
		    	System.out.println("Exiting...");
		    	Main.setRunning(false);
		    }
		});
		System.out.println("Wiimmfi notifier is ready !");
		startTime = System.currentTimeMillis();
	}
	
	public static void header() {
		System.out.print("			Wiimmfi Notifier\n");
		System.out.print("			Version : ");
		System.out.print(Main.version + "\n");
		System.out.print("			by Azlino\n");
		System.out.print("-------------------------------------------------------------------------------\n\n");
	}

	private static void configureLog4j2() {
		System.setProperty("logsPath", "logs");
		System.setProperty("log4j.configurationFile", "log4j2.properties");
		// On transfert tout vers les loggers
		System.setErr(IoBuilder.forLogger(LogManager.getLogger("STDERR")).setLevel(Level.ERROR).buildPrintStream());
		System.setOut(IoBuilder.forLogger(LogManager.getLogger("STDOUT")).setLevel(Level.INFO).buildPrintStream());
	}
	
	public static String getUptime() {
		long uptime = System.currentTimeMillis() - startTime;
		final int day = (int) (uptime / (1000 * 3600 * 24));
		uptime %= 1000 * 3600 * 24;
		final int hour = (int) (uptime / (1000 * 3600));
		uptime %= 1000 * 3600;
		final int min = (int) (uptime / (1000 * 60));
		uptime %= 1000 * 60;
		final int sec = (int) (uptime / 1000);
		return day + "d " + hour + "h " + min + "m " + sec + "s";
	}
	
	private synchronized static void launchCheckGamesListTask() {
		if (checkGamesListTask != null && checkGamesListTask.isAlive()) {
			return;
		}
		checkGamesListTask = new Thread() {
			@Override
			public void run() {
				try {
					while (Main.isRunning()) {
						Main.printNewEvent("Check games list task started", true);
			        	try {
							GamesListParser.parseWiimmfiGamesList();
							GamesListParser.warnUsers();
				        	GamesListParser.gamesFinishedActivityWarning();
				        	Stats.checkGamesListCount.incrementAndGet();
							Main.printNewEvent("Check games list task finished", true);
						} catch (Exception e) {
							e.printStackTrace();
							Main.printNewEvent("Check games list task failed", true);
							if (e instanceof FlareSolverrException) {
								try {
									Main.printNewEvent("An error occured from FlareSolverr, let's ask browser session re-creation", false);
									FlareSolverrGatewayManager.instance.recreateBrowserSession();
									Main.printNewEvent("Browser session re-creation : success", false);
								} catch (IOException ex) {
									ex.printStackTrace();
									Main.printNewEvent("Browser session re-creation : failure", false);
								}
							}
						}
			        	Thread.sleep(TimeUnit.SECONDS.toMillis(Config.gamesListParsingSecondsInterval));
					}
				} catch (final InterruptedException e) {
				}
				stopThread(this);
			}
		};
		checkGamesListTask.setName("checkGamesListTask");
		checkGamesListTask.setDaemon(false);
		checkGamesListTask.start();
	}
	
	public static void printNewEvent(String event, boolean debug) {
		if (debug) {
			if (Config.debug) {
				System.out.println(new Date() + " | [DEBUG] " + event);
			}
		} else {
			System.out.println(new Date() + " | " + event);
		}
	}

	public static void printNewEvent(String event, boolean debug, BotInterfaces botInterface) {
		event = "[" + botInterface.toString() + "] " + event;
		printNewEvent(event, debug);
	}
	
	private synchronized static void stopThread(Thread thread) {
		if (thread != null) {
			thread = null;
		}
	}

	public static boolean isRunning() {
		return running;
	}

	public static void setRunning(boolean running) {
		Main.running = running;
	}
}