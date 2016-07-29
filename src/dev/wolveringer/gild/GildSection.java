package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildCostumDataAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildUpdateSectionStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction.Action;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePlayerEvent;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.nbt.NBTCompressedStreamTools;
import dev.wolveringer.nbt.NBTTagCompound;
import lombok.Getter;

public class GildSection {
	private static final String MEMBER_TABLE_NAME = "GILDE_MEMBERS";
	
	private static final String COSTUM_DATA_KEY = "costum_data";
	
	@Getter
	private Gilde handle;
	@Getter
	private GildeType type;
	@Getter
	protected boolean active;
	private GildSectionPermission permissions = new GildSectionPermission(this);
	private NBTTagCompound costumData;
	
	protected ArrayList<Integer> players = new ArrayList<>();
	
	public GildSection(Gilde handle, GildeType type,boolean active,HashMap<InformationKey, String> infos) {
		this.handle = handle;
		this.type = type;
		this.active = active;
		try {
			if(infos.get(new InformationKey(type, COSTUM_DATA_KEY)).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(infos.get(new InformationKey(type, COSTUM_DATA_KEY)));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		permissions.loadGroups();
		loadData();
	}

	public void loadData(){
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `key`,`value` FROM "+Gilde.TABLE_NAME+" WHERE `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		HashMap<String, String> values = new InitHashMap<String, String>() {
			@Override
			public String defaultValue(String key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(info[0], info[1]);
		
		try {
			if(values.get(COSTUM_DATA_KEY).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(values.get(COSTUM_DATA_KEY));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		
		ArrayList<String[]> members = MySQL.getInstance().querySync("SELECT `playerId`, `rank` FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		for(String[] member : members){
			players.add(Integer.parseInt(member[0]));
			permissions.players.put(Integer.parseInt(member[0]), member[1]);
		}
	}
	
	public GildSectionPermission getPermission(){
		return permissions;
	}
	
	public List<Integer> getPlayers() {
		return Collections.unmodifiableList(players);
	}
	
	public void removePlayer(int player){
		if(players.contains(new Integer(player))){
			players.remove(new Integer(player));
			permissions.setGroup(player, null);
		}
	}
	public void addPlayer(int player,String rank){
		if(!players.contains(new Integer(player))){
			players.add(new Integer(player));
			permissions.setGroup(player, permissions.getGroup(rank));
		}
	}
	
	public NBTTagCompound getCostumData() {
		return costumData;
	}
	
	public void setCostumData(NBTTagCompound comp){
		this.costumData = comp;
		try {
			MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+NBTCompressedStreamTools.toString(comp)+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='"+COSTUM_DATA_KEY+"'");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setActive(boolean active) {
		if(active == this.active)
			return;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+active+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='active'");
		this.active = active;
		if(!active)
			for(Integer member : players)
				removePlayer(member);
		if(active)
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.ACTIVE_GILD_SECTION));
		else
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.DEACTIVE_GILD_SECTION));
	}
}/*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildCostumDataAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildUpdateSectionStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction.Action;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePlayerEvent;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.nbt.NBTCompressedStreamTools;
import dev.wolveringer.nbt.NBTTagCompound;
import lombok.Getter;

public class GildSection {
	private static final String MEMBER_TABLE_NAME = "GILDE_MEMBERS";
	
	private static final String COSTUM_DATA_KEY = "costum_data";
	
	@Getter
	private Gilde handle;
	@Getter
	private GildeType type;
	@Getter
	protected boolean active;
	private GildSectionPermission permissions = new GildSectionPermission(this);
	private NBTTagCompound costumData;
	
	protected ArrayList<Integer> players = new ArrayList<>();
	
	public GildSection(Gilde handle, GildeType type,boolean active,HashMap<InformationKey, String> infos) {
		this.handle = handle;
		this.type = type;
		this.active = active;
		try {
			if(infos.get(new InformationKey(type, COSTUM_DATA_KEY)).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(infos.get(new InformationKey(type, COSTUM_DATA_KEY)));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		permissions.loadGroups();
		loadData();
	}

	public void loadData(){
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `key`,`value` FROM "+Gilde.TABLE_NAME+" WHERE `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		HashMap<String, String> values = new InitHashMap<String, String>() {
			@Override
			public String defaultValue(String key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(info[0], info[1]);
		
		try {
			if(values.get(COSTUM_DATA_KEY).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(values.get(COSTUM_DATA_KEY));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		
		ArrayList<String[]> members = MySQL.getInstance().querySync("SELECT `playerId`, `rank` FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		for(String[] member : members){
			players.add(Integer.parseInt(member[0]));
			permissions.players.put(Integer.parseInt(member[0]), member[1]);
		}
	}
	
	public GildSectionPermission getPermission(){
		return permissions;
	}
	
	public List<Integer> getPlayers() {
		return Collections.unmodifiableList(players);
	}
	
	public void removePlayer(int player){
		if(players.contains(new Integer(player))){
			players.remove(new Integer(player));
			permissions.setGroup(player, null);
		}
	}
	public void addPlayer(int player,String rank){
		if(!players.contains(new Integer(player))){
			players.add(new Integer(player));
		}
		permissions.setGroup(player, permissions.getGroup(rank));
	}
	
	public NBTTagCompound getCostumData() {
		return costumData;
	}
	
	public void setCostumData(NBTTagCompound comp){
		this.costumData = comp;
		try {
			MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+NBTCompressedStreamTools.toString(comp)+"' WHERE  `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='"+COSTUM_DATA_KEY+"'");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setActive(boolean active) {
		if(active == this.active)
			return;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+active+"' WHERE  `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='active'");
		this.active = active;
		if(!active)
			for(Integer member : players)
				removePlayer(member);
		if(active)
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.ACTIVE_GILD_SECTION));
		else
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.DEACTIVE_GILD_SECTION));
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildCostumDataAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildUpdateSectionStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction.Action;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePlayerEvent;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.nbt.NBTCompressedStreamTools;
import dev.wolveringer.nbt.NBTTagCompound;
import lombok.Getter;

public class GildSection {
	private static final String MEMBER_TABLE_NAME = "GILDE_MEMBERS";
	
	private static final String COSTUM_DATA_KEY = "costum_data";
	
	@Getter
	private Gilde handle;
	@Getter
	private GildeType type;
	@Getter
	protected boolean active;
	private GildSectionPermission permissions = new GildSectionPermission(this);
	private NBTTagCompound costumData;
	
	protected ArrayList<Integer> players = new ArrayList<>();
	
	public GildSection(Gilde handle, GildeType type,boolean active,HashMap<InformationKey, String> infos) {
		this.handle = handle;
		this.type = type;
		this.active = active;
		try {
			if(infos.get(new InformationKey(type, COSTUM_DATA_KEY)).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(infos.get(COSTUM_DATA_KEY));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		permissions.loadGroups();
		loadData();
	}

	public void loadData(){
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `key`,`value` FROM "+Gilde.TABLE_NAME+" WHERE `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		HashMap<String, String> values = new InitHashMap<String, String>() {
			@Override
			public String defaultValue(String key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(info[0], info[1]);
		
		try {
			if(values.get(COSTUM_DATA_KEY).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(values.get(COSTUM_DATA_KEY));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		
		ArrayList<String[]> members = MySQL.getInstance().querySync("SELECT `playerId`, `rank` FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		for(String[] member : members){
			players.add(Integer.parseInt(member[0]));
			permissions.players.put(Integer.parseInt(member[0]), member[1]);
		}
	}
	
	public GildSectionPermission getPermission(){
		return permissions;
	}
	
	public List<Integer> getPlayers() {
		return Collections.unmodifiableList(players);
	}
	
	public void removePlayer(int player){
		if(players.contains(new Integer(player))){
			players.remove(new Integer(player));
			permissions.setGroup(player, null);
		}
	}
	public void addPlayer(int player,String rank){
		if(!players.contains(new Integer(player))){
			players.add(new Integer(player));
			permissions.setGroup(player, permissions.getGroup(rank));
		}
	}
	
	public NBTTagCompound getCostumData() {
		return costumData;
	}
	
	public void setCostumData(NBTTagCompound comp){
		this.costumData = comp;
		try {
			MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+NBTCompressedStreamTools.toString(comp)+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='"+COSTUM_DATA_KEY+"'");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setActive(boolean active) {
		if(active == this.active)
			return;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+active+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='active'");
		this.active = active;
		if(!active)
			for(Integer member : players)
				removePlayer(member);
		if(active)
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.ACTIVE_GILD_SECTION));
		else
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.DEACTIVE_GILD_SECTION));
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildCostumDataAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildUpdateSectionStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction.Action;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePlayerEvent;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.nbt.NBTCompressedStreamTools;
import dev.wolveringer.nbt.NBTTagCompound;
import lombok.Getter;

public class GildSection {
	private static final String MEMBER_TABLE_NAME = "GILDE_MEMBERS";
	
	private static final String COSTUM_DATA_KEY = "costum_data";
	
	@Getter
	private Gilde handle;
	@Getter
	private GildeType type;
	@Getter
	protected boolean active;
	private GildSectionPermission permissions = new GildSectionPermission(this);
	private NBTTagCompound costumData;
	
	protected ArrayList<Integer> players = new ArrayList<>();
	
	public GildSection(Gilde handle, GildeType type,boolean active,HashMap<InformationKey, String> infos) {
		this.handle = handle;
		this.type = type;
		this.active = active;
		try {
			if(infos.get(new InformationKey(type, COSTUM_DATA_KEY)).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(infos.get(new InformationKey(type, COSTUM_DATA_KEY)));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		permissions.loadGroups();
		loadData();
	}

	public void loadData(){
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `key`,`value` FROM "+Gilde.TABLE_NAME+" WHERE `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		HashMap<String, String> values = new InitHashMap<String, String>() {
			@Override
			public String defaultValue(String key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(info[0], info[1]);
		
		try {
			if(values.get(COSTUM_DATA_KEY).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(values.get(COSTUM_DATA_KEY));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		
		ArrayList<String[]> members = MySQL.getInstance().querySync("SELECT `playerId`, `rank` FROM `GILDE_MEMBERS` WHERE `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		for(String[] member : members){
			players.add(Integer.parseInt(member[0]));
			permissions.players.put(Integer.parseInt(member[0]), member[1]);
		}
	}
	
	public GildSectionPermission getPermission(){
		return permissions;
	}
	
	public List<Integer> getPlayers() {
		return Collections.unmodifiableList(players);
	}
	
	public void removePlayer(int player){
		if(players.contains(new Integer(player))){
			players.remove(new Integer(player));
			permissions.setGroup(player, null);
		}
	}
	public void addPlayer(int player,String rank){
		if(!players.contains(new Integer(player))){
			players.add(new Integer(player));
			permissions.setGroup(player, permissions.getGroup(rank));
		}
	}
	
	public NBTTagCompound getCostumData() {
		return costumData;
	}
	
	public void setCostumData(NBTTagCompound comp){
		this.costumData = comp;
		try {
			MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+NBTCompressedStreamTools.toString(comp)+"' WHERE  `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='"+COSTUM_DATA_KEY+"'");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setActive(boolean active) {
		if(active == this.active)
			return;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+active+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='active'");
		this.active = active;
		if(!active)
			for(Integer member : players)
				removePlayer(member);
		if(active)
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.ACTIVE_GILD_SECTION));
		else
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.DEACTIVE_GILD_SECTION));
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildCostumDataAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildUpdateSectionStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction.Action;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePlayerEvent;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.nbt.NBTCompressedStreamTools;
import dev.wolveringer.nbt.NBTTagCompound;
import lombok.Getter;

public class GildSection {
	private static final String MEMBER_TABLE_NAME = "GILDE_MEMBERS";
	
	private static final String COSTUM_DATA_KEY = "costum_data";
	
	@Getter
	private Gilde handle;
	@Getter
	private GildeType type;
	@Getter
	protected boolean active;
	private GildSectionPermission permissions = new GildSectionPermission(this);
	private NBTTagCompound costumData;
	
	protected ArrayList<Integer> players = new ArrayList<>();
	
	public GildSection(Gilde handle, GildeType type,boolean active,HashMap<InformationKey, String> infos) {
		this.handle = handle;
		this.type = type;
		this.active = active;
		try {
			if(infos.get(new InformationKey(type, COSTUM_DATA_KEY)).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(infos.get(new InformationKey(type, COSTUM_DATA_KEY)));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		permissions.loadGroups();
		loadData();
	}

	public void loadData(){
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `key`,`value` FROM "+Gilde.TABLE_NAME+" WHERE `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		HashMap<String, String> values = new InitHashMap<String, String>() {
			@Override
			public String defaultValue(String key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(info[0], info[1]);
		
		try {
			if(values.get(COSTUM_DATA_KEY).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(values.get(COSTUM_DATA_KEY));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		
		ArrayList<String[]> members = MySQL.getInstance().querySync("SELECT `playerId`, `rank` FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		for(String[] member : members){
			players.add(Integer.parseInt(member[0]));
			permissions.players.put(Integer.parseInt(member[0]), member[1]);
		}
	}
	
	public GildSectionPermission getPermission(){
		return permissions;
	}
	
	public List<Integer> getPlayers() {
		return Collections.unmodifiableList(players);
	}
	
	public void removePlayer(int player){
		if(players.contains(new Integer(player))){
			players.remove(new Integer(player));
			permissions.setGroup(player, null);
		}
	}
	public void addPlayer(int player,String rank){
		if(!players.contains(new Integer(player))){
			players.add(new Integer(player));
			permissions.setGroup(player, permissions.getGroup(rank));
		}
	}
	
	public NBTTagCompound getCostumData() {
		return costumData;
	}
	
	public void setCostumData(NBTTagCompound comp){
		this.costumData = comp;
		try {
			MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+NBTCompressedStreamTools.toString(comp)+"' WHERE  `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='"+COSTUM_DATA_KEY+"'");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setActive(boolean active) {
		if(active == this.active)
			return;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+active+"' WHERE  `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='active'");
		this.active = active;
		if(!active)
			for(Integer member : players)
				removePlayer(member);
		if(active)
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.ACTIVE_GILD_SECTION));
		else
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.DEACTIVE_GILD_SECTION));
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildCostumDataAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildUpdateSectionStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction.Action;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePlayerEvent;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.nbt.NBTCompressedStreamTools;
import dev.wolveringer.nbt.NBTTagCompound;
import lombok.Getter;

public class GildSection {
	private static final String MEMBER_TABLE_NAME = "GILDE_MEMBERS";
	
	private static final String COSTUM_DATA_KEY = "costum_data";
	
	@Getter
	private Gilde handle;
	@Getter
	private GildeType type;
	@Getter
	protected boolean active;
	private GildSectionPermission permissions = new GildSectionPermission(this);
	private NBTTagCompound costumData;
	
	protected ArrayList<Integer> players = new ArrayList<>();
	
	public GildSection(Gilde handle, GildeType type,boolean active,HashMap<InformationKey, String> infos) {
		this.handle = handle;
		this.type = type;
		this.active = active;
		try {
			if(infos.get(new InformationKey(type, COSTUM_DATA_KEY)).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(infos.get(new InformationKey(type, COSTUM_DATA_KEY)));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		permissions.loadGroups();
		loadData();
	}

	public void loadData(){
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `key`,`value` FROM "+Gilde.TABLE_NAME+" WHERE `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		HashMap<String, String> values = new InitHashMap<String, String>() {
			@Override
			public String defaultValue(String key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(info[0], info[1]);
		
		try {
			if(values.get(COSTUM_DATA_KEY).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(values.get(COSTUM_DATA_KEY));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		
		ArrayList<String[]> members = MySQL.getInstance().querySync("SELECT `playerId`, `rank` FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		for(String[] member : members){
			players.add(Integer.parseInt(member[0]));
			permissions.players.put(Integer.parseInt(member[0]), member[1]);
		}
	}
	
	public GildSectionPermission getPermission(){
		return permissions;
	}
	
	public List<Integer> getPlayers() {
		return Collections.unmodifiableList(players);
	}
	
	public void removePlayer(int player){
		if(players.contains(new Integer(player))){
			players.remove(new Integer(player));
			permissions.setGroup(player, null);
		}
	}
	public void addPlayer(int player,String rank){
		if(!players.contains(new Integer(player))){
			players.add(new Integer(player));
			permissions.setGroup(player, permissions.getGroup(rank));
		}
	}
	
	public NBTTagCompound getCostumData() {
		return costumData;
	}
	
	public void setCostumData(NBTTagCompound comp){
		this.costumData = comp;
		try {
			MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+NBTCompressedStreamTools.toString(comp)+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='"+COSTUM_DATA_KEY+"'");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setActive(boolean active) {
		if(active == this.active)
			return;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+active+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='active'");
		this.active = active;
		if(!active)
			for(Integer member : players)
				removePlayer(member);
		if(active)
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.ACTIVE_GILD_SECTION));
		else
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.DEACTIVE_GILD_SECTION));
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildCostumDataAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildUpdateSectionStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction.Action;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePlayerEvent;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.nbt.NBTCompressedStreamTools;
import dev.wolveringer.nbt.NBTTagCompound;
import lombok.Getter;

public class GildSection {
	private static final String MEMBER_TABLE_NAME = "GILDE_MEMBERS";
	
	private static final String COSTUM_DATA_KEY = "costum_data";
	
	@Getter
	private Gilde handle;
	@Getter
	private GildeType type;
	@Getter
	protected boolean active;
	private GildSectionPermission permissions = new GildSectionPermission(this);
	private NBTTagCompound costumData;
	
	protected ArrayList<Integer> players = new ArrayList<>();
	
	public GildSection(Gilde handle, GildeType type,boolean active,HashMap<InformationKey, String> infos) {
		this.handle = handle;
		this.type = type;
		this.active = active;
		try {
			if(infos.get(new InformationKey(type, COSTUM_DATA_KEY)).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(infos.get(new InformationKey(type, COSTUM_DATA_KEY)));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		permissions.loadGroups();
		loadData();
	}

	public void loadData(){
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `key`,`value` FROM "+Gilde.TABLE_NAME+" WHERE `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		HashMap<String, String> values = new InitHashMap<String, String>() {
			@Override
			public String defaultValue(String key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(info[0], info[1]);
		
		try {
			if(values.get(COSTUM_DATA_KEY).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(values.get(COSTUM_DATA_KEY));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		
		ArrayList<String[]> members = MySQL.getInstance().querySync("SELECT `playerId`, `rank` FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		for(String[] member : members){
			players.add(Integer.parseInt(member[0]));
			permissions.players.put(Integer.parseInt(member[0]), member[1]);
		}
	}
	
	public GildSectionPermission getPermission(){
		return permissions;
	}
	
	public List<Integer> getPlayers() {
		return Collections.unmodifiableList(players);
	}
	
	public void removePlayer(int player){
		if(players.contains(new Integer(player))){
			players.remove(new Integer(player));
			permissions.setGroup(player, null);
		}
	}
	public void addPlayer(int player,String rank){
		if(!players.contains(new Integer(player))){
			players.add(new Integer(player));
			permissions.setGroup(player, permissions.getGroup(rank));
		}
	}
	
	public NBTTagCompound getCostumData() {
		return costumData;
	}
	
	public void setCostumData(NBTTagCompound comp){
		this.costumData = comp;
		try {
			MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+NBTCompressedStreamTools.toString(comp)+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='"+COSTUM_DATA_KEY+"'");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setActive(boolean active) {
		if(active == this.active)
			return;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+active+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='active'");
		this.active = active;
		if(!active)
			for(Integer member : players)
				removePlayer(member);
		if(active)
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.ACTIVE_GILD_SECTION));
		else
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.DEACTIVE_GILD_SECTION));
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildCostumDataAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildUpdateSectionStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction.Action;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePlayerEvent;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.nbt.NBTCompressedStreamTools;
import dev.wolveringer.nbt.NBTTagCompound;
import lombok.Getter;

public class GildSection {
	private static final String MEMBER_TABLE_NAME = "GILDE_MEMBERS";
	
	private static final String COSTUM_DATA_KEY = "costum_data";
	
	@Getter
	private Gilde handle;
	@Getter
	private GildeType type;
	@Getter
	protected boolean active;
	private GildSectionPermission permissions = new GildSectionPermission(this);
	private NBTTagCompound costumData;
	
	protected ArrayList<Integer> players = new ArrayList<>();
	
	public GildSection(Gilde handle, GildeType type,boolean active,HashMap<InformationKey, String> infos) {
		this.handle = handle;
		this.type = type;
		this.active = active;
		try {
			if(infos.get(new InformationKey(type, COSTUM_DATA_KEY)).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(infos.get(new InformationKey(type, COSTUM_DATA_KEY)));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		permissions.loadGroups();
		loadData();
	}

	public void loadData(){
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `key`,`value` FROM "+Gilde.TABLE_NAME+" WHERE `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		HashMap<String, String> values = new InitHashMap<String, String>() {
			@Override
			public String defaultValue(String key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(info[0], info[1]);
		
		try {
			if(values.get(COSTUM_DATA_KEY).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(values.get(COSTUM_DATA_KEY));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		
		ArrayList<String[]> members = MySQL.getInstance().querySync("SELECT `playerId`, `rank` FROM `GILDE_MEMBERS` WHERE `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		for(String[] member : members){
			players.add(Integer.parseInt(member[0]));
			permissions.players.put(Integer.parseInt(member[0]), member[1]);
		}
	}
	
	public GildSectionPermission getPermission(){
		return permissions;
	}
	
	public List<Integer> getPlayers() {
		return Collections.unmodifiableList(players);
	}
	
	public void removePlayer(int player){
		if(players.contains(new Integer(player))){
			players.remove(new Integer(player));
			permissions.setGroup(player, null);
		}
	}
	public void addPlayer(int player,String rank){
		if(!players.contains(new Integer(player))){
			players.add(new Integer(player));
			permissions.setGroup(player, permissions.getGroup(rank));
		}
	}
	
	public NBTTagCompound getCostumData() {
		return costumData;
	}
	
	public void setCostumData(NBTTagCompound comp){
		this.costumData = comp;
		try {
			MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+NBTCompressedStreamTools.toString(comp)+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='"+COSTUM_DATA_KEY+"'");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setActive(boolean active) {
		if(active == this.active)
			return;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+active+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='active'");
		this.active = active;
		if(!active)
			for(Integer member : players)
				removePlayer(member);
		if(active)
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.ACTIVE_GILD_SECTION));
		else
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.DEACTIVE_GILD_SECTION));
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildCostumDataAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildUpdateSectionStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction.Action;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePlayerEvent;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.nbt.NBTCompressedStreamTools;
import dev.wolveringer.nbt.NBTTagCompound;
import lombok.Getter;

public class GildSection {
	private static final String MEMBER_TABLE_NAME = "GILDE_MEMBERS";
	
	private static final String COSTUM_DATA_KEY = "costum_data";
	
	@Getter
	private Gilde handle;
	@Getter
	private GildeType type;
	@Getter
	protected boolean active;
	private GildSectionPermission permissions = new GildSectionPermission(this);
	private NBTTagCompound costumData;
	
	protected ArrayList<Integer> players = new ArrayList<>();
	
	public GildSection(Gilde handle, GildeType type,boolean active,HashMap<InformationKey, String> infos) {
		this.handle = handle;
		this.type = type;
		this.active = active;
		try {
			if(infos.get(new InformationKey(type, COSTUM_DATA_KEY)).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(infos.get(new InformationKey(type, COSTUM_DATA_KEY)));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		permissions.loadGroups();
		loadData();
	}

	public void loadData(){
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `key`,`value` FROM "+Gilde.TABLE_NAME+" WHERE `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		HashMap<String, String> values = new InitHashMap<String, String>() {
			@Override
			public String defaultValue(String key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(info[0], info[1]);
		
		try {
			if(values.get(COSTUM_DATA_KEY).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(values.get(COSTUM_DATA_KEY));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		
		ArrayList<String[]> members = MySQL.getInstance().querySync("SELECT `playerId`, `rank` FROM `GILDE_MEMBERS` WHERE `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		for(String[] member : members){
			players.add(Integer.parseInt(member[0]));
			permissions.players.put(Integer.parseInt(member[0]), member[1]);
		}
	}
	
	public GildSectionPermission getPermission(){
		return permissions;
	}
	
	public List<Integer> getPlayers() {
		return Collections.unmodifiableList(players);
	}
	
	public void removePlayer(int player){
		if(players.contains(new Integer(player))){
			players.remove(new Integer(player));
			permissions.setGroup(player, null);
		}
	}
	public void addPlayer(int player,String rank){
		if(!players.contains(new Integer(player))){
			players.add(new Integer(player));
			permissions.setGroup(player, permissions.getGroup(rank));
		}
	}
	
	public NBTTagCompound getCostumData() {
		return costumData;
	}
	
	public void setCostumData(NBTTagCompound comp){
		this.costumData = comp;
		try {
			MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+NBTCompressedStreamTools.toString(comp)+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='"+COSTUM_DATA_KEY+"'");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setActive(boolean active) {
		if(active == this.active)
			return;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+active+"' WHERE  `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='active'");
		this.active = active;
		if(!active)
			for(Integer member : players)
				removePlayer(member);
		if(active)
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.ACTIVE_GILD_SECTION));
		else
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.DEACTIVE_GILD_SECTION));
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildCostumDataAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildUpdateSectionStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction.Action;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePlayerEvent;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.nbt.NBTCompressedStreamTools;
import dev.wolveringer.nbt.NBTTagCompound;
import lombok.Getter;

public class GildSection {
	private static final String MEMBER_TABLE_NAME = "GILDE_MEMBERS";
	
	private static final String COSTUM_DATA_KEY = "costum_data";
	
	@Getter
	private Gilde handle;
	@Getter
	private GildeType type;
	@Getter
	protected boolean active;
	private GildSectionPermission permissions = new GildSectionPermission(this);
	private NBTTagCompound costumData;
	
	protected ArrayList<Integer> players = new ArrayList<>();
	
	public GildSection(Gilde handle, GildeType type,boolean active,HashMap<InformationKey, String> infos) {
		this.handle = handle;
		this.type = type;
		this.active = active;
		try {
			if(infos.get(new InformationKey(type, COSTUM_DATA_KEY)).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(infos.get(COSTUM_DATA_KEY));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		permissions.loadGroups();
		loadData();
	}

	public void loadData(){
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `key`,`value` FROM "+Gilde.TABLE_NAME+" WHERE `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		HashMap<String, String> values = new InitHashMap<String, String>() {
			@Override
			public String defaultValue(String key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(info[0], info[1]);
		
		try {
			if(values.get(COSTUM_DATA_KEY).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(values.get(COSTUM_DATA_KEY));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		
		ArrayList<String[]> members = MySQL.getInstance().querySync("SELECT `playerId`, `rank` FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		for(String[] member : members){
			players.add(Integer.parseInt(member[0]));
			permissions.players.put(Integer.parseInt(member[0]), member[1]);
		}
	}
	
	public GildSectionPermission getPermission(){
		return permissions;
	}
	
	public List<Integer> getPlayers() {
		return Collections.unmodifiableList(players);
	}
	
	public void removePlayer(int player){
		if(players.contains(new Integer(player))){
			players.remove(new Integer(player));
			permissions.setGroup(player, null);
		}
	}
	public void addPlayer(int player,String rank){
		if(!players.contains(new Integer(player))){
			players.add(new Integer(player));
			permissions.setGroup(player, permissions.getGroup(rank));
		}
	}
	
	public NBTTagCompound getCostumData() {
		return costumData;
	}
	
	public void setCostumData(NBTTagCompound comp){
		this.costumData = comp;
		try {
			MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+NBTCompressedStreamTools.toString(comp)+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='"+COSTUM_DATA_KEY+"'");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setActive(boolean active) {
		if(active == this.active)
			return;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+active+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='active'");
		this.active = active;
		if(!active)
			for(Integer member : players)
				removePlayer(member);
		if(active)
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.ACTIVE_GILD_SECTION));
		else
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.DEACTIVE_GILD_SECTION));
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildCostumDataAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildUpdateSectionStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction.Action;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePlayerEvent;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.nbt.NBTCompressedStreamTools;
import dev.wolveringer.nbt.NBTTagCompound;
import lombok.Getter;

public class GildSection {
	private static final String MEMBER_TABLE_NAME = "GILDE_MEMBERS";
	
	private static final String COSTUM_DATA_KEY = "costum_data";
	
	@Getter
	private Gilde handle;
	@Getter
	private GildeType type;
	@Getter
	protected boolean active;
	private GildSectionPermission permissions = new GildSectionPermission(this);
	private NBTTagCompound costumData;
	
	protected ArrayList<Integer> players = new ArrayList<>();
	
	public GildSection(Gilde handle, GildeType type,boolean active,HashMap<InformationKey, String> infos) {
		this.handle = handle;
		this.type = type;
		this.active = active;
		try {
			if(infos.get(new InformationKey(type, COSTUM_DATA_KEY)).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(infos.get(new InformationKey(type, COSTUM_DATA_KEY)));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		permissions.loadGroups();
		loadData();
	}

	public void loadData(){
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `key`,`value` FROM "+Gilde.TABLE_NAME+" WHERE `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		HashMap<String, String> values = new InitHashMap<String, String>() {
			@Override
			public String defaultValue(String key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(info[0], info[1]);
		
		try {
			if(values.get(new InformationKey(type, COSTUM_DATA_KEY)).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(values.get(COSTUM_DATA_KEY));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		
		ArrayList<String[]> members = MySQL.getInstance().querySync("SELECT `playerId`, `rank` FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		for(String[] member : members){
			players.add(Integer.parseInt(member[0]));
			permissions.players.put(Integer.parseInt(member[0]), member[1]);
		}
	}
	
	public GildSectionPermission getPermission(){
		return permissions;
	}
	
	public List<Integer> getPlayers() {
		return Collections.unmodifiableList(players);
	}
	
	public void removePlayer(int player){
		if(players.contains(new Integer(player))){
			players.remove(new Integer(player));
			permissions.setGroup(player, null);
		}
	}
	public void addPlayer(int player,String rank){
		if(!players.contains(new Integer(player))){
			players.add(new Integer(player));
			permissions.setGroup(player, permissions.getGroup(rank));
		}
	}
	
	public NBTTagCompound getCostumData() {
		return costumData;
	}
	
	public void setCostumData(NBTTagCompound comp){
		this.costumData = comp;
		try {
			MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+NBTCompressedStreamTools.toString(comp)+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='"+COSTUM_DATA_KEY+"'");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setActive(boolean active) {
		if(active == this.active)
			return;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+active+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='active'");
		this.active = active;
		if(!active)
			for(Integer member : players)
				removePlayer(member);
		if(active)
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.ACTIVE_GILD_SECTION));
		else
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.DEACTIVE_GILD_SECTION));
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildCostumDataAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildUpdateSectionStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction.Action;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePlayerEvent;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.nbt.NBTCompressedStreamTools;
import dev.wolveringer.nbt.NBTTagCompound;
import lombok.Getter;

public class GildSection {
	private static final String MEMBER_TABLE_NAME = "GILDE_MEMBERS";
	
	private static final String COSTUM_DATA_KEY = "costum_data";
	
	@Getter
	private Gilde handle;
	@Getter
	private GildeType type;
	@Getter
	protected boolean active;
	private GildSectionPermission permissions = new GildSectionPermission(this);
	private NBTTagCompound costumData;
	
	protected ArrayList<Integer> players = new ArrayList<>();
	
	public GildSection(Gilde handle, GildeType type,boolean active,HashMap<InformationKey, String> infos) {
		this.handle = handle;
		this.type = type;
		this.active = active;
		try {
			if(infos.get(new InformationKey(type, COSTUM_DATA_KEY)).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(infos.get(new InformationKey(type, COSTUM_DATA_KEY)));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		permissions.loadGroups();
		loadData();
	}

	public void loadData(){
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `key`,`value` FROM "+Gilde.TABLE_NAME+" WHERE `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		HashMap<String, String> values = new InitHashMap<String, String>() {
			@Override
			public String defaultValue(String key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(info[0], info[1]);
		
		try {
			if(values.get(COSTUM_DATA_KEY).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(values.get(COSTUM_DATA_KEY));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		
		ArrayList<String[]> members = MySQL.getInstance().querySync("SELECT `playerId`, `rank` FROM `GILDE_MEMBERS` WHERE `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		for(String[] member : members){
			players.add(Integer.parseInt(member[0]));
			permissions.players.put(Integer.parseInt(member[0]), member[1]);
		}
	}
	
	public GildSectionPermission getPermission(){
		return permissions;
	}
	
	public List<Integer> getPlayers() {
		return Collections.unmodifiableList(players);
	}
	
	public void removePlayer(int player){
		if(players.contains(new Integer(player))){
			players.remove(new Integer(player));
			permissions.setGroup(player, null);
		}
	}
	public void addPlayer(int player,String rank){
		if(!players.contains(new Integer(player))){
			players.add(new Integer(player));
			permissions.setGroup(player, permissions.getGroup(rank));
		}
	}
	
	public NBTTagCompound getCostumData() {
		return costumData;
	}
	
	public void setCostumData(NBTTagCompound comp){
		this.costumData = comp;
		try {
			MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+NBTCompressedStreamTools.toString(comp)+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='"+COSTUM_DATA_KEY+"'");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setActive(boolean active) {
		if(active == this.active)
			return;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+active+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='active'");
		this.active = active;
		if(!active)
			for(Integer member : players)
				removePlayer(member);
		if(active)
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.ACTIVE_GILD_SECTION));
		else
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.DEACTIVE_GILD_SECTION));
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildCostumDataAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildUpdateSectionStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction.Action;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePlayerEvent;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.nbt.NBTCompressedStreamTools;
import dev.wolveringer.nbt.NBTTagCompound;
import lombok.Getter;

public class GildSection {
	private static final String MEMBER_TABLE_NAME = "GILDE_MEMBERS";
	
	private static final String COSTUM_DATA_KEY = "costum_data";
	
	@Getter
	private Gilde handle;
	@Getter
	private GildeType type;
	@Getter
	protected boolean active;
	private GildSectionPermission permissions = new GildSectionPermission(this);
	private NBTTagCompound costumData;
	
	protected ArrayList<Integer> players = new ArrayList<>();
	
	public GildSection(Gilde handle, GildeType type,boolean active,HashMap<InformationKey, String> infos) {
		this.handle = handle;
		this.type = type;
		this.active = active;
		try {
			if(infos.get(new InformationKey(type, COSTUM_DATA_KEY)).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(infos.get(new InformationKey(type, COSTUM_DATA_KEY)));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		permissions.loadGroups();
		loadData();
	}

	public void loadData(){
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `key`,`value` FROM "+Gilde.TABLE_NAME+" WHERE `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		HashMap<String, String> values = new InitHashMap<String, String>() {
			@Override
			public String defaultValue(String key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(info[0], info[1]);
		
		try {
			if(values.get(COSTUM_DATA_KEY).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(values.get(COSTUM_DATA_KEY));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		
		ArrayList<String[]> members = MySQL.getInstance().querySync("SELECT `playerId`, `rank` FROM `GILDE_MEMBERS` WHERE `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		for(String[] member : members){
			players.add(Integer.parseInt(member[0]));
			permissions.players.put(Integer.parseInt(member[0]), member[1]);
		}
	}
	
	public GildSectionPermission getPermission(){
		return permissions;
	}
	
	public List<Integer> getPlayers() {
		return Collections.unmodifiableList(players);
	}
	
	public void removePlayer(int player){
		if(players.contains(new Integer(player))){
			players.remove(new Integer(player));
			permissions.setGroup(player, null);
		}
	}
	public void addPlayer(int player,String rank){
		if(!players.contains(new Integer(player))){
			players.add(new Integer(player));
			permissions.setGroup(player, permissions.getGroup(rank));
		}
	}
	
	public NBTTagCompound getCostumData() {
		return costumData;
	}
	
	public void setCostumData(NBTTagCompound comp){
		this.costumData = comp;
		try {
			MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+NBTCompressedStreamTools.toString(comp)+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='"+COSTUM_DATA_KEY+"'");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setActive(boolean active) {
		if(active == this.active)
			return;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+active+"' WHERE  `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='active'");
		this.active = active;
		if(!active)
			for(Integer member : players)
				removePlayer(member);
		if(active)
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.ACTIVE_GILD_SECTION));
		else
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.DEACTIVE_GILD_SECTION));
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildCostumDataAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildUpdateSectionStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction.Action;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePlayerEvent;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.nbt.NBTCompressedStreamTools;
import dev.wolveringer.nbt.NBTTagCompound;
import lombok.Getter;

public class GildSection {
	private static final String MEMBER_TABLE_NAME = "GILDE_MEMBERS";
	
	private static final String COSTUM_DATA_KEY = "costum_data";
	
	@Getter
	private Gilde handle;
	@Getter
	private GildeType type;
	@Getter
	protected boolean active;
	private GildSectionPermission permissions = new GildSectionPermission(this);
	private NBTTagCompound costumData;
	
	protected ArrayList<Integer> players = new ArrayList<>();
	
	public GildSection(Gilde handle, GildeType type,boolean active,HashMap<InformationKey, String> infos) {
		this.handle = handle;
		this.type = type;
		this.active = active;
		try {
			if(infos.get(COSTUM_DATA_KEY).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(infos.get(COSTUM_DATA_KEY));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		permissions.loadGroups();
		loadData();
	}

	public void loadData(){
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `key`,`value` FROM "+Gilde.TABLE_NAME+" WHERE `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		HashMap<String, String> values = new InitHashMap<String, String>() {
			@Override
			public String defaultValue(String key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(info[0], info[1]);
		
		try {
			if(values.get(COSTUM_DATA_KEY).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(values.get(COSTUM_DATA_KEY));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		
		ArrayList<String[]> members = MySQL.getInstance().querySync("SELECT `playerId`, `rank` FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		for(String[] member : members){
			players.add(Integer.parseInt(member[0]));
			permissions.players.put(Integer.parseInt(member[0]), member[1]);
		}
	}
	
	public GildSectionPermission getPermission(){
		return permissions;
	}
	
	public List<Integer> getPlayers() {
		return Collections.unmodifiableList(players);
	}
	
	public void removePlayer(int player){
		if(players.contains(new Integer(player))){
			players.remove(new Integer(player));
			permissions.setGroup(player, null);
		}
	}
	public void addPlayer(int player,String rank){
		if(!players.contains(new Integer(player))){
			players.add(new Integer(player));
			permissions.setGroup(player, permissions.getGroup(rank));
		}
	}
	
	public NBTTagCompound getCostumData() {
		return costumData;
	}
	
	public void setCostumData(NBTTagCompound comp){
		this.costumData = comp;
		try {
			MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+NBTCompressedStreamTools.toString(comp)+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='"+COSTUM_DATA_KEY+"'");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setActive(boolean active) {
		if(active == this.active)
			return;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+active+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='active'");
		this.active = active;
		if(!active)
			for(Integer member : players)
				removePlayer(member);
		if(active)
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.ACTIVE_GILD_SECTION));
		else
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.DEACTIVE_GILD_SECTION));
	}
}
                         
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildCostumDataAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildUpdateSectionStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction.Action;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePlayerEvent;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.nbt.NBTCompressedStreamTools;
import dev.wolveringer.nbt.NBTTagCompound;
import lombok.Getter;

public class GildSection {
	private static final String MEMBER_TABLE_NAME = "GILDE_MEMBERS";
	
	private static final String COSTUM_DATA_KEY = "costum_data";
	
	@Getter
	private Gilde handle;
	@Getter
	private GildeType type;
	@Getter
	protected boolean active;
	private GildSectionPermission permissions = new GildSectionPermission(this);
	private NBTTagCompound costumData;
	
	protected ArrayList<Integer> players = new ArrayList<>();
	
	public GildSection(Gilde handle, GildeType type,boolean active,HashMap<InformationKey, String> infos) {
		this.handle = handle;
		this.type = type;
		this.active = active;
		try {
			this.costumData = NBTCompressedStreamTools.read(infos.get(new InformationKey(type, COSTUM_DATA_KEY)));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		permissions.loadGroups();
		loadData();
	}

	public void loadData(){
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `key`,`value` FROM "+Gilde.TABLE_NAME+" WHERE `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		HashMap<String, String> values = new InitHashMap<String, String>() {
			@Override
			public String defaultValue(String key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(info[0], info[1]);
		
		try {
			if(values.get(COSTUM_DATA_KEY).equalsIgnoreCase(""))
				this.costumData = new NBTTagCompound();
			else
				this.costumData = NBTCompressedStreamTools.read(values.get(COSTUM_DATA_KEY));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		
		ArrayList<String[]> members = MySQL.getInstance().querySync("SELECT `playerId`, `rank` FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		for(String[] member : members){
			players.add(Integer.parseInt(member[0]));
			permissions.players.put(Integer.parseInt(member[0]), member[1]);
		}
	}
	
	public GildSectionPermission getPermission(){
		return permissions;
	}
	
	public List<Integer> getPlayers() {
		return Collections.unmodifiableList(players);
	}
	
	public void removePlayer(int player){
		if(players.contains(new Integer(player))){
			players.remove(new Integer(player));
			permissions.setGroup(player, null);
		}
	}
	public void addPlayer(int player,String rank){
		if(!players.contains(new Integer(player))){
			players.add(new Integer(player));
			permissions.setGroup(player, permissions.getGroup(rank));
		}
	}
	
	public NBTTagCompound getCostumData() {
		return costumData;
	}
	
	public void setCostumData(NBTTagCompound comp){
		this.costumData = comp;
		try {
			MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+NBTCompressedStreamTools.toString(comp)+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='"+COSTUM_DATA_KEY+"'");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setActive(boolean active) {
		if(active == this.active)
			return;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+active+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='active'");
		this.active = active;
		if(!active)
			for(Integer member : players)
				removePlayer(member);
		if(active)
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.ACTIVE_GILD_SECTION));
		else
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.DEACTIVE_GILD_SECTION));
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildCostumDataAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildUpdateSectionStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction.Action;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePlayerEvent;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.nbt.NBTCompressedStreamTools;
import dev.wolveringer.nbt.NBTTagCompound;
import lombok.Getter;

public class GildSection {
	private static final String MEMBER_TABLE_NAME = "GILDE_MEMBERS";
	
	private static final String COSTUM_DATA_KEY = "costum_data";
	
	@Getter
	private Gilde handle;
	@Getter
	private GildeType type;
	@Getter
	protected boolean active;
	private GildSectionPermission permissions = new GildSectionPermission(this);
	private NBTTagCompound costumData;
	
	protected ArrayList<Integer> players = new ArrayList<>();
	
	public GildSection(Gilde handle, GildeType type,boolean active,HashMap<InformationKey, String> infos) {
		this.handle = handle;
		this.type = type;
		this.active = active;
		try {
			this.costumData = NBTCompressedStreamTools.read(infos.get(new InformationKey(type, COSTUM_DATA_KEY)));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		permissions.loadGroups();
		loadData();
	}

	public void loadData(){
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `key`,`value` FROM "+Gilde.TABLE_NAME+" WHERE `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		HashMap<String, String> values = new InitHashMap<String, String>() {
			@Override
			public String defaultValue(String key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(info[0], info[1]);
		
		try {
			if(values.get(COSTUM_DATA_KEY).equalsIgnoreCase("")){
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+uuid+"','"+t.toString()+"','active','false')");
				this.costumData = new NBTTagCompound();
			}else
				this.costumData = NBTCompressedStreamTools.read(values.get(COSTUM_DATA_KEY));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		
		ArrayList<String[]> members = MySQL.getInstance().querySync("SELECT `playerId`, `rank` FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		for(String[] member : members){
			players.add(Integer.parseInt(member[0]));
			permissions.players.put(Integer.parseInt(member[0]), member[1]);
		}
	}
	
	public GildSectionPermission getPermission(){
		return permissions;
	}
	
	public List<Integer> getPlayers() {
		return Collections.unmodifiableList(players);
	}
	
	public void removePlayer(int player){
		if(players.contains(new Integer(player))){
			players.remove(new Integer(player));
			permissions.setGroup(player, null);
		}
	}
	public void addPlayer(int player,String rank){
		if(!players.contains(new Integer(player))){
			players.add(new Integer(player));
			permissions.setGroup(player, permissions.getGroup(rank));
		}
	}
	
	public NBTTagCompound getCostumData() {
		return costumData;
	}
	
	public void setCostumData(NBTTagCompound comp){
		this.costumData = comp;
		try {
			MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+NBTCompressedStreamTools.toString(comp)+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='"+COSTUM_DATA_KEY+"'");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setActive(boolean active) {
		if(active == this.active)
			return;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+active+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='active'");
		this.active = active;
		if(!active)
			for(Integer member : players)
				removePlayer(member);
		if(active)
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.ACTIVE_GILD_SECTION));
		else
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.DEACTIVE_GILD_SECTION));
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                          
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildCostumDataAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildUpdateSectionStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction.Action;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePlayerEvent;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.nbt.NBTCompressedStreamTools;
import dev.wolveringer.nbt.NBTTagCompound;
import lombok.Getter;

public class GildSection {
	private static final String MEMBER_TABLE_NAME = "GILDE_MEMBERS";
	
	private static final String COSTUM_DATA_KEY = "costum_data";
	
	@Getter
	private Gilde handle;
	@Getter
	private GildeType type;
	@Getter
	protected boolean active;
	private GildSectionPermission permissions = new GildSectionPermission(this);
	private NBTTagCompound costumData;
	
	protected ArrayList<Integer> players = new ArrayList<>();
	
	public GildSection(Gilde handle, GildeType type,boolean active,HashMap<InformationKey, String> infos) {
		this.handle = handle;
		this.type = type;
		this.active = active;
		try {
			if(infos.get(COSTUM_DATA_KEY).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(values.get(COSTUM_DATA_KEY));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		permissions.loadGroups();
		loadData();
	}

	public void loadData(){
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `key`,`value` FROM "+Gilde.TABLE_NAME+" WHERE `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		HashMap<String, String> values = new InitHashMap<String, String>() {
			@Override
			public String defaultValue(String key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(info[0], info[1]);
		
		try {
			if(values.get(COSTUM_DATA_KEY).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(values.get(COSTUM_DATA_KEY));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		
		ArrayList<String[]> members = MySQL.getInstance().querySync("SELECT `playerId`, `rank` FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		for(String[] member : members){
			players.add(Integer.parseInt(member[0]));
			permissions.players.put(Integer.parseInt(member[0]), member[1]);
		}
	}
	
	public GildSectionPermission getPermission(){
		return permissions;
	}
	
	public List<Integer> getPlayers() {
		return Collections.unmodifiableList(players);
	}
	
	public void removePlayer(int player){
		if(players.contains(new Integer(player))){
			players.remove(new Integer(player));
			permissions.setGroup(player, null);
		}
	}
	public void addPlayer(int player,String rank){
		if(!players.contains(new Integer(player))){
			players.add(new Integer(player));
			permissions.setGroup(player, permissions.getGroup(rank));
		}
	}
	
	public NBTTagCompound getCostumData() {
		return costumData;
	}
	
	public void setCostumData(NBTTagCompound comp){
		this.costumData = comp;
		try {
			MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+NBTCompressedStreamTools.toString(comp)+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='"+COSTUM_DATA_KEY+"'");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setActive(boolean active) {
		if(active == this.active)
			return;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+active+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='active'");
		this.active = active;
		if(!active)
			for(Integer member : players)
				removePlayer(member);
		if(active)
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.ACTIVE_GILD_SECTION));
		else
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.DEACTIVE_GILD_SECTION));
	}
}
                        
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildCostumDataAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildUpdateSectionStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction.Action;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePlayerEvent;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.nbt.NBTCompressedStreamTools;
import dev.wolveringer.nbt.NBTTagCompound;
import lombok.Getter;

public class GildSection {
	private static final String MEMBER_TABLE_NAME = "GILDE_MEMBERS";
	
	private static final String COSTUM_DATA_KEY = "costum_data";
	
	@Getter
	private Gilde handle;
	@Getter
	private GildeType type;
	@Getter
	protected boolean active;
	private GildSectionPermission permissions = new GildSectionPermission(this);
	private NBTTagCompound costumData;
	
	protected ArrayList<Integer> players = new ArrayList<>();
	
	public GildSection(Gilde handle, GildeType type,boolean active,HashMap<InformationKey, String> infos) {
		this.handle = handle;
		this.type = type;
		this.active = active;
		try {
			this.costumData = NBTCompressedStreamTools.read(infos.get(new InformationKey(type, COSTUM_DATA_KEY)));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		permissions.loadGroups();
		loadData();
	}

	public void loadData(){
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `key`,`value` FROM "+Gilde.TABLE_NAME+" WHERE `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		HashMap<String, String> values = new InitHashMap<String, String>() {
			@Override
			public String defaultValue(String key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(info[0], info[1]);
		
		try {
			this.costumData = NBTCompressedStreamTools.read(values.get(COSTUM_DATA_KEY));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		
		ArrayList<String[]> members = MySQL.getInstance().querySync("SELECT `playerId`, `rank` FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		for(String[] member : members){
			players.add(Integer.parseInt(member[0]));
			permissions.players.put(Integer.parseInt(member[0]), member[1]);
		}
	}
	
	public GildSectionPermission getPermission(){
		return permissions;
	}
	
	public List<Integer> getPlayers() {
		return Collections.unmodifiableList(players);
	}
	
	public void removePlayer(int player){
		if(players.contains(new Integer(player))){
			players.remove(new Integer(player));
			permissions.setGroup(player, null);
		}
	}
	public void addPlayer(int player,String rank){
		if(!players.contains(new Integer(player))){
			players.add(new Integer(player));
			permissions.setGroup(player, permissions.getGroup(rank));
		}
	}
	
	public NBTTagCompound getCostumData() {
		return costumData;
	}
	
	public void setCostumData(NBTTagCompound comp){
		this.costumData = comp;
		try {
			MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+NBTCompressedStreamTools.toString(comp)+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='"+COSTUM_DATA_KEY+"'");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setActive(boolean active) {
		if(active == this.active)
			return;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+active+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='active'");
		this.active = active;
		if(!active)
			for(Integer member : players)
				removePlayer(member);
		if(active)
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.ACTIVE_GILD_SECTION));
		else
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.DEACTIVE_GILD_SECTION));
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildCostumDataAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildUpdateSectionStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction.Action;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePlayerEvent;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.nbt.NBTCompressedStreamTools;
import dev.wolveringer.nbt.NBTTagCompound;
import lombok.Getter;

public class GildSection {
	private static final String MEMBER_TABLE_NAME = "GILDE_MEMBERS";
	
	private static final String COSTUM_DATA_KEY = "costum_data";
	
	@Getter
	private Gilde handle;
	@Getter
	private GildeType type;
	@Getter
	protected boolean active;
	private GildSectionPermission permissions = new GildSectionPermission(this);
	private NBTTagCompound costumData;
	
	protected ArrayList<Integer> players = new ArrayList<>();
	
	public GildSection(Gilde handle, GildeType type,boolean active,HashMap<InformationKey, String> infos) {
		this.handle = handle;
		this.type = type;
		this.active = active;
		try {
			this.costumData = NBTCompressedStreamTools.read(infos.get(new InformationKey(type, COSTUM_DATA_KEY)));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		permissions.loadGroups();
		loadData();
	}

	public void loadData(){
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `key`,`value` FROM "+Gilde.TABLE_NAME+" WHERE `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		HashMap<String, String> values = new InitHashMap<String, String>() {
			@Override
			public String defaultValue(String key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(info[0], info[1]);
		
		try {
			if(values.get(COSTUM_DATA_KEY).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(values.get(COSTUM_DATA_KEY));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		
		ArrayList<String[]> members = MySQL.getInstance().querySync("SELECT `playerId`, `rank` FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		for(String[] member : members){
			players.add(Integer.parseInt(member[0]));
			permissions.players.put(Integer.parseInt(member[0]), member[1]);
		}
	}
	
	public GildSectionPermission getPermission(){
		return permissions;
	}
	
	public List<Integer> getPlayers() {
		return Collections.unmodifiableList(players);
	}
	
	public void removePlayer(int player){
		if(players.contains(new Integer(player))){
			players.remove(new Integer(player));
			permissions.setGroup(player, null);
		}
	}
	public void addPlayer(int player,String rank){
		if(!players.contains(new Integer(player))){
			players.add(new Integer(player));
			permissions.setGroup(player, permissions.getGroup(rank));
		}
	}
	
	public NBTTagCompound getCostumData() {
		return costumData;
	}
	
	public void setCostumData(NBTTagCompound comp){
		this.costumData = comp;
		try {
			MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+NBTCompressedStreamTools.toString(comp)+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='"+COSTUM_DATA_KEY+"'");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setActive(boolean active) {
		if(active == this.active)
			return;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+active+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='active'");
		this.active = active;
		if(!active)
			for(Integer member : players)
				removePlayer(member);
		if(active)
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.ACTIVE_GILD_SECTION));
		else
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.DEACTIVE_GILD_SECTION));
	}
}
                                                                                                                                                                                                                                                                                                                                                             
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildCostumDataAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildUpdateSectionStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction.Action;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePlayerEvent;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.nbt.NBTCompressedStreamTools;
import dev.wolveringer.nbt.NBTTagCompound;
import lombok.Getter;

public class GildSection {
	private static final String MEMBER_TABLE_NAME = "GILDE_MEMBERS";
	
	private static final String COSTUM_DATA_KEY = "costum_data";
	
	@Getter
	private Gilde handle;
	@Getter
	private GildeType type;
	@Getter
	protected boolean active;
	private GildSectionPermission permissions = new GildSectionPermission(this);
	private NBTTagCompound costumData;
	
	protected ArrayList<Integer> players = new ArrayList<>();
	
	public GildSection(Gilde handle, GildeType type,boolean active,HashMap<InformationKey, String> infos) {
		this.handle = handle;
		this.type = type;
		this.active = active;
		try {
			if(infos.get(COSTUM_DATA_KEY).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(infos.get(COSTUM_DATA_KEY));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		permissions.loadGroups();
		loadData();
	}

	public void loadData(){
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `key`,`value` FROM "+Gilde.TABLE_NAME+" WHERE `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		HashMap<String, String> values = new InitHashMap<String, String>() {
			@Override
			public String defaultValue(String key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(info[0], info[1]);
		
		try {
			if(values.get(COSTUM_DATA_KEY).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(values.get(COSTUM_DATA_KEY));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		
		ArrayList<String[]> members = MySQL.getInstance().querySync("SELECT `playerId`, `rank` FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		for(String[] member : members){
			players.add(Integer.parseInt(member[0]));
			permissions.players.put(Integer.parseInt(member[0]), member[1]);
		}
	}
	
	public GildSectionPermission getPermission(){
		return permissions;
	}
	
	public List<Integer> getPlayers() {
		return Collections.unmodifiableList(players);
	}
	
	public void removePlayer(int player){
		if(players.contains(new Integer(player))){
			players.remove(new Integer(player));
			permissions.setGroup(player, null);
		}
	}
	public void addPlayer(int player,String rank){
		if(!players.contains(new Integer(player))){
			players.add(new Integer(player));
			permissions.setGroup(player, permissions.getGroup(rank));
		}
	}
	
	public NBTTagCompound getCostumData() {
		return costumData;
	}
	
	public void setCostumData(NBTTagCompound comp){
		this.costumData = comp;
		try {
			MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+NBTCompressedStreamTools.toString(comp)+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='"+COSTUM_DATA_KEY+"'");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setActive(boolean active) {
		if(active == this.active)
			return;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+active+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='active'");
		this.active = active;
		if(!active)
			for(Integer member : players)
				removePlayer(member);
		if(active)
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.ACTIVE_GILD_SECTION));
		else
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.DEACTIVE_GILD_SECTION));
	}
}
                         
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildCostumDataAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildUpdateSectionStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction.Action;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePlayerEvent;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.nbt.NBTCompressedStreamTools;
import dev.wolveringer.nbt.NBTTagCompound;
import lombok.Getter;

public class GildSection {
	private static final String MEMBER_TABLE_NAME = "GILDE_MEMBERS";
	
	private static final String COSTUM_DATA_KEY = "costum_data";
	
	@Getter
	private Gilde handle;
	@Getter
	private GildeType type;
	@Getter
	protected boolean active;
	private GildSectionPermission permissions = new GildSectionPermission(this);
	private NBTTagCompound costumData;
	
	protected ArrayList<Integer> players = new ArrayList<>();
	
	public GildSection(Gilde handle, GildeType type,boolean active,HashMap<InformationKey, String> infos) {
		this.handle = handle;
		this.type = type;
		this.active = active;
		try {
			this.costumData = NBTCompressedStreamTools.read(infos.get(new InformationKey(type, COSTUM_DATA_KEY)));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		permissions.loadGroups();
		loadData();
	}

	public void loadData(){
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `key`,`value` FROM "+Gilde.TABLE_NAME+" WHERE `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		HashMap<String, String> values = new InitHashMap<String, String>() {
			@Override
			public String defaultValue(String key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(info[0], info[1]);
		
		try {
			this.costumData = NBTCompressedStreamTools.read(values.get(COSTUM_DATA_KEY));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		
		ArrayList<String[]> members = MySQL.getInstance().querySync("SELECT `playerId`, `rank` FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		for(String[] member : members){
			players.add(Integer.parseInt(member[0]));
			permissions.players.put(Integer.parseInt(member[0]), member[1]);
		}
	}
	
	public GildSectionPermission getPermission(){
		return permissions;
	}
	
	public List<Integer> getPlayers() {
		return Collections.unmodifiableList(players);
	}
	
	public void removePlayer(int player){
		if(players.contains(new Integer(player))){
			players.remove(new Integer(player));
			permissions.setGroup(player, null);
		}
	}
	public void addPlayer(int player,String rank){
		if(!players.contains(new Integer(player))){
			players.add(new Integer(player));
			permissions.setGroup(player, permissions.getGroup(rank));
		}
	}
	
	public NBTTagCompound getCostumData() {
		return costumData;
	}
	
	public void setCostumData(NBTTagCompound comp){
		this.costumData = comp;
		try {
			MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+NBTCompressedStreamTools.toString(comp)+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='"+COSTUM_DATA_KEY+"'");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setActive(boolean active) {
		if(active == this.active)
			return;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+active+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='active'");
		this.active = active;
		if(!active)
			for(Integer member : players)
				removePlayer(member);
		if(active)
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.ACTIVE_GILD_SECTION));
		else
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.DEACTIVE_GILD_SECTION));
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildCostumDataAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildUpdateSectionStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction.Action;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePlayerEvent;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.nbt.NBTCompressedStreamTools;
import dev.wolveringer.nbt.NBTTagCompound;
import lombok.Getter;

public class GildSection {
	private static final String MEMBER_TABLE_NAME = "GILDE_MEMBERS";
	
	private static final String COSTUM_DATA_KEY = "costum_data";
	
	@Getter
	private Gilde handle;
	@Getter
	private GildeType type;
	@Getter
	protected boolean active;
	private GildSectionPermission permissions = new GildSectionPermission(this);
	private NBTTagCompound costumData;
	
	protected ArrayList<Integer> players = new ArrayList<>();
	
	public GildSection(Gilde handle, GildeType type,boolean active,HashMap<InformationKey, String> infos) {
		this.handle = handle;
		this.type = type;
		this.active = active;
		try {
			this.costumData = NBTCompressedStreamTools.read(infos.get(new InformationKey(type, COSTUM_DATA_KEY)));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		permissions.loadGroups();
		loadData();
	}

	public void loadData(){
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `key`,`value` FROM "+Gilde.TABLE_NAME+" WHERE `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		HashMap<String, String> values = new InitHashMap<String, String>() {
			@Override
			public String defaultValue(String key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(info[0], info[1]);
		
		try {
			if(values.get(COSTUM_DATA_KEY).equalsIgnoreCase("")){
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+uuid+"','"+t.toString()+"','active','false')");
				this.costumData = new NBTTagCompound();
			}else
				this.costumData = NBTCompressedStreamTools.read(values.get(COSTUM_DATA_KEY));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		
		ArrayList<String[]> members = MySQL.getInstance().querySync("SELECT `playerId`, `rank` FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		for(String[] member : members){
			players.add(Integer.parseInt(member[0]));
			permissions.players.put(Integer.parseInt(member[0]), member[1]);
		}
	}
	
	public GildSectionPermission getPermission(){
		return permissions;
	}
	
	public List<Integer> getPlayers() {
		return Collections.unmodifiableList(players);
	}
	
	public void removePlayer(int player){
		if(players.contains(new Integer(player))){
			players.remove(new Integer(player));
			permissions.setGroup(player, null);
		}
	}
	public void addPlayer(int player,String rank){
		if(!players.contains(new Integer(player))){
			players.add(new Integer(player));
			permissions.setGroup(player, permissions.getGroup(rank));
		}
	}
	
	public NBTTagCompound getCostumData() {
		return costumData;
	}
	
	public void setCostumData(NBTTagCompound comp){
		this.costumData = comp;
		try {
			MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+NBTCompressedStreamTools.toString(comp)+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='"+COSTUM_DATA_KEY+"'");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setActive(boolean active) {
		if(active == this.active)
			return;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+active+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='active'");
		this.active = active;
		if(!active)
			for(Integer member : players)
				removePlayer(member);
		if(active)
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.ACTIVE_GILD_SECTION));
		else
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.DEACTIVE_GILD_SECTION));
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                          
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildCostumDataAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildUpdateSectionStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction.Action;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePlayerEvent;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.nbt.NBTCompressedStreamTools;
import dev.wolveringer.nbt.NBTTagCompound;
import lombok.Getter;

public class GildSection {
	private static final String MEMBER_TABLE_NAME = "GILDE_MEMBERS";
	
	private static final String COSTUM_DATA_KEY = "costum_data";
	
	@Getter
	private Gilde handle;
	@Getter
	private GildeType type;
	@Getter
	protected boolean active;
	private GildSectionPermission permissions = new GildSectionPermission(this);
	private NBTTagCompound costumData;
	
	protected ArrayList<Integer> players = new ArrayList<>();
	
	public GildSection(Gilde handle, GildeType type,boolean active,HashMap<InformationKey, String> infos) {
		this.handle = handle;
		this.type = type;
		this.active = active;
		try {
			if(infos.get(new InformationKey(type, COSTUM_DATA_KEY)).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(infos.get(new InformationKey(type, COSTUM_DATA_KEY)));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		permissions.loadGroups();
		loadData();
	}

	public void loadData(){
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `key`,`value` FROM "+Gilde.TABLE_NAME+" WHERE `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		HashMap<String, String> values = new InitHashMap<String, String>() {
			@Override
			public String defaultValue(String key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(info[0], info[1]);
		
		try {
			if(values.get(COSTUM_DATA_KEY).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(values.get(COSTUM_DATA_KEY));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		
		ArrayList<String[]> members = MySQL.getInstance().querySync("SELECT `playerId`, `rank` FROM `GILDE_MEMBERS` WHERE `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		for(String[] member : members){
			players.add(Integer.parseInt(member[0]));
			permissions.players.put(Integer.parseInt(member[0]), member[1]);
		}
	}
	
	public GildSectionPermission getPermission(){
		return permissions;
	}
	
	public List<Integer> getPlayers() {
		return Collections.unmodifiableList(players);
	}
	
	public void removePlayer(int player){
		if(players.contains(new Integer(player))){
			players.remove(new Integer(player));
			permissions.setGroup(player, null);
		}
	}
	public void addPlayer(int player,String rank){
		if(!players.contains(new Integer(player))){
			players.add(new Integer(player));
			permissions.setGroup(player, permissions.getGroup(rank));
		}
	}
	
	public NBTTagCompound getCostumData() {
		return costumData;
	}
	
	public void setCostumData(NBTTagCompound comp){
		this.costumData = comp;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+NBTCompressedStreamTools.toString(comp)+"' WHERE  `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='"+COSTUM_DATA_KEY+"'");
	}
	
	public void setActive(boolean active) {
		if(active == this.active)
			return;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+active+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='active'");
		this.active = active;
		if(!active)
			for(Integer member : players)
				removePlayer(member);
		if(active)
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.ACTIVE_GILD_SECTION));
		else
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.DEACTIVE_GILD_SECTION));
	}
}
                                      
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildCostumDataAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildUpdateSectionStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction.Action;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePlayerEvent;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.nbt.NBTCompressedStreamTools;
import dev.wolveringer.nbt.NBTTagCompound;
import lombok.Getter;

public class GildSection {
	private static final String MEMBER_TABLE_NAME = "GILDE_MEMBERS";
	
	private static final String COSTUM_DATA_KEY = "costum_data";
	
	@Getter
	private Gilde handle;
	@Getter
	private GildeType type;
	@Getter
	protected boolean active;
	private GildSectionPermission permissions = new GildSectionPermission(this);
	private NBTTagCompound costumData;
	
	protected ArrayList<Integer> players = new ArrayList<>();
	
	public GildSection(Gilde handle, GildeType type,boolean active,HashMap<InformationKey, String> infos) {
		this.handle = handle;
		this.type = type;
		this.active = active;
		try {
			this.costumData = NBTCompressedStreamTools.read(infos.get(new InformationKey(type, COSTUM_DATA_KEY)));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		permissions.loadGroups();
		loadData();
	}

	public void loadData(){
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `key`,`value` FROM "+Gilde.TABLE_NAME+" WHERE `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		HashMap<String, String> values = new InitHashMap<String, String>() {
			@Override
			public String defaultValue(String key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(info[0], info[1]);
		
		try {
			if(values.get(COSTUM_DATA_KEY).equalsIgnoreCase(""))
				this.costumData = new NBTTagCompound();
			else
				this.costumData = NBTCompressedStreamTools.read(values.get(COSTUM_DATA_KEY));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		
		ArrayList<String[]> members = MySQL.getInstance().querySync("SELECT `playerId`, `rank` FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		for(String[] member : members){
			players.add(Integer.parseInt(member[0]));
			permissions.players.put(Integer.parseInt(member[0]), member[1]);
		}
	}
	
	public GildSectionPermission getPermission(){
		return permissions;
	}
	
	public List<Integer> getPlayers() {
		return Collections.unmodifiableList(players);
	}
	
	public void removePlayer(int player){
		if(players.contains(new Integer(player))){
			players.remove(new Integer(player));
			permissions.setGroup(player, null);
		}
	}
	public void addPlayer(int player,String rank){
		if(!players.contains(new Integer(player))){
			players.add(new Integer(player));
			permissions.setGroup(player, permissions.getGroup(rank));
		}
	}
	
	public NBTTagCompound getCostumData() {
		return costumData;
	}
	
	public void setCostumData(NBTTagCompound comp){
		this.costumData = comp;
		try {
			MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+NBTCompressedStreamTools.toString(comp)+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='"+COSTUM_DATA_KEY+"'");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setActive(boolean active) {
		if(active == this.active)
			return;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+active+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='active'");
		this.active = active;
		if(!active)
			for(Integer member : players)
				removePlayer(member);
		if(active)
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.ACTIVE_GILD_SECTION));
		else
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.DEACTIVE_GILD_SECTION));
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildCostumDataAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildUpdateSectionStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction.Action;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePlayerEvent;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.nbt.NBTCompressedStreamTools;
import dev.wolveringer.nbt.NBTTagCompound;
import lombok.Getter;

public class GildSection {
	private static final String MEMBER_TABLE_NAME = "GILDE_MEMBERS";
	
	private static final String COSTUM_DATA_KEY = "costum_data";
	
	@Getter
	private Gilde handle;
	@Getter
	private GildeType type;
	@Getter
	protected boolean active;
	private GildSectionPermission permissions = new GildSectionPermission(this);
	private NBTTagCompound costumData;
	
	protected ArrayList<Integer> players = new ArrayList<>();
	
	public GildSection(Gilde handle, GildeType type,boolean active,HashMap<InformationKey, String> infos) {
		this.handle = handle;
		this.type = type;
		this.active = active;
		try {
			if(values.get(COSTUM_DATA_KEY).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(values.get(COSTUM_DATA_KEY));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		permissions.loadGroups();
		loadData();
	}

	public void loadData(){
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `key`,`value` FROM "+Gilde.TABLE_NAME+" WHERE `uuid`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		HashMap<String, String> values = new InitHashMap<String, String>() {
			@Override
			public String defaultValue(String key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(info[0], info[1]);
		
		try {
			if(values.get(COSTUM_DATA_KEY).equalsIgnoreCase("")){
				this.costumData = new NBTTagCompound();
				MySQL.getInstance().commandSync("INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+handle.getUuid()+"','"+type.toString()+"','"+COSTUM_DATA_KEY+"','"+NBTCompressedStreamTools.toString(this.costumData)+"')");
			}else
				this.costumData = NBTCompressedStreamTools.read(values.get(COSTUM_DATA_KEY));
		} catch (Exception e) {
			e.printStackTrace();
			this.costumData = new NBTTagCompound();
		}
		
		ArrayList<String[]> members = MySQL.getInstance().querySync("SELECT `playerId`, `rank` FROM `GILDE_MEMBERS` WHERE `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"'");
		for(String[] member : members){
			players.add(Integer.parseInt(member[0]));
			permissions.players.put(Integer.parseInt(member[0]), member[1]);
		}
	}
	
	public GildSectionPermission getPermission(){
		return permissions;
	}
	
	public List<Integer> getPlayers() {
		return Collections.unmodifiableList(players);
	}
	
	public void removePlayer(int player){
		if(players.contains(new Integer(player))){
			players.remove(new Integer(player));
			permissions.setGroup(player, null);
		}
	}
	public void addPlayer(int player,String rank){
		if(!players.contains(new Integer(player))){
			players.add(new Integer(player));
			permissions.setGroup(player, permissions.getGroup(rank));
		}
	}
	
	public NBTTagCompound getCostumData() {
		return costumData;
	}
	
	public void setCostumData(NBTTagCompound comp){
		this.costumData = comp;
		try {
			MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+NBTCompressedStreamTools.toString(comp)+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='"+COSTUM_DATA_KEY+"'");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setActive(boolean active) {
		if(active == this.active)
			return;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`='"+active+"' WHERE  `gilde`='"+handle.getUuid().toString()+"' AND `section`='"+type.toString()+"' AND `key`='active'");
		this.active = active;
		if(!active)
			for(Integer member : players)
				removePlayer(member);
		if(active)
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.ACTIVE_GILD_SECTION));
		else
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.DEACTIVE_GILD_SECTION));
	}
}
                       
*/