package com.dutchjelly.craftenhance.messaging;

import com.dutchjelly.craftenhance.CraftEnhance;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Messenger {
	
	private static CraftEnhance plugin;
	private static String prefix;

	public static void Init(CraftEnhance plugin){
		Messenger.plugin = plugin;
		Messenger.prefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("global-prefix"));
	}

	public static void Message(String message){
        Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', message));
    }

	public static void Message(String message, CommandSender sender) {
		if(sender == null) {
		    Message(message);
		    return;
        }
        message = ChatColor.translateAlternateColorCodes('&', message);
        message = prefix + message;
        SendMessage(message, sender);
    }

	public static void MessageFromConfig(String path, CommandSender sender, String placeHolder){
		if(path == null || sender == null || placeHolder == null) return;
		String message = plugin.getConfig().getString(path).replace("[PLACEHOLDER]", placeHolder);
		SendMessage(message, sender);
	}
	
	public static void MessageFromConfig(String path, CommandSender sender){
		if(path == null || sender == null) return;
		String message = plugin.getConfig().getString(path);
		SendMessage(message, sender);
	}
	
	private static void SendMessage(String s, CommandSender sender){
		if(s == null) s = "";
		sender.sendMessage(s);
	}

	public static void Error(String error){
		Message("&4&lError&r -- " + error);
	}

}
