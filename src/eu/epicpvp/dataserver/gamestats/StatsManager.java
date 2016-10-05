package eu.epicpvp.dataserver.gamestats;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import eu.epicpvp.dataserver.player.OnlinePlayer;
import eu.epicpvp.dataserver.protocoll.packets.PacketInStatsEdit;
import eu.epicpvp.dataserver.protocoll.packets.PacketInStatsEdit.EditStats;
import eu.epicpvp.dataserver.protocoll.packets.PacketOutStats;
import eu.epicpvp.datenserver.definitions.dataserver.gamestats.GameType;
import eu.epicpvp.datenserver.definitions.dataserver.gamestats.StatsKey;
import eu.epicpvp.datenserver.definitions.gamestats.Statistic;
import eu.epicpvp.mysql.MySQL;

public class StatsManager {
	public static final String TABLE_PREFIX = "statistics_";

	public static void initTables() {
		for (GameType game : GameType.values()) {
			if (!game.isMySQL())
				continue;
			StatsKey[] stats = game.getStats();
			String tt = "playerId INT UNIQUE,";
			for (StatsKey s : stats) {
				tt = tt + s.getMySQLSyntax() + ",";
			}
			String t = "CREATE TABLE IF NOT EXISTS " + TABLE_PREFIX + game.getShortName() + "(" + tt.substring(0, tt.length() - 1) + ")";
			MySQL.getInstance().command(t);
		}
	}

	private OnlinePlayer owner;
	private HashMap<GameType, Statistic[]> stats = new HashMap<>();

	public StatsManager(OnlinePlayer owner) {
		this.owner = owner;
	}

	private void applayChange(Statistic statistic,EditStats change) {
		if (Statistic.types.get(change.getValue().getClass()) != Statistic.types.get(statistic.getValue().getClass()))
			throw new RuntimeException("A " + change.getValue().getClass() + "[" + change.getValue() + "] cant be cast to a " + statistic.getValue().getClass() + "[" + statistic.getValue() + "] statistic");
		switch (change.getAction()) {
		case ADD:
			switch (Statistic.types.get(change.getValue().getClass())) {
			case 0:
				statistic.setOutput(statistic.asInt() + (int) change.getValue());
				break;
			case 1:
				statistic.setOutput(statistic.asDouble() + (double) change.getValue());
				break;
			case 2:
				throw new RuntimeException("String is not addable");
			default:
				throw new RuntimeException("No class found");
			}
			break;
		case REMOVE:
			switch (Statistic.types.get(change.getValue().getClass())) {
			case 0:
				statistic.setOutput(statistic.asInt() - (int) change.getValue());
				break;
			case 1:
				statistic.setOutput(statistic.asDouble() - (double) change.getValue());
				break;
			case 2:
				throw new RuntimeException("String is not removeable");
			default:
				throw new RuntimeException("No class found");
			}
			break;
		case SET:
			switch (Statistic.types.get(change.getValue().getClass())) {
			case 0:
				statistic.setOutput((int) change.getValue());
				break;
			case 1:
				statistic.setOutput((double) change.getValue());;
				break;
			case 2:
				statistic.setOutput(change.getValue()+"");;
				break;
			default:
				throw new RuntimeException("No class found");
			}
			break;
		default:
			throw new RuntimeException("Type not found");
		}
		statistic.setNeedSave(true);
	}

	public PacketOutStats getStats(GameType game) {
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
					applayChange(s,stat);
		}
	}

	public void save() {
		for (GameType game : stats.keySet()) {
			ArrayList<Statistic> needSaves = new ArrayList<>();
			for (Statistic s : stats.get(game)) {
				if (s.needSave())
					needSaves.add(s);
			}
			if (needSaves.size() != 0) {
				save(game, needSaves.toArray(new Statistic[0]));
			}
		}
	}

	private void save(GameType game, Statistic... statistics) {
		String values = "";
		for (Statistic s : statistics) {
			values += "`" + s.getStatsKey().getMySQLName() + "`='" + s.getValue() + "',";
		}
		String mySQLSyntax = "UPDATE `" + TABLE_PREFIX + game.getShortName() + "` SET " + values.substring(0, values.length() - 1) + " WHERE `playerId` ='" + owner.getPlayerId() + "'";
		MySQL.getInstance().command(mySQLSyntax);
	}

	private Statistic[] loadStats(GameType game) {
		// Table name: users_"+typ.getKürzel()
		StatsKey[] keys = game.getStats();
		String mySQLSyntax = "SELECT ";
		for (StatsKey k : keys)
			mySQLSyntax += ",`" + k.getMySQLName() + "`";
		mySQLSyntax = mySQLSyntax.replaceFirst(",", "");
		mySQLSyntax += " FROM " + TABLE_PREFIX + game.getShortName() + " ";
		mySQLSyntax += "WHERE `playerId`='" + owner.getPlayerId() + "' LIMIT 1";

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

	private Statistic[] insertStats(GameType game) {
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
		for (Statistic s : statistiks) {
			rowNames += "`" + s.getStatsKey().getMySQLName() + "`,";
			values += "'" + s.getValue() + "',";
		}
		//INSERT INTO `gems_list`(`name`, `gems`, `uuid`) VALUES ([value-1],[value-2],[value-3])
		String mySQLSyntax = "INSERT INTO `" + TABLE_PREFIX + game.getShortName() + "` (`playerId`," + rowNames.substring(0, rowNames.length() - 1) + ") VALUES ('" + owner.getPlayerId() + "'," + values.substring(0, values.length() - 1) + ")";
		MySQL.getInstance().commandSync(mySQLSyntax);
		return statistiks;
	}

	public static void main(String[] args) {
		System.out.println(new Date(1460481484000L));
		/*
		GameType game = GameType.Money;
		StatsKey[] keys = game.getStats();
		Statistic[] statistiks = new Statistic[keys.length];
		for (int i = 0; i < statistiks.length; i++) {
			statistiks[i] = new Statistic(keys[i], null);
		}
		// "INSERT INTO users_"+typ.getKürzel()+" ("+tt.substring(0,
		// tt.length()-1)+") VALUES ("+ti.subSequence(0, ti.length()-1)+");"
		String rowNames = "";
		String values = "";
		for (Statistic s : statistiks) {
			rowNames += "`" + s.getStatsKey().getMySQLName() + "`,";
			values += "`" + s.getValue() + "`,";
		}
		//INSERT INTO `gems_list`(`name`, `gems`, `uuid`) VALUES ([value-1],[value-2],[value-3])
		String mySQLSyntax = "INSERT INTO `" + TABLE_PREFIX + game.getShortName() + "` (`playerId`," + rowNames.substring(0, rowNames.length() - 1) + ") VALUES ('null'," + values.substring(0, values.length() - 1) + ")";
		System.out.println(mySQLSyntax);
		*/
	}
}
