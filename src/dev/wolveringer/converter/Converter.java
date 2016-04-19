package dev.wolveringer.converter;

import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.threads.EventLoop;

public class Converter {
	public static void convert(){
		MySQL _old = new MySQL(new MySQL.MySQLConfiguration("148.251.143.2", 3306, "old", "root", "55P_YHmK8MXlPiqEpGKuH_5WVlhsXT", true,100));
		MySQL _new = new MySQL(new MySQL.MySQLConfiguration("148.251.143.2", 3306, "games", "root", "55P_YHmK8MXlPiqEpGKuH_5WVlhsXT", true,100));
		_new.setEventLoop(new EventLoop(1000));
		_old.setEventLoop(new EventLoop(1000));
		_old.connect();
		_new.connect();
		
		PlayerIdConverter players = new PlayerIdConverter(_old, _new);
		players.loadOldDatabase();
		players.transfare();
		players.loadPlayerIds();
		players.transfareProperties();
		
		MoneyConverter money = new MoneyConverter(_old, _new, players);
		money.transfare();
		
		PermissionConverter perms = new PermissionConverter(_old, _new, players);
		perms.transfare();
		
		GameStatsConverter stats = new GameStatsConverter(_old, _new, players);
		stats.transfare();
		
		SkyPvPConverter skywars= new SkyPvPConverter(_old, _new, players);
		skywars.transfare();
		
		GuildenConverter pvpGuilde = new GuildenConverter(_old, _new, players, "PvP");
		pvpGuilde.transfare();
		
		GuildenConverter skyGuilde = new GuildenConverter(_old, _new, players, "Sky");
		skyGuilde.transfare();
	}
}
