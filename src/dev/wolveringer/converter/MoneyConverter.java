package dev.wolveringer.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import dev.wolveringer.mysql.MySQL;

public class MoneyConverter {
	private MySQL _old;
	private MySQL _new;
	private PlayerIdConverter  ids;
	
	public MoneyConverter(MySQL _old,MySQL _new,PlayerIdConverter  ids) {
		this._old =_old;
		this._new = _new;
		this.ids = ids;
	}
	
	public void transfare(){
		HashMap<String, String> gems = new HashMap<>();
		HashMap<String, String> coins = new HashMap<>();
		
		
		ArrayList<String[]> query;
		System.out.println("Reading gem list");
		query = _old.querySync("SELECT DISTINCT(`gems_list`.`uuid`),`gems_list`.`gems` FROM `gems_list`", -1);
		for(String[] s : query)
			gems.put(s[0], s[1]);
		System.out.println("Reading coin list");
		query = _old.querySync("SELECT DISTINCT(`coins_list`.`uuid`),`coins_list`.`coins` FROM `coins_list`", -1);
		for(String[] s : query)
			coins.put(s[0], s[1]);
		
		
		
		ArrayList<String> inserted = new ArrayList<>();
		ArrayList<String> toInsert = new ArrayList<>();
		toInsert.addAll(gems.keySet());
		toInsert.addAll(coins.keySet());
		System.out.println("Removing aöredy insert");
		query = _new.querySync("SELECT `playerId` FROM `statistics_MONEY`", -1);
		for(String[] s : query){
			toInsert.remove(ids.getUUID(Integer.parseInt(s[0]))+"");
			inserted.add(ids.getUUID(Integer.parseInt(s[0]))+"");
		}
		
		int i = 0;
		int max = toInsert.size();
		
		for(String s : toInsert){
			if(inserted.contains(s)){
				i++;
				continue;
			}
			if(!coins.containsKey(s)){
				coins.put(s, "0");
				System.out.println("No coin information abaut: "+s);
			}
			if(!gems.containsKey(s)){
				gems.put(s, "0");
				System.out.println("No Gems information abaut: "+s);
			}
			System.out.println("Insert: "+(i++)+"/"+max);
			try{
				inserted.add(s);
				if(ids.getPlayerId(UUID.fromString(s)) != -1)
					_new.command("INSERT INTO `statistics_MONEY`(`playerId`, `coins`, `gems`) VALUES ('"+ids.getPlayerId(UUID.fromString(s))+"','"+coins.get(s)+"','"+gems.get(s)+"')");
			}catch(Exception e){
				e.printStackTrace();
				System.out.println("Cant load player for: "+s);
			}
		}
		System.out.println("Waiting for event loop");
		EventLoopWaiter.wait(_new.getEventLoop());
		System.out.println("Done");
	}
}
