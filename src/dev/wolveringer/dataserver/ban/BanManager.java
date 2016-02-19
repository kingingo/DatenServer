package dev.wolveringer.dataserver.ban;

import java.util.ArrayList;
import java.util.UUID;

public class BanManager {
	private ArrayList<BanEntity> bans = new ArrayList<>();
	
	
	
	public void loadBans(){
		
	}
	
	
	public BanEntity getEntity(String name,String ip,UUID uuid){
		int match = 0;
		BanEntity e = null;
		for(BanEntity te : new ArrayList<>(bans)){
			int tmatch = te.matchPlayer(ip, name, uuid);
			if(tmatch>match){
				match = tmatch;
				e = te;
			}
		}
		return e;
	}
}
