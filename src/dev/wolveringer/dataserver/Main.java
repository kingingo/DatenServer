package dev.wolveringer.dataserver;

import java.io.IOException;

import dev.wolveringer.configuration.ServerConfiguration;
import dev.wolveringer.connection.server.ServerThread;
import dev.wolveringer.dataserver.ban.BanManager;
import dev.wolveringer.dataserver.gamestats.MoneyConverter;
import dev.wolveringer.dataserver.gamestats.StatsManager;
import dev.wolveringer.dataserver.gamestats.TopStatsManager;
import dev.wolveringer.dataserver.player.PlayerSkinManager;
import dev.wolveringer.dataserver.protocoll.packets.Packet;
import dev.wolveringer.dataserver.save.SaveManager;
import dev.wolveringer.dataserver.terminal.ConsoleWriter;
import dev.wolveringer.dataserver.terminal.Terminal;
import dev.wolveringer.dataserver.uuid.UUIDManager;
import dev.wolveringer.language.LanguageManager;
import dev.wolveringer.mysql.MySQL;

public class Main {
	private static Terminal terminal;
	
	private static boolean supportTerminal = true;
	
	public static void main(String[] args) throws InterruptedException, IOException {
		if(args.length == 1){
			if(args[0].equalsIgnoreCase("convertMoney")){
				MoneyConverter.main(args);
				return;
			}
		}
		System.out.println("Datenserver protocoll version: "+Packet.PROTOCOLL_VERSION);
		if(supportTerminal){
			System.out.println("Setting up Terminal");
			terminal = new Terminal();
			terminal.init();
		}
		
		
		getConsoleWriter().write("§aSetting up DatenServer");
		
		
		//TODO init MySQL
		ServerConfiguration.init();
		MySQL.setInstance(new MySQL(ServerConfiguration.getMySQLConfiguration())); //new MySQL("148.251.143.2", "3306", "test", "root", "55P_YHmK8MXlPiqEpGKuH_5WVlhsXT")
		if(!MySQL.getInstance().isMySQLSupported()){
			getConsoleWriter().sendMessage("§cMySQL isnt supported");
			return;
		}
		if(!MySQL.getInstance().connect()){
			getConsoleWriter().sendMessage("§cCantconnect to MySQL");
			return;
		}
		UUIDManager.init();
		LanguageManager.init();
		PlayerSkinManager.init();
		BanManager.setManager(new BanManager());
		StatsManager.initTables();
		TopStatsManager.setManager(new TopStatsManager());
		SaveManager.setSaveManager(new SaveManager().start());
		TickSeduller s = new TickSeduller();
		s.start();
		ServerThread server = new ServerThread(ServerConfiguration.getServerHost());
		server.start();
		getConsoleWriter().write("§aSetting up done! Main-Thread -> Sleeping...");
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
