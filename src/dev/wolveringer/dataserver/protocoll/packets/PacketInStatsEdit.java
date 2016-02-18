package dev.wolveringer.dataserver.protocoll.packets;

import java.util.UUID;

import dev.wolveringer.dataserver.gamestats.Game;
import dev.wolveringer.dataserver.gamestats.StatsKey;
import dev.wolveringer.dataserver.protocoll.DataBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PacketInStatsEdit extends Packet {
	@AllArgsConstructor
	@Getter
	public static class EditStats {
		private Game game;
		private Action action;
		private StatsKey key;
		private Object value;
	}

	public static enum Action {
		ADD, REMOVE, SET;
		private Action() {
		}
	}

	@Getter
	private UUID player;
	@Getter
	private EditStats[] changes;

	@Override
	public void read(DataBuffer buffer) {
		player = buffer.readUUID();
		changes = new EditStats[buffer.readByte()];

		for (int i = 0; i < changes.length; i++) {
			Game game = Game.values()[buffer.readByte()];
			Action action = Action.values()[buffer.readByte()];
			StatsKey key = StatsKey.values()[buffer.readableBytes()];
			Object value = null;
			int id = -1;
			switch (id = buffer.readByte()) { // Value Type
			case 0:
				value = buffer.readInt();
				break;
			case 1:
				value = buffer.readDouble();
				break;
			case 2:
				value = buffer.readString();
				break;
			default:
				System.out.println("Wron stats id: " + id);
				break;
			}
			changes[i] = new EditStats(game, action,key, value);
		}
	}
}
