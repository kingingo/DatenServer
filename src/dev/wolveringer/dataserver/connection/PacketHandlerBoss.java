package dev.wolveringer.dataserver.connection;

import dev.wolveringer.dataserver.Main;
import dev.wolveringer.dataserver.player.OnlinePlayer;
import dev.wolveringer.dataserver.player.PlayerManager;
import dev.wolveringer.dataserver.protocoll.packets.Packet;
import dev.wolveringer.dataserver.protocoll.packets.PacketForward;
import dev.wolveringer.dataserver.protocoll.packets.PacketHandschakeInStart;
import dev.wolveringer.dataserver.protocoll.packets.PacketInChatMessage;
import dev.wolveringer.dataserver.protocoll.packets.PacketInConnectionStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketInConnectionStatus.Status;
import dev.wolveringer.dataserver.protocoll.packets.PacketInServerSwitch;
import dev.wolveringer.dataserver.protocoll.packets.PacketInStatsEdit;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutHandschakeAccept;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutPacketStatus;

public class PacketHandlerBoss {
	private Client owner;
	private boolean handschakeComplete = false;

	public PacketHandlerBoss(Client owner) {
		this.owner = owner;
	}

	public void handle(Packet packet) {
		if (!handschakeComplete) {
			if (packet instanceof PacketHandschakeInStart) {
				if (!((PacketHandschakeInStart) packet).getPassword().equals(Main.Password)) {
					owner.disconnect("Password incorrect");
					return;
				}
				owner.host = ((PacketHandschakeInStart) packet).getHost();
				owner.type = ((PacketHandschakeInStart) packet).getType();
				owner.writePacket(new PacketOutHandschakeAccept());
				System.out.println("Client connected ("+owner.host+"|"+owner.type+")");
				handschakeComplete = true;
			}
			return;
		}
		if (packet instanceof PacketForward) {
			System.out.println("Packet forward not implimented yet!");
		} else if (packet instanceof PacketInServerSwitch) {
			OnlinePlayer player = PlayerManager.getPlayer(((PacketInServerSwitch) packet).getPlayer());
			if(player == null){
				owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(0, "Player not found")));
				return;
			}
			player.setServer(((PacketInServerSwitch) packet).getServer());
			System.out.println("Player switched (" + ((PacketInServerSwitch) packet).getPlayer() + ") -> " + ((PacketInServerSwitch) packet).getServer());
		} else if (packet instanceof PacketInStatsEdit) {
			OnlinePlayer player = PlayerManager.getPlayer(((PacketInStatsEdit) packet).getPlayer());
			if(player == null){
				owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(0, "Player not found")));
				return;
			}
			player.getStatsManager().applayChanges((PacketInStatsEdit) packet);
			System.out.println("Player stats change (" + ((PacketInServerSwitch) packet).getPlayer() + ")");
		}
		else if(packet instanceof PacketInConnectionStatus){
			if(((PacketInConnectionStatus)packet).getStatus() == Status.CONNECTED){
				System.out.println("Player connected (" + ((PacketInConnectionStatus) packet).getPlayer() + ")");
				PlayerManager.loadPlayer(((PacketInConnectionStatus) packet).getPlayer(), owner);
			}
			else
			{
				System.out.println("Player disconnected (" + ((PacketInConnectionStatus) packet).getPlayer() + ")");
				PlayerManager.savePlayer(((PacketInConnectionStatus) packet).getPlayer());
			}
		}
		else if(packet instanceof PacketInChatMessage){
			
		}
	}

}
