package dev.wolveringer.dataserver.terminal.commands;

import java.io.File;
import java.io.IOException;

import dev.wolveringer.dataserver.Main;
import dev.wolveringer.dataserver.player.PlayerManager;
import dev.wolveringer.dataserver.terminal.CommandExecutor;
import dev.wolveringer.dataserver.terminal.ConsoleWriter;

public class CommandRestart implements CommandExecutor {

	@Override
	public void onCommand(String command, ConsoleWriter writer, String[] args) {
		Main.stop(true);
	}

	@Override
	public String[] getArguments() {
		return new String[] { "/restart jvm | restart Java Virtual Machine" };
	}

}
