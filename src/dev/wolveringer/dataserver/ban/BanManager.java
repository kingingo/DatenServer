package dev.wolveringer.dataserver.ban;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.mysql.MySQL.MySQLConfiguration;

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

	public void banPlayer(String name, String ip, String suuid, String banner, String bannerUUID, String bannerIP, int level, long end, String reson) {
		UUID uuid = suuid != null ? UUID.fromString(suuid) : null;

		BanEntity old;
		while ((old = getEntity(name, ip, uuid)) != null) {
			if (old.isActive()) {
				old.setActive(false);
				saveEntity(old);
				System.out.println("Disable ban: "+old);
			} else
				bans.remove(old);
		}
		BanEntity e;
		bans.add(e = new BanEntity(ip, name, uuid + "", banner, bannerUUID + "", bannerIP, new Date().getTime()+"", reson, level, end));
		if (end != -1) {
			MySQL.getInstance().command("INSERT INTO `BG_ZEITBAN`(`name`, `nameip`, `name_uuid`, `banner`, `bannerip`, `banner_uuid`, `date`, `time`, `reason`, `aktiv`) VALUES ('" + name + "','" + ip + "','" + uuid + "','" + banner + "','" + bannerIP + "','" + bannerUUID + "','" + new Date().getTime() + "','" + end + "','" + reson + "','true')");
		} else {
			MySQL.getInstance().command("INSERT INTO `BG_BAN`(`name`, `nameip`, `name_uuid`, `banner`, `bannerip`, `banner_uuid`, `time`, `reason`, `level`, `aktiv`) VALUES ('" + name + "','" + ip + "','" + uuid + "','" + banner + "','" + bannerIP + "','" + bannerUUID + "','" + new Date().getTime() + "','" + reson + "','" + level + "','true')");
		}
	}

	public void saveEntity(BanEntity e) {
		if (e.needSave()) {
			if (e.isTempBanned()) {
				MySQL.getInstance().command("UPDATE `BG_ZEITBAN` SET `time`='" + e.getEnd() + "',`reason`='" + e.getReson() + "',`aktiv`='" + e.isActive() + "' WHERE " + buildXORMySQL("nameip", (e.getIp())) + " AND "+ buildXORMySQL("name_uuid", (e.getUuids().size() > 0 ? e.getUuids().get(0).toString() : null)) + " AND "+buildXORMySQL("name", (e.getUsernames().size() == 0 ? null : e.getUsernames().get(0)) )+ "");
			} else {
				MySQL.getInstance().command("UPDATE `BG_BAN` SET `level`='" + e.getEnd() + "',`reason`='" + e.getReson() + "',`aktiv`='" + e.isActive() + "' WHERE " + buildXORMySQL("nameip", (e.getIp())) + " AND "+ buildXORMySQL("name_uuid", (e.getUuids().size() > 0 ? e.getUuids().get(0).toString() : null)) + " AND "+buildXORMySQL("name", (e.getUsernames().size() == 0 ? null : e.getUsernames().get(0)) )+ "");
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
		MySQL.setInstance(new MySQL(new MySQLConfiguration("148.251.143.2", 3306, "games", "root", "55P_YHmK8MXlPiqEpGKuH_5WVlhsXT", true,10)));
		MySQL.getInstance().connect();
		BanManager m = new BanManager();
		m.loadBans();
		//larsd1999:system:6155c27b-61b3-49af-9c67-9b526afc9c15
		System.out.println(m.getEntity("larsd1999", "system", UUID.fromString("6155c27b-61b3-49af-9c67-9b526afc9c15")).isActive()); //UUID.fromString("57091d6f-839f-48b7-a4b1-4474222d4ad1")
		System.out.println(System.currentTimeMillis());
		Thread.sleep(10000);
	}
}
