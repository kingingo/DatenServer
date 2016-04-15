package dev.wolveringer.converter;

import java.util.ArrayList;
import java.util.UUID;

import dev.wolveringer.mysql.MySQL;

public class PermissionConverter {
	private MySQL _old;
	private MySQL _new;
	private PlayerIdConverter  ids;
	
	public PermissionConverter(MySQL _old,MySQL _new,PlayerIdConverter  ids) {
		this._old =_old;
		this._new = _new;
		this.ids = ids;
	}
	
	public void transfare(){
		ArrayList<String[]> querry;
		querry = _old.querySync("SELECT `uuid`,`prefix`,`permission`,`pgroup`,`grouptyp` FROM `game_perm`",-1);
		_new.commandSync("TRUNCATE game_perm");
		
		for(String[] perm : querry){
			if(perm[0].equalsIgnoreCase("none") || perm[0].equalsIgnoreCase(""))
				perm[0] = "-2";
			else
				perm[0] = ids.getPlayerId(UUID.fromString(perm[0]))+"";
			_new.command("UPDATE `game_perm` SET `playerId`='"+perm[0]+"',`prefix`='"+perm[1]+"',`permission`='"+perm[2]+"',`pgroup`='"+perm[3]+"',`grouptyp`='"+perm[4]+"'");
		}
		EventLoopWaiter.wait(_new.getEventLoop());
	}
}
