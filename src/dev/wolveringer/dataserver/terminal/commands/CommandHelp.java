package dev.wolveringer.dataserver.terminal.commands;

import org.apache.commons.lang3.StringUtils;

import dev.wolveringer.dataserver.terminal.ArgumentList;
import dev.wolveringer.dataserver.terminal.CommandExecutor;
import dev.wolveringer.dataserver.terminal.CommandRegistry;
import dev.wolveringer.dataserver.terminal.ConsoleWriter;
import dev.wolveringer.dataserver.terminal.ArgumentList.Argument;
import dev.wolveringer.dataserver.terminal.CommandRegistry.CommandHolder;

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
