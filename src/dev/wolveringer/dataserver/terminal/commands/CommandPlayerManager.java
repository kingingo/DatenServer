package dev.wolveringer.dataserver.terminal.commands;

import dev.wolveringer.dataserver.terminal.ArgumentList;
import dev.wolveringer.dataserver.terminal.CommandExecutor;
import dev.wolveringer.dataserver.terminal.ConsoleWriter;
import dev.wolveringer.dataserver.terminal.ArgumentList.Argument;

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
