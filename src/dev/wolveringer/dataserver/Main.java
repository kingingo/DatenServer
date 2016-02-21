package dev.wolveringer.dataserver;

import java.io.IOException;
import java.net.InetSocketAddress;

import dev.wolveringer.connection.server.ServerThread;
import dev.wolveringer.dataserver.ban.BanManager;
import dev.wolveringer.dataserver.save.SaveManager;
import dev.wolveringer.dataserver.terminal.ConsoleWriter;
import dev.wolveringer.dataserver.terminal.Terminal;
import dev.wolveringer.dataserver.uuid.UUIDManager;
import dev.wolveringer.mysql.MySQL;

public class Main {
	public static byte[] Password = "HelloWorld".getBytes();
	private static Terminal terminal;
	
	public static void main(String[] args) throws InterruptedException, IOException {
		//terminal = new Terminal();
		//terminal.init();
		
		getConsoleWriter().write("§aHello world");
		System.out.println("§bHello world");
		
		
		//TODO init MySQL
		MySQL.setInstance(new MySQL("148.251.143.2", "3306", "games", "root", "55P_YHmK8MXlPiqEpGKuH_5WVlhsXT"));
		UUIDManager.init();
		BanManager.setManager(new BanManager());
		SaveManager.setSaveManager(new SaveManager().start());
		System.out.println("Server started");
		ServerThread server = new ServerThread(new InetSocketAddress("localhost", 1111));
		server.start();
		
		while (true) {
			Thread.sleep(1000000);
		}
	}

	public static ConsoleWriter getConsoleWriter() {
		return new ConsoleWriter(null){
			@Override
			public void write(String string) {
				System.out.println(string);
			}
		};
		//return terminal.getConsolenWriter();
	}
}
