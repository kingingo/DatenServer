package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import dev.wolveringer.arrays.CachedArrayList;
import dev.wolveringer.dataserver.player.OnlinePlayer;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.mysql.MySQL;

//CREATE TABLE IF NOT EXISTS GILDE_MONEY (`gilde` VARCHAR(36), `section` VARCHAR(20), `date` BIGINT, `playerId` BIGINT, `amount` BIGINT, `message` TEXT)
public class GildenManager {
	public static GildenManager manager;
	public static GildenManager getManager() {
		return manager;
	}
	public static void setManager(GildenManager manager) {
		GildenManager.manager = manager;
	}
	
	private CachedArrayList<Gilde> gilden = new CachedArrayList<>(20, TimeUnit.MINUTES);
	public GildenManager() {
	}

	public CachedArrayList<Gilde> getAllLoadedGilden() {
		return gilden;
	}
	
	private synchronized Gilde loadGilde(UUID gilde) {
		for (Gilde g : gilden)
			if (g.getUuid().equals(gilde))
				return g;
		Gilde g = new Gilde(gilde);
		g.load();
		gilden.add(g);
		return g;
	}
	
	public void deleteGilde(UUID gilde){
		gilden.remove(getGilde(gilde));
		MySQL.getInstance().command("DELETE FROM `GILDE_MEMBERS` WHERE `gilde`='"+gilde.toString()+"';");
		MySQL.getInstance().command("DELETE FROM `GILDE_INFORMATION` WHERE `uuid`='"+gilde.toString()+"'");
		MySQL.getInstance().command("DELETE FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+gilde.toString()+"'");
	}

	public Gilde createGilde(String name,OnlinePlayer player){
		/*
		 *  
	      INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+uuid+"','"+GildeType.ALL.toString()+"','name','"+name+"')
	      INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+uuid+"','"+GildeType.ALL.toString()+"','ownerId','"+owner+"')
	      INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+uuid+"','"+GildeType.ALL.toString()+"','active','false')
		 */
		UUID uuid = UUID.randomUUID();
		Gilde temp;
		while ((temp = getLoadedGilde(uuid)) != null) {
			if(temp.isExist())
				uuid = UUID.randomUUID();
			else
				gilden.remove(temp);
		}
		if(getGilde(name,false) != null)
			gilden.remove(getGilde(name, false));
		ArrayList<String> commands = new ArrayList<>();
		commands.add("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+uuid+"','"+GildeType.ALL.toString()+"','shortName','"+name+"')");
		commands.add("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+uuid+"','"+GildeType.ALL.toString()+"','name','"+name+"')");
		commands.add("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+uuid+"','"+GildeType.ALL.toString()+"','ownerId','"+player.getPlayerId()+"')");
		for(GildeType t : GildeType.getPossibleValues()){
			commands.add("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+uuid+"','"+t.toString()+"','active','false')");
		}
		MySQL.getInstance().commandSync(commands.toArray(new String[0]));
		Gilde gilde = new Gilde(uuid);
		gilde.load();
		gilden.add(gilde);
		return gilde;
	}
	
	public Gilde getLoadedGilde(UUID uuid) {
		for (Gilde g : new ArrayList<>(gilden))
			if (g != null && g.getUuid() != null)
				if (g.getUuid().equals(uuid))
					return g;
		return null;
	}

	public Gilde getGilde(UUID uuid) {
		for (Gilde g : new ArrayList<>(gilden))
			if (g != null && g.getUuid() != null)
				if (g.getUuid().equals(uuid))
					return g;
		return loadGilde(uuid);
	}

	public Gilde getGilde(String name) {
		return getGilde(name, true);
	}
	
	public Gilde getGilde(String name, boolean load) {
		for (Gilde g : gilden)
			if (g != null && g.getName() != null)
				if (g.getName() != null && g.getName().equalsIgnoreCase(name))
					return g;
		if(!load)
			return null;
		ArrayList<String[]> response = MySQL.getInstance().querySync("SELECT `uuid` FROM `GILDE_INFORMATION` WHERE `key`='name' AND `value`='"+name+"'");
		if(response.size() == 0)
			return new Gilde.NOT_EXISTING_Gilde(name);
		return getGilde(UUID.fromString(response.get(0)[0]));
	}

	public Gilde getGilde(int player, GildeType type) {
		for (Gilde g : gilden)
			if (g != null && g.getName() != null)
				for (GildSection s : g.getActiveSections())
					if (s.getType() == type && s.players.contains(new Integer(player)))
						return g;
		ArrayList<String[]> response = MySQL.getInstance().querySync("SELECT `gilde` FROM `GILDE_MEMBERS` WHERE `playerId`='"+player+"' AND `section`='"+type.toString()+"'");
		if(response.size() == 0)
			return new Gilde.NOT_EXISTING_Gilde(UUID.randomUUID());
		return getGilde(UUID.fromString(response.get(0)[0]));
	}
	
	public Gilde getOwnGilde(int player){
		for (Gilde g : gilden)
			if(g.getOwnerId() == player)
				return g;
		ArrayList<String[]> response = MySQL.getInstance().querySync("SELECT `uuid` FROM `GILDE_INFORMATION` WHERE `key`='ownerId' AND `value`='"+player+"'");
		if(response.size() == 0)
			return new Gilde.NOT_EXISTING_Gilde(UUID.randomUUID());
		return getGilde(UUID.fromString(response.get(0)[0]));
	}
	
	public HashMap<UUID, String> getAvariableGilden(GildeType type){ //TODO Faster check if active?
		ArrayList<String[]> response = MySQL.getInstance().querySync("SELECT `uuid` FROM `GILDE_INFORMATION` WHERE `key`='active' AND `value`='true'"+(type == GildeType.ALL ? "" : " AND `section`='"+type.name()+"'"));
		ArrayList<String[]> names = MySQL.getInstance().querySync("SELECT `uuid`,`value` FROM `GILDE_INFORMATION` WHERE `key`='name'");
		
		ArrayList<UUID> active = new ArrayList<>();
		for(String[] r : response)
			active.add(UUID.fromString(r[0]));
		HashMap<UUID, String> out = new HashMap<>();
		for(String[] r : names){
			UUID uuid = UUID.fromString(r[0]);
			if(active.contains(uuid)){
				out.put(uuid, r[1]);
			}
		}
		return out;
	}
}
/*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import dev.wolveringer.arrays.CachedArrayList;
import dev.wolveringer.dataserver.player.OnlinePlayer;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.mysql.MySQL;

public class GildenManager {
	public static GildenManager manager;
	public static GildenManager getManager() {
		return manager;
	}
	public static void setManager(GildenManager manager) {
		GildenManager.manager = manager;
	}
	
	private CachedArrayList<Gilde> gilden = new CachedArrayList<>(20, TimeUnit.MINUTES);
	public GildenManager() {
	}

	public CachedArrayList<Gilde> getAllLoadedGilden() {
		return gilden;
	}
	
	private synchronized Gilde loadGilde(UUID gilde) {
		for (Gilde g : gilden)
			if (g.getUuid().equals(gilde))
				return g;
		Gilde g = new Gilde(gilde);
		g.load();
		gilden.add(g);
		return g;
	}
	
	public void deleteGilde(UUID gilde){
		gilden.remove(getGilde(gilde));
		MySQL.getInstance().command("DELETE FROM `GILDE_MEMBERS` WHERE `gilde`='"+gilde.toString()+"';");
		MySQL.getInstance().command("DELETE FROM `GILDE_INFORMATION` WHERE `uuid`='"+gilde.toString()+"'");
		MySQL.getInstance().command("DELETE FROM `GILDE_PERMISSIONS` WHERE `uuid`=''");
	}

	public Gilde createGilde(String name,OnlinePlayer player){
		/.*
		 *  
	 INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+uuid+"','"+GildeType.ALL.toString()+"','name','"+name+"')
	 INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+uuid+"','"+GildeType.ALL.toString()+"','ownerId','"+owner+"')
	  INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+uuid+"','"+GildeType.ALL.toString()+"','active','false')
		 *./
		UUID uuid = UUID.randomUUID();
		ArrayList<String> commands = new ArrayList<>();
		commands.add("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+uuid+"','"+GildeType.ALL.toString()+"','shortName','"+name+"')");
		commands.add("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+uuid+"','"+GildeType.ALL.toString()+"','name','"+name+"')");
		commands.add("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+uuid+"','"+GildeType.ALL.toString()+"','ownerId','"+player.getPlayerId()+"')");
		for(GildeType t : GildeType.getPossibleValues()){
			commands.add("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+uuid+"','"+t.toString()+"','active','false')");
		}
		MySQL.getInstance().commandSync(commands.toArray(new String[0]));
		return getGilde(uuid);
	}
	
	public Gilde getLoadedGilde(UUID uuid) {
		for (Gilde g : new ArrayList<>(gilden))
			if (g != null && g.getUuid() != null)
				if (g.getUuid().equals(uuid))
					return g;
		return null;
	}

	public Gilde getGilde(UUID uuid) {
		for (Gilde g : new ArrayList<>(gilden))
			if (g != null && g.getUuid() != null)
				if (g.getUuid().equals(uuid))
					return g;
		return loadGilde(uuid);
	}

	public Gilde getGilde(String name) {
		for (Gilde g : gilden)
			if (g != null && g.getName() != null)
				if (g.getName() != null && g.getName().equalsIgnoreCase(name))
					return g;
		ArrayList<String[]> response = MySQL.getInstance().querySync("SELECT `uuid` FROM `GILDE_INFORMATION` WHERE `key`='name' AND `value`='"+name+"'");
		if(response.size() == 0)
			return null;
		return getGilde(UUID.fromString(response.get(0)[0]));
	}

	public Gilde getGilde(int player, GildeType type) {
		for (Gilde g : gilden)
			if (g != null && g.getName() != null)
				for (GildSection s : g.getActiveSections())
					if (s.getType() == type && s.players.contains(new Integer(player)))
						return g;
		ArrayList<String[]> response = MySQL.getInstance().querySync("SELECT `gilde` FROM `GILDE_MEMBERS` WHERE `playerId`='"+player+"' AND `section`='"+type.toString()+"'");
		if(response.size() == 0)
			return null;
		return getGilde(UUID.fromString(response.get(0)[0]));
	}
	
	public Gilde getOwnGilde(int player){
		for (Gilde g : gilden)
			if(g.getOwnerId() == player)
				return g;
		ArrayList<String[]> response = MySQL.getInstance().querySync("SELECT `uuid` FROM `GILDE_INFORMATION` WHERE `key`='ownerId' AND `value`='"+player+"'");
		if(response.size() == 0)
			return null;
		return getGilde(UUID.fromString(response.get(0)[0]));
	}
	
	public HashMap<UUID, String> getAvariableGilden(GildeType type){ //TODO Faster check if active?
		ArrayList<String[]> response = MySQL.getInstance().querySync("SELECT `uuid` FROM `GILDE_INFORMATION` WHERE `key`='active' AND `value`='true'"+(type == GildeType.ALL ? "" : " AND `section`='"+type.name()+"'"));
		ArrayList<String[]> names = MySQL.getInstance().querySync("SELECT `uuid`,`value` FROM `GILDE_INFORMATION` WHERE `key`='name'");
		
		ArrayList<UUID> active = new ArrayList<>();
		for(String[] r : response)
			active.add(UUID.fromString(r[0]));
		HashMap<UUID, String> out = new HashMap<>();
		for(String[] r : names){
			UUID uuid = UUID.fromString(r[0]);
			if(active.contains(uuid)){
				out.put(uuid, r[1]);
			}
		}
		return out;
	}
}
*/
