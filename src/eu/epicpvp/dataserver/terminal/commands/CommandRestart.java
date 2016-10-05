package eu.epicpvp.dataserver.terminal.commands;

import eu.epicpvp.dataserver.Main;
import eu.epicpvp.dataserver.terminal.ArgumentList;
import eu.epicpvp.dataserver.terminal.ArgumentList.Argument;
import eu.epicpvp.dataserver.terminal.CommandExecutor;
import eu.epicpvp.dataserver.terminal.ConsoleWriter;

public class CommandRestart implements CommandExecutor {

	@Override
	public void onCommand(String command, ConsoleWriter writer, String[] args) {
		Main.stop(true);
	}

	@Override
	public ArgumentList getArguments() {
		return ArgumentList.builder().arg(new Argument("/restart", "Restart the server.")).build();
	}

}
