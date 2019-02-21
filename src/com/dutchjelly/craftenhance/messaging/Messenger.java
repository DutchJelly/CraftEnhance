package com.dutchjelly.craftenhance.messaging;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.dutchjelly.craftenhance.CraftEnhance;

public class Messenger {
	
	private CraftEnhance main;
	private String prefix;
	
	
	public Messenger(CraftEnhance main){
		this.main = main;
		this.prefix = ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("global-prefix"));
	}
	
	public void message(String message, CommandSender sender){
		message = ChatColor.translateAlternateColorCodes('&', message);
		message = prefix + message;
		sendMessage(message, sender);
	}
	
	public void messageFromConfig(String path, CommandSender sender, String placeHolder){
		if(path == null || sender == null || placeHolder == null) return;
		String message = main.getConfig().getString(path).replace("[PLACEHOLDER]", placeHolder);
		sendMessage(message, sender);
	}
	
	public void messageFromConfig(String path, CommandSender sender){
		if(path == null || sender == null) return;
		String message = main.getConfig().getString(path);
		sendMessage(message, sender);
	}
	
	private void sendMessage(String s, CommandSender sender){
		if(s == null) s = "";
		sender.sendMessage(s);
	}
	
}
