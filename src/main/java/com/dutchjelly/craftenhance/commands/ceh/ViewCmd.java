package com.dutchjelly.craftenhance.commands.ceh;

import com.dutchjelly.craftenhance.commandhandling.CommandRoute;
import com.dutchjelly.craftenhance.commandhandling.CustomCmdHandler;
import com.dutchjelly.craftenhance.commandhandling.ICommand;
import com.dutchjelly.craftenhance.crafthandling.RecipeLoader;
import com.dutchjelly.craftenhance.crafthandling.recipes.EnhancedRecipe;
import com.dutchjelly.craftenhance.crafthandling.recipes.FurnaceRecipe;
import com.dutchjelly.craftenhance.crafthandling.recipes.RecipeType;
import com.dutchjelly.craftenhance.crafthandling.recipes.WBRecipe;
import com.dutchjelly.craftenhance.gui.GuiManager;
import com.dutchjelly.craftenhance.gui.guis.viewers.FurnaceRecipeViewer;
import com.dutchjelly.craftenhance.gui.guis.viewers.WBRecipeViewer;
import com.dutchjelly.craftenhance.messaging.Debug;
import com.dutchjelly.craftenhance.messaging.Messenger;
import com.dutchjelly.craftenhance.util.PermissionTypes;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

@CommandRoute(cmdPath={"ceh.view"}, perms="perms.recipe-viewer")
public class ViewCmd implements ICommand {

	private CustomCmdHandler handler;

	public ViewCmd(CustomCmdHandler handler){
		this.handler = handler;
	}

	@Override
	public String getDescription() {
		return "View a recipe with the specified key.";
	}

	@Override
	public void handlePlayerCommand(Player p, String[] args) {
		if(args.length == 0) {
			Messenger.Message("Please specify a recipe key.", p);
			return;
		}
        final EnhancedRecipe specifiedRecipe = RecipeLoader.getInstance().getLoadedRecipes().stream().filter(x ->
				x.getKey().equals(args[0])
				&& (!x.isHidden() || p.hasPermission(PermissionTypes.Edit.getPerm()) || p.hasPermission(x.getPermissions() + ".hidden"))
				&& (x.getPermissions() == null || x.getPermissions().equals("") || p.hasPermission(x.getPermissions()))
		).findFirst().orElse(null);
        if(specifiedRecipe == null) {
        	Messenger.Message("There's no recipe (or you cannot see the recipe) with key " + args[0] + ".", p);
        	return;
		}

		final GuiManager manager = handler.getMain().getGuiManager();

		if(specifiedRecipe.getType() == RecipeType.WORKBENCH)
			manager.openGUI(p, new WBRecipeViewer(manager, null, p, (WBRecipe)specifiedRecipe));
		if(specifiedRecipe.getType() == RecipeType.FURNACE)
			manager.openGUI(p, new FurnaceRecipeViewer(manager, null, p, (FurnaceRecipe)specifiedRecipe));
		else Debug.Send("Could not find the class of a clicked recipe.");

	}

	@Override
	public void handleConsoleCommand(CommandSender sender, String[] args) {
		if(args.length != 2) {
			Messenger.Message("Please specify a recipe key and player name.");
			return;
		}
		Player pl = Bukkit.getPlayer(args[1]);
		if(pl == null) pl = Bukkit.getPlayer(UUID.fromString(args[1]));
		if(pl == null) {
			Messenger.Message("No (online) player exists with name or UUID " + args[1], sender);
			return;
		}


		final Player p = pl; //final for lambda :(

		final EnhancedRecipe specifiedRecipe = RecipeLoader.getInstance().getLoadedRecipes().stream().filter(x ->
				x.getKey().equals(args[0])
						&& (!x.isHidden() || p.hasPermission(PermissionTypes.Edit.getPerm()) || p.hasPermission(x.getPermissions() + ".hidden"))
						&& (x.getPermissions() == null || x.getPermissions().equals("") || p.hasPermission(x.getPermissions()))
		).findFirst().orElse(null);

		if(specifiedRecipe == null) {
			Messenger.Message("There's no recipe with the key " + args[0] + " or the player cannot access it.", sender);
			return;
		}

		final GuiManager manager = handler.getMain().getGuiManager();

		if(specifiedRecipe.getType() == RecipeType.WORKBENCH)
			manager.openGUI(p, new WBRecipeViewer(manager, null, p, (WBRecipe)specifiedRecipe));
		if(specifiedRecipe.getType() == RecipeType.FURNACE)
			manager.openGUI(p, new FurnaceRecipeViewer(manager, null, p, (FurnaceRecipe)specifiedRecipe));
		else Debug.Send("Could not find the class of a clicked recipe.");
	}
	

}