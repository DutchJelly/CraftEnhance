package com.dutchjelly.craftenhance.commands.ceh;

import com.dutchjelly.craftenhance.CraftEnhance;
import com.dutchjelly.craftenhance.IEnhancedRecipe;
import com.dutchjelly.craftenhance.PermissionTypes;
import com.dutchjelly.craftenhance.commandhandling.ICommand;
import com.dutchjelly.craftenhance.commandhandling.CommandRoute;
import com.dutchjelly.craftenhance.commandhandling.CustomCmdHandler;
import com.dutchjelly.craftenhance.crafthandling.RecipeLoader;
import com.dutchjelly.craftenhance.crafthandling.recipes.WBRecipe;
import com.dutchjelly.craftenhance.gui.guis.RecipesViewer;
import com.dutchjelly.craftenhance.gui.templates.GuiTemplate;
import com.dutchjelly.craftenhance.messaging.Messenger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@CommandRoute(cmdPath={"recipes","ceh.viewer"}, perms="perms.recipe-viewer")
public class RecipesCmd implements ICommand {

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
		final CraftEnhance main = handler.getMain();
        final GuiTemplate template = main.getGuiTemplatesFile().getTemplate(RecipesViewer.class);
		final List<IEnhancedRecipe> recipes = RecipeLoader.getInstance().getLoadedRecipes().stream().filter(x ->
				(!handler.getMain().getConfig().getBoolean("only-show-available") || x.getPermissions() == null || x.getPermissions() == "" || p.hasPermission(x.getPermissions()))
				&& (!x.isHidden() || p.hasPermission(PermissionTypes.Edit.getPerm()) || p.hasPermission(x.getPermissions() + ".hidden"))).collect(Collectors.toList()
        );
		final RecipesViewer gui = new RecipesViewer(main.getGuiManager(), template, null, p, new ArrayList<>(recipes));

		if(args.length == 1){
		    try{
                int pageIndex = Integer.valueOf(args[0]);
                gui.setPage(pageIndex); //setpage will handle invalid indexes and will jump to the nearest valid page
            }catch(NumberFormatException e){
		        p.sendMessage("that's not a number");
            }
        }
		handler.getMain().getGuiManager().openGUI(p, gui);
	}

	@Override
	public void handleConsoleCommand(CommandSender sender, String[] args) {
		Messenger.MessageFromConfig("messages.commands.only-for-players", sender);
	}
	

}