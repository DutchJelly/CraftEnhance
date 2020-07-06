package com.dutchjelly.craftenhance.commands.ceh;

import com.dutchjelly.craftenhance.IEnhancedRecipe;
import com.dutchjelly.craftenhance.commandhandling.ICommand;
import com.dutchjelly.craftenhance.commandhandling.CommandRoute;
import com.dutchjelly.craftenhance.commandhandling.CustomCmdHandler;
import com.dutchjelly.craftenhance.crafthandling.RecipeLoader;
import com.dutchjelly.craftenhance.crafthandling.recipes.WBRecipe;
import com.dutchjelly.craftenhance.messaging.Messenger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
			Messenger.MessageFromConfig("messages.commands.few-arguments", p, "2");
			return;
		}
		IEnhancedRecipe recipe = handler.getMain().getFm().getRecipe(args[0]);
		if(recipe == null) {
			Messenger.Message("That recipe key doesn't exist", p);
			return;
		}

		//TODO this method of changing the key changes the order of the recipes, which is a weird side-effect, so resolve this

		handler.getMain().getFm().removeRecipe(recipe);
        RecipeLoader.getInstance().unloadRecipe(recipe);
		recipe.setKey(args[1]);
		handler.getMain().getFm().saveRecipe(recipe);
        RecipeLoader.getInstance().loadRecipe(recipe);
		Messenger.Message("The key has been changed to " + args[1] + ".", p);
	}

	@Override
	public void handleConsoleCommand(CommandSender sender, String[] args) {
		if(args.length != 2) {
			Messenger.MessageFromConfig("messages.commands.few-arguments", sender, "2");
			return;
		}
        IEnhancedRecipe recipe = handler.getMain().getFm().getRecipe(args[0]);
		if(recipe == null) {
			Messenger.Message("That recipe key doesn't exist", sender);
			return;
		}
        handler.getMain().getFm().removeRecipe(recipe);
        RecipeLoader.getInstance().unloadRecipe(recipe);
        recipe.setKey(args[1]);
        handler.getMain().getFm().saveRecipe(recipe);
        RecipeLoader.getInstance().loadRecipe(recipe);
        Messenger.Message("The key has been changed to " + args[1] + ".", sender);
	}
}
