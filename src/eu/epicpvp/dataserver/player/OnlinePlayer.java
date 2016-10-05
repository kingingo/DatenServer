package eu.epicpvp.dataserver.player;

import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import eu.epicpvp.dataserver.connection.Client;
import eu.epicpvp.dataserver.gamestats.StatsManager;
import eu.epicpvp.dataserver.skin.UUIDFetcher;
import eu.epicpvp.dataserver.uuid.UUIDManager;
import eu.epicpvp.datenserver.definitions.connection.ClientType;
import eu.epicpvp.datenserver.definitions.dataserver.player.LanguageType;
import eu.epicpvp.mysql.MySQL;
import lombok.Getter;
import lombok.Setter;

public class OnlinePlayer {
	@Getter
	private int playerId = -1;

	@Getter
	private String name = null;
	private UUID uuid = null;
	@Getter
	private String nickname = null;
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

	private boolean isLoading = true;
	@Getter
	@Setter
	private boolean deleted = false;

	@Getter
	private long lastPasswordChange = -1;

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
		this.isLoading = true;
		ArrayList<String[]> response;
		if (this.playerId != -1) {
			response = MySQL.getInstance().querySync("SELECT `playerId`, `name`, `uuid` FROM `users` WHERE `playerId`='" + this.playerId + "'", 1);
		} else if (this.name != null) {
			response = MySQL.getInstance().querySync("SELECT `playerId`, `name`, `uuid` FROM `users` WHERE `name`='" + this.name + "'", 1);
			if (response.size() == 0) {
				response = MySQL.getInstance().querySync("SELECT `playerId`, `name`, `uuid` FROM `users` WHERE `name`='" + this.name.toLowerCase() + "'", 1);
				if (response.size() != 0) {
					System.out.println("Updating username (lowercase to real): " + this.name + ":" + ((String[]) response.get(0))[1]);
					MySQL.getInstance().commandSync("UPDATE `users` SET `name`='" + ((String[]) response.get(0))[1] + "' WHERE `name`='" + this.name + "'");
				} else {
					try {
						this.uuid = UUIDFetcher.getUUIDOf(this.name);
						response = MySQL.getInstance().querySync("SELECT `playerId`, `name`, `uuid` FROM `users` WHERE `uuid`='" + this.uuid + "'", 1);
						if (response.size() != 0) {
							ArrayList<String[]> querryUUID = MySQL.getInstance().querySync("SELECT `premium` FROM `user_properties` WHERE `playerId`='" + Integer.parseInt(((String[]) response.get(0))[0]) + "'", 1);
							if ((querryUUID.size() > 0) && (((String[]) querryUUID.get(0))[0].equalsIgnoreCase("1"))) {
								this.playerId = Integer.parseInt(((String[]) response.get(0))[0]);
								System.out.println("Player " + ((String[]) response.get(0))[1] + " change his name to " + this.name);
								setName(this.name);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			if (this.uuid != null) {
				response = MySQL.getInstance().querySync("SELECT `playerId`, `name`, `uuid` FROM `users` WHERE `users`.`uuid`='" + this.uuid + "'", 1);
			} else {
				throw new RuntimeException("Cant load an player without informations (" + name + " - "+uuid+" - "+playerId+")!");
			}
		}
		if (response.size() == 0) {
			if (this.name == null) {
				throw new RuntimeException("Cant create a new player without a name! (" + this.playerId + ":" + this.name + ":" + this.uuid + ":" + this.server + ")");
			}
			this.newPlayer = true;
			MySQL.getInstance().commandSync("INSERT INTO `users`(`name`, `uuid`) VALUES ('" + this.name + "','" + UUIDManager.getOfflineUUID(this.name) + "')");
			response = MySQL.getInstance().querySync("SELECT `playerId`, `name`, `uuid` FROM `users` WHERE `name`='" + this.name + "'", 1);
		}
		System.out.println("User credicals for " + this.playerId + ":" + this.name + ":" + this.uuid + " -> " + StringUtils.join((Object[]) response.get(0), ":"));
		this.playerId = Integer.parseInt(((String[]) response.get(0))[0]);
		if (this.name == null) {
			this.name = ((String[]) response.get(0))[1];
		} else if (!this.name.equals(((String[]) response.get(0))[1])) {
			setName(this.name);
		}
		this.uuid = UUID.fromString(((String[]) response.get(0))[2]);
		if (this.newPlayer) {
			MySQL.getInstance().commandSync("INSERT INTO `user_properties`(`playerId`, `password`, `premium`,`language`) VALUES ('" + this.playerId + "','',0,'" + LanguageType.ENGLISH.getShortName() + "')");
		}
		response = MySQL.getInstance().querySync("SELECT `password`,`premium`,`language`,`nickname`,`pwChangeDate` FROM `user_properties` WHERE `playerId`='" + this.playerId + "'", 1);
		if (response.size() == 0) {
			System.err.println("Cant find user properties for: " + this.playerId + " (" + this.name + ")");
			MySQL.getInstance().commandSync("INSERT INTO `user_properties`(`playerId`, `password`, `premium`,`language`) VALUES ('" + this.playerId + "','',0,'" + LanguageType.ENGLISH.getShortName() + "')");
			response = MySQL.getInstance().querySync("SELECT `password`,`premium`,`language`,`nickname`,`pwChangeDate` FROM `user_properties` WHERE `playerId`='" + this.playerId + "'", 1);
		}
		if (!((String[]) response.get(0))[0].equalsIgnoreCase("")) {
			this.loginPassword = ((String[]) response.get(0))[0];
		}
		this.isPremium = ((((String[]) response.get(0))[1].equalsIgnoreCase("1")) || (((String[]) response.get(0))[1].equalsIgnoreCase("true")));
		this.language = LanguageType.getLanguageFromName(((String[]) response.get(0))[2]);
		this.nickname = response.get(0).length > 3 ? response.get(0)[3] : null;
		if(this.nickname == null || this.nickname.equalsIgnoreCase("null") || this.nickname.length() == 0)
			this.nickname = null;
		this.lastPasswordChange = Long.parseLong(response.get(0)[4]);
		this.skinManager = new PlayerSkinManager(this);
		this.skinManager.load();
		this.statsManager = new StatsManager(this);
		this.isLoading = false;
		if(!deleted)
			PlayerManager.deleteDuplicated(this);
	}

	public boolean isPremiumPlayer() {
		return isPremium;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
		MySQL.getInstance().command("UPDATE `user_properties` SET `nickname`='" + nickname + "' WHERE `playerId`='" + playerId + "'");
	}

	public Client getPlayerBungeecord() {
		if (owner != null && owner.getType() != ClientType.BUNGEECORD)
			return null;
		return owner;
	}

	public void save() {
		if (isLoaded())
			statsManager.save();
	}

	public void setLanguage(LanguageType lang) {
		this.language = lang;
		MySQL.getInstance().command("UPDATE `user_properties` SET `language`='" + lang.getShortName() + "' WHERE `playerId`='" + playerId + "'");
	}

	public void setPassword(String value) {
		this.loginPassword = value;
		MySQL.getInstance().command("UPDATE `user_properties` SET `password`='" + value + "',`pwChangeDate`='"+(lastPasswordChange = System.currentTimeMillis())+"' WHERE `playerId`='" + playerId + "'");
	}

	public void setPremium(Boolean valueOf) { // TODO
		System.out.println("Setpremium from " + isPremium + " to " + valueOf);
		if (valueOf == isPremium)
			return;
		UUID newUUID;
		if (valueOf == true)
			try {
				newUUID = UUIDManager.getOnlineUUID(name);
			} catch (Exception e) {
				e.printStackTrace();
				newUUID = null;
			}
		else
			newUUID = UUIDManager.getOfflineUUID(name);
		if (newUUID == null)
			throw new NullPointerException("cant featch online UUID!");
		MySQL.getInstance().command("UPDATE `user_properties` SET `premium`='" + (valueOf ? 1 : 0) + "' WHERE `playerId`='" + playerId + "'");
		this.isPremium = valueOf;
		setUUID(newUUID);
	}

	public void setUUID(UUID newUUID) { // TODO
		MySQL.getInstance().command("UPDATE `users` SET `uuid`='" + newUUID + "' WHERE `playerId`='" + playerId + "'");
		this.uuid = newUUID;
	}

	public UUID getUuid() { // TODO
		return uuid;
	}

	public boolean isPlaying() {
		return server != null && !server.equalsIgnoreCase("undefined") && owner != null && owner.isConnected();
	}

	public PlayerSkinManager getSkinManager() { // TODO
		return skinManager;
	}

	public void setServer(String server, Client owner) {
		this.server = server;
		this.owner = owner;
	}

	public boolean isLoaded() {
		return playerId != -1 && name != null && uuid != null;
	}

	@Override
	public String toString() {
		return "OnlinePlayer [name=" + name + ", uuid=" + uuid + ", isPremium=" + isPremium + ", loginPassword=" + loginPassword + ", statsManager=" + statsManager + ", owner=" + owner + ", server=" + server + "]";
	}

	public void setName(String value) {
		this.name = value;
		MySQL.getInstance().commandSync("UPDATE `users` SET `name`='" + name + "' WHERE `playerId`='" + playerId + "'");
	}

	public void waitWhileLoading() {
		while (isLoading) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
