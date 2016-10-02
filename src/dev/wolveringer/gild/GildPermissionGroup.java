package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePermissionEvent;
import dev.wolveringer.mysql.MySQL;
import lombok.Getter;
import lombok.ToString;

@ToString
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
		if(permissions.remove(permission.toLowerCase()))
			MySQL.getInstance().command("DELETE FROM `GILDE_PERMISSIONS` WHERE `uuid`='"+handle.getHandle().getHandle().getUuid().toString()+"' AND `group`='"+name+"' AND `section`='"+handle.getHandle().getType().toString()+"' AND `permission`='"+permission+"'");
		EventHelper.callGildEvent(handle.getHandle().getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getHandle().getUuid(), handle.getHandle().getType(), GildePermissionEvent.Action.REMOVE, name, permission));
	}
	public void addPermission(String permission){
		addPermission(permission, true);
	}
	public void addPermission(String permission,boolean callevent){
		if(!permissions.contains(permission)){
			permissions.add(permission);
			MySQL.getInstance().command("INSERT INTO `GILDE_PERMISSIONS`(`uuid`, `section`, `group`, `permission`) VALUES ('"+handle.getHandle().getHandle().getUuid().toString()+"', '"+handle.getHandle().getType().toString()+"','"+name+"','"+permission+"')");
			if(callevent)
				EventHelper.callGildEvent(handle.getHandle().getHandle().getUuid(), new GildePermissionEvent(handle.getHandle().getHandle().getUuid(), handle.getHandle().getType(), GildePermissionEvent.Action.ADD, name, permission));
		}
	}
	public boolean hasPermission(String permission){
		return permissions.contains(permission);
	}
}