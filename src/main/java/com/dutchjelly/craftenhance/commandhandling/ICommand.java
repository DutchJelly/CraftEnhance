package com.dutchjelly.craftenhance.commandhandling;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface ICommand {
	
	public String getDescription();
	
	public void handlePlayerCommand(Player p, String[] args);
	
	public void handleConsoleCommand(CommandSender sender, String[] args);
	
}
