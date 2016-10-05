package eu.epicpvp.dataserver.terminal.commands;

import org.apache.commons.lang3.StringUtils;

import eu.epicpvp.dataserver.terminal.ArgumentList;
import eu.epicpvp.dataserver.terminal.ArgumentList.Argument;
import eu.epicpvp.dataserver.terminal.CommandExecutor;
import eu.epicpvp.dataserver.terminal.CommandRegistry;
import eu.epicpvp.dataserver.terminal.CommandRegistry.CommandHolder;
import eu.epicpvp.dataserver.terminal.ConsoleWriter;

public class CommandHelp implements CommandExecutor {

	@Override
	public void onCommand(String command, ConsoleWriter writer, String[] args) {
		writer.write("§aCommands: ");
		for (CommandHolder e : CommandRegistry.getCommands()) {
			writer.write("  §7- §a/" + e.getCommand() + (e.getAlias().isEmpty() ? "" : " §7| §a/" + StringUtils.join(e.getAlias(), " §7| §a/")));
		}
	}

	@Override
	public ArgumentList getArguments() {
		return ArgumentList.builder().arg(new Argument("/help", "Shows this help page.")).build();
	}

}
