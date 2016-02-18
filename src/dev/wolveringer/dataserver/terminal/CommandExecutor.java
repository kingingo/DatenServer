package dev.wolveringer.dataserver.terminal;

public interface CommandExecutor {
	public void onCommand(String command,ConsoleWriter writer,String[] args);
	public String[] getArguments();
}
