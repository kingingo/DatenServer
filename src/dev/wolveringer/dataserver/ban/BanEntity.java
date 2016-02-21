package dev.wolveringer.dataserver.ban;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import dev.wolveringer.mysql.MySQL;
import lombok.Getter;

public class BanEntity {
	@Getter
	private String ip;
	@Getter
	private ArrayList<String> usernames = new ArrayList<>();
	@Getter
	private ArrayList<UUID> uuids = new ArrayList<>();
	@Getter
	private String reson;
	
	@Getter
	private String banner;
	@Getter
	private String bannerIp;
	@Getter
	private UUID bannerUUID;
	
	@Getter
	private Date bannedUntil;
	
	@Getter
	private int level;
	@Getter
	private long end;
	
	private boolean needSave = false;
	
	public BanEntity(String ip,String username,String uuid,String banner,String bannerUUID,String date,String reson,int level) {
		this(ip, username, uuid, banner, bannerUUID, date, reson, level, -1);
	}
	public BanEntity(String ip,String username,String uuid,String banner,String bannerUUID,String date,String reson,int level,long end) {
		this.end = end;
		this.ip = ip;
		if(username != null && username.length() != 0 && !username.equalsIgnoreCase("null"))
			this.usernames.add(username.toLowerCase());
		if(uuid != null && uuid.length() != 0 && !uuid.equalsIgnoreCase("null"))
			this.uuids.add(UUID.fromString(uuid));
		this.banner = banner;
		if(bannerUUID != null && bannerUUID.length() != 0 && !bannerUUID.equalsIgnoreCase("null") && !bannerUUID.equalsIgnoreCase("AACHack"))
			this.bannerUUID = UUID.fromString(bannerUUID);
		this.bannedUntil = new Date(1950, 0, 0); //TODO paradise
		this.level = level;
		this.reson = reson;
	}
	public BanEntity(String ip,String username,String uuid,String banner,String bannerUUID,String bannerIp,String date,String reson,int level,long end) {
		this.end = end;
		this.ip = ip;
		this.bannerIp = bannerIp;
		if(username != null && username.length() != 0 && !username.equalsIgnoreCase("null"))
			this.usernames.add(username.toLowerCase());
		if(uuid != null && uuid.length() != 0 && !uuid.equalsIgnoreCase("null"))
			this.uuids.add(UUID.fromString(uuid));
		this.banner = banner;
		if(bannerUUID != null && bannerUUID.length() != 0 && !bannerUUID.equalsIgnoreCase("null") && !bannerUUID.equalsIgnoreCase("AACHack"))
			this.bannerUUID = UUID.fromString(bannerUUID);
		this.bannedUntil = new Date(1950, 0, 0); //TODO paradise
		this.level = level;
		this.reson = reson;
	}
	
	public boolean isTempBanned(){
		return end != -1;
	}
	
	public boolean isActive(){
		return end == -1 || System.currentTimeMillis()<end;
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
	
	public boolean needSave() {
		return needSave;
	}
	
	@Override
	public String toString() {
		return "BanEntity [ip=" + ip + ", usernames=" + usernames + ", uuids=" + uuids + ", reson=" + reson + ", banner=" + banner + ", bannerUUID=" + bannerUUID + ", bannedUntil=" + bannedUntil + ", level=" + level + ", end=" + end + "]";
	}
	public void setActive(boolean b) {
		if(b == false)
			end = 0;
		else
			end = -1;
		needSave = true;
	}
	public void setReson(String reson){
		this.reson = reson;
		needSave = true;
	}
	public void setLevel(int level){
		this.level = level;
		needSave = true;
	}
	public void setTime(long end) {
		this.end = end;
	}
	
	public void save(){
		if(needSave){
			needSave = false;
			if(isTempBanned()){
				MySQL.getInstance().command("UPDATE `BG_ZEITBAN` SET `time`='"+end+"',`reason`='"+reson+"',`aktiv`='"+isActive()+"' WHERE nameip='"+ip+"' AND name_uuid='"+(uuids.size() >= 1?uuids.get(0):"null")+"' AND name='"+(usernames.size()==0?"null":usernames.get(0))+"'");
			}
			else
			{
				MySQL.getInstance().command("UPDATE `BG_BAN` SET `level`='"+level+"',`reason`='"+reson+"',`aktiv`='"+isActive()+"' WHERE nameip='"+ip+"' AND name_uuid='"+(uuids.size() >= 1?uuids.get(0):"null")+"' AND name='"+(usernames.size()==0?"null":usernames.get(0))+"'");
			}
		}
	}
}
