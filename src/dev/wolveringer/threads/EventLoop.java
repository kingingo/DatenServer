package dev.wolveringer.threads;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Getter;
import lombok.Setter;

public class EventLoop {
	private int MAX_THREADS = 0;
	private AtomicInteger threads = new AtomicInteger(0);
	private List<Runnable> todo = (List<Runnable>) Collections.synchronizedList(new ArrayList<Runnable>());
	@Getter
	@Setter
	private int maxQueueSize = -1;
	@Getter
	@Setter
	private int warnQueueSize = -1;
	private String name;
	
	public EventLoop(String name, int maxthreads) {
		this.MAX_THREADS = maxthreads;
		this.name = name;
	}
	
	public EventLoop(int maxthreads) {
		this.MAX_THREADS = maxthreads;
	}
	
	public Thread join(Runnable run) {
		if (MAX_THREADS <= 0 || threads.get() < MAX_THREADS) {
			threads.addAndGet(1);
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						run.run();
					} catch (Exception e) {
						e.printStackTrace();
					}
					while (true) {
						Runnable next = null;
						synchronized (todo) {
							if (todo.size() != 0) {
								next = todo.get(0);
								todo.remove(next);
							} else
								break;
						}
						next.run();
					}
					threads.addAndGet(-1);
				}
			});
			t.setName(name+"_worker #"+threads.get());
			t.start();
			return t;
		} else
			synchronized (todo) {
				if(maxQueueSize != -1 && todo.size() >= maxQueueSize)
					throw new RuntimeException("["+name+"]  Event loop queue full! (Max-Size: "+maxQueueSize+")");
				if(warnQueueSize != -1 && todo.size() >= warnQueueSize)
					System.out.println("["+name+"] Event loop queue is overloaded (Size: "+todo.size()+")");
				todo.add(run);
			}
		return null;
	}
	
	public void waitForAll(){
		while (threads.get() > 0) {
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
			}
		}
	}
	
	public int getCurruntThreads(){
		return threads.get();
	}
	
	public List<Runnable> getQueue() {
		return Collections.unmodifiableList(todo);
	}
	
	public static void main(String[] args) {
		EventLoop loop = new EventLoop(1);
		loop.setMaxQueueSize(501);
		for(int i = 0;i<500;i++)
			loop.join(new Runnable() {
				@Override
				public void run() {
					System.out.println("Done worker threads: "+loop.getQueue().size());
				}
			});
	}
}
