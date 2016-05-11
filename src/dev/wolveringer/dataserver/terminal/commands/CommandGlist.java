package dev.wolveringer.dataserver.terminal.commands;

import dev.wolveringer.dataserver.player.PlayerManager;
import dev.wolveringer.dataserver.terminal.ArgumentList;
import dev.wolveringer.dataserver.terminal.CommandExecutor;
import dev.wolveringer.dataserver.terminal.ConsoleWriter;
import dev.wolveringer.dataserver.terminal.ArgumentList.Argument;

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
	public ArgumentList getArguments() {
		return ArgumentList.builder().arg(new Argument("/glist", "Show all players")).build();
	}

}
