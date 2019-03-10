package com.dutchjelly.craftenhance.commands;

import com.dutchjelly.craftenhance.commandhandling.CmdInterface;
import com.dutchjelly.craftenhance.commandhandling.CustomCmd;
import com.dutchjelly.craftenhance.commandhandling.CustomCmdHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.dutchjelly.craftenhance.util.CraftRecipe;

@CustomCmd(cmdPath="ceh.specs", perms="perms.recipe-editor")
public class SpecsCommand implements CmdInterface {

	private CustomCmdHandler handler;
	
	public SpecsCommand(CustomCmdHandler handler){
		this.handler = handler;
	}

	@Override
	public String getDescription() {
		return "The view command opens an inventory that contains all available recipes for the sender of the command, unless it's configured to show all. The usage is /ceh view or /recipes";
	}

	@Override
	public void handlePlayerCommand(Player p, String[] args) {
		if(args.length != 1) {
			handler.getMain().getMessenger().messageFromConfig("messages.commands.few-arguments", p, "1");
			return;
		}
		CraftRecipe recipe = handler.getMain().getFileManager().getRecipe(args[0]);
		if(recipe == null) {
			handler.getMain().getMessenger().message("That recipe key doesn't exist", p);
			return;
		}
		handler.getMain().getMessenger().message("&fKey: &e" + recipe.getKey() + " &fPerms: &e" + recipe.getPerms(), p);
	}

	@Override
	public void handleConsoleCommand(CommandSender sender, String[] args) {
		handler.getMain().getMessenger().messageFromConfig("messages.commands.only-for-players", sender);
	}
}