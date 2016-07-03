package dev.wolveringer.dataserver;

import java.io.IOException;
import java.util.logging.Logger;

import dev.wolveringer.autorestart.RestartTimer;
import dev.wolveringer.configuration.ServerConfiguration;
import dev.wolveringer.connection.server.ServerThread;
import dev.wolveringer.converter.Converter;
import dev.wolveringer.dataserver.ban.BanManager;
import dev.wolveringer.dataserver.gamestats.StatsManager;
import dev.wolveringer.dataserver.gamestats.TopStatsManager;
import dev.wolveringer.dataserver.player.PlayerManager;
import dev.wolveringer.dataserver.player.PlayerSkinManager;
import dev.wolveringer.dataserver.protocoll.packets.Packet;
import dev.wolveringer.dataserver.save.SaveManager;
import dev.wolveringer.dataserver.terminal.ConsoleWriter;
import dev.wolveringer.dataserver.terminal.Terminal;
import dev.wolveringer.doublecoins.BoosterManager;
import dev.wolveringer.gild.GildenManager;
import dev.wolveringer.language.LanguageManager;
import dev.wolveringer.log.SystemLogger;
import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.report.ReportManager;
import lombok.Getter;

public class Main {
	public static Logger logger;
	@Getter
	private static Terminal terminal;
	
	private static boolean supportTerminal = true;
	
	@Getter
	private static ServerThread server;
	private static RestartTimer restarter;
	
	public static void main(String[] args) throws InterruptedException, IOException {
		if(args.length == 1){
			if(args[0].equalsIgnoreCase("convertAll")){
				System.out.println("Switching to convert mode!");
				Converter.convert();
				return;
			}
			else if(args[0].equalsIgnoreCase("wait")){
				Thread.sleep(5*1000);
			}
		}
		System.out.println("Datenserver protocoll version: "+Packet.PROTOCOLL_VERSION);
		if(supportTerminal){
			System.out.println("Setting up Terminal");
			terminal = new Terminal();
			terminal.init();
		}
		
		logger = new SystemLogger();
		getConsoleWriter().write("§aSetting up DatenServer");
		
		
		//TODO init MySQL
		ServerConfiguration.init();
		MySQL.setInstance(new MySQL(ServerConfiguration.getMySQLConfiguration()));
		if(!MySQL.getInstance().isMySQLSupported()){
			getConsoleWriter().sendMessage("§cMySQL is not supported");
			return;
		}
		if(!MySQL.getInstance().connect()){
			getConsoleWriter().sendMessage("§cCannot connect to MySQL");
			return;
		}

		LanguageManager.init();
		PlayerSkinManager.init();
		BanManager.setManager(new BanManager());
		BanManager.getManager().loadBans();
		StatsManager.initTables();
		TopStatsManager.setManager(new TopStatsManager());
		SaveManager.setSaveManager(new SaveManager().start());
		ReportManager.setInstance(new ReportManager());
		BoosterManager.setManager(new BoosterManager());
		ReportManager.getInstance().load();
		GildenManager.setManager(new GildenManager());
		
		TickScheduler s = new TickScheduler();
		s.start();
		restarter = new RestartTimer(3, 0, 0);
		restarter.startListening();
		server = new ServerThread(ServerConfiguration.getServerHost());
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
	
	public static void stop(boolean restart){
		Main.getConsoleWriter().sendMessage("§cStopping server!");
		Main.getTerminal().lock("§cShutting down...");
		Main.getServer().stop();
		PlayerManager.unloadAll();
		MySQL.getInstance().getEventLoop().waitForAll();
		Main.getTerminal().unlock();
		Main.getTerminal().uninstall();
		System.exit(-1);
	}
}
