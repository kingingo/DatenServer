package dev.wolveringer.dataserver.terminal;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.logging.Level;

import org.fusesource.jansi.AnsiConsole;

import dev.wolveringer.dataserver.Main;
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
		final PrintStream defaultErr = System.err;
		System.setErr(new CostumSystemPrintStream(){
			@Override
			public void write(String message) {
				Main.logger.log(Level.WARNING, message);
				Terminal.this.write("§c[ERROR] §6"+message);
			}
		});
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
								if(console.getCursorBuffer() == null){}
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
										buffer.getClass().getField("buffer").set(buffer, new StringBuilder());
										buffer.cursor = 0;
									}catch(Exception exx){
										exx.printStackTrace();
									}
									write("§cHard buffer reset!");
								}
							}
							String in = console.readLine();
							if ("".equalsIgnoreCase(in))
								continue;
							String command = in.split(" ")[0];
							String[] args = new String[0];
							if (in.split(" ").length > 1)
								args = Arrays.copyOfRange(in.split(" "), 1, in.split(" ").length);
							if(args.length==1 && args[0].equalsIgnoreCase("help")){
								CommandRegistry.help(command, writer);
							}
							else
							CommandRegistry.runCommand(command, args, writer);
						} catch (Exception e) {
							writer.write("§cAn error happend while performing this command:");
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
	
	protected synchronized void write(String message) {
		if(message == null || message.length() == 0)
			return;
		if(message.split("\n").length > 1){
			for(String s : message.split("\n"))
				write(s);
			return;
		}
		try {
			Main.logger.log(Level.INFO, message);
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
			while (Ansi.stripAnsi(ChatColor.stripColor(message)).length() < input_message.length()+Ansi.stripAnsi(ChatColor.stripColor(promt)).length()) {
				message = message + " ";
			}
			AnsiConsole.out.println("\r"+ChatColor.toAnsiFormat(message));
			console.resetPromptLine(ChatColor.toAnsiFormat(promt), Ansi.stripAnsi(input_message), cursor);
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