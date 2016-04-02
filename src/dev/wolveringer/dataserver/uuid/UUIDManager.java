package dev.wolveringer.dataserver.uuid;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.Charsets;
import org.json.JSONArray;
import org.json.JSONObject;

import dev.wolveringer.mysql.MySQL;

public class UUIDManager {
	private static HashMap<String, Long> lastUpdate = new HashMap<String, Long>(){
		public Long get(Object key) {
			Long r = super.get(key);
			if(r == null)
				return 0L;
			return r;
		};
	};
	static UUIDManager instance;

	/**
	 * Run it Async!
	 * @param player
	 * @return
	 */
	public static UUID getUUID(String player){
		UUID uuid;
		if (instance.premiumUUIDS.containsKey(player.toLowerCase())) {
			uuid = instance.premiumUUIDS.get(player.toLowerCase());
		} else {
			uuid = instance.getOfflineUUID(player);
		}
		return uuid;
	}

	/**
	 * Run it Async!
	 * @param player
	 * @param active
	 */
	public static void setPremiumUUID(String player,boolean active){
		instance.setPremiumUUID0(player, active);
	}
	
	/**
	 * Run it Async!
	 * @param player
	 * @param curruntName
	 */
	public static void saveUpdatePremiumName(UUID player,String curruntName){
		if(lastUpdate.get(curruntName)+(30*60*1000)<System.currentTimeMillis())
			instance.checkRename0(player, curruntName);
	}
	
	public static String getName(UUID uuid){
		if(instance.uuidToString.containsKey(uuid))
			return instance.uuidToString.get(uuid);
		return null;
	}
	
	/**
	 * Run it Async!
	 * @param player
	 * @param curruntName
	 */
	public static void updatePremiumName(UUID player,String curruntName){
		instance.checkRename0(player, curruntName);
	}
	
	private HashMap<String, UUID> premiumUUIDS = new HashMap<>();
	private HashMap<String, String> tables_uuid;
	private HashMap<String, String> tables_names;

	private HashMap<UUID, String> uuidToString = new HashMap<>();
	
	public static void init(){
		instance = new UUIDManager();
	}
	
	private UUIDManager() {
		loadTables();
		loadPremiumUUIDS();
	}

	private void loadPremiumUUIDS() {
		ArrayList<String[]> out = MySQL.getInstance().querySync("SELECT premium,uuid,player FROM users", -1);
		for(String[] s : out){
			if(s[0].equalsIgnoreCase("true"))
				premiumUUIDS.put(s[2],UUID.fromString(s[1]));
			uuidToString.put(UUID.fromString(s[1]), s[2]);
		}
		System.out.println("Loaded "+out.size()+" Premium Player");
	}

	//TRANSFARE VALUES
	/*
	public void correct(){
		HashMap<String,String> users = new HashMap<>();
		
		try{
			 Statement stmt = MySQL.getInstance().getConnectionInstance().createStatement();
	 	     ResultSet rs = stmt.executeQuery("SELECT * FROM list_users");
	 	     while(rs.next()){
	 	    	 users.put(rs.getString(1), rs.getString(2));
	 	    	 System.out.println("LOAD "+rs.getString(1));
	 	     }
		 }catch (Exception ex) {
			 ex.printStackTrace();
		 }
		
		 int size = users.size();
		 int count=0;
		 
		 for(String user : users.keySet()){
			 count++;
			 if(!this.catcher.containsKey(user.toLowerCase())){
	 	    	 System.out.println("("+count+"/"+size+")SET "+user);
	 	    	MySQL.getInstance().commandSync("INSERT INTO users (player,uuid,premium,password) values ('"+user+"','"+getOfflineUUID(user)+"','false','"+users.get(user)+"');");
			 }
		 }
	}
	*/

	private void replaceUUID(UUID old_uuid, UUID new_uuid) {
		for (String table : tables_uuid.keySet()) {
			MySQL.getInstance().commandSync("UPDATE " + table + " SET " + tables_uuid.get(table) + "='" + new_uuid + "' WHERE " + tables_uuid.get(table) + "='" + old_uuid + "'");
			System.out.println("[EpicPvP]: Die UUID (" + old_uuid + ") wurde zu " + new_uuid + " in der Tabelle " + table + " in der Spalte " + tables_uuid.get(table) + " geaendert!");
		}
	}

	private void replaceName(String old_name, String new_name) {
		old_name = old_name.toLowerCase();
		new_name = new_name.toLowerCase();
		for (String table : tables_names.keySet()) {
			MySQL.getInstance().commandSync("UPDATE " + table + " SET " + tables_names.get(table) + "='" + new_name + "' WHERE " + tables_names.get(table) + "='" + old_name + "'");
			System.out.println("[EpicPvP]: Der Name (" + old_name + ") wurde zu dem Namen " + new_name + " in der Tabelle " + table + "->" + tables_names.get(table) + " geaendert!");
		}
	}

	public void loadTables() {
		this.tables_uuid = new HashMap<>();
		this.tables_names = new HashMap<>();
		ResultSet rs = null;
		Statement stmt;
		for (String table : MySQL.getInstance().getTables()) {
			try {
				stmt = MySQL.getInstance().getConnectionInstance().createStatement();
				rs = stmt.executeQuery("SELECT * FROM " + table + " LIMIT 1");
				if (rs.next()) {
					for (int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++) {
						if (rs.getMetaData().getColumnLabel(i).toLowerCase().contains("uuid")) {
							tables_uuid.put(table, rs.getMetaData().getColumnLabel(i));
							break;
						} else if (rs.getMetaData().getColumnLabel(i).toLowerCase().equalsIgnoreCase("name") || rs.getMetaData().getColumnLabel(i).toLowerCase().equalsIgnoreCase("player")) {
							if (table.equalsIgnoreCase("bg_server"))
								continue;
							if (table.equalsIgnoreCase("bg_debug"))
								continue;
							tables_names.put(table, rs.getMetaData().getColumnLabel(i));
							break;
						}
					}
				}
				rs.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/*
	
	public void setPWS(){
		ArrayList<UUID> list = new ArrayList<>();
		
		MySQL.getInstance().commandSync("CREATE TABLE IF NOT EXISTS users(player varchar(30),uuid varchar(60),premium varchar(30),password varchar(30));");
		
		try {
			ResultSet rs1 = kDaten.getMysql().Query("SELECT `uuid` FROM users");
			while (rs1.next()) {
				list.add(UUID.fromString(rs1.getString(1)));
			}
			rs1.close();
		} catch (Exception err) {
			System.err.println(err);
		}
		
		int size = list.size();
		int count = 0;
		
		for(UUID uuid : list){
			try {
				ResultSet rs = kDaten.getMysql().Query("SELECT `player` FROM list_premium WHERE premium='true' AND uuid='"+uuid+"'");
				while (rs.next()) {
					try {
						ResultSet rs1 = kDaten.getMysql().Query("SELECT `password` FROM list_users WHERE name='"+rs.getString(1)+"'");
						while (rs1.next()) {
							kDaten.getMysql().Update("UPDATE users SET password='"+rs1.getString(1)+"' WHERE uuid='"+uuid+"'");
							count++;
							System.out.println("("+count+"/"+size+")SET "+uuid+" PW:"+rs1.getString(1));
						}
						rs1.close();
					} catch (Exception err) {
						System.err.println(err);
					}
				}
				rs.close();
			} catch (Exception err) {
				System.err.println(err);
			}
		}
	}
	
	public void loadUsers(){
		this.catcher.clear();
		
		kDaten.getMysql().Update("CREATE TABLE IF NOT EXISTS users(player varchar(30),uuid varchar(60),premium varchar(30),password varchar(30));");
		try {
			ResultSet rs = kDaten.getMysql().Query("SELECT `player`,`uuid` FROM users WHERE premium='true';");
			while (rs.next()) {
				this.catcher.put(rs.getString(1).toLowerCase(), UUID.fromString(rs.getString(2)));
			}
			rs.close();
		} catch (Exception err) {
			System.err.println(err);
		}
	}
	
	*/
	public UUID getMojangUUID(String name) {
		try {
			HttpURLConnection connection = createConnection(getProxy());
			String body = new JSONArray(Arrays.asList(name.toLowerCase())).toString();
			writeBody(connection, body);
			InputStreamReader inr = new InputStreamReader(connection.getInputStream());
			BufferedReader reader = new BufferedReader(inr);
			String s = null;
			StringBuilder sb = new StringBuilder();
			while ((s = reader.readLine()) != null) {
				sb.append(s);
			}

			if (sb.toString().length() <= 2) {
				return null;
			}

			try {
				JSONArray array = (JSONArray) new JSONArray(sb.toString());
				JSONObject obj = array.getJSONObject(0);
				s = obj.get("id").toString();
			} catch (IllegalStateException e) {
				s = sb.toString().substring(8, 40);
			}
			UUID uuid = paradiseUUID(s);
			return uuid;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
	}

	private Proxy getProxy() {
		return Proxy.NO_PROXY;
	}

	/*
	public void updateMojangNames() throws Exception {
		HashMap<UUID, String> list = new HashMap<>();
		ArrayList<String> names = new ArrayList<>();

		for (String name : this.premiumUUIDS.keySet()) {
			names.add(name);
			list.put(this.premiumUUIDS.get(name), name);
		}

		int size = names.size();
		int count = 0;

		int requests = (int) Math.ceil(names.size() / PROFILES_PER_REQUEST);
		for (int i = 0; i < requests; i++) {
			HttpURLConnection connection = createConnection(getProxy());
			String body = new JSONObject(names.subList(i * 100, Math.min((i + 1) * 100, names.size()))).toString();
			writeBody(connection, body);
			JSONArray array;
			try {
				array = (JSONArray) new JSONArray(new InputStreamReader(connection.getInputStream()));
			} catch (Exception e) {
				connection.disconnect();
				connection = createConnection(getProxy());
				body = new JSONArray(names.subList(i * 100, Math.min((i + 1) * 100, names.size()))).toString();
				writeBody(connection, body);
				array = (JSONArray) new JSONArray(new InputStreamReader(connection.getInputStream()));
			}

			for (Object profile : array) {
				JSONObject jsonProfile = (JSONObject) profile;
				String id = (String) jsonProfile.get("id");
				String name = (String) jsonProfile.get("name");
				UUID uuid = getUUID(id);
				count++;

				if (this.premiumUUIDS.containsKey(name.toLowerCase()) && this.premiumUUIDS.get(name.toLowerCase()).equals(uuid)) {
					list.remove(uuid);
					System.out.println("(" + count + "/" + size + ") REMOVE: " + name + " : " + uuid);
				} else {
					System.out.println("(" + count + "/" + size + ") ADD: " + this.premiumUUIDS.containsKey(name.toLowerCase()) + " " + name + " : " + uuid);
				}
			}

			if (rateLimiting && i != requests - 1) {
				Thread.sleep(100L);
			}
		}

		names.clear();

		if (!list.isEmpty()) {
			updatePremiumNames(list);
		}
	}
	*/

	private UUID paradiseUUID(String id) {
		return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32));
	}

	private void writeBody(HttpURLConnection connection, String body) throws Exception {
		OutputStream stream = connection.getOutputStream();
		stream.write(body.getBytes());
		stream.flush();
		stream.close();
	}

	private HttpURLConnection createConnection(Proxy proxy) throws Exception {

		URL url = new URL("https://api.mojang.com/profiles/minecraft");
		HttpURLConnection connection;

		if (proxy == null) {
			connection = (HttpURLConnection) url.openConnection();
		} else {
			connection = (HttpURLConnection) url.openConnection(proxy);
		}

		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setUseCaches(false);
		connection.setDoInput(true);
		connection.setDoOutput(true);
		return connection;
	}

	private <K,V> HashMap<V,K> reverse(Map<K,V> map) {
	    HashMap<V,K> rev = new HashMap<V, K>();
	    for(Map.Entry<K,V> entry : map.entrySet())
	        rev.put(entry.getValue(), entry.getKey());
	    return rev;
	}
	
	public void updatePremiumNames() throws Exception {
		ArrayList<UUID> uuids = new ArrayList<>(premiumUUIDS.values());
		HashMap<UUID, String> reversedUUID = reverse(premiumUUIDS);
		
		for (UUID uuid : uuids) {
			checkRename0(uuid, reversedUUID.get(uuid));
		}

		uuids.clear();
	}

	protected void checkRename0(UUID player,String curruntName){
		try{
			if(!premiumUUIDS.containsKey(curruntName))
				return;
			lastUpdate.put(curruntName, System.currentTimeMillis());
			HttpURLConnection connection = (HttpURLConnection) new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + player.toString().replace("-", "")).openConnection(getProxy());
			JSONObject response = (JSONObject) new JSONObject(new InputStreamReader(connection.getInputStream()));
			if (!response.has("name")) {
				return;
			}
			String name = (String) response.get("name");
			if(name.equalsIgnoreCase(curruntName))
				return;
			String cause = response.has("cause") ? (String) response.get("cause") : null;
			String errorMessage = response.has("errorMessage") ? (String) response.get("errorMessage") : null;
			if (cause != null && cause.length() > 0) {
				new IllegalStateException(errorMessage).printStackTrace();	//Dont cancel the hole task but print the stacktrace
				return;
			}
			
			premiumUUIDS.remove(curruntName);
			this.premiumUUIDS.put(name.toLowerCase(), player);
			replaceName(curruntName, name.toLowerCase());
			System.out.println("Player renamed from \""+curruntName+"\" to \""+name+"\"");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public UUID getOfflineUUID(String player) {
		return UUID.nameUUIDFromBytes(new StringBuilder().append("OfflinePlayer:").append(player.toLowerCase()).toString().getBytes(Charsets.UTF_8));
	}

	protected void setPremiumUUID0(String name, boolean bool) {
		if (bool) {
			if (this.premiumUUIDS.containsKey(name.toLowerCase())) { //ALREADY PREMIUM
				return;
			}
			UUID oldUUID = getOfflineUUID(name);
			UUID new_uuid = getMojangUUID(name);

			if (new_uuid != null) {
				this.premiumUUIDS.put(name.toLowerCase(), new_uuid);
				this.uuidToString.remove(oldUUID);
				this.uuidToString.put(new_uuid, name.toLowerCase());
				replaceUUID(oldUUID, new_uuid);
				MySQL.getInstance().commandSync("UPDATE users SET premium='true' WHERE uuid='" + new_uuid + "'");
				//TODO UUID UPDATE?
			} else {
				//TODO Not Premium
			}
		} else {
			if (!this.premiumUUIDS.containsKey(name.toLowerCase())) {//ALREADY OFFLINE UUID
				return;
			}
			UUID oldUUID = this.premiumUUIDS.get(name.toLowerCase());
			this.premiumUUIDS.remove(name.toLowerCase());
			this.uuidToString.remove(oldUUID);
			UUID new_uuid = getOfflineUUID(name);
			this.uuidToString.put(new_uuid, name.toLowerCase());
			replaceUUID(oldUUID, new_uuid);
			MySQL.getInstance().commandSync("UPDATE users SET premium='false' WHERE uuid='" + new_uuid + "'");
			//TODO UUID UPDATE?
		}
	}
}
