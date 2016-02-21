package dev.wolveringer.dataserver.gamestats;

import java.util.ArrayList;
import java.util.HashMap;

import dev.wolveringer.dataserver.player.OnlinePlayer;
import dev.wolveringer.dataserver.protocoll.packets.PacketInStatsEdit;
import dev.wolveringer.dataserver.protocoll.packets.PacketOutStats;
import dev.wolveringer.mysql.MySQL;

public class StatsManager {
	public static class Statistic {
		private static HashMap<Class<?>, Integer> types = new HashMap<>();

		static {
			types.put(int.class, 0);
			types.put(Integer.class, 0);
			types.put(double.class, 1);
			types.put(Double.class, 1);
			types.put(String.class, 2);
		}

		private StatsKey stat;
		private Object output;
		private boolean needSave = false;
		public Statistic(StatsKey stat, Object output) {
			this.stat = stat;
			if (output != null)
			switch (types.get(stat.getType())) {
			case 0:
				this.output = (int) Integer.valueOf(output.toString());
				break;
			case 1:
				this.output = (double) Double.valueOf(output.toString());
				break;
			case 2:
				this.output = output;
				break;
			default:
				throw new RuntimeException("No class found");
			}
			else {
				switch (types.get(stat.getType())) {
				case 0:
					this.output = (int) 0;
					break;
				case 1:
					this.output = (double) 0D;
					break;
				case 2:
					this.output = "";
					break;
				default:
					throw new RuntimeException("No class found");
				}
			}
		}

		public int asInt() {
			return (int) Integer.valueOf(output.toString());
		}

		public double asDouble() {
			return (double) Double.valueOf(output.toString());
		}

		public String asString() {
			return (String) output;
		}

		public int getTypeId() {
			return types.get(stat.getType());
		}

		public StatsKey getStatsKey() {
			return stat;
		}

		public Object getValue() {
			return output;
		}

		protected void applayChange(PacketInStatsEdit.EditStats change) {
			if (types.get(change.getValue().getClass()) != types.get(output.getClass()))
				throw new RuntimeException("A "+change.getValue().getClass() + "["+change.getValue()+"] cant be cast to a " + output.getClass() + "["+output+"] statistic");
			switch (change.getAction()) {
			case ADD:
				switch (types.get(change.getValue().getClass())) {
				case 0:
					output = asInt() + (int) change.getValue();
					break;
				case 1:
					output = asDouble() + (double) change.getValue();
					break;
				case 2:
					throw new RuntimeException("String is not addable");
				default:
					throw new RuntimeException("No class found");
				}
				break;
			case REMOVE:
				switch (types.get(change.getValue().getClass())) {
				case 0:
					output = asInt() - (int) change.getValue();
					break;
				case 1:
					output = asDouble() - (double) change.getValue();
					break;
				case 2:
					throw new RuntimeException("String is not removeable");
				default:
					throw new RuntimeException("No class found");
				}
				break;
			case SET:
				switch (types.get(change.getValue().getClass())) {
				case 0:
					output = (int) change.getValue();
					break;
				case 1:
					output = (double) change.getValue();
					break;
				case 2:
					output = change.getValue();
					break;
				default:
					throw new RuntimeException("No class found");
				}
				break;
			default:
				throw new RuntimeException("Type not found");
			}
			needSave = true;
		}
		public boolean needSave() {
			return needSave;
		}
	}

	private OnlinePlayer owner;
	private HashMap<Game, Statistic[]> stats = new HashMap<>();

	public StatsManager(OnlinePlayer owner) {
		this.owner = owner;
	}
	
	public PacketOutStats getStats(Game game) {
		if (!stats.containsKey(game))
			loadStats(game);
		return new PacketOutStats(owner.getUuid(), game, stats.get(game));
	}

	public void applayChanges(PacketInStatsEdit packet) {
		for (PacketInStatsEdit.EditStats stat : packet.getChanges()) {
			if (!stats.containsKey(stat.getGame()))
				loadStats(stat.getGame());
			for (Statistic s : stats.get(stat.getGame()))
				if (s.getStatsKey() == stat.getKey())
					s.applayChange(stat);
		}
	}

	public void save(){
		for(Game game : stats.keySet()){
			ArrayList<Statistic> needSaves = new ArrayList<>();
			for(Statistic s : stats.get(game)){
				if(s.needSave())
					needSaves.add(s);
			}
			if(needSaves.size() != 0){
				save(game,needSaves.toArray(new Statistic[0]));
			}
		}
	}
	
	private void save(Game game,Statistic...statistics){
		String values = "";
		for(Statistic s : statistics){
			values += "`"+s.getStatsKey().getMySQLName()+"`='"+s.getValue()+"',";
		}
		String mySQLSyntax = "UPDATE `users_"+game.getKuerzel()+"` SET "+values.substring(0, values.length()-1)+" WHERE UUID='"+owner.getUuid()+"'";
		MySQL.getInstance().command(mySQLSyntax);
	}
	
	private Statistic[] loadStats(Game game) {
		// Table name: users_"+typ.getKürzel()
		StatsKey[] keys = game.getStats();
		String mySQLSyntax = "SELECT ";
		for (StatsKey k : keys)
			mySQLSyntax += ",`" + k.getMySQLName() + "`";
		mySQLSyntax = mySQLSyntax.replaceFirst(",", "");
		mySQLSyntax += " FROM users_" + game.getKuerzel() + " ";
		mySQLSyntax += "WHERE UUID='" + owner.getUuid() + "' LIMIT 1";

		ArrayList<String[]> data = MySQL.getInstance().querySync(mySQLSyntax, 1);
		if (data.size() == 0) {
			return insertStats(game);
		}
		Statistic[] statistiks = new Statistic[keys.length];
		for (int i = 0; i < data.get(0).length; i++) {
			statistiks[i] = new Statistic(keys[i], data.get(0)[i]);
		}
		stats.put(game, statistiks);
		return statistiks;
	}

	private Statistic[] insertStats(Game game) {
		StatsKey[] keys = game.getStats();
		Statistic[] statistiks = new Statistic[keys.length];
		for (int i = 0; i < statistiks.length; i++) {
			statistiks[i] = new Statistic(keys[i], null);
		}
		stats.put(game, statistiks);
		// "INSERT INTO users_"+typ.getKürzel()+" ("+tt.substring(0,
		// tt.length()-1)+") VALUES ("+ti.subSequence(0, ti.length()-1)+");"
		String rowNames = "";
		String values = "";
		for(Statistic s : statistiks){
			rowNames += "`"+s.getStatsKey().getMySQLName()+"`,";
			values += "`"+s.getValue()+"`,";
		}
		//INSERT INTO `gems_list`(`name`, `gems`, `uuid`) VALUES ([value-1],[value-2],[value-3])
		String mySQLSyntax = "INSERT INTO `users_"+game.getKuerzel()+"` (`player`,`UUID`,"+rowNames.substring(0, rowNames.length()-1)+") VALUES ('"+owner.getName()+"','"+owner.getUuid().toString()+"',"+values.substring(0, values.length()-1)+")";
		MySQL.getInstance().commandSync(mySQLSyntax);
		return statistiks;
	}

	public static void main(String[] args) {
		StatsKey[] keys = Game.SheepWars.getStats();
		Statistic[] statistiks = new Statistic[keys.length];
		for (int i = 0; i < statistiks.length; i++) {
			statistiks[i] = new Statistic(keys[i], null);
		}
		// "INSERT INTO users_"+typ.getKürzel()+" ("+tt.substring(0,
		// tt.length()-1)+") VALUES ("+ti.subSequence(0, ti.length()-1)+");"
		String rowNames = "";
		String values = "";
		for(Statistic s : statistiks){
			rowNames += "`"+s.getStatsKey().getMySQLName()+"`,";
			values += "`"+s.getValue()+"`,";
		}
		//INSERT INTO `gems_list`(`name`, `gems`, `uuid`) VALUES ([value-1],[value-2],[value-3])
		String mySQLSyntax = "INSERT INTO `users_"+Game.BedWars.getKuerzel()+"` (`player`,`UUID`,"+rowNames.substring(0, rowNames.length()-1)+") VALUES ('Underknown','underknown',"+values.substring(0, values.length()-1)+")";
		System.out.println(mySQLSyntax);
	}
}
