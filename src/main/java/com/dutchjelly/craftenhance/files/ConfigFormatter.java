package com.dutchjelly.craftenhance.files;

import com.dutchjelly.craftenhance.CraftEnhance;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigFormatter {
	
	private CraftEnhance main;
	
	private ConfigFormatter(CraftEnhance main){
		this.main = main;
	}
	
	public static ConfigFormatter init(CraftEnhance main){
		return new ConfigFormatter(main);
	}
	
	//Gives all messages in the config the prefix and translates the colors.
	public void formatConfigMessages(){
		FileConfiguration config = main.getConfig();
		String path;
		giveColors(config, "global-prefix");
		for(String msgSection : config.getConfigurationSection("messages").getKeys(false)){
			path = "messages." + msgSection;
			for(String msgKey : config.getConfigurationSection(path).getKeys(false)){
				giveColors(config, path + "." + msgKey);
				givePrefix(config, path + "." + msgKey);
			}
		}
	}
		
	//Translate colors to the string on path in config.
	private void giveColors(FileConfiguration config, String path){
		config.set(path, ChatColor.translateAlternateColorCodes('&', config.getString(path)));
	}
	
	//Add a prefix to the string on path in config.
	private void givePrefix(FileConfiguration config, String path){
		config.set(path, config.getString("global-prefix") + config.getString(path));
	}
}
