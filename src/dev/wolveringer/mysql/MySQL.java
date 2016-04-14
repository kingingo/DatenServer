package dev.wolveringer.mysql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import dev.wolveringer.threads.EventLoop;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class MySQL {
	private static MySQL instance;
	
	public static MySQL getInstance(){
		return instance;
	}
	
	public static interface ThreadFactory {
		public void createAsync(Runnable run);
		public void createAsync(Runnable run,EventLoop loop);
	}
	
	public static final EventLoop DEFAULT_LOOP = new EventLoop(100);
	
	private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
		@Override
		public void createAsync(Runnable run) {
			createAsync(run, DEFAULT_LOOP);
		}
		@Override
		public void createAsync(Runnable run,EventLoop loop) {
			loop.join(run);
			//BungeeCord.getInstance().getScheduler().runAsync(Main.getMain(), run);
		}
	};
	
	public static void setInstance(MySQL instance) {
		MySQL.instance = instance;
	}
	
	@AllArgsConstructor
	public static class MySQLConfiguration {
		private String host;
		private int port;
		private String database;
		private String user;
		private String password;
		private boolean autoReconnect;
		
		public boolean isValid(){
			if ((host == null) || (host == "")) { return false; }
			if ((port == 0)) { return false; }
			if ((database == null) || (database == "")) { return false; }
			if ((user == null) || (user == "")) { return false; }
			if ((password == null) || (password == "")) { return false; }
			return true;
		}
		
		public String buildURL(){
			return "jdbc:mysql://" + host + ":" + port + "/" + database + "?" + "user=" + user + "&" + "password=" + password + "&autoReconnect="+autoReconnect;
		}
	}
	
	private Connection conn = null;
	private MySQLConfiguration config;
	private boolean connect = false;
	@Getter
	@Setter
	private EventLoop eventLoop = DEFAULT_LOOP;
	@Getter
	private boolean MySQLSupported;
	
	public MySQL(MySQLConfiguration config) {
		if(!config.isValid())
			throw new RuntimeException("MySQL Configuration is not valid");
		try {
			Class.forName("com.mysql.jdbc.Driver"); //Load class
			MySQLSupported = true;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.err.println("MySQL Treiber nicht gefunden.");
			MySQLSupported = false;
		}
		this.config = config;
	}
	
	public Connection getConnectionInstance() {
		try {
			if (conn == null || conn.isClosed() || !conn.isValid(500)) {
				try {
					conn = DriverManager.getConnection(config.buildURL());
					connect = true;
				}
				catch (SQLException e) {
					System.out.println("Connect nicht moeglich");
					connect = false;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}
	
	public ArrayList<String[]> querySync(String select, int limit) {
		ArrayList<String[]> x = new ArrayList<String[]>();
		conn = getConnectionInstance();
		
		if (conn != null) {
			Statement query;
			try {
				query = conn.createStatement();
				String sql = select;
				ResultSet result = query.executeQuery(sql);
				int spaltenzahl = result.getMetaData().getColumnCount();
				spalteninhalt(limit == -1 ? Integer.MAX_VALUE : limit,spaltenzahl, result, x);
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return x;
	}
	
	@SuppressWarnings("unchecked")
	public void query(final String select, final int limit, final Callback<ArrayList<String[]>>... call) {
		THREAD_FACTORY.createAsync(new Runnable() {
			@Override
			public void run() {
				ArrayList<String[]> out = querySync(select, limit);
				for (Callback<ArrayList<String[]>> c : call)
					c.done(out, null);
			}
		},eventLoop);
	}
	
	private void spalteninhalt(int zeilenAnzahl,int spalten, ResultSet result, ArrayList<String[]> x) {
		try{
			int c = 0;
			while (result.next()) {
				if(c >= zeilenAnzahl)
					break;
				String[] temp = new String[spalten];
				for (int k = 1; k <= spalten; k++) {
					String y = result.getMetaData().getColumnName(k);
					try {
						String name = result.getString(y);
						temp[k - 1] = name;
					}
					catch (SQLException e) {
						temp[k - 1] = null;
					}
				}
				x.add(temp);
				c++;
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	private <T> String join(T[] array, String cement) {
		StringBuilder builder = new StringBuilder();
		
		if (array == null || array.length == 0) { return null; }
		
		for (T t : array) {
			builder.append(t).append(cement);
		}
		
		builder.delete(builder.length() - cement.length(), builder.length());
		
		return builder.toString();
	}
	
	@SuppressWarnings("unused")
	private void spaltenname(int spaltenzahl, ResultSet result, ArrayList<String> x) {
		String[] temp = new String[spaltenzahl];
		for (int k = 1; k <= spaltenzahl; k++) {
			String y;
			try {
				y = result.getMetaData().getColumnName(k);
				temp[k - 1] = y;
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		x.add(join(temp, "::"));
	}
	
	public void commandSync(String command) {
		conn = getConnectionInstance();
		if (conn != null) {
			try {
				String sql = command;
				PreparedStatement preparedStatement = conn.prepareStatement(sql);
				preparedStatement.executeUpdate();
			}
			catch (Exception e) {
				throw new RuntimeException("Command error while executing \""+command+"\":",e);
			}
		}
	}
	
	@SuppressWarnings({ "unchecked" })
	public void command(final String command, final Callback<Boolean>... call) {
		THREAD_FACTORY.createAsync(new Runnable() {
			public void run() {
				Exception ex = null;
				try {
					commandSync(command);
				}
				catch (RuntimeException e) {
					ex = e;
				}
				for (Callback<Boolean> c : call)
					c.done(ex == null, ex == null ? null : ex.getCause());
				if (call.length == 0 && ex != null){
					System.out.println(ex.getMessage());
					ex.getCause().printStackTrace();
				}
			}
		},eventLoop);
	}
	
	public boolean isConnected() {
		return connect;
	}
	
	public boolean connect(){
		if(!MySQLSupported)
			return false;
		try {
			if (conn == null || conn.isClosed() || !conn.isValid(500)) {
				try {
					conn = DriverManager.getConnection(config.buildURL());
					connect = true;
					return true;
				}
				catch (SQLException e) {
					System.out.println("Connect nicht moeglich");
					connect = false;
					return false;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public static interface Callback<T>{
		public void done(T obj,Throwable ex);
	}
	public List<String> getTables() {
		ArrayList<String> tables = new ArrayList<>();
		try{
			DatabaseMetaData md = getConnectionInstance().getMetaData();
			ResultSet rs = md.getTables(null, null, "%", null);
			while (rs.next()) {
				tables.add(rs.getString(3));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println("Tables: "+tables);
		return tables;
	}
}
