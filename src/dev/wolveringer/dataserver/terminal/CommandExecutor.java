package dev.wolveringer.dataserver.terminal;

import java.io.PrintWriter;
import java.util.Arrays;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import dev.wolveringer.dataserver.Main;
import jline.TerminalFactory;

public interface CommandExecutor {
	Options options = new Options();
	CommandLineParser optionsParser = new BasicParser();
	HelpFormatter optionsHelper = new HelpFormatter();
	
	public void onCommand(String command,ConsoleWriter writer,String[] args);
	public String[] getArguments();
	
	public default CommandLine paradiseOptions(String[] args,int start){
		return paradiseOptions(args, start,true);
	}
	public default CommandLine paradiseOptions(String[] args,int start,boolean sendHelp){
		try {
			CommandLine line = optionsParser.parse(options, Arrays.copyOfRange(args, start, args.length));
			if(line == null)
				throw new Exception("line == null");
			return line;
		} catch (Exception e) {
			if(sendHelp){
				Main.getConsoleWriter().write("§cException: "+e.getMessage());
				optionsHelper.printUsage(new PrintWriter(new CostumSystemPrintStream()), TerminalFactory.get().getWidth(), "Command Help", options);
			}
		}
		return null;
	}
}
