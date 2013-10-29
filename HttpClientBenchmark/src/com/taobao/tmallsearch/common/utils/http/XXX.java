package com.taobao.tmallsearch.common.utils.http;

import java.util.concurrent.ConcurrentHashMap;

public class XXX {
	public static void main(String[] args) throws InterruptedException {
		ConcurrentHashMap<String, String> chm = new ConcurrentHashMap<>();
		String a = chm.putIfAbsent("xiaolin", "mu");
		System.out.println(a);
	}
}
