package dev.wolveringer.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import dev.wolveringer.mysql.MySQL;

/**
 * TODO!
 * @author root
 *
 */
public class SkyPvPConverter {
	
	//list_skyblock_worlds
	private MySQL _old;
	private MySQL _new;
	private PlayerIdConverter  ids;
	
	public SkyPvPConverter(MySQL _old,MySQL _new,PlayerIdConverter  ids) {
		this._old =_old;
		this._new = _new;
		this.ids = ids;
	}
	
	public void transfare(){
		ArrayList<String[]> query;
		ArrayList<String> alredyInserted = new ArrayList<>();
		query = _new.querySync("SELECT `playerId`,`worldName`,`X`,`Z` FROM `list_skyblock_worlds`", -1);
		
		System.out.println("Reading old island list");
		query = _old.querySync("SELECT `UUID`,`worldName`,`X`,`Z` FROM `list_skyblock_worlds`", -1);
		
		System.out.println("Reading allredy inserted list");
		System.out.println("Waiting for event loop");
		EventLoopWaiter.wait(_new.getEventLoop());
		System.out.println("Done");
	}
}
