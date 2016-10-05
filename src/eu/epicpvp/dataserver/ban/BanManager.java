package eu.epicpvp.dataserver.ban;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import eu.epicpvp.datenserver.definitions.dataserver.ban.BanEntity;
import eu.epicpvp.mysql.MySQL;
import eu.epicpvp.mysql.MySQL.MySQLConfiguration;
import lombok.AllArgsConstructor;
import lombok.Getter;

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

		bans = MySQL.getInstance().querySync("SELECT `name`,`nameip`,`name_uuid`,`banner`,`banner_uuid`,`time`,`reason`,`level`,`aktiv`,`banId` FROM `BG_BAN`", -1); // WHERE `aktiv`='true'
		for (String[] data : bans) {
			BanEntity e = new BanEntity(data[1], data[0], data[2], data[3], data[4], data[5], data[6], Integer.parseInt(data[7]));
			e.setActive(Boolean.parseBoolean(data[8]));
			e.setBanId(Integer.parseInt(data[9]));
			this.bans.add(e);
		}

		bans = MySQL.getInstance().querySync("SELECT `name`,`nameip`,`name_uuid`,`banner`,`banner_uuid`,`date`,`reason`,`time`,`aktiv`,`banId` FROM `BG_ZEITBAN`", -1); // WHERE `aktiv`='true'
		for (String[] data : bans) {
			BanEntity e = new BanEntity(data[1], data[0], data[2], data[3], data[4], data[5], data[6], -1, Long.parseLong(data[7]));
			e.setActive(Boolean.parseBoolean(data[8]));
			e.setBanId(Integer.parseInt(data[9]));
			this.bans.add(e);
		}
	}

	public BanEntity getEntity(String name, String ip, UUID uuid) {
		if (ip != null)
			ip.toLowerCase();
		int match = 0;
		BanEntity e = null;
		for (BanEntity te : new ArrayList<>(bans)) {
			if (!te.isActive())
				continue;
			int tmatch = te.matchPlayer(ip, name, uuid);
			if (tmatch > match || (e != null && tmatch == match && e.getDate()<te.getDate())) {
				match = tmatch;
				e = te;
			}
		}
		System.out.println("Find ban entity for: "+name+":"+ip+":"+uuid+" -> "+e);
		return e;
	}

	@AllArgsConstructor
	@Getter
	private static class BanEntityHolder {
		private BanEntity entity;
		private int match;
	}

	public ArrayList<BanEntity> getEntitys(String name, String ip, UUID uuid) {
		if (ip != null)
			ip.toLowerCase();
		List<BanEntityHolder> enties = new ArrayList<>();
		for (BanEntity te : new ArrayList<>(bans)) {
			int tmatch = te.matchPlayer(ip, name, uuid);
			if (tmatch > 0) {
				enties.add(new BanEntityHolder(te, tmatch));
			}
		}
		if(enties.size() == 0)
			return new ArrayList<>();
		Collections.sort(enties,new Comparator<BanEntityHolder>() {
			@Override
			public int compare(BanEntityHolder o1, BanEntityHolder o2) {
				int c1 = Integer.compare(o2.getMatch(), o1.getMatch());
				if(c1 != 0){
					return c1;
				}
				return o2.getEntity().getBannedUntil().compareTo(o1.getEntity().getBannedUntil());
			}
		});
		Collections.sort(enties,new Comparator<BanEntityHolder>() {
			@Override
			public int compare(BanEntityHolder o1, BanEntityHolder o2) {
				return Boolean.compare(o2.getEntity().isActive(), o1.getEntity().isActive());
			}
		});
		ArrayList<BanEntity> returnEntities = new ArrayList<>();
		for(BanEntityHolder h : enties){
			returnEntities.add(h.getEntity());
		}
		return returnEntities;
	}

	public void banPlayer(String name, String ip, String suuid, String banner, String bannerUUID, String bannerIP, int level, long end, String reson) {
		UUID uuid = suuid != null ? UUID.fromString(suuid) : null;

		BanEntity old;
		while ((old = getEntity(name, ip, uuid)) != null) {
			if (old.isActive()) {
				old.setActive(false);
				saveEntity(old);
				System.out.println("Over ban old ban ("+old+")");
			}
		}
		long date = System.currentTimeMillis();
		BanEntity e = new BanEntity(ip, name, uuid + "", banner, bannerUUID + "", bannerIP, date+"", reson, level, end);
		List<String[]> out;
		if (end != -1) {
			MySQL.getInstance().commandSync("INSERT INTO `BG_ZEITBAN`(`name`, `nameip`, `name_uuid`, `banner`, `bannerip`, `banner_uuid`, `date`, `time`, `reason`, `aktiv`) VALUES ('" + name + "','" + ip + "','" + uuid + "','" + banner + "','" + bannerIP + "','" + bannerUUID + "','" + date + "','" + end + "','" + reson + "','true')");
			out = MySQL.getInstance().querySync("SELECT `banId` FROM `BG_ZEITBAN` WHERE `date`='"+date+"'",1);
		} else {
			MySQL.getInstance().commandSync("INSERT INTO `BG_BAN`(`name`, `nameip`, `name_uuid`, `banner`, `bannerip`, `banner_uuid`, `time`, `reason`, `level`, `aktiv`) VALUES ('" + name + "','" + ip + "','" + uuid + "','" + banner + "','" + bannerIP + "','" + bannerUUID + "','" + date + "','" + reson + "','" + level + "','true')");
			out = MySQL.getInstance().querySync("SELECT `banId` FROM `BG_BAN` WHERE `time`='"+date+"'",1);
		}
		if(out.size() >= 1){
			e.setBanId(Integer.parseInt(out.get(0)[0]));
			bans.add(e);
		}
	}

	public void saveEntity(BanEntity e) {
		if (e.needSave()) {
			if (e.isTempBanned()) {
				MySQL.getInstance().command("UPDATE `BG_ZEITBAN` SET `time`='" + e.getEnd() + "',`reason`='" + e.getReson() + "',`aktiv`='" + e.isActive() + "' WHERE `banId`="+e.getBanId()+"");
			} else {
				MySQL.getInstance().command("UPDATE `BG_BAN` SET `level`='" + e.getEnd() + "',`reason`='" + e.getReson() + "',`aktiv`='" + e.isActive() + "' WHERE `banId`="+e.getBanId()+"");
			}
			e.saved();
		}
	}

	private String buildXORMySQL(String row,String input){
		return input == null ? "("+row+"='null' OR "+row+"='undefined')" : row+"='"+input+"'";
	}

	public void unbanPlayer(String name, UUID uuid, String ip) {
		BanEntity ban = getEntity(name, ip, uuid);
		if (ban != null) {
			ban.setActive(false);
		}
	}

	public static void main(String[] args) throws InterruptedException {
		MySQL.setInstance(new MySQL(new MySQLConfiguration("148.251.14.168", 3306, "games", "root", "55P_YHmK8MXlPiqEpGKuH_5WVlhsXT", true,10)));
		MySQL.getInstance().connect();
		BanManager banManager = new BanManager();
		long start = System.currentTimeMillis();
		banManager.loadBans();
		System.out.println("Time: "+(System.currentTimeMillis()-start));
		//larsd1999:system:6155c27b-61b3-49af-9c67-9b526afc9c15
		//banManager.banPlayer("WolverinDEV", null, null, "system", "CONSOLE", "null", 2, -1, "Fucking test :D");
		List<BanEntity> entries = banManager.getEntitys("wagadogo", null, null);
		for(BanEntity e : entries)
			System.out.println(e);
		MySQL.getInstance().getEventLoop().waitForAll();
		System.out.println("Done");
	}
}
