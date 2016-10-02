package dev.wolveringer.gild;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;

import dev.wolveringer.event.EventHelper;
import dev.wolveringer.events.gilde.GildeMoneyChangeEvent;
import dev.wolveringer.gilde.MoneyLogRecord;
import dev.wolveringer.mysql.MySQL;
import dev.wolveringer.sync.WaitForObject;
import lombok.Getter;

public class GildSectionMoney {
	private GildSection handle;
	
	private ArrayList<MoneyLogRecord> records = new ArrayList<>();
	@Getter
	private int currentBalance;
	private WaitForObject initObject = new WaitForObject();
	
	public GildSectionMoney(GildSection section) {
		this.handle = section;
	}
	
	@SuppressWarnings("unchecked")
	public void init(){
		MySQL.getInstance().query("SELECT `amount`,`playerId`,`date`,`message` FROM `GILDE_MONEY` WHERE `gilde`='"+handle.getHandle().getUuid()+"' AND `section`='"+handle.getType().name()+"'", -1,new MySQL.Callback<ArrayList<String[]>>(){ // `date`='-1' AND `playerId`='-1'");
			@Override
			public void done(ArrayList<String[]> response, Throwable ex) {
				if(ex != null){
					ex.printStackTrace();
					return;
				}
				if(response.size() == 0){
					MySQL.getInstance().command("INSERT INTO `GILDE_MONEY` (`gilde`, `section`, `date`, `playerId`, `amount`, `message`) VALUES ('"+handle.getHandle().getUuid()+"', '"+handle.getType().name()+"', '-1', '-1', '0', 'Current bank balance');");
					response.add(new String[]{"0","-1","-1",""});
				}
				for(String[] element : response){
					try{
						if(element[1].equalsIgnoreCase("-1") && element[2].equalsIgnoreCase("-1"))
							currentBalance = Integer.parseInt(response.get(0)[0]);
						else
						{
							records.add(new MoneyLogRecord(Long.parseLong(element[2]), Integer.parseInt(element[1]), Integer.parseInt(element[0]), element[3]));
						}
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				initObject.done();
			}
		});
	}
	
	public List<MoneyLogRecord> getRecords(){
		initObject.waitFor(5000);
		return records;
	}
	
	public void logRecord(int playerId,int amount,String message){
		logRecord(playerId, amount, message, System.currentTimeMillis());
	}
	
	public void logRecord(int playerId,int amount,String message,long date){
		initObject.waitFor(5000);
		MySQL.getInstance().command("INSERT INTO `GILDE_MONEY` (`gilde`, `section`, `date`, `playerId`, `amount`, `message`) VALUES ('"+handle.getHandle().getUuid()+"', '"+handle.getType().name()+"', '"+date+"', '"+playerId+"', '"+amount+"', '"+ObjectUtils.toString(message)+"');");
		MoneyLogRecord r;
		records.add(r = new MoneyLogRecord(date, playerId, amount, message));
		EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildeMoneyChangeEvent(handle.getHandle().getUuid(), handle.getType(), GildeMoneyChangeEvent.Action.HISTORY_ADD, -1, r));
	}
	
	public synchronized void addBalance(int balance){
		initObject.waitFor(5000);
		this.currentBalance += balance;
		MySQL.getInstance().command("UPDATE `GILDE_MONEY` SET `amount`='"+balance+"' WHERE `gilde`='"+handle.getHandle().getUuid()+"' AND `section`='"+handle.getType().name()+"' AND `date`='-1' AND `playerId`='-1'");
		
		if(balance > 0)
			EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildeMoneyChangeEvent(handle.getHandle().getUuid(), handle.getType(), GildeMoneyChangeEvent.Action.ADD, Math.abs(balance), null));
		else
			EventHelper.callGildEvent(handle.getHandle().getUuid(), new GildeMoneyChangeEvent(handle.getHandle().getUuid(), handle.getType(), GildeMoneyChangeEvent.Action.REMOVE, Math.abs(balance), null));
	}
}
