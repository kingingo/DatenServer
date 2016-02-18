package dev.wolveringer.dataserver;

import dev.wolveringer.dataserver.terminal.ConsoleWriter;
import dev.wolveringer.dataserver.terminal.Terminal;

public class Main {
	public static byte[] Password = "HelloWorld".getBytes();
	private static Terminal terminal;
	
	public static void main(String[] args) throws InterruptedException {
		terminal = new Terminal();
		terminal.init();
		
		getConsoleWriter().write("§aHello world");
		System.out.println("§bHello world");
		
		while (true) {
			Thread.sleep(1000000);
		}
	}

	public static ConsoleWriter getConsoleWriter() {
		return terminal.getConsolenWriter();
	}
}
