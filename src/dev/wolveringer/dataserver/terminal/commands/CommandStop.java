package dev.wolveringer.dataserver.terminal.commands;

import dev.wolveringer.dataserver.Main;
import dev.wolveringer.dataserver.player.PlayerManager;
import dev.wolveringer.dataserver.terminal.CommandExecutor;
import dev.wolveringer.dataserver.terminal.ConsoleWriter;

public class CommandStop implements CommandExecutor{

	@Override
	public void onCommand(String command, ConsoleWriter writer, String[] args) {
		writer.sendMessage("§cStopping server!");
		Main.getTerminal().lock("§cShutting down...");
		Main.getServer().stop();
		PlayerManager.unloadAll();
		Main.getTerminal().unlock();
		Main.getTerminal().uninstall();
		System.exit(-1);
	}

	@Override
	public String[] getArguments() {
		return new String[]{"§c/stop"};
	}

}
