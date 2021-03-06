package eu.epicpvp.dataserver.terminal.commands;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.ArrayUtils;

import eu.epicpvp.connection.server.ServerThread;
import eu.epicpvp.dataserver.connection.Client;
import eu.epicpvp.dataserver.protocoll.packets.PacketServerAction;
import eu.epicpvp.dataserver.protocoll.packets.PacketServerAction.Action;
import eu.epicpvp.dataserver.terminal.ArgumentList;
import eu.epicpvp.dataserver.terminal.ArgumentList.Argument;
import eu.epicpvp.dataserver.terminal.ChatColor;
import eu.epicpvp.dataserver.terminal.CommandExecutor;
import eu.epicpvp.dataserver.terminal.ConsoleWriter;
import eu.epicpvp.datenserver.definitions.connection.ClientType;
import eu.epicpvp.datenserver.definitions.dataserver.gamestats.GameState;
import eu.epicpvp.datenserver.definitions.dataserver.gamestats.GameType;
import eu.epicpvp.serverbalancer.ArcadeManager;

public class CommandServerManager implements CommandExecutor {

	public CommandServerManager() {
		options.addOption("st", "subtype", true, "Subtype parttern");
		options.addOption("gt", "gametype", true, "Gametype");
		options.addOption("t", "type", true, "Server type");
		options.addOption("l", "inlobby", false, "List server only in lobby");
		options.addOption("m", "message", true, "Restart/Stop message");
	}

	@Override
	public void onCommand(String command, ConsoleWriter writer, String[] args) {
		if (args.length >= 1) {
			if (args[0].equalsIgnoreCase("list")) {
				CommandLine cmdArgs = paradiseOptions(args, 1, true);
				if (cmdArgs == null) {
					writer.sendMessage("§cCommand line is null! (Input '" + Arrays.toString(ArrayUtils.subarray(args, 1, args.length)) + "')");
					return;
				}
				if (ClientType.valueOf(cmdArgs.getOptionValue("type", ClientType.ALL + "")) == null) {
					writer.sendMessage("§cClientType not found!");
					return;
				}
				Stream<Client> clients = ServerThread.getServer(ClientType.valueOf(cmdArgs.getOptionValue("type", ClientType.ALL.toString()))).stream();
				if (cmdArgs.hasOption("gametype")) {
					final GameType type = GameType.valueOf(cmdArgs.getOptionValue("gametype"));
					if (type == null) {
						writer.sendMessage("§cGameType not found!");
						return;
					}
					writer.sendMessage("§aChecking for Gametype: " + type);
					clients = clients.filter(new Predicate<Client>() {
						@Override
						public boolean test(Client t) {
							return t.getStatus().getTyp().ordinal() == type.ordinal();
						}
					});
				}
				if (cmdArgs.hasOption("subtype")) {
					String subType = cmdArgs.getOptionValue("subtype");
					writer.sendMessage("§aChecking for subtype: " + subType);
					clients = clients.filter(new Predicate<Client>() {
						@Override
						public boolean test(Client t) {
							return t.getStatus().getSubType().matches(subType);
						}
					});
				}
				if (cmdArgs.hasOption("inlobby")) {
					writer.sendMessage("§aChecking for inlobby");
					clients = clients.filter(new Predicate<Client>() {
						@Override
						public boolean test(Client t) {
							return t.getStatus().getState() == GameState.LobbyPhase;
						}
					});
				}
				clients = clients.sorted(new Comparator<Client>() {
					@Override
					public int compare(Client c1, Client c2) {
						if (c1 == null || c1.getName() == null)
							return 1;
						if (c2 == null || c2.getName() == null)
							return -1;
						return c1.getName().compareToIgnoreCase(c2.getName());
					}
				});
				Iterator<Client> iclients = clients.iterator();
				if (iclients.hasNext()) {
					writer.sendMessage("§aServers:");
					int count = 0;
					int player = 0;
					while (iclients.hasNext()) {
						count++;
						Client c = iclients.next();
						player += c.getPlayers().size();
						writer.sendMessage(" §7- §" + (c.isConnected() ? "a" : "c") + c.getName() + "§r§7[§b" + c.getStatus().getSubType() + "§7] §eServer-ID: §b" + c.getStatus().getServerId() + " §eType: §b" + c.getType() + " §eGame: §b" + c.getStatus().getTyp() + " §eState: §b" + c.getStatus().getState() + " §ePlayers: §b" + c.getStatus().getPlayers()+"§e|§b"+c.getPlayers().size() + " §ePublic: §b" + c.getStatus().isVisiable());
					}
					writer.sendMessage("§b" + count + " §aServers are now displayed. Player online on these servers (incorrect): §b" + player);
					int playersCount = 0;
					for(Client bungee : ServerThread.getBungeecords()){
						playersCount = playersCount+bungee.getStatus().getPlayers();
					}
					writer.sendMessage("§aCurrently online players (real): §b" + playersCount );
				} else
					writer.sendMessage("§cEs wurden keine Server unter diesem Parameter gefunden.");
			} else if (args[0].equalsIgnoreCase("printLobbies")) {
				ArcadeManager.writeServers();
			}
			if (args[0].equalsIgnoreCase("restart")) {
				CommandLine cmdArgs = paradiseOptions(args, 1, true);
				if (cmdArgs == null)
					return;
				int filter = 0;
				if (ClientType.valueOf(cmdArgs.getOptionValue("type", ClientType.ALL + "")) == null) {
					writer.sendMessage("§cClientType not found!");
					return;
				}
				Stream<Client> clients = ServerThread.getServer(ClientType.valueOf(cmdArgs.getOptionValue("type", ClientType.ALL + ""))).stream();
				if (cmdArgs.hasOption("gametype")) {
					final GameType type = GameType.valueOf(cmdArgs.getOptionValue("gametype"));
					if (type == null) {
						writer.sendMessage("§cGameType not found!");
						return;
					}
					clients = clients.filter(new Predicate<Client>() {
						@Override
						public boolean test(Client t) {
							return t.getStatus().getTyp().ordinal() == type.ordinal();
						}
					});
					filter++;
				}
				if (cmdArgs.hasOption("subtype")) {
					String subType = cmdArgs.getOptionValue("subtype");
					if (subType == null) {
						writer.sendMessage("§cSubtype not found!");
						return;
					}
					clients = clients.filter(new Predicate<Client>() {
						@Override
						public boolean test(Client t) {
							if (t == null || t.getStatus() == null || t.getStatus().getSubType() == null)
								return false;
							return t.getStatus().getSubType().matches(subType);
						}
					});
					filter++;
				}
				if (filter == 0) {
					writer.sendMessage("Please provide a filter!");
					return;
				}
				Iterator<Client> iclients = clients.iterator();
				int count = 0;
				String message = "§cServer is restarting.";
				if (cmdArgs.hasOption("message"))
					message = ChatColor.translateAlternateColorCodes('&', cmdArgs.getOptionValue("message"));
				while (iclients.hasNext()) {
					count++;
					iclients.next().writePacket(new PacketServerAction(new PacketServerAction.PlayerAction[]
					{ new PacketServerAction.PlayerAction(-1, Action.RESTART, message) }));
				}
				writer.sendMessage("§c" + count + " server are restarting");
			}
			if (args[0].equalsIgnoreCase("stop")) {
				CommandLine cmdArgs = paradiseOptions(args, 1, true);
				if (cmdArgs == null)
					return;
				if (ClientType.valueOf(cmdArgs.getOptionValue("type", ClientType.ALL + "")) == null) {
					writer.sendMessage("§cClientType not found!");
				}
				Stream<Client> clients = ServerThread.getServer(ClientType.valueOf(cmdArgs.getOptionValue("type", ClientType.ALL + ""))).stream();
				int filter = 0;
				if (cmdArgs.hasOption("gametype")) {
					final GameType type = GameType.valueOf(cmdArgs.getOptionValue("gametype"));
					if (type == null) {
						writer.sendMessage("§cGameType not found!");
						return;
					}
					clients = clients.filter(new Predicate<Client>() {
						@Override
						public boolean test(Client t) {
							return t.getStatus().getTyp() == type;
						}
					});
					filter++;
				}
				if (cmdArgs.hasOption("subtype")) {
					String subType = cmdArgs.getOptionValue("subtype");
					clients = clients.filter(new Predicate<Client>() {
						@Override
						public boolean test(Client t) {
							return t.getStatus().getSubType().matches(subType);
						}
					});
					filter++;
				}
				if (filter == 0) {
					writer.sendMessage("Please provide a filter!");
					return;
				}
				Iterator<Client> iclients = clients.iterator();
				int count = 0;
				String message = "§cServer is stoping.";
				if (cmdArgs.hasOption("message"))
					message = ChatColor.translateAlternateColorCodes('&', cmdArgs.getOptionValue("message"));
				while (iclients.hasNext()) {
					count++;
					iclients.next().writePacket(new PacketServerAction(new PacketServerAction.PlayerAction[]
					{ new PacketServerAction.PlayerAction(-1, Action.STOP, message) }));
				}
				writer.sendMessage("§c" + count + " server are stoping");
			}
			if(args[0].equalsIgnoreCase("printQueue")){
				ArcadeManager.writeServers();
				return;
			}
		}
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("info")) {
				Client client = ServerThread.getServer(args[1]);
				if (client == null)
					writer.sendMessage("§cClient not found!");
				else {
					writer.sendMessage("§aServerinformationen für den Server: " + client.getName());
					writer.sendMessage("  §aHost: §b" + client.getHost());
					writer.sendMessage("  §aPing: §b" + client.getPing());
					writer.sendMessage("  §aType: §b" + client.getType());
					if (client.getType() != ClientType.BUNGEECORD) {
						writer.sendMessage("  §aGamemode: §b" + client.getStatus().getTyp());
						writer.sendMessage("  §aSub-type: §b" + client.getStatus().getSubType());
						writer.sendMessage("  §aState: §b" + client.getStatus().getState());
					}
					writer.sendMessage("  §aSpieler: §b" + client.getStatus().getPlayers() + "/" + client.getStatus().getPlayers());
					writer.sendMessage("  §aVisiable: §b" + client.getStatus().isVisiable());
				}
			} else if (args[0].equalsIgnoreCase("blacklist") && args[1].equalsIgnoreCase("list")) {
				writer.sendMessage("§aBlacklist:");
				for (GameType t : ArcadeManager.getBlackList().keySet()) {
					writer.sendMessage("  §aType: " + t);
					for (String un : ArcadeManager.getBlackList().get(t))
						writer.sendMessage("  §7- §e" + un);
				}
			}
		}
		if (args.length >= 2) {
			if (args[0].equalsIgnoreCase("switch")) {
				Client client = ServerThread.getServer(args[1]);
				if (client == null)
					writer.sendMessage("§cClient not found!");
				else {
					if (client.getType() != ClientType.ACARDE) {
						writer.sendMessage("§cClient isnt a Arcade Server!");
						return;
					}
					GameType type = GameType.valueOf(args[2]);
					String subType = args.length == 4 ? args[3] : "NONE";
					if (type == null) {
						writer.sendMessage("§cGameType not found");
						return;
					}
					if (client.getStatus().getTyp() == type && client.getStatus().getSubType().equalsIgnoreCase(subType)) {
						writer.sendMessage("§aServer ist schon in diesem zustand!");
						return;
					}
					client.setGame(type, subType);
					writer.sendMessage("§aDer Game-type wurde geändert.");
				}
			}
		}
		if (args.length == 3) {
			if (args[0].equalsIgnoreCase("setVisiabe")) {
				Client client = ServerThread.getServer(args[1]);
				if (client == null)
					writer.sendMessage("§cClient not found!");
				else {
					boolean flag = Boolean.getBoolean(args[2]);
					client.getStatus().setVisiable(flag);
					writer.sendMessage("§aDu hast die sichtbarkeit geändert.");
				}
			}
		}

		if (args.length == 4) {
			if (args[0].equalsIgnoreCase("blacklist")) {
				GameType type = GameType.valueOf(args[2]);
				if (type == null) {
					writer.sendMessage("§cGametype not found.");
					return;
				}
				if (args[1].equalsIgnoreCase("add")) {
					if (ArcadeManager.getBlackList().get(type).add(args[3]))
						writer.sendMessage("§aType blacklisted");
					else
						writer.sendMessage("§cType alredy blacklisted!");
				} else if (args[1].equalsIgnoreCase("remove")) {
					if (ArcadeManager.getBlackList().get(type).remove(args[3]))
						writer.sendMessage("§aBlacklist type removed!");
					else
						writer.sendMessage("§cServer subtype isnt blacklisted.");
				} else
					writer.sendMessage("§cOperaion " + args[1] + " not supported.");
			}
		}
	}

	@Override
	public ArgumentList getArguments() {
		ArgumentList.ArgumentListBuilder builder = ArgumentList.builder();
		builder.arg(new Argument("/smanager list [-subtype <pattern> | -gametype <gametype> | -type <clientype> | -inlobby]", "List servers with condition"));
		builder.arg(new Argument("/smanager info <ClientName>", "List alle informations abaout a client"));
		builder.arg(new Argument("/smanager restart [-subtype <pattern> | -gametype <gametype> | -type <clientype> | -m <message>]", "Restart all servers with condition"));
		builder.arg(new Argument("/smanager stop [-subtype <pattern> | -gametype <gametype> | -type <clientype> | -m <message>]", "Stop all servers with condition"));
		builder.arg(new Argument("/smanager switch <ClientName> <GameType> [<SubType>]", "Switch a server to an other type"));
		builder.arg(new Argument("/smanager setVisiabe <ClientName> <flag>", "Set the server visiablety", true));
		builder.arg(new Argument("/smanager blacklist add <Gametype> <Subtype>", "Add servertype to blacklist"));
		builder.arg(new Argument("/smanager blacklist remove <Gametype> <Subtype>", "Remove servertype from blacklist"));
		builder.arg(new Argument("/smanager blacklist list", "List all blocked server types."));
		builder.arg(new Argument("/smanager printQueue", "Print the Arcade server assignment."));
		return builder.build();
	}

}
