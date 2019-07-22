package com.dutchjelly.craftenhance.commands.ceh;

import com.dutchjelly.craftenhance.commandhandling.ICommand;
import com.dutchjelly.craftenhance.commandhandling.CommandRoute;
import com.dutchjelly.craftenhance.commandhandling.CustomCmdHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.dutchjelly.craftenhance.model.CraftRecipe;

@CommandRoute(cmdPath="ceh.changekey", perms="perms.recipe-editor")
public class ChangeKeyCmd implements ICommand {

	private CustomCmdHandler handler;
	
	public ChangeKeyCmd(CustomCmdHandler handler){
		this.handler = handler;
	}

	@Override
	public String getDescription() {
		return "With this command you can change the key of an existing recipe. The usage is /ceh changekey oldkey newkey.";
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
		handler.getMain().getFileManager().removeRecipe(recipe);
		recipe.setKey(args[1]);
		handler.getMain().getFileManager().saveRecipe(recipe);
		handler.getMain().getMessenger().message("The key has been changed to " + args[1] + ".", p);
	}

	@Override
	public void handleConsoleCommand(CommandSender sender, String[] args) {
		if(args.length != 2) {
			handler.getMain().getMessenger().messageFromConfig("messages.commands.few-arguments", sender, "2");
			return;
		}
		CraftRecipe recipe = handler.getMain().getFileManager().getRecipe(args[0]);
		if(recipe == null) {
			handler.getMain().getMessenger().message("That recipe key doesn't exist", sender);
			return;
		}
        handler.getMain().getFileManager().removeRecipe(recipe);
        recipe.setKey(args[1]);
        handler.getMain().getFileManager().saveRecipe(recipe);
		handler.getMain().getMessenger().message("The key has been changed to " + args[1] + ".", sender);
	}
}
