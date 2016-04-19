package dev.wolveringer.configuration;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

import dev.wolveringer.configuration.file.YamlConfiguration;
import dev.wolveringer.mysql.MySQL.MySQLConfiguration;

public class ServerConfiguration {
	private static Configuration config;
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
}
