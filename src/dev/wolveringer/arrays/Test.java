package dev.wolveringer.arrays;

import java.util.concurrent.TimeUnit;

public class Test {
	public static void main(String[] args) {
		CachedArrayList<String> list = new CachedArrayList<>(5, TimeUnit.SECONDS);
		list.add("Hello world (2)");
		list.add("Hello world (1)",3,TimeUnit.SECONDS);
		while (list.size() != 0) {
			System.out.println("List: "+list);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Empty");
	}
}
