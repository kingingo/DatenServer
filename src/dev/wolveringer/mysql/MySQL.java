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

public class MySQL {
	private static MySQL instance;
	
	public static MySQL getInstance(){
		return instance;
	}
	
	public static interface ThreadFactory {
		public void createAsync(Runnable run);
	}
	
	private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
		@Override
		public void createAsync(Runnable run) {
			new Thread(run).start();
			//BungeeCord.getInstance().getScheduler().runAsync(Main.getMain(), run);
		}
	};
	
	public static void setInstance(MySQL instance) {
		MySQL.instance = instance;
	}
	
	private Connection conn = null;
	private String dbHost = null;
	private String dbPort = "3306";
	private String database = null;
	private String dbUser = null;
	private String dbPassword = null;
	private boolean connect = false;
	
	public MySQL(String Host, String Port, String base, String user, String Password) {
		if ((Host == null) || (Host == "")) { throw new NullPointerException("Host can`t be null"); }
		if ((base == null) || (base == "")) { throw new NullPointerException("Data-Base can`t be null"); }
		if ((user == null) || (user == "")) { throw new NullPointerException("User can`t be null"); }
		if ((Password == null) || (Password == "")) { throw new NullPointerException("Password can`t be null"); }
		dbHost = Host;
		try {
			Integer.parseInt(Port);
		}
		catch (Exception e) {
			dbPort = "3306";
		}
		database = base;
		dbUser = user;
		dbPassword = Password;
		getConnectionInstance();
	}
	
	public Connection getConnectionInstance() {
		if (conn == null) {
			try {
				Class.forName("com.mysql.jdbc.Driver");
				conn = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":" + dbPort + "/" + database + "?" + "user=" + dbUser + "&" + "password=" + dbPassword + "&autoReconnect=true");
				System.out.print("Connect true!");
				connect = true;
			}
			catch (ClassNotFoundException e) {
				System.out.println("Treiber nicht gefunden");
			}
			catch (SQLException e) {
				System.out.println("Connect nicht moeglich");
				connect = false;
			}
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
		});
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
			catch (SQLException e) {
				throw new RuntimeException(e);
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
					c.done(ex == null, ex.getCause());
				if (call.length == 0 && ex != null) ex.getCause().printStackTrace();
			}
		});
	}
	
	public boolean isConnected() {
		return connect;
	}
	public static interface Callback<T>{
		public void done(T obj,Throwable ex);
	}
	public List<String> getTables() {
		ArrayList<String> tables = new ArrayList<>();
		try{
			DatabaseMetaData md = conn.getMetaData();
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
