package dev.wolveringer.dataserver.terminal;

import java.io.IOException;
import java.util.Arrays;
import java.util.Stack;

import org.fusesource.jansi.AnsiConsole;

import jline.console.ConsoleReader;
import jline.console.CursorBuffer;
import jline.internal.Ansi;

public class Terminal {
	private ConsoleReader console;
	private ConsoleWriter writer;
	private Thread reader;
	private boolean active = true;
	private String message;

	public void init() {
		AnsiConsole.systemInstall();
		System.setOut(new CostumSystemPrintStream());
		writer = new ConsoleWriter(this);
		initReader();
	}

	private void initReader() {
		try {
			console = new ConsoleReader(System.in, AnsiConsole.out());
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (reader == null)
			reader = new Thread() {
				@Override
				public void run() {
					while (this.isAlive()) {
						if (!active) {
							try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							continue;
						}
						try {
							try{
								if(console.getCursorBuffer() == null){
									throw new Exception();
								}
								StringBuilder b = new StringBuilder();
								b.append(console.getCursorBuffer());
								b = null;
							}catch(Exception e){
								try{
									console.getCursorBuffer().buffer.delete(0, console.getCursorBuffer().buffer.length());
								}catch(Exception ex){
									try{
										CursorBuffer buffer = console.getCursorBuffer();
										buffer.getClass().getField("buffer").setAccessible(true);
										buffer.getClass().getField("buffer").set(buffer, new StringBuffer());
										buffer.cursor = 0;
									}catch(Exception exx){
										exx.printStackTrace();
									}
									System.out.println("Hard buffer reset!");
								}
							}
							String in = console.readLine(getPromt());
							if ("".equalsIgnoreCase(in))
								continue;
							String command = in.split(" ")[0];
							String[] args = new String[0];
							if (in.split(" ").length > 1)
								args = Arrays.copyOfRange(in.split(" "), 1, in.split(" ").length);
							CommandRegistry.runCommand(command, args, writer);
						} catch (Exception e) {
							writer.write("§cAn error happend:");
							e.printStackTrace();
						}
					}
				}
			};
		reader.start();
	}

	public void uninstall(){
		console.shutdown();
		reader.interrupt();
		AnsiConsole.systemUninstall();
	}
	
	public void lock() {
		lock(null);
	}

	private String getPromt() {
		String prefix = "";
		prefix += "§a> §o";
		return prefix;
	}

	public ConsoleWriter getConsolenWriter() {
		return writer;
	}

	public ConsoleReader getConsolenReader() {
		return console;
	}
	
	protected void write(String message) {
		try {
			String promt = "";
			String input_message = "";
			int cursor = 0;
			if (!active) {
				promt = ChatColor.toAnsiFormat(this.message);
				cursor = promt.length();
			} else {
				input_message = console.getCursorBuffer().toString();
				promt = "\r" + getPromt();
				cursor = console.getCursorBuffer().cursor;
			}
			//console.resetPromptLine("", "", 0);
			while (Ansi.stripAnsi(ChatColor.stripColor(message)).length() < input_message.length()) {
				message = message + " ";
			}
			AnsiConsole.out.println("\r"+ChatColor.toAnsiFormat(message));
			console.resetPromptLine(ChatColor.toAnsiFormat(promt), input_message, cursor);
		} catch (Exception e) {
		}
	}

	public void lock(String message) {
		active = false;
		if (message == null)
			message = "";
		try {
			console.killLine();
			this.message = ChatColor.toAnsiFormat(message);
			console.resetPromptLine(this.message, "", this.message.length());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void unlock() {
		active = true;
		try {
			console.killLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
/*

package dev.wolveringer.CloudSystem.terminal;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fusesource.jansi.AnsiConsole;

import dev.wolveringer.CloudSystem.log.CloudLogger;
import dev.wolveringer.CloudSystem.log.LoggingOutputStream;
import dev.wolveringer.CloudSystem.system.server.CloudServer;
import jline.console.ConsoleReader;

public class Terminal {
	private ConsoleReader console;
	private ConsoleWriter writer;
	private Thread reader;
	private boolean active = true;
	private String message;
	private CloudServer attach = null;
	private Logger logger;
	public void init() {
		AnsiConsole.systemInstall();
		
		writer = new ConsoleWriter(this);
		initReader();
	    
		this.logger = new CloudLogger();
	    System.setErr(new PrintStream(new LoggingOutputStream(this.logger, Level.SEVERE), true));
	    System.setOut(new PrintStream(new LoggingOutputStream(this.logger, Level.INFO), true));
		
	}

	private void initReader() {
		try {
			console = new ConsoleReader(System.in, AnsiConsole.out());
			console.setExpandEvents(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (reader == null)
			reader = new Thread() {
				@Override
				public void run() {
					while (this.isAlive()) {
						if (!active) {
							try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							continue;
						}
						try {
							String in = console.readLine(getPromt());
							if ("".equalsIgnoreCase(in))
								continue;
							String command = in.split(" ")[0];
							if (attach != null) {
								if (command.equalsIgnoreCase("exit")) {
									setServerConsole(null);
									return;
								} else
									attach.getRunner().getConsole().runCommand(in);
								continue;
							}
							String[] args = new String[0];
							if (in.split(" ").length > 1)
								args = Arrays.copyOfRange(in.split(" "), 1, in.split(" ").length);
							CommandRegistry.runCommand(command, args, writer);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			};
		reader.start();
	}

	public void lock() {
		lock(null);
	}

	private String getPromt() {
		String prefix = "";
		if (attach != null)
			prefix += attach.getHost() + ":" + attach.getPort() + "> ";
		else
			prefix += "System> ";
		return prefix;
	}

	public ConsoleWriter getConsolenWriter() {
		return writer;
	}

	public ConsoleReader getConsolenReader() {
		return console;
	}

	public void setServerConsole(CloudServer server) {
		if(attach != null)
			attach.getRunner().getConsole().setAttach(false);
		if (server != null){
			server.getRunner().getConsole().setAttach(true);
		}else {
			this.attach = null;
			writer.clear();
			for(int i = 0;i<100;i++)
				write("");
			writer.write("§aConsole: Cloud System");
		}
		this.attach = server;
	}

	protected void write(String message) {
		logger.info(message);
		/*
		synchronized (console){
			try {
				/*
				String promt = "";
				String input_message = "";
				if (!active) {
					input_message = this.message;
				} else {
					input_message = console.getCursorBuffer().toString();
					promt = getPromt();
				}
				AnsiConsole.out.println("\r"+ChatColor.toAnsiFormat(message));
				console.resetPromptLine("", input_message,0);
				//console.resetPromptLine(attach == null?"":promt, input_message, input_message.length());
				console.print('\r' + ChatColor.toAnsiFormat(message) + org.fusesource.jansi.Ansi.ansi().reset().toString()+'\n');
				console.drawLine();
				console.flush();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void lock(String message) {
		active = false;
		if (message == null)
			message = "";
		this.message = message;
		try {
			console.killLine();
			this.message = ChatColor.toAnsiFormat(message);
			console.resetPromptLine("", this.message, ChatColor.stripColor(this.message).length());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void unlock() {
		active = true;
		try{
			console.killLine();
		}catch (IOException e){
			e.printStackTrace();
		}
	}
	public Logger getLogger() {
		return logger;
	}
}
*/