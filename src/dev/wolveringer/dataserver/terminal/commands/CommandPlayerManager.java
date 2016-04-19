package dev.wolveringer.dataserver.terminal.commands;

import dev.wolveringer.dataserver.terminal.CommandExecutor;
import dev.wolveringer.dataserver.terminal.ConsoleWriter;

public class CommandPlayerManager implements CommandExecutor{

	@Override
	public void onCommand(String command, ConsoleWriter writer, String[] args) {
		writer.sendMessage("§ccomming soon");
	}

	@Override
	public String[] getArguments() {
		return new String[]{"§a/playermanager - §cComming soon"};
	}

}
