package dev.wolveringer.dataserver.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import dev.wolveringer.client.connection.ClientType;
import dev.wolveringer.dataserver.connection.Client;
import dev.wolveringer.dataserver.gamestats.StatsManager;
import dev.wolveringer.dataserver.skin.OperationCallback;
import dev.wolveringer.dataserver.skin.SkinCash;
import dev.wolveringer.dataserver.uuid.UUIDManager;
import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.skin.Skin;
import lombok.Getter;
import lombok.Setter;

public class OnlinePlayer {
	@Getter
	private String name;
	private UUID uuid;
	private boolean isPremium = false;
	@Getter
	private String loginPassword = null;
	@Getter
	private LanguageType lang;
	@Getter
	private StatsManager statsManager;
	private Client owner;
	@Getter
	@Setter
	private String server = "undefined";
	@Getter
	@Setter
	private boolean disableUnload;
	@Getter
	@Setter
	private String curruntIp;
	
	private PlayerSkinManager skinManager;
	
	public OnlinePlayer(String name,Client owner) {
		this.name = name.toLowerCase();
		this.uuid = UUIDManager.getUUID(name.toLowerCase());
		this.owner = owner;
	}
	public boolean isPremium() {
		return isPremium;
	}
	
	public Client getPlayerBungeecord() {
		if(owner != null && owner.getType() != ClientType.BUNGEECORD)
			return null;
		return owner;
	}
	
	public void setOwner(Client owner) {
		this.owner = owner;
	}
	
	@SuppressWarnings("unchecked")
	protected void load(){
		ArrayList<String[]> response = MySQL.getInstance().querySync("SELECT `premium`,`password` FROM `users` WHERE uuid='"+uuid+"'", 1);
		if(response.size() == 0){
			MySQL.getInstance().commandSync("INSERT INTO `users`(`player`, `uuid`, `premium`, `password`, `last_login`) VALUES ('"+name+"','"+uuid.toString()+"','"+isPremium+"','null','-1')");
		}
		else
		{
			this.isPremium = Boolean.parseBoolean(response.get(0)[0]);
			if(response.get(0).length >= 2)
				this.loginPassword = response.get(0)[1].equalsIgnoreCase("null") ? null : response.get(0)[1];
			else
				System.out.println(Arrays.asList(response.get(0)));
			ArrayList<String[]> r = MySQL.getInstance().querySync("SELECT `language` FROM `language_user` WHERE uuid='"+uuid+"'", 1);
			if(r.size() == 0){
				lang = LanguageType.getLanguageFromName(r.get(0)[0]);
			}
			else{
				lang = LanguageType.ENGLISH;
				MySQL.getInstance().command("INSERT INTO `language_user`(`uuid`, `language`) VALUES ('"+getUuid()+"','"+lang.getShortName()+"')");
			}
		}
		skinManager = new PlayerSkinManager(this);
		skinManager.load();
		statsManager = new StatsManager(this);
	}
	
	public void save(){
		statsManager.save();
	}

	public void setLanguage(LanguageType lang){
		this.lang = lang;
		MySQL.getInstance().command("UPDATE `language_user` SET `language`='"+lang.getShortName()+"' WHERE `uuid`='"+uuid+"'");
	}
	
	public void setPassword(String value) {
		this.loginPassword = value;
		MySQL.getInstance().command("UPDATE `users` SET `password`='"+value+"' WHERE uuid='"+uuid.toString()+"'"); //Cript?
	}

	public void setPremium(Boolean valueOf) {
		System.out.println("Setpremium from "+isPremium+" to "+valueOf);
		if(valueOf == isPremium)
			return;
		isPremium = valueOf;
		UUIDManager.setPremiumUUID(name, valueOf);
		UUID newUUID = UUIDManager.getUUID(name);
		MySQL.getInstance().commandSync("UPDATE `users` SET `premium`='"+valueOf.toString()+"',`uuid`='"+newUUID+"' WHERE uuid='"+uuid.toString()+"'");
		PlayerManager.changeUUID(uuid, newUUID);
		this.uuid = newUUID;
	}
	public UUID getUuid() {
		if(uuid == null)
			System.out.println("UUID = null"); //TODO load
		return uuid;
	}
	public boolean isPlaying(){
		return !server.equalsIgnoreCase("undefined") && (owner != null && owner.isConnected());
	}
	public PlayerSkinManager getSkinManager() {
		return skinManager;
	}
	
	@Override
	public String toString() {
		return "OnlinePlayer [name=" + name + ", uuid=" + uuid + ", isPremium=" + isPremium + ", loginPassword=" + loginPassword + ", statsManager=" + statsManager + ", owner=" + owner + ", server=" + server + "]";
	}
}
