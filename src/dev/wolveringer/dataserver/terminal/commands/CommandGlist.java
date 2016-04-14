package dev.wolveringer.dataserver.terminal.commands;

import java.util.List;

import dev.wolveringer.connection.server.ServerThread;
import dev.wolveringer.dataserver.player.PlayerManager;
import dev.wolveringer.dataserver.terminal.CommandExecutor;
import dev.wolveringer.dataserver.terminal.ConsoleWriter;

public class CommandGlist implements CommandExecutor{

	@Override
	public void onCommand(String command, ConsoleWriter writer, String[] args) {
		for(String player : PlayerManager.getPlayersFromServer(null))
			System.out.println("- "+player+" "+PlayerManager.getPlayer(player).getServer()+" "+PlayerManager.getPlayer(player).getPlayerBungeecord());
	}

	@Override
	public String[] getArguments() {
		// TODO Auto-generated method stub
		return null;
	}

}
