package dev.wolveringer.dataserver.connection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import dev.wolveringer.configuration.ServerConfiguration;
import dev.wolveringer.connection.server.ServerThread;
import dev.wolveringer.dataserver.ban.BanEntity;
import dev.wolveringer.dataserver.ban.BanManager;
import dev.wolveringer.dataserver.gamestats.GameState;
import dev.wolveringer.dataserver.gamestats.GameType;
import dev.wolveringer.dataserver.gamestats.TopStatsManager;
import dev.wolveringer.dataserver.player.LanguageType;
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
import dev.wolveringer.dataserver.protocoll.packets.PacketInPlayerSettingsRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketInServerStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketInServerStatusRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketInGetServer;
import dev.wolveringer.dataserver.protocoll.packets.PacketInLobbyServerRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketInLobbyServerRequest.GameRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutPlayerSettings.SettingValue;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutServerStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutServerStatus.Action;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutTopTen;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutTopTen.RankInformation;
import dev.wolveringer.dataserver.protocoll.packets.PacketPing;
import dev.wolveringer.dataserver.protocoll.packets.PacketPlayerIdRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketPlayerIdResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketPong;
import dev.wolveringer.dataserver.protocoll.packets.PacketServerAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketServerMessage;
import dev.wolveringer.dataserver.protocoll.packets.PacketSettingUpdate;
import dev.wolveringer.dataserver.protocoll.packets.PacketSkinData;
import dev.wolveringer.dataserver.protocoll.packets.PacketSkinRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketSkinSet;
import dev.wolveringer.dataserver.protocoll.packets.PacketServerAction.PlayerAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketSkinData.SkinResponse;
import dev.wolveringer.dataserver.skin.SkinCash;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.language.LanguageFile;
import dev.wolveringer.language.LanguageManager;
import dev.wolveringer.serverbalancer.AcardeManager;
import dev.wolveringer.serverbalancer.AcardeManager.ServerType;
import dev.wolveringer.skin.Skin;
import dev.wolveringer.skin.SteveSkin;
import dev.wolveringer.dataserver.protocoll.packets.PacketInServerSwitch;
import dev.wolveringer.dataserver.protocoll.packets.PacketInStatsEdit;
import dev.wolveringer.dataserver.protocoll.packets.PacketInStatsRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketInTopTenRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketLanguageRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketLanguageResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutBanStats;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutHandschakeAccept;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutLobbyServer;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutLobbyServer.GameServers;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutLobbyServer.ServerKey;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutPacketStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutPacketStatus.Error;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutPlayerServer;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutPlayerSettings;
import dev.wolveringer.dataserver.protocoll.packets.PacketChatMessage.Target;
import dev.wolveringer.dataserver.protocoll.packets.PacketDisconnect;
import dev.wolveringer.dataserver.protocoll.packets.PacketEventCondition;
import dev.wolveringer.dataserver.protocoll.packets.PacketEventTypeSettings;

public class PacketHandlerBoss {
	private Client owner;
	private boolean handschakeComplete = false;

	public PacketHandlerBoss(Client owner) {
		this.owner = owner;
	}

	public void handle(Packet packet) {
		if (!handschakeComplete) {
			if (packet instanceof PacketHandschakeInStart) {
				if (!Arrays.equals(((PacketHandschakeInStart) packet).getPassword(), ServerConfiguration.getServerPassword().getBytes())) {
					owner.disconnect("Password incorrect [" + ((PacketHandschakeInStart) packet).getHost() + "|" + ((PacketHandschakeInStart) packet).getName() + "]");
					return;
				}
				if (!((PacketHandschakeInStart) packet).getProtocollVersion().equalsIgnoreCase(Packet.PROTOCOLL_VERSION)) {
					owner.disconnect("Protocollversion is not up to date!");
					System.out.println("A client try to connect with version-number: " + ((PacketHandschakeInStart) packet).getProtocollVersion() + " Server-version: " + Packet.PROTOCOLL_VERSION);
					return;
				}
				if (ServerThread.getServer(((PacketHandschakeInStart) packet).getName()) != null) {
					if (!ServerThread.getServer(((PacketHandschakeInStart) packet).getName()).isReachable(1000)) {
						if (ServerThread.getServer(((PacketHandschakeInStart) packet).getName()) != null)
							ServerThread.getServer(((PacketHandschakeInStart) packet).getName()).disconnect("Timeout (Logged in from other location)");
					} else {
						owner.disconnect("A server with this name is alredy connected!");
						System.out.println("Server " + ((PacketHandschakeInStart) packet).getName() + " try to connect twice!");
						return;
					}
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
			if (((PacketForward) packet).getTarget() == null && ((PacketForward) packet).getCtarget() == null) {
				owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(-1, "Both targets are null")));
				System.out.println("Forward failed packet to " + ((PacketForward) packet).getTarget() + ":" + ((PacketForward) packet).getCtarget());
				return;
			}
			if (((PacketForward) packet).getTarget() != null) {
				Client c = ServerThread.getServer(((PacketForward) packet).getTarget());
				if (c == null) {
					owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(-1, "Target " + ((PacketForward) packet).getTarget() + " not found!")));
					return;
				}
				c.writePacket(packet);
				owner.writePacket(new PacketOutPacketStatus(packet, null));
				return;
			}
			if (((PacketForward) packet).getCtarget() != null) {
				ArrayList<Client> ca = ServerThread.getServer(((PacketForward) packet).getCtarget());
				for (Client c : ca)
					c.writePacket(packet);
				owner.writePacket(new PacketOutPacketStatus(packet, null));
				return;
			}
			System.out.println("Forward failed packet to " + ((PacketForward) packet).getTarget() + ":" + ((PacketForward) packet).getCtarget());
			owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(-1, "Undefined error....")));

		} else if (packet instanceof PacketInServerSwitch) {
			OnlinePlayer player = PlayerManager.getPlayer(((PacketInServerSwitch) packet).getPlayer());
			if (player == null) {
				owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(0, "Player not found")));
				return;
			}
			String old = player.getServer();
			player.setServer(((PacketInServerSwitch) packet).getServer(), owner);

			EventHelper.callServerSwitchEvent(player, owner, old, player.getServer());
			System.out.println("Player switched (" + ((PacketInServerSwitch) packet).getPlayer() + ") -> " + ((PacketInServerSwitch) packet).getServer());
			owner.writePacket(new PacketOutPacketStatus(packet, null));
		} else if (packet instanceof PacketInStatsEdit) {
			OnlinePlayer player = PlayerManager.getPlayer(((PacketInStatsEdit) packet).getPlayer());
			if (player == null) {
				owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(0, "Player not found")));
				return;
			}
			player.getStatsManager().applayChanges((PacketInStatsEdit) packet);
			owner.writePacket(new PacketOutPacketStatus(packet, null));
		} else if (packet instanceof PacketInStatsRequest) {
			OnlinePlayer player = PlayerManager.getPlayer(((PacketInStatsRequest) packet).getPlayer());
			if (player == null) {
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
			case NAME:
				player.setName(((PacketInChangePlayerSettings) packet).getValue());
				break;
			case PASSWORD:
				player.setPassword(((PacketInChangePlayerSettings) packet).getValue());
				break;
			case PREMIUM_LOGIN:
				player.setPremium(Boolean.valueOf(((PacketInChangePlayerSettings) packet).getValue()));
				break;
			case UUID:
				player.setUUID(UUID.fromString(((PacketInChangePlayerSettings) packet).getValue()));
				break;
			case LANGUAGE:
				player.setLanguage(LanguageType.getLanguageFromName(((PacketInChangePlayerSettings) packet).getValue()));
				break;
			case CURRUNT_IP:
				player.setCurruntIp(((PacketInChangePlayerSettings) packet).getValue());
				break;
			default:
				break;
			}
			Client bungeecord = player.getPlayerBungeecord();
			if (bungeecord != null)
				bungeecord.writePacket(new PacketSettingUpdate(player.getUuid(), ((PacketInChangePlayerSettings) packet).getSetting(), ((PacketInChangePlayerSettings) packet).getValue()));
			Client server = ServerThread.getServer(player.getServer());
			if (server != null)
				server.writePacket(new PacketSettingUpdate(player.getUuid(), ((PacketInChangePlayerSettings) packet).getSetting(), ((PacketInChangePlayerSettings) packet).getValue()));
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
				case NAME:
					values.add(new SettingValue(s, player.getName()));
					break;
				case PASSWORD:
					values.add(new SettingValue(s, player.getLoginPassword()));
					break;
				case PREMIUM_LOGIN:
					values.add(new SettingValue(s, player.isPremiumPlayer() + ""));
					break;
				case UUID:
					values.add(new SettingValue(s, player.getUuid().toString()));
					break;
				case LANGUAGE:
					values.add(new SettingValue(s, player.getLanguage().getShortName()));
					break;
				case CURRUNT_IP:
					values.add(new SettingValue(s, player.getCurruntIp()));
					break;
				default:
					break;
				}
			owner.writePacket(new PacketOutPlayerSettings(player.getPlayerId(), values.toArray(new SettingValue[0])));
		} else if (packet instanceof PacketChatMessage) {
			ArrayList<PacketOutPacketStatus.Error> errors = new ArrayList<>();
			loop: for (Target target : ((PacketChatMessage) packet).getTargets()) {
				switch (target.getType()) {
				case BROTCAST:
					for (Client clients : ServerThread.getBungeecords())
						if (clients != owner)
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
		} else if (packet instanceof PacketInGetServer) {
			OnlinePlayer player = PlayerManager.getPlayer(((PacketInGetServer) packet).getPlayer());
			if (player == null) {
				owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(0, "Player not found")));
				return;
			}
			owner.writePacket(new PacketOutPlayerServer(player.getUuid(), player.getServer()));
		} else if (packet instanceof PacketInBanStatsRequest) {//TODO down
			BanEntity e = BanManager.getManager().getEntity(((PacketInBanStatsRequest) packet).getName(), ((PacketInBanStatsRequest) packet).getIp(), ((PacketInBanStatsRequest) packet).getPlayer());
			owner.writePacket(new PacketOutBanStats(packet.getPacketUUID(), e));
		} else if (packet instanceof PacketInBanPlayer) {//TODO
			PacketInBanPlayer p = (PacketInBanPlayer) packet;
			BanManager.getManager().banPlayer(p.getName(), p.getIp(), p.getUuid(), p.getBannerName(), p.getBannerUuid(), p.getBannerIp(), p.getLevel(), p.getEnd(), p.getReson());
			owner.writePacket(new PacketOutPacketStatus(packet, null));
		} else if (packet instanceof PacketServerAction) { //TODO
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
			owner.writePacket(new PacketOutPacketStatus(packet, null));
		} else if (packet instanceof PacketInServerStatusRequest) {
			switch (((PacketInServerStatusRequest) packet).getAction()) {
			case BUNGEECORD:
				for (Client client : ServerThread.getBungeecords())
					if (client.getName().equalsIgnoreCase(((PacketInServerStatusRequest) packet).getValue())) {
						List<String> player = client.getPlayers();
						owner.writePacket(new PacketOutServerStatus(Action.BUNGEECORD, null, ((PacketInServerStatusRequest) packet).getValue(), client.getStatus().getServerId(), client.getStatus().isVisiable(), client.getStatus().getState(), player.size(), client.getStatus().getMaxPlayers(), ((PacketInServerStatusRequest) packet).isPlayer() ? player : null));
						return;
					}
				break;
			case SERVER:
				List<String> player = PlayerManager.getPlayersFromServer(((PacketInServerStatusRequest) packet).getValue());
				Client client = ServerThread.getServer(((PacketInServerStatusRequest) packet).getValue());
				if (client == null) {
					owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(-1, "Server/Bungeecord not found")));
					return;
				}
				if (player != null) {
					owner.writePacket(new PacketOutServerStatus(Action.SERVER, null, ((PacketInServerStatusRequest) packet).getValue(), client.getStatus().getServerId(), client.getStatus().isVisiable(), client.getStatus().getState(), player.size(), client.getStatus().getMaxPlayers(), ((PacketInServerStatusRequest) packet).isPlayer() ? player : null));
					return;
				}
				break;
			case GENERAL:
				List<String> players = PlayerManager.getPlayersFromServer(null);
				if (players != null) {
					owner.writePacket(new PacketOutServerStatus(Action.GENERAL, null, ((PacketInServerStatusRequest) packet).getValue(), "network", true, GameState.NONE, players.size(), -1, ((PacketInServerStatusRequest) packet).isPlayer() ? players : null));
					return;
				}
				break;
			case GAMETYPE:
				ArrayList<Client> allClients = new ArrayList<>();
				for (GameType type : ((PacketInServerStatusRequest) packet).getGames())
					allClients.addAll(ServerThread.getServer(type));
				ArrayList<String> splayers = new ArrayList<>();
				int playercount = 0;
				int maxPlayerCount = 0;
				for (Client c : allClients) {
					if (((PacketInServerStatusRequest) packet).isPlayer())
						splayers.addAll(c.getPlayers());
					playercount += c.getStatus().getPlayers();
					maxPlayerCount += c.getStatus().getMaxPlayers();
				}
				owner.writePacket(new PacketOutServerStatus(Action.GAMETYPE, ((PacketInServerStatusRequest) packet).getGames(), ((PacketInServerStatusRequest) packet).getValue(), "GameType[]", true, GameState.NONE, playercount, maxPlayerCount, ((PacketInServerStatusRequest) packet).isPlayer() ? splayers : null));
				break;
			default:
				break;
			}
			owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(-1, "Server/Bungeecord not found")));
		} else if (packet instanceof PacketPing) {
			owner.writePacket(new PacketPong(System.currentTimeMillis()));
			owner.lastPing = System.currentTimeMillis() - ((PacketPing) packet).getTime();
			owner.lastPingTime = System.currentTimeMillis();
		} else if (packet instanceof PacketPong) {
			owner.lastPing = System.currentTimeMillis() - ((PacketPong) packet).getTime();
			owner.lastPingTime = System.currentTimeMillis();
		} else if (packet instanceof PacketServerMessage) {
			for (dev.wolveringer.dataserver.protocoll.packets.PacketServerMessage.Target t : ((PacketServerMessage) packet).getTargets()) {
				int limit = -1;
				int count = 0;
				if(t.getTarget() != null && t.getTarget().startsWith("targetlimit;"))
					limit = Integer.parseInt(t.getTarget().substring("targetlimit;".length()));
				if (t.getTargetType() != null) {
					System.out.println("Boardcast backet to "+t.getTargetType()+" with limit: "+t.getTarget()+"("+limit+")");
					ArrayList<Client> targets = ServerThread.getServer(t.getTargetType());
					for (Client tc : targets){
						if(count >= limit && limit != -1)
							continue;
						if (tc != owner){
							count++;
							tc.writePacket(packet);
						}
					}
				} else {
					Client client = ServerThread.getServer(t.getTarget());
					if (client == null) {
						owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(-1, "Target not found")));
						return;
					} else {
						client.writePacket(packet);
					}
				}
			}
			owner.writePacket(new PacketOutPacketStatus(packet, null));
		} else if (packet instanceof PacketForward) {
			if (((PacketForward) packet).getCtarget() != null) {
				ArrayList<Client> targets = ServerThread.getServer(((PacketForward) packet).getCtarget());
				for (Client tc : targets)
					if (tc != owner)
						tc.writePacket(packet);
			} else {
				Client client = ServerThread.getServer(((PacketForward) packet).getTarget());
				if (client == null) {
					owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(-1, "Target not found")));
				} else {
					client.writePacket(packet);
				}
			}
			owner.writePacket(new PacketOutPacketStatus(packet, null));
		} else if (packet instanceof PacketInLobbyServerRequest) {
			GameServers[] response = new GameServers[((PacketInLobbyServerRequest) packet).getRequest().length];
			HashMap<ServerType, ArrayList<Client>> tempServers = new HashMap<ServerType, ArrayList<Client>>(AcardeManager.getLastCalculated());
			HashMap<GameType, ArrayList<Client>> servers = new HashMap<GameType, ArrayList<Client>>() {
				@Override
				public ArrayList<Client> get(Object key) {
					ArrayList<Client> out = super.get(key);
					if (out == null) {
						super.put((GameType) key, out = new ArrayList<>());
					}
					return out;
				}
			};

			for (ServerType t : tempServers.keySet())
				servers.get(t.getType()).addAll(tempServers.get(t));

			for (int i = 0; i < response.length; i++) {
				GameRequest request = ((PacketInLobbyServerRequest) packet).getRequest()[i];
				ServerKey[] sresponse = new ServerKey[servers.containsKey(request.getGame()) ? Math.min(request.getMaxServers() == -1 ? Integer.MAX_VALUE : request.getMaxServers(), servers.get(request.getGame()).size()) : 0];
				for (int j = 0; j < sresponse.length; j++) {
					Client c = servers.get(request.getGame()).get(j);
					sresponse[j] = new ServerKey(c.getStatus().getServerId(), c.getStatus().getSubType(), c.getStatus().getPlayers(), c.getStatus().getMaxPlayers(), c.getStatus().getMots());
				}
				response[i] = new GameServers(request.getGame(), sresponse);
			}
			owner.writePacket(new PacketOutLobbyServer(response));
		} else if (packet instanceof PacketInTopTenRequest) {
			ArrayList<String[]> out = TopStatsManager.getManager().getTopTen(((PacketInTopTenRequest) packet).getGame(), ((PacketInTopTenRequest) packet).getCondition());
			RankInformation[] infos = new RankInformation[out.size()];
			for (int i = 0; i < infos.length; i++) {
				infos[i] = new RankInformation(out.get(i)[0], out.get(i)[1]);
			}
			owner.writePacket(new PacketOutTopTen(((PacketInTopTenRequest) packet).getGame(), ((PacketInTopTenRequest) packet).getCondition(), infos));
		} else if (packet instanceof PacketSkinRequest) {
			SkinResponse[] rout = new SkinResponse[((PacketSkinRequest) packet).getRequests().length];
			for (int i = 0; i < rout.length; i++) {
				Skin out = new SteveSkin();
				switch (((PacketSkinRequest) packet).getRequests()[i].getType()) {
				case FROM_PLAYER:
					OnlinePlayer player = PlayerManager.getPlayer(((PacketSkinRequest) packet).getRequests()[i].getPlayerId());
					if (player == null)
						break;
					out = player.getSkinManager().getSkin();
					break;
				case NAME:
					out = SkinCash.getSkin(((PacketSkinRequest) packet).getRequests()[i].getName());
					break;
				case UUID:
					out = SkinCash.getSkin(((PacketSkinRequest) packet).getRequests()[i].getUuid());
					break;
				default:
					break;
				}
				rout[i] = new SkinResponse(out);
			}
			owner.writePacket(new PacketSkinData(((PacketSkinRequest) packet).getRequestUUID(), rout));
		} else if (packet instanceof PacketSkinSet) {
			OnlinePlayer player = PlayerManager.getPlayer(((PacketSkinSet) packet).getPlayerId());
			switch (((PacketSkinSet) packet).getType()) {
			case NAME:
				player.getSkinManager().setSkin(((PacketSkinSet) packet).getSkinName());
				break;
			case UUID:
				player.getSkinManager().setSkin(((PacketSkinSet) packet).getSkinUUID());
				break;
			case PROPS:
				player.getSkinManager().setSkin(((PacketSkinSet) packet).getRawValue(), ((PacketSkinSet) packet).getSignature());
				break;
			case NONE:
				player.getSkinManager().disableSkin();
				break;
			default:
				break;
			}
			owner.writePacket(new PacketOutPacketStatus(packet, null));
		} else if (packet instanceof PacketEventCondition) {
			owner.getEventHander().handUpdate((PacketEventCondition) packet);
			owner.writePacket(new PacketOutPacketStatus(packet, null));
		} else if (packet instanceof PacketEventTypeSettings) {
			owner.getEventHander().handUpdate((PacketEventTypeSettings) packet);
			owner.writePacket(new PacketOutPacketStatus(packet, null));
		} else if (packet instanceof PacketLanguageRequest) {
			PacketLanguageRequest r = (PacketLanguageRequest) packet;
			LanguageFile file = LanguageManager.getLanguage(r.getType());
			if (file.getVersion() <= r.getVersion()) {
				owner.writePacket(new PacketLanguageResponse(r.getType(), file.getVersion(), null));
			} else {
				owner.writePacket(new PacketLanguageResponse(r.getType(), file.getVersion(), file.getFileAsString()));
			}
		} else if (packet instanceof PacketPlayerIdRequest) {
			int[] ids = null;
			if (((PacketPlayerIdRequest) packet).getNames() != null) {
				ids = new int[((PacketPlayerIdRequest) packet).getNames().length];
				for (int i = 0; i < ids.length; i++) {
					try {
						ids[i] = PlayerManager.getPlayer(((PacketPlayerIdRequest) packet).getNames()[i]).getPlayerId();
					} catch (Exception e) {
						e.printStackTrace();
						ids[i] = -2;
					}
				}
			} else if (((PacketPlayerIdRequest) packet).getUuids() != null) {
				ids = new int[((PacketPlayerIdRequest) packet).getUuids().length];
				for (int i = 0; i < ids.length; i++)
					try {
						ids[i] = PlayerManager.getPlayer(((PacketPlayerIdRequest) packet).getUuids()[i]).getPlayerId();
					} catch (Exception e) {
						e.printStackTrace();
						ids[i] = -2;
					}
			}
			if(ids == null)
				ids = new int[0];
			owner.writePacket(new PacketPlayerIdResponse(packet.getPacketUUID(), ids));
		}
	}

}
