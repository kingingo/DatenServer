package eu.epicpvp.converter;

import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import eu.epicpvp.mysql.MySQL;

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
		
		for(String[] q : query){
			try{
				if(q[0].startsWith("!"))
					q[0] = "-"+ids.getPlayerId(UUID.fromString(q[0].substring(1)));
				else
					q[0] = ids.getPlayerId(UUID.fromString(q[0]))+"";
				if(q[0].equalsIgnoreCase("--1"))
					throw new RuntimeException("Player id not found");
				_new.command("INSERT INTO `list_skyblock_worlds`(`playerId`, `worldName`, `X`, `Z`) VALUES ('"+q[0]+"','"+q[1]+"','"+q[2]+"','"+q[3]+"')");
			}catch(Exception e){
				System.out.println("Cant transfare uuid: "+StringUtils.join(q,":"));
				e.printStackTrace();
			}
		}
		
		System.out.println("Reading allredy inserted list");
		System.out.println("Waiting for event loop");
		EventLoopWaiter.wait(_new.getEventLoop());
		System.out.println("Done");
	}
}
