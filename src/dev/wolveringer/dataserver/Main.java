package dev.wolveringer.dataserver;

import java.io.IOException;
import java.net.InetSocketAddress;

import dev.wolveringer.connection.server.ServerThread;
import dev.wolveringer.dataserver.ban.BanManager;
import dev.wolveringer.dataserver.gamestats.StatsManager;
import dev.wolveringer.dataserver.save.SaveManager;
import dev.wolveringer.dataserver.terminal.ConsoleWriter;
import dev.wolveringer.dataserver.terminal.Terminal;
import dev.wolveringer.dataserver.uuid.UUIDManager;
import dev.wolveringer.mysql.MySQL;

public class Main {
	public static byte[] Password = "HelloWorld".getBytes();
	private static Terminal terminal;
	
	private static boolean supportTerminal = true;
	
	public static void main(String[] args) throws InterruptedException, IOException {
		if(supportTerminal){
			terminal = new Terminal();
			terminal.init();
		}
		
		getConsoleWriter().write("§aHello world");
		System.out.println("§bHello world");
		
		
		//TODO init MySQL
		MySQL.setInstance(new MySQL("148.251.143.2", "3306", "test", "root", "55P_YHmK8MXlPiqEpGKuH_5WVlhsXT"));
		UUIDManager.init();
		BanManager.setManager(new BanManager());
		StatsManager.initTables();
		SaveManager.setSaveManager(new SaveManager().start());
		TickSeduller s = new TickSeduller();
		s.start();
		System.out.println("Server started");
		ServerThread server = new ServerThread(new InetSocketAddress("localhost", 1111));
		server.start();
		
		while (true) {
			Thread.sleep(1000000);
		}
	}

	public static ConsoleWriter getConsoleWriter() {
		if(!supportTerminal)
			return new ConsoleWriter(null){
				@Override
				public void write(String string) {
					System.out.println(string);
				}
			};
		return terminal.getConsolenWriter();
	}
}
