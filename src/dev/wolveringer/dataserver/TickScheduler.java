package dev.wolveringer.dataserver;

import dev.wolveringer.serverbalancer.ArcadeManager;

public class TickScheduler {
	boolean active;
	Thread runner = new Thread(new Runnable() {
		@Override
		public void run() {
			while (active) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try{
					long start = System.currentTimeMillis();
					System.out.println("Start balancing");
					ArcadeManager.balance();
					ArcadeManager.writeServers();
					System.out.println("Balancing done in "+(System.currentTimeMillis()-start)+"ms");
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	});
	
	public void start(){
		active = true;
		runner.start();
	}
}
