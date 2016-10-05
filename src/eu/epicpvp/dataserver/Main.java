package eu.epicpvp.dataserver;

import java.io.IOException;
import java.util.logging.Logger;

import eu.epicpvp.autorestart.RestartTimer;
import eu.epicpvp.configuration.ServerConfiguration;
import eu.epicpvp.connection.server.ServerThread;
import eu.epicpvp.converter.Converter;
import eu.epicpvp.dataserver.ban.BanManager;
import eu.epicpvp.dataserver.gamestats.StatsManager;
import eu.epicpvp.dataserver.gamestats.TopStatsManager;
import eu.epicpvp.dataserver.player.PlayerManager;
import eu.epicpvp.dataserver.player.PlayerSkinManager;
import eu.epicpvp.dataserver.protocoll.packets.Packet;
import eu.epicpvp.dataserver.save.SaveManager;
import eu.epicpvp.dataserver.terminal.ConsoleWriter;
import eu.epicpvp.dataserver.terminal.Terminal;
import eu.epicpvp.doublecoins.BoosterManager;
import eu.epicpvp.gild.GildenManager;
import eu.epicpvp.language.LanguageManager;
import eu.epicpvp.log.SystemLogger;
import eu.epicpvp.mysql.MySQL;
import eu.epicpvp.report.ReportManager;
import eu.epicpvp.teamspeak.TeamspeakClient;
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
		
		if(ServerConfiguration.isTeamspeakBotEnabled()){
			TeamspeakClient.setInstance(ServerConfiguration.createClient());
			if(TeamspeakClient.getInstance() == null){
				getConsoleWriter().sendMessage("§cTeamspeak client cant connect!");
				return;
			}
			getConsoleWriter().sendMessage("§aTeamspeak client connected!");
		}
		else
			getConsoleWriter().sendMessage("§6Teamspeak client is disabled!");
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
