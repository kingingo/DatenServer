package dev.wolveringer.dataserver.terminal;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.ArrayUtils;

import dev.wolveringer.dataserver.terminal.commands.CommandBroadcast;
import dev.wolveringer.dataserver.terminal.commands.CommandGlist;
import dev.wolveringer.dataserver.terminal.commands.CommandHelp;
import dev.wolveringer.dataserver.terminal.commands.CommandPlayerManager;
import dev.wolveringer.dataserver.terminal.commands.CommandRestart;
import dev.wolveringer.dataserver.terminal.commands.CommandSendMessage;
import dev.wolveringer.dataserver.terminal.commands.CommandServerManager;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class CommandRegistry {
	@AllArgsConstructor
	@Getter
	public static class CommandHolder {
		private String command;
		private List<String> alias;
		private CommandExecutor executor;
		
		public boolean accept(String is){
			return command.equalsIgnoreCase(is) || alias.contains(is.toLowerCase());
		}
	}
	static {
		cmds = new CopyOnWriteArrayList<CommandHolder>();
		
		registerCommand(new CommandHelp(), "help");
		registerCommand(new CommandServerManager(), "smanager");
		registerCommand(new CommandPlayerManager(), "pmanager");
		registerCommand(new CommandRestart(), "restart","stop");
		registerCommand(new CommandSendMessage(), "sendMessage");
		registerCommand(new CommandBroadcast(), "broadcast","bc");
		registerCommand(new CommandGlist(), "glist");
	}
	
	private static CopyOnWriteArrayList<CommandHolder> cmds;

	public static void registerCommand(CommandExecutor cmd, String... commands) {
		for(int i = 0;i<commands.length;i++)
			commands[i] = commands[i].toLowerCase();
		cmds.add(new CommandHolder(commands[0], Arrays.asList(ArrayUtils.subarray(commands, 1, commands.length)),cmd));
	}

	public static void runCommand(String command,String[] args,ConsoleWriter writer){
		for(CommandHolder h : cmds){
			if(h.accept(command)){
				h.executor.onCommand(command, writer, args);
				return;
			}
		}
		writer.write("§cCommand not found. help for more informations");
	}

	public static CopyOnWriteArrayList<CommandHolder> getCommands() {
		return cmds;
	}

	public static void help(String command, ConsoleWriter writer) {
		for(CommandHolder h : cmds){
			if(h.accept(command)){
				h.executor.printHelp(false);
				return;
			}
		}
		writer.write("§cCommand not found. help for more informations");
	}
}
