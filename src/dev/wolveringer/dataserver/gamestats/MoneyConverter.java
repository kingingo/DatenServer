package dev.wolveringer.dataserver.gamestats;

import java.util.ArrayList;
import java.util.HashMap;

import dev.wolveringer.mysql.MySQL;

public class MoneyConverter {
	public static void main(String[] args) {
		HashMap<String, String> gems = new HashMap<>();
		HashMap<String, String> coins = new HashMap<>();
		
		
		MySQL.setInstance(new MySQL(new MySQL.MySQLConfiguration("148.251.143.2", 3306, "test", "root", "55P_YHmK8MXlPiqEpGKuH_5WVlhsXT", true)));
		ArrayList<String[]> query;
		System.out.println("Reading gem list");
		query = MySQL.getInstance().querySync("SELECT DISTINCT(`gems_list`.`uuid`),`gems_list`.`gems` FROM `gems_list`", -1);
		for(String[] s : query)
			gems.put(s[0], s[1]);
		System.out.println("Reading coin list");
		query = MySQL.getInstance().querySync("SELECT DISTINCT(`coins_list`.`uuid`),`coins_list`.`coins` FROM `coins_list`", -1);
		for(String[] s : query)
			coins.put(s[0], s[1]);
		
		
		
		ArrayList<String> insert = new ArrayList<>();
		ArrayList<String> toInsert = new ArrayList<>();
		toInsert.addAll(gems.keySet());
		toInsert.addAll(coins.keySet());
		
		int i = 0;
		int max = toInsert.size();
		
		for(String s : gems.keySet()){
			if(insert.contains(s)){
				i++;
				continue;
			}
			if(!coins.containsKey(s)){
				coins.put(s, "0");
				System.out.println("No coin information abaut: "+s);
			}
			System.out.println("Insert: "+(i++)+"/"+max);
			MySQL.getInstance().command("INSERT INTO `users_MONEY`(`player`, `UUID`, `coins`, `gems`) VALUES ('undef','"+s+"','"+coins.get(s)+"','"+gems.get(s)+"')");
		}
		System.out.println("Waiting for event loop");
		while (MySQL.LOOP.getCurruntThreads()>0) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("Queue length: "+MySQL.LOOP.getQueue().size());
		}
		System.out.println("Done");
	}
}
