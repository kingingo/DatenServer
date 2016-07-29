package dev.wolveringer.dataserver.connection;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import dev.wolveringer.booster.NetworkBooster;
import dev.wolveringer.configuration.ServerConfiguration;
import dev.wolveringer.connection.server.ServerThread;
import dev.wolveringer.dataserver.ban.BanEntity;
import dev.wolveringer.dataserver.ban.BanManager;
import dev.wolveringer.dataserver.gamestats.GameState;
import dev.wolveringer.dataserver.gamestats.GameType;
import dev.wolveringer.dataserver.gamestats.StatsKey;
import dev.wolveringer.dataserver.gamestats.TopStatsManager;
import dev.wolveringer.dataserver.player.LanguageType;
import dev.wolveringer.dataserver.player.OnlinePlayer;
import dev.wolveringer.dataserver.player.PlayerManager;
import dev.wolveringer.dataserver.player.Setting;
import dev.wolveringer.dataserver.protocoll.packets.Packet;
import dev.wolveringer.dataserver.protocoll.packets.PacketBoosterActive;
import dev.wolveringer.dataserver.protocoll.packets.PacketBoosterStatusRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketBoosterStatusResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketChatMessage;
import dev.wolveringer.dataserver.protocoll.packets.PacketChatMessage.Target;
import dev.wolveringer.dataserver.protocoll.packets.PacketDisconnect;
import dev.wolveringer.dataserver.protocoll.packets.PacketEventCondition;
import dev.wolveringer.dataserver.protocoll.packets.PacketEventTypeSettings;
import dev.wolveringer.dataserver.protocoll.packets.PacketForward;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildCostumDataAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildCostumDataResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildCreate;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildInformationRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildInformationResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemberResponse.MemberInformation;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildMemeberAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildPermissionEdit;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildPermissionRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildPermissionResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildSarch;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildSarchResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketGildUpdateSectionStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketHandshakeInStart;
import dev.wolveringer.dataserver.protocoll.packets.PacketInBanPlayer;
import dev.wolveringer.dataserver.protocoll.packets.PacketInBanStatsRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketInChangePlayerSettings;
import dev.wolveringer.dataserver.protocoll.packets.PacketInGetServer;
import dev.wolveringer.dataserver.protocoll.packets.PacketInLobbyServerRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketInLobbyServerRequest.GameRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketInPlayerSettingsRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketInServerStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketInServerStatusRequest;
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
import dev.wolveringer.dataserver.protocoll.packets.PacketOutPlayerSettings.SettingValue;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutServerStatus;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutServerStatus.Action;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutTopTen;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutTopTen.RankInformation;
import dev.wolveringer.dataserver.protocoll.packets.PacketPing;
import dev.wolveringer.dataserver.protocoll.packets.PacketPlayerIdRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketPlayerIdResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketPong;
import dev.wolveringer.dataserver.protocoll.packets.PacketReportEdit;
import dev.wolveringer.dataserver.protocoll.packets.PacketReportRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketReportResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketServerAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketServerAction.PlayerAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketServerMessage;
import dev.wolveringer.dataserver.protocoll.packets.PacketSettingUpdate;
import dev.wolveringer.dataserver.protocoll.packets.PacketSkinData;
import dev.wolveringer.dataserver.protocoll.packets.PacketSkinData.SkinResponse;
import dev.wolveringer.dataserver.protocoll.packets.PacketSkinRequest;
import dev.wolveringer.dataserver.protocoll.packets.PacketSkinSet;
import dev.wolveringer.dataserver.protocoll.packets.PacketTeamspeakAction;
import dev.wolveringer.dataserver.protocoll.packets.PacketTeamspeakRequestAction;
import dev.wolveringer.dataserver.skin.SkinCache;
import dev.wolveringer.doublecoins.BoosterManager;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.gild.GildPermissionGroup;
import dev.wolveringer.gild.Gilde;
import dev.wolveringer.gild.GildenManager;
import dev.wolveringer.gilde.GildeType;
import dev.wolveringer.language.LanguageFile;
import dev.wolveringer.language.LanguageManager;
import dev.wolveringer.report.ReportEntity;
import dev.wolveringer.report.ReportManager;
import dev.wolveringer.serverbalancer.ArcadeManager;
import dev.wolveringer.serverbalancer.ArcadeManager.ServerType;
import dev.wolveringer.skin.Skin;
import dev.wolveringer.skin.SteveSkin;
import dev.wolveringer.teamspeak.TeamspeakClient;

public class PacketHandlerBoss {
	private Client owner;
	private boolean handshakeComplete = false;

	public PacketHandlerBoss(Client owner) {
		
		"".equals("");
		this.owner = owner;
	}

	public void handle(Packet packet) {
		if (!handshakeComplete) {
			if (packet instanceof PacketHandshakeInStart) {
				if (!Arrays.equals(((PacketHandshakeInStart) packet).getPassword(), ServerConfiguration.getServerPassword().getBytes())) {
					owner.disconnect("Password incorrect [" + ((PacketHandshakeInStart) packet).getHost() + "|" + ((PacketHandshakeInStart) packet).getName() + "]");
					return;
				}
				if (!((PacketHandshakeInStart) packet).getProtocollVersion().equalsIgnoreCase(Packet.PROTOCOLL_VERSION) && false) {
					owner.disconnect("Protocollversion is not up to date!");
					//System.out.println("A client try to connect with version-number: " + ((PacketHandshakeInStart) packet).getProtocollVersion() + " Server-version: " + Packet.PROTOCOLL_VERSION);
					return;
				}
				if (ServerThread.getServer(((PacketHandshakeInStart) packet).getName()) != null) {
					if (!ServerThread.getServer(((PacketHandshakeInStart) packet).getName()).isReachable(1000)) {
						if (ServerThread.getServer(((PacketHandshakeInStart) packet).getName()) != null)
							ServerThread.getServer(((PacketHandshakeInStart) packet).getName()).disconnect("Timeout (Logged in from other location)");
					} else {
						owner.disconnect("A server with this name is already connected!");
						System.out.println("Server " + ((PacketHandshakeInStart) packet).getName() + " try to connect twice!");
						return;
					}
				}
				owner.host = ((PacketHandshakeInStart) packet).getHost();
				owner.type = ((PacketHandshakeInStart) packet).getType();
				owner.name = ((PacketHandshakeInStart) packet).getName();
				owner.writePacket(new PacketOutHandschakeAccept());
				handshakeComplete = true;
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
			System.out.println("Player switched "+PlayerManager.getPlayer(((PacketInServerSwitch) packet).getPlayer()).getName()+" (" + ((PacketInServerSwitch) packet).getPlayer() + ") -> " + ((PacketInServerSwitch) packet).getServer());
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
			case NICKNAME:
				player.setNickname(((PacketInChangePlayerSettings) packet).getValue());
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
				case NICKNAME:
					values.add(new SettingValue(s, player.getNickname()));
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
						//if (clients != owner)
							clients.writePacket(new PacketChatMessage(((PacketChatMessage) packet).getMessage(), new Target[] { target }));
					break loop;
				case PLAYER:
					OnlinePlayer player = PlayerManager.getPlayer(Integer.parseInt(target.getTarget()));
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
			owner.writePacket(new PacketOutPlayerServer(player.getPlayerId(), player.getServer()));
		} else if (packet instanceof PacketInBanStatsRequest) {//TODO down
			if(((PacketInBanStatsRequest) packet).getDeep() == 1){
				BanEntity e = BanManager.getManager().getEntity(((PacketInBanStatsRequest) packet).getName(), ((PacketInBanStatsRequest) packet).getIp(), ((PacketInBanStatsRequest) packet).getPlayer());
				if(e == null)
					owner.writePacket(new PacketOutBanStats(packet.getPacketUUID(), Arrays.asList())); //Faster but only for one entity
				else
					owner.writePacket(new PacketOutBanStats(packet.getPacketUUID(), Arrays.asList(e))); //Faster but only for one entity
				return;
			}
			ArrayList<BanEntity> entities = BanManager.getManager().getEntitys(((PacketInBanStatsRequest) packet).getName(), ((PacketInBanStatsRequest) packet).getIp(), ((PacketInBanStatsRequest) packet).getPlayer());
			owner.writePacket(new PacketOutBanStats(packet.getPacketUUID(), entities.subList(0, Math.min(((PacketInBanStatsRequest) packet).getDeep(), entities.size()))));
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
					errors.add(new PacketOutPacketStatus.Error(0, "Player " + action.getPlayer() + " is online"));
					continue;
				}
				owner.writePacket(new PacketServerAction(new PlayerAction[] { action }));
			}
			owner.writePacket(new PacketOutPacketStatus(packet, errors.toArray(new Error[0])));
		} else if (packet instanceof PacketInServerStatus) {
			owner.getStatus().applyPacket((PacketInServerStatus) packet);
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
					owner.writePacket(new PacketOutServerStatus(Action.SERVER, new GameType[]{client.getStatus().getTyp()}, ((PacketInServerStatusRequest) packet).getValue(), client.getStatus().getServerId(), client.getStatus().isVisiable(), client.getStatus().getState(), player.size(), client.getStatus().getMaxPlayers(), ((PacketInServerStatusRequest) packet).isPlayer() ? player : null));
					return;
				}
				break;
			case GENERAL:
				List<String> players = PlayerManager.getPlayersFromServer(null);
				int playersCount = 0;
				for(Client bungee : ServerThread.getBungeecords()){
					playersCount = playersCount+bungee.getStatus().getPlayers();
				}
				if (players != null) {
					owner.writePacket(new PacketOutServerStatus(Action.GENERAL, null, ((PacketInServerStatusRequest) packet).getValue(), "network", true, GameState.NONE, playersCount, -1, ((PacketInServerStatusRequest) packet).isPlayer() ? players : null));
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
			
			//IS THAT RIGHT?
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
			HashMap<ServerType, ArrayList<Client>> tempServers = new HashMap<ServerType, ArrayList<Client>>(ArcadeManager.getLastCalculated());
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
					out = SkinCache.getSkin(((PacketSkinRequest) packet).getRequests()[i].getName());
					break;
				case UUID:
					out = SkinCache.getSkin(((PacketSkinRequest) packet).getRequests()[i].getUuid());
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
						ids[i] = -3;
					}
			}
			if(ids == null)
				ids = new int[0];
			owner.writePacket(new PacketPlayerIdResponse(packet.getPacketUUID(), ids));
		} else if(packet instanceof PacketReportRequest){
			PacketReportRequest p = (PacketReportRequest) packet;
			List<ReportEntity> response;
			switch (p.getType()) {
			case OPEN_REPORTS:
				response = ReportManager.getInstance().getOpenReports();
				break;
			case PLAYER_OPEN_REPORTS:
				response = ReportManager.getInstance().getReportsFromReporter(p.getValue(), false);
				break;
			default:
				owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(-1, "Type not found")));
				return;
			}
			owner.writePacket(new PacketReportResponse(p.getPacketUUID(), response.toArray(new ReportEntity[0])));
		} else if(packet instanceof PacketReportEdit){
			PacketReportEdit p = (PacketReportEdit) packet;
			ReportEntity e;
			switch (p.getEdit()) {
			case CREATE:
				int id = ReportManager.getInstance().createReport(p.getValue(), p.getValue2(), p.getReson(), p.getInfo());
				owner.writePacket(new PacketReportResponse(p.getPacketUUID(), new ReportEntity[]{ReportManager.getInstance().getReportEntity(id)}));
				return;
			case ADD_WORKER:
				e = ReportManager.getInstance().getReportEntity(p.getValue());
				if(e == null){
					owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(-1, "Type report not found")));
					return;
				}
				ReportManager.getInstance().addWorker(e, p.getValue2());
				owner.writePacket(new PacketOutPacketStatus(packet, null));
				return;
			case DONE_WORKER:
				e = ReportManager.getInstance().getReportEntity(p.getValue());
				if(e == null){
					owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(-1, "Type report not found")));
					return;
				}
				ReportManager.getInstance().doneWorker(e, p.getValue2());
				owner.writePacket(new PacketOutPacketStatus(packet, null));
				return;
			case CLOSE:
				e = ReportManager.getInstance().getReportEntity(p.getValue());
				if(e == null){
					owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(-1, "Type report not found")));
					return;
				}
				ReportManager.getInstance().closeReport(e,p.getValue2());
				return;
			default:
				owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(-1, "Type not found")));
				return;
			}
		} else if(packet instanceof PacketBoosterStatusRequest){
			PacketBoosterStatusRequest p = (PacketBoosterStatusRequest) packet;
			if(p.getPlayerId() == -1){
				NetworkBooster booster = BoosterManager.getManager().getBooster(p.getType());
				owner.writePacket(new PacketBoosterStatusResponse(booster.getPlayer(), booster.getType(), booster.getStart(), booster.getTime()));
				return;
			}
			NetworkBooster booster = BoosterManager.getManager().getBooster(p.getType(),PlayerManager.getPlayer(p.getPlayerId()));
			owner.writePacket(new PacketBoosterStatusResponse(booster.getPlayer(), booster.getType(), booster.getStart(), booster.getTime()));
			return;
		} else if(packet instanceof PacketBoosterActive){
			PacketBoosterActive p = (PacketBoosterActive) packet;
			OnlinePlayer player = PlayerManager.getPlayer(p.getPlayerId());
			BoosterManager.getManager().activeBooster(player, p.getTime(), p.getType());
			owner.writePacket(new PacketOutPacketStatus(packet, null));
		}
		
		else if(packet instanceof PacketGildSarch){
			List<Entry<UUID, String>> response = new ArrayList<>();
			Gilde g = null;
			switch (((PacketGildSarch) packet).getAction()) {
			case GILDE_NAME:
				g = GildenManager.getManager().getGilde(((PacketGildSarch) packet).getValue());
				break;
			case PLAYER:
				g = GildenManager.getManager().getGilde(Integer.parseInt(((PacketGildSarch) packet).getValue().split(";")[0]), GildeType.values()[Integer.parseInt(((PacketGildSarch) packet).getValue().split(";")[0])]);
				break;
			case OWN_GILD:
				g = GildenManager.getManager().getOwnGilde(Integer.valueOf(((PacketGildSarch) packet).getValue()));
				break;
			case TYPE:
				response.addAll(GildenManager.getManager().getAvariableGilden(GildeType.values()[Integer.valueOf(((PacketGildSarch) packet).getValue())]).entrySet());
				break;
			default:
				break;
			}
			final Gilde gc = g;
			if(g != null)
				response.add(new Entry<UUID, String>() {
					@Override
					public UUID getKey() {
						return gc.getUuid();
					}

					@Override
					public String getValue() {
						return gc.getName();
					}

					@Override
					public String setValue(String value) {
						return gc.getName();
					}
				});
			//owner.writePacket(new PacketGildSarchResponse(packet.getPacketUUID(), new HashMap<UUID, String>(response))); //TODO
		}
		else if(packet instanceof PacketGildInformationRequest){
			Gilde gilde = GildenManager.getManager().getGilde((((PacketGildInformationRequest) packet).getGild()));
			if(gilde == null){
				owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(-2, "Gilde not found!")));
				return;
			}
			owner.writePacket(new PacketGildInformationResponse(gilde.getUuid(), gilde.getActiveSectionsArray(), gilde.getName(), gilde.getShortName(), gilde.getOwnerId()));
		}
		else if(packet instanceof PacketGildCostumDataAction){
			Gilde gilde = GildenManager.getManager().getGilde((((PacketGildCostumDataAction) packet).getGilde()));
			if(gilde == null){
				owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(-2, "Gilde not found!")));
				return;
			}
			if(!gilde.getSelection(((PacketGildCostumDataAction) packet).getType()).isActive()){
				owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(-1, "Section not active!")));
				return;
			}
			switch (((PacketGildCostumDataAction) packet).getAction()) {
			case GET:
				owner.writePacket(new PacketGildCostumDataResponse(gilde.getUuid(), ((PacketGildCostumDataAction) packet).getType(), gilde.getSelection(((PacketGildCostumDataAction) packet).getType()).getCostumData()));
				return;
			case SET:
				gilde.getSelection(((PacketGildCostumDataAction) packet).getType()).setCostumData(((PacketGildCostumDataAction) packet).getData());
				owner.writePacket(new PacketOutPacketStatus(packet, null));
				return;
			default:
				break;
			}
		}
		else if(packet instanceof PacketGildMemberRequest){
			Gilde gilde = GildenManager.getManager().getGilde((((PacketGildMemberRequest) packet).getGilde()));
			if(gilde == null){
				owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(-2, "Gilde not found!")));
				return;
			}
			owner.writePacket(new PacketGildMemberResponse(gilde.getUuid(), gilde.buildMemberInfo()));
		}
		else if(packet instanceof PacketGildMemeberAction){
			Gilde gilde = GildenManager.getManager().getGilde((((PacketGildMemeberAction) packet).getGilde()));
			if(gilde == null){
				owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(-2, "Gilde not found!")));
				return;
			}
			if(!gilde.getSelection(((PacketGildMemeberAction) packet).getType()).isActive()){
				owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(-1, "Section not active!")));
				return;
			}
			switch (((PacketGildMemeberAction) packet).getAction()) {
			case CHANGE_GROUP:
				gilde.getSelection(((PacketGildMemeberAction) packet).getType()).addPlayer(((PacketGildMemeberAction) packet).getPlayerId(), ((PacketGildMemeberAction) packet).getData());
				break;
			case KICK:
				gilde.getSelection(((PacketGildMemeberAction) packet).getType()).removePlayer(((PacketGildMemeberAction) packet).getPlayerId());
				break;
			default:
				System.out.println("Type -> "+((PacketGildMemeberAction) packet).getAction()+" not supported");
				owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(-1, "Type -> "+((PacketGildMemeberAction) packet).getAction()+" not supported")));
				return;
			}
			owner.writePacket(new PacketOutPacketStatus(packet, null));
		}
		else if(packet instanceof PacketGildPermissionRequest){
			Gilde gilde = GildenManager.getManager().getGilde((((PacketGildPermissionRequest) packet).getGilde()));
			if(gilde == null){
				owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(-2, "Gilde not found!")));
				return;
			}
			if(!gilde.getSelection(((PacketGildPermissionRequest) packet).getType()).isActive()){
				owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(-1, "Section not active!")));
				return;
			}
			if(((PacketGildPermissionRequest) packet).getGroup() == null){
				owner.writePacket(new PacketGildPermissionResponse(gilde.getUuid(), ((PacketGildPermissionRequest) packet).getType(), ((PacketGildPermissionRequest) packet).getGroup(), gilde.getSelection(((PacketGildPermissionRequest) packet).getType()).getPermission().getGroups()));
				return;
			}
			owner.writePacket(new PacketGildPermissionResponse(gilde.getUuid(), ((PacketGildPermissionRequest) packet).getType(), ((PacketGildPermissionRequest) packet).getGroup(), gilde.getSelection(((PacketGildPermissionRequest) packet).getType()).getPermission().getGroup(((PacketGildPermissionRequest) packet).getGroup()).getPermissions()));
		}
		else if(packet instanceof PacketGildPermissionEdit){
			Gilde gilde = GildenManager.getManager().getGilde((((PacketGildPermissionEdit) packet).getGilde()));
			if(gilde == null){
				owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(-2, "Gilde not found!")));
				return;
			}
			if(!gilde.getSelection(((PacketGildPermissionEdit) packet).getType()).isActive()){
				owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(-1, "Section not active!")));
				return;
			}
			switch (((PacketGildPermissionEdit) packet).getAction()) {
			case ADD_PERMISSION:
				gilde.getSelection(((PacketGildPermissionEdit) packet).getType()).getPermission().getGroup(((PacketGildPermissionEdit) packet).getGroup()).addPermission(((PacketGildPermissionEdit) packet).getPermission());
				owner.writePacket(new PacketOutPacketStatus(packet, null));
				return;
			case REMOVE_PERMISSION:
				gilde.getSelection(((PacketGildPermissionEdit) packet).getType()).getPermission().getGroup(((PacketGildPermissionEdit) packet).getGroup()).removePermission(((PacketGildPermissionEdit) packet).getPermission());
				owner.writePacket(new PacketOutPacketStatus(packet, null));
				return;
			case DELETE_GROUP:
				gilde.getSelection(((PacketGildPermissionEdit) packet).getType()).getPermission().deleteGroup(((PacketGildPermissionEdit) packet).getGroup());
				owner.writePacket(new PacketOutPacketStatus(packet, null));
				return;
			case CREATE_GROUP:
				gilde.getSelection(((PacketGildPermissionEdit) packet).getType()).getPermission().createGroup(((PacketGildPermissionEdit) packet).getGroup());
				owner.writePacket(new PacketOutPacketStatus(packet, null));
				return;
			default:
				break;
			}
		}
		else if(packet instanceof PacketGildUpdateSectionStatus){
			Gilde gilde = GildenManager.getManager().getGilde((((PacketGildUpdateSectionStatus) packet).getGilde()));
			if(gilde == null){
				owner.writePacket(new PacketOutPacketStatus(packet, new PacketOutPacketStatus.Error(-2, "Gilde not found!")));
				return;
			}
			gilde.getSelection(((PacketGildUpdateSectionStatus) packet).getType()).setActive(((PacketGildUpdateSectionStatus) packet).isState());
			owner.writePacket(new PacketOutPacketStatus(packet, null));
		}
		else if(packet instanceof PacketGildCreate){
			//TODO
		}
		else if(packet instanceof PacketTeamspeakAction){
			switch (((PacketTeamspeakAction) packet).getAction()) {
			case UNLINK:
				TeamspeakClient.getInstance().unlink(PlayerManager.getPlayer(((PacketTeamspeakAction) packet).getPlayerId()));
				break;
			case UPDATE_AVATAR:
				TeamspeakClient.getInstance().updateIcon(PlayerManager.getPlayer(((PacketTeamspeakAction) packet).getPlayerId()));
				break;
			case UPDATE_GROUPS:
				TeamspeakClient.getInstance().updateGroups(PlayerManager.getPlayer(((PacketTeamspeakAction) packet).getPlayerId()),Arrays.asList(((PacketTeamspeakAction) packet).getData().split(";")));
				break;
			default:
				break;
			}
			owner.writePacket(new PacketOutPacketStatus(packet, null));
		}
		else if(packet instanceof PacketTeamspeakRequestAction){
			if(((PacketTeamspeakRequestAction) packet).isAccept())
				TeamspeakClient.getInstance().acceptRequest(((PacketTeamspeakRequestAction) packet).getRequest());
			else
				TeamspeakClient.getInstance().denyRequest(((PacketTeamspeakRequestAction) packet).getRequest());
			owner.writePacket(new PacketOutPacketStatus(packet, null));
		}
	}

}
