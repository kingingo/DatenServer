package dev.wolveringer.teamspeak;

import java.awt.Image;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.TS3Query.FloodRate;
import com.github.theholywaffle.teamspeak3.api.ClientProperty;
import com.github.theholywaffle.teamspeak3.api.TextMessageTargetMode;
import com.github.theholywaffle.teamspeak3.api.event.ClientJoinEvent;
import com.github.theholywaffle.teamspeak3.api.event.TS3EventAdapter;
import com.github.theholywaffle.teamspeak3.api.event.TS3EventType;
import com.github.theholywaffle.teamspeak3.api.event.TextMessageEvent;
import com.github.theholywaffle.teamspeak3.api.reconnect.ConnectionHandler;
import com.github.theholywaffle.teamspeak3.api.reconnect.ReconnectStrategy;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.github.theholywaffle.teamspeak3.api.wrapper.FileTransfare;

import dev.wolveringer.configuration.ServerConfiguration;
import eu.epicpvp.datenserver.definitions.dataserver.gamestats.GameType;
import eu.epicpvp.datenserver.definitions.dataserver.gamestats.StatsKey;
import dev.wolveringer.dataserver.player.OnlinePlayer;
import dev.wolveringer.dataserver.player.PlayerManager;
import dev.wolveringer.dataserver.protocoll.packets.PacketInStatsEdit;
import dev.wolveringer.dataserver.protocoll.packets.PacketInStatsEdit.Action;
import dev.wolveringer.event.EventHelper;
import dev.wolveringer.gamestats.Statistic;
import dev.wolveringer.hashmaps.CachedHashMap;
import dev.wolveringer.skin.SteveSkin;
import dev.wolveringer.thread.ThreadFactory;
import lombok.Getter;
import lombok.Setter;

public class TeamspeakClient {
	@Getter
	@Setter
	private static TeamspeakClient instance;

	private final TS3Config config;
	@Getter
	private TS3Query client;
	@Getter
	private boolean connected;

	private static final String IPADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	private CachedHashMap<UUID, Entry<Client, OnlinePlayer>> requests = new CachedHashMap<>(60, TimeUnit.SECONDS);
	private HashMap<String, Integer> groupMapping = new HashMap<>();
	private List<Integer> ignoreGroups = new ArrayList<>();
	private int ownId;

	public TeamspeakClient(TS3Config config) {
		this.config = config;
		this.config.setFloodRate(FloodRate.UNLIMITED);
		this.groupMapping = ServerConfiguration.getGroupMapping();
		this.ignoreGroups = ServerConfiguration.getIgnoreGroup();
		config.setReconnectStrategy(ReconnectStrategy.exponentialBackoff());
		config.setConnectionHandler(new ConnectionHandler() {

			@Override
			public void onConnect(TS3Query ts3Query) {
				login(ServerConfiguration.config.getString("teamspeak.username"), ServerConfiguration.config.getString("teamspeak.password"), ServerConfiguration.config.getInt("teamspeak.serverId"));
				ts3Query.getApi().setNickname(ServerConfiguration.config.getString("teamspeak.nickname"));
				System.out.println("§aTeamspeak client connected!");
			}

			@Override
			public void onDisconnect(TS3Query ts3Query) {
				System.out.println("§cTeamspeak client disconnected!");
			}
		});
		this.client = new TS3Query(config);
		this.client.getApi().addTS3Listeners(new TS3EventAdapter() {
			@Override
			public void onTextMessage(TextMessageEvent e) {
				if (e.getTargetMode() == TextMessageTargetMode.CLIENT && ownId != e.getInvokerId()) {
					System.out.println("Having an message: " + e.getMessage());
					ThreadFactory.getInstance().createThread(() -> {
						String username = e.getMessage();
						if (!username.matches("([a-zA-Z0-9_]){3,16}")) {
							getClient().getAsyncApi().sendPrivateMessage(e.getInvokerId(), "Invalid minecraft name!");
							return;
						}
						final OnlinePlayer player = PlayerManager.getPlayer(username, false);
						if (player == null || !player.isPlaying()) {
							getClient().getAsyncApi().sendPrivateMessage(e.getInvokerId(), "Target player isnt online!");
							return;
						}
						if (getIdentifier(player) != null) {
							getClient().getAsyncApi().sendPrivateMessage(e.getInvokerId(), "This account is alredy linked!");
							return;
						}
						String ip = player.getCurruntIp();
						final Client clientinfo = getClient().getApi().getClientInfo(e.getInvokerId());
						try {
							getClient().getAsyncApi().sendPrivateMessage(e.getInvokerId(), "Resolving ip...");
							if (ip == null) {
								getClient().getAsyncApi().sendPrivateMessage(e.getInvokerId(), "Please rejoin on the MinecraftServer.");
								return;
							}
							if (!ip.equalsIgnoreCase(clientinfo.getIp())) {
								InetAddress ip1 = getAdress(ip);
								InetAddress ip2 = getAdress(clientinfo.getIp());
								if (ip1 == null) {
									getClient().getAsyncApi().sendPrivateMessage(e.getInvokerId(), "Cant resolve your minecraft adress (" + ip + ")");
									return;
								}
								if (ip2 == null) {
									getClient().getAsyncApi().sendPrivateMessage(e.getInvokerId(), "Cant resolve your teamspeak adress (" + clientinfo.getIp() + ")");
									return;
								}
								if (!ip1.getHostAddress().equalsIgnoreCase(ip2.getHostAddress())) {
									System.out.println("Unmatching ip (" + ip1 + "/" + ip2 + ")");
									getClient().getAsyncApi().sendPrivateMessage(e.getInvokerId(), "The target player isnt you! (IP_MISMATCH)");
									return;
								}
							}
						} catch (Exception ex) {
							ex.printStackTrace();
							getClient().getAsyncApi().sendPrivateMessage(e.getInvokerId(), "Cant resolve IP. Try again.");
							return;
						}
						UUID token = UUID.randomUUID();
						EventHelper.callTeamspeakLinkRequestEvent(username, clientinfo, token);
						requests.put(token, new Entry<Client, OnlinePlayer>() {

							@Override
							public Client getKey() {
								return (Client) clientinfo;
							}

							@Override
							public OnlinePlayer getValue() {
								return player;
							}

							@Override
							public OnlinePlayer setValue(OnlinePlayer value) {
								return player;
							}
						});
						getClient().getAsyncApi().sendPrivateMessage(e.getInvokerId(), "An request was send to you in minecraft.");
					}).start();
				}
			}

			@Override
			public void onClientJoin(ClientJoinEvent e) {
				String[] groups = e.getClientServerGroups().split(",");
				for (String g : groups)
					if (g.equalsIgnoreCase(String.valueOf(ServerConfiguration.getTeamspeakLinkedGroupId()))) {
						return;
					}
				getClient().getAsyncApi().sendPrivateMessage(e.getClientId(), "Hey " + e.getClientNickname() + "! Wie folgt kannst du dich verifizieren:");
				getClient().getAsyncApi().sendPrivateMessage(e.getClientId(), "1. Joine auf unser Netzwerk: ClashMC.eu");
				getClient().getAsyncApi().sendPrivateMessage(e.getClientId(), "2. Sende deinen Minecraft-Nick in diesen Channel");
				getClient().getAsyncApi().sendPrivateMessage(e.getClientId(), "3. Bestätige die Anfrage, welche dir InGame zugesendet wird");
				getClient().getAsyncApi().sendPrivateMessage(e.getClientId(), "4. Genieße viele Vorteile als Verifizierter-Spieler");
				if (e.getClientNickname().contains("DominikEnders")) {
					getClient().getAsyncApi().pokeClient(e.getClientId(), "[b]Hey du wichser. Bitter volführe deine verifizirung und zwar jetzt![/b]");
				} else {
					getClient().getAsyncApi().pokeClient(e.getClientId(), "[b]Willkommen! Bitte verifiziere dich![/b]");
				}
			}

		});
	}

	private InetAddress getAdress(String in) throws UnknownHostException {
		InetAddress out = null;
		if (in.matches(IPADDRESS_PATTERN))
			try {
				out = Inet4Address.getByName(in);
			} catch (Exception e) {
				e.printStackTrace();
			}
		else
			try {
				out = Inet6Address.getByName(in);
			} catch (Exception e) {
				e.printStackTrace();
			}
		if (out == null) {
			try {
				out = InetAddress.getByName(in);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return out;
	}

	public void unlink(OnlinePlayer player) {
		String identifier = getIdentifier(player);
		if (identifier == null)
			return;
		try {
			Client client = getClient().getApi().getClientByUId(identifier);
			int databaseId = client.getDatabaseId();
			getClient().getApi().removeClientFromServerGroup(ServerConfiguration.getTeamspeakLinkedGroupId(), databaseId);
			getClient().getAsyncApi().sendPrivateMessage(client.getId(), "You identity is unlinked right now.");
			for (int group : client.getServerGroups()) {
				if (!ignoreGroups.contains(new Integer(group)))
					this.client.getApi().removeClientFromServerGroup(group, client.getDatabaseId());
			}
			this.client.getApi().deleteClientPermission(client.getDatabaseId(), "i_icon_id");
			Map<ClientProperty, String> options = new HashMap<>();
			options.put(ClientProperty.CLIENT_DESCRIPTION, "Unlinked.");
		} catch (Exception e) {
			e.printStackTrace();
		}
		player.getStatsManager().applayChanges(new PacketInStatsEdit(player.getPlayerId(), new PacketInStatsEdit.EditStats[] { new PacketInStatsEdit.EditStats(GameType.TEAMSPEAK, Action.SET, StatsKey.TEAMSPEAK_IDENTITY, "") }));
	}

	public void updateGroups(OnlinePlayer player, List<String> groups) {
		String identifier = getIdentifier(player);
		if (identifier == null)
			return;
		Client client = getClient().getApi().getClientByUId(identifier);
		List<Integer> targetGroups = new ArrayList<>();
		for (String s : groups) {
			if (groupMapping.containsKey(s))
				targetGroups.add(groupMapping.get(s));
			else
				System.err.println("Missing group mapping for " + s);
		}
		targetGroups.add(ServerConfiguration.getTeamspeakLinkedGroupId());

		for (int group : client.getServerGroups()) {
			if (targetGroups.contains(new Integer(group))) {
				targetGroups.remove(new Integer(group));
				continue;
			}
			if (!ignoreGroups.contains(new Integer(group)))
				this.client.getApi().removeClientFromServerGroup(group, client.getDatabaseId());
		}

		for (Integer i : targetGroups)
			this.client.getApi().addClientToServerGroup(i, client.getDatabaseId());
	}

	public void updateIcon(OnlinePlayer player) {
		String identifier = getIdentifier(player);
		if (identifier == null)
			return;
		Client client = getClient().getApi().getClientByUId(identifier);
		if (client == null) {
			System.out.println("Cant update avatar for " + player.getName());
			return;
		}
		if (!(player.getSkinManager().getSkin() instanceof SteveSkin))
			setMinecraftIcon(client, player.getSkinManager().getSkin().getName());
		else
			setMinecraftIcon(client, player.getName());
	}

	private String getIdentifier(OnlinePlayer player) {
		String identifier = null;
		for (Statistic s : player.getStatsManager().getStats(GameType.TEAMSPEAK).getStats())
			if (s.getStatsKey() == StatsKey.TEAMSPEAK_IDENTITY)
				identifier = (String) s.getValue();
		if ("".equalsIgnoreCase(identifier))
			identifier = null;
		return identifier;
	}

	public boolean isRequestOpen(UUID uuid) {
		return requests.containsKey(uuid);
	}

	public void acceptRequest(UUID uuid) {
		Client info = requests.get(uuid).getKey();
		OnlinePlayer player = requests.get(uuid).getValue();
		requests.remove(uuid);
		getClient().getApi().addClientToServerGroup(ServerConfiguration.getTeamspeakLinkedGroupId(), info.getDatabaseId());
		player.getStatsManager().applayChanges(new PacketInStatsEdit(player.getPlayerId(), new PacketInStatsEdit.EditStats[] { new PacketInStatsEdit.EditStats(GameType.TEAMSPEAK, Action.SET, StatsKey.TEAMSPEAK_IDENTITY, info.getUniqueIdentifier()) }));
		client.getAsyncApi().sendPrivateMessage(info.getId(), "Danke das du dich verifieziert hast!");
		client.getAsyncApi().sendPrivateMessage(info.getId(), "Nun ist es für dich möglich den Support, weitere Channel sowie einen Avatar zu nutzen!");
		client.getAsyncApi().sendPrivateMessage(info.getId(), "");
		client.getAsyncApi().sendPrivateMessage(info.getId(), "Verwalten kannst du deine Ränge sowie dein Icon InGame per /ts");

		updateIcon(player);
		updateDiscription(player, info);
	}

	public void denyRequest(UUID uuid) {
		Client info = requests.get(uuid).getKey();
		requests.remove(uuid);
		client.getApi().sendPrivateMessage(info.getId(), "Link request canceled!");
	}

	public void connect() {
		System.out.println("§6Teamspeak client reconnected!");
		client.connect();
		connected = true;
		ThreadFactory.getFactory().createThread(()->{
			while (connected) {
				if(!client.isConnected()){
					connected = false;
					connect();
				}
			}
		});
	}

	public boolean login(String username, String password, int serverId) {
		boolean success = client.getApi().login(username, password);
		if (success)
			client.getApi().selectVirtualServerById(serverId);
		client.getApi().registerEvent(TS3EventType.TEXT_PRIVATE);
		client.getApi().registerEvent(TS3EventType.SERVER);
		this.ownId = client.getApi().whoAmI().getId();
		return success;
	}

	public void setName(String name) {
		client.getApi().setNickname(name);
	}

	private int minAdd = 10000;

	public void setMinecraftIcon(Client client, String skin) {
		try {
			Image image = MinecraftUtils.getHead(skin, true);
			if (image == null)
				return;
			image = image.getScaledInstance(16, 16, 1);
			byte[] data = toByte(image, "png");

			this.client.getApi().deleteClientPermission(client.getDatabaseId(), "i_icon_id");
			int iconId = client.getDatabaseId() + minAdd;
			this.client.getApi().deleteFile("/icon_" + iconId);
			FileTransfare trans = this.client.getApi().initFileUpload("/icon_" + iconId, data.length, null, true);
			this.client.getApi().uploadFile(trans, data);
			this.client.getApi().addClientPermission(client.getDatabaseId(), "i_icon_id", iconId, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private byte[] toByte(Image image, String type) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(MinecraftUtils.toBufferedImage(image), type, baos);
		byte[] imageInByte = baos.toByteArray();
		return imageInByte;
	}

	public void updateDiscription(OnlinePlayer player,Client client){
		//Minecraft username: WolverinDEV | PlayerID: 123321 | Online auf: SkyPvP

		Map<ClientProperty, String> options = new HashMap<>();
		options.put(ClientProperty.CLIENT_DESCRIPTION, "InGame-Name: " + player.getName());
		instance.getClient().getApi().editClient(client.getId(), options);
	}

	public static void main(String[] args) {
		//WolverinDEV_03
		//vHbzrtxd
		// -DtsHost=ts.epicpvp.eu -DtsUsername=WolverinDEV_02
		// -DtsPassword=aL8JPz4C
		final TS3Config config = new TS3Config();
		config.setHost(System.getProperty("tsHost"));
		config.setDebugLevel(Level.ALL);

		final TS3Query query = new TS3Query(config);
		query.connect();

		final TS3Api api = query.getApi();
		api.login(System.getProperty("tsUsername"), System.getProperty("tsPassword"));
		api.selectVirtualServerById(1);
		String message = "Ne war ein Problem mit meinem Windows System und nicht mit der pom. Musste build.source.encoding neu hinzufügen weil windows eben WINDOWS-0532895437593475348765346894378647896438764678439679836789468943SHITENCODING used und nicht UTF-8";
		api.sendPrivateMessage(api.getClientByNameExact("WolverinDEV", false).getId(), message);
		//TeamspeakClient client = new TeamspeakClient(new TS3Config().setHost(System.getProperty("tsHost")).setDebugLevel(Level.ALL));
		//client.connect();
		//System.out.println("Connected");
		//if (!client.login(System.getProperty("tsUsername"), System.getProperty("tsPassword"), 1)) {
		//	System.out.println("Cant login!");
		//	System.exit(-1);
		//}
		//System.out.println("Logged in");
		//Client player = client.client.getApi().getClientByNameExact("CuzImZyp | Flo", true);
		//client.client.getApi().broadcast("Hello world");
		// client.setMinecraftIcon(player, "CuzImZyp");
	}
}
