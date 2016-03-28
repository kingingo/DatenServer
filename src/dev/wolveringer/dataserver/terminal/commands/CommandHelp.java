package dev.wolveringer.dataserver.terminal.commands;

import dev.wolveringer.dataserver.terminal.CommandExecutor;
import dev.wolveringer.dataserver.terminal.ConsoleWriter;

public class CommandHelp implements CommandExecutor{

	@Override
	public void onCommand(String command, ConsoleWriter writer, String[] args) {
		writer.write("§aMomentan sind keine befehle möglich.");
	}

	@Override
	public String[] getArguments() {
		return new String[0];
	}
	
}
