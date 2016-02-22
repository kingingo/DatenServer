package dev.wolveringer.dataserver.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import dev.wolveringer.dataserver.connection.Client;
import dev.wolveringer.dataserver.connection.ClientType;
import dev.wolveringer.dataserver.gamestats.StatsManager;
import dev.wolveringer.dataserver.uuid.UUIDManager;
import dev.wolveringer.mysql.MySQL;
import lombok.Getter;
import lombok.Setter;

public class OnlinePlayer {
	public static enum Setting {
		PREMIUM_LOGIN,
		PASSWORD,
		UUID;
	}
	
	@Getter
	private String name;
	private UUID uuid;
	private boolean isPremium = false;
	@Getter
	private String loginPassword = null;
	
	@Getter
	private StatsManager statsManager;
	private Client owner;
	@Getter
	@Setter
	private String server = "undefined";
	
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
			
		}
		statsManager = new StatsManager(this);
	}
	
	public void save(){
		statsManager.save();
	}

	public void setPassword(String value) {
		this.loginPassword = value;
		MySQL.getInstance().command("UPDATE `users` SET `password`='"+value+"' WHERE uuid='"+uuid.toString()+"'"); //Cript?
	}

	public void setPremium(Boolean valueOf) {
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
	@Override
	public String toString() {
		return "OnlinePlayer [name=" + name + ", uuid=" + uuid + ", isPremium=" + isPremium + ", loginPassword=" + loginPassword + ", statsManager=" + statsManager + ", owner=" + owner + ", server=" + server + "]";
	}
}
