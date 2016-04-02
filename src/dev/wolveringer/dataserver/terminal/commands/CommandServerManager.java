package dev.wolveringer.dataserver.terminal.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;

import dev.wolveringer.client.connection.ClientType;
import dev.wolveringer.connection.server.ServerThread;
import dev.wolveringer.dataserver.connection.Client;
import dev.wolveringer.dataserver.gamestats.GameState;
import dev.wolveringer.dataserver.gamestats.GameType;
import dev.wolveringer.dataserver.terminal.CommandExecutor;
import dev.wolveringer.dataserver.terminal.ConsoleWriter;
import dev.wolveringer.serverbalancer.AcardeManager;

public class CommandServerManager implements CommandExecutor {

	public CommandServerManager() {
		options.addOption("st", "subtype", true, "Subtype parttern");
		options.addOption("t", "type", true, "Server type");
		options.addOption("l", "inlobby", false, "List server only in lobby");
	}

	@Override
	public void onCommand(String command, ConsoleWriter writer, String[] args) {
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("help")) {
				for(String s : getArguments())
					writer.sendMessage(s);
			}
		}
		if(args.length >= 1){
			if(args[0].equalsIgnoreCase("list")){
				CommandLine cmdArgs = paradiseOptions(args, 1, true);
				if (cmdArgs == null)
					return;
				if (ClientType.valueOf(cmdArgs.getOptionValue("type", ClientType.ALL + "")) == null) {
					writer.sendMessage("§cClientType not found!");
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
							return t.getStatus().getTyp() == type;
						}
					});
				}
				if (cmdArgs.hasOption("subtype")) {
					String subType = cmdArgs.getOptionValue("subtype");
					clients = clients.filter(new Predicate<Client>() {
						@Override
						public boolean test(Client t) {
							return t.getStatus().getSubType().matches(subType);
						}
					});
				}
				if (cmdArgs.hasOption("inlobby"))
					clients = clients.filter(new Predicate<Client>() {
						@Override
						public boolean test(Client t) {
							return t.getStatus().getState() == GameState.LobbyPhase;
						}
					});
				Iterator<Client> iclients = clients.iterator();
				if (iclients.hasNext()) {
					writer.sendMessage("§aServers:");
					while (iclients.hasNext()) {
						Client c = iclients.next();
						writer.sendMessage(" §7- §a" + c.getName() + "§r§7[§b" + c.getStatus().getSubType() + "§7] §eServer-ID: §6"+c.getStatus().getServerId()+" §eType: §6" + c.getType() + " §eGame: §6" + c.getStatus().getTyp() + " §eState: §6" + c.getStatus().getState() + " §ePlayers: " + c.getStatus().getPlayers()+" §ePublic: §6"+c.getStatus().isVisiable());
					}
				} else
					writer.sendMessage("§cEs wurden keine Server unter diesem Parameter gefunden.");
			}
			else if(args[0].equalsIgnoreCase("printLobbies")){
				AcardeManager.writeServers();
			}
		}
		if(args.length == 2){
			if(args[0].equalsIgnoreCase("info")){
				Client client = ServerThread.getServer(args[1]);
				if(client == null)
					writer.sendMessage("§cClient not found!");
				else
				{
					writer.sendMessage("§aServerinformationen für den Server: "+client.getName());
					writer.sendMessage("  §aHost: §b"+client.getHost());
					writer.sendMessage("  §aPing: §b"+client.getPing());
					writer.sendMessage("  §aType: §b"+client.getType());
					if(client.getType() != ClientType.BUNGEECORD){
						writer.sendMessage("  §aGamemode: §b"+client.getStatus().getTyp());
						writer.sendMessage("  §aSub-type: §b"+client.getStatus().getSubType());
						writer.sendMessage("  §aState: §b"+client.getStatus().getState());
					}
					writer.sendMessage("  §aSpieler: §b"+client.getStatus().getPlayers()+"/"+client.getStatus().getPlayers());
					writer.sendMessage("  §aVisiable: §b"+client.getStatus().isVisiable());
				}
			}
		}
		if(args.length >= 2){
			if(args[0].equalsIgnoreCase("switch")){
				Client client = ServerThread.getServer(args[1]);
				if(client == null)
					writer.sendMessage("§cClient not found!");
				else
				{
					if(client.getType() != ClientType.ACARDE){
						writer.sendMessage("§cClient isnt a Arcade Server!");
						return;
					}
					GameType type = GameType.valueOf(args[2]);
					String subType = args.length == 4 ? args[3] : "NONE";
					if(type == null){
						writer.sendMessage("§cGameType not found");
						return;
					}
					if(client.getStatus().getTyp() == type && client.getStatus().getSubType().equalsIgnoreCase(subType)){
						writer.sendMessage("§aServer ist schon in diesem zustand!");
						return;
					}
					client.setGame(type, subType);
					writer.sendMessage("§aDer Game-type wurde geändert.");
				}
			}
		}
		if(args.length == 3){
			if(args[0].equalsIgnoreCase("setVisiabe")){
				Client client = ServerThread.getServer(args[1]);
				if(client == null)
					writer.sendMessage("§cClient not found!");
				else
				{
					boolean flag = Boolean.getBoolean(args[2]);
					client.getStatus().setVisiable(flag);
					writer.sendMessage("§aDu hast die sichtbarkeit geändert.");
				}
			}
		}
	}

	@Override
	public String[] getArguments() {
		ArrayList<String> list = new ArrayList<>();
		list.add("§a/smanager list [-subtype <pattern> | -gametype <gametype> | -type <clientype> | -inlobby]");
		list.add("§a/smanager info <ClientName>");
		list.add("§a/smanager switch <ClientName> <GameType> [<SubType>]");
		list.add("§a/smanager setVisiabe <ClientName> <flag>");
		list.add("§a/smanager printLobbies");
		return list.toArray(new String[0]);
	}

}
