package dev.wolveringer.dataserver.terminal.commands;

import dev.wolveringer.dataserver.player.PlayerManager;
import dev.wolveringer.dataserver.terminal.CommandExecutor;
import dev.wolveringer.dataserver.terminal.ConsoleWriter;

public class CommandGlist implements CommandExecutor{

	@Override
	public void onCommand(String command, ConsoleWriter writer, String[] args) {
		writer.sendMessage("§aAll players on this Network:");
		for(String player : PlayerManager.getPlayersFromServer(null))
			writer.sendMessage("  §7- §a"+player+" §7-> §b"+PlayerManager.getPlayer(player).getPlayerBungeecord()+" §7-> §c"+PlayerManager.getPlayer(player).getServer());
		writer.sendMessage("§7----------------------------");
		writer.sendMessage("§aAll together: "+PlayerManager.getPlayersFromServer(null).size());
	}

	@Override
	public String[] getArguments() {
		return new String[]{"§a/glist §7- §aShow all players"};
	}

}
