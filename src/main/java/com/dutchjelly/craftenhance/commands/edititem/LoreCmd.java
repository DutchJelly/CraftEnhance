package com.dutchjelly.craftenhance.commands.edititem;

import com.dutchjelly.craftenhance.messaging.Messenger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.dutchjelly.craftenhance.commandhandling.ICommand;
import com.dutchjelly.craftenhance.commandhandling.CommandRoute;
import com.dutchjelly.craftenhance.commandhandling.CustomCmdHandler;
import com.dutchjelly.craftenhance.itemcreation.ItemCreator;
import com.dutchjelly.craftenhance.itemcreation.ParseResult;

@CommandRoute(cmdPath="edititem.lore", perms="perms.item-editor")
public class LoreCmd implements ICommand {

	
	private CustomCmdHandler handler;
	
	public LoreCmd(CustomCmdHandler handler){
		this.handler = handler;
	}
	
	@Override
	public String getDescription() {
		return "The lore command allows users to edit the lore of the item. The lore is basically the description below the item name and attributes when hovering over it. An example of the usage is /edititem lore 3 this example text will be put on line 3 of the lore of the held item.";
	}

	@Override
	public void handlePlayerCommand(Player p, String[] args) {
		ItemCreator creator = new ItemCreator(p.getInventory().getItemInHand(), args);
		ParseResult result = creator.setLore();
		p.getInventory().setItemInHand(creator.getItem());
		Messenger.Message(result.getMessage(), p);
	}

	@Override
	public void handleConsoleCommand(CommandSender sender, String[] args) {
		Messenger.MessageFromConfig("messages.commands.only-for-players", sender);
	}

}
