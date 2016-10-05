package eu.epicpvp.converter;

import java.util.ArrayList;
import java.util.UUID;

import eu.epicpvp.mysql.MySQL;

public class GuildenConverter {
	private MySQL _old;
	private MySQL _new;
	private PlayerIdConverter  ids;
	private String server;
	
	public GuildenConverter(MySQL _old,MySQL _new,PlayerIdConverter  ids,String server) {
		this._old =_old;
		this._new = _new;
		this.ids = ids;
		this.server = server;
	}
	
	public void transfare(){
		ArrayList<String[]> querry;
		System.out.println("Transfaring guilds");
		querry = _old.querySync("SELECT `gilde`, `gildentag`, `member`, `founder_uuid`, `owner_uuid` FROM `list_gilden_"+server+"`",-1);
		_new.commandSync("TRUNCATE list_gilden_"+server);
		
		for(String[] q : querry){
			q[3] = ids.getPlayerId(UUID.fromString(q[3]))+"";
			q[4] = ids.getPlayerId(UUID.fromString(q[4]))+"";
			_new.command("INSERT INTO `list_gilden_"+server+"`(`gilde`, `gildentag`, `member`, `founder_playerId`, `owner_playerId`) VALUES ('"+q[0]+"','"+q[1]+"','"+q[2]+"','"+q[3]+"','"+q[4]+"')");
		}
		
		EventLoopWaiter.wait(_new.getEventLoop());
		System.out.println("Transfaring users");
		
		querry = _old.querySync("SELECT `player`, `UUID`, `gilde` FROM `list_gilden_"+server+"_user`",-1);
		_new.commandSync("TRUNCATE list_gilden_"+server+"_user");
		for(String[] q : querry){
			q[0] = ids.getPlayerId(UUID.fromString(q[1]))+"";
			if(q[0].equalsIgnoreCase("-1"))
				continue;
			_new.command("INSERT INTO `list_gilden_"+server+"_user`(`playerId`, `gilde`) VALUES ('"+q[0]+"','"+q[2]+"')");
		}
		
		EventLoopWaiter.wait(_new.getEventLoop());
	}
}
