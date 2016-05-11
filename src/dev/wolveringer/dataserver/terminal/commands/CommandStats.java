package dev.wolveringer.dataserver.terminal.commands;

import dev.wolveringer.dataserver.gamestats.GameType;
import dev.wolveringer.dataserver.gamestats.StatsKey;
import dev.wolveringer.dataserver.player.OnlinePlayer;
import dev.wolveringer.dataserver.player.PlayerManager;
import dev.wolveringer.dataserver.protocoll.packets.PacketInStatsEdit.Action;
import dev.wolveringer.dataserver.terminal.ArgumentList;
import dev.wolveringer.dataserver.terminal.CommandExecutor;
import dev.wolveringer.dataserver.terminal.ConsoleWriter;
import dev.wolveringer.dataserver.terminal.ArgumentList.Argument;
import dev.wolveringer.gamestats.Statistic;

public class CommandStats implements CommandExecutor{

	@Override
	public void onCommand(String command, ConsoleWriter writer, String[] args) {
		if(args.length == 6){
			if(args[0].equalsIgnoreCase("change")){
				OnlinePlayer player = PlayerManager.getPlayer(args[1]);
				if(player.getLoginPassword() == null || player.getLoginPassword().length()<3){
					writer.sendMessage("§cThis player never played on this network!");
					return;
				}
				GameType type = GameType.get(args[2]);
				if(type == null){
					writer.sendMessage("§cCant find gametype "+args[2]);
					return;
				}
			}
		}
		else if(args.length == 3){
			if(args[0].equalsIgnoreCase("info")){
				OnlinePlayer player = PlayerManager.getPlayer(args[1]);
				if(player.getLoginPassword() == null || player.getLoginPassword().length()<3){
					writer.sendMessage("§cThis player never played on this network!");
					return;
				}
				GameType type = GameType.get(args[2]);
				if(type == null){
					writer.sendMessage("§cCant find gametype "+args[2]);
					return;
				}
				writer.sendMessage("§aStatistic for player §e"+player.getName()+" in gamtype "+type.name());
				for(Statistic c : player.getStatsManager().getStats(type).getStats()){
					writer.sendMessage("§a"+c.getStatsKey().getMySQLName()+" §7-> §b"+c.getValue());
				}
				Action action = Action.valueOf(args[3].toUpperCase());
				if(action == null){
					writer.sendMessage("§cCant find action "+args[3]);
					return;
				}
				StatsKey key = StatsKey.valueOf(args[4].toUpperCase());
				if(key == null){
					writer.sendMessage("§cCant find StatsKey "+args[4]);
					return;
				}
				
			}
		}
	}

	@Override
	public ArgumentList getArguments() {
		return ArgumentList.builder().arg(new Argument("/smanager info <Playername> <Gametype>", "Show the statistik of a Gametype")).arg(new Argument("/smanager change <Playername> <GameType> <add/remove/set> <StatsKey> <Value>", "Change the statistik of a player")).build();
	}
	
	public static void main(String[] args) {
		System.out.println(Action.valueOf("add"));
	}
}
