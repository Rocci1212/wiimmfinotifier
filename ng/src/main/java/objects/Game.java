package objects;

public class Game {
	private String uniqueId;
	private String productionName;
	private String type; // Wii / NDS / DSiWare
	private int onlineCount;
	private short warnPlayingActivity = 0; // 1 = Used 2 = Not used

	public Game(String uniqueId, String productionName, String type, int onlineCount) {
		setUniqueId(uniqueId);
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

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public short getWarnPlayingActivity() {
		return warnPlayingActivity;
	}

	public void setWarnPlayingActivity(short warnPlayingActivity) {
		this.warnPlayingActivity = warnPlayingActivity;
	}
}