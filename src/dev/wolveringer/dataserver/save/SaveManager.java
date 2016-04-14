package dev.wolveringer.dataserver.save;

import dev.wolveringer.configuration.ServerConfiguration;
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

	public SaveManager start() {
		active = true;
		runner = new Thread(new Runnable() {
			@Override
			public void run() {
				while (active) {
					try {
						Thread.sleep(ServerConfiguration.getSaveManagerPeriode());
					} catch (InterruptedException e) {
					}
					System.out.println("§aSaving all players...");
					runSaveTick();
					System.out.println("All players saved!");
				}
			}
		});
		runner.start();
		return this;
	}

	public void stop() {
		active = false;
		if (runner != null)
			runner.interrupt();
	}

	public void saveAll() {
		runSaveTick();
	}

	protected void runSaveTick() {
		for (OnlinePlayer player : PlayerManager.getPlayers()) {
			try{
				player.save();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
