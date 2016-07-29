package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse.MemberInformation;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class Gilde {
	public static final String TABLE_NAME = "GILDE_INFORMATION";

	@Getter
	private UUID uuid;
	@Getter
	private String name;
	@Getter
	private String shortName;
	@Getter
	private int ownerId;

	private HashMap<GildeType, GildSection> selections = new HashMap<>();
	private boolean exist;
	private boolean loaded;

	public Gilde(UUID uuid) {
		this.uuid = uuid;
		load();
	}

	public synchronized void reload() {
		loaded = false;
		load();
	}
	
	public void setShortName(String shortName) {
		if (!exist)
			return;
		this.shortName = shortName;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`=" + shortName + " WHERE `uuid`='" + uuid + "' AND `section`='" + GildeType.ALL.toString() + "' AND `key`='shortName'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.SHORT_NAME));
	}

	public void setName(String name) {
		if (!exist)
			return;
		this.name = name;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`=" + name + " WHERE `uuid`='" + uuid + "' AND `section`='" + GildeType.ALL.toString() + "' AND `key`='name'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.NAME));
	}

	public void load() {
		if (loaded)
			return;
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `section`,`key`,`value` FROM " + TABLE_NAME + " WHERE `uuid`='" + uuid.toString() + "'", -1);
		if (informations.size() == 0) {
			exist = false;
			return;
		}
		HashMap<InformationKey, String> values = new InitHashMap<InformationKey, String>() {
			@Override
			public String defaultValue(InformationKey key) {
				return "";
			}
		};
		for (String[] info : informations)
			values.put(new InformationKey(GildeType.valueOf(info[0]), info[1]), info[2]);

		name = values.get(new InformationKey(GildeType.ALL, "name"));
		shortName = values.get(new InformationKey(GildeType.ALL, "shortName"));
		ownerId = Integer.parseInt(values.get(new InformationKey(GildeType.ALL, "ownerId")));

		for (GildeType type : GildeType.getPossibleValues())
			selections.put(type, new GildSection(this, type, values.get(new InformationKey(type, "active")).equalsIgnoreCase("true"), values));
	}

	public GildSection getSelection(GildeType type) {
		return selections.get(type);
	}

	public List<GildSection> getActiveSections() {
		ArrayList<GildSection> out = new ArrayList<>();
		for (GildSection s : selections.values())
			if (s.isActive())
				out.add(s);
		return out;
	}

	public GildeType[] getActiveSectionsArray() {
		ArrayList<GildeType> types = new ArrayList<>();
		for (GildSection sec : getActiveSections())
			types.add(sec.getType());
		return types.toArray(new GildeType[0]);
	}

	@Getter
	private static class PlayerInfo {
		private final int playerId;
		private HashMap<GildeType, String> ranks;

		public MemberInformation create() {
			GildeType[] types = new GildeType[ranks.size()];
			String[] groups = new String[ranks.size()];
			int index = 0;
			for (Entry<GildeType, String> e : ranks.entrySet()) {
				types[index] = e.getKey();
				groups[index++] = e.getValue();
			}
			return new MemberInformation(index, types, groups);
		}

		public PlayerInfo(int playerId) {
			this.playerId = playerId;
			this.ranks = new HashMap<>();
		}
	}

	public MemberInformation[] buildMemberInfo() {
		HashMap<Integer, PlayerInfo> info = new InitHashMap<Integer, PlayerInfo>() {
			@Override
			public PlayerInfo defaultValue(Integer key) {
				return new PlayerInfo(key);
			}
		};

		for (GildSection s : getActiveSections())
			for (Integer member : s.players)
				info.get(member).ranks.put(s.getType(), s.getPermission().getGroup(member).getName());
		MemberInformation[] infos = new MemberInformation[info.size()];
		int index = 0;
		for (PlayerInfo i : info.values())
			infos[index++] = i.create();
		return infos;
	}
}/*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse.MemberInformation;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class Gilde {
	public static final String TABLE_NAME = "GILDE_INFORMATION";
	
	@Getter
	private UUID uuid;
	@Getter
	private String name;
	@Getter
	private String shortName;
	@Getter
	private int ownerId;
	
	private HashMap<GildeType, GildSection> selections;
	private boolean exist;
	private boolean loaded;
	
	public Gilde(UUID uuid) {
		this.uuid = uuid;
		load();
	}
	
	public synchronized void reload(){
		loaded = false;
		load();
	}
	
	public void setShortName(String shortName) {
		if(!exist)
			return;
		this.shortName = shortName;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`="+shortName+" WHERE `uuid`='"+uuid+"' AND`section`='"+GildeType.ALL.toString()+"' AND `key`='shortName'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.SHORT_NAME));
	}
	public void setName(String name) {
		if(!exist)
			return;
		this.name = name;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`="+name+" WHERE `uuid`='"+uuid+"' AND`section`='"+GildeType.ALL.toString()+"' AND `key`='shortName'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.NAME));
	}
	
	public void load(){
		if(loaded)
			return;
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `section`,`key`,`value` FROM "+TABLE_NAME+" WHERE `uuid`='"+uuid.toString()+"'", -1);
		if(informations.size() == 0){
			exist = false;
			return;
		}
		HashMap<InformationKey, String> values = new InitHashMap<InformationKey, String>() {
			@Override
			public String defaultValue(InformationKey key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(new InformationKey(GildeType.valueOf(info[0]), info[1]), info[2]);
		
		name = values.get(new InformationKey(GildeType.ALL, "name"));
		shortName = values.get(new InformationKey(GildeType.ALL, "shortName"));
		ownerId = Integer.parseInt(values.get(new InformationKey(GildeType.ALL, "ownerId")));
		
		for(GildeType type : GildeType.getPossibleValues())
			selections.put(type, new GildSection(this, type, values.get(new InformationKey(type, "active")).equalsIgnoreCase("true"),values));
	}
	
	public GildSection getSelection(GildeType type){
		return selections.get(type);
	}
	
	public List<GildSection> getActiveSections(){
		ArrayList<GildSection> out = new ArrayList<>();
		for(GildSection s : selections.values())
			if(s.isActive())
				out.add(s);
		return out;
	}

	public GildeType[] getActiveSectionsArray() {
		ArrayList<GildeType> types = new ArrayList<>();
		for(GildSection sec : getActiveSections())
			types.add(sec.getType());
		return types.toArray(new GildeType[0]);
	}
	
	@Getter
	private static class PlayerInfo {
		private final int playerId;
		private HashMap<GildeType, String> ranks;
		
		public MemberInformation create(){
			GildeType[] types = new GildeType[ranks.size()];
			String[] groups = new String[ranks.size()];
			int index = 0;
			for(Entry<GildeType, String> e : ranks.entrySet()){
				types[index] = e.getKey();
				groups[index++] = e.getValue();
			}
			return new MemberInformation(index, types, groups);
		}



		public PlayerInfo(int playerId) {
			this.playerId = playerId;
			this.ranks = new HashMap<>();
		}
	}
	
	public MemberInformation[] buildMemberInfo(){
		HashMap<Integer, PlayerInfo> info = new InitHashMap<Integer, PlayerInfo>() {
			@Override
			public PlayerInfo defaultValue(Integer key) {
				return new PlayerInfo(key);
			}
		};
		
		for(GildSection s : getActiveSections())
			for(Integer member : s.players){
				info.get(member).ranks.put(s.getType(), s.getPermission().getGroup(member).getName());
			}
		return null;
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse.MemberInformation;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class Gilde {
	public static final String TABLE_NAME = "GILDE_INFORMATION";

	@Getter
	private UUID uuid;
	@Getter
	private String name;
	@Getter
	private String shortName;
	@Getter
	private int ownerId;

	private HashMap<GildeType, GildSection> selections;
	private boolean exist;
	private boolean loaded;

	public Gilde(UUID uuid) {
		this.uuid = uuid;
		load();
	}

	public synchronized void reload() {
		loaded = false;
		load();
	}
	
	/.*
	 INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+uuid+"','"+GildeType.ALL.toString()+"','shortName','"+name+"')
	 INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+uuid+"','"+GildeType.ALL.toString()+"','name','"+name+"')
	 INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+uuid+"','"+GildeType.ALL.toString()+"','ownerId','"+owner+"')
	 *./

	public void setShortName(String shortName) {
		if (!exist)
			return;
		this.shortName = shortName;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`=" + shortName + " WHERE `uuid`='" + uuid + "' AND `section`='" + GildeType.ALL.toString() + "' AND `key`='shortName'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.SHORT_NAME));
	}

	public void setName(String name) {
		if (!exist)
			return;
		this.name = name;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`=" + name + " WHERE `uuid`='" + uuid + "' AND `section`='" + GildeType.ALL.toString() + "' AND `key`='name'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.NAME));
	}

	public void load() {
		if (loaded)
			return;
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `section`,`key`,`value` FROM " + TABLE_NAME + " WHERE `uuid`='" + uuid.toString() + "'", -1);
		if (informations.size() == 0) {
			exist = false;
			return;
		}
		HashMap<InformationKey, String> values = new InitHashMap<InformationKey, String>() {
			@Override
			public String defaultValue(InformationKey key) {
				return "";
			}
		};
		for (String[] info : informations)
			values.put(new InformationKey(GildeType.valueOf(info[0]), info[1]), info[2]);

		name = values.get(new InformationKey(GildeType.ALL, "name"));
		shortName = values.get(new InformationKey(GildeType.ALL, "shortName"));
		ownerId = Integer.parseInt(values.get(new InformationKey(GildeType.ALL, "ownerId")));

		for (GildeType type : GildeType.getPossibleValues())
			selections.put(type, new GildSection(this, type, values.get(new InformationKey(type, "active")).equalsIgnoreCase("true"), values));
	}

	public GildSection getSelection(GildeType type) {
		return selections.get(type);
	}

	public List<GildSection> getActiveSections() {
		ArrayList<GildSection> out = new ArrayList<>();
		for (GildSection s : selections.values())
			if (s.isActive())
				out.add(s);
		return out;
	}

	public GildeType[] getActiveSectionsArray() {
		ArrayList<GildeType> types = new ArrayList<>();
		for (GildSection sec : getActiveSections())
			types.add(sec.getType());
		return types.toArray(new GildeType[0]);
	}

	@Getter
	private static class PlayerInfo {
		private final int playerId;
		private HashMap<GildeType, String> ranks;

		public MemberInformation create() {
			GildeType[] types = new GildeType[ranks.size()];
			String[] groups = new String[ranks.size()];
			int index = 0;
			for (Entry<GildeType, String> e : ranks.entrySet()) {
				types[index] = e.getKey();
				groups[index++] = e.getValue();
			}
			return new MemberInformation(index, types, groups);
		}

		public PlayerInfo(int playerId) {
			this.playerId = playerId;
			this.ranks = new HashMap<>();
		}
	}

	public MemberInformation[] buildMemberInfo() {
		HashMap<Integer, PlayerInfo> info = new InitHashMap<Integer, PlayerInfo>() {
			@Override
			public PlayerInfo defaultValue(Integer key) {
				return new PlayerInfo(key);
			}
		};

		for (GildSection s : getActiveSections())
			for (Integer member : s.players)
				info.get(member).ranks.put(s.getType(), s.getPermission().getGroup(member).getName());
		MemberInformation[] infos = new MemberInformation[info.size()];
		int index = 0;
		for (PlayerInfo i : info.values())
			infos[index++] = i.create();
		return infos;
	}
}
                                                                                                                                                        
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse.MemberInformation;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class Gilde {
	public static final String TABLE_NAME = "GILDE_INFORMATION";

	@Getter
	private UUID uuid;
	@Getter
	private String name;
	@Getter
	private String shortName;
	@Getter
	private int ownerId;

	private HashMap<GildeType, GildSection> selections;
	private boolean exist;
	private boolean loaded;

	public Gilde(UUID uuid) {
		this.uuid = uuid;
		load();
	}

	public synchronized void reload() {
		loaded = false;
		load();
	}
	
	public void setShortName(String shortName) {
		if (!exist)
			return;
		this.shortName = shortName;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`=" + shortName + " WHERE `uuid`='" + uuid + "' AND `section`='" + GildeType.ALL.toString() + "' AND `key`='shortName'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.SHORT_NAME));
	}

	public void setName(String name) {
		if (!exist)
			return;
		this.name = name;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`=" + name + " WHERE `uuid`='" + uuid + "' AND `section`='" + GildeType.ALL.toString() + "' AND `key`='name'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.NAME));
	}

	public void load() {
		if (loaded)
			return;
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `section`,`key`,`value` FROM " + TABLE_NAME + " WHERE `uuid`='" + uuid.toString() + "'", -1);
		if (informations.size() == 0) {
			exist = false;
			return;
		}
		HashMap<InformationKey, String> values = new InitHashMap<InformationKey, String>() {
			@Override
			public String defaultValue(InformationKey key) {
				return "";
			}
		};
		for (String[] info : informations)
			values.put(new InformationKey(GildeType.valueOf(info[0]), info[1]), info[2]);

		name = values.get(new InformationKey(GildeType.ALL, "name"));
		shortName = values.get(new InformationKey(GildeType.ALL, "shortName"));
		ownerId = Integer.parseInt(values.get(new InformationKey(GildeType.ALL, "ownerId")));

		for (GildeType type : GildeType.getPossibleValues())
			selections.put(type, new GildSection(this, type, values.get(new InformationKey(type, "active")).equalsIgnoreCase("true"), values));
	}

	public GildSection getSelection(GildeType type) {
		return selections.get(type);
	}

	public List<GildSection> getActiveSections() {
		ArrayList<GildSection> out = new ArrayList<>();
		for (GildSection s : selections.values())
			if (s.isActive())
				out.add(s);
		return out;
	}

	public GildeType[] getActiveSectionsArray() {
		ArrayList<GildeType> types = new ArrayList<>();
		for (GildSection sec : getActiveSections())
			types.add(sec.getType());
		return types.toArray(new GildeType[0]);
	}

	@Getter
	private static class PlayerInfo {
		private final int playerId;
		private HashMap<GildeType, String> ranks;

		public MemberInformation create() {
			GildeType[] types = new GildeType[ranks.size()];
			String[] groups = new String[ranks.size()];
			int index = 0;
			for (Entry<GildeType, String> e : ranks.entrySet()) {
				types[index] = e.getKey();
				groups[index++] = e.getValue();
			}
			return new MemberInformation(index, types, groups);
		}

		public PlayerInfo(int playerId) {
			this.playerId = playerId;
			this.ranks = new HashMap<>();
		}
	}

	public MemberInformation[] buildMemberInfo() {
		HashMap<Integer, PlayerInfo> info = new InitHashMap<Integer, PlayerInfo>() {
			@Override
			public PlayerInfo defaultValue(Integer key) {
				return new PlayerInfo(key);
			}
		};

		for (GildSection s : getActiveSections())
			for (Integer member : s.players)
				info.get(member).ranks.put(s.getType(), s.getPermission().getGroup(member).getName());
		MemberInformation[] infos = new MemberInformation[info.size()];
		int index = 0;
		for (PlayerInfo i : info.values())
			infos[index++] = i.create();
		return infos;
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse.MemberInformation;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class Gilde {
	public static final String TABLE_NAME = "GILDE_INFORMATION";
	
	@Getter
	private UUID uuid;
	@Getter
	private String name;
	@Getter
	private String shortName;
	@Getter
	private int ownerId;
	
	private HashMap<GildeType, GildSection> selections;
	private boolean exist;
	private boolean loaded;
	
	public Gilde(UUID uuid) {
		this.uuid = uuid;
		load();
	}
	
	public synchronized void reload(){
		loaded = false;
		load();
	}
	
	public void setShortName(String shortName) {
		if(!exist)
			return;
		this.shortName = shortName;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`="+shortName+" WHERE `uuid`='"+uuid+"' AND`section`='"+GildeType.ALL.toString()+"' AND `key`='shortName'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.SHORT_NAME));
	}
	public void setName(String name) {
		if(!exist)
			return;
		this.name = name;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`="+name+" WHERE `uuid`='"+uuid+"' AND`section`='"+GildeType.ALL.toString()+"' AND `key`='shortName'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.NAME));
	}
	
	public void load(){
		if(loaded)
			return;
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `section`,`key`,`value` FROM "+TABLE_NAME+" WHERE `uuid`='"+uuid.toString()+"'", -1);
		if(informations.size() == 0){
			exist = false;
			return;
		}
		HashMap<InformationKey, String> values = new InitHashMap<InformationKey, String>() {
			@Override
			public String defaultValue(InformationKey key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(new InformationKey(GildeType.valueOf(info[0]), info[1]), info[2]);
		
		name = values.get(new InformationKey(GildeType.ALL, "name"));
		shortName = values.get(new InformationKey(GildeType.ALL, "shortName"));
		ownerId = Integer.parseInt(values.get(new InformationKey(GildeType.ALL, "ownerId")));
		
		for(GildeType type : GildeType.getPossibleValues())
			selections.put(type, new GildSection(this, type, values.get(new InformationKey(type, "active")).equalsIgnoreCase("true"),values));
	}
	
	public GildSection getSelection(GildeType type){
		return selections.get(type);
	}
	
	public List<GildSection> getActiveSections(){
		ArrayList<GildSection> out = new ArrayList<>();
		for(GildSection s : selections.values())
			if(s.isActive())
				out.add(s);
		return out;
	}

	public GildeType[] getActiveSectionsArray() {
		ArrayList<GildeType> types = new ArrayList<>();
		for(GildSection sec : getActiveSections())
			types.add(sec.getType());
		return types.toArray(new GildeType[0]);
	}
	
	@Getter
	private static class PlayerInfo {
		private final int playerId;
		private HashMap<GildeType, String> ranks;
		
		public MemberInformation create(){
			GildeType[] types = new GildeType[ranks.size()];
			String[] groups = new String[ranks.size()];
			int index = 0;
			for(Entry<GildeType, String> e : ranks.entrySet()){
				types[index] = e.getKey();
				groups[index++] = e.getValue();
			}
			return new MemberInformation(index, types, groups);
		}



		public PlayerInfo(int playerId) {
			this.playerId = playerId;
			this.ranks = new HashMap<>();
		}
	}
	
	public MemberInformation[] buildMemberInfo(){
		HashMap<Integer, PlayerInfo> info = new InitHashMap<Integer, PlayerInfo>() {
			@Override
			public PlayerInfo defaultValue(Integer key) {
				return new PlayerInfo(key);
			}
		};
		
		for(GildSection s : getActiveSections())
			for(Integer member : s.players){
				
			}
		return null;
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse.MemberInformation;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class Gilde {
	public static final String TABLE_NAME = "GILDE_INFORMATION";

	@Getter
	private UUID uuid;
	@Getter
	private String name;
	@Getter
	private String shortName;
	@Getter
	private int ownerId;

	private HashMap<GildeType, GildSection> selections = new HashMap<>();
	private boolean exist;
	private boolean loaded;

	public Gilde(UUID uuid) {
		this.uuid = uuid;
		load();
	}

	public synchronized void reload() {
		loaded = false;
		load();
	}
	
	public void setShortName(String shortName) {
		if (!exist)
			return;
		this.shortName = shortName;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`=" + shortName + " WHERE `uuid`='" + uuid + "' AND `section`='" + GildeType.ALL.toString() + "' AND `key`='shortName'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.SHORT_NAME));
	}

	public void setName(String name) {
		if (!exist)
			return;
		this.name = name;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`=" + name + " WHERE `uuid`='" + uuid + "' AND `section`='" + GildeType.ALL.toString() + "' AND `key`='name'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.NAME));
	}

	public void load() {
		if (loaded)
			return;
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `section`,`key`,`value` FROM " + TABLE_NAME + " WHERE `uuid`='" + uuid.toString() + "'", -1);
		if (informations.size() == 0) {
			exist = false;
			return;
		}
		HashMap<InformationKey, String> values = new InitHashMap<InformationKey, String>() {
			@Override
			public String defaultValue(InformationKey key) {
				return "";
			}
		};
		for (String[] info : informations)
			values.put(new InformationKey(GildeType.valueOf(info[0]), info[1]), info[2]);

		name = values.get(new InformationKey(GildeType.ALL, "name"));
		shortName = values.get(new InformationKey(GildeType.ALL, "shortName"));
		ownerId = Integer.parseInt(values.get(new InformationKey(GildeType.ALL, "ownerId")));

		for (GildeType type : GildeType.getPossibleValues())
			selections.put(type, new GildSection(this, type, values.get(new InformationKey(type, "active")).equalsIgnoreCase("true"), values));
	}

	public GildSection getSelection(GildeType type) {
		return selections.get(type);
	}

	public List<GildSection> getActiveSections() {
		ArrayList<GildSection> out = new ArrayList<>();
		for (GildSection s : selections.values())
			if (s.isActive())
				out.add(s);
		return out;
	}

	public GildeType[] getActiveSectionsArray() {
		ArrayList<GildeType> types = new ArrayList<>();
		for (GildSection sec : getActiveSections())
			types.add(sec.getType());
		return types.toArray(new GildeType[0]);
	}

	@Getter
	private static class PlayerInfo {
		private final int playerId;
		private HashMap<GildeType, String> ranks;

		public MemberInformation create() {
			GildeType[] types = new GildeType[ranks.size()];
			String[] groups = new String[ranks.size()];
			int index = 0;
			for (Entry<GildeType, String> e : ranks.entrySet()) {
				types[index] = e.getKey();
				groups[index++] = e.getValue();
			}
			return new MemberInformation(playerId, types, groups);
		}

		public PlayerInfo(int playerId) {
			this.playerId = playerId;
			this.ranks = new HashMap<>();
		}
	}

	public MemberInformation[] buildMemberInfo() {
		HashMap<Integer, PlayerInfo> info = new InitHashMap<Integer, PlayerInfo>() {
			@Override
			public PlayerInfo defaultValue(Integer key) {
				return new PlayerInfo(key);
			}
		};

		for (GildSection s : getActiveSections())
			for (Integer member : s.players)
				info.get(member).ranks.put(s.getType(), s.getPermission().getGroup(member).getName());
		MemberInformation[] infos = new MemberInformation[info.size()];
		int index = 0;
		for (PlayerInfo i : info.values())
			infos[index++] = i.create();
		return infos;
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse.MemberInformation;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class Gilde {
	public static final String TABLE_NAME = "GILDE_INFORMATION";

	@Getter
	private UUID uuid;
	@Getter
	private String name;
	@Getter
	private String shortName;
	@Getter
	private int ownerId;

	private HashMap<GildeType, GildSection> selections;
	private boolean exist;
	private boolean loaded;

	public Gilde(UUID uuid) {
		this.uuid = uuid;
		load();
	}

	public synchronized void reload() {
		loaded = false;
		load();
	}

	public void setShortName(String shortName) {
		if (!exist)
			return;
		this.shortName = shortName;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`=" + shortName + " WHERE `uuid`='" + uuid + "' AND `section`='" + GildeType.ALL.toString() + "' AND `key`='shortName'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.SHORT_NAME));
	}

	public void setName(String name) {
		if (!exist)
			return;
		this.name = name;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`=" + name + " WHERE `uuid`='" + uuid + "' AND `section`='" + GildeType.ALL.toString() + "' AND `key`='name'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.NAME));
	}

	public void load() {
		if (loaded)
			return;
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `section`,`key`,`value` FROM " + TABLE_NAME + " WHERE `uuid`='" + uuid.toString() + "'", -1);
		if (informations.size() == 0) {
			exist = false;
			return;
		}
		HashMap<InformationKey, String> values = new InitHashMap<InformationKey, String>() {
			@Override
			public String defaultValue(InformationKey key) {
				return "";
			}
		};
		for (String[] info : informations)
			values.put(new InformationKey(GildeType.valueOf(info[0]), info[1]), info[2]);

		name = values.get(new InformationKey(GildeType.ALL, "name"));
		shortName = values.get(new InformationKey(GildeType.ALL, "shortName"));
		ownerId = Integer.parseInt(values.get(new InformationKey(GildeType.ALL, "ownerId")));

		for (GildeType type : GildeType.getPossibleValues())
			selections.put(type, new GildSection(this, type, values.get(new InformationKey(type, "active")).equalsIgnoreCase("true"), values));
	}

	public GildSection getSelection(GildeType type) {
		return selections.get(type);
	}

	public List<GildSection> getActiveSections() {
		ArrayList<GildSection> out = new ArrayList<>();
		for (GildSection s : selections.values())
			if (s.isActive())
				out.add(s);
		return out;
	}

	public GildeType[] getActiveSectionsArray() {
		ArrayList<GildeType> types = new ArrayList<>();
		for (GildSection sec : getActiveSections())
			types.add(sec.getType());
		return types.toArray(new GildeType[0]);
	}

	@Getter
	private static class PlayerInfo {
		private final int playerId;
		private HashMap<GildeType, String> ranks;

		public MemberInformation create() {
			GildeType[] types = new GildeType[ranks.size()];
			String[] groups = new String[ranks.size()];
			int index = 0;
			for (Entry<GildeType, String> e : ranks.entrySet()) {
				types[index] = e.getKey();
				groups[index++] = e.getValue();
			}
			return new MemberInformation(index, types, groups);
		}

		public PlayerInfo(int playerId) {
			this.playerId = playerId;
			this.ranks = new HashMap<>();
		}
	}

	public MemberInformation[] buildMemberInfo() {
		HashMap<Integer, PlayerInfo> info = new InitHashMap<Integer, PlayerInfo>() {
			@Override
			public PlayerInfo defaultValue(Integer key) {
				return new PlayerInfo(key);
			}
		};

		for (GildSection s : getActiveSections())
			for (Integer member : s.players)
				info.get(member).ranks.put(s.getType(), s.getPermission().getGroup(member).getName());
		MemberInformation[] infos = new MemberInformation[info.size()];
		int index = 0;
		for (PlayerInfo i : info.values())
			infos[index++] = i.create();
		return infos;
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse.MemberInformation;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class Gilde {
	public static final String TABLE_NAME = "GILDE_INFORMATION";

	@Getter
	private UUID uuid;
	@Getter
	private String name;
	@Getter
	private String shortName;
	@Getter
	private int ownerId;

	private HashMap<GildeType, GildSection> selections;
	private boolean exist;
	private boolean loaded;

	public Gilde(UUID uuid) {
		this.uuid = uuid;
		load();
	}

	public synchronized void reload() {
		loaded = false;
		load();
	}

	public void setShortName(String shortName) {
		if (!exist)
			return;
		this.shortName = shortName;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`=" + shortName + " WHERE `uuid`='" + uuid + "' AND`section`='" + GildeType.ALL.toString() + "' AND `key`='shortName'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.SHORT_NAME));
	}

	public void setName(String name) {
		if (!exist)
			return;
		this.name = name;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`=" + name + " WHERE `uuid`='" + uuid + "' AND `section`='" + GildeType.ALL.toString() + "' AND `key`='shortName'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.NAME));
	}

	public void load() {
		if (loaded)
			return;
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `section`,`key`,`value` FROM " + TABLE_NAME + " WHERE `uuid`='" + uuid.toString() + "'", -1);
		if (informations.size() == 0) {
			exist = false;
			return;
		}
		HashMap<InformationKey, String> values = new InitHashMap<InformationKey, String>() {
			@Override
			public String defaultValue(InformationKey key) {
				return "";
			}
		};
		for (String[] info : informations)
			values.put(new InformationKey(GildeType.valueOf(info[0]), info[1]), info[2]);

		name = values.get(new InformationKey(GildeType.ALL, "name"));
		shortName = values.get(new InformationKey(GildeType.ALL, "shortName"));
		ownerId = Integer.parseInt(values.get(new InformationKey(GildeType.ALL, "ownerId")));

		for (GildeType type : GildeType.getPossibleValues())
			selections.put(type, new GildSection(this, type, values.get(new InformationKey(type, "active")).equalsIgnoreCase("true"), values));
	}

	public GildSection getSelection(GildeType type) {
		return selections.get(type);
	}

	public List<GildSection> getActiveSections() {
		ArrayList<GildSection> out = new ArrayList<>();
		for (GildSection s : selections.values())
			if (s.isActive())
				out.add(s);
		return out;
	}

	public GildeType[] getActiveSectionsArray() {
		ArrayList<GildeType> types = new ArrayList<>();
		for (GildSection sec : getActiveSections())
			types.add(sec.getType());
		return types.toArray(new GildeType[0]);
	}

	@Getter
	private static class PlayerInfo {
		private final int playerId;
		private HashMap<GildeType, String> ranks;

		public MemberInformation create() {
			GildeType[] types = new GildeType[ranks.size()];
			String[] groups = new String[ranks.size()];
			int index = 0;
			for (Entry<GildeType, String> e : ranks.entrySet()) {
				types[index] = e.getKey();
				groups[index++] = e.getValue();
			}
			return new MemberInformation(index, types, groups);
		}

		public PlayerInfo(int playerId) {
			this.playerId = playerId;
			this.ranks = new HashMap<>();
		}
	}

	public MemberInformation[] buildMemberInfo() {
		HashMap<Integer, PlayerInfo> info = new InitHashMap<Integer, PlayerInfo>() {
			@Override
			public PlayerInfo defaultValue(Integer key) {
				return new PlayerInfo(key);
			}
		};

		for (GildSection s : getActiveSections())
			for (Integer member : s.players)
				info.get(member).ranks.put(s.getType(), s.getPermission().getGroup(member).getName());
		MemberInformation[] infos = new MemberInformation[info.size()];
		int index = 0;
		for (PlayerInfo i : info.values())
			infos[index++] = i.create();
		return infos;
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse.MemberInformation;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class Gilde {
	public static final String TABLE_NAME = "GILDE_INFORMATION";

	@Getter
	private UUID uuid;
	@Getter
	private String name;
	@Getter
	private String shortName;
	@Getter
	private int ownerId;

	private HashMap<GildeType, GildSection> selections;
	private boolean exist;
	private boolean loaded;

	public Gilde(UUID uuid) {
		this.uuid = uuid;
		load();
	}

	public synchronized void reload() {
		loaded = false;
		load();
	}

	public void setShortName(String shortName) {
		if (!exist)
			return;
		this.shortName = shortName;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`=" + shortName + " WHERE `uuid`='" + uuid + "' AND `section`='" + GildeType.ALL.toString() + "' AND `key`='shortName'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.SHORT_NAME));
	}

	public void setName(String name) {
		if (!exist)
			return;
		this.name = name;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`=" + name + " WHERE `uuid`='" + uuid + "' AND `section`='" + GildeType.ALL.toString() + "' AND `key`='shortName'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.NAME));
	}

	public void load() {
		if (loaded)
			return;
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `section`,`key`,`value` FROM " + TABLE_NAME + " WHERE `uuid`='" + uuid.toString() + "'", -1);
		if (informations.size() == 0) {
			exist = false;
			return;
		}
		HashMap<InformationKey, String> values = new InitHashMap<InformationKey, String>() {
			@Override
			public String defaultValue(InformationKey key) {
				return "";
			}
		};
		for (String[] info : informations)
			values.put(new InformationKey(GildeType.valueOf(info[0]), info[1]), info[2]);

		name = values.get(new InformationKey(GildeType.ALL, "name"));
		shortName = values.get(new InformationKey(GildeType.ALL, "shortName"));
		ownerId = Integer.parseInt(values.get(new InformationKey(GildeType.ALL, "ownerId")));

		for (GildeType type : GildeType.getPossibleValues())
			selections.put(type, new GildSection(this, type, values.get(new InformationKey(type, "active")).equalsIgnoreCase("true"), values));
	}

	public GildSection getSelection(GildeType type) {
		return selections.get(type);
	}

	public List<GildSection> getActiveSections() {
		ArrayList<GildSection> out = new ArrayList<>();
		for (GildSection s : selections.values())
			if (s.isActive())
				out.add(s);
		return out;
	}

	public GildeType[] getActiveSectionsArray() {
		ArrayList<GildeType> types = new ArrayList<>();
		for (GildSection sec : getActiveSections())
			types.add(sec.getType());
		return types.toArray(new GildeType[0]);
	}

	@Getter
	private static class PlayerInfo {
		private final int playerId;
		private HashMap<GildeType, String> ranks;

		public MemberInformation create() {
			GildeType[] types = new GildeType[ranks.size()];
			String[] groups = new String[ranks.size()];
			int index = 0;
			for (Entry<GildeType, String> e : ranks.entrySet()) {
				types[index] = e.getKey();
				groups[index++] = e.getValue();
			}
			return new MemberInformation(index, types, groups);
		}

		public PlayerInfo(int playerId) {
			this.playerId = playerId;
			this.ranks = new HashMap<>();
		}
	}

	public MemberInformation[] buildMemberInfo() {
		HashMap<Integer, PlayerInfo> info = new InitHashMap<Integer, PlayerInfo>() {
			@Override
			public PlayerInfo defaultValue(Integer key) {
				return new PlayerInfo(key);
			}
		};

		for (GildSection s : getActiveSections())
			for (Integer member : s.players)
				info.get(member).ranks.put(s.getType(), s.getPermission().getGroup(member).getName());
		MemberInformation[] infos = new MemberInformation[info.size()];
		int index = 0;
		for (PlayerInfo i : info.values())
			infos[index++] = i.create();
		return infos;
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse.MemberInformation;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class Gilde {
	public static final String TABLE_NAME = "GILDE_INFORMATION";

	@Getter
	private UUID uuid;
	@Getter
	private String name;
	@Getter
	private String shortName;
	@Getter
	private int ownerId;

	private HashMap<GildeType, GildSection> selections;
	private boolean exist;
	private boolean loaded;

	public Gilde(UUID uuid) {
		this.uuid = uuid;
		load();
	}

	public synchronized void reload() {
		loaded = false;
		load();
	}
	
	public void setShortName(String shortName) {
		if (!exist)
			return;
		this.shortName = shortName;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`=" + shortName + " WHERE `uuid`='" + uuid + "' AND `section`='" + GildeType.ALL.toString() + "' AND `key`='shortName'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.SHORT_NAME));
	}

	public void setName(String name) {
		if (!exist)
			return;
		this.name = name;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`=" + name + " WHERE `uuid`='" + uuid + "' AND `section`='" + GildeType.ALL.toString() + "' AND `key`='name'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.NAME));
	}

	public void load() {
		if (loaded)
			return;
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `section`,`key`,`value` FROM " + TABLE_NAME + " WHERE `uuid`='" + uuid.toString() + "'", -1);
		if (informations.size() == 0) {
			exist = false;
			return;
		}
		HashMap<InformationKey, String> values = new InitHashMap<InformationKey, String>() {
			@Override
			public String defaultValue(InformationKey key) {
				return "";
			}
		};
		for (String[] info : informations)
			values.put(new InformationKey(GildeType.valueOf(info[0]), info[1]), info[2]);

		name = values.get(new InformationKey(GildeType.ALL, "name"));
		shortName = values.get(new InformationKey(GildeType.ALL, "shortName"));
		ownerId = Integer.parseInt(values.get(new InformationKey(GildeType.ALL, "ownerId")));

		for (GildeType type : GildeType.getPossibleValues())
			selections.put(type, new GildSection(this, type, values.get(new InformationKey(type, "active")).equalsIgnoreCase("true"), values));
	}

	public GildSection getSelection(GildeType type) {
		return selections.get(type);
	}

	public List<GildSection> getActiveSections() {
		ArrayList<GildSection> out = new ArrayList<>();
		for (GildSection s : selections.values())
			if (s.isActive())
				out.add(s);
		return out;
	}

	public GildeType[] getActiveSectionsArray() {
		ArrayList<GildeType> types = new ArrayList<>();
		for (GildSection sec : getActiveSections())
			types.add(sec.getType());
		return types.toArray(new GildeType[0]);
	}

	@Getter
	private static class PlayerInfo {
		private final int playerId;
		private HashMap<GildeType, String> ranks;

		public MemberInformation create() {
			GildeType[] types = new GildeType[ranks.size()];
			String[] groups = new String[ranks.size()];
			int index = 0;
			for (Entry<GildeType, String> e : ranks.entrySet()) {
				types[index] = e.getKey();
				groups[index++] = e.getValue();
			}
			return new MemberInformation(index, types, groups);
		}

		public PlayerInfo(int playerId) {
			this.playerId = playerId;
			this.ranks = new HashMap<>();
		}
	}

	public MemberInformation[] buildMemberInfo() {
		HashMap<Integer, PlayerInfo> info = new InitHashMap<Integer, PlayerInfo>() {
			@Override
			public PlayerInfo defaultValue(Integer key) {
				return new PlayerInfo(key);
			}
		};

		for (GildSection s : getActiveSections())
			for (Integer member : s.players)
				info.get(member).ranks.put(s.getType(), s.getPermission().getGroup(member).getName());
		MemberInformation[] infos = new MemberInformation[info.size()];
		int index = 0;
		for (PlayerInfo i : info.values())
			infos[index++] = i.create();
		return infos;
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse.MemberInformation;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class Gilde {
	public static final String TABLE_NAME = "GILDE_INFORMATION";

	@Getter
	private UUID uuid;
	@Getter
	private String name;
	@Getter
	private String shortName;
	@Getter
	private int ownerId;

	private HashMap<GildeType, GildSection> selections;
	private boolean exist;
	private boolean loaded;

	public Gilde(UUID uuid) {
		this.uuid = uuid;
		load();
	}

	public synchronized void reload() {
		loaded = false;
		load();
	}
	
	/.*
	 INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+uuid+"','"+GildeType.ALL.toString()+"','shortName','"+name+"')
	 INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+uuid+"','"+GildeType.ALL.toString()+"','name','"+name+"')
	 INSERT INTO `GILDE_INFORMATION`(`uuid`, `section`, `key`, `value`) VALUES ('"+uuid+"','"+GildeType.ALL.toString()+"','ownerId','"+owner+"')
	 *./

	public void setShortName(String shortName) {
		if (!exist)
			return;
		this.shortName = shortName;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`=" + shortName + " WHERE `uuid`='" + uuid + "' AND `section`='" + GildeType.ALL.toString() + "' AND `key`='shortName'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.SHORT_NAME));
	}

	public void setName(String name) {
		if (!exist)
			return;
		this.name = name;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`=" + name + " WHERE `uuid`='" + uuid + "' AND `section`='" + GildeType.ALL.toString() + "' AND `key`='name'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.NAME));
	}

	public void load() {
		if (loaded)
			return;
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `section`,`key`,`value` FROM " + TABLE_NAME + " WHERE `uuid`='" + uuid.toString() + "'", -1);
		if (informations.size() == 0) {
			exist = false;
			return;
		}
		HashMap<InformationKey, String> values = new InitHashMap<InformationKey, String>() {
			@Override
			public String defaultValue(InformationKey key) {
				return "";
			}
		};
		for (String[] info : informations)
			values.put(new InformationKey(GildeType.valueOf(info[0]), info[1]), info[2]);

		name = values.get(new InformationKey(GildeType.ALL, "name"));
		shortName = values.get(new InformationKey(GildeType.ALL, "shortName"));
		ownerId = Integer.parseInt(values.get(new InformationKey(GildeType.ALL, "ownerId")));

		for (GildeType type : GildeType.getPossibleValues())
			selections.put(type, new GildSection(this, type, values.get(new InformationKey(type, "active")).equalsIgnoreCase("true"), values));
	}

	public GildSection getSelection(GildeType type) {
		return selections.get(type);
	}

	public List<GildSection> getActiveSections() {
		ArrayList<GildSection> out = new ArrayList<>();
		for (GildSection s : selections.values())
			if (s.isActive())
				out.add(s);
		return out;
	}

	public GildeType[] getActiveSectionsArray() {
		ArrayList<GildeType> types = new ArrayList<>();
		for (GildSection sec : getActiveSections())
			types.add(sec.getType());
		return types.toArray(new GildeType[0]);
	}

	@Getter
	private static class PlayerInfo {
		private final int playerId;
		private HashMap<GildeType, String> ranks;

		public MemberInformation create() {
			GildeType[] types = new GildeType[ranks.size()];
			String[] groups = new String[ranks.size()];
			int index = 0;
			for (Entry<GildeType, String> e : ranks.entrySet()) {
				types[index] = e.getKey();
				groups[index++] = e.getValue();
			}
			return new MemberInformation(index, types, groups);
		}

		public PlayerInfo(int playerId) {
			this.playerId = playerId;
			this.ranks = new HashMap<>();
		}
	}

	public MemberInformation[] buildMemberInfo() {
		HashMap<Integer, PlayerInfo> info = new InitHashMap<Integer, PlayerInfo>() {
			@Override
			public PlayerInfo defaultValue(Integer key) {
				return new PlayerInfo(key);
			}
		};

		for (GildSection s : getActiveSections())
			for (Integer member : s.players)
				info.get(member).ranks.put(s.getType(), s.getPermission().getGroup(member).getName());
		MemberInformation[] infos = new MemberInformation[info.size()];
		int index = 0;
		for (PlayerInfo i : info.values())
			infos[index++] = i.create();
		return infos;
	}
}
                                                                                                                                                        
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse.MemberInformation;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class Gilde {
	public static final String TABLE_NAME = "GILDE_INFORMATION";

	@Getter
	private UUID uuid;
	@Getter
	private String name;
	@Getter
	private String shortName;
	@Getter
	private int ownerId;

	private HashMap<GildeType, GildSection> selections = new HashMap<>();
	private boolean exist;
	private boolean loaded;

	public Gilde(UUID uuid) {
		this.uuid = uuid;
		load();
	}

	public synchronized void reload() {
		loaded = false;
		load();
	}
	
	public void setShortName(String shortName) {
		if (!exist)
			return;
		this.shortName = shortName;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`=" + shortName + " WHERE `uuid`='" + uuid + "' AND `section`='" + GildeType.ALL.toString() + "' AND `key`='shortName'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.SHORT_NAME));
	}

	public void setName(String name) {
		if (!exist)
			return;
		this.name = name;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`=" + name + " WHERE `uuid`='" + uuid + "' AND `section`='" + GildeType.ALL.toString() + "' AND `key`='name'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.NAME));
	}

	public void load() {
		if (loaded)
			return;
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `section`,`key`,`value` FROM " + TABLE_NAME + " WHERE `uuid`='" + uuid.toString() + "'", -1);
		if (informations.size() == 0) {
			exist = false;
			return;
		}
		HashMap<InformationKey, String> values = new InitHashMap<InformationKey, String>() {
			@Override
			public String defaultValue(InformationKey key) {
				return "";
			}
		};
		for (String[] info : informations)
			values.put(new InformationKey(GildeType.valueOf(info[0]), info[1]), info[2]);

		name = values.get(new InformationKey(GildeType.ALL, "name"));
		shortName = values.get(new InformationKey(GildeType.ALL, "shortName"));
		ownerId = Integer.parseInt(values.get(new InformationKey(GildeType.ALL, "ownerId")));

		for (GildeType type : GildeType.getPossibleValues())
			selections.put(type, new GildSection(this, type, values.get(new InformationKey(type, "active")).equalsIgnoreCase("true"), values));
	}

	public GildSection getSelection(GildeType type) {
		return selections.get(type);
	}

	public List<GildSection> getActiveSections() {
		ArrayList<GildSection> out = new ArrayList<>();
		for (GildSection s : selections.values())
			if (s.isActive())
				out.add(s);
		return out;
	}

	public GildeType[] getActiveSectionsArray() {
		ArrayList<GildeType> types = new ArrayList<>();
		for (GildSection sec : getActiveSections())
			types.add(sec.getType());
		return types.toArray(new GildeType[0]);
	}

	@Getter
	private static class PlayerInfo {
		private final int playerId;
		private HashMap<GildeType, String> ranks;

		public MemberInformation create() {
			GildeType[] types = new GildeType[ranks.size()];
			String[] groups = new String[ranks.size()];
			int index = 0;
			for (Entry<GildeType, String> e : ranks.entrySet()) {
				types[index] = e.getKey();
				groups[index++] = e.getValue();
			}
			return new MemberInformation(index, types, groups);
		}

		public PlayerInfo(int playerId) {
			this.playerId = playerId;
			this.ranks = new HashMap<>();
		}
	}

	public MemberInformation[] buildMemberInfo() {
		HashMap<Integer, PlayerInfo> info = new InitHashMap<Integer, PlayerInfo>() {
			@Override
			public PlayerInfo defaultValue(Integer key) {
				return new PlayerInfo(key);
			}
		};

		for (GildSection s : getActiveSections())
			for (Integer member : s.players)
				info.get(member).ranks.put(s.getType(), s.getPermission().getGroup(member).getName());
		MemberInformation[] infos = new MemberInformation[info.size()];
		int index = 0;
		for (PlayerInfo i : info.values())
			infos[index++] = i.create();
		return infos;
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse.MemberInformation;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class Gilde {
	public static final String TABLE_NAME = "GILDE_INFORMATION";

	@Getter
	private UUID uuid;
	@Getter
	private String name;
	@Getter
	private String shortName;
	@Getter
	private int ownerId;

	private HashMap<GildeType, GildSection> selections;
	private boolean exist;
	private boolean loaded;

	public Gilde(UUID uuid) {
		this.uuid = uuid;
		load();
	}

	public synchronized void reload() {
		loaded = false;
		load();
	}

	public void setShortName(String shortName) {
		if (!exist)
			return;
		this.shortName = shortName;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`=" + shortName + " WHERE `uuid`='" + uuid + "' AND `section`='" + GildeType.ALL.toString() + "' AND `key`='shortName'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.SHORT_NAME));
	}

	public void setName(String name) {
		if (!exist)
			return;
		this.name = name;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`=" + name + " WHERE `uuid`='" + uuid + "' AND `section`='" + GildeType.ALL.toString() + "' AND `key`='shortName'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.NAME));
	}

	public void load() {
		if (loaded)
			return;
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `section`,`key`,`value` FROM " + TABLE_NAME + " WHERE `uuid`='" + uuid.toString() + "'", -1);
		if (informations.size() == 0) {
			exist = false;
			return;
		}
		HashMap<InformationKey, String> values = new InitHashMap<InformationKey, String>() {
			@Override
			public String defaultValue(InformationKey key) {
				return "";
			}
		};
		for (String[] info : informations)
			values.put(new InformationKey(GildeType.valueOf(info[0]), info[1]), info[2]);

		name = values.get(new InformationKey(GildeType.ALL, "name"));
		shortName = values.get(new InformationKey(GildeType.ALL, "shortName"));
		ownerId = Integer.parseInt(values.get(new InformationKey(GildeType.ALL, "ownerId")));

		for (GildeType type : GildeType.getPossibleValues())
			selections.put(type, new GildSection(this, type, values.get(new InformationKey(type, "active")).equalsIgnoreCase("true"), values));
	}

	public GildSection getSelection(GildeType type) {
		return selections.get(type);
	}

	public List<GildSection> getActiveSections() {
		ArrayList<GildSection> out = new ArrayList<>();
		for (GildSection s : selections.values())
			if (s.isActive())
				out.add(s);
		return out;
	}

	public GildeType[] getActiveSectionsArray() {
		ArrayList<GildeType> types = new ArrayList<>();
		for (GildSection sec : getActiveSections())
			types.add(sec.getType());
		return types.toArray(new GildeType[0]);
	}

	@Getter
	private static class PlayerInfo {
		private final int playerId;
		private HashMap<GildeType, String> ranks;

		public MemberInformation create() {
			GildeType[] types = new GildeType[ranks.size()];
			String[] groups = new String[ranks.size()];
			int index = 0;
			for (Entry<GildeType, String> e : ranks.entrySet()) {
				types[index] = e.getKey();
				groups[index++] = e.getValue();
			}
			return new MemberInformation(index, types, groups);
		}

		public PlayerInfo(int playerId) {
			this.playerId = playerId;
			this.ranks = new HashMap<>();
		}
	}

	public MemberInformation[] buildMemberInfo() {
		HashMap<Integer, PlayerInfo> info = new InitHashMap<Integer, PlayerInfo>() {
			@Override
			public PlayerInfo defaultValue(Integer key) {
				return new PlayerInfo(key);
			}
		};

		for (GildSection s : getActiveSections())
			for (Integer member : s.players)
				info.get(member).ranks.put(s.getType(), s.getPermission().getGroup(member).getName());
		MemberInformation[] infos = new MemberInformation[info.size()];
		int index = 0;
		for (PlayerInfo i : info.values())
			infos[index++] = i.create();
		return infos;
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse.MemberInformation;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import lombok.Getter;

public class Gilde {
	public static final String TABLE_NAME = "GILDE_INFORMATION";
	
	@Getter
	private UUID uuid;
	@Getter
	private String name;
	@Getter
	private String shortName;
	@Getter
	private int ownerId;
	
	private HashMap<GildeType, GildSection> selections;
	private boolean exist;
	private boolean loaded;
	
	public Gilde(UUID uuid) {
		this.uuid = uuid;
		load();
	}
	
	public synchronized void reload(){
		loaded = false;
		load();
	}
	
	public void setShortName(String shortName) {
		if(!exist)
			return;
		this.shortName = shortName;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`="+shortName+" WHERE `uuid`='"+uuid+"' AND`section`='"+GildeType.ALL.toString()+"' AND `key`='shortName'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.SHORT_NAME));
	}
	public void setName(String name) {
		if(!exist)
			return;
		this.name = name;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`="+name+" WHERE `uuid`='"+uuid+"' AND`section`='"+GildeType.ALL.toString()+"' AND `key`='shortName'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.NAME));
	}
	
	public void load(){
		if(loaded)
			return;
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `section`,`key`,`value` FROM "+TABLE_NAME+" WHERE `uuid`='"+uuid.toString()+"'", -1);
		if(informations.size() == 0){
			exist = false;
			return;
		}
		HashMap<InformationKey, String> values = new InitHashMap<InformationKey, String>() {
			@Override
			public String defaultValue(InformationKey key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(new InformationKey(GildeType.valueOf(info[0]), info[1]), info[2]);
		
		name = values.get(new InformationKey(GildeType.ALL, "name"));
		shortName = values.get(new InformationKey(GildeType.ALL, "shortName"));
		ownerId = Integer.parseInt(values.get(new InformationKey(GildeType.ALL, "ownerId")));
		
		for(GildeType type : GildeType.getPossibleValues())
			selections.put(type, new GildSection(this, type, values.get(new InformationKey(type, "active")).equalsIgnoreCase("true"),values));
	}
	
	public GildSection getSelection(GildeType type){
		return selections.get(type);
	}
	
	public List<GildSection> getActiveSections(){
		ArrayList<GildSection> out = new ArrayList<>();
		for(GildSection s : selections.values())
			if(s.isActive())
				out.add(s);
		return out;
	}

	public GildeType[] getActiveSectionsArray() {
		ArrayList<GildeType> types = new ArrayList<>();
		for(GildSection sec : getActiveSections())
			types.add(sec.getType());
		return types.toArray(new GildeType[0]);
	}
	
	public MemberInformation[] buildMemberInfo(){
		return null;
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse.MemberInformation;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import lombok.Getter;

public class Gilde {
	public static final String TABLE_NAME = "GILDE_INFORMATION";
	
	@Getter
	private UUID uuid;
	@Getter
	private String name;
	@Getter
	private String shortName;
	@Getter
	private int ownerId;
	
	private HashMap<GildeType, GildSection> selections;
	private boolean exist;
	private boolean loaded;
	
	public Gilde(UUID uuid) {
		this.uuid = uuid;
		load();
	}
	
	public synchronized void reload(){
		loaded = false;
		load();
	}
	
	public void setShortName(String shortName) {
		if(!exist)
			return;
		this.shortName = shortName;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`="+shortName+" WHERE `uuid`='"+uuid+"' AND`section`='"+GildeType.ALL.toString()+"' AND `key`='shortName'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.SHORT_NAME));
	}
	public void setName(String name) {
		if(!exist)
			return;
		this.name = name;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`="+name+" WHERE `uuid`='"+uuid+"' AND`section`='"+GildeType.ALL.toString()+"' AND `key`='shortName'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.NAME));
	}
	
	public void load(){
		if(loaded)
			return;
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `section`,`key`,`value` FROM "+TABLE_NAME+" WHERE `uuid`='"+uuid.toString()+"'", -1);
		if(informations.size() == 0){
			exist = false;
			return;
		}
		HashMap<InformationKey, String> values = new InitHashMap<InformationKey, String>() {
			@Override
			public String defaultValue(InformationKey key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(new InformationKey(GildeType.valueOf(info[0]), info[1]), info[2]);
		
		name = values.get(new InformationKey(GildeType.ALL, "name"));
		shortName = values.get(new InformationKey(GildeType.ALL, "shortName"));
		ownerId = Integer.parseInt(values.get(new InformationKey(GildeType.ALL, "ownerId")));
		
		for(GildeType type : GildeType.getPossibleValues())
			selections.put(type, new GildSection(this, type, values.get(new InformationKey(type, "active")).equalsIgnoreCase("true"),values));
	}
	
	public GildSection getSelection(GildeType type){
		return selections.get(type);
	}
	
	public List<GildSection> getActiveSections(){
		ArrayList<GildSection> out = new ArrayList<>();
		for(GildSection s : selections.values())
			if(s.isActive())
				out.add(s);
		return out;
	}

	public GildeType[] getActiveSectionsArray() {
		ArrayList<GildeType> types = new ArrayList<>();
		for(GildSection sec : getActiveSections())
			types.add(sec.getType());
		return types.toArray(new GildeType[0]);
	}
	
	private static class PlayerInfo {
		private int playerId;
		private HashMap<GildeType, String> ranks;
		
		public MemberInformation create(){
			GildeType[] types = new GildeType[ranks.size()];
			String[] groups = new String[ranks.size()];
			int index = 0;
			for(Entry<GildeType, String> e : ranks.entrySet()){
				types[index] = e.getKey();
				groups[index++] = e.getValue();
			}
			return new MemberInformation(index, types, groups);
		}
	}
	
	public MemberInformation[] buildMemberInfo(){
		return null;
	}
}
                                                                                                                                                                                                                                                                                                                                             
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse.MemberInformation;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class Gilde {
	public static final String TABLE_NAME = "GILDE_INFORMATION";
	
	@Getter
	private UUID uuid;
	@Getter
	private String name;
	@Getter
	private String shortName;
	@Getter
	private int ownerId;
	
	private HashMap<GildeType, GildSection> selections;
	private boolean exist;
	private boolean loaded;
	
	public Gilde(UUID uuid) {
		this.uuid = uuid;
		load();
	}
	
	public synchronized void reload(){
		loaded = false;
		load();
	}
	
	public void setShortName(String shortName) {
		if(!exist)
			return;
		this.shortName = shortName;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`="+shortName+" WHERE `uuid`='"+uuid+"' AND`section`='"+GildeType.ALL.toString()+"' AND `key`='shortName'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.SHORT_NAME));
	}
	public void setName(String name) {
		if(!exist)
			return;
		this.name = name;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`="+name+" WHERE `uuid`='"+uuid+"' AND`section`='"+GildeType.ALL.toString()+"' AND `key`='shortName'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.NAME));
	}
	
	public void load(){
		if(loaded)
			return;
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `section`,`key`,`value` FROM "+TABLE_NAME+" WHERE `uuid`='"+uuid.toString()+"'", -1);
		if(informations.size() == 0){
			exist = false;
			return;
		}
		HashMap<InformationKey, String> values = new InitHashMap<InformationKey, String>() {
			@Override
			public String defaultValue(InformationKey key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(new InformationKey(GildeType.valueOf(info[0]), info[1]), info[2]);
		
		name = values.get(new InformationKey(GildeType.ALL, "name"));
		shortName = values.get(new InformationKey(GildeType.ALL, "shortName"));
		ownerId = Integer.parseInt(values.get(new InformationKey(GildeType.ALL, "ownerId")));
		
		for(GildeType type : GildeType.getPossibleValues())
			selections.put(type, new GildSection(this, type, values.get(new InformationKey(type, "active")).equalsIgnoreCase("true"),values));
	}
	
	public GildSection getSelection(GildeType type){
		return selections.get(type);
	}
	
	public List<GildSection> getActiveSections(){
		ArrayList<GildSection> out = new ArrayList<>();
		for(GildSection s : selections.values())
			if(s.isActive())
				out.add(s);
		return out;
	}

	public GildeType[] getActiveSectionsArray() {
		ArrayList<GildeType> types = new ArrayList<>();
		for(GildSection sec : getActiveSections())
			types.add(sec.getType());
		return types.toArray(new GildeType[0]);
	}
	
	@AllArgsConstructor
	@Getter
	private static class PlayerInfo {
		private int playerId;
		private HashMap<GildeType, String> ranks;
		
		public MemberInformation create(){
			GildeType[] types = new GildeType[ranks.size()];
			String[] groups = new String[ranks.size()];
			int index = 0;
			for(Entry<GildeType, String> e : ranks.entrySet()){
				types[index] = e.getKey();
				groups[index++] = e.getValue();
			}
			return new MemberInformation(index, types, groups);
		}
	}
	
	public MemberInformation[] buildMemberInfo(){
		return null;
	}
}
                                                                                                                                                                                                                                                                             
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse.MemberInformation;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class Gilde {
	public static final String TABLE_NAME = "GILDE_INFORMATION";
	
	@Getter
	private UUID uuid;
	@Getter
	private String name;
	@Getter
	private String shortName;
	@Getter
	private int ownerId;
	
	private HashMap<GildeType, GildSection> selections;
	private boolean exist;
	private boolean loaded;
	
	public Gilde(UUID uuid) {
		this.uuid = uuid;
		load();
	}
	
	public synchronized void reload(){
		loaded = false;
		load();
	}
	
	public void setShortName(String shortName) {
		if(!exist)
			return;
		this.shortName = shortName;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`="+shortName+" WHERE `uuid`='"+uuid+"' AND`section`='"+GildeType.ALL.toString()+"' AND `key`='shortName'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.SHORT_NAME));
	}
	public void setName(String name) {
		if(!exist)
			return;
		this.name = name;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`="+name+" WHERE `uuid`='"+uuid+"' AND`section`='"+GildeType.ALL.toString()+"' AND `key`='shortName'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.NAME));
	}
	
	public void load(){
		if(loaded)
			return;
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `section`,`key`,`value` FROM "+TABLE_NAME+" WHERE `uuid`='"+uuid.toString()+"'", -1);
		if(informations.size() == 0){
			exist = false;
			return;
		}
		HashMap<InformationKey, String> values = new InitHashMap<InformationKey, String>() {
			@Override
			public String defaultValue(InformationKey key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(new InformationKey(GildeType.valueOf(info[0]), info[1]), info[2]);
		
		name = values.get(new InformationKey(GildeType.ALL, "name"));
		shortName = values.get(new InformationKey(GildeType.ALL, "shortName"));
		ownerId = Integer.parseInt(values.get(new InformationKey(GildeType.ALL, "ownerId")));
		
		for(GildeType type : GildeType.getPossibleValues())
			selections.put(type, new GildSection(this, type, values.get(new InformationKey(type, "active")).equalsIgnoreCase("true"),values));
	}
	
	public GildSection getSelection(GildeType type){
		return selections.get(type);
	}
	
	public List<GildSection> getActiveSections(){
		ArrayList<GildSection> out = new ArrayList<>();
		for(GildSection s : selections.values())
			if(s.isActive())
				out.add(s);
		return out;
	}

	public GildeType[] getActiveSectionsArray() {
		ArrayList<GildeType> types = new ArrayList<>();
		for(GildSection sec : getActiveSections())
			types.add(sec.getType());
		return types.toArray(new GildeType[0]);
	}
	
	@Getter
	private static class PlayerInfo {
		private final int playerId;
		private HashMap<GildeType, String> ranks;
		
		public MemberInformation create(){
			GildeType[] types = new GildeType[ranks.size()];
			String[] groups = new String[ranks.size()];
			int index = 0;
			for(Entry<GildeType, String> e : ranks.entrySet()){
				types[index] = e.getKey();
				groups[index++] = e.getValue();
			}
			return new MemberInformation(index, types, groups);
		}
	}
	
	public MemberInformation[] buildMemberInfo(){
		return null;
	}
}
                                                                                                                                                                                                                                                                                            
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import lombok.Getter;

public class Gilde {
	public static final String TABLE_NAME = "GILDE_INFORMATION";
	
	@Getter
	private UUID uuid;
	@Getter
	private String name;
	@Getter
	private String shortName;
	@Getter
	private int ownerId;
	
	private HashMap<GildeType, GildSection> selections;
	private boolean exist;
	private boolean loaded;
	
	public Gilde(UUID uuid) {
		this.uuid = uuid;
		load();
	}
	
	public synchronized void reload(){
		loaded = false;
		load();
	}
	
	public void setShortName(String shortName) {
		if(!exist)
			return;
		this.shortName = shortName;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`="+shortName+" WHERE `uuid`='"+uuid+"' AND`section`='"+GildeType.ALL.toString()+"' AND `key`='shortName'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.SHORT_NAME));
	}
	public void setName(String name) {
		if(!exist)
			return;
		this.name = name;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`="+name+" WHERE `uuid`='"+uuid+"' AND`section`='"+GildeType.ALL.toString()+"' AND `key`='shortName'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.NAME));
	}
	
	public void load(){
		if(loaded)
			return;
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `section`,`key`,`value` FROM "+TABLE_NAME+" WHERE `uuid`='"+uuid.toString()+"'", -1);
		if(informations.size() == 0){
			exist = false;
			return;
		}
		HashMap<InformationKey, String> values = new InitHashMap<InformationKey, String>() {
			@Override
			public String defaultValue(InformationKey key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(new InformationKey(GildeType.valueOf(info[0]), info[1]), info[2]);
		
		name = values.get(new InformationKey(GildeType.ALL, "name"));
		shortName = values.get(new InformationKey(GildeType.ALL, "shortName"));
		ownerId = Integer.parseInt(values.get(new InformationKey(GildeType.ALL, "ownerId")));
		
		for(GildeType type : GildeType.getPossibleValues())
			selections.put(type, new GildSection(this, type, values.get(new InformationKey(type, "active")).equalsIgnoreCase("true"),values));
	}
	
	public GildSection getSelection(GildeType type){
		return selections.get(type);
	}
	
	public List<GildSection> getActiveSections(){
		ArrayList<GildSection> out = new ArrayList<>();
		for(GildSection s : selections.values())
			if(s.isActive())
				out.add(s);
		return out;
	}

	public GildeType[] getActiveSectionsArray() {
		ArrayList<GildeType> types = new ArrayList<>();
		for(GildSection sec : getActiveSections())
			types.add(sec.getType());
		return types.toArray(new GildeType[0]);
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import lombok.Getter;

public class Gilde {
	public static final String TABLE_NAME = "GILDE_INFORMATION";
	
	@Getter
	private UUID uuid;
	@Getter
	private String name;
	@Getter
	private String shortName;
	@Getter
	private int ownerId;
	
	private HashMap<GildeType, GildSection> selections;
	private boolean exist;
	private boolean loaded;
	
	public Gilde(UUID uuid) {
		this.uuid = uuid;
		load();
	}
	
	public synchronized void reload(){
		loaded = false;
		load();
	}
	
	public void setShortName(String shortName) {
		if(!exist)
			return;
		this.shortName = shortName;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`="+shortName+" WHERE `uuid`='"+uuid+"' AND`section`='"+GildeType.ALL.toString()+"' AND `key`='shortName'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.SHORT_NAME));
	}
	public void setName(String name) {
		if(!exist)
			return;
		this.name = name;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`="+name+" WHERE `uuid`='"+uuid+"' AND`section`='"+GildeType.ALL.toString()+"' AND `key`='shortName'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.NAME));
	}
	
	public void load(){
		if(loaded)
			return;
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `section`,`key`,`value` FROM "+TABLE_NAME+" WHERE `uuid`='"+uuid.toString()+"'", -1);
		if(informations.size() == 0){
			exist = false;
			return;
		}
		HashMap<InformationKey, String> values = new InitHashMap<InformationKey, String>() {
			@Override
			public String defaultValue(InformationKey key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(new InformationKey(GildeType.valueOf(info[0]), info[1]), info[2]);
		
		name = values.get(new InformationKey(GildeType.ALL, "name"));
		shortName = values.get(new InformationKey(GildeType.ALL, "shortName"));
		ownerId = Integer.parseInt(values.get(new InformationKey(GildeType.ALL, "ownerId")));
		
		for(GildeType type : GildeType.getPossibleValues())
			selections.put(type, new GildSection(this, type, values.get(new InformationKey(type, "active")).equalsIgnoreCase("true"),values));
	}
	
	public GildSection getSelection(GildeType type){
		return selections.get(type);
	}
	
	public List<GildSection> getActiveSections(){
		ArrayList<GildSection> out = new ArrayList<>();
		for(GildSection s : selections.values())
			if(s.isActive())
				out.add(s);
		return out;
	}
}
                                                                                                                                                                        
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildePropertiesUpdate;
import dev.wolveringer.events.gilde.GildePropertiesUpdate.Property;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import lombok.Getter;

public class Gilde {
	public static final String TABLE_NAME = "GILDE_INFORMATION";
	
	@Getter
	private UUID uuid;
	@Getter
	private String name;
	@Getter
	private String shortName;
	@Getter
	private int ownerId;
	
	private HashMap<GildeType, GildSection> selections;
	private boolean exist;
	private boolean loaded;
	
	public Gilde(UUID uuid) {
		this.uuid = uuid;
		load();
	}
	
	public synchronized void reload(){
		loaded = false;
		load();
	}
	
	public void setShortName(String shortName) {
		if(!exist)
			return;
		this.shortName = shortName;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`="+shortName+" WHERE `uuid`='"+uuid+"' AND`section`='"+GildeType.ALL.toString()+"' AND `key`='shortName'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.SHORT_NAME));
	}
	public void setName(String name) {
		if(!exist)
			return;
		this.name = name;
		MySQL.getInstance().command("UPDATE `GILDE_INFORMATION` SET `value`="+name+" WHERE `uuid`='"+uuid+"' AND`section`='"+GildeType.ALL.toString()+"' AND `key`='shortName'");
		EventHelper.callGildEvent(uuid, new GildePropertiesUpdate(uuid, GildeType.ALL, Property.NAME));
	}
	
	public void load(){
		if(loaded)
			return;
		ArrayList<String[]> informations = MySQL.getInstance().querySync("SELECT `section`,`key`,`value` FROM "+TABLE_NAME+" WHERE `uuid`='"+uuid.toString()+"'", -1);
		if(informations.size() == 0){
			exist = false;
			return;
		}
		HashMap<InformationKey, String> values = new InitHashMap<InformationKey, String>() {
			@Override
			public String defaultValue(InformationKey key) {
				return "";
			}
		};
		for(String[] info : informations)
			values.put(new InformationKey(GildeType.valueOf(info[0]), info[1]), info[2]);
		
		name = values.get(new InformationKey(GildeType.ALL, "name"));
		shortName = values.get(new InformationKey(GildeType.ALL, "shortName"));
		
		for(GildeType type : GildeType.getPossibleValues())
			selections.put(type, new GildSection(this, type, values.get(new InformationKey(type, "active")).equalsIgnoreCase("true"),values));
	}
	
	public GildSection getSelection(GildeType type){
		return selections.get(type);
	}
	
	public List<GildSection> getActiveSections(){
		ArrayList<GildSection> out = new ArrayList<>();
		for(GildSection s : selections.values())
			if(s.isActive())
				out.add(s);
		return out;
	}
}
                                                                                                                                                                                                                                                                
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import dev.wolveringer.client.ClientWrapper;
import dev.wolveringer.client.PacketHandleErrorException;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildInformationResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse.MemberInformation;
import dev.wolveringer.gilde.GildeType;
import lombok.Getter;

public class Gilde {
	@Getter
	private ClientWrapper connection;
	
	@Getter
	private UUID uuid;
	@Getter
	private String name;
	@Getter
	private String shortName;
	
	private HashMap<GildeType, GildSection> selections;
	private boolean exist;
	private boolean loaded;
	
	public Gilde(ClientWrapper connection,UUID uuid) {
		this.uuid = uuid;
		this.connection = connection;
	}
	
	public synchronized void reload(){
		loaded = false;
		load();
	}
	
	public synchronized void reloadNameAndShortname(){
		PacketGildInformationResponse infos = connection.getGildeInformations(uuid).getSync();
		name = infos.getName();
		shortName = infos.getShortName();
	}
	
	public void load(){
		if(loaded)
			return;
		try{
			PacketGildInformationResponse infos = connection.getGildeInformations(uuid).getSync();
			name = infos.getName();
			shortName = infos.getShortName();
			for(GildeType t : infos.getActiveSections())
				selections.put(t, new GildSection(this, t, true));
			for(GildeType t : GildeType.values())
				if(t != GildeType.ALL)
					selections.putIfAbsent(t, new GildSection(this, t, false));
			PacketGildMemberResponse member = connection.getGildeMembers(uuid).getSync();
			for(MemberInformation i : member.getMember()){
				for(int j = 0;j<i.getMember().length;j++) {
					selections.get(i.getMember()[j]).players.add(i.getPlayerId());
					selections.get(i.getMember()[j]).getPermission().players.put(i.getPlayerId(), i.getGroups()[j]);
				}
			}
			exist = true;
			loaded = true;
		}catch(PacketHandleErrorException e){
			if(e.getErrors()[0].getId() == -2){
				loaded = true;
				exist = false;
			}
			else
				e.printStackTrace();
		}
			
	}
	
	public GildSection getSelection(GildeType type){
		return selections.get(type);
	}
	
	public List<GildSection> getActiveSections(){
		ArrayList<GildSection> out = new ArrayList<>();
		for(GildSection s : selections.values())
			if(s.isActive())
				out.add(s);
		return out;
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import dev.wolveringer.client.ClientWrapper;
import dev.wolveringer.client.PacketHandleErrorException;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildInformationResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse.MemberInformation;
import dev.wolveringer.gilde.GildeType;
import lombok.Getter;

public class Gilde {
	@Getter
	private UUID uuid;
	@Getter
	private String name;
	@Getter
	private String shortName;
	
	private HashMap<GildeType, GildSection> selections;
	private boolean exist;
	private boolean loaded;
	
	public Gilde(ClientWrapper connection,UUID uuid) {
		this.uuid = uuid;
		this.connection = connection;
	}
	
	public synchronized void reload(){
		loaded = false;
		load();
	}
	
	public synchronized void reloadNameAndShortname(){
		PacketGildInformationResponse infos = connection.getGildeInformations(uuid).getSync();
		name = infos.getName();
		shortName = infos.getShortName();
	}
	
	public void load(){
		if(loaded)
			return;
		try{
			PacketGildInformationResponse infos = connection.getGildeInformations(uuid).getSync();
			name = infos.getName();
			shortName = infos.getShortName();
			for(GildeType t : infos.getActiveSections())
				selections.put(t, new GildSection(this, t, true));
			for(GildeType t : GildeType.values())
				if(t != GildeType.ALL)
					selections.putIfAbsent(t, new GildSection(this, t, false));
			PacketGildMemberResponse member = connection.getGildeMembers(uuid).getSync();
			for(MemberInformation i : member.getMember()){
				for(int j = 0;j<i.getMember().length;j++) {
					selections.get(i.getMember()[j]).players.add(i.getPlayerId());
					selections.get(i.getMember()[j]).getPermission().players.put(i.getPlayerId(), i.getGroups()[j]);
				}
			}
			exist = true;
			loaded = true;
		}catch(PacketHandleErrorException e){
			if(e.getErrors()[0].getId() == -2){
				loaded = true;
				exist = false;
			}
			else
				e.printStackTrace();
		}
			
	}
	
	public GildSection getSelection(GildeType type){
		return selections.get(type);
	}
	
	public List<GildSection> getActiveSections(){
		ArrayList<GildSection> out = new ArrayList<>();
		for(GildSection s : selections.values())
			if(s.isActive())
				out.add(s);
		return out;
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import dev.wolveringer.client.ClientWrapper;
import dev.wolveringer.client.PacketHandleErrorException;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildInformationResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse.MemberInformation;
import dev.wolveringer.gilde.GildeType;
import lombok.Getter;

public class Gilde {
	@Getter
	private UUID uuid;
	@Getter
	private String name;
	@Getter
	private String shortName;
	
	private HashMap<GildeType, GildSection> selections;
	private boolean exist;
	private boolean loaded;
	
	public Gilde(UUID uuid) {
		this.uuid = uuid;
	}
	
	public synchronized void reload(){
		loaded = false;
		load();
	}
	
	public synchronized void reloadNameAndShortname(){
		PacketGildInformationResponse infos = connection.getGildeInformations(uuid).getSync();
		name = infos.getName();
		shortName = infos.getShortName();
	}
	
	public void load(){
		if(loaded)
			return;
		try{
			PacketGildInformationResponse infos = connection.getGildeInformations(uuid).getSync();
			name = infos.getName();
			shortName = infos.getShortName();
			for(GildeType t : infos.getActiveSections())
				selections.put(t, new GildSection(this, t, true));
			for(GildeType t : GildeType.values())
				if(t != GildeType.ALL)
					selections.putIfAbsent(t, new GildSection(this, t, false));
			PacketGildMemberResponse member = connection.getGildeMembers(uuid).getSync();
			for(MemberInformation i : member.getMember()){
				for(int j = 0;j<i.getMember().length;j++) {
					selections.get(i.getMember()[j]).players.add(i.getPlayerId());
					selections.get(i.getMember()[j]).getPermission().players.put(i.getPlayerId(), i.getGroups()[j]);
				}
			}
			exist = true;
			loaded = true;
		}catch(PacketHandleErrorException e){
			if(e.getErrors()[0].getId() == -2){
				loaded = true;
				exist = false;
			}
			else
				e.printStackTrace();
		}
			
	}
	
	public GildSection getSelection(GildeType type){
		return selections.get(type);
	}
	
	public List<GildSection> getActiveSections(){
		ArrayList<GildSection> out = new ArrayList<>();
		for(GildSection s : selections.values())
			if(s.isActive())
				out.add(s);
		return out;
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildInformationResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse.MemberInformation;
import dev.wolveringer.gilde.GildeType;
import lombok.Getter;

public class Gilde {
	@Getter
	private UUID uuid;
	@Getter
	private String name;
	@Getter
	private String shortName;
	
	private HashMap<GildeType, GildSection> selections;
	private boolean exist;
	private boolean loaded;
	
	public Gilde(UUID uuid) {
		this.uuid = uuid;
	}
	
	public synchronized void reload(){
		loaded = false;
		load();
	}
	
	public synchronized void reloadNameAndShortname(){
		PacketGildInformationResponse infos = connection.getGildeInformations(uuid).getSync();
		name = infos.getName();
		shortName = infos.getShortName();
	}
	
	public void load(){
		if(loaded)
			return;
		try{
			PacketGildInformationResponse infos = connection.getGildeInformations(uuid).getSync();
			name = infos.getName();
			shortName = infos.getShortName();
			for(GildeType t : infos.getActiveSections())
				selections.put(t, new GildSection(this, t, true));
			for(GildeType t : GildeType.values())
				if(t != GildeType.ALL)
					selections.putIfAbsent(t, new GildSection(this, t, false));
			PacketGildMemberResponse member = connection.getGildeMembers(uuid).getSync();
			for(MemberInformation i : member.getMember()){
				for(int j = 0;j<i.getMember().length;j++) {
					selections.get(i.getMember()[j]).players.add(i.getPlayerId());
					selections.get(i.getMember()[j]).getPermission().players.put(i.getPlayerId(), i.getGroups()[j]);
				}
			}
			exist = true;
			loaded = true;
		}catch(PacketHandleErrorException e){
			if(e.getErrors()[0].getId() == -2){
				loaded = true;
				exist = false;
			}
			else
				e.printStackTrace();
		}
			
	}
	
	public GildSection getSelection(GildeType type){
		return selections.get(type);
	}
	
	public List<GildSection> getActiveSections(){
		ArrayList<GildSection> out = new ArrayList<>();
		for(GildSection s : selections.values())
			if(s.isActive())
				out.add(s);
		return out;
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
*//*
-----------------------------------------------
package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import dev.wolveringer.dataserver.protocoll.packets.PacketGildInformationResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse.MemberInformation;
import dev.wolveringer.gilde.GildeType;
import lombok.Getter;

public class Gilde {
	@Getter
	private UUID uuid;
	@Getter
	private String name;
	@Getter
	private String shortName;
	
	private HashMap<GildeType, GildSection> selections;
	private boolean exist;
	private boolean loaded;
	
	public Gilde(UUID uuid) {
		this.uuid = uuid;
	}
	
	public synchronized void reload(){
		loaded = false;
		load();
	}
	
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public void load(){
		if(loaded)
			return;
		try{
			PacketGildInformationResponse infos = connection.getGildeInformations(uuid).getSync();
			name = infos.getName();
			shortName = infos.getShortName();
			for(GildeType t : infos.getActiveSections())
				selections.put(t, new GildSection(this, t, true));
			for(GildeType t : GildeType.values())
				if(t != GildeType.ALL)
					selections.putIfAbsent(t, new GildSection(this, t, false));
			PacketGildMemberResponse member = connection.getGildeMembers(uuid).getSync();
			for(MemberInformation i : member.getMember()){
				for(int j = 0;j<i.getMember().length;j++) {
					selections.get(i.getMember()[j]).players.add(i.getPlayerId());
					selections.get(i.getMember()[j]).getPermission().players.put(i.getPlayerId(), i.getGroups()[j]);
				}
			}
			exist = true;
			loaded = true;
		}catch(PacketHandleErrorException e){
			if(e.getErrors()[0].getId() == -2){
				loaded = true;
				exist = false;
			}
			else
				e.printStackTrace();
		}
			
	}
	
	public GildSection getSelection(GildeType type){
		return selections.get(type);
	}
	
	public List<GildSection> getActiveSections(){
		ArrayList<GildSection> out = new ArrayList<>();
		for(GildSection s : selections.values())
			if(s.isActive())
				out.add(s);
		return out;
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  
*/