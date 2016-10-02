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
import dev.wolveringer.gilde.GildeVariables;
import dev.wolveringer.hashmaps.InitHashMap;
import dev.wolveringer.mysql.MySQL;
import lombok.Getter;

public class Gilde {
	public static class NOT_EXISTING_Gilde extends Gilde {

		public NOT_EXISTING_Gilde(UUID uuid) {
			super(uuid);
		}
		public NOT_EXISTING_Gilde(String name) {
			super(UUID.randomUUID());
		}
		@Override
		public boolean isExist() {
			return false;
		}
	}
	
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
	@Getter
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
		exist = true;
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

		for (GildSection s : getActiveSections()){
			for (Integer member : s.players){
					info.get(member).ranks.put(s.getType(), s.getPermission().getGroup(member).getName());
			}
			for (Integer member : s.requestedPlayers){
				info.get(member).ranks.put(s.getType(), GildeVariables.INVITE_GROUP);
			}
		}
		MemberInformation[] infos = new MemberInformation[info.size()];
		int index = 0;
		for (PlayerInfo i : info.values())
			infos[index++] = i.create();
		return infos;
	}
}