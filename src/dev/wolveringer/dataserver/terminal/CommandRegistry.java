package dev.wolveringer.dataserver.terminal;

import java.util.Collection;
import java.util.HashMap;

public class CommandRegistry {
	static {
		cmds = new HashMap<String, CommandExecutor>();
		
		//registerCommand(new CMD_HELP(), "help");
		//registerCommand(new CMD_STOP(), "stop");
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
