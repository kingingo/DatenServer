package dev.wolveringer.converter;

import java.util.LinkedList;

import dev.wolveringer.threads.EventLoop;

public class EventLoopWaiter {
	public static void wait(EventLoop loop){
		LinkedList<Long> diffs = new LinkedList<>();
		System.out.println("Waiting for event loop");
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
		}
		long lastQueueSize = loop.getQueue().size()+loop.getCurruntThreads();
		long start = System.currentTimeMillis();
		while (loop.getQueue().size()>0 || loop.getCurruntThreads() > 0) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			long curruntQueueSize = loop.getQueue().size()+loop.getCurruntThreads();
			diffs.addLast(lastQueueSize-curruntQueueSize);
			lastQueueSize = curruntQueueSize;
			System.out.println("Waiting for task completion: "+curruntQueueSize+" | Time: "+(curruntQueueSize/calculateNormalDiff(diffs))+"seconds | Speed: "+calculateNormalDiff(diffs)+" elements/second");
			
			if(diffs.size()>30){
				diffs.pollFirst();
			}
		}
		long allTime = System.currentTimeMillis()-start;
		System.out.println("Time needed: "+(allTime/1000)+" seconds");
		diffs.clear();
	}
	
	private static long calculateNormalDiff(LinkedList<Long> diffs){
		long all =  0L;
		for(Long l : diffs)
			all+=l;
		if(diffs.size() == 0)
			return 1;
		long out = all/diffs.size();
		return out == 0L ? 1 : out;
	}
}
