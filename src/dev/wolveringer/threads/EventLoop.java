package dev.wolveringer.threads;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class EventLoop {
	private int MAX_THREADS = 0;
	private AtomicInteger threads = new AtomicInteger(0);
	private List<Runnable> todo = (List<Runnable>) Collections.synchronizedList(new ArrayList<Runnable>());

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
			t.start();
			return t;
		} else
			synchronized (todo) {
				todo.add(run);
			}
		return null;
	}
	
	public int getCurruntThreads(){
		return threads.get();
	}
	
	public List<Runnable> getQueue() {
		return Collections.unmodifiableList(todo);
	}
	
	/*
	public static void main(String[] args) {
		for(int i = 0;i<500;i++)
			join(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.println("Done worker threads: "+threads.get());
				}
			});
	}
	*/
}
