package com.dutchjelly.craftenhance.commands.edititem;

import com.dutchjelly.craftenhance.messaging.Messenger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.dutchjelly.craftenhance.commandhandling.ICommand;
import com.dutchjelly.craftenhance.commandhandling.CommandRoute;
import com.dutchjelly.craftenhance.commandhandling.CustomCmdHandler;
import com.dutchjelly.craftenhance.itemcreation.ItemCreator;
import com.dutchjelly.craftenhance.itemcreation.ParseResult;

@CommandRoute(cmdPath="edititem.itemflag", perms="perms.item-editor")
public class ItemFlagCmd implements ICommand {

	
	private CustomCmdHandler handler;
	
	public ItemFlagCmd(CustomCmdHandler handler){
		this.handler = handler;
	}
	
	@Override
	public String getDescription() {
		return "The itemflag command allows users to toggle itemflags of the held item. An example of the usage is /edititem itemflag hide_enchants hide_attributes. These itemflag names are documented in the bukkit documentation, google \"itemflags bukkit\", and the first result should contain a list of all itemflags and what they do.";
	}

	@Override
	public void handlePlayerCommand(Player p, String[] args) {
		ItemCreator creator = new ItemCreator(p.getInventory().getItemInHand(), args);
		ParseResult result = creator.setItemFlags();
		p.getInventory().setItemInHand(creator.getItem());
		Messenger.Message(result.getMessage(), p);
	}

	@Override
	public void handleConsoleCommand(CommandSender sender, String[] args) {
		Messenger.MessageFromConfig("messages.commands.only-for-players", sender);
	}

}
