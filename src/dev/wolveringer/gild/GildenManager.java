package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import dev.wolveringer.arrays.CachedArrayList;
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

	private synchronized Gilde loadGilde(UUID gilde) {
		for (Gilde g : gilden)
			if (g.getUuid().equals(gilde))
				return g;
		Gilde g = new Gilde(gilde);
		g.load();
		gilden.add(g);
		return g;
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
}
