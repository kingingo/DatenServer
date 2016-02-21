package dev.wolveringer.dataserver.save;

import dev.wolveringer.dataserver.player.OnlinePlayer;
import dev.wolveringer.dataserver.player.PlayerManager;

public class SaveManager {
	private static SaveManager manager;
	
	public static void setSaveManager(SaveManager manager) {
		SaveManager.manager = manager;
	}
	public static SaveManager getSaveManager() {
		return manager;
	}
	
	private Thread runner;
	private boolean active = false;
	
	public SaveManager start(){
		active = true;
		runner = new Thread(new Runnable() {
			@Override
			public void run() {
				while (active) {
					try {
						Thread.sleep(5*60*1000);
					} catch (InterruptedException e) {
					}
					runSaveTick();
				}
			}
		});
		runner.start();
		return this;
	}
	
	public void stop(){
		active = false;
		if(runner != null)
			runner.interrupt();
	}
	
	public void saveAll(){
		runSaveTick();
	}
	
	protected void runSaveTick(){
		for(OnlinePlayer player : PlayerManager.getPlayer()){
			player.save();
		}
	}
}
