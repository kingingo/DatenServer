package dev.wolveringer.converter;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

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
			System.out.println("Waiting for task completion: "+curruntQueueSize+" | Time: "+getDurationBreakdown(curruntQueueSize/calculateNormalDiff(diffs)*1000)+" seconds | Speed: "+calculateNormalDiff(diffs)+" elements/second");
			
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
	
	public static String getDurationBreakdown(long millis) {
		if (millis < 0) {
			return "millis<0";
		}
		if(millis == 0)
			return "now";
		long days = TimeUnit.MILLISECONDS.toDays(millis);
		millis -= TimeUnit.DAYS.toMillis(days);
		long hours = TimeUnit.MILLISECONDS.toHours(millis);
		millis -= TimeUnit.HOURS.toMillis(hours);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
		millis -= TimeUnit.MINUTES.toMillis(minutes);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

		StringBuilder sb = new StringBuilder(64);
		if (days > 0) {
			sb.append(days);
			sb.append(" day" + (days == 1 ? "" : "s") + " ");
		}
		if (hours > 0) {
			sb.append(hours);
			sb.append(" hour" + (hours == 1 ? "" : "s") + " ");
		}
		if (minutes > 0) {
			sb.append(minutes);
			sb.append(" minute" + (minutes == 1 ? "" : "s") + " ");
		}
		if (seconds > 0) {
			sb.append(seconds);
			sb.append(" second" + (seconds == 1 ? "" : "s") + "");
		}
		return (sb.toString());
	}
}
