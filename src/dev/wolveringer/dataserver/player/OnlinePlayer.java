package dev.wolveringer.dataserver.player;

import java.util.UUID;

import dev.wolveringer.dataserver.connection.Client;
import dev.wolveringer.dataserver.gamestats.StatsManager;
import lombok.Getter;
import lombok.Setter;

public class OnlinePlayer {
	public static enum Setting {
		PREMIUM_LOGIN,
		PASSWORD;
	}
	
	@Getter
	private String name;
	@Getter
	private UUID uuid;
	private boolean isPremium;
	@Getter
	private String loginPassword;
	
	@Getter
	private StatsManager statsManager;
	private Client owner;
	@Getter
	@Setter
	private String server;
	
	public OnlinePlayer(UUID uuid,Client owner) {
		this.uuid = uuid;
		this.owner = owner;
	}
	
	public boolean isPremium() {
		return isPremium;
	}
	
	public Client getPlayerBungeecord() {
		return owner;
	}
	
	protected void load(){
		System.out.println("Playerloading not implimented yet!");
	}

	public void setPassword(String value) {
		this.loginPassword = value;
		System.out.println("Password setting not implimented yet!");
	}

	public void setPremium(Boolean valueOf) {
		isPremium = valueOf;
		System.out.println("Premium setting!");
	}
}
