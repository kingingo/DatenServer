package dev.wolveringer.dataserver.terminal;

import java.util.Collection;
import java.util.HashMap;

import dev.wolveringer.dataserver.terminal.commands.CommandHelp;
import dev.wolveringer.dataserver.terminal.commands.CommandPlayerManager;
import dev.wolveringer.dataserver.terminal.commands.CommandServerManager;
import dev.wolveringer.dataserver.terminal.commands.CommandStop;

public class CommandRegistry {
	static {
		cmds = new HashMap<String, CommandExecutor>();
		
		registerCommand(new CommandHelp(), "help");
		registerCommand(new CommandServerManager(), "smanager");
		registerCommand(new CommandPlayerManager(), "pmanager");
		registerCommand(new CommandStop(), "stop");
		//registerCommand(new CMD_TEST(), "test");
		//registerCommand(new CMD_LISTSERVER(), "list");
		//registerCommand(new CMD_ATTACH(), "attach");
		//registerCommand(new CMD_CREATESERVER(), "createserver");
		//registerCommand(new CMD_CONNECT(), "connect");
	}
	
	private static HashMap<String, CommandExecutor> cmds;

	public static void registerCommand(CommandExecutor cmd, String... commands) {
		for(String command : commands)
			if(command != null && cmd != null)
			cmds.put(command, cmd);
	}

	public static void runCommand(String command,String[] args,ConsoleWriter writer){
		if(cmds.get(command) != null)
			cmds.get(command).onCommand(command, writer, args);
		else
			writer.write("§cCommand not found. help for more informations");
	}

	public static Collection<CommandExecutor> getCommands() {
		return cmds.values();
	}
}
