package eu.epicpvp.dataserver.connection;

import eu.epicpvp.threads.EventLoop;

public class ThreadHandleManager {

	private static final EventLoop HandlerEventLoop = new EventLoop(400);

	static {
		HandlerEventLoop.setWarnQueueSize(1000);
	}
	
	public static Thread join(Runnable run) {
		return HandlerEventLoop.join(run);
	}
}
