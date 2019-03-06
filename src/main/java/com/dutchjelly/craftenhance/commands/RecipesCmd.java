package com.dutchjelly.craftenhance.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.dutchjelly.craftenhance.commandhandling.CmdInterface;
import com.dutchjelly.craftenhance.commandhandling.CustomCmd;
import com.dutchjelly.craftenhance.commandhandling.CustomCmdHandler;

@CustomCmd(cmdPath={"recipes","ceh.viewer"}, perms="perms.recipe-viewer")
public class RecipesCmd implements CmdInterface {

	private CustomCmdHandler handler;
	
	public RecipesCmd(CustomCmdHandler handler){
		this.handler = handler;
	}

	@Override
	public String getDescription() {
		return "The view command opens an inventory that contains all available recipes for the sender of the command, unless it's configured to show all. The usage is /ceh view or /recipes";
	}

	@Override
	public void handlePlayerCommand(Player p, String[] args) {
		handler.getMain().getGUIContainer().openRecipesViewer(p);
	}

	@Override
	public void handleConsoleCommand(CommandSender sender, String[] args) {
		handler.getMain().getMessenger().messageFromConfig("messages.commands.only-for-players", sender);
	}
	

}