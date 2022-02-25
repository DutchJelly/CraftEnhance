package com.dutchjelly.craftenhance.commands.edititem;

import com.dutchjelly.craftenhance.messaging.Messenger;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.dutchjelly.craftenhance.commandhandling.ICommand;
import com.dutchjelly.craftenhance.commandhandling.CommandRoute;
import com.dutchjelly.craftenhance.commandhandling.CustomCmdHandler;
import com.dutchjelly.craftenhance.itemcreation.ItemCreator;
import com.dutchjelly.craftenhance.itemcreation.ParseResult;

@CommandRoute(cmdPath="edititem.durability", perms="perms.item-editor")
public class DurabilityCmd implements ICommand {

	
	private CustomCmdHandler handler;
	
	public DurabilityCmd(CustomCmdHandler handler){
		this.handler = handler;
	}
	
	@Override
	public String getDescription() {
		return "The durability command allows players to edit the durability of items in their main hand. An example of the usage is \"/edititem durability 50\". This will set the durability of the item in the command sender to 50%.";
	}

	@Override
	public void handlePlayerCommand(Player p, String[] args) {
		ItemCreator creator = new ItemCreator(p.getInventory().getItemInHand(), args);
		ParseResult result = creator.setDurability();
		p.getInventory().setItemInHand(creator.getItem());
		Messenger.Message(result.getMessage(), p);
	}

	@Override
	public void handleConsoleCommand(CommandSender sender, String[] args) {
		Messenger.MessageFromConfig("messages.commands.only-for-players", sender);
	}

}
