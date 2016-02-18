package dev.wolveringer.dataserver.protocoll.packets;

import java.util.UUID;

import dev.wolveringer.dataserver.gamestats.Game;
import dev.wolveringer.dataserver.gamestats.StatsManager;
import dev.wolveringer.dataserver.protocoll.DataBuffer;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PacketOutStats extends Packet{
	private UUID player;
	private Game game;
	private StatsManager.Statistic[] stats;
	
	@Override
	public void write(DataBuffer buffer) {
		buffer.writeUUID(player);
		buffer.writeByte(game.ordinal());
		buffer.writeByte(stats.length);
		
		for(StatsManager.Statistic stat : stats){
			buffer.writeByte(stat.getStatsKey().ordinal());
			buffer.writeByte(stat.getTypeId());
			switch (stat.getTypeId()) {
			case 0:
				buffer.writeInt(stat.asInt());
				break;
			case 1:
				buffer.writeDouble(stat.asDouble());
				break;
			case 2:
				buffer.writeString(stat.asString());
				break;
			default:
				System.out.println("Wron stats id: "+stat.getTypeId());
				break;
			}
		}
	}
}
