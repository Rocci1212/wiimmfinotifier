package objects;

public class Game {
	private String productionName;
	private String type; // Wii / NDS / DSiWare
	private int onlineCount;
	private short warnPlayingActivity = 0; // 1 = Used 2 = Not used

	public Game(String productionName, String type, int onlineCount) {
		setProductionName(productionName);
		setType(type);
		setOnlineCount(onlineCount);
	}
	
	public String getProductionName() {
		return productionName;
	}

	public void setProductionName(String productionName) {
		this.productionName = productionName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getOnlineCount() {
		return onlineCount;
	}

	public void setOnlineCount(int onlineCount) {
		this.onlineCount = onlineCount;
	}

	public short getWarnPlayingActivity() {
		return warnPlayingActivity;
	}

	public void setWarnPlayingActivity(short warnPlayingActivity) {
		this.warnPlayingActivity = warnPlayingActivity;
	}
}