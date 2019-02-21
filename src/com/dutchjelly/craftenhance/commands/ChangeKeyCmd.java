package com.dutchjelly.craftenhance.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.dutchjelly.craftenhance.commandhandling.CmdInterface;
import com.dutchjelly.craftenhance.commandhandling.CustomCmd;
import com.dutchjelly.craftenhance.commandhandling.CustomCmdHandler;
import com.dutchjelly.craftenhance.util.Recipe;

@CustomCmd(cmdPath="ceh.changekey", perms="perms.recipe-editor")
public class ChangeKeyCmd implements CmdInterface {

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
		Recipe recipe = handler.getMain().getFileManager().getRecipe(args[0]);
		if(recipe == null) {
			handler.getMain().getMessenger().message("That recipe key doesn't exist", p);
			return;
		}
		handler.getMain().getFileManager().changeKey(recipe, args[1]);
		handler.getMain().getMessenger().message("The key has been changed to " + args[1] + ".", p);
	}

	@Override
	public void handleConsoleCommand(CommandSender sender, String[] args) {
		if(args.length != 2) {
			handler.getMain().getMessenger().messageFromConfig("messages.commands.few-arguments", sender, "2");
			return;
		}
		Recipe recipe = handler.getMain().getFileManager().getRecipe(args[0]);
		if(recipe == null) {
			handler.getMain().getMessenger().message("That recipe key doesn't exist", sender);
			return;
		}
		handler.getMain().getFileManager().changeKey(recipe, args[1]);
		handler.getMain().getMessenger().message("The key has been changed to " + args[1] + ".", sender);
	}
}
