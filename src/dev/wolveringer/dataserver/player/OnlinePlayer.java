package dev.wolveringer.dataserver.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import dev.wolveringer.client.connection.ClientType;
import dev.wolveringer.dataserver.connection.Client;
import dev.wolveringer.dataserver.gamestats.StatsManager;
import dev.wolveringer.dataserver.skin.UUIDFetcher;
import dev.wolveringer.dataserver.uuid.UUIDManager;
import dev.wolveringer.mysql.MySQL;
import lombok.Getter;
import lombok.Setter;

public class OnlinePlayer {
	@Getter
	private int playerId = -1;

	@Getter
	private String name = null;
	private UUID uuid = null;

	private boolean newPlayer = false;

	private boolean isPremium = false;
	@Getter
	private String loginPassword = null;
	@Getter
	private LanguageType language;
	@Getter
	private StatsManager statsManager;
	private Client owner;
	
	@Getter
	private String server = null;
	@Getter
	@Setter
	private boolean disableUnload;
	@Getter
	@Setter
	private String curruntIp;

	private PlayerSkinManager skinManager;

	public OnlinePlayer(String name) {
		this.name = name;
	}

	public OnlinePlayer(UUID uuid) {
		this.uuid = uuid;
	}

	public OnlinePlayer(int id) {
		this.playerId = id;
	}

	protected void load() {
		ArrayList<String[]> response;
		if (playerId != -1) { //Load from playerId
			response = MySQL.getInstance().querySync("SELECT `playerId`, `name`, `uuid` FROM `users` WHERE `playerId`='" + playerId + "'", 1);
		} else if (name != null) {
			response = MySQL.getInstance().querySync("SELECT `playerId`, `name`, `uuid` FROM `users` WHERE `name`='" + name + "'", 1);
		} else if (uuid != null) {
			response = MySQL.getInstance().querySync("SELECT `playerId`, `name`, `uuid` FROM `users` WHERE `uuid`='" + uuid + "'", 1);
		} else
			throw new RuntimeException("Cant load an player without informations!");

		if (response.size() == 0) { //Insert player
			if (name == null)
				throw new RuntimeException("Cant create a new player without a name! ("+playerId+":"+name+":"+uuid+")");
			this.newPlayer = true;
			MySQL.getInstance().commandSync("INSERT INTO `users`(`name`, `uuid`) VALUES ('" + name + "','" + UUIDManager.getOfflineUUID(name) + "')"); //user_properties INSERT INTO `user_properties`(`playerId`, `password`, `premium`) VALUES ([value-1],[value-2],[value-3])
			response = MySQL.getInstance().querySync("SELECT `playerId`, `name`, `uuid` FROM `users` WHERE `name`='" + name + "'", 1);
		}

		this.playerId = Integer.parseInt(response.get(0)[0]);
		this.name = response.get(0)[1];
		this.uuid = UUID.fromString(response.get(0)[2]);

		//TODO Checking for name update!
		if (newPlayer) {
			MySQL.getInstance().commandSync("INSERT INTO `user_properties`(`playerId`, `password`, `premium`,`language`) VALUES ('" + playerId + "','',0,'"+LanguageType.ENGLISH.getShortName()+"')");
		}
		response = MySQL.getInstance().querySync("SELECT `password`,`premium`,`language` FROM `user_properties` WHERE `playerId`='" + playerId + "'", 1);
		if (response.size() == 0) {
			System.err.println("Cant find user properties for: " + playerId + " (" + name + ")");
			MySQL.getInstance().commandSync("INSERT INTO `user_properties`(`playerId`, `password`, `premium`,`language`) VALUES ('" + playerId + "','',0,'"+LanguageType.ENGLISH.getShortName()+"')");
			response = MySQL.getInstance().querySync("SELECT `password`,`premium`,`language` FROM `user_properties` WHERE `playerId`='" + playerId + "'", 1);
		}
		if (!response.get(0)[0].equalsIgnoreCase(""))
			this.loginPassword = response.get(0)[0];
		isPremium = response.get(0)[1].equalsIgnoreCase("1") || response.get(0)[1].equalsIgnoreCase("true");
		language = LanguageType.getLanguageFromName(response.get(0)[2]);
		
		skinManager = new PlayerSkinManager(this);
		skinManager.load();
		statsManager = new StatsManager(this);
	}

	public boolean isPremiumPlayer() {
		return isPremium;
	}

	public Client getPlayerBungeecord() {
		if (owner != null && owner.getType() != ClientType.BUNGEECORD)
			return null;
		return owner;
	}
	
	public void save() {
		statsManager.save();
	}

	public void setLanguage(LanguageType lang) {
		this.language = lang;
		MySQL.getInstance().command("UPDATE `user_properties` SET `language`='"+lang.getShortName()+"' WHERE `playerId`='"+playerId+"'");
	}

	public void setPassword(String value) {
		this.loginPassword = value;
		MySQL.getInstance().command("UPDATE `user_properties` SET `password`='"+value+"' WHERE `playerId`='"+playerId+"'");
	}

	public void setPremium(Boolean valueOf) { //TODO
		System.out.println("Setpremium from " + isPremium + " to " + valueOf);
		if (valueOf == isPremium)
			return;
		UUID newUUID;
		if(valueOf == true)
			try {
				newUUID = UUIDManager.getOnlineUUID(name);
			} catch (Exception e) {
				e.printStackTrace();
				newUUID = null;
			}
		else
			newUUID = UUIDManager.getOfflineUUID(name);
		if(newUUID == null)
			throw new NullPointerException("cant featch online UUID!");
		MySQL.getInstance().command("UPDATE `user_properties` SET `premium`='"+(valueOf?1:0)+"' WHERE `playerId`='"+playerId+"'");
		this.isPremium = valueOf;
		setUUID(newUUID);
	}

	public void setUUID(UUID newUUID) { //TODO
		MySQL.getInstance().command("UPDATE `users` SET `uuid`='"+newUUID+"' WHERE `playerId`='"+playerId+"'");
		this.uuid = newUUID;
	}

	public UUID getUuid() { //TODO
		return uuid;
	}

	public boolean isPlaying() {
		return server != null && !server.equalsIgnoreCase("undefined") && owner != null && owner.isConnected();
	}

	public PlayerSkinManager getSkinManager() { //TODO
		return skinManager;
	}

	public void setServer(String server,Client owner){
		this.server = server;
		this.owner = owner;
	}
	
	public boolean isLoaded(){
		return playerId != -1 && name != null && uuid != null;
	}
	
	@Override
	public String toString() {
		return "OnlinePlayer [name=" + name + ", uuid=" + uuid + ", isPremium=" + isPremium + ", loginPassword=" + loginPassword + ", statsManager=" + statsManager + ", owner=" + owner + ", server=" + server + "]";
	}

	public void setName(String value) {
		this.name = value;
		MySQL.getInstance().command("UPDATE `users` SET `name`='"+name+"' WHERE `playerId`='"+playerId+"'");
	}
}
