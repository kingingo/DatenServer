package dev.wolveringer.dataserver.terminal.commands;

import dev.wolveringer.dataserver.terminal.CommandExecutor;
import dev.wolveringer.dataserver.terminal.CommandRegistry;
import dev.wolveringer.dataserver.terminal.ConsoleWriter;

public class CommandHelp implements CommandExecutor{

	@Override
	public void onCommand(String command, ConsoleWriter writer, String[] args) {
		writer.write("§fCommands: ");
		for(CommandExecutor e : CommandRegistry.getCommands()){
			if(e.getArguments() != null)
				for(String s : e.getArguments())
					writer.write(" "+s);
			else
				System.out.println("No args found for: "+e.getClass().getName());
		}
	}

	@Override
	public String[] getArguments() {
		return new String[0];
	}
	
}
