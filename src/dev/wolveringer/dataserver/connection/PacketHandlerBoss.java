package dev.wolveringer.dataserver.connection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.mysql.fabric.Server;

import dev.wolveringer.connection.server.ServerThread;
import dev.wolveringer.dataserver.Main;
import dev.wolveringer.dataserver.ban.BanEntity;
import dev.wolveringer.dataserver.ban.BanManager;
import dev.wolveringer.dataserver.gamestats.GameType;
import dev.wolveringer.dataserver.gamestats.TopStatsManager;
import dev.wolveringer.dataserver.player.OnlinePlayer;
import dev.wolveringer.dataserver.player.PlayerManager;
import dev.wolveringer.dataserver.player.Setting;
import dev.wolveringer.dataserver.protocoll.packets.Packet;
import dev.wolveringer.dataserver.protocoll.packets.PacketForward;
import dev.wolveringer.dataserver.protocoll.packets.PacketHandschakeInStart;
import dev.wolveringer.dataserver.protocoll.packets.PacketInBanPlayer;
import dev.wolveringer.dataserver.protocoll.packets.PacketInBanStatsRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketInChangePlayerSettings;
import dev.wolveringer.dataserver.protocoll.packets.PacketChatMessage;
import dev.wolveringer.dataserver.protocoll.packets.PacketInConnectionStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketInPlayerSettingsRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketInServerStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketInServerStatus.GameState;
import dev.wolveringer.dataserver.protocoll.packets.PacketInServerStatusRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketInConnectionStatus.Status;
import dev.wolveringer.dataserver.protocoll.packets.PacketInGetServer;
import dev.wolveringer.dataserver.protocoll.packets.PacketInLobbyServerRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketInLobbyServerRequest.GameRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketInNameRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutPlayerSettings.SettingValue;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutServerStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutServerStatus.Action;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutTopTen;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutTopTen.RankInformation;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutUUIDResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketPingPong;
import dev.wolveringer.dataserver.protocoll.packets.PacketServerAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketServerMessage;
import dev.wolveringer.dataserver.protocoll.packets.PacketSettingUpdate;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutUUIDResponse.UUIDKey;
import dev.wolveringer.dataserver.protocoll.packets.PacketServerAction.PlayerAction;
import dev.wolveringer.dataserver.uuid.UUIDManager;
import dev.wolveringer.serverbalancer.AcardeManager;
import dev.wolveringer.dataserver.protocoll.packets.PacketInServerSwitch;
import dev.wolveringer.dataserver.protocoll.packets.PacketInStatsEdit;
import dev.wolveringer.dataserver.protocoll.packets.PacketInStatsRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketInTopTenRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketInUUIDRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutBanStats;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutHandschakeAccept;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutLobbyServer;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutLobbyServer.GameServers;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutLobbyServer.ServerKey;
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
				owner.name = ((PacketHandschakeInStart) packet).getName();
				owner.writePacket(new PacketOutHandschakeAccept());
				handschakeComplete = true;
				System.out.println("Client connected (" + owner.host + "|" + owner.type + ")");
			}
			return;
		}
		if (packet instanceof PacketForward) {
			System.out.println("Packet forward not implimented yet!");
			owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(-1, "Packet forward not implimented yet!")));
		} else if (packet instanceof PacketInServerSwitch) {
			OnlinePlayer player = PlayerManager.getPlayer(((PacketInServerSwitch) packet).getPlayer());
			if (player == null) {
				owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(0, "Player not found")));
				return;
			}
			player.setOwner(owner);
			player.setServer(((PacketInServerSwitch) packet).getServer());
			System.out.println("Player switched (" + ((PacketInServerSwitch) packet).getPlayer() + ") -> " + ((PacketInServerSwitch) packet).getServer());
			owner.writePacket(new PacketOutPacketStatus(packet, null));
		} else if (packet instanceof PacketInStatsEdit) {
			OnlinePlayer player = PlayerManager.getPlayer(((PacketInStatsEdit) packet).getPlayer());
			if (player == null) {
				owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(0, "Player not found")));
				return;
			}
			player.getStatsManager().applayChanges((PacketInStatsEdit) packet);
			System.out.println("Player stats change (" + ((PacketInStatsEdit) packet).getPlayer() + ")");
			owner.writePacket(new PacketOutPacketStatus(packet, null));
		} else if (packet instanceof PacketInStatsRequest) {
			OnlinePlayer player = PlayerManager.getPlayer(((PacketInStatsRequest) packet).getPlayer());
			if (player == null) {
				System.out.println(((PacketInStatsRequest) packet).getPlayer() + ":" + PlayerManager.getPlayer());
				owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(0, "Player not found")));
				return;
			}
			owner.writePacket(player.getStatsManager().getStats(((PacketInStatsRequest) packet).getGame()));
		} else if (packet instanceof PacketInChangePlayerSettings) {
			OnlinePlayer player = PlayerManager.getPlayer(((PacketInChangePlayerSettings) packet).getPlayer());
			if (player == null) {
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
			case LANGUAGE:
				player.setLanguage(LanguageType.get(((PacketInChangePlayerSettings) packet).getValue()));
				break;
			default:
				break;
			}
			Client bungeecord = player.getPlayerBungeecord();
			bungeecord.writePacket(new PacketSettingUpdate(player.getUuid(),((PacketInChangePlayerSettings) packet).getSetting(), ((PacketInChangePlayerSettings) packet).getValue()));
			Client server = ServerThread.getServer(player.getServer());
			if(server != null)
				server.writePacket(new PacketSettingUpdate(player.getUuid(),((PacketInChangePlayerSettings) packet).getSetting(), ((PacketInChangePlayerSettings) packet).getValue()));
			owner.writePacket(new PacketOutPacketStatus(packet, null));
		} else if (packet instanceof PacketInPlayerSettingsRequest) {
			OnlinePlayer player = PlayerManager.getPlayer(((PacketInPlayerSettingsRequest) packet).getPlayer());
			if (player == null) {
				owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(0, "Player not found")));
				return;
			}
			ArrayList<SettingValue> values = new ArrayList<>();
			for (Setting s : ((PacketInPlayerSettingsRequest) packet).getSettings())
				switch (s) {
				case PASSWORD:
					values.add(new SettingValue(s, player.getLoginPassword()));
					break;
				case PREMIUM_LOGIN:
					values.add(new SettingValue(s, player.isPremium() + ""));
					break;
				case UUID:
					values.add(new SettingValue(s, player.getUuid().toString()));
					break;
				case LANGUAGE:
					values.add(new SettingValue(s, player.getLang().getDef()));
					break;
				default:
					break;
				}
			owner.writePacket(new PacketOutPlayerSettings(player.getUuid(), values.toArray(new SettingValue[0])));
		} else if (packet instanceof PacketInConnectionStatus) {
			if (((PacketInConnectionStatus) packet).getStatus() == Status.CONNECTED) {
				System.out.println("Player connected (" + ((PacketInConnectionStatus) packet).getPlayer() + ")");
				PlayerManager.loadPlayer(((PacketInConnectionStatus) packet).getPlayer(), owner);
			} else {
				System.out.println("Player disconnected (" + ((PacketInConnectionStatus) packet).getPlayer() + ")");
				PlayerManager.getPlayer(((PacketInConnectionStatus) packet).getPlayer()).save();
				PlayerManager.unload(((PacketInConnectionStatus) packet).getPlayer());
			}
			owner.writePacket(new PacketOutPacketStatus(packet, null));
		} else if (packet instanceof PacketChatMessage) {
			ArrayList<PacketOutPacketStatus.Error> errors = new ArrayList<>();
			loop: for (Target target : ((PacketChatMessage) packet).getTargets()) {
				switch (target.getType()) {
				case BROTCAST:
					for (Client clients : ServerThread.getBungeecords())
						if(clients != owner)
						clients.writePacket(new PacketChatMessage(((PacketChatMessage) packet).getMessage(), new Target[] { target }));
					break loop;
				case PLAYER:
					OnlinePlayer player = PlayerManager.getPlayer(UUID.fromString(target.getTarget()));
					if (player == null || !player.isPlaying())
						errors.add(new PacketOutPacketStatus.Error(0, "Player \"" + target.getTarget() + "\" isnt online!"));
					else {
						Client client = player.getPlayerBungeecord();
						if (client != null)
							client.writePacket(new PacketChatMessage(((PacketChatMessage) packet).getMessage(), new Target[] { target }));
					}
				default:
					break;
				}
			}
			owner.writePacket(new PacketOutPacketStatus(packet, errors.toArray(new PacketOutPacketStatus.Error[0])));
		} else if (packet instanceof PacketDisconnect) {
			owner.closePipeline();
			System.out.println("Client[" + owner.getHost() + "] disconnected (" + ((PacketDisconnect) packet).getReson() + ")");
			return;
		} else if (packet instanceof PacketInUUIDRequest) {
			UUIDKey[] out = new UUIDKey[((PacketInUUIDRequest) packet).getPlayers().length];
			int i = 0;
			for (String player : ((PacketInUUIDRequest) packet).getPlayers()) {
				UUID uuid = UUIDManager.getUUID(player);
				UUIDManager.saveUpdatePremiumName(uuid, player);
				uuid = UUIDManager.getUUID(player); //UUID after update
				out[i] = new UUIDKey(player, uuid);
				i++;
			}
			owner.writePacket(new PacketOutUUIDResponse(out));
		} else if (packet instanceof PacketInGetServer) {
			OnlinePlayer player = PlayerManager.getPlayer(((PacketInGetServer) packet).getPlayer());
			if (player == null) {
				owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(0, "Player not found")));
				return;
			}
			owner.writePacket(new PacketOutPlayerServer(player.getUuid(), player.getServer()));
		} else if (packet instanceof PacketInBanStatsRequest) {
			BanEntity e = BanManager.getManager().getEntity(((PacketInBanStatsRequest) packet).getName(), ((PacketInBanStatsRequest) packet).getIp(), ((PacketInBanStatsRequest) packet).getPlayer());
			owner.writePacket(new PacketOutBanStats(packet.getPacketUUID(), e));
		} else if (packet instanceof PacketInBanPlayer) {
			PacketInBanPlayer p = (PacketInBanPlayer) packet;
			BanManager.getManager().banPlayer(p.getName(), p.getIp(), p.getUuid(), p.getBannerName(), p.getBannerUuid(), p.getBannerIp(), p.getLevel(), p.getEnd(), p.getReson());
			owner.writePacket(new PacketOutPacketStatus(packet, null));
		} else if (packet instanceof PacketInNameRequest) {
			UUIDKey[] out = new UUIDKey[((PacketInNameRequest) packet).getUuids().length];
			int i = 0;
			for (UUID player : ((PacketInNameRequest) packet).getUuids()) {
				out[i] = new UUIDKey(UUIDManager.getName(player), player);
				i++;
			}
			owner.writePacket(new PacketOutNameResponse(out));
		} else if (packet instanceof PacketServerAction) {
			ArrayList<Error> errors = new ArrayList<>();
			for (PlayerAction action : ((PacketServerAction) packet).getActions()) {
				OnlinePlayer player = PlayerManager.getPlayer(action.getPlayer());
				if (player == null) {
					errors.add(new PacketOutPacketStatus.Error(0, "Player " + action.getPlayer() + " not found"));
					continue;
				}
				Client owner = player.getPlayerBungeecord();
				if (owner == null) {
					errors.add(new PacketOutPacketStatus.Error(0, "Player " + action.getPlayer() + " ist online"));
					continue;
				}
				owner.writePacket(new PacketServerAction(new PlayerAction[] { action }));
			}
			owner.writePacket(new PacketOutPacketStatus(packet, errors.toArray(new Error[0])));
		} else if (packet instanceof PacketInServerStatus) {
			owner.getStatus().applayPacket((PacketInServerStatus) packet);
			System.out.println("Change status");
			owner.writePacket(new PacketOutPacketStatus(packet, null));
		} else if (packet instanceof PacketInServerStatusRequest) {
			switch (((PacketInServerStatusRequest) packet).getAction()) {
			case BUNGEECORD:
				for (Client client : ServerThread.getBungeecords())
					if (client.getName().equalsIgnoreCase(((PacketInServerStatusRequest) packet).getValue())) {
						List<String> player = client.getPlayers();
						owner.writePacket(new PacketOutServerStatus(Action.BUNGEECORD, ((PacketInServerStatusRequest) packet).getValue(),client.getStatus().getServerId(),client.getStatus().isVisiable(),client.getStatus().getState(), player.size(), client.getStatus().getMaxPlayers(), ((PacketInServerStatusRequest) packet).isPlayer() ? player : null));
						return;
					}
				break;
			case SERVER:
				List<String> player = PlayerManager.getPlayers(((PacketInServerStatusRequest) packet).getValue());
				Client client = ServerThread.getServer(((PacketInServerStatusRequest) packet).getValue());
				if(client == null){
					owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(-1, "Server/Bungeecord not found")));
					return;
				}
				if (player != null) {
					owner.writePacket(new PacketOutServerStatus(Action.SERVER, ((PacketInServerStatusRequest) packet).getValue(),client.getStatus().getServerId(),client.getStatus().isVisiable(),client.getStatus().getState(), player.size(), client.getStatus().getMaxPlayers(), ((PacketInServerStatusRequest) packet).isPlayer() ? player : null));
					return;
				}
				break;
			case GENERAL:
				List<String> players = PlayerManager.getPlayers(null);
				if (players != null) {
					owner.writePacket(new PacketOutServerStatus(Action.GENERAL, ((PacketInServerStatusRequest) packet).getValue(),"network",true,GameState.NONE, players.size(), -1, ((PacketInServerStatusRequest) packet).isPlayer() ? players : null));
					return;
				}
				break;
			default:
				break;
			}
			owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(-1, "Server/Bungeecord not found")));
		}
		else if(packet instanceof PacketPingPong){
			owner.writePacket(packet);
			owner.lastPing = System.currentTimeMillis()-((PacketPingPong)packet).getTime();
			owner.lastPingTime = System.currentTimeMillis();
		}
		else if(packet instanceof PacketServerMessage){
			for(dev.wolveringer.dataserver.protocoll.packets.PacketServerMessage.Target t : ((PacketServerMessage) packet).getTargets()){
				if(t.getTargetType() != null){
					ArrayList<Client> targets = ServerThread.getServer(t.getTargetType());
					for(Client tc : targets)
						if(tc != owner)
						tc.writePacket(packet);
				}
				else
				{
					Client client = ServerThread.getServer(t.getTarget());
					if(client == null){
						owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(-1, "Target not found")));
						return;
					}
					else
					{
						client.writePacket(packet);
					}
				}
			}
			owner.writePacket(new PacketOutPacketStatus(packet, null));
		}
		else if(packet instanceof PacketForward){
			if(((PacketForward)packet).getCtarget() != null){
				ArrayList<Client> targets = ServerThread.getServer(((PacketForward)packet).getCtarget());
				for(Client tc : targets)
					if(tc != owner)
						tc.writePacket(packet);
			}
			else {
				Client client = ServerThread.getServer(((PacketForward)packet).getTarget());
				if(client == null){
					owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(-1, "Target not found")));
				}
				else
				{
					client.writePacket(packet);
				}
			}
			owner.writePacket(new PacketOutPacketStatus(packet, null));
		}
		else if(packet instanceof PacketInLobbyServerRequest){
			GameServers[] response = new GameServers[((PacketInLobbyServerRequest) packet).getRequest().length];
			HashMap<GameType, ArrayList<Client>> servers = AcardeManager.getLastCalculated();
			for (int i = 0; i < response.length; i++){
				GameRequest request = ((PacketInLobbyServerRequest) packet).getRequest()[i];
				ServerKey[] sresponse = new ServerKey[Math.min(request.getMaxServers(), servers.get(request.getGame()).size())];
				for(int j = 0;j<sresponse.length;j++){
					Client c = servers.get(request.getGame()).get(j);
					sresponse[j] = new ServerKey(c.getStatus().getServerId(), c.getStatus().getPlayers(), c.getStatus().getMaxPlayers(), c.getStatus().getMots());
				}
				response[i] = new GameServers(request.getGame(), sresponse);
			}
			owner.writePacket(new PacketOutLobbyServer(response));
		}
		else if(packet instanceof PacketInTopTenRequest){
			ArrayList<String[]> out = TopStatsManager.getManager().getTopTen(((PacketInTopTenRequest) packet).getGame(), ((PacketInTopTenRequest) packet).getCondition());
			RankInformation[] infos = new RankInformation[out.size()];
			for (int i = 0; i < infos.length; i++) {
				infos[i] = new RankInformation(out.get(i)[0], out.get(i)[1]);
			}
			owner.writePacket(new PacketOutTopTen(((PacketInTopTenRequest) packet).getGame(), ((PacketInTopTenRequest) packet).getCondition(), infos));
		}
	}

}
