package com.dutchjelly.craftenhance.commands.ceh;

import com.dutchjelly.craftenhance.commandhandling.ICommand;
import com.dutchjelly.craftenhance.commandhandling.CommandRoute;
import com.dutchjelly.craftenhance.commandhandling.CustomCmdHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandRoute(cmdPath="ceh.orderrecipes", perms="perms.recipe-editor")
public class OrderRecipesCmd implements ICommand {
	
	private CustomCmdHandler handler;
	
	public OrderRecipesCmd(CustomCmdHandler handler){
		this.handler = handler;
	}
	
	@Override
	public String getDescription() {
		return "The orderrecipes command opens a gui to order recipes in the recipes viewer gui. Players with the permission of recipe editing can order the recipes by left- or rightclickign them. The clicked recipe will shift respectively to the left and right. To open the gui use /ceh orderrecipes.";
	}

	@Override
	public void handlePlayerCommand(Player p, String[] args) {
		handler.getMain().getGUIContainer().openOrderEditor(p);
	}

	@Override
	public void handleConsoleCommand(CommandSender sender, String[] args) {
		handler.getMain().getMessenger().messageFromConfig("messages.commands.only-for-players", sender);
	}

}
