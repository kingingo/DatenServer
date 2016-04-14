package dev.wolveringer.converter;

import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.threads.EventLoop;

public class Converter {
	private MySQL _old;
	private MySQL _new;

	public static void convert(){
		MySQL _old = new MySQL(new MySQL.MySQLConfiguration("148.251.143.2", 3306, "games", "root", "55P_YHmK8MXlPiqEpGKuH_5WVlhsXT", true));
		MySQL _new = new MySQL(new MySQL.MySQLConfiguration("148.251.143.2", 3306, "test", "root", "55P_YHmK8MXlPiqEpGKuH_5WVlhsXT", true));
		_new.setEventLoop(new EventLoop(1000));
		_old.setEventLoop(new EventLoop(1000));
		PlayerIdConverter players = new PlayerIdConverter(_old, _new);
		players.loadOldDatabase();
		players.transfare();
		players.loadPlayerIds();
		MoneyConverter money = new MoneyConverter(_old, _new, players);
		//money.transfare();
		GameStatsConverter stats = new GameStatsConverter(_old, _new, players);
		stats.transfare();
	}
}
