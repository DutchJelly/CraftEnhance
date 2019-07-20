package com.dutchjelly.craftenhance.messaging;

import com.dutchjelly.craftenhance.CraftEnhance;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Logger;

public class Debug {
	
	public static void init(CraftEnhance main){
		enable = main.getConfig().getBoolean("enable-debug");
		prefix = main.getConfig().getString("debug-prefix");
		logger = main.getLogger();
	}

	private static Logger logger;
	private static boolean enable; //could be a config thing
	private static String prefix;
	
	public static void Send(Object obj){
		if(!enable) return;
		
		System.out.println(prefix + (obj != null ? obj.toString() : "null"));
	}
	
	public static void Send(Object sender, Object obj){
		if(!enable) return;
		
		logger.info(prefix + "<" + sender.getClass().getName() + "> " + obj != null ? obj.toString() : "null");
	}
	
	public static void Send(Object[] arr){
		if(arr == null) return;
		logger.info(prefix + " ");
		for(int i = 0; i < arr.length; i++){
			if(arr[i] == null) continue;
			logger.info(arr[i].toString());
		}
		logger.info("");
	}
}
