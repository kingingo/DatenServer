package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePermissionEvent;
import dev.wolveringer.mysql.MySQL;
import lombok.Getter;

public class GildPermissionGroup {
	@Getter
	private GildSectionPermission handle;
	
	@Getter
	private String name;
	protected ArrayList<String> permissions = new ArrayList<>();
	
	public GildPermissionGroup(GildSectionPermission handle,String name) {
		this.name = name;
		this.handle = handle;
	}
	
	public List<String> getPermissions() {
		return Collections.unmodifiableList(permissions);
	}
	
	public void removePermission(String permission){
		if(permissions.remove(permission))
			MySQL.getInstance().command("DELETE FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getHandle().getUuid().toString()+"' AND `group`='"+name+"' AND `section`='"+handle.getHandle().getType().toString()+"' AND `permission`='"+permission+"'");
		EventHelper.callGildEvent(handle.getHandle().getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getHandle().getUuid(), handle.getHandle().getType(), GildePermissionEvent.Action.REMOVE, name, permission));
	}
	public void addPermission(String permission){
		addPermission(permission, true);
	}
	public void addPermission(String permission,boolean callevent){
		if(!permission.contains(permission)){
			permissions.add(permission);
			MySQL.getInstance().command("INSERT INTO `GILDE_PERMISSIONS`(`uuid`, `section`, `group`, `permission`) VALUES ('"+handle.getHandle().getHandle().getUuid().toString()+"', '"+handle.getHandle().getType().toString()+"','"+name+"','"+permission+"')");
			if(callevent)
				EventHelper.callGildEvent(handle.getHandle().getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getHandle().getUuid(), handle.getHandle().getType(), GildePermissionEvent.Action.ADD, name, permission));
		}
	}
	public boolean hasPermission(String permission){
		return permissions.contains(permission);
	}
}/*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import dev.wolveringer.client.ClientWrapper;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildPermissionEdit;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildPermissionEdit.Action;
import lombok.Getter;

public class GildPermissionGroup {
	@Getter
	private GildSectionPermission handle;
	
	@Getter
	private String name;
	protected ArrayList<String> permissions;
	
	public GildPermissionGroup(GildSectionPermission handle,String name) {
		this.name = name;
		this.handle = handle;
	}
	
	private synchronized void init(){
		if(permissions == null){
			ClientWrapper connection = handle.getHandle().getHandle().getConnection();
			permissions = connection.getPermissions(this).getSync();
		}
	}
	
	public void reload(){
		permissions = null;
		init();
	}
	
	public List<String> getPermissions() {
		return Collections.unmodifiableList(permissions);
	}
	
	public void removePermission(String permission){
		init();
		if(permissions.remove(permission)){
			handle.getHandle().getHandle().getConnection().writePacket(new PacketGildPermissionEdit(handle.getHandle().getHandle().getUuid(), handle.getHandle().getType(), Action.REMOVE_PERMISSION, name, permission));
		}
	}
	public void addPermission(String permission){
		init();
		if(!permissions.contains(permission)){
			handle.getHandle().getHandle().getConnection().writePacket(new PacketGildPermissionEdit(handle.getHandle().getHandle().getUuid(), handle.getHandle().getType(), Action.ADD_PERMISSION, name, permission));
			permissions.add(permission);
		}
	}
	public boolean hasPermission(String permission){
		init();
		return permissions.contains(permission);
	}
	public ArrayList<LoadedPlayer> getPlayers(){
		ArrayList<LoadedPlayer> players = new ArrayList<>();
		for(Entry<Integer, String> player : handle.players.entrySet())
			if(player.getValue().equalsIgnoreCase(name))
				players.add(handle.getHandle().getHandle().getConnection().getPlayer(player.getKey()));
		return players;
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import dev.wolveringer.client.ClientWrapper;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildPermissionEdit;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildPermissionEdit.Action;
import lombok.Getter;

public class GildPermissionGroup {
	@Getter
	private GildSectionPermission handle;
	
	@Getter
	private String name;
	protected ArrayList<String> permissions;
	
	public GildPermissionGroup(GildSectionPermission handle,String name) {
		this.name = name;
		this.handle = handle;
	}
	
	private synchronized void init(){
		if(permissions == null){
			ClientWrapper connection = handle.getHandle().getHandle().getConnection();
			permissions = connection.getPermissions(this).getSync();
		}
	}
	
	public void reload(){
		permissions = null;
		init();
	}
	
	public List<String> getPermissions() {
		return Collections.unmodifiableList(permissions);
	}
	
	public void removePermission(String permission){
		
	}
	public void addPermission(String permission){
	
	}
	public boolean hasPermission(String permission){
		init();
		return permissions.contains(permission);
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildPermissionEdit;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildPermissionEdit.Action;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePermissionEvent;
import dev.wolveringer.mysql.MySQL;
import lombok.Getter;

public class GildPermissionGroup {
	@Getter
	private GildSectionPermission handle;
	
	@Getter
	private String name;
	protected ArrayList<String> permissions = new ArrayList<>();
	
	public GildPermissionGroup(GildSectionPermission handle,String name) {
		this.name = name;
		this.handle = handle;
	}
	
	public void reload(){
	}
	
	public List<String> getPermissions() {
		return Collections.unmodifiableList(permissions);
	}
	
	public void removePermission(String permission){
		if(permissions.remove(permission))
			MySQL.getInstance().command("DELETE FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getHandle().getUuid().toString()+"' AND `group`='"+name+"' AND `permission`='"+permission+"'");
		EventHelper.callGildEvent(handle.getHandle().getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getHandle().getUuid(), handle.getHandle().getType(), Action.REMOVE_PERMISSION, name, permission));
	}
	public void addPermission(String permission){
		if(!permission.contains(permission)){
			permissions.add(permission);
			MySQL.getInstance().command("INSERT INTO `GILDE_PERMISSIONS`(`uuid`, `group`, `permission`) VALUES ('"+handle.getHandle().getHandle().getUuid().toString()+"','"+name+"','"+permission+"')");
			EventHelper.callGildEvent(handle.getHandle().getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getHandle().getUuid(), handle.getHandle().getType(), Action.ADD_PERMISSION, name, permission));
		}
	}
	public boolean hasPermission(String permission){
		return permissions.contains(permission);
	}
}
                                 
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildPermissionEdit;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildPermissionEdit.Action;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePermissionEvent;
import dev.wolveringer.mysql.MySQL;
import lombok.Getter;

public class GildPermissionGroup {
	@Getter
	private GildSectionPermission handle;
	
	@Getter
	private String name;
	protected ArrayList<String> permissions = new ArrayList<>();
	
	public GildPermissionGroup(GildSectionPermission handle,String name) {
		this.name = name;
		this.handle = handle;
	}
	
	public void reload(){
	}
	
	public List<String> getPermissions() {
		return Collections.unmodifiableList(permissions);
	}
	
	public void removePermission(String permission){
		if(permissions.remove(permission))
			MySQL.getInstance().command("DELETE FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getHandle().getUuid().toString()+"' AND `group`='"+name+"' AND `permission`='"+permission+"'");
	}
	public void addPermission(String permission){
		if(!permission.contains(permission)){
			permissions.add(permission);
			MySQL.getInstance().command("INSERT INTO `GILDE_PERMISSIONS`(`uuid`, `group`, `permission`) VALUES ('"+handle.getHandle().getHandle().getUuid().toString()+"','"+name+"','"+permission+"')");
			EventHelper.callGildEvent(handle.getHandle().getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getHandle().getUuid(), handle.getHandle().getType(), Action.ADD_PERMISSION, name, permission));
		}
	}
	public boolean hasPermission(String permission){
		return permissions.contains(permission);
	}
}
                                                                                                                                                                                                                                                      
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePermissionEvent;
import dev.wolveringer.mysql.MySQL;
import lombok.Getter;

public class GildPermissionGroup {
	@Getter
	private GildSectionPermission handle;
	
	@Getter
	private String name;
	protected ArrayList<String> permissions = new ArrayList<>();
	
	public GildPermissionGroup(GildSectionPermission handle,String name) {
		this.name = name;
		this.handle = handle;
	}
	
	public void reload(){
		//TODO
	}
	
	public List<String> getPermissions() {
		return Collections.unmodifiableList(permissions);
	}
	
	public void removePermission(String permission){
		if(permissions.remove(permission))
			MySQL.getInstance().command("DELETE FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getHandle().getUuid().toString()+"' AND `group`='"+name+"' AND `permission`='"+permission+"'");
		EventHelper.callGildEvent(handle.getHandle().getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getHandle().getUuid(), handle.getHandle().getType(), GildePermissionEvent.Action.REMOVE, name, permission));
	}
	public void addPermission(String permission){
		if(!permission.contains(permission)){
			permissions.add(permission);
			MySQL.getInstance().command("INSERT INTO `GILDE_PERMISSIONS`(`uuid`, `group`, `permission`) VALUES ('"+handle.getHandle().getHandle().getUuid().toString()+"','"+name+"','"+permission+"')");
			EventHelper.callGildEvent(handle.getHandle().getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getHandle().getUuid(), handle.getHandle().getType(), GildePermissionEvent.Action.ADD, name, permission));
		}
	}
	public boolean hasPermission(String permission){
		return permissions.contains(permission);
	}
}
                                                                                                                                                                                                   
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePermissionEvent;
import dev.wolveringer.mysql.MySQL;
import lombok.Getter;

public class GildPermissionGroup {
	@Getter
	private GildSectionPermission handle;
	
	@Getter
	private String name;
	protected ArrayList<String> permissions = new ArrayList<>();
	
	public GildPermissionGroup(GildSectionPermission handle,String name) {
		this.name = name;
		this.handle = handle;
	}
	
	public List<String> getPermissions() {
		return Collections.unmodifiableList(permissions);
	}
	
	public void removePermission(String permission){
		if(permissions.remove(permission))
			MySQL.getInstance().command("DELETE FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getHandle().getUuid().toString()+"' AND `group`='"+name+"' AND `section`='"+handle.getHandle().getType().toString()+"' AND `permission`='"+permission+"'");
		EventHelper.callGildEvent(handle.getHandle().getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getHandle().getUuid(), handle.getHandle().getType(), GildePermissionEvent.Action.REMOVE, name, permission));
	}
	public void addPermission(String permission){
		if(!permission.contains(permission)){
			permissions.add(permission);
			MySQL.getInstance().command("INSERT INTO `GILDE_PERMISSIONS`(`uuid`, `section`, `group`, `permission`) VALUES ('"+handle.getHandle().getHandle().getUuid().toString()+"', '"+handle.getHandle().getType().toString()+"','"+name+"','"+permission+"')");
			EventHelper.callGildEvent(handle.getHandle().getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getHandle().getUuid(), handle.getHandle().getType(), GildePermissionEvent.Action.ADD, name, permission));
		}
	}
	public boolean hasPermission(String permission){
		return permissions.contains(permission);
	}
}
                                                                                                                  
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import dev.wolveringer.client.ClientWrapper;
import dev.wolveringer.client.LoadedPlayer;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildPermissionEdit;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildPermissionEdit.Action;
import lombok.Getter;

public class GildPermissionGroup {
	@Getter
	private GildSectionPermission handle;
	
	@Getter
	private String name;
	protected ArrayList<String> permissions;
	
	public GildPermissionGroup(GildSectionPermission handle,String name) {
		this.name = name;
		this.handle = handle;
	}
	
	public void reload(){
		permissions = null;
		init();
	}
	
	public List<String> getPermissions() {
		return Collections.unmodifiableList(permissions);
	}
	
	public void removePermission(String permission){
		
	}
	public void addPermission(String permission){
	
	}
	public boolean hasPermission(String permission){
		return permissions.contains(permission);
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
*/