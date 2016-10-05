package eu.epicpvp.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import eu.epicpvp.mysql.MySQL;

public class PlayerIdConverter {
	private MySQL _old;
	private MySQL _new;

	private HashMap<String, UUID> nameToUUID = new HashMap<>();
	private HashMap<UUID, String> uuidToName = new HashMap<>();
	private ArrayList<UUID> premium = new ArrayList<>();
	private HashMap<UUID, Integer> uuidToPlayerId = new HashMap<>();
	private HashMap<String, UUID> playerIdToName = new HashMap<>();
	
	public PlayerIdConverter(MySQL _old,MySQL _new) {
		this._old =_old;
		this._new = _new;
	}
	
	public void loadOldDatabase(){
		System.out.println("Loading old datebase");
		ArrayList<String[]> querry = _old.querySync("SELECT `name` FROM `coins_list` WHERE `name`!='none'", -1);
		for(String[] q : querry){
			try{
				put(q[0].toLowerCase(), UUID.nameUUIDFromBytes(("OfflinePlayer:"+q[0].toLowerCase()).getBytes()));
			}catch(Exception e){
				System.out.println("Cant load player : "+e.getMessage()+":"+StringUtils.join(q,":"));
			}
		}
		System.out.println("Loading old datebase 2!");
		//SELECT * FROM `coins_list` WHERE `name`!='none'
		querry = _old.querySync("SELECT `player`,`uuid`,`premium` FROM `list_premium`", -1);
		//UUID.nameUUIDFromBytes(("OfflinePlayer:"+player[0].toLowerCase()).getBytes()
		for(String[] player : querry){
			try{
				if(player[2].equalsIgnoreCase("true")){
					put(player[0].toLowerCase(), UUID.fromString(player[1]));
					if(player[2].equalsIgnoreCase("true"))
						premium.add(UUID.fromString(player[1]));
				}
				else
				{
					put(player[0].toLowerCase(), UUID.nameUUIDFromBytes(("OfflinePlayer:"+player[0].toLowerCase()).getBytes()));
				}
			}catch(Exception e){
				System.out.println("Cant load player : "+e.getMessage()+":"+StringUtils.join(player,":"));
			}
		}
		System.out.println("Old datebase loaded");
	}
	
	public void loadPlayerIds(){
		if(uuidToPlayerId.size() != 0)
			return;
		System.out.println("Loading player ids");
		ArrayList<String[]> querry = _new.querySync("SELECT `playerId`, `uuid` FROM `users`", -1);
		for(String[] player : querry){
			try{
				uuidToPlayerId.put(UUID.fromString(player[1]),Integer.parseInt(player[0]));
				playerIdToName.put(player[0], UUID.fromString(player[1]));
			}catch(Exception e){
				System.out.println("Cant load player (2) : "+e.getMessage()+":"+StringUtils.join(player,":"));
			}
		}
		System.out.println("Player ids loaded");
	}
	
	public void transfare(){
		System.out.println("Transfare base database");
		
		HashMap<String, UUID> _nameToUUID = new HashMap<>(nameToUUID);
		HashMap<UUID, String> _uuidToName = new HashMap<>(uuidToName);
		
		ArrayList<String[]> querry = _new.querySync("SELECT `name`, `uuid` FROM `users`", -1);
		for(String[] player : querry){
			try{
				_nameToUUID.remove(player[0]);
				_uuidToName.remove(UUID.fromString(player[1]));
			}catch(Exception e){
				System.out.println("Cant load player (1) : "+e.getMessage()+":"+StringUtils.join(player,":"));
			}
		}
		
		System.out.println("Insert players: "+_nameToUUID.keySet().size());
		try {
			Thread.sleep(2500);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		for(String name : _nameToUUID.keySet()){
			UUID uuid = _nameToUUID.get(name);
			if(uuid != null)
				_new.command("INSERT INTO `users`(`name`, `uuid`) VALUES ('"+name+"','"+uuid+"')");
			else
				System.out.println("Cant find uuid for "+name);
		}
		
		EventLoopWaiter.wait(_new.getEventLoop());
		System.out.println("Database base converted");
		
	
		//list_premium
	}
	
	public void transfareProperties(){
		ArrayList<String[]> querry;
		System.out.println("Transfare props");
		if(uuidToPlayerId.size() == 0){
			System.out.println("Loading ids");
			loadPlayerIds();
		}
		ArrayList<UUID> toInsert = new ArrayList<>(uuidToPlayerId.keySet());
		System.out.println("Size a: "+toInsert.size());
		querry = _new.querySync("SELECT `playerId` FROM `user_properties`",-1);
		System.out.println("Indexing");
		ArrayList<String> alredyIn = new ArrayList<>();
		for(String[] q : querry){
			try{
				alredyIn.add(q[0]);
			}catch (Exception e) {
				e.printStackTrace();
				System.out.println("Cant paradise user "+StringUtils.join(q," "));
			}
		}
		System.out.println("Size b: "+alredyIn.size());
		HashMap<String, String> password = new HashMap<>();
		querry = _old.querySync("SELECT `name`,`password` FROM `list_users` WHERE 1", -1);
		System.out.println("Putting passwords");
		for(String[] user : querry)
			password.put(user[0], user[1]);
		for(UUID uuid : toInsert){
			int playerId;
			if((playerId = getPlayerId(uuid)) == -1){
				System.out.println("Missing player uuid: "+uuid);
				continue;
			}
			if(alredyIn.contains(playerId+""))
				continue;
			String up = password.get(getName(uuid));
			if(up == null)
				up = "";
			_new.command("INSERT INTO `user_properties`(`playerId`, `password`, `premium`, `language`) VALUES ('"+playerId+"','"+up+"','"+(premium.contains(uuid)?"1":"0")+"','en')");
		}
		
		EventLoopWaiter.wait(_new.getEventLoop());
		System.out.println("Props converted");
	}
	
	public UUID getUUID(String player){
		UUID uuid = nameToUUID.get(player);
		if(uuid == null)
			return nameToUUID.get(player.toLowerCase());
		return uuid;
	}
	public String getName(UUID uuid){
		return uuidToName.get(uuid);
	}
	public int getPlayerId(UUID player){
		if(player == null)
			return -1;
		if(uuidToPlayerId == null || uuidToPlayerId.size()  == 0){
		}
		try{
			if(!uuidToPlayerId.containsKey(player))
				return -1;
			return uuidToPlayerId.get(player);
		}catch(Exception e){
			e.printStackTrace();
			return -1;
		}
	}
	private void put(String name,UUID uuid){
		if(uuid == null || name == null)
			return;
		nameToUUID.put(name, uuid);
		uuidToName.put(uuid, name);
	}

	public UUID getUUID(int parseInt) {
		return playerIdToName.get(parseInt);
	}
	
	public static void main(String[] args) {
		HashMap<String, UUID> u = new HashMap<>();
		u.put("x1", UUID.randomUUID());
		u.put("x1", UUID.randomUUID());
		System.out.println(u);
	}
}
