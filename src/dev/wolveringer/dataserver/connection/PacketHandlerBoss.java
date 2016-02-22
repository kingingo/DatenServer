package dev.wolveringer.dataserver.connection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import dev.wolveringer.connection.server.ServerThread;
import dev.wolveringer.dataserver.Main;
import dev.wolveringer.dataserver.ban.BanEntity;
import dev.wolveringer.dataserver.ban.BanManager;
import dev.wolveringer.dataserver.player.OnlinePlayer;
import dev.wolveringer.dataserver.player.OnlinePlayer.Setting;
import dev.wolveringer.dataserver.player.PlayerManager;
import dev.wolveringer.dataserver.protocoll.packets.Packet;
import dev.wolveringer.dataserver.protocoll.packets.PacketForward;
import dev.wolveringer.dataserver.protocoll.packets.PacketHandschakeInStart;
import dev.wolveringer.dataserver.protocoll.packets.PacketInBanPlayer;
import dev.wolveringer.dataserver.protocoll.packets.PacketInBanStatsRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketInChangePlayerSettings;
import dev.wolveringer.dataserver.protocoll.packets.PacketChatMessage;
import dev.wolveringer.dataserver.protocoll.packets.PacketInConnectionStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketInPlayerSettingsRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketInConnectionStatus.Status;
import dev.wolveringer.dataserver.protocoll.packets.PacketInGetServer;
import dev.wolveringer.dataserver.protocoll.packets.PacketInNameRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutPlayerSettings.SettingValue;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutUUIDResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketServerAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutUUIDResponse.UUIDKey;
import dev.wolveringer.dataserver.protocoll.packets.PacketServerAction.PlayerAction;
import dev.wolveringer.dataserver.uuid.UUIDManager;
import dev.wolveringer.dataserver.protocoll.packets.PacketInServerSwitch;
import dev.wolveringer.dataserver.protocoll.packets.PacketInStatsEdit;
import dev.wolveringer.dataserver.protocoll.packets.PacketInStatsRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketInUUIDRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutBanStats;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutHandschakeAccept;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutNameResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutPacketStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutPacketStatus.Error;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutPlayerServer;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutPlayerSettings;
import dev.wolveringer.dataserver.protocoll.packets.PacketChatMessage.Target;
import dev.wolveringer.dataserver.protocoll.packets.PacketDisconnect;

public class PacketHandlerBoss {
	private Client owner;
	private boolean handschakeComplete = false;

	public PacketHandlerBoss(Client owner) {
		this.owner = owner;
	}

	public void handle(Packet packet) {
		if (!handschakeComplete) {
			if (packet instanceof PacketHandschakeInStart) {
				if (!Arrays.equals(((PacketHandschakeInStart) packet).getPassword(), Main.Password)) {
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
			owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(-1, "Packet forward not implimented yet!")));
		} else if (packet instanceof PacketInServerSwitch) {
			OnlinePlayer player = PlayerManager.getPlayer(((PacketInServerSwitch) packet).getPlayer());
			if(player == null){
				owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(0, "Player not found")));
				return;
			}
			player.setOwner(owner);
			player.setServer(((PacketInServerSwitch) packet).getServer());
			System.out.println("Player switched (" + ((PacketInServerSwitch) packet).getPlayer() + ") -> " + ((PacketInServerSwitch) packet).getServer());
			owner.writePacket(new PacketOutPacketStatus(packet, null));
		} else if (packet instanceof PacketInStatsEdit) {
			OnlinePlayer player = PlayerManager.getPlayer(((PacketInStatsEdit) packet).getPlayer());
			if(player == null){
				owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(0, "Player not found")));
				return;
			}
			player.getStatsManager().applayChanges((PacketInStatsEdit) packet);
			System.out.println("Player stats change (" + ((PacketInStatsEdit) packet).getPlayer() + ")");
			owner.writePacket(new PacketOutPacketStatus(packet, null));
		}
		else if(packet instanceof PacketInStatsRequest){
			OnlinePlayer player = PlayerManager.getPlayer(((PacketInStatsRequest) packet).getPlayer());
			if(player == null){
				System.out.println(((PacketInStatsRequest) packet).getPlayer()+":"+PlayerManager.getPlayer());
				owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(0, "Player not found")));
				return;
			}
			owner.writePacket(player.getStatsManager().getStats(((PacketInStatsRequest) packet).getGame()));
		}
		else if(packet instanceof PacketInChangePlayerSettings){
			OnlinePlayer player = PlayerManager.getPlayer(((PacketInChangePlayerSettings) packet).getPlayer());
			if(player == null){
				owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(0, "Player not found")));
				return;
			}
			switch (((PacketInChangePlayerSettings) packet).getSetting()) {
			case PASSWORD:
				player.setPassword(((PacketInChangePlayerSettings) packet).getValue());
				break;
			case PREMIUM_LOGIN:
				player.setPremium(Boolean.valueOf(((PacketInChangePlayerSettings) packet).getValue()));
				break;
			default:
				break;
			}
			owner.writePacket(new PacketOutPacketStatus(packet, null));
		}
		else if(packet instanceof PacketInPlayerSettingsRequest){
			OnlinePlayer player = PlayerManager.getPlayer(((PacketInPlayerSettingsRequest) packet).getPlayer());
			if(player == null){
				owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(0, "Player not found")));
				return;
			}
			ArrayList<SettingValue> values = new ArrayList<>();
			for(Setting s : ((PacketInPlayerSettingsRequest) packet).getSettings())
				switch (s) {
				case PASSWORD:
					values.add(new SettingValue(s, player.getLoginPassword()));
					break;
				case PREMIUM_LOGIN:
					values.add(new SettingValue(s, player.isPremium()+""));
					break;
				case UUID:
					values.add(new SettingValue(s, player.getUuid().toString()));
					break;
				default:
					break;
				}
			owner.writePacket(new PacketOutPlayerSettings(player.getUuid(), values.toArray(new SettingValue[0])));
		}
		else if(packet instanceof PacketInConnectionStatus){
			if(((PacketInConnectionStatus)packet).getStatus() == Status.CONNECTED){
				System.out.println("Player connected (" + ((PacketInConnectionStatus) packet).getPlayer() + ")");
				PlayerManager.loadPlayer(((PacketInConnectionStatus) packet).getPlayer(), owner);
			}
			else
			{
				System.out.println("Player disconnected (" + ((PacketInConnectionStatus) packet).getPlayer() + ")");
				PlayerManager.getPlayer(((PacketInConnectionStatus) packet).getPlayer()).save();
				PlayerManager.unload(((PacketInConnectionStatus) packet).getPlayer());
			}
			owner.writePacket(new PacketOutPacketStatus(packet, null));
		}
		else if(packet instanceof PacketChatMessage){
			ArrayList<PacketOutPacketStatus.Error> errors = new ArrayList<>();
			loop:
			for(Target target : ((PacketChatMessage) packet).getTargets()){
				switch (target.getType()) {
				case BROTCAST:
					for(Client clients : ServerThread.getBungeecords())
						clients.writePacket(new PacketChatMessage(((PacketChatMessage) packet).getMessage(), new Target[]{target}));
					break loop;
				case PLAYER:
					OnlinePlayer player = PlayerManager.getPlayer(UUID.fromString(target.getTarget()));
					if(player == null)
						errors.add(new PacketOutPacketStatus.Error(0, "Player \""+target.getTarget()+"\" isnt online!"));
					else{
						Client client = player.getPlayerBungeecord();
						if(client != null)
							client.writePacket(new PacketChatMessage(((PacketChatMessage) packet).getMessage(), new Target[]{target}));
					}
				default:
					break;
				}
			}
			owner.writePacket(new PacketOutPacketStatus(packet, errors.toArray(new PacketOutPacketStatus.Error[0])));
		}
		else if(packet instanceof PacketDisconnect){
			owner.closePipeline();
			System.out.println("Client["+owner.getHost()+"] disconnected ("+((PacketDisconnect)packet).getReson()+")");
			return;
		}
		else if(packet instanceof PacketInUUIDRequest){
			UUIDKey[] out = new UUIDKey[((PacketInUUIDRequest) packet).getPlayers().length];
			int i = 0;
			for(String player : ((PacketInUUIDRequest) packet).getPlayers()){
				UUID uuid = UUIDManager.getUUID(player);
				UUIDManager.saveUpdatePremiumName(uuid, player);
				uuid = UUIDManager.getUUID(player); //UUID after update
				out[i] = new UUIDKey(player, uuid);
				i++;
			}
			owner.writePacket(new PacketOutUUIDResponse(out));
		}
		else if(packet instanceof PacketInGetServer){
			OnlinePlayer player = PlayerManager.getPlayer(((PacketInGetServer) packet).getPlayer());
			if(player == null){
				owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(0, "Player not found")));
				return;
			}
			owner.writePacket(new PacketOutPlayerServer(player.getUuid(), player.getServer()));
		}
		else if(packet instanceof PacketInBanStatsRequest){
			BanEntity e = BanManager.getManager().getEntity(((PacketInBanStatsRequest) packet).getName(), ((PacketInBanStatsRequest) packet).getIp(), ((PacketInBanStatsRequest) packet).getPlayer());
			owner.writePacket(new PacketOutBanStats(packet.getPacketUUID(), e));
		}
		else if(packet instanceof PacketInBanPlayer){
			PacketInBanPlayer p = (PacketInBanPlayer) packet;
			BanManager.getManager().banPlayer(p.getName(), p.getIp(), p.getUuid(), p.getBannerName(), p.getBannerUuid(), p.getBannerIp(), p.getLevel(), p.getEnd(), p.getReson());
			owner.writePacket(new PacketOutPacketStatus(packet, null));
		}
		else if(packet instanceof PacketInNameRequest){
			UUIDKey[] out = new UUIDKey[((PacketInNameRequest) packet).getUuids().length];
			int i = 0;
			for(UUID player : ((PacketInNameRequest) packet).getUuids()){
				out[i] = new UUIDKey(UUIDManager.getName(player), player);
				i++;
			}
			owner.writePacket(new PacketOutNameResponse(out));
		}
		else if(packet instanceof PacketServerAction){
			ArrayList<Error> errors = new ArrayList<>();
			for(PlayerAction action : ((PacketServerAction) packet).getActions()){
				OnlinePlayer player = PlayerManager.getPlayer(action.getPlayer());
				if(player == null){
					errors.add(new PacketOutPacketStatus.Error(0, "Player "+action.getPlayer()+" not found"));
					continue;
				}
				Client owner = player.getPlayerBungeecord();
				if(owner == null){
					errors.add(new PacketOutPacketStatus.Error(0, "Player "+action.getPlayer()+" ist online"));
					continue;
				}
				owner.writePacket(new PacketServerAction(new PlayerAction[]{action}));
			}
			owner.writePacket(new PacketOutPacketStatus(packet, errors.toArray(new Error[0])));
		}
	}

}
