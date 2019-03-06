package com.dutchjelly.craftenhance.messaging;

import com.dutchjelly.craftenhance.CraftEnhance;

public class Debug {
	
	public static void init(CraftEnhance main){
		enable = main.getConfig().getBoolean("enable-debug");
		prefix = main.getConfig().getString("debug-prefix");
	}
	
	private static boolean enable; //could be a config thing
	private static String prefix;
	
	public static void Send(Object obj){
		if(!enable) return;
		
		System.out.println(prefix + (obj != null ? obj.toString() : "null"));
	}
	
	public static void Send(Object sender, Object obj){
		if(!enable) return;
		
		System.out.println(prefix + "<" + sender.getClass().getName() + "> " + obj != null ? obj.toString() : "null");
	}
	
	public static void Send(Object[] arr){
		if(arr == null) return;
		System.out.print(prefix + " ");
		for(int i = 0; i < arr.length; i++){
			if(arr[i] == null) continue;
			System.out.print(arr[i].toString());
		}
		System.out.println();
	}
	
}
