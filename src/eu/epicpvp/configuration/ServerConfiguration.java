package eu.epicpvp.configuration;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.github.theholywaffle.teamspeak3.TS3Config;

import eu.epicpvp.configuration.file.YamlConfiguration;
import eu.epicpvp.mysql.MySQL.MySQLConfiguration;
import eu.epicpvp.teamspeak.TeamspeakClient;

public class ServerConfiguration {
	public static Configuration config;
	private static MySQLConfiguration MySQLConfig;
	
	public static void init() {
		if (!new File("config.yml").exists())
			try {
				new File("config.yml").createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		config = YamlConfiguration.loadConfiguration(new File("config.yml"));
		config.addDefault("MySQL.host", "localhost");
		config.addDefault("MySQL.port", 3306);
		config.addDefault("MySQL.datenbank", "test");
		config.addDefault("MySQL.user", "root");
		config.addDefault("MySQL.password", "undefined");
		config.addDefault("MySQL.autoReconnect", true);
		
		config.addDefault("server.port", 1111);
		config.addDefault("server.host", "localhost");
		config.addDefault("server.password", "HelloWorld");
		
		config.addDefault("teamspeak.host", "localhost");
		config.addDefault("teamspeak.port", 10011);
		config.addDefault("teamspeak.nickname", "WolverinDEV");
		config.addDefault("teamspeak.username", "serveradmin");
		config.addDefault("teamspeak.password", "undefined");
		config.addDefault("teamspeak.serverId", 1);
		config.addDefault("teamspeak.groups.linked", 5);
		config.addDefault("teamspeak.groups.ignore", Arrays.asList(7,8,9));
		config.addDefault("teamspeak.groups.mapping", Arrays.asList("owner|22","developer|23"));
		config.addDefault("teamspeak.enabled", true);
		
		config.addDefault("savemanager.periode", 5*60*1000);
		
		config.options().copyDefaults(true);
		config.save();
	}

	public static MySQLConfiguration getMySQLConfiguration() {
		if(MySQLConfig == null){
			MySQLConfig = new MySQLConfiguration(config.getString("MySQL.host"), config.getInt("MySQL.port"), config.getString("MySQL.datenbank"), config.getString("MySQL.user"), config.getString("MySQL.password"), config.getBoolean("MySQL.autoReconnect"),10);
		}
		return MySQLConfig;
	}

	public static String getServerPassword(){
		return config.getString("server.password");
	}
	
	public static int getSaveManagerPeriode(){
		return config.getInt("savemanager.periode");
	}
	
	public static InetSocketAddress getServerHost() {
		return new InetSocketAddress(config.getString("server.host"), config.getInt("server.port"));
	}
	
	public static boolean isTeamspeakBotEnabled(){
		return config.getBoolean("teamspeak.enabled");
	}
	
	public static TeamspeakClient createClient(){
		if(!isTeamspeakBotEnabled())
			return null;
		try{
			TeamspeakClient client = new TeamspeakClient(new TS3Config().setHost(config.getString("teamspeak.host")).setQueryPort(config.getInt("teamspeak.port")));
			client.connect();
			if(client.login(config.getString("teamspeak.username"), config.getString("teamspeak.password"), config.getInt("teamspeak.serverId"))){
				client.setName(config.getString("teamspeak.nickname"));
				return client;
			}
			client.getClient().exit();
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static List<Integer> getIgnoreGroup(){
		return config.getIntegerList("teamspeak.groups.ignore");
	}
	
	public static HashMap<String, Integer> getGroupMapping(){
		HashMap<String, Integer> out = new HashMap<>();
		for(String s : config.getStringList("teamspeak.groups.mapping"))
			out.put(s.split("\\|")[0], Integer.parseInt(s.split("\\|")[1]));
		return out;
	}
	
	public static int getTeamspeakLinkedGroupId(){
		return config.getInt("teamspeak.groups.linked");
	}
}
