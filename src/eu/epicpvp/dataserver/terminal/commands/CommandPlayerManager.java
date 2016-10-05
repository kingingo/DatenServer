package eu.epicpvp.dataserver.terminal.commands;

import eu.epicpvp.dataserver.terminal.ArgumentList;
import eu.epicpvp.dataserver.terminal.ArgumentList.Argument;
import eu.epicpvp.dataserver.terminal.CommandExecutor;
import eu.epicpvp.dataserver.terminal.ConsoleWriter;

public class CommandPlayerManager implements CommandExecutor{

	@Override
	public void onCommand(String command, ConsoleWriter writer, String[] args) {
		writer.sendMessage("§ccomming soon");
	}

	@Override
	public ArgumentList getArguments() {
		return ArgumentList.builder().arg(new Argument("/playermanager", "Comming soon")).build();
	}

}
