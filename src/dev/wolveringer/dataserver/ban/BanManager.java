package dev.wolveringer.dataserver.ban;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import dev.wolveringer.mysql.MySQL;

public class BanManager {
	private static BanManager manager;
	
	public static BanManager getManager() {
		return manager;
	}
	public static void setManager(BanManager manager) {
		BanManager.manager = manager;
	}
	
	private ArrayList<BanEntity> bans = new ArrayList<>();

	public void loadBans() {
		ArrayList<String[]> bans;

		bans = MySQL.getInstance().querySync("SELECT `name`,`nameip`,`name_uuid`,`banner`,`banner_uuid`,`time`,`reason`,`level` FROM `BG_BAN` WHERE `aktiv`='true'", -1);
		for (String[] data : bans) {
			BanEntity e = new BanEntity(data[1], data[0], data[2], data[3], data[4], data[5], data[6], Integer.parseInt(data[7]));
			this.bans.add(e);
		}

		bans = MySQL.getInstance().querySync("SELECT `name`,`nameip`,`name_uuid`,`banner`,`banner_uuid`,`date`,`reason`,`time` FROM `BG_ZEITBAN` WHERE `aktiv`='true'", -1);
		for (String[] data : bans) {
			BanEntity e = new BanEntity(data[1], data[0], data[2], data[3], data[4], data[5], data[6], -1, Long.parseLong(data[7]));
			this.bans.add(e);
		}
	}

	public BanEntity getEntity(String name, String ip, UUID uuid) {
		if(name != null)
			name = name.toLowerCase();
		if(ip != null)
			ip.toLowerCase();
		int match = 0;
		BanEntity e = null;
		for (BanEntity te : new ArrayList<>(bans)) {
			if(!te.isActive())
				continue;
			int tmatch = te.matchPlayer(ip, name, uuid);
			if (tmatch > match) {
				match = tmatch;
				e = te;
			}
		}
		return e;
	}
	
	public void banPlayer(String name,String ip,String suuid,String banner,String bannerUUID,String bannerIP,int level,long end,String reson){
		UUID uuid = suuid != null ? UUID.fromString(suuid) : null;
		
		BanEntity old;
		while ((old = getEntity(name, ip, uuid))!=null) {
			if(old.isActive()){
				if(!old.getReson().equalsIgnoreCase(reson))
					old.setReson(reson);
				if(old.getLevel() != level)
					old.setLevel(level);
				if(old.getEnd() != end)
					old.setTime(end);
				old.save();
				return;
			}
			else
				bans.remove(old);
		}
			bans.add(new BanEntity(ip, name, uuid+"", banner, bannerUUID+"",bannerIP, new Date().toString(), reson, level,end));
			if(end != -1){
				MySQL.getInstance().command("INSERT INTO `BG_ZEITBAN`(`name`, `nameip`, `name_uuid`, `banner`, `bannerip`, `banner_uuid`, `date`, `time`, `reason`, `aktiv`) VALUES ('"+name+"','"+ip+"','"+uuid+"','"+banner+"','"+bannerIP+"','"+bannerUUID+"','"+new Date()+"','"+end+"','"+reson+"','true')");
			}
			else
			{
				MySQL.getInstance().command("INSERT INTO `BG_BAN`(`name`, `nameip`, `name_uuid`, `banner`, `bannerip`, `banner_uuid`, `time`, `reason`, `level`, `aktiv`) VALUES ('"+name+"','"+ip+"','"+uuid+"','"+banner+"','"+bannerIP+"','"+bannerUUID+"','"+new Date()+"','"+reson+"','"+level+"','true')");
			}
	}
	
	public void unbanPlayer(String name,UUID uuid,String ip){
		BanEntity ban = getEntity(name, ip, uuid);
		if(ban != null){
			ban.setActive(false);
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
		MySQL.setInstance(new MySQL("148.251.143.2", "3306", "games", "root", "55P_YHmK8MXlPiqEpGKuH_5WVlhsXT"));
		BanManager m = new BanManager();
		m.loadBans();
		m.banPlayer("WolverinDEV", null, null, "System", UUID.randomUUID().toString(), "0.0.0.0", 5, System.currentTimeMillis()+60*1000, "Testing");
		System.out.println(m.getEntity("WolverinDEV", null, UUID.fromString("57091d6f-839f-48b7-a4b1-4474222d4ad1"))); //UUID.fromString("57091d6f-839f-48b7-a4b1-4474222d4ad1")
		System.out.println(System.currentTimeMillis());
		Thread.sleep(10000);
	}
}
