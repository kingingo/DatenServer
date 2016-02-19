package dev.wolveringer.dataserver.ban;

import java.sql.Date;
import java.util.ArrayList;
import java.util.UUID;

import lombok.Getter;

public class BanEntity {
	@Getter
	private String ip;
	@Getter
	private ArrayList<String> usernames;
	@Getter
	private ArrayList<UUID> uuids;
	@Getter
	private String reson;
	
	@Getter
	private String banner;
	@Getter
	private UUID bannerUUID;
	
	@Getter
	private Date bannedUntil;
	
	@Getter
	private int level;
	private long end;
	
	
	public BanEntity(String ip,String username,String uuid,String banner,String bannerUUID,String date,String reson,int level) {
		this(ip, username, uuid, banner, bannerUUID, date, reson, level, -1);
	}
	public BanEntity(String ip,String username,String uuid,String banner,String bannerUUID,String date,String reson,int level,int end) {
		this.end = end;
		this.ip = ip;
		if(username != null && username.length() == 0 && username.equalsIgnoreCase("null"))
			this.usernames.add(username);
		if(uuid != null && uuid.length() == 0 && uuid.equalsIgnoreCase("null"))
			this.uuids.add(UUID.fromString(uuid));
		this.banner = banner;
		if(bannerUUID != null && bannerUUID.length() == 0 && bannerUUID.equalsIgnoreCase("null"))
			this.bannerUUID = UUID.fromString(bannerUUID);
		this.bannedUntil = new Date(1950, 0, 0); //TODO paradise
		this.level = level;
		this.reson = reson;
	}
	
	public boolean isTempBanned(){
		return end != -1;
	}
	
	public int matchPlayer(String ip,String name,UUID uuid){
		int value = 0;
		if(ip != null)
			if(ip.equalsIgnoreCase(ip))
				value++;
		if(usernames.contains(name))
			value++;
		if(uuids.contains(uuid))
			value++;
		return value;
	}
}
