package dev.wolveringer.dataserver.gamestats;

import java.util.HashMap;

import dev.wolveringer.dataserver.player.OnlinePlayer;
import dev.wolveringer.dataserver.protocoll.packets.PacketInStatsEdit;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutStats;

public class StatsManager {
	public static class Statistic {
		private static HashMap<Class<?>, Integer> types = new HashMap<>();

		static {
			types.put(int.class, 0);
			types.put(double.class, 1);
			types.put(String.class, 2);
		}

		private StatsKey stat;
		private Object output;

		public Statistic(StatsKey stat, Object output) {
			this.stat = stat;
			this.output = output;
		}

		public int asInt() {
			return (int) output;
		}

		public double asDouble() {
			return (double) output;
		}

		public String asString() {
			return (String) output;
		}

		public int getTypeId() {
			return types.get(stat.getType());
		}

		public StatsKey getStatsKey() {
			return stat;
		}

		public Object getValue() {
			return output;
		}

		protected void applayChange(PacketInStatsEdit.EditStats change) {
			if (change.getValue().getClass().equals(output.getClass()))
				throw new RuntimeException(change.getValue().getClass() + " cant be a " + output.getClass() + " statistic");
			switch (change.getAction()) {
			case ADD:
				switch (types.get(change.getValue().getClass())) {
				case 0:
					output = asInt()+(int)change.getValue();
					break;
				case 1:
					output = asDouble()+(double) change.getValue();
					break;
				case 2:
					throw new RuntimeException("String is not addable");
				default:
					throw new RuntimeException("No class found");
				}
				break;
			case REMOVE:
				switch (types.get(change.getValue().getClass())) {
				case 0:
					output = asInt()-(int)change.getValue();
					break;
				case 1:
					output = asDouble()-(double) change.getValue();
					break;
				case 2:
					throw new RuntimeException("String is not removeable");
				default:
					throw new RuntimeException("No class found");
				}
				break;
			case SET:
				switch (types.get(change.getValue().getClass())) {
				case 0:
					output = (int)change.getValue();
					break;
				case 1:
					output = (double) change.getValue();
					break;
				case 2:
					output = change.getValue();
					break;
				default:
					throw new RuntimeException("No class found");
				}
				break;
			default:
				throw new RuntimeException("Type not found");
			}
		}
	}

	private OnlinePlayer owner;
	private HashMap<Game, Statistic[]> stats = new HashMap<>();

	public PacketOutStats getStats(Game game) {
		if (!stats.containsKey(game))
			loadStats(game);
		return new PacketOutStats(owner.getUuid(), game, stats.get(game));
	}

	public void applayChanges(PacketInStatsEdit packet) {
		for (PacketInStatsEdit.EditStats stat : packet.getChanges()) {
			if (!stats.containsKey(stat.getGame()))
				loadStats(stat.getGame());
			for (Statistic s : stats.get(stat.getGame()))
				if (s.getStatsKey() == stat.getKey())
					s.applayChange(stat);
		}
	}

	private void loadStats(Game game) {
		System.out.println("Stats loading not implimented yet!");
	}
}
