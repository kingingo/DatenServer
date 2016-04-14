package dev.wolveringer.converter;

import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import dev.wolveringer.dataserver.gamestats.GameType;
import dev.wolveringer.dataserver.gamestats.StatsKey;
import dev.wolveringer.mysql.MySQL;

public class GameStatsConverter {
	private MySQL _old;
	private MySQL _new;
	private PlayerIdConverter  ids;
	
	public GameStatsConverter(MySQL _old,MySQL _new,PlayerIdConverter  ids) {
		this._old =_old;
		this._new = _new;
		this.ids = ids;
	}
	
	public void transfare(){
		System.out.println("Transfaring game stats");
		for(GameType type : GameType.values()){
			if(type != GameType.ALL && type != GameType.Money &&  type != GameType.NONE){
				System.out.println("Loading game old: "+type);
				ArrayList<String[]> old = _old.querySync(buildSelect(type,"users_","UUID"), -1);
				int oldSize = old.size();
				System.out.println("Old size: "+old.size()+"\nLoading game new: "+type);
				ArrayList<String[]> temp = _new.querySync("SELECT `playerId` FROM statistics_"+type.getShortName()+" WHERE 1",-1);
				System.out.println("New size: "+temp.size()+" remove double: "+type);
				ArrayList<String> alredyIn = new ArrayList<>();
				for(String[] s : temp)
					alredyIn.add(s[0].replaceAll(" ", ""));
				int added = 0;
				int skipped = 0;
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				for(int i = 0;i<old.size();i++){
					if(oldSize != old.size())
						System.out.println("?!: "+oldSize+":"+old.size());
					String[] player = old.get(i);
					try{
						player[0] = ids.getPlayerId(UUID.fromString(player[0]))+"";
						if(player[0].equalsIgnoreCase("-1")){
							skipped++;
							continue;
						}
						if(alredyIn.contains(player[0])){
							skipped++;
							continue;
						}
						_new.command(buildInsert(type, player));
						alredyIn.add(player[0]);
						added++;
					}catch(Exception e){
						System.out.println("Cant insert player: "+StringUtils.join(player," ")+" - "+e.getMessage());
					}
				}
				System.out.println("Players added: "+added+" Players skipped: "+skipped);
				EventLoopWaiter.wait(_new.getEventLoop());
			}
		}
	}
	
	private String buildSelect(GameType game,String tablePrefix,String rowName) {
		// Table name: users_"+typ.getKürzel()
		StatsKey[] keys = game.getStats();
		String mySQLSyntax = "SELECT `"+rowName+"`, ";
		for (StatsKey k : keys)
			mySQLSyntax += ",`" + k.getMySQLName() + "`";
		mySQLSyntax = mySQLSyntax.replaceFirst(",", "");
		mySQLSyntax += " FROM " + tablePrefix + game.getShortName() + " ";
		return mySQLSyntax;
	}

	/*
	 * args[0] == playerId
	 */
	private String buildInsert(GameType game,String[] args) {
		StatsKey[] keys = game.getStats();
		// "INSERT INTO users_"+typ.getKürzel()+" ("+tt.substring(0,
		// tt.length()-1)+") VALUES ("+ti.subSequence(0, ti.length()-1)+");"
		String rowNames = "";
		String values = "";
		int count = 1;
		for (StatsKey s : keys) {
			rowNames += "`" + s.getMySQLName() + "`,";
			values += "'" + args[count++] + "',";
		}
		//INSERT INTO `gems_list`(`name`, `gems`, `uuid`) VALUES ([value-1],[value-2],[value-3])
		String mySQLSyntax = "INSERT INTO `" + "statistics_" + game.getShortName() + "` (`playerId`," + rowNames.substring(0, rowNames.length() - 1) + ") VALUES ('" + args[0] + "'," + values.substring(0, values.length() - 1) + ")";
		return mySQLSyntax;
	}
}
