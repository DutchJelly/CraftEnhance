package com.dutchjelly.craftenhance.commands.ceh;

import com.dutchjelly.craftenhance.commandhandling.ICommand;
import com.dutchjelly.craftenhance.commandhandling.CommandRoute;
import com.dutchjelly.craftenhance.commandhandling.CustomCmdHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.dutchjelly.craftenhance.model.CraftRecipe;

@CommandRoute(cmdPath="ceh.setpermission", perms="perms.recipe-editor")
public class SetPermissionCmd implements ICommand {

	private CustomCmdHandler handler;
	
	public SetPermissionCmd(CustomCmdHandler handler){
		this.handler = handler;
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public void handlePlayerCommand(Player p, String[] args) {
		if(args.length != 2) {
			handler.getMain().getMessenger().messageFromConfig("messages.commands.few-arguments", p, "2");
			return;
		}
		CraftRecipe recipe = handler.getMain().getFileManager().getRecipe(args[0]);
		if(recipe == null) {
			handler.getMain().getMessenger().message("That recipe key doesn't exist", p);
			return;
		}
		recipe.setPerms(args[1]);
		handler.getMain().getFileManager().saveRecipe(recipe);
		handler.getMain().getMessenger().message("Successfully set the permissions of the recipe to " + args[1] + ".", p);
	}

	@Override
	public void handleConsoleCommand(CommandSender sender, String[] args) {
		handler.getMain().getMessenger().messageFromConfig("messages.commands.only-for-players", sender);
	}
}