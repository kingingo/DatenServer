package eu.epicpvp.autorestart;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import eu.epicpvp.connection.server.ServerThread;
import eu.epicpvp.dataserver.connection.Client;
import eu.epicpvp.dataserver.protocoll.packets.PacketChatMessage;
import eu.epicpvp.dataserver.protocoll.packets.PacketChatMessage.TargetType;
import eu.epicpvp.dataserver.protocoll.packets.PacketServerAction;
import eu.epicpvp.dataserver.protocoll.packets.PacketServerAction.Action;
import eu.epicpvp.dataserver.terminal.ChatColor;
import eu.epicpvp.datenserver.definitions.connection.ClientType;

public class RestartTimer extends TimerTask{
	private Timer timer;
	private long diff;
	private int houer;
	private int minute;
	private int second;
	
	public RestartTimer(int houer,int minute,int second) {
		this.houer = houer;
		this.minute = minute;
		this.second = second;
		this.diff = TimeUnit.DAYS.toMillis(1);
	}
	
	public void startListening(){
		timer = new Timer();
		timer.schedule(this, createCalendar().getTime(), diff);
	}
	
	public void stopListining(){
		timer.cancel();
		timer = null;
	}

	private Calendar createCalendar(){
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, houer);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, second);
		System.out.println(calendar.getTime());
		if(System.currentTimeMillis()>calendar.getTimeInMillis())
			calendar.add(Calendar.DATE, 1);
		return calendar;
	}
	
	public static void main(String[] args) {
		new RestartTimer(20, 21, 30).startListening();
	}
	
	@Override
	public void run() {
		System.out.println("Run restart!");
		runRestartTimer();
	}
	
	private void runRestartTimer(){
		broadcast("§c§l>> §c§lServer restart in 5 min.");
		try {
			Thread.sleep(3*60*1000);
		} catch (InterruptedException e) {
		}
		broadcast("§c§l>> §c§lServer restart in 2 min.");
		try {
			Thread.sleep(1*60*1000);
		} catch (InterruptedException e) {
		}
		broadcast("§c§l>> §c§lServer restart in 1 min.");
		try {
			Thread.sleep(30*1000);
		} catch (InterruptedException e) {
		}
		broadcast("§c§l>> §c§lServer restart in 30 sec.");
		try {
			Thread.sleep(15*1000);
		} catch (InterruptedException e) {
		}
		for(int i = 15;i>0;i--){
			broadcast("§c§l>> §c§lServer restart in "+i+" sec.");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		broadcast("§c§l>> §c§lServer restart!");
		for(Client c : ServerThread.getBungeecords()){
			c.writePacket(new PacketServerAction(new PacketServerAction.PlayerAction[]{new PacketServerAction.PlayerAction(-1, Action.RESTART, "§cGlobal network restart!")}));
		}
		for(Client c : ServerThread.getServer(ClientType.ALL)){
			if(c.getType() != ClientType.BUNGEECORD)
				c.writePacket(new PacketServerAction(new PacketServerAction.PlayerAction[]{new PacketServerAction.PlayerAction(-1, Action.RESTART, "§cGlobal network restart!")}));
		}
	}
	
	private void broadcast(String message){
		for(Client c : ServerThread.getBungeecords())
			c.writePacket(new PacketChatMessage(ChatColor.translateAlternateColorCodes('&', message), new PacketChatMessage.Target[]{new PacketChatMessage.Target(TargetType.BROTCAST, null, null)}));
	}
}
