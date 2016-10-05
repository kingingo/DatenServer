package eu.epicpvp.gild;

import java.util.ArrayList;
import java.util.HashMap;

import eu.epicpvp.datenserver.definitions.events.gilde.GildePermissionEvent;
import eu.epicpvp.datenserver.definitions.events.gilde.GildePlayerEvent;
import eu.epicpvp.event.EventHelper;
import eu.epicpvp.mysql.MySQL;
import lombok.Getter;

public class GildSectionPermission {
	public static final String TABLE_NAME = "GILDE_PERMISSIONS";
	
	@Getter
	private GildSection handle;

	private ArrayList<GildPermissionGroup> groups;
	protected HashMap<Integer, String> players = new HashMap<>();

	public GildSectionPermission(GildSection handle) {
		this.handle = handle;
	}

	public synchronized void loadGroups(){
		groups = new ArrayList<>();
		ArrayList<String[]> infos = MySQL.getInstance().querySync("SELECT `group`,`permission` FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getUuid().toString()+"' AND `section`='"+handle.getType().toString()+"'");
		if(infos.size() == 0){ //Auto insert default groups
			infos.add(new String[]{"owner","permission.itemid.338"});
			infos.add(new String[]{"default","permission.itemid.38"});
			infos.add(new String[]{"default","group.default"});
			MySQL.getInstance().commandSync("INSERT INTO `GILDE_PERMISSIONS`(`uuid`, `section`, `group`, `permission`) VALUES ('"+handle.getHandle().getUuid()+"','"+handle.getType().toString()+"','owner','permission.itemid.338')");
			MySQL.getInstance().commandSync("INSERT INTO `GILDE_PERMISSIONS`(`uuid`, `section`, `group`, `permission`) VALUES ('"+handle.getHandle().getUuid()+"','"+handle.getType().toString()+"','default','permission.itemid.38')");
			MySQL.getInstance().commandSync("INSERT INTO `GILDE_PERMISSIONS`(`uuid`, `section`, `group`, `permission`) VALUES ('"+handle.getHandle().getUuid()+"','"+handle.getType().toString()+"','default','group.default')");
		}
		for(String[] groupPerm : infos){
			GildPermissionGroup group = getGroup(groupPerm[0]);
			if(group == null)
				groups.add(group = new GildPermissionGroup(this, groupPerm[0]));
			group.permissions.add(groupPerm[1]);
		}
	}
	
	public ArrayList<String> getGroups() {
		ArrayList<String> out = new ArrayList<>();
		for (GildPermissionGroup g : groups)
			out.add(g.getName());
		return out;
	}

	public GildPermissionGroup getGroup(String name) {
		for (GildPermissionGroup g : groups)
			if (g.getName().equalsIgnoreCase(name))
				return g;
		System.out.println("Did not found permissiongroup "+name+" (Groups: "+groups+")");
		return null;
	}
	
	public GildPermissionGroup getGroup(int player) {
		return getGroup(players.get(new Integer(player)));
	}
	
	public void setGroup(int player,GildPermissionGroup group){
		System.out.println("Setgroup "+group+" for "+player);
		GildPermissionGroup old = getGroup(player);
		if(old != null && old.equals(group))
			return;
		if(old != null){
			players.remove(new Integer(player));
			MySQL.getInstance().command("DELETE FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getHandle().getUuid().toString()+"' AND `section` = '"+handle.getType().toString()+"' AND `playerId` = '"+player+"'");
		}else
		{
			if(group != null && !handle.players.contains(new Integer(player)))
				throw new IllegalArgumentException("Cant set permission of a player whitch isnt in gild.");
		}
		if(group != null){
			System.out.println("New group: "+group.getName());
			players.put(new Integer(player), group.getName());
			MySQL.getInstance().command("INSERT INTO `GILDE_MEMBERS`(`gilde`, `section`, `playerId`, `rank`) VALUES ('"+handle.getHandle().getUuid().toString()+"','"+handle.getType().toString()+"','"+player+"','"+group.getName()+"')");
		}
		
		if(handle.isActive())
			if(old != null && group != null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.CHANGE, player, handle.getType(), group.getName()));
			else if(group == null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.REMOVE, player, handle.getType(), null));
			else if(old == null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.ADD, player, handle.getType(), group.getName()));
	}

	public void deleteGroup(String group) {
		GildPermissionGroup g = getGroup(group);
		if(g == null)
			return;
		groups.remove(g);
		MySQL.getInstance().command("DELETE FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getUuid()+"' AND `group`='"+group+"' AND `section`='"+handle.getType().toString()+"'");
		MySQL.getInstance().command("UPDATE `GILDE_MEMBERS` SET `rank`='default' WHERE `gilde`='"+handle.getHandle().getUuid()+"' AND `section`='"+handle.getType().toString()+"' AND `rank`='"+group+"'");
		EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getUuid(), handle.getType(), GildePermissionEvent.Action.GROUP_REMOVE, group, null));
	}
	
	public void createGroup(String group) {
		GildPermissionGroup g = getGroup(group);
		if(g != null)
			return;
		groups.add(g = new GildPermissionGroup(this, group));
		g.addPermission("permission.itemid.6",false); //creates the group
		EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getUuid(), handle.getType(), GildePermissionEvent.Action.GROUP_ADD, group, null));
	}
}/*
-----------------------------------------------
package eu.epicpvp.gild;

import java.util.ArrayList;
import java.util.HashMap;

import eu.epicpvp.dataserver.protocoll.packets.PacketGildPermissionEdit;
import eu.epicpvp.dataserver.protocoll.packets.PacketGildPermissionEdit.Action;
import eu.epicpvp.event.EventHelper;
import eu.epicpvp.events.gilde.GildePermissionEvent;
import eu.epicpvp.events.gilde.GildePlayerEvent;
import eu.epicpvp.mysql.MySQL;
import lombok.Getter;

public class GildSectionPermission {
	public static final String TABLE_NAME = "GILDE_PERMISSIONS";
	
	@Getter
	private GildSection handle;

	private ArrayList<GildPermissionGroup> groups;
	protected HashMap<Integer, String> players = new HashMap<>();

	public GildSectionPermission(GildSection handle) {
		this.handle = handle;
	}

	public synchronized void loadGroups(){
		groups = new ArrayList<>();
		ArrayList<String[]> infos = MySQL.getInstance().querySync("SELECT `group`,`permission` FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getUuid().toString()+"' AND `section`='"+handle.getType().toString()+"'");
		if(infos.size() == 0){ //Auto insert default groups
			infos.add(new String[]{"owner","permission.itemid.338"});
			infos.add(new String[]{"default","permission.itemid.38"});
		}
		for(String[] groupPerm : infos){
			GildPermissionGroup group = getGroup(groupPerm[0]);
			if(group == null)
				groups.add(group = new GildPermissionGroup(this, groupPerm[0]));
			group.permissions.add(groupPerm[1]);
		}
	}
	
	public ArrayList<String> getGroups() {
		ArrayList<String> out = new ArrayList<>();
		for (GildPermissionGroup g : groups)
			out.add(g.getName());
		return out;
	}

	public GildPermissionGroup getGroup(String name) {
		for (GildPermissionGroup g : groups)
			if (g.getName().equalsIgnoreCase(name))
				return g;
		return null;
	}
	
	public GildPermissionGroup getGroup(int player) {
		return getGroup(players.get(new Integer(player)));
	}
	
	public void setGroup(int player,GildPermissionGroup group){
		GildPermissionGroup old = getGroup(player);
		if(old != null && old.equals(group))
			return;
		if(old != null){
			players.remove(new Integer(player));
			MySQL.getInstance().command("DELETE FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getHandle().getUuid().toString()+"' AND `section` = '"+handle.getType().toString()+"' AND `playerId` = '"+player+"'");
		}else
		{
			if(!handle.players.contains(new Integer(player)))
				throw new IllegalArgumentException("Cant set permission of a player whitch isnt in gild.");
		}
		if(group != null){
			players.put(new Integer(player), group.getName());
			MySQL.getInstance().command("INSERT INTO `GILDE_MEMBERS`(`gilde`, `section`, `playerId`, `rank`) VALUES ('"+handle.getHandle().getUuid().toString()+"','"+handle.getType().toString()+"','"+player+"','"+group.getName()+"')");
		}
		
		if(handle.isActive())
			if(old != null && group != null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.CHANGE, player, handle.getType(), group.getName()));
			else if(group == null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.REMOVE, player, handle.getType(), null));
			else if(old == null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.ADD, player, handle.getType(), group.getName()));
	}

	public void deleteGroup(String group) {
		GildPermissionGroup g = getGroup(group);
		if(g == null)
			return;
		groups.remove(g);
		MySQL.getInstance().command("DELETE FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getUuid()+"' AND `group`='"+group+"' AND `section`='"+handle.getType().toString()+"'");
		MySQL.getInstance().command("UPDATE `GILDE_MEMBERS` SET `rank`='default' WHERE `gilde`='"+handle.getHandle().getUuid()+"' AND `section`='"+handle.getType().toString()+"' AND `rank`='"+group+"'");
		EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getUuid(), handle.getType(), GildePermissionEvent.Action.GROUP_REMOVE, group, null));
	}
	
	public void createGroup(String group) {
		GildPermissionGroup g = getGroup(group);
		if(g != null)
			return;
		groups.add(g = new GildPermissionGroup(this, group));
		g.addPermission("group.importance-0",false); //creates the group
		EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getUuid(), handle.getType(), GildePermissionEvent.Action.GROUP_ADD, group, null));
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         
*//*
-----------------------------------------------
package eu.epicpvp.gild;

import java.util.ArrayList;
import java.util.HashMap;

import eu.epicpvp.dataserver.protocoll.packets.PacketGildPermissionEdit;
import eu.epicpvp.dataserver.protocoll.packets.PacketGildPermissionEdit.Action;
import eu.epicpvp.event.EventHelper;
import eu.epicpvp.events.gilde.GildePermissionEvent;
import eu.epicpvp.events.gilde.GildePlayerEvent;
import eu.epicpvp.mysql.MySQL;
import lombok.Getter;

public class GildSectionPermission {
	public static final String TABLE_NAME = "GILDE_PERMISSIONS";
	
	@Getter
	private GildSection handle;

	private ArrayList<GildPermissionGroup> groups;
	protected HashMap<Integer, String> players = new HashMap<>();

	public GildSectionPermission(GildSection handle) {
		this.handle = handle;
	}

	public synchronized void loadGroups(){
		groups = new ArrayList<>();
		ArrayList<String[]> infos = MySQL.getInstance().querySync("SELECT `group`,`permission` FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getUuid().toString()+"' AND `section`='"+handle.getType().toString()+"'");
		if(infos.size() == 0){ //Auto insert default groups
			infos.add({""});
		}
		for(String[] groupPerm : infos){
			GildPermissionGroup group = getGroup(groupPerm[0]);
			if(group == null)
				groups.add(group = new GildPermissionGroup(this, groupPerm[0]));
			group.permissions.add(groupPerm[1]);
		}
	}
	
	public ArrayList<String> getGroups() {
		ArrayList<String> out = new ArrayList<>();
		for (GildPermissionGroup g : groups)
			out.add(g.getName());
		return out;
	}

	public GildPermissionGroup getGroup(String name) {
		for (GildPermissionGroup g : groups)
			if (g.getName().equalsIgnoreCase(name))
				return g;
		return null;
	}
	
	public GildPermissionGroup getGroup(int player) {
		return getGroup(players.get(new Integer(player)));
	}
	
	public void setGroup(int player,GildPermissionGroup group){
		GildPermissionGroup old = getGroup(player);
		if(old != null && old.equals(group))
			return;
		if(old != null){
			players.remove(new Integer(player));
			MySQL.getInstance().command("DELETE FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getHandle().getUuid().toString()+"' AND `section` = '"+handle.getType().toString()+"' AND `playerId` = '"+player+"'");
		}else
		{
			if(!handle.players.contains(new Integer(player)))
				throw new IllegalArgumentException("Cant set permission of a player whitch isnt in gild.");
		}
		if(group != null){
			players.put(new Integer(player), group.getName());
			MySQL.getInstance().command("INSERT INTO `GILDE_MEMBERS`(`gilde`, `section`, `playerId`, `rank`) VALUES ('"+handle.getHandle().getUuid().toString()+"','"+handle.getType().toString()+"','"+player+"','"+group.getName()+"')");
		}
		
		if(handle.isActive())
			if(old != null && group != null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.CHANGE, player, handle.getType(), group.getName()));
			else if(group == null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.REMOVE, player, handle.getType(), null));
			else if(old == null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.ADD, player, handle.getType(), group.getName()));
	}

	public void deleteGroup(String group) {
		GildPermissionGroup g = getGroup(group);
		if(g == null)
			return;
		groups.remove(g);
		MySQL.getInstance().command("DELETE FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getUuid()+"' AND `group`='"+group+"' AND `section`='"+handle.getType().toString()+"'");
		MySQL.getInstance().command("UPDATE `GILDE_MEMBERS` SET `rank`='default' WHERE `gilde`='"+handle.getHandle().getUuid()+"' AND `section`='"+handle.getType().toString()+"' AND `rank`='"+group+"'");
		EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getUuid(), handle.getType(), GildePermissionEvent.Action.GROUP_REMOVE, group, null));
	}
	
	public void createGroup(String group) {
		GildPermissionGroup g = getGroup(group);
		if(g != null)
			return;
		groups.add(g = new GildPermissionGroup(this, group));
		g.addPermission("group.importance-0",false); //creates the group
		EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getUuid(), handle.getType(), GildePermissionEvent.Action.GROUP_ADD, group, null));
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                
*//*
-----------------------------------------------
package eu.epicpvp.gild;

import java.util.ArrayList;
import java.util.HashMap;

import eu.epicpvp.dataserver.protocoll.packets.PacketGildPermissionEdit;
import eu.epicpvp.dataserver.protocoll.packets.PacketGildPermissionEdit.Action;
import eu.epicpvp.event.EventHelper;
import eu.epicpvp.events.gilde.GildePermissionEvent;
import eu.epicpvp.events.gilde.GildePlayerEvent;
import eu.epicpvp.mysql.MySQL;
import lombok.Getter;

public class GildSectionPermission {
	public static final String TABLE_NAME = "GILDE_PERMISSIONS";
	
	@Getter
	private GildSection handle;

	private ArrayList<GildPermissionGroup> groups;
	protected HashMap<Integer, String> players = new HashMap<>();

	public GildSectionPermission(GildSection handle) {
		this.handle = handle;
	}

	public synchronized void loadGroups(){
		groups = new ArrayList<>();
		ArrayList<String[]> infos = MySQL.getInstance().querySync("SELECT `group`,`permission` FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getUuid().toString()+"' AND `section`='"+handle.getType().toString()+"'");
		if(infos.size() == 0){ //Auto insert default groups
			infos.add(new String[]{"owner","permission.itemid.338"});
			infos.add(new String[]{"default","permission.itemid.38"});
			MySQL.getInstance().commandSync("");
		}
		for(String[] groupPerm : infos){
			GildPermissionGroup group = getGroup(groupPerm[0]);
			if(group == null)
				groups.add(group = new GildPermissionGroup(this, groupPerm[0]));
			group.permissions.add(groupPerm[1]);
		}
	}
	
	public ArrayList<String> getGroups() {
		ArrayList<String> out = new ArrayList<>();
		for (GildPermissionGroup g : groups)
			out.add(g.getName());
		return out;
	}

	public GildPermissionGroup getGroup(String name) {
		for (GildPermissionGroup g : groups)
			if (g.getName().equalsIgnoreCase(name))
				return g;
		return null;
	}
	
	public GildPermissionGroup getGroup(int player) {
		return getGroup(players.get(new Integer(player)));
	}
	
	public void setGroup(int player,GildPermissionGroup group){
		GildPermissionGroup old = getGroup(player);
		if(old != null && old.equals(group))
			return;
		if(old != null){
			players.remove(new Integer(player));
			MySQL.getInstance().command("DELETE FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getHandle().getUuid().toString()+"' AND `section` = '"+handle.getType().toString()+"' AND `playerId` = '"+player+"'");
		}else
		{
			if(!handle.players.contains(new Integer(player)))
				throw new IllegalArgumentException("Cant set permission of a player whitch isnt in gild.");
		}
		if(group != null){
			players.put(new Integer(player), group.getName());
			MySQL.getInstance().command("INSERT INTO `GILDE_MEMBERS`(`gilde`, `section`, `playerId`, `rank`) VALUES ('"+handle.getHandle().getUuid().toString()+"','"+handle.getType().toString()+"','"+player+"','"+group.getName()+"')");
		}
		
		if(handle.isActive())
			if(old != null && group != null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.CHANGE, player, handle.getType(), group.getName()));
			else if(group == null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.REMOVE, player, handle.getType(), null));
			else if(old == null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.ADD, player, handle.getType(), group.getName()));
	}

	public void deleteGroup(String group) {
		GildPermissionGroup g = getGroup(group);
		if(g == null)
			return;
		groups.remove(g);
		MySQL.getInstance().command("DELETE FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getUuid()+"' AND `group`='"+group+"' AND `section`='"+handle.getType().toString()+"'");
		MySQL.getInstance().command("UPDATE `GILDE_MEMBERS` SET `rank`='default' WHERE `gilde`='"+handle.getHandle().getUuid()+"' AND `section`='"+handle.getType().toString()+"' AND `rank`='"+group+"'");
		EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getUuid(), handle.getType(), GildePermissionEvent.Action.GROUP_REMOVE, group, null));
	}
	
	public void createGroup(String group) {
		GildPermissionGroup g = getGroup(group);
		if(g != null)
			return;
		groups.add(g = new GildPermissionGroup(this, group));
		g.addPermission("group.importance-0",false); //creates the group
		EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getUuid(), handle.getType(), GildePermissionEvent.Action.GROUP_ADD, group, null));
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                 
*//*
-----------------------------------------------
package eu.epicpvp.gild;

import java.util.ArrayList;
import java.util.HashMap;

import eu.epicpvp.dataserver.protocoll.packets.PacketGildPermissionEdit;
import eu.epicpvp.dataserver.protocoll.packets.PacketGildPermissionEdit.Action;
import eu.epicpvp.event.EventHelper;
import eu.epicpvp.events.gilde.GildePermissionEvent;
import eu.epicpvp.events.gilde.GildePlayerEvent;
import eu.epicpvp.mysql.MySQL;
import lombok.Getter;

public class GildSectionPermission {
	public static final String TABLE_NAME = "GILDE_PERMISSIONS";
	
	@Getter
	private GildSection handle;

	private ArrayList<GildPermissionGroup> groups;
	protected HashMap<Integer, String> players = new HashMap<>();

	public GildSectionPermission(GildSection handle) {
		this.handle = handle;
	}

	public synchronized void loadGroups(){
		groups = new ArrayList<>();
		ArrayList<String[]> infos = MySQL.getInstance().querySync("SELECT `group`,`permission` FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getUuid().toString()+"' AND `section`='"+handle.getType().toString()+"'");
		if(infos.size() == 0){ //Auto insert default groups
			
		}
		for(String[] groupPerm : infos){
			GildPermissionGroup group = getGroup(groupPerm[0]);
			if(group == null)
				groups.add(group = new GildPermissionGroup(this, groupPerm[0]));
			group.permissions.add(groupPerm[1]);
		}
	}
	
	public ArrayList<String> getGroups() {
		ArrayList<String> out = new ArrayList<>();
		for (GildPermissionGroup g : groups)
			out.add(g.getName());
		return out;
	}

	public GildPermissionGroup getGroup(String name) {
		for (GildPermissionGroup g : groups)
			if (g.getName().equalsIgnoreCase(name))
				return g;
		return null;
	}
	
	public GildPermissionGroup getGroup(int player) {
		return getGroup(players.get(new Integer(player)));
	}
	
	public void setGroup(int player,GildPermissionGroup group){
		GildPermissionGroup old = getGroup(player);
		if(old != null && old.equals(group))
			return;
		if(old != null){
			players.remove(new Integer(player));
			MySQL.getInstance().command("DELETE FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getHandle().getUuid().toString()+"' AND `section` = '"+handle.getType().toString()+"' AND `playerId` = '"+player+"'");
		}else
		{
			if(!handle.players.contains(new Integer(player)))
				throw new IllegalArgumentException("Cant set permission of a player whitch isnt in gild.");
		}
		if(group != null){
			players.put(new Integer(player), group.getName());
			MySQL.getInstance().command("INSERT INTO `GILDE_MEMBERS`(`gilde`, `section`, `playerId`, `rank`) VALUES ('"+handle.getHandle().getUuid().toString()+"','"+handle.getType().toString()+"','"+player+"','"+group.getName()+"')");
		}
		
		if(handle.isActive())
			if(old != null && group != null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.CHANGE, player, handle.getType(), group.getName()));
			else if(group == null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.REMOVE, player, handle.getType(), null));
			else if(old == null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.ADD, player, handle.getType(), group.getName()));
	}

	public void deleteGroup(String group) {
		GildPermissionGroup g = getGroup(group);
		if(g == null)
			return;
		groups.remove(g);
		MySQL.getInstance().command("DELETE FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getUuid()+"' AND `group`='"+group+"' AND `section`='"+handle.getType().toString()+"'");
		MySQL.getInstance().command("UPDATE `GILDE_MEMBERS` SET `rank`='default' WHERE `gilde`='"+handle.getHandle().getUuid()+"' AND `section`='"+handle.getType().toString()+"' AND `rank`='"+group+"'");
		EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getUuid(), handle.getType(), GildePermissionEvent.Action.GROUP_REMOVE, group, null));
	}
	
	public void createGroup(String group) {
		GildPermissionGroup g = getGroup(group);
		if(g != null)
			return;
		groups.add(g = new GildPermissionGroup(this, group));
		g.addPermission("group.importance-0",false); //creates the group
		EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getUuid(), handle.getType(), GildePermissionEvent.Action.GROUP_ADD, group, null));
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                
*//*
-----------------------------------------------
package eu.epicpvp.gild;

import java.util.ArrayList;
import java.util.HashMap;

import eu.epicpvp.dataserver.protocoll.packets.PacketGildPermissionEdit;
import eu.epicpvp.dataserver.protocoll.packets.PacketGildPermissionEdit.Action;
import eu.epicpvp.event.EventHelper;
import eu.epicpvp.events.gilde.GildePermissionEvent;
import eu.epicpvp.events.gilde.GildePlayerEvent;
import eu.epicpvp.mysql.MySQL;
import lombok.Getter;

public class GildSectionPermission {
	public static final String TABLE_NAME = "GILDE_PERMISSIONS";
	
	@Getter
	private GildSection handle;

	private ArrayList<GildPermissionGroup> groups;
	protected HashMap<Integer, String> players = new HashMap<>();

	public GildSectionPermission(GildSection handle) {
		this.handle = handle;
	}

	public synchronized void loadGroups(){
		groups = new ArrayList<>();
		ArrayList<String[]> infos = MySQL.getInstance().querySync("SELECT `group`,`permission` FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getUuid().toString()+"' AND `section`='"+handle.getType().toString()+"'");
		if(infos.size() == 0){ //Auto insert default groups
			
		}
		for(String[] groupPerm : infos){
			GildPermissionGroup group = getGroup(groupPerm[0]);
			if(group == null)
				groups.add(group = new GildPermissionGroup(this, groupPerm[0]));
			group.permissions.add(groupPerm[1]);
		}
	}
	
	public ArrayList<String> getGroups() {
		ArrayList<String> out = new ArrayList<>();
		for (GildPermissionGroup g : groups)
			out.add(g.getName());
		return out;
	}

	public GildPermissionGroup getGroup(String name) {
		for (GildPermissionGroup g : groups)
			if (g.getName().equalsIgnoreCase(name))
				return g;
		return null;
	}
	
	public GildPermissionGroup getGroup(int player) {
		return getGroup(players.get(new Integer(player)));
	}
	
	public void setGroup(int player,GildPermissionGroup group){
		GildPermissionGroup old = getGroup(player);
		if(old != null && old.equals(group))
			return;
		if(old != null){
			players.remove(new Integer(player));
			MySQL.getInstance().command("DELETE FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getHandle().getUuid().toString()+"' AND `section` = '"+handle.getType().toString()+"' AND `playerId` = '"+player+"'");
		}else
		{
			if(!handle.players.contains(new Integer(player)))
				throw new IllegalArgumentException("Cant set permission of a player whitch isnt in gild.");
		}
		if(group != null){
			players.put(new Integer(player), group.getName());
			MySQL.getInstance().command("INSERT INTO `GILDE_MEMBERS`(`gilde`, `section`, `playerId`, `rank`) VALUES ('"+handle.getHandle().getUuid().toString()+"','"+handle.getType().toString()+"','"+player+"','"+group.getName()+"')");
		}
		
		if(handle.isActive())
			if(old != null && group != null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.CHANGE, player, handle.getType(), group.getName()));
			else if(group == null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.REMOVE, player, handle.getType(), null));
			else if(old == null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.ADD, player, handle.getType(), group.getName()));
	}

	public void deleteGroup(String group) {
		GildPermissionGroup g = getGroup(group);
		if(g == null)
			return;
		groups.remove(g);
		MySQL.getInstance().command("DELETE FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getUuid()+"' AND `group`='"+group+"' AND `section`='"+handle.getType().toString()+"'");
		MySQL.getInstance().command("UPDATE `GILDE_MEMBERS` SET `rank`='default' WHERE `gilde`='"+handle.getHandle().getUuid()+"' AND `section`='"+handle.getType().toString()+"' AND `rank`='"+group+"'");
		EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getUuid(), handle.getType(), GildePermissionEvent.Action.GROUP_REMOVE, group, null));
	}
	
	public void createGroup(String group) {
		GildPermissionGroup g = getGroup(group);
		if(g != null)
			return;
		groups.add(g = new GildPermissionGroup(this, group));
		g.addPermission("group.importance-0",false); //creates the group
		EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getUuid(), handle.getType(), GildePermissionEvent.Action.GROUP_ADD, group, null));
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                
*//*
-----------------------------------------------
package eu.epicpvp.gild;

import java.util.ArrayList;
import java.util.HashMap;

import eu.epicpvp.dataserver.protocoll.packets.PacketGildPermissionEdit;
import eu.epicpvp.dataserver.protocoll.packets.PacketGildPermissionEdit.Action;
import eu.epicpvp.event.EventHelper;
import eu.epicpvp.events.gilde.GildePermissionEvent;
import eu.epicpvp.events.gilde.GildePlayerEvent;
import eu.epicpvp.mysql.MySQL;
import lombok.Getter;

public class GildSectionPermission {
	public static final String TABLE_NAME = "GILDE_PERMISSIONS";
	
	@Getter
	private GildSection handle;

	private ArrayList<GildPermissionGroup> groups;
	protected HashMap<Integer, String> players = new HashMap<>();

	public GildSectionPermission(GildSection handle) {
		this.handle = handle;
	}

	public synchronized void loadGroups(){
		groups = new ArrayList<>();
		ArrayList<String[]> infos = MySQL.getInstance().querySync("SELECT `group`,`permission` FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getUuid().toString()+"' AND `section`='"+handle.getType().toString()+"'");
		if(infos.size() == 0){ //Auto insert default groups
			infos.add(new String[]{"owner","permission.itemid.338"});
			infos.add(new String[]{"default","permission.itemid.38"});
			MySQL.getInstance().commandSync("");
		}
		for(String[] groupPerm : infos){
			GildPermissionGroup group = getGroup(groupPerm[0]);
			if(group == null)
				groups.add(group = new GildPermissionGroup(this, groupPerm[0]));
			group.permissions.add(groupPerm[1]);
		}
	}
	
	public ArrayList<String> getGroups() {
		ArrayList<String> out = new ArrayList<>();
		for (GildPermissionGroup g : groups)
			out.add(g.getName());
		return out;
	}

	public GildPermissionGroup getGroup(String name) {
		for (GildPermissionGroup g : groups)
			if (g.getName().equalsIgnoreCase(name))
				return g;
		return null;
	}
	
	public GildPermissionGroup getGroup(int player) {
		return getGroup(players.get(new Integer(player)));
	}
	
	public void setGroup(int player,GildPermissionGroup group){
		GildPermissionGroup old = getGroup(player);
		if(old != null && old.equals(group))
			return;
		if(old != null){
			players.remove(new Integer(player));
			MySQL.getInstance().command("DELETE FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getHandle().getUuid().toString()+"' AND `section` = '"+handle.getType().toString()+"' AND `playerId` = '"+player+"'");
		}else
		{
			if(!handle.players.contains(new Integer(player)))
				throw new IllegalArgumentException("Cant set permission of a player whitch isnt in gild.");
		}
		if(group != null){
			players.put(new Integer(player), group.getName());
			MySQL.getInstance().command("INSERT INTO `GILDE_MEMBERS`(`gilde`, `section`, `playerId`, `rank`) VALUES ('"+handle.getHandle().getUuid().toString()+"','"+handle.getType().toString()+"','"+player+"','"+group.getName()+"')");
		}
		
		if(handle.isActive())
			if(old != null && group != null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.CHANGE, player, handle.getType(), group.getName()));
			else if(group == null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.REMOVE, player, handle.getType(), null));
			else if(old == null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.ADD, player, handle.getType(), group.getName()));
	}

	public void deleteGroup(String group) {
		GildPermissionGroup g = getGroup(group);
		if(g == null)
			return;
		groups.remove(g);
		MySQL.getInstance().command("DELETE FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getUuid()+"' AND `group`='"+group+"' AND `section`='"+handle.getType().toString()+"'");
		MySQL.getInstance().command("UPDATE `GILDE_MEMBERS` SET `rank`='default' WHERE `gilde`='"+handle.getHandle().getUuid()+"' AND `section`='"+handle.getType().toString()+"' AND `rank`='"+group+"'");
		EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getUuid(), handle.getType(), GildePermissionEvent.Action.GROUP_REMOVE, group, null));
	}
	
	public void createGroup(String group) {
		GildPermissionGroup g = getGroup(group);
		if(g != null)
			return;
		groups.add(g = new GildPermissionGroup(this, group));
		g.addPermission("group.importance-0",false); //creates the group
		EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getUuid(), handle.getType(), GildePermissionEvent.Action.GROUP_ADD, group, null));
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                 
*//*
-----------------------------------------------
package eu.epicpvp.gild;

import java.util.ArrayList;
import java.util.HashMap;

import eu.epicpvp.dataserver.protocoll.packets.PacketGildPermissionEdit;
import eu.epicpvp.dataserver.protocoll.packets.PacketGildPermissionEdit.Action;
import eu.epicpvp.event.EventHelper;
import eu.epicpvp.events.gilde.GildePermissionEvent;
import eu.epicpvp.events.gilde.GildePlayerEvent;
import eu.epicpvp.mysql.MySQL;
import lombok.Getter;

public class GildSectionPermission {
	public static final String TABLE_NAME = "GILDE_PERMISSIONS";
	
	@Getter
	private GildSection handle;

	private ArrayList<GildPermissionGroup> groups;
	protected HashMap<Integer, String> players = new HashMap<>();

	public GildSectionPermission(GildSection handle) {
		this.handle = handle;
	}

	public synchronized void loadGroups(){
		groups = new ArrayList<>();
		ArrayList<String[]> infos = MySQL.getInstance().querySync("SELECT `group`,`permission` FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getUuid().toString()+"' AND `section`='"+handle.getType().toString()+"'");
		if(infos.size() == 0){ //Auto insert default groups
			infos.add({""});
		}
		for(String[] groupPerm : infos){
			GildPermissionGroup group = getGroup(groupPerm[0]);
			if(group == null)
				groups.add(group = new GildPermissionGroup(this, groupPerm[0]));
			group.permissions.add(groupPerm[1]);
		}
	}
	
	public ArrayList<String> getGroups() {
		ArrayList<String> out = new ArrayList<>();
		for (GildPermissionGroup g : groups)
			out.add(g.getName());
		return out;
	}

	public GildPermissionGroup getGroup(String name) {
		for (GildPermissionGroup g : groups)
			if (g.getName().equalsIgnoreCase(name))
				return g;
		return null;
	}
	
	public GildPermissionGroup getGroup(int player) {
		return getGroup(players.get(new Integer(player)));
	}
	
	public void setGroup(int player,GildPermissionGroup group){
		GildPermissionGroup old = getGroup(player);
		if(old != null && old.equals(group))
			return;
		if(old != null){
			players.remove(new Integer(player));
			MySQL.getInstance().command("DELETE FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getHandle().getUuid().toString()+"' AND `section` = '"+handle.getType().toString()+"' AND `playerId` = '"+player+"'");
		}else
		{
			if(!handle.players.contains(new Integer(player)))
				throw new IllegalArgumentException("Cant set permission of a player whitch isnt in gild.");
		}
		if(group != null){
			players.put(new Integer(player), group.getName());
			MySQL.getInstance().command("INSERT INTO `GILDE_MEMBERS`(`gilde`, `section`, `playerId`, `rank`) VALUES ('"+handle.getHandle().getUuid().toString()+"','"+handle.getType().toString()+"','"+player+"','"+group.getName()+"')");
		}
		
		if(handle.isActive())
			if(old != null && group != null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.CHANGE, player, handle.getType(), group.getName()));
			else if(group == null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.REMOVE, player, handle.getType(), null));
			else if(old == null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.ADD, player, handle.getType(), group.getName()));
	}

	public void deleteGroup(String group) {
		GildPermissionGroup g = getGroup(group);
		if(g == null)
			return;
		groups.remove(g);
		MySQL.getInstance().command("DELETE FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getUuid()+"' AND `group`='"+group+"' AND `section`='"+handle.getType().toString()+"'");
		MySQL.getInstance().command("UPDATE `GILDE_MEMBERS` SET `rank`='default' WHERE `gilde`='"+handle.getHandle().getUuid()+"' AND `section`='"+handle.getType().toString()+"' AND `rank`='"+group+"'");
		EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getUuid(), handle.getType(), GildePermissionEvent.Action.GROUP_REMOVE, group, null));
	}
	
	public void createGroup(String group) {
		GildPermissionGroup g = getGroup(group);
		if(g != null)
			return;
		groups.add(g = new GildPermissionGroup(this, group));
		g.addPermission("group.importance-0",false); //creates the group
		EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getUuid(), handle.getType(), GildePermissionEvent.Action.GROUP_ADD, group, null));
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                
*//*
-----------------------------------------------
package eu.epicpvp.gild;

import java.util.ArrayList;
import java.util.HashMap;

import eu.epicpvp.dataserver.protocoll.packets.PacketGildPermissionEdit;
import eu.epicpvp.dataserver.protocoll.packets.PacketGildPermissionEdit.Action;
import eu.epicpvp.event.EventHelper;
import eu.epicpvp.events.gilde.GildePermissionEvent;
import eu.epicpvp.events.gilde.GildePlayerEvent;
import eu.epicpvp.mysql.MySQL;
import lombok.Getter;

public class GildSectionPermission {
	public static final String TABLE_NAME = "GILDE_PERMISSIONS";
	
	@Getter
	private GildSection handle;

	private ArrayList<GildPermissionGroup> groups;
	protected HashMap<Integer, String> players = new HashMap<>();

	public GildSectionPermission(GildSection handle) {
		this.handle = handle;
	}

	public synchronized void loadGroups(){
		groups = new ArrayList<>();
		ArrayList<String[]> infos = MySQL.getInstance().querySync("SELECT `group`,`permission` FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getUuid().toString()+"' AND `section`='"+handle.getType().toString()+"'");
		if(infos.size() == 0){ //Auto insert default groups
			infos.add(new String[]{"owner","permission.itemid.338"});
			infos.add(new String[]{"default","permission.itemid.38"});
		}
		for(String[] groupPerm : infos){
			GildPermissionGroup group = getGroup(groupPerm[0]);
			if(group == null)
				groups.add(group = new GildPermissionGroup(this, groupPerm[0]));
			group.permissions.add(groupPerm[1]);
		}
	}
	
	public ArrayList<String> getGroups() {
		ArrayList<String> out = new ArrayList<>();
		for (GildPermissionGroup g : groups)
			out.add(g.getName());
		return out;
	}

	public GildPermissionGroup getGroup(String name) {
		for (GildPermissionGroup g : groups)
			if (g.getName().equalsIgnoreCase(name))
				return g;
		return null;
	}
	
	public GildPermissionGroup getGroup(int player) {
		return getGroup(players.get(new Integer(player)));
	}
	
	public void setGroup(int player,GildPermissionGroup group){
		GildPermissionGroup old = getGroup(player);
		if(old != null && old.equals(group))
			return;
		if(old != null){
			players.remove(new Integer(player));
			MySQL.getInstance().command("DELETE FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getHandle().getUuid().toString()+"' AND `section` = '"+handle.getType().toString()+"' AND `playerId` = '"+player+"'");
		}else
		{
			if(!handle.players.contains(new Integer(player)))
				throw new IllegalArgumentException("Cant set permission of a player whitch isnt in gild.");
		}
		if(group != null){
			players.put(new Integer(player), group.getName());
			MySQL.getInstance().command("INSERT INTO `GILDE_MEMBERS`(`gilde`, `section`, `playerId`, `rank`) VALUES ('"+handle.getHandle().getUuid().toString()+"','"+handle.getType().toString()+"','"+player+"','"+group.getName()+"')");
		}
		
		if(handle.isActive())
			if(old != null && group != null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.CHANGE, player, handle.getType(), group.getName()));
			else if(group == null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.REMOVE, player, handle.getType(), null));
			else if(old == null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.ADD, player, handle.getType(), group.getName()));
	}

	public void deleteGroup(String group) {
		GildPermissionGroup g = getGroup(group);
		if(g == null)
			return;
		groups.remove(g);
		MySQL.getInstance().command("DELETE FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getUuid()+"' AND `group`='"+group+"' AND `section`='"+handle.getType().toString()+"'");
		MySQL.getInstance().command("UPDATE `GILDE_MEMBERS` SET `rank`='default' WHERE `gilde`='"+handle.getHandle().getUuid()+"' AND `section`='"+handle.getType().toString()+"' AND `rank`='"+group+"'");
		EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getUuid(), handle.getType(), GildePermissionEvent.Action.GROUP_REMOVE, group, null));
	}
	
	public void createGroup(String group) {
		GildPermissionGroup g = getGroup(group);
		if(g != null)
			return;
		groups.add(g = new GildPermissionGroup(this, group));
		g.addPermission("group.importance-0",false); //creates the group
		EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getUuid(), handle.getType(), GildePermissionEvent.Action.GROUP_ADD, group, null));
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         
*//*
-----------------------------------------------
package eu.epicpvp.gild;

import java.util.ArrayList;
import java.util.HashMap;

import eu.epicpvp.dataserver.protocoll.packets.PacketGildPermissionEdit;
import eu.epicpvp.dataserver.protocoll.packets.PacketGildPermissionEdit.Action;
import eu.epicpvp.event.EventHelper;
import eu.epicpvp.events.gilde.GildePermissionEvent;
import eu.epicpvp.events.gilde.GildePlayerEvent;
import eu.epicpvp.mysql.MySQL;
import lombok.Getter;

public class GildSectionPermission {
	public static final String TABLE_NAME = "GILDE_PERMISSIONS";
	
	@Getter
	private GildSection handle;

	private ArrayList<GildPermissionGroup> groups;
	protected HashMap<Integer, String> players = new HashMap<>();

	public GildSectionPermission(GildSection handle) {
		this.handle = handle;
	}

	public synchronized void loadGroups(){
		groups = new ArrayList<>();
		ArrayList<String[]> infos = MySQL.getInstance().querySync("SELECT `group`,`permission` FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getUuid().toString()+"' AND `section`='"+handle.getType().toString()+"'");
		if(infos.size() == 0){ //Auto insert default groups
			infos.add(new String[]{"owner","permission.itemid.338"});
			infos.add(new String[]{"default","permission.itemid.38"});
			MySQL.getInstance().commandSync("INSERT INTO `GILDE_PERMISSIONS`(`uuid`, `section`, `group`, `permission`) VALUES ('"+handle.getHandle().getUuid()+"','"+handle.getType().toString()+"','owner','permission.itemid.338')");
			MySQL.getInstance().commandSync("INSERT INTO `GILDE_PERMISSIONS`(`uuid`, `section`, `group`, `permission`) VALUES ('"+handle.getHandle().getUuid()+"','"+handle.getType().toString()+"','default','permission.itemid.38')");
		}
		for(String[] groupPerm : infos){
			GildPermissionGroup group = getGroup(groupPerm[0]);
			if(group == null)
				groups.add(group = new GildPermissionGroup(this, groupPerm[0]));
			group.permissions.add(groupPerm[1]);
		}
	}
	
	public ArrayList<String> getGroups() {
		ArrayList<String> out = new ArrayList<>();
		for (GildPermissionGroup g : groups)
			out.add(g.getName());
		return out;
	}

	public GildPermissionGroup getGroup(String name) {
		for (GildPermissionGroup g : groups)
			if (g.getName().equalsIgnoreCase(name))
				return g;
		return null;
	}
	
	public GildPermissionGroup getGroup(int player) {
		return getGroup(players.get(new Integer(player)));
	}
	
	public void setGroup(int player,GildPermissionGroup group){
		GildPermissionGroup old = getGroup(player);
		if(old != null && old.equals(group))
			return;
		if(old != null){
			players.remove(new Integer(player));
			MySQL.getInstance().command("DELETE FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getHandle().getUuid().toString()+"' AND `section` = '"+handle.getType().toString()+"' AND `playerId` = '"+player+"'");
		}else
		{
			if(!handle.players.contains(new Integer(player)))
				throw new IllegalArgumentException("Cant set permission of a player whitch isnt in gild.");
		}
		if(group != null){
			players.put(new Integer(player), group.getName());
			MySQL.getInstance().command("INSERT INTO `GILDE_MEMBERS`(`gilde`, `section`, `playerId`, `rank`) VALUES ('"+handle.getHandle().getUuid().toString()+"','"+handle.getType().toString()+"','"+player+"','"+group.getName()+"')");
		}
		
		if(handle.isActive())
			if(old != null && group != null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.CHANGE, player, handle.getType(), group.getName()));
			else if(group == null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.REMOVE, player, handle.getType(), null));
			else if(old == null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.ADD, player, handle.getType(), group.getName()));
	}

	public void deleteGroup(String group) {
		GildPermissionGroup g = getGroup(group);
		if(g == null)
			return;
		groups.remove(g);
		MySQL.getInstance().command("DELETE FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getUuid()+"' AND `group`='"+group+"' AND `section`='"+handle.getType().toString()+"'");
		MySQL.getInstance().command("UPDATE `GILDE_MEMBERS` SET `rank`='default' WHERE `gilde`='"+handle.getHandle().getUuid()+"' AND `section`='"+handle.getType().toString()+"' AND `rank`='"+group+"'");
		EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getUuid(), handle.getType(), GildePermissionEvent.Action.GROUP_REMOVE, group, null));
	}
	
	public void createGroup(String group) {
		GildPermissionGroup g = getGroup(group);
		if(g != null)
			return;
		groups.add(g = new GildPermissionGroup(this, group));
		g.addPermission("group.importance-0",false); //creates the group
		EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getUuid(), handle.getType(), GildePermissionEvent.Action.GROUP_ADD, group, null));
	}
}
                                          
*//*
-----------------------------------------------
package eu.epicpvp.gild;

import java.util.ArrayList;
import java.util.HashMap;

import eu.epicpvp.dataserver.protocoll.packets.PacketGildPermissionEdit;
import eu.epicpvp.dataserver.protocoll.packets.PacketGildPermissionEdit.Action;
import eu.epicpvp.event.EventHelper;
import eu.epicpvp.events.gilde.GildePermissionEvent;
import eu.epicpvp.events.gilde.GildePlayerEvent;
import eu.epicpvp.mysql.MySQL;
import lombok.Getter;

public class GildSectionPermission {
	public static final String TABLE_NAME = "GILDE_PERMISSIONS";
	
	@Getter
	private GildSection handle;

	private ArrayList<GildPermissionGroup> groups;
	protected HashMap<Integer, String> players = new HashMap<>();

	public GildSectionPermission(GildSection handle) {
		this.handle = handle;
	}

	public synchronized void loadGroups(){
		groups = new ArrayList<>();
		ArrayList<String[]> infos = MySQL.getInstance().querySync("SELECT `group`,`permission` FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getUuid().toString()+"' AND `section`='"+handle.getType().toString()+"'");
		if(infos.size() == 0){ //Auto insert default groups
			infos.add(new String[]{"owner","permission.itemid.338"});
			infos.add(new String[]{"default","permission.itemid.38"});
			MySQL.getInstance().commandSync("INSERT INTO `GILDE_PERMISSIONS`(`uuid`, `section`, `group`, `permission`) VALUES ('"+handle.getHandle().getUuid()+"','"+handle.getType().toString()+"','owner','permission.itemid.338')");
			MySQL.getInstance().commandSync("INSERT INTO `GILDE_PERMISSIONS`(`uuid`, `section`, `group`, `permission`) VALUES ('"+handle.getHandle().getUuid()+"','"+handle.getType().toString()+"','default','permission.itemid.38')");
		}
		for(String[] groupPerm : infos){
			GildPermissionGroup group = getGroup(groupPerm[0]);
			if(group == null)
				groups.add(group = new GildPermissionGroup(this, groupPerm[0]));
			group.permissions.add(groupPerm[1]);
		}
	}
	
	public ArrayList<String> getGroups() {
		ArrayList<String> out = new ArrayList<>();
		for (GildPermissionGroup g : groups)
			out.add(g.getName());
		return out;
	}

	public GildPermissionGroup getGroup(String name) {
		for (GildPermissionGroup g : groups)
			if (g.getName().equalsIgnoreCase(name))
				return g;
		return null;
	}
	
	public GildPermissionGroup getGroup(int player) {
		return getGroup(players.get(new Integer(player)));
	}
	
	public void setGroup(int player,GildPermissionGroup group){
		GildPermissionGroup old = getGroup(player);
		if(old != null && old.equals(group))
			return;
		if(old != null){
			players.remove(new Integer(player));
			MySQL.getInstance().command("DELETE FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getHandle().getUuid().toString()+"' AND `section` = '"+handle.getType().toString()+"' AND `playerId` = '"+player+"'");
		}else
		{
			if(!handle.players.contains(new Integer(player)))
				throw new IllegalArgumentException("Cant set permission of a player whitch isnt in gild.");
		}
		if(group != null){
			players.put(new Integer(player), group.getName());
			MySQL.getInstance().command("INSERT INTO `GILDE_MEMBERS`(`gilde`, `section`, `playerId`, `rank`) VALUES ('"+handle.getHandle().getUuid().toString()+"','"+handle.getType().toString()+"','"+player+"','"+group.getName()+"')");
		}
		
		if(handle.isActive())
			if(old != null && group != null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.CHANGE, player, handle.getType(), group.getName()));
			else if(group == null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.REMOVE, player, handle.getType(), null));
			else if(old == null)
				EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.ADD, player, handle.getType(), group.getName()));
	}

	public void deleteGroup(String group) {
		GildPermissionGroup g = getGroup(group);
		if(g == null)
			return;
		groups.remove(g);
		MySQL.getInstance().command("DELETE FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getUuid()+"' AND `group`='"+group+"' AND `section`='"+handle.getType().toString()+"'");
		MySQL.getInstance().command("UPDATE `GILDE_MEMBERS` SET `rank`='default' WHERE `gilde`='"+handle.getHandle().getUuid()+"' AND `section`='"+handle.getType().toString()+"' AND `rank`='"+group+"'");
		EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getUuid(), handle.getType(), GildePermissionEvent.Action.GROUP_REMOVE, group, null));
	}
	
	public void createGroup(String group) {
		GildPermissionGroup g = getGroup(group);
		if(g != null)
			return;
		groups.add(g = new GildPermissionGroup(this, group));
		g.addPermission("group.importance-0",false); //creates the group
		EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getUuid(), handle.getType(), GildePermissionEvent.Action.GROUP_ADD, group, null));
	}
}
                                          
*//*
-----------------------------------------------
package eu.epicpvp.gild;

import java.util.ArrayList;
import java.util.HashMap;

import eu.epicpvp.dataserver.protocoll.packets.PacketGildPermissionEdit;
import eu.epicpvp.dataserver.protocoll.packets.PacketGildPermissionEdit.Action;
import eu.epicpvp.event.EventHelper;
import eu.epicpvp.events.gilde.GildePermissionEvent;
import eu.epicpvp.events.gilde.GildePlayerEvent;
import eu.epicpvp.mysql.MySQL;
import lombok.Getter;

public class GildSectionPermission {
	public static final String TABLE_NAME = "GILDE_PERMISSIONS";
	
	@Getter
	private GildSection handle;

	private ArrayList<GildPermissionGroup> groups;
	protected HashMap<Integer, String> players = new HashMap<>();

	public GildSectionPermission(GildSection handle) {
		this.handle = handle;
	}

	public synchronized void loadGroups(){
		groups = new ArrayList<>();
		ArrayList<String[]> infos = MySQL.getInstance().querySync("SELECT `group`,`permission` FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getUuid().toString()+"'");
		for(String[] groupPerm : infos){
			GildPermissionGroup group = getGroup(groupPerm[0]);
			if(group == null)
				groups.add(group = new GildPermissionGroup(this, groupPerm[0]));
			group.permissions.add(groupPerm[1]);
		}
	}
	
	public ArrayList<String> getGroups() {
		ArrayList<String> out = new ArrayList<>();
		for (GildPermissionGroup g : groups)
			out.add(g.getName());
		return out;
	}

	public GildPermissionGroup getGroup(String name) {
		for (GildPermissionGroup g : groups)
			if (g.getName().equalsIgnoreCase(name))
				return g;
		return null;
	}
	
	public GildPermissionGroup getGroup(int player) {
		return getGroup(players.get(new Integer(player)));
	}
	
	public void setGroup(int player,GildPermissionGroup group){
		GildPermissionGroup old = getGroup(player);
		if(old != null && old.equals(group))
			return;
		if(old != null){
			players.remove(new Integer(player));
			MySQL.getInstance().command("DELETE FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getHandle().getUuid().toString()+"' AND `section` = '"+handle.getType().toString()+"' AND `playerId` = '"+player+"'");
		}else
		{
			if(!handle.players.contains(new Integer(player)))
				throw new IllegalArgumentException("Cant set permission of a player whitch isnt in gild.");
		}
		if(group != null){
			players.put(new Integer(player), group.getName());
		}
		
		if(old != null && group != null)
			EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.CHANGE, player, handle.getType(), group.getName()));
		else if(group == null)
			EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.REMOVE, player, handle.getType(), null));
		else if(old == null)
			EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.ADD, player, handle.getType(), group.getName()));
	}

	public void unloadGroup(String group) {
		groups.remove(getGroup(group));
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
*//*
-----------------------------------------------
package eu.epicpvp.gild;

import java.util.ArrayList;
import java.util.HashMap;

import eu.epicpvp.dataserver.protocoll.packets.PacketGildPermissionEdit;
import eu.epicpvp.dataserver.protocoll.packets.PacketGildPermissionEdit.Action;
import eu.epicpvp.event.EventHelper;
import eu.epicpvp.events.gilde.GildePermissionEvent;
import eu.epicpvp.events.gilde.GildePlayerEvent;
import eu.epicpvp.mysql.MySQL;
import lombok.Getter;

public class GildSectionPermission {
	public static final String TABLE_NAME = "GILDE_PERMISSIONS";
	
	@Getter
	private GildSection handle;

	private ArrayList<GildPermissionGroup> groups;
	protected HashMap<Integer, String> players = new HashMap<>();

	public GildSectionPermission(GildSection handle) {
		this.handle = handle;
	}

	public synchronized void loadGroups(){
		groups = new ArrayList<>();
		ArrayList<String[]> infos = MySQL.getInstance().querySync("SELECT `group`,`permission` FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getUuid().toString()+"' AND `section`='"+handle.getType().toString()+"'");
		for(String[] groupPerm : infos){
			GildPermissionGroup group = getGroup(groupPerm[0]);
			if(group == null)
				groups.add(group = new GildPermissionGroup(this, groupPerm[0]));
			group.permissions.add(groupPerm[1]);
		}
	}
	
	public ArrayList<String> getGroups() {
		ArrayList<String> out = new ArrayList<>();
		for (GildPermissionGroup g : groups)
			out.add(g.getName());
		return out;
	}

	public GildPermissionGroup getGroup(String name) {
		for (GildPermissionGroup g : groups)
			if (g.getName().equalsIgnoreCase(name))
				return g;
		return null;
	}
	
	public GildPermissionGroup getGroup(int player) {
		return getGroup(players.get(new Integer(player)));
	}
	
	public void setGroup(int player,GildPermissionGroup group){
		GildPermissionGroup old = getGroup(player);
		if(old != null && old.equals(group))
			return;
		if(old != null){
			players.remove(new Integer(player));
			MySQL.getInstance().command("DELETE FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getHandle().getUuid().toString()+"' AND `section` = '"+handle.getType().toString()+"' AND `playerId` = '"+player+"'");
		}else
		{
			if(!handle.players.contains(new Integer(player)))
				throw new IllegalArgumentException("Cant set permission of a player whitch isnt in gild.");
		}
		if(group != null){
			players.put(new Integer(player), group.getName());
			MySQL.getInstance().command("INSERT INTO `GILDE_MEMBERS`(`gilde`, `section`, `playerId`, `rank`) VALUES ('"+handle.getHandle().getUuid().toString()+"','"+handle.getType().toString()+"','"+player+"','"+group.getName()+"')");
		}
		
		if(old != null && group != null)
			EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.CHANGE, player, handle.getType(), group.getName()));
		else if(group == null)
			EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.REMOVE, player, handle.getType(), null));
		else if(old == null)
			EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildePlayerEvent(handle.getHandle().getUuid(), GildePlayerEvent.Action.ADD, player, handle.getType(), group.getName()));
	}

	public void deleteGroup(String group) {
		groups.remove(getGroup(group));
		MySQL.getInstance().command("DELETE FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getUuid()+"' AND `group`='"+group+"' AND `section`='"+handle.getType().toString()+"'");
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
*//*
-----------------------------------------------
package eu.epicpvp.gild;

import java.util.ArrayList;
import java.util.HashMap;

import eu.epicpvp.dataserver.protocoll.packets.PacketGildPermissionEdit;
import eu.epicpvp.dataserver.protocoll.packets.PacketGildPermissionEdit.Action;
import eu.epicpvp.mysql.MySQL;
import lombok.Getter;

public class GildSectionPermission {
	public static final String TABLE_NAME = "GILDE_PERMISSIONS";
	
	@Getter
	private GildSection handle;

	private ArrayList<GildPermissionGroup> groups;
	protected HashMap<Integer, String> players = new HashMap<>();

	public GildSectionPermission(GildSection handle) {
		this.handle = handle;
	}

	public synchronized void loadGroups(){
		groups = new ArrayList<>();
		ArrayList<String[]> infos = MySQL.getInstance().querySync("SELECT `group`,`permission` FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getUuid().toString()+"'");
		for(String[] groupPerm : infos){
			GildPermissionGroup group = getGroup(groupPerm[0]);
			if(group == null)
				groups.add(group = new GildPermissionGroup(this, groupPerm[0]));
			group.permissions.add(groupPerm[1]);
		}
	}
	
	public ArrayList<String> getGroups() {
		ArrayList<String> out = new ArrayList<>();
		for (GildPermissionGroup g : groups)
			out.add(g.getName());
		return out;
	}

	public GildPermissionGroup getGroup(String name) {
		for (GildPermissionGroup g : groups)
			if (g.getName().equalsIgnoreCase(name))
				return g;
		return null;
	}
	
	public GildPermissionGroup getGroup(int player) {
		return getGroup(players.get(new Integer(player)));
	}
	
	public void setGroup(int player,GildPermissionGroup group){
		GildPermissionGroup old = getGroup(player);
		if(old != null && old.equals(group))
			return;
		if(old != null){
			players.remove(new Integer(player));
			MySQL.getInstance().command("DELETE FROM `GILDE_MEMBERS` WHERE `gilde`='' AND `section` = '' AND `playerId` = ''");
		}else
		{
			if(!handle.players.contains(new Integer(player.getPlayerId())))
				throw new IllegalArgumentException("Cant set permission of a player whitch isnt in gild.");
		}
		if(group != null){
			handle.getHandle().getConnection().writePacket(new PacketGildPermissionEdit(handle.getHandle().getUuid(), handle.getType(), Action.ADD_GROUP, String.valueOf(player.getPlayerId()), group.getName()));
			players.put(new Integer(player.getPlayerId()), group.getName());
		}
	}

	public void unloadGroup(String group) {
		groups.remove(getGroup(group));
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
*//*
-----------------------------------------------
package eu.epicpvp.gild;

import java.util.ArrayList;
import java.util.HashMap;

import eu.epicpvp.dataserver.protocoll.packets.PacketGildPermissionEdit;
import eu.epicpvp.dataserver.protocoll.packets.PacketGildPermissionEdit.Action;
import eu.epicpvp.mysql.MySQL;
import lombok.Getter;

public class GildSectionPermission {
	public static final String TABLE_NAME = "GILDE_PERMISSIONS";
	
	@Getter
	private GildSection handle;

	private ArrayList<GildPermissionGroup> groups;
	protected HashMap<Integer, String> players = new HashMap<>();

	public GildSectionPermission(GildSection handle) {
		this.handle = handle;
	}

	public synchronized void loadGroups(){
		groups = new ArrayList<>();
		ArrayList<String[]> infos = MySQL.getInstance().querySync("SELECT `group`,`permission` FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getUuid().toString()+"'");
		for(String[] groupPerm : infos){
			GildPermissionGroup group = getGroup(groupPerm[0]);
			if(group == null)
				groups.add(group = new GildPermissionGroup(this, groupPerm[0]));
			group.permissions.add(groupPerm[1]);
		}
	}
	
	public ArrayList<String> getGroups() {
		ArrayList<String> out = new ArrayList<>();
		for (GildPermissionGroup g : groups)
			out.add(g.getName());
		return out;
	}

	public GildPermissionGroup getGroup(String name) {
		for (GildPermissionGroup g : groups)
			if (g.getName().equalsIgnoreCase(name))
				return g;
		return null;
	}
	
	public GildPermissionGroup getGroup(int player) {
		return getGroup(players.get(new Integer(player)));
	}
	
	public void setGroup(int player,GildPermissionGroup group){
		GildPermissionGroup old = getGroup(player);
		if(old != null && old.equals(group))
			return;
		if(old != null){
			players.remove(new Integer(player));
			MySQL.getInstance().command("DELETE FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getHandle().getUuid().toString()+"' AND `section` = '"+handle.getType().toString()+"' AND `playerId` = '"+player+"'");
		}else
		{
			if(!handle.players.contains(new Integer(player.getPlayerId())))
				throw new IllegalArgumentException("Cant set permission of a player whitch isnt in gild.");
		}
		if(group != null){
			handle.getHandle().getConnection().writePacket(new PacketGildPermissionEdit(handle.getHandle().getUuid(), handle.getType(), Action.ADD_GROUP, String.valueOf(player.getPlayerId()), group.getName()));
			players.put(new Integer(player.getPlayerId()), group.getName());
		}
	}

	public void unloadGroup(String group) {
		groups.remove(getGroup(group));
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        
*//*
-----------------------------------------------
package eu.epicpvp.gild;

import java.util.ArrayList;
import java.util.HashMap;

import eu.epicpvp.dataserver.protocoll.packets.PacketGildPermissionEdit;
import eu.epicpvp.dataserver.protocoll.packets.PacketGildPermissionEdit.Action;
import eu.epicpvp.mysql.MySQL;
import lombok.Getter;

public class GildSectionPermission {
	public static final String TABLE_NAME = "GILDE_PERMISSIONS";
	
	@Getter
	private GildSection handle;

	private ArrayList<GildPermissionGroup> groups;
	protected HashMap<Integer, String> players = new HashMap<>();

	public GildSectionPermission(GildSection handle) {
		this.handle = handle;
	}

	public synchronized void loadGroups(){
		groups = new ArrayList<>();
		ArrayList<String[]> infos = MySQL.getInstance().querySync("SELECT `group`,`permission` FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getUuid().toString()+"'");
		for(String[] groupPerm : infos){
			GildPermissionGroup group = getGroup(groupPerm[0]);
			if(group == null)
				groups.add(group = new GildPermissionGroup(this, groupPerm[0]));
			group.permissions.add(groupPerm[1]);
		}
	}
	
	public ArrayList<String> getGroups() {
		ArrayList<String> out = new ArrayList<>();
		for (GildPermissionGroup g : groups)
			out.add(g.getName());
		return out;
	}

	public GildPermissionGroup getGroup(String name) {
		for (GildPermissionGroup g : groups)
			if (g.getName().equalsIgnoreCase(name))
				return g;
		return null;
	}
	
	public GildPermissionGroup getGroup(int player) {
		return getGroup(players.get(new Integer(player)));
	}
	
	public void setGroup(int player,GildPermissionGroup group){
		GildPermissionGroup old = getGroup(player);
		if(old != null && old.equals(group))
			return;
		if(old != null){
			players.remove(new Integer(player));
			MySQL.getInstance().command("DELETE FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getHandle().getUuid().toString()+"' AND `section` = '"+handle.getType().toString()+"' AND `playerId` = '"+player+"'");
		}else
		{
			if(!handle.players.contains(new Integer(player.getPlayerId())))
				throw new IllegalArgumentException("Cant set permission of a player whitch isnt in gild.");
		}
		if(group != null){
			handle.getHandle().getConnection().writePacket(new PacketGildPermissionEdit(handle.getHandle().getUuid(), handle.getType(), Action.ADD_GROUP, String.valueOf(player.getPlayerId()), group.getName()));
			players.put(new Integer(player.getPlayerId()), group.getName());
		}
	}

	public void unloadGroup(String group) {
		groups.remove(getGroup(group));
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        
*//*
-----------------------------------------------
package eu.epicpvp.gild;

import java.util.ArrayList;
import java.util.HashMap;

import eu.epicpvp.client.LoadedPlayer;
import eu.epicpvp.dataserver.protocoll.packets.PacketGildPermissionEdit;
import eu.epicpvp.dataserver.protocoll.packets.PacketGildPermissionEdit.Action;
import lombok.Getter;

public class GildSectionPermission {
	@Getter
	private GildSection handle;

	private ArrayList<GildPermissionGroup> groups;
	protected HashMap<Integer, String> players = new HashMap<>();

	public GildSectionPermission(GildSection handle) {
		this.handle = handle;
	}

	private synchronized void init() {
		if (groups == null) {
			ArrayList<String> names = handle.getHandle().getConnection().getGildGroups(this).getSync();
			groups = new ArrayList<>();
			for (String s : names)
				groups.add(new GildPermissionGroup(this, s));
		}
	}

	public synchronized void reload(){
		if(groups == null)
			throw new RuntimeException("Cant reload before loading");
		ArrayList<String> names = handle.getHandle().getConnection().getGildGroups(this).getSync();
		for (String s : names)
			if(getGroup(s) == null)
				loadGroup(s);
		for(GildPermissionGroup g : new ArrayList<>(groups))
			if(!names.contains(g.getName()))
				groups.remove(g);
	}
	
	public ArrayList<String> getGroups() {
		init();
		ArrayList<String> out = new ArrayList<>();
		for (GildPermissionGroup g : groups)
			out.add(g.getName());
		return out;
	}

	public GildPermissionGroup getGroup(String name) {
		init();
		for (GildPermissionGroup g : groups)
			if (g.getName().equalsIgnoreCase(name))
				return g;
		return null;
	}

	protected void loadGroup(String name){
		if(getGroup(name) != null)
			return;
		groups.add(new GildPermissionGroup(this, name));
	}
	
	public GildPermissionGroup getGroup(LoadedPlayer player) {
		init();
		return getGroup(players.get(new Integer(player.getPlayerId())));
	}
	
	public void setGroup(LoadedPlayer player,GildPermissionGroup group){
		GildPermissionGroup old = getGroup(player);
		if(old != null && old.equals(group))
			return;
		if(old != null){
			handle.getHandle().getConnection().writePacket(new PacketGildPermissionEdit(handle.getHandle().getUuid(), handle.getType(), Action.REMOVE_GROUP, String.valueOf(player.getPlayerId()), old.getName()));
			players.remove(new Integer(player.getPlayerId()));
		}else
		{
			if(!handle.players.contains(new Integer(player.getPlayerId())))
				throw new IllegalArgumentException("Cant set permission of a player whitch isnt in gild.");
		}
		if(group != null){
			handle.getHandle().getConnection().writePacket(new PacketGildPermissionEdit(handle.getHandle().getUuid(), handle.getType(), Action.ADD_GROUP, String.valueOf(player.getPlayerId()), group.getName()));
			players.put(new Integer(player.getPlayerId()), group.getName());
		}
	}

	public void unloadGroup(String group) {
		groups.remove(getGroup(group));
	}
}
                                                                                                                                                                                          
*/