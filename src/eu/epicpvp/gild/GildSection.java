package eu.epicpvp.gild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import eu.epicpvp.datenserver.definitions.events.gilde.GildePlayerEvent;
import eu.epicpvp.datenserver.definitions.events.gilde.GildePropertiesUpdate;
import eu.epicpvp.datenserver.definitions.events.gilde.GildePropertiesUpdate.Property;
import eu.epicpvp.datenserver.definitions.gilde.GildeType;
import eu.epicpvp.datenserver.definitions.hashmaps.InitHashMap;
import eu.epicpvp.event.EventHelper;
import eu.epicpvp.mysql.MySQL;
import eu.epicpvp.nbt.NBTCompressedStreamTools;
import eu.epicpvp.nbt.NBTTagCompound;
import lombok.Getter;

public class GildSection {
	private static final String COSTUM_DATA_KEY = "costum_data";
	private static final String INVITE_GROUP = "invatation";
	@Getter
	private Gilde handle;
	@Getter
	private GildeType type;
	@Getter
	protected boolean active;
	@Getter
	private GildSectionMoney money = new GildSectionMoney(this);
	private GildSectionPermission permissions = new GildSectionPermission(this);
	private NBTTagCompound costumData;
	
	protected ArrayList<Integer> players = new ArrayList<>();
	@Getter
	protected ArrayList<Integer> requestedPlayers = new ArrayList<>();
	
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
		loadData();
		permissions.loadGroups();
		money.init();
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
			if(member[1].equalsIgnoreCase(INVITE_GROUP)){
				requestedPlayers.add(Integer.parseInt(member[0]));
			}
			else
			{
				players.add(Integer.parseInt(member[0]));
				permissions.players.put(Integer.parseInt(member[0]), member[1]);	
			}
		}
	}
	
	public void addRequest(int player){
		requestedPlayers.add((Integer)player);
		MySQL.getInstance().command("INSERT INTO `GILDE_MEMBERS`(`gilde`, `section`, `playerId`, `rank`) VALUES ('"+handle.getUuid().toString()+"','"+getType().toString()+"','"+player+"','"+INVITE_GROUP+"')");
		EventHelper.callGildEvent(getHandle().getUuid(), new GildePlayerEvent(getHandle().getUuid(), GildePlayerEvent.Action.ADD, player, getType(), INVITE_GROUP));
	}
	
	public void removeRequest(int player){
		requestedPlayers.remove((Object)(Integer)player);
		MySQL.getInstance().command("DELETE FROM `GILDE_MEMBERS` WHERE `gilde`='"+getHandle().getUuid().toString()+"' AND `section` = '"+getType().toString()+"' AND `playerId` = '"+player+"' AND rank='"+INVITE_GROUP+"'");
		EventHelper.callGildEvent(getHandle().getUuid(), new GildePlayerEvent(getHandle().getUuid(), GildePlayerEvent.Action.REMOVE, player, getType(), INVITE_GROUP));
	}
	
	public void acceptRequest(int player){
		requestedPlayers.remove((Object)(Integer)player);
		for(Gilde g : GildenManager.getManager().getAllLoadedGilden()){
			for(GildSection s : g.getActiveSections())
				if(s.requestedPlayers.contains(player))
					s.removeRequest(player);
		}
		MySQL.getInstance().command("DELETE FROM `GILDE_MEMBERS` WHERE `playerId` = '"+player+"' AND rank='"+INVITE_GROUP+"'");
		addPlayer(player, "default");
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
			for(Integer member : new ArrayList<>(players))
				removePlayer(member);
		if(active)
			addPlayer(handle.getOwnerId(), "owner");
		if(active)
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.ACTIVE_GILD_SECTION));
		else
			EventHelper.callGildEvent(handle.getUuid(), new GildePropertiesUpdate(handle.getUuid(), type, Property.DEACTIVE_GILD_SECTION));
	}
}