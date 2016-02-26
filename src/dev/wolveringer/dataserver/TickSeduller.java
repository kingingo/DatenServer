package dev.wolveringer.dataserver;

import dev.wolveringer.serverbalancer.AcardeManager;

public class TickSeduller {
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
					AcardeManager.balance();
					System.out.println("Balancing done in "+(System.currentTimeMillis()-start)+"ms");
					AcardeManager.writeServers();
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
