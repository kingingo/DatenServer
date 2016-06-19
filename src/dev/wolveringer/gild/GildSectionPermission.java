package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.HashMap;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildPermissionEdit;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildPermissionEdit.Action;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePermissionEvent;
import dev.wolveringer.events.gilde.GildePlayerEvent;
import dev.wolveringer.mysql.MySQL;
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
